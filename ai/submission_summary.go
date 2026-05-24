package ai

import (
	"context"
	"errors"
	"fmt"
	"strings"
	"time"

	"fanclub-apiserver/cache"
	"fanclub-apiserver/database/generated"
	"fanclub-apiserver/database/model"
	"fanclub-apiserver/dto/resp"
	"fanclub-apiserver/errs"
	"fanclub-apiserver/g"

	"github.com/cloudwego/eino/schema"
	"go.uber.org/zap"
	"gorm.io/cli/gorm/typed"
	"gorm.io/gorm"
)

type submissionSummaryService struct{}

var SubmissionSummary = new(submissionSummaryService)

const summaryCacheTTL = 24 * time.Hour

type StreamCallback func(content string) error

func (s *submissionSummaryService) GetOrGenerateSummary(
	ctx context.Context,
	submissionID int64,
) (*resp.SubmissionSummary, error) {
	result, err := s.GetFromDB(ctx, submissionID)
	if err == nil && result != nil {
		return result, nil
	}

	cacheKey := s.getCacheKey(submissionID)
	if content, err := s.getFromCache(ctx, cacheKey); err == nil && content != "" {
		if err := s.saveToDB(ctx, submissionID, content); err != nil {
			g.Warn("Failed to save summary to DB from cache", zap.Error(err))
		}
		return &resp.SubmissionSummary{
			SubmissionID: submissionID,
			Content:      content,
			FromCache:    true,
		}, nil
	}

	content, err := s.generateSummary(ctx, submissionID)
	if err != nil {
		return nil, err
	}

	return &resp.SubmissionSummary{
		SubmissionID: submissionID,
		Content:      content,
		FromCache:    false,
	}, nil
}

func (s *submissionSummaryService) GetFromDB(
	ctx context.Context,
	submissionID int64,
) (*resp.SubmissionSummary, error) {
	var summary model.TreeholeSubmissionSummary
	q := typed.G[model.TreeholeSubmissionSummary](
		g.DB,
		generated.TreeholeSubmissionSummary.SubmissionID.Eq(submissionID),
	)
	if err := q.Scan(ctx, &summary); err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, errs.WrapError(err, "获取总结失败", string(errs.DataNotFound))
	}
	return &resp.SubmissionSummary{
		SubmissionID: submissionID,
		Content:      summary.Content,
		FromCache:    true,
	}, nil
}

func (s *submissionSummaryService) StreamGenerate(
	ctx context.Context,
	submissionID int64,
) (*resp.SubmissionSummary, error) {
	content, err := s.generateSummary(ctx, submissionID)
	if err != nil {
		return nil, err
	}

	return &resp.SubmissionSummary{
		SubmissionID: submissionID,
		Content:      content,
		FromCache:    false,
	}, nil
}

func (s *submissionSummaryService) StreamGenerateWithCallback(
	ctx context.Context,
	submissionID int64,
	callback StreamCallback,
) error {
	submission, err := s.getSubmission(ctx, submissionID)
	if err != nil {
		return err
	}

	if submission.ContentMarkdown == "" {
		return fmt.Errorf("投稿内容为空")
	}

	prompt := s.buildPrompt(submission.ContentMarkdown)
	messages := []*schema.Message{
		schema.SystemMessage(prompt),
	}
	g.Debug("AI generate summary", zap.Any("messages", messages))

	stream, err := s.streamGenerate(ctx, messages)
	if err != nil {
		return fmt.Errorf("调用AI生成总结失败: %w", err)
	}
	defer stream.Close()

	cacheKey := s.getCacheKey(submissionID)
	if err := s.initCache(ctx, cacheKey); err != nil {
		g.Warn("Failed to init cache", zap.Error(err))
	}

	var content strings.Builder
	for {
		chunk, err := stream.Recv()
		if err != nil {
			break
		}
		content.WriteString(chunk.Content)
		if err := s.appendToCache(ctx, cacheKey, chunk.Content); err != nil {
			g.Warn("Failed to append to cache", zap.Error(err))
		}
		if callback != nil {
			if err := callback(chunk.Content); err != nil {
				return fmt.Errorf("SSE回调失败: %w", err)
			}
		}
	}

	finalContent := content.String()
	if err := s.saveToDB(ctx, submissionID, finalContent); err != nil {
		return fmt.Errorf("保存总结到数据库失败: %w", err)
	}

	return nil
}

func (s *submissionSummaryService) generateSummary(
	ctx context.Context,
	submissionID int64,
) (string, error) {
	submission, err := s.getSubmission(ctx, submissionID)
	if err != nil {
		return "", err
	}

	if submission.ContentMarkdown == "" {
		return "", fmt.Errorf("投稿内容为空")
	}

	prompt := s.buildPrompt(submission.ContentMarkdown)
	messages := []*schema.Message{
		schema.SystemMessage(prompt),
	}

	stream, err := s.streamGenerate(ctx, messages)
	if err != nil {
		return "", fmt.Errorf("调用AI生成总结失败: %w", err)
	}
	defer stream.Close()

	var content strings.Builder
	cacheKey := s.getCacheKey(submissionID)

	if err := s.initCache(ctx, cacheKey); err != nil {
		g.Warn("Failed to init cache", zap.Error(err))
	}

	for {
		chunk, err := stream.Recv()
		if err != nil {
			break
		}
		content.WriteString(chunk.Content)
		if err := s.appendToCache(ctx, cacheKey, chunk.Content); err != nil {
			g.Warn("Failed to append to cache", zap.Error(err))
		}
	}

	finalContent := content.String()
	if err := s.saveToDB(ctx, submissionID, finalContent); err != nil {
		return "", fmt.Errorf("保存总结到数据库失败: %w", err)
	}

	return finalContent, nil
}

func (s *submissionSummaryService) buildPrompt(content string) string {
	return fmt.Sprintf(`你是一个专业的学习助手。请分析以下投稿内容，用初高中学生能理解的语言进行总结。

投稿内容：
%s

请用 Markdown 格式输出总结，包括：
1. 内容概述（1-2句话，简洁易懂）
2. 简要评价或启示（1句话）
3. 重难点词汇（3-5个）：列出文中的重要词汇或概念，每个词汇用一句话简要说明含义或用法

要求：
- 总结控制在120字以内（不含重难点词汇部分）
- 使用通俗易懂的语言，避免过于专业的术语
- 重难点词汇说明控制在50字以内
- 使用中文输出`, content)
}

func (s *submissionSummaryService) streamGenerate(
	ctx context.Context,
	messages []*schema.Message,
) (*schema.StreamReader[*schema.Message], error) {
	if ChatModel == nil {
		return nil, fmt.Errorf("AI 模型未初始化")
	}
	return ChatModel.Stream(ctx, messages)
}

func (s *submissionSummaryService) getSubmission(
	ctx context.Context,
	submissionID int64,
) (*model.TreeholeSubmission, error) {
	var submission model.TreeholeSubmission
	q := typed.G[model.TreeholeSubmission](g.DB, generated.BaseModel.ID.Eq(submissionID))
	if err := q.Scan(ctx, &submission); err != nil {
		return nil, errs.WrapError(err, "获取投稿信息失败", string(errs.DataNotFound))
	}
	return &submission, nil
}

func (s *submissionSummaryService) getFromCache(
	ctx context.Context,
	key string,
) (string, error) {
	if g.Redis == nil {
		return "", fmt.Errorf("redis not available")
	}
	return g.Redis.Get(ctx, key).Result()
}

func (s *submissionSummaryService) initCache(
	ctx context.Context,
	key string,
) error {
	if g.Redis == nil {
		return nil
	}
	return g.Redis.Set(ctx, key, "", summaryCacheTTL).Err()
}

func (s *submissionSummaryService) appendToCache(
	ctx context.Context,
	key string,
	content string,
) error {
	if g.Redis == nil {
		return nil
	}
	return g.Redis.Append(ctx, key, content).Err()
}

func (s *submissionSummaryService) saveToDB(
	ctx context.Context,
	submissionID int64,
	content string,
) error {
	summary := &model.TreeholeSubmissionSummary{
		SubmissionID: submissionID,
		Content:      content,
	}

	q := typed.G[model.TreeholeSubmissionSummary](
		g.DB,
		generated.TreeholeSubmissionSummary.SubmissionID.Eq(submissionID),
	)

	var existing model.TreeholeSubmissionSummary
	if err := q.Scan(ctx, &existing); err == nil {
		summary.ID = existing.ID
		return g.DB.Save(summary).Error
	}

	return g.DB.Create(summary).Error
}

func (s *submissionSummaryService) getCacheKey(submissionID int64) string {
	return string(cache.TreeholeSubmissionSummary) + fmt.Sprintf("%d", submissionID)
}
