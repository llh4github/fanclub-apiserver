package resp

// ImageUploadCredential 图片上传凭证响应
type ImageUploadCredential struct {
	// 上传凭证
	Token string `json:"token"`
	// 对象存储路径
	ObjectKey string `json:"object_key"`
	// 存储区域
	Region string `json:"region"`
	// 过期时间（Unix 时间戳）
	ExpiresAt int64 `json:"expires_at"`
}
