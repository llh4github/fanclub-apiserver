package req

// GetUserRequest 获取用户请求参数
type GetUserRequest struct {
	// 用户ID
	ID int `json:"id,string" swagger:"required,description=用户ID"`
}

// UpdatePassword 修改密码请求
type UpdatePassword struct {
	// 会话ID，用于AES解密
	SessionID string `json:"session_id" example:"abc123" validate:"required"`
	// 用户ID
	UserID int64 `json:"user_id" example:"123" validate:"required,min=1"`
	// 新密码（AES加密）
	NewPassword string `json:"new_password" example:"encrypted_new_password" validate:"required,min=6,max=60"`
}
