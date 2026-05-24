package middleware

import (
	"time"
)

// 预定义的限流器单例（滑动窗口算法）

// GlobalRateLimiter 全局限流器：30次/分钟/IP
// 适用：所有接口，防止基础攻击
var GlobalRateLimiter = RateLimitByIP(30, 1*time.Minute)

// CaptchaRateLimiter 验证码限流器：10次/分钟/IP
// 适用：验证码接口，防止验证码滥用
var CaptchaRateLimiter = RateLimitByIP(10, 1*time.Minute)

// LoginRateLimiter 登录限流器：5次/分钟/IP
// 适用：登录接口，防止暴力破解
var LoginRateLimiter = RateLimitByIP(5, 1*time.Minute)

// CryptoRateLimiter 加密接口限流器：10次/分钟/IP
// 适用：密钥交换接口
var CryptoRateLimiter = RateLimitByIP(10, 1*time.Minute)

// OSSRateLimiter OSS 接口限流器：5次/分钟/IP
// 适用：OSS 上传凭证接口
var OSSRateLimiter = RateLimitByIP(5, 1*time.Minute)

// PublicReadRateLimiter 公开读接口限流器：30次/分钟/IP+端点
// 适用：GET 请求的数据查询接口
var PublicReadRateLimiter = RateLimitByIPAndEndpoint(30, 1*time.Minute)

// AuthWriteRateLimiter 已认证写操作限流器：100次/分钟/用户
// 适用：POST/PUT/DELETE 请求
var AuthWriteRateLimiter = RateLimitByUser(100, 1*time.Minute)

// 备用的精细化限流器（如果需要不同配置）

// PublicReadStrictRateLimiter 严格版公开读限流器：20次/分钟/IP+端点
var PublicReadStrictRateLimiter = RateLimitByIPAndEndpoint(20, 1*time.Minute)

// AuthWriteStrictRateLimiter 严格版已认证限流器：50次/分钟/用户
var AuthWriteStrictRateLimiter = RateLimitByUser(50, 1*time.Minute)
