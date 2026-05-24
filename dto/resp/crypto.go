package resp

// InitKeyExchangeResp 初始化密钥交换响应
type InitKeyExchangeResp struct {
	// SessionID 会话ID，用于后续密钥交换
	SessionID string `json:"session_id"`
	// RSAPublicKey RSA公钥(PEM格式,base64编码)
	RSAPublicKey string `json:"rsa_public_key"`
	// ExpireAt 公钥过期时间(UNIX时间戳)
	ExpireAt int64 `json:"expire_at"`
}

// CompleteKeyExchangeResp 完成密钥交换响应
type CompleteKeyExchangeResp struct {
	// Success 是否成功
	Success bool `json:"success"`
	// KeyExpireAt 对称密钥过期时间(UNIX秒时间戳)
	KeyExpireAt int64 `json:"key_expire_at"`
}
