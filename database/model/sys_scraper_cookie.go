package model

// SysScraperCookie 爬虫 Cookie 配置表
type SysScraperCookie struct {
	BaseModel
	// Cookie 名称
	Name string `json:"name" gorm:"type:varchar(50);not null;uniqueIndex:uk_uid_name_domain"`
	// Cookie 值
	Value string `json:"value" gorm:"type:varchar(255);not null"`
	// Cookie 所属域名
	Domain string `json:"domain" gorm:"type:varchar(100);not null;uniqueIndex:uk_uid_name_domain"`
	// Cookie 过期时间戳 (毫秒)
	ExpiresAt *int64 `json:"expires_at" gorm:"check:expires_at >= 0"`
	// cookie值所属用户
	UID int64 `json:"uid,string" gorm:"not null;uniqueIndex:uk_uid_name_domain"`
	// 是否需要刷新
	NeedRefresh bool `json:"need_refresh" gorm:"default:false;not null"`
	// 上次刷新时间
	LastRefreshTime *int64 `json:"last_refresh_time" gorm:"type:bigint"`
}

// TableName 指定表名
func (SysScraperCookie) TableName() string {
	return "sys_scraper_cookie"
}
