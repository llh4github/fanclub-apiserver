package req

// InitKeyExchange 初始化密钥交换请求
type InitKeyExchange struct {
}

// CompleteKeyExchange 完成密钥交换请求
type CompleteKeyExchange struct {
	// SessionID 会话ID
	SessionID string `json:"session_id" example:"abc123" validate:"required"`
	// EncryptedAESKey 用RSA公钥加密的AES密钥(base64编码)
	EncryptedAESKey string `json:"encrypted_aes_key" example:"SGVsbG8gV29ybGQh" validate:"required"`
}
