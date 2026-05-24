package ai

import (
	"context"
	"fanclub-apiserver/g"
	"fmt"
	"strings"
	"time"

	"github.com/cloudwego/eino-ext/components/model/ark"
	"github.com/cloudwego/eino/schema"
)

type scheduleItem struct {
	Topic     string    `json:"topic"`
	Emoji     string    `json:"emoji"`
	StartTime time.Time `json:"start_time"`
	EndTime   time.Time `json:"end_time"`
}

var ChatModel *ark.ChatModel

func InitArkClient() error {
	config := g.Cfg.ARK
	if config.APIKey == "" {
		return nil
	}

	chatModelConfig := &ark.ChatModelConfig{
		APIKey:  config.APIKey,
		Model:   config.ModelName,
		BaseURL: config.BaseURL,
	}

	if config.Timeout != nil {
		chatModelConfig.Timeout = config.Timeout
	}

	var err error
	ChatModel, err = ark.NewChatModel(context.Background(), chatModelConfig)
	if err != nil {
		return fmt.Errorf("failed to create ARK chat model: %w", err)
	}

	return nil
}

// GenerateSubmissionSummary 生成投稿总结（流式）
//
// 使用 AI 模型生成投稿内容的总结，以流式方式返回
//
// Parameters:
//   - ctx: 上下文
//   - content: 投稿内容（Markdown 格式）
//
// Returns:
//   - *schema.StreamReader[*schema.Message]: 流式读取器
//   - error: 错误信息
func GenerateSubmissionSummary(ctx context.Context, content string) (*schema.StreamReader[*schema.Message], error) {
	if ChatModel == nil {
		return nil, fmt.Errorf("AI 模型未初始化")
	}

	prompt := buildSummaryPrompt(content)
	messages := []*schema.Message{
		schema.SystemMessage(prompt),
	}

	return ChatModel.Stream(ctx, messages)
}

func buildSummaryPrompt(content string) string {
	return fmt.Sprintf(`你是一个专业的树洞投稿分析助手。请分析以下投稿内容，生成简洁的总结。

投稿内容：
%s

请用 Markdown 格式输出总结，包括：
1. 内容概述（1-2句话）
2. 主要观点或情感倾向
3. 关键词标签（3-5个，用逗号分隔）

要求：
- 总结控制在100字以内
- 使用客观、中性的语言
- 关键词标签使用中文`, content)
}

// ConcatStreamContent 将流式内容拼接为完整字符串
//
// Parameters:
//   - stream: 流式读取器
//
// Returns:
//   - string: 完整内容
//   - error: 错误信息
func ConcatStreamContent(stream *schema.StreamReader[*schema.Message]) (string, error) {
	if stream == nil {
		return "", nil
	}
	defer stream.Close()

	var content strings.Builder
	for {
		chunk, err := stream.Recv()
		if err != nil {
			break
		}
		content.WriteString(chunk.Content)
	}
	return content.String(), nil
}
