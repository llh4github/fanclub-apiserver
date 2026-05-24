package model

import (
	"fanclub-apiserver/consts"
	"time"

	"github.com/lib/pq"
)

// TreeholeSubmission 树洞投稿记录
type TreeholeSubmission struct {
	BaseModel
	// 稿件ID（由 sonyflake 生成，使用36进制编码）
	SubmissionID string `json:"submission_id" gorm:"type:varchar(50);uniqueIndex;not null"`
	// 关联的主题ID
	TopicID int64 `json:"topic_id" gorm:"not null;index:idx_submissions_query,sort:asc"`
	// 原始投稿内容 (Markdown格式)
	ContentMarkdown string `json:"content_markdown" gorm:"type:text;not null"`
	// 投稿内容 (Markdown转HTML后的内容)
	ContentHtml string `json:"content_html" gorm:"type:text;not null"`
	// 投稿摘要
	Summary string `json:"summary" gorm:"type:text;not null"`
	// 投稿时间
	SubmitTime time.Time `json:"submit_time" gorm:"type:timestamptz;not null;index:idx_submissions_query,sort:asc"`
	// 审核状态: 0=不宜展示, 1=未审核, 2=可以展示
	AuditStatus int `json:"audit_status" gorm:"default:1;not null"`
	// 关联的主题
	Topic *TreeholeTopic `json:"topic,omitempty" gorm:"foreignKey:TopicID"`
	// B 站 ID，通常称为 UID
	Bid int64 `json:"bid" gorm:"not null;index:idx_submissions_query,sort:asc"`
	// 修改者
	Modifier *SysUser `json:"modifier,omitempty" gorm:"foreignKey:ModifierID;references:ID;constraint:OnUpdate:CASCADE,OnDelete:SET NULL;"`
	// 修改者ID
	ModifierID *int64 `json:"modifier_id,string" gorm:"column:modifier_id"`
	// 图片链接（PostgreSQL 原生数组）
	ImageURLs pq.StringArray `json:"image_urls" gorm:"type:text[];default:'{}'"`
	// 图片链接是否已与 OSS 确认
	ImageURLsConfirmed bool `json:"image_urls_confirmed" gorm:"default:false;not null"`
	// AI 总结
	SummaryRecord *TreeholeSubmissionSummary `json:"summary_record,omitempty" gorm:"foreignKey:SubmissionID"`
}

// TableName 指定表名
func (TreeholeSubmission) TableName() string {
	return "treehole_submissions"
}

func (t *TreeholeSubmission) IsPending() bool {
	return t.AuditStatus == int(consts.AuditStatusPending)
}

func (t *TreeholeSubmission) IsApproved() bool {
	return t.AuditStatus == int(consts.AuditStatusApproved)
}

func (t *TreeholeSubmission) IsHidden() bool {
	return t.AuditStatus == int(consts.AuditStatusHidden)
}
