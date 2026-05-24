package middleware

// 限流中间件使用示例
//
// 根据当前项目实际情况推荐的限流配置策略：
//
// 项目特点：
//   - 31个接口，混合认证模式
//   - 包含公开接口（验证码、登录）和已认证接口
//   - 管理员和普通用户两档角色
//
// 推荐的限流策略：
//
// 1. 全局限流（基础保护）
//    - 适用：所有接口
//    - 限制：30次/分钟（平均0.5次/秒）
//    - 依据：IP
//    - 目的：防止恶意爬虫和基础DDoS攻击
//
// 2. 公开接口限流（验证码、登录）
//    - 适用：/captcha/*, /auth/login
//    - 限制：10次/分钟
//    - 依据：IP
//    - 目的：防止暴力破解和验证码滥用
//
// 3. 管理员接口（宽松限制）
//    - 适用：所有已认证接口
//    - 限制：100次/分钟
//    - 依据：用户ID
//    - 目的：不影响正常管理工作
//
// 4. 公开数据查询（中等限制）
//    - 适用：GET请求的数据接口
//    - 限制：30次/分钟
//    - 依据：IP+端点
//    - 目的：允许正常访问，防止爬虫
//
// 5. 写操作接口（严格限制）
//    - 适用：POST, PUT, DELETE 请求
//    - 限制：20次/分钟
//    - 依据：用户ID
//    - 目的：防止误操作和恶意提交

// 示例 1: 全局限流 - 基于 IP（基础保护）
// 限制：30次/分钟，平均0.5次/秒
// app.Use(middleware.RateLimitByIP(30, 1*time.Minute))

// 示例 2: 验证码接口严格限流 - 基于 IP
// 限制：10次/分钟，防止暴力破解
// router.Group("captcha").Use(middleware.RateLimitByIP(10, 1*time.Minute))

// 示例 3: 登录接口严格限流 - 基于 IP
// 限制：5次/分钟，防止暴力破解
// router.Post("/auth/login", middleware.RateLimitByIP(5, 1*time.Minute), apis.Login)

// 示例 4: 管理员接口宽松限流 - 基于用户 ID
// 限制：100次/分钟，不影响管理工作
// router.Group("/admin").Use(jwtHandler, middleware.RateLimitByUser(100, 1*time.Minute))

// 示例 5: 公开数据查询 - 基于 IP + 端点
// 限制：30次/分钟，允许正常访问
// router.Group("anchor/song").Use(middleware.RateLimitByIPAndEndpoint(30, 1*time.Minute))
// router.Group("anchor/live").Use(middleware.RateLimitByIPAndEndpoint(30, 1*time.Minute))

// 示例 6: 动态限流 - 根据用户角色调整
// 管理员: 100次/分钟，普通用户: 50次/分钟
// router.Use(middleware.DynamicRateLimit(func(c fiber.Ctx) int {
//     claims := c.Locals("claims")
//     if claims != nil {
//         if claim, ok := claims.(*g.Claims); ok {
//             if claim.Role == "admin" {
//                 return 100
//             }
//             return 50
//         }
//     }
//     return 30
// }, 1*time.Minute))

// 示例 7: 写操作严格限流 - 基于用户ID
// 限制：20次/分钟，防止恶意提交
// router.Group("anchor/song").Use(jwtHandler, middleware.RateLimitByUser(20, 1*time.Minute))

// 示例 8: 自定义 key 函数限流 - 基于 API Key 或 IP
// router.Use(middleware.RateLimitByKeyFunc(func(c fiber.Ctx) string {
//     return c.Get("X-Api-Key", c.IP())
// }, 50, 1*time.Minute))

// 实际应用示例
//
// 在 api/rest/base.go 中的使用：
//
// func RigsterRouter(router fiber.Router) {
//     // 1. 全局限流（基础保护）
//     router.Use(middleware.RateLimitByIP(30, 1*time.Minute))
//
//     // 2. 验证码接口严格限流
//     router.Group("captcha").Use(middleware.RateLimitByIP(10, 1*time.Minute))
//
//     // 3. 登录接口严格限流
//     router.Post("/auth/login", middleware.RateLimitByIP(5, 1*time.Minute), ...)
// }
//
// 推荐的分层限流策略：
//
// 层级一：全局基础限流（最外层）
//   - 限制：30次/分钟/IP
//   - 依据：IP
//   - 目的：防止大规模攻击
//
// 层级二：接口分组限流（中间层）
//   - 公开接口：10次/分钟/IP
//   - 已认证接口：50次/分钟/用户
//   - 目的：区分不同接口的风险等级
//
// 层级三：敏感操作限流（最内层）
//   - 登录/验证码：5次/分钟/IP
//   - 写操作：20次/分钟/用户
//   - 目的：保护高风险操作
