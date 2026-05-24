package middleware

import (
	"fanclub-apiserver/errs"
	"fanclub-apiserver/g"
	"time"

	"github.com/gofiber/fiber/v3"
	"github.com/gofiber/fiber/v3/middleware/limiter"
)

// RateLimitConfig 限流中间件配置
//
// 参数:
//   - Max: 最大请求数
//   - Expiration: 时间窗口长度
//   - KeyGenerator: 自定义 key 生成函数
//   - MaxFunc: 动态最大请求数函数
type RateLimitConfig struct {
	Max          int
	Expiration   time.Duration
	KeyGenerator func(fiber.Ctx) string
	MaxFunc      func(fiber.Ctx) int
}

// RateLimitByIP 创建基于 IP 的限流中间件（滑动窗口算法）
//
// 参数:
//   - maxRequests: 时间窗口内的最大请求数
//   - window: 时间窗口长度
//
// 使用场景: 适用于公共接口，限制每个 IP 的请求频率
func RateLimitByIP(maxRequests int, window time.Duration) fiber.Handler {
	return RateLimit(&RateLimitConfig{
		Max:        maxRequests,
		Expiration: window,
		KeyGenerator: func(c fiber.Ctx) string {
			return c.IP()
		},
	})
}

// RateLimitByUser 创建基于用户 ID 的限流中间件（滑动窗口算法）
//
// 参数:
//   - maxRequests: 时间窗口内的最大请求数
//   - window: 时间窗口长度
//
// 使用场景: 适用于已认证接口，按用户维度限制请求频率，无认证时降级为 IP 限流
func RateLimitByUser(maxRequests int, window time.Duration) fiber.Handler {
	return RateLimit(&RateLimitConfig{
		Max:        maxRequests,
		Expiration: window,
		KeyGenerator: func(c fiber.Ctx) string {
			claims := c.Locals("claims")
			if claims == nil {
				return c.IP()
			}
			if claim, ok := claims.(*g.Claims); ok {
				return claim.Subject
			}
			return c.IP()
		},
	})
}

// RateLimitByEndpoint 创建基于接口端点的限流中间件（滑动窗口算法）
//
// 参数:
//   - maxRequests: 时间窗口内的最大请求数
//   - window: 时间窗口长度
//
// 使用场景: 适用于限制特定接口的总请求量，不区分用户/IP
func RateLimitByEndpoint(maxRequests int, window time.Duration) fiber.Handler {
	return RateLimit(&RateLimitConfig{
		Max:        maxRequests,
		Expiration: window,
		KeyGenerator: func(c fiber.Ctx) string {
			return c.Path()
		},
	})
}

// RateLimitByIPAndEndpoint 创建基于 IP 和接口端点的限流中间件（滑动窗口算法）
//
// 参数:
//   - maxRequests: 时间窗口内的最大请求数
//   - window: 时间窗口长度
//
// 使用场景: 适用于限制每个 IP 对每个接口的请求频率，防止单一 IP 对特定接口过度请求
func RateLimitByIPAndEndpoint(maxRequests int, window time.Duration) fiber.Handler {
	return RateLimit(&RateLimitConfig{
		Max:        maxRequests,
		Expiration: window,
		KeyGenerator: func(c fiber.Ctx) string {
			return c.IP() + ":" + c.Path()
		},
	})
}

// RateLimitByKeyFunc 创建基于自定义 key 函数的限流中间件（滑动窗口算法）
//
// 参数:
//   - keyFunc: 自定义 key 生成函数
//   - maxRequests: 默认最大请求数
//   - window: 时间窗口长度
//
// 使用场景: 适用于需要根据业务逻辑自定义限流 key 的场景
func RateLimitByKeyFunc(keyFunc func(fiber.Ctx) string, maxRequests int, window time.Duration) fiber.Handler {
	return RateLimit(&RateLimitConfig{
		Max:          maxRequests,
		Expiration:   window,
		KeyGenerator: keyFunc,
	})
}

// DynamicRateLimit 创建支持动态限额的限流中间件（滑动窗口算法）
//
// 参数:
//   - maxFunc: 动态最大请求数函数，根据请求上下文返回不同的限制数
//   - window: 时间窗口长度
//
// 使用场景: 适用于根据用户角色、会员等级等动态调整限流阈值的场景
func DynamicRateLimit(maxFunc func(fiber.Ctx) int, window time.Duration) fiber.Handler {
	return limiter.New(limiter.Config{
		MaxFunc:           maxFunc,
		Expiration:        window,
		LimiterMiddleware: limiter.SlidingWindow{},
		KeyGenerator: func(c fiber.Ctx) string {
			return c.IP()
		},
		LimitReached:           rateLimitExceeded,
		SkipFailedRequests:     false,
		SkipSuccessfulRequests: false,
	})
}

// RateLimit 通用限流中间件创建函数
//
// 参数:
//   - config: 限流配置
//
// 返回: fiber 中间件处理器
func RateLimit(config *RateLimitConfig) fiber.Handler {
	return limiter.New(limiter.Config{
		Max:                    config.Max,
		Expiration:             config.Expiration,
		LimiterMiddleware:      limiter.SlidingWindow{},
		KeyGenerator:           config.KeyGenerator,
		LimitReached:           rateLimitExceeded,
		SkipFailedRequests:     false,
		SkipSuccessfulRequests: false,
	})
}

// rateLimitExceeded 限流触发时的响应处理
//
// 当请求超过限制时，返回统一的错误响应格式
func rateLimitExceeded(c fiber.Ctx) error {
	return c.Status(fiber.StatusOK).JSON(rateLimitErrorResponse{
		Code:    string(errs.RateLimitExceeded),
		Message: "请求过于频繁，请稍后再试",
		Ts:      time.Now().UnixMilli(),
	})
}

// rateLimitErrorResponse 限流错误响应结构
type rateLimitErrorResponse struct {
	Code    string `json:"code"`
	Message string `json:"msg"`
	Ts      int64  `json:"ts"`
}
