package req

// CookieListReq Cookie 列表查询请求
type CookieListReq struct {
	// 每页数量
	PageSize int `json:"page_size" form:"page_size" query:"page_size" example:"10" validate:"omitempty,min=1,max=100"`
	// 页码
	PageIndex int `json:"page_index" form:"page_index" query:"page_index" example:"0" validate:"omitempty,min=0"`
	// Cookie 类型: scraper(爬虫) 或 user(普通用户)
	CookieType string `json:"cookie_type" form:"cookie_type" query:"cookie_type" example:"scraper" validate:"omitempty,oneof=scraper user ''"`
	// 是否需要刷新
	NeedRefresh *bool `json:"need_refresh" form:"need_refresh" query:"need_refresh" example:"true"`
}

// CookieUpdateReq 更新 Cookie 请求
type CookieUpdateReq struct {
	// Cookie 名称
	Name string `json:"name" example:"SESSDATA" validate:"required,min=1,max=50"`
	// Cookie 值
	Value string `json:"value" example:"xxx" validate:"required,min=1,max=255"`
	// Cookie 所属域名
	Domain string `json:"domain" example:"bilibili.com" validate:"required,min=1,max=100"`
	// Cookie 过期时间戳 (毫秒)
	ExpiresAt *int64 `json:"expires_at,string" example:"1234567890000"`
	// Cookie 类型: scraper(爬虫) 或 user(普通用户)
	CookieType string `json:"cookie_type" example:"scraper" validate:"omitempty,oneof=scraper user ''"`
	// 是否需要刷新
	NeedRefresh bool `json:"need_refresh" example:"true"`
}

// CookieCreateReq 创建 Cookie 请求
type CookieCreateReq struct {
	// Cookie 名称
	Name string `json:"name" example:"SESSDATA" validate:"required,min=1,max=50"`
	// Cookie 值
	Value string `json:"value" example:"xxx" validate:"required,min=1,max=255"`
	// Cookie 所属域名
	Domain string `json:"domain" example:"bilibili.com" validate:"required,min=1,max=100"`
	// Cookie 过期时间戳 (毫秒)
	ExpiresAt *int64 `json:"expires_at,string" example:"1234567890000"`
	// cookie值所属用户UID
	UID int64 `json:"uid,string" example:"12345678" validate:"required,min=1"`
	// Cookie 类型: scraper(爬虫) 或 user(普通用户)
	CookieType string `json:"cookie_type" example:"scraper" validate:"omitempty,oneof=scraper user ''"`
}
