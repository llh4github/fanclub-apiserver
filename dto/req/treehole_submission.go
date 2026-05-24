package req

// CreateTreeholeSubmission 创建树洞投稿请求
type CreateTreeholeSubmission struct {
	// 关联的主题ID
	TopicID int64 `json:"topic_id,string" example:"123" validate:"required,min=1"`
	// 投稿内容（Markdown格式）
	Content string `json:"content" example:"# 标题\n\n这是一段投稿内容..." validate:"required,min=1"`
	// 验证码 Token，来自于验证码校验接口成功响应数据
	CaptchaToken string `json:"captcha_token" example:"abc123" validate:"required"`
}

// PageTreeholeSubmissionAdmin 后台分页查询投稿请求
type PageTreeholeSubmissionAdmin struct {
	// 主题ID（可选）
	TopicID int64 `json:"topic_id,string" form:"topic_id" query:"topic_id" example:"123" validate:"omitempty,min=0"`
	// 投稿ID（可选，精确查询）
	SubmissionID string `json:"submission_id" form:"submission_id" query:"submission_id" example:"4nrc8h7k9p2m" validate:"omitempty"`
	// 审核状态（可选）：0=不宜展示, 1=未审核, 2=可以展示
	AuditStatus *int `json:"audit_status" form:"audit_status" query:"audit_status" example:"1" validate:"omitempty,min=0,max=2"`
	// 分页参数
	PageParam
}

// GetTreeholeSubmission 查询投稿详情请求
type GetTreeholeSubmission struct {
	// 投稿ID
	SubmissionID string `json:"submission_id" form:"submission_id" query:"submission_id" example:"4nrc8h7k9p2m" validate:"required"`
}

// UpdateSubmissionAuditStatus 修改投稿审核状态请求
type UpdateSubmissionAuditStatus struct {
	// 投稿数据ID（主键）
	ID int64 `json:"id,string" example:"123456789" validate:"required,min=1"`
	// 审核状态：0=不宜展示, 2=可以展示（不支持修改为1未审核状态）
	AuditStatus int `json:"audit_status" example:"2" validate:"gte=0,lte=2,ne=1"`
}

// NavigateTreeholeSubmission 树洞投稿导航请求（每次只查一条）
type NavigateTreeholeSubmission struct {
	// 主题ID
	TopicID int64 `json:"topic_id,string" query:"topic_id" example:"123" validate:"required,min=1"`
	// 页码（从 0 开始）
	PageIndex int `json:"page_index" query:"page_index" example:"0" validate:"omitempty,min=0"`
	// 是否仅查询通过审查的（默认 false，包含已通过和待审核的）
	OnlyApproved bool `json:"only_approved" query:"only_approved" example:"false"`
}

// GetSubmissionSummary 获取投稿总结请求
type GetSubmissionSummary struct {
	// 投稿数据ID
	SubmissionID int64 `json:"submission_id,string" query:"submission_id" example:"123456789" validate:"required,min=1"`
}
