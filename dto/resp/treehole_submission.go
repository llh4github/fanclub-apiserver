package resp

import "time"

// CreateSubmission 创建投稿响应
type CreateSubmission struct {
	// 稿件ID
	SubmissionID string `json:"submission_id"`
}

// SubmissionDetail 投稿详情（公开接口返回值）
type SubmissionDetail struct {
	// 数据ID
	ID int64 `json:"id"`
	// 稿件ID
	SubmissionID string `json:"submission_id"`
	// 投稿内容（HTML格式）
	ContentHtml string `json:"content_html"`
	// 投稿时间
	SubmitTime time.Time `json:"submit_time"`
}

// SubmissionListItem 列表项结构（排除大文本字段）
type SubmissionListItem struct {
	// 主键ID
	ID int64 `json:"id,string"`
	// 稿件ID
	SubmissionID string `json:"submission_id"`
	// 主题ID
	TopicID int64 `json:"topic_id"`
	// 投稿摘要
	Summary string `json:"summary"`
	// 投稿时间
	SubmitTime time.Time `json:"submit_time"`
	// 审核状态: 0=不宜展示, 1=未审核, 2=可以展示
	AuditStatus int `json:"audit_status"`
}

// TreeholeSubmissionNavItem 导航列表项（只包含必要字段）
type TreeholeSubmissionNavItem struct {
	// 数据ID
	ID int64 `json:"id,string"`
	// 稿件ID
	SubmissionID string `json:"submission_id"`
	// HTML内容
	ContentHtml string `json:"content_html"`
	// 投稿时间
	SubmitTime time.Time `json:"submit_time"`
	// 审核状态: 0=不宜展示, 1=未审核, 2=可以展示
	AuditStatus int `json:"audit_status"`
	// 是否有 AI 总结
	HasSummary bool `json:"has_summary"`
	// AI 总结记录ID（内部使用，用于判断是否存在，0表示不存在）
	SummarySubmissionID int64 `json:"-"`
}

// TreeholeSubmissionNavResp 树洞投稿导航响应
type TreeholeSubmissionNavResp struct {
	// 当前页码
	PageIndex int `json:"page_index"`
	// 审核通过的总投稿数
	TotalCount int64 `json:"total_count"`
	// 当前页投稿（每页固定一条）
	Record *TreeholeSubmissionNavItem `json:"record"`
}

// SubmissionSummary 投稿总结响应
type SubmissionSummary struct {
	// 投稿数据ID
	SubmissionID int64 `json:"submission_id,string"`
	// AI 总结内容（Markdown 格式）
	Content string `json:"content"`
	// 数据来源：true=缓存/数据库，false=AI生成
	FromCache bool `json:"from_cache"`
}
