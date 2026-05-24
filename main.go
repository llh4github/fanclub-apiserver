package main

import (
	"context"
	"fanclub-apiserver/ai"
	"fanclub-apiserver/api"
	"fanclub-apiserver/cache"
	"fanclub-apiserver/g"
	"fanclub-apiserver/middleware"
	"fanclub-apiserver/scheduler"
	"fanclub-apiserver/storage"
	"fmt"
	"os"

	_ "fanclub-apiserver/docs"

	"github.com/gofiber/fiber/v3"
	"go.uber.org/zap"
)

//	@title			Fanclub API
//	@version		1.0
//	@description	Fanclub API documentation
//	@termsOfService	http://swagger.io/terms/
//	@contact.name	API Support
//	@contact.email	support@fanclub.com
//	@license.name	MIT
//	@license.url	https://opensource.org/licenses/MIT
//	@host			localhost:8080
//	@BasePath		/api

func main() {
	// 打印环境变量
	fmt.Printf("APP_ENV: %s\n", os.Getenv("APP_ENV"))

	if err := g.InitSonyflake(); err != nil {
		panic(fmt.Sprintf("Failed to init Sonyflake: %v", err))
	}
	// 加载配置
	if err := g.LoadConfig(); err != nil {
		panic(fmt.Sprintf("Failed to load config: %v", err))
	}

	// 根据配置中的日志级别初始化日志
	if err := g.InitLogger(g.Cfg.Logs.Level); err != nil {
		panic(fmt.Sprintf("Failed to initialize logger: %v", err))
	}
	defer func() {
		err := g.Sync()
		if err != nil {

		}
	}()

	g.Debug("Config loaded successfully", zap.Any("cfg", g.Cfg))
	g.Info("Starting application")
	// 打印构建信息
	g.Info("Build information",
		zap.String("version", g.Version),
		zap.String("branch", g.Branch),
		zap.String("gitCommit", g.GitCommit),
		zap.String("buildTime", g.BuildTime),
		zap.String("env", g.Cfg.Server.Env),
	)
	// 初始化Redis
	if err := g.InitRedis(); err != nil {
		g.Fatal("Failed to initialize Redis", zap.Error(err))
	}

	// 注册 Redis Functions
	if err := cache.RegisterFunctions(context.Background()); err != nil {
		g.Warn("Failed to register Redis functions", zap.Error(err))
	} else {
		g.Info("register Redis functions OK")
	}
	// 初始化数据库
	if err := g.InitDB(); err != nil {
		g.Fatal("Failed to initialize database", zap.Error(err))
	}

	// 初始化 ARK 客户端
	if err := ai.InitArkClient(); err != nil {
		g.Fatal("Failed to initialize ARK client", zap.Error(err))
	}

	// 初始化 OSS 客户端
	if err := storage.InitOSS(); err != nil {
		g.Fatal("Failed to initialize OSS client", zap.Error(err))
	}

	// 创建Fiber应用
	app := fiber.New(fiber.Config{
		ErrorHandler: middleware.ErrorHandler(),
	})
	g.Info("Fiber app created")

	// 注册路由
	api.SetupRoutes(app)

	if err := scheduler.Init(); err != nil {
		g.Fatal("Failed to initialize scheduler", zap.Error(err))
	}

	// 启动服务器
	serverAddr := fmt.Sprintf("%s:%s", g.Cfg.Server.Host, g.Cfg.Server.Port)
	g.Info("Server starting", zap.String("address", serverAddr))
	if err := app.Listen(serverAddr); err != nil {
		g.Fatal("Failed to start server", zap.Error(err))
	}
}
