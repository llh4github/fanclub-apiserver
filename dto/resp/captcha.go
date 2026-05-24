package resp

// SlideCaptcha 滑动验证码响应
type SlideCaptcha struct {
	// 验证码Key
	CaptchaKey string `json:"captcha_key"`
	// 背景图片Base64
	ImageBase64 string `json:"image_base64"`
	// 滑块图片Base64
	TileBase64 string `json:"tile_base64"`
	// 滑块宽度
	TileWidth int `json:"tile_width"`
	// 滑块高度
	TileHeight int `json:"tile_height"`
	// 滑块X坐标
	TileX int `json:"tile_x"`
	// 滑块Y坐标
	TileY int `json:"tile_y"`
}

// ClickCaptcha 点选验证码响应
type ClickCaptcha struct {
	// 验证码Key
	CaptchaKey string `json:"captcha_key"`
	// 主图Base64
	MasterImage string `json:"master_image"`
	// 缩略图Base64
	ThumbImage string `json:"thumb_image"`
}

// CaptchaVerifyResult 验证码验证结果
type CaptchaVerifyResult struct {
	// 验证是否成功
	Success bool `json:"success"`
	// 验证码Token（验证成功后返回）
	Token string `json:"token"`
}
