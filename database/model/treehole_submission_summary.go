package model

// TreeholeSubmissionSummary AI 对投稿内容的总结记录
type TreeholeSubmissionSummary struct {
	BaseModel
	// 关联的投稿数据ID（外键，引用 treehole_submissions.id）
	SubmissionID int64 `json:"submission_id,string" gorm:"uniqueIndex;not null"`
	// AI 总结内容（Markdown 格式）
	Content string `json:"content" gorm:"type:text;not null"`
}

func (TreeholeSubmissionSummary) TableName() string {
	return "treehole_submission_summaries"
}
