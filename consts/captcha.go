package consts

// CaptchaScene 验证码使用场景
type CaptchaScene string

const (
	// CaptchaSceneLogin 登录场景
	CaptchaSceneLogin CaptchaScene = "login"
	// CaptchaSceneSubmission 投稿场景
	CaptchaSceneSubmission CaptchaScene = "submission"
)
