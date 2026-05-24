package model

import "time"

// TreeholeTopic 树洞主题
type TreeholeTopic struct {
	BaseModel
	// 主播B站UID
	Bid int64 `json:"bid" gorm:"not null"`
	// 树洞主题标题
	Title string `json:"title" gorm:"type:varchar(125);not null"`
	// 树洞主题描述
	Description string `json:"description" gorm:"type:varchar(300)"`
	// 投稿开放时间窗口
	OpenAt time.Time `json:"open_at" gorm:"type:timestamptz;not null"`
	// 投稿关闭时间窗口
	CloseAt time.Time `json:"close_at" gorm:"type:timestamptz;not null"`
	// 是否启用
	IsActive bool `json:"is_active" gorm:"default:true;not null"`
	// 关联的投稿列表
	Submissions []TreeholeSubmission `json:"submissions,omitempty" gorm:"foreignKey:TopicID"`

	CreatorModifier
}

// TableName 指定表名
func (TreeholeTopic) TableName() string {
	return "treehole_topics"
}
