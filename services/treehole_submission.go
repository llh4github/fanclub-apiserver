package services

import (
	"errors"
	"fmt"
	"time"

	"fanclub-apiserver/cache"
	"fanclub-apiserver/consts"
	"fanclub-apiserver/database/generated"
	"fanclub-apiserver/database/model"
	"fanclub-apiserver/dto/req"
	"fanclub-apiserver/dto/resp"
	"fanclub-apiserver/errs"
	"fanclub-apiserver/g"
	"fanclub-apiserver/utils"

	"gorm.io/cli/gorm/typed"
	"gorm.io/gorm"
	"gorm.io/gorm/clause"
)

var TreeholeSubmission = new(treeholeSubmissionService)

type treeholeSubmissionService struct {
}

// Create 创建树洞投稿
//
// 在同一事务内完成以下操作：
// 1. 查询主题（带行锁）
// 2. 验证主题状态和时间窗口
// 3. 处理 Markdown 内容
// 4. 创建投稿记录
//
// Parameters:
//   - appCtx: 应用上下文
//   - topicID: 主题ID
//   - markdownContent: Markdown 格式的投稿内容
//
// Returns:
//   - submissionID: 生成的稿件ID
//   - error: 错误信息
func (s *treeholeSubmissionService) Create(appCtx *g.AppCtx, topicID int64, markdownContent string) (string, error) {
	// 处理 Markdown 内容（在事务外处理，避免长时间持有锁）
	result, err := utils.ProcessMarkdown(markdownContent, utils.MinContentLength, utils.MaxContentLength)
	if err != nil {
		if errors.Is(err, utils.ErrContentTooShort) {
			return "", errs.WrapError(err, "投稿内容不得少于10字", string(errs.ReqParamValidFailed))
		}
		if errors.Is(err, utils.ErrContentTooLong) {
			return "", errs.WrapError(err, "投稿内容不得超过800字", string(errs.ReqParamValidFailed))
		}
		if errors.Is(err, utils.ErrTooManyImages) {
			return "", errs.WrapError(err, "图片数量不得超过5张", string(errs.ReqParamValidFailed))
		}
		return "", errs.WrapError(err, "处理投稿内容失败", string(errs.ReqParamValidFailed))
	}

	// 生成稿件 ID（在事务外生成）
	submissionID, err := g.NextIDStr()
	if err != nil {
		return "", errs.WrapError(err, "生成稿件ID失败", string(errs.DataCreateFailed))
	}

	// 在事务内完成验证和创建
	err = g.DB.WithContext(appCtx.C).Transaction(func(tx *gorm.DB) error {
		// 1. 查询主题
		var topic model.TreeholeTopic
		if err := tx.Where(generated.BaseModel.ID.Eq(int64(topicID))).
			First(&topic).Error; err != nil {
			return errs.WrapError(err, "树洞主题不存在", string(errs.DataNotFound))
		}

		// 2. 验证主题是否启用
		if !topic.IsActive {
			return errs.WrapError(nil, "该树洞主题已关闭", string(errs.ReqParamValidFailed))
		}

		// 3. 验证当前时间是否在投稿时间窗口内
		now := time.Now()
		if now.Before(topic.OpenAt) {
			return errs.WrapError(nil, "投稿尚未开始", string(errs.ReqParamValidFailed))
		}
		if now.After(topic.CloseAt) {
			return errs.WrapError(nil, "投稿已结束", string(errs.ReqParamValidFailed))
		}

		// 4. 创建投稿记录
		submission := &model.TreeholeSubmission{
			SubmissionID:    submissionID,
			TopicID:         int64(topicID),
			ContentMarkdown: markdownContent,
			ContentHtml:     result.HTMLContent,
			Summary:         result.Summary,
			SubmitTime:      now,
			AuditStatus:     int(consts.AuditStatusPending),
			Bid:             topic.Bid,
			ImageURLs:       result.ImageURLs,
		}

		q := typed.G[model.TreeholeSubmission](tx)
		if err := q.Create(appCtx.C, submission); err != nil {
			return errs.WrapError(err, "创建树洞投稿失败", string(errs.DataCreateFailed))
		}

		return nil
	})

	if err != nil {
		return "", err
	}

	return submissionID, nil
}

// PageAdmin 后台分页查询投稿列表
//
// 查询所有投稿，支持按主题ID、审核状态、投稿ID和BID筛选
// 根据角色自动添加 BID 筛选条件
// 排除大文本字段（ContentMarkdown、ContentHtml），只返回列表需要的字段
//
// Parameters:
//   - appCtx: 应用上下文
//   - topicID: 主题ID（可选，0表示不筛选）
//   - submissionID: 投稿ID（可选，空字符串表示不筛选）
//   - auditStatus: 审核状态（可选，nil表示不筛选）
//   - pageParam: 分页参数
//
// Returns:
//   - *resp.PageResult[resp.SubmissionListItem]: 分页结果
//   - error: 错误信息
func (s *treeholeSubmissionService) PageAdmin(appCtx *g.AppCtx, topicID int64, submissionID string, auditStatus *consts.AuditStatus, pageParam req.PageParam) (*resp.PageResult[resp.SubmissionListItem], error) {
	page := pageParam.PageIndex
	if page <= 0 {
		page = 1
	}
	pageSize := pageParam.PageSize
	if pageSize <= 0 {
		pageSize = 10
	}

	// 根据角色获取 BID 用于筛选
	bid, err := getBidForQuery(appCtx)
	if err != nil {
		return nil, err
	}

	var conds []clause.Expression
	if bid > 0 {
		conds = append(conds, generated.TreeholeSubmission.Bid.Eq(bid))
	}

	if topicID > 0 {
		conds = append(conds, generated.TreeholeSubmission.TopicID.Eq(int64(topicID)))
	}

	if submissionID != "" {
		conds = append(conds, generated.TreeholeSubmission.SubmissionID.Eq(submissionID))
	}

	if auditStatus != nil {
		conds = append(conds, generated.TreeholeSubmission.AuditStatus.Eq(int(*auditStatus)))
	}

	q := typed.G[model.TreeholeSubmission](g.DB, conds...)

	// 统计总数
	total, err := q.Count(appCtx.C, "id")
	if err != nil {
		return nil, errs.WrapError(err, "统计投稿数量失败", string(errs.DataNotFound))
	}

	if total == 0 {
		return &resp.PageResult[resp.SubmissionListItem]{
			TotalRowCount: 0,
			TotalPage:     0,
			Records:       []resp.SubmissionListItem{},
		}, nil
	}

	// 计算分页
	totalPage := (total + int64(pageSize) - 1) / int64(pageSize)
	offset := (page - 1) * pageSize

	// 查询列表，只选择需要的字段（排除大文本）
	var records []resp.SubmissionListItem
	if err := q.
		Select(
			generated.BaseModel.ID,
			generated.TreeholeSubmission.SubmissionID,
			generated.TreeholeSubmission.TopicID,
			generated.TreeholeSubmission.Summary,
			generated.TreeholeSubmission.SubmitTime,
			generated.TreeholeSubmission.AuditStatus,
		).
		Order(clause.OrderBy{Columns: []clause.OrderByColumn{{
			Column: clause.Column{Name: "submit_time"},
			Desc:   true,
		}}}).
		Offset(offset).
		Limit(pageSize).
		Scan(appCtx.C, &records); err != nil {
		return nil, errs.WrapError(err, "分页查询投稿失败", string(errs.DataNotFound))
	}

	return &resp.PageResult[resp.SubmissionListItem]{
		TotalRowCount: int(total),
		TotalPage:     int(totalPage),
		Records:       records,
	}, nil
}

// submissionDetailResult 投稿详情查询结果结构
type submissionDetailResult struct {
	ID           int64
	SubmissionID string
	ContentHtml  string
	SubmitTime   time.Time
}

// GetBySubmissionID 根据投稿ID获取投稿详情
//
// 仅返回审核通过的投稿详情
// 查不到数据返回 nil，不返回错误
// 使用缓存提高查询性能
//
// Parameters:
//   - appCtx: 应用上下文
//   - submissionID: 投稿ID
//
// Returns:
//   - *resp.SubmissionDetail: 投稿详情，nil表示未找到
//   - error: 错误信息（通常为 nil）
func (s *treeholeSubmissionService) GetBySubmissionID(appCtx *g.AppCtx, submissionID string) (*resp.SubmissionDetail, error) {
	cacheKey := fmt.Sprintf("%s%s", cache.TreeholeTopic, submissionID)

	return cache.CacheData(defaultCacheTTL, appCtx.C, cacheKey, func() (*resp.SubmissionDetail, error) {
		var result submissionDetailResult
		q := typed.G[model.TreeholeSubmission](g.DB)
		if err := q.
			Select(
				generated.BaseModel.ID,
				generated.TreeholeSubmission.SubmissionID,
				generated.TreeholeSubmission.ContentHtml,
				generated.TreeholeSubmission.SubmitTime,
			).
			Where(generated.TreeholeSubmission.SubmissionID.Eq(submissionID)).
			Where(generated.TreeholeSubmission.AuditStatus.Eq(int(consts.AuditStatusApproved))).
			Scan(appCtx.C, &result); err != nil {
			// 数据找不到不是错误，返回 nil
			if errors.Is(err, gorm.ErrRecordNotFound) {
				return nil, nil
			}
			return nil, errs.WrapError(err, "查询投稿详情失败", string(errs.DataNotFound))
		}

		// 检查是否找到记录
		if result.ID == 0 {
			return nil, nil
		}

		return &resp.SubmissionDetail{
			ID:           result.ID,
			SubmissionID: result.SubmissionID,
			ContentHtml:  result.ContentHtml,
			SubmitTime:   result.SubmitTime,
		}, nil
	})
}

// UpdateAuditStatusByID 根据ID修改审核状态
//
// 只有 anchor 角色才能修改，且只能修改自己 BID 下的投稿
//
// Parameters:
//   - appCtx: 应用上下文
//   - id: 投稿数据ID（主键）
//   - auditStatus: 新的审核状态（0=不宜展示, 2=可以展示）
//
// Returns:
//   - error: 错误信息
func (s *treeholeSubmissionService) UpdateAuditStatusByID(appCtx *g.AppCtx, id int64, auditStatus int) error {
	// 1. 验证用户角色
	if !appCtx.Role.IsAnchor() {
		return errs.WrapError(nil, "只有主播才能修改审核状态", string(errs.PermissionDenied))
	}

	// 2. 验证用户已绑定 BID
	if appCtx.BID == nil {
		return errs.NoBindAnchorError
	}

	return g.DB.WithContext(appCtx.C).Transaction(func(tx *gorm.DB) error {
		var submission model.TreeholeSubmission
		q := typed.G[model.TreeholeSubmission](tx)
		// 3. 查询时加入 bid 条件，避免修改他人投稿
		result, err := q.
			Where(generated.BaseModel.ID.Eq(id)).
			Where(generated.TreeholeSubmission.Bid.Eq(*appCtx.BID)).
			First(appCtx.C)
		if err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				return errs.WrapError(err, "投稿不存在或无权修改", string(errs.DataNotFound))
			}
			return errs.WrapError(err, "查询投稿失败", string(errs.DataNotFound))
		}
		submission = result

		updates := map[string]interface{}{
			"audit_status": auditStatus,
			"modifier_id":  appCtx.UserID,
		}
		if err := tx.Model(&submission).Updates(updates).Error; err != nil {
			return errs.WrapError(err, "更新审核状态失败", string(errs.DataUdpateFailed))
		}

		// 清除缓存
		cacheKey := fmt.Sprintf("%s%s", cache.TreeholeTopic, submission.SubmissionID)
		g.Redis.Del(appCtx.C, cacheKey)

		return nil
	})
}

// NavigateByTopicID 根据主题ID获取投稿列表（用于导航）
//
// 按 submit_time 和 id 升序排列，使用普通分页，pageSize 固定为 1
// 根据角色自动添加 BID 筛选条件
//
// Parameters:
//   - appCtx: 应用上下文
//   - topicID: 主题ID
//   - pageIndex: 页码（从 0 开始）
//   - onlyApproved: 是否仅查询通过审查的
//
// Returns:
//   - *resp.PageResult[resp.TreeholeSubmissionNavItem]: 分页结果
//   - error: 错误信息
func (s *treeholeSubmissionService) NavigateByTopicID(
	appCtx *g.AppCtx,
	topicID int64,
	pageIndex int,
	onlyApproved bool,
) (*resp.TreeholeSubmissionNavResp, error) {
	page := pageIndex
	if page <= 0 {
		page = 1
	}

	pageSize := 1

	bid, err := getBidForQuery(appCtx)
	if err != nil {
		return nil, err
	}

	conds := []clause.Expression{
		generated.TreeholeSubmission.TopicID.Eq(int64(topicID)),
	}

	if onlyApproved {
		conds = append(conds, generated.TreeholeSubmission.AuditStatus.Eq(int(consts.AuditStatusApproved)))
	} else {
		conds = append(conds, generated.TreeholeSubmission.AuditStatus.Neq(int(consts.AuditStatusHidden)))
	}

	if bid > 0 {
		conds = append(conds, generated.TreeholeSubmission.Bid.Eq(bid))
	}

	q := typed.G[model.TreeholeSubmission](g.DB, conds...)

	total, err := q.Count(appCtx.C, "id")
	if err != nil {
		return nil, errs.WrapError(err, "统计投稿数量失败", string(errs.DataNotFound))
	}

	if total == 0 {
		return &resp.TreeholeSubmissionNavResp{
			PageIndex:  page,
			TotalCount: 0,
			Record:     nil,
		}, nil
	}

	offset := (page - 1) * pageSize

	var record *resp.TreeholeSubmissionNavItem
	if err := q.
		Select(
			generated.BaseModel.ID.WithTable("treehole_submissions"),
			generated.TreeholeSubmission.SubmissionID.WithTable("treehole_submissions"),
			generated.TreeholeSubmission.ContentHtml,
			generated.TreeholeSubmission.SubmitTime,
			generated.TreeholeSubmission.AuditStatus,
			generated.BaseModel.ID.WithTable("SummaryRecord").As("summary_submission_id"),
		).
		Joins(clause.LeftJoin.Association("SummaryRecord"), func(_ typed.JoinBuilder, _ clause.Table, _ clause.Table) error {
			return nil
		}).
		Order(clause.OrderBy{Columns: []clause.OrderByColumn{
			{Column: clause.Column{Table: "treehole_submissions", Name: "submit_time"}, Desc: false},
			{Column: clause.Column{Table: "treehole_submissions", Name: "id"}, Desc: false},
		}}).
		Offset(offset).
		Limit(1).
		Scan(appCtx.C, &record); err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) || record == nil {
			return &resp.TreeholeSubmissionNavResp{
				PageIndex:  page,
				TotalCount: total,
				Record:     nil,
			}, nil
		}
		return nil, errs.WrapError(err, "查询投稿失败", string(errs.DataNotFound))
	}

	if record != nil {
		record.HasSummary = record.SummarySubmissionID != 0
	}

	return &resp.TreeholeSubmissionNavResp{
		PageIndex:  page,
		TotalCount: total,
		Record:     record,
	}, nil
}
