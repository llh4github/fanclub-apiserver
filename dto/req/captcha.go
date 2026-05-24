package req

// VerifyClickCaptcha 点选验证码验证请求
type VerifyClickCaptcha struct {
	// 验证码缓存键
	CaptchaKey string `json:"captcha_key" example:"abc123" validate:"required"`
	// 用户点击的坐标列表，格式为 "x1,y1;x2,y2;..."
	Dots string `json:"dots" example:"120,50;200,80" validate:"required"`
	// 验证码使用场景 (login/submission)
	Scene string `json:"scene" example:"login" validate:"required,oneof=login submission"`
}

// VerifySlideCaptcha 滑动验证码验证请求
type VerifySlideCaptcha struct {
	// 验证码Key
	CaptchaKey string `json:"captcha_key" example:"abc123" validate:"required"`
	// 用户滑动距离
	SlideDistance int `json:"slide_distance" example:"50" validate:"required,min=0"`
	// 验证码使用场景 (login/submission)
	Scene string `json:"scene" example:"login" validate:"required,oneof=login submission"`
}
