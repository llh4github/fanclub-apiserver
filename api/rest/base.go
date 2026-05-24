package rest

import (
	"regexp"

	"fanclub-apiserver/middleware"

	"github.com/go-playground/validator/v10"
	"github.com/gofiber/fiber/v3"
)

var validate = validator.New()

func init() {
	validate.RegisterValidation("regexp", func(fl validator.FieldLevel) bool {
		field := fl.Field()
		if field.String() == "" {
			return true
		}
		pattern := fl.Param()
		matched, _ := regexp.MatchString(pattern, field.String())
		return matched
	})
}

const errMsgInvalidParams = "请求参数不正确"
const errMsgInvalidBody = "请求体不正确"

// jwtHandler 需要验证jwt的接口加上这个
var jwtHandler = middleware.JwtHandler()

// RigsterRouter 注册路由
func RigsterRouter(router fiber.Router) {
	// 应用全局限流（最外层）：30次/分钟/IP
	router.Use(middleware.GlobalRateLimiter)

	// 验证码接口：10次/分钟/IP
	registerCaptcha(router)

	// 登录接口：5次/分钟/IP
	rigsterAuth(router)

	// 已认证接口（内部各自配置）
	rigsterAnchorSong(router)
	registerAnchorLiveSchedule(router)
	registerAnchorLiveRecord(router)
	registerTreeholeTopic(router)
	registerTreeholeSubmission(router)
	registerTreeholeSubmissionSummary(router)
	registerUser(router)

	// 公开读接口
	rigsterAnchorFollowerNum(router)
	rigsterViewerScBv(router)

	// 加密相关接口：10次/分钟/IP
	registerCrypto(router)

	// OSS 对象存储公开接口：20次/分钟/IP
	registerOSS(router)
	registerOssCallback(router)
}
