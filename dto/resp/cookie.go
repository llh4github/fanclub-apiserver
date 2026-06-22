package resp

// CookieInfo Cookie 信息
type CookieInfo struct {
	// Cookie ID
	ID int64 `json:"id,string"`
	// Cookie 名称
	Name string `json:"name"`
	// Cookie 值（脱敏显示）
	Value string `json:"value"`
	// Cookie 所属域名
	Domain string `json:"domain"`
	// Cookie 过期时间戳 (毫秒)
	ExpiresAt *int64 `json:"expires_at,string"`
	// cookie值所属用户UID
	UID int64 `json:"uid,string"`
	// 是否需要刷新
	NeedRefresh bool `json:"need_refresh"`
	// 上次刷新时间
	LastRefreshTime *int64 `json:"last_refresh_time,string"`
	// 创建时间
	CreatedAt int64 `json:"created_at,string"`
	// 更新时间
	UpdatedAt int64 `json:"updated_at,string"`
}

// CookieRefreshResp Cookie 刷新结果
type CookieRefreshResp struct {
	// Cookie ID
	ID int64 `json:"id,string"`
	// 是否成功
	Success bool `json:"success"`
	// 刷新消息
	Message string `json:"message"`
	// 新的 RefreshToken
	NewRefreshToken string `json:"new_refresh_token,omitempty"`
}

// CookieBatchRefreshResp 批量刷新结果
type CookieBatchRefreshResp struct {
	// 总数
	Total int `json:"total"`
	// 成功数
	SuccessCount int `json:"success_count"`
	// 失败数
	FailedCount int `json:"failed_count"`
	// 详细结果
	Results []CookieRefreshResp `json:"results"`
}
