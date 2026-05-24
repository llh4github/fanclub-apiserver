package api

import (
	"fanclub-apiserver/api/rest"
	"fanclub-apiserver/g"

	_ "fanclub-apiserver/docs"

	"github.com/gofiber/contrib/v3/swaggo"
	"github.com/gofiber/fiber/v3"
	"github.com/gofiber/fiber/v3/middleware/healthcheck"
	"github.com/gofiber/fiber/v3/middleware/requestid"
	"github.com/gofiber/fiber/v3/middleware/responsetime"
)

// SetupRoutes 注册所有路由
func SetupRoutes(app *fiber.App) {
	// 全局中间件
	if g.Cfg.Server.RequestIDEnabled {
		app.Use(requestid.New())
	}
	if g.Cfg.Server.ResponseTimeEnabled {
		app.Use(responsetime.New())
	}

	// 健康检查路由
	app.Get(healthcheck.LivenessEndpoint, healthcheck.New())
	app.Get(healthcheck.ReadinessEndpoint, healthcheck.New(healthcheck.Config{
		Probe: func(c fiber.Ctx) bool {
			// 检查数据库连接
			if g.DB == nil {
				return false
			}
			// 这里可以添加其他服务的健康检查
			return true
		},
	}))
	app.Get(healthcheck.StartupEndpoint, healthcheck.New())

	// Swagger route - only enable in development environment
	if g.Cfg.Server.SwaggerEnabled {
		app.Get("/swagger/*", swaggo.HandlerDefault)
		g.Info("swagger enabled")
	} else {
		g.Info("swgger not enable")
	}

	api := app.Group("/api")
	rest.RigsterRouter(api)

}
