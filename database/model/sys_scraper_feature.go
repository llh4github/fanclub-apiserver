package model

// SysScraperFeature 爬虫功能配置
type SysScraperFeature struct {
	BaseModel
	// 是否获取粉丝数
	Follower bool `json:"follower" gorm:"default:true;not null"`
	// 主播 ID
	AnchorID int64 `json:"anchor_id" gorm:"not null;uniqueIndex:uk_anchor_id;check:anchor_id >= 0"`
	// 是否数据监控
	Monitor bool `json:"monitor" gorm:"default:true;not null"`
	// 关联的主播信息
	Anchor *AnchorInfo `json:"anchor,omitempty" gorm:"foreignKey:AnchorID"`
}

// TableName 指定表名
func (SysScraperFeature) TableName() string {
	return "sys_scraper_feature"
}
