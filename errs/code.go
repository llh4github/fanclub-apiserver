package errs

type ErorrCode string

const (
	AuthFailed            ErorrCode = "AuthFailed"
	AuthTokenExpired      ErorrCode = "AuthTokenExpired"
	LoginFailed           ErorrCode = "LoginFailed"
	ReqParamValidFailed   ErorrCode = "ReqParamValidFailed"
	DataNotFound          ErorrCode = "DataNotFound"
	NotFound              ErorrCode = "NotFound"
	DataUdpateFailed      ErorrCode = "DataUdpateFailed"
	DataCreateFailed      ErorrCode = "DataCreateFailed"
	DataDeleteFailed      ErorrCode = "DataDeleteFailed"
	CaptchaGenerateFailed ErorrCode = "CaptchaGenerateFailed"
	CaptchaVerifyFailed   ErorrCode = "CaptchaVerifyFailed"
	RSAKeyGenerateFailed  ErorrCode = "RSAKeyGenerateFailed"
	RSAEncryptFailed      ErorrCode = "RSAEncryptFailed"
	RSADecryptFailed      ErorrCode = "RSADecryptFailed"
	AESKeyExchangeFailed  ErorrCode = "AESKeyExchangeFailed"
	AESEncryptFailed      ErorrCode = "AESEncryptFailed"
	AESDecryptFailed      ErorrCode = "AESDecryptFailed"
	TokenRefreshFailed    ErorrCode = "TokenRefreshFailed"
	PasswordUpdateFailed  ErorrCode = "PasswordUpdateFailed"
	PermissionDenied      ErorrCode = "PermissionDenied"
	RateLimitExceeded     ErorrCode = "RateLimitExceeded"
	ConcurrentOperation   ErorrCode = "ConcurrentOperation"
	UnkonwError           ErorrCode = "UnkonwError"
)

var (
	LoginError            = NewAppError("用户名或密码错误", LoginFailed)
	LoginStatusError      = NewAppError("登录状态失效，请重新登录", LoginFailed)
	CapchaError           = NewAppError("验证码错误或已过期", AuthFailed)
	NoBindAnchorError     = NewAppError("后台用户未绑定主播", AuthFailed)
	CaptchaGenerateErr    = NewAppError("生成验证码失败", CaptchaGenerateFailed)
	UserNotFoundError     = NewAppError("用户不存在", DataNotFound)
	OldPasswordError      = NewAppError("旧密码错误", PasswordUpdateFailed)
	PasswordUpdateErr     = NewAppError("密码修改失败", PasswordUpdateFailed)
	PermissionDeniedError = NewAppError("权限不足，无法执行此操作", PermissionDenied)
	TokenExpiredError     = NewAppError("登录已过期，请重新登录", AuthTokenExpired)
)
