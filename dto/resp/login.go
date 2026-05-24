package resp

import "time"

// Login 登录响应
type Login struct {
	// 用户 ID
	ID int64 `json:"id,string"`
	// 用户名
	Username string `json:"username"`
	// 访问令牌
	AccessToken string `json:"access_token"`
	// 刷新令牌（已迁移至 HttpOnly Cookie，不通过 JSON 返回）
	// 保留此字段便于服务层传递 token 给 Cookie 设置逻辑
	RefreshToken string `json:"-"`
	// aceess_token 过期时间
	ExpirationTime time.Time `json:"expiration_time"`
}
