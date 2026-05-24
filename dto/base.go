package dto

import "time"

// TokenPair 登录令牌对
type TokenPair struct {
	AccessToken  string `json:"access_token"`
	RefreshToken string `json:"refresh_token"`
	// aceess_token 过期时间
	ExpirationTime time.Time `json:"expiration_time"`
}
