package req

// GenerateImageUploadCredential 生成图片上传凭证请求
type GenerateImageUploadCredential struct {
	// 文件名（不含路径）
	Filename string `json:"filename" query:"filename" validate:"required"`
}
