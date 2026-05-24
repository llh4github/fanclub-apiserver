package req

// Login 登录请求
type Login struct {
	// SessionID 加密会话ID
	SessionID string `json:"session_id" example:"abc123" validate:"required"`
	// 用户名
	Username string `json:"username" example:"jerry" validate:"required,min=3,max=12"`
	// 密码
	Password string `json:"password" example:"12345qaz" validate:"required,min=6,max=120"`
	// 验证码 Token，来自于验证码校验接口成功响应数据
	CaptchaToken string `json:"captcha_token" example:"abc123" validate:"required"`
}

// RefreshToken 刷新令牌请求
type RefreshToken struct {
	// 刷新令牌
	RefreshToken string `json:"refresh_token" example:"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." validate:"required"`
}
