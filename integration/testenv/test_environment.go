package testenv

import (
	"context"
	"fmt"
	"log"
	"time"

	"fanclub-apiserver/cache"
	"fanclub-apiserver/database/model"
	"fanclub-apiserver/g"

	"github.com/redis/go-redis/v9"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"

	"github.com/testcontainers/testcontainers-go"
	postgrescontainer "github.com/testcontainers/testcontainers-go/modules/postgres"
	rediscontainer "github.com/testcontainers/testcontainers-go/modules/redis"
)

// TestEnvironment 测试环境结构体
type TestEnvironment struct {
	Ctx               context.Context
	PostgresContainer *postgrescontainer.PostgresContainer
	RedisContainer    *rediscontainer.RedisContainer
}

// GetTestEnvironment 创建并返回新的测试环境实例
func GetTestEnvironment() (*TestEnvironment, error) {
	return NewTestEnvironment()
}

// NewTestEnvironment 创建测试环境
func NewTestEnvironment() (*TestEnvironment, error) {
	ctx := context.Background()
	// 初始化 sonyflake
	if err := g.InitSonyflake(); err != nil {
		return nil, fmt.Errorf("Failed to initialize sonyflake: %v", err)
	}

	if err := g.InitLogger("debug"); err != nil {
		return nil, fmt.Errorf("Failed to initialize logger: %v", err)
	}

	// 设置 model 包的 ID 生成函数
	model.GenerateID = g.NextID

	// 启动 PostgreSQL 容器
	postgresContainer, err := postgrescontainer.RunContainer(ctx,
		testcontainers.WithImage("postgres:17-alpine"),
		postgrescontainer.WithDatabase("fanclub_dev"),
		postgrescontainer.WithUsername("postgres"),
		postgrescontainer.WithPassword("123456"),
	)
	if err != nil {
		return nil, fmt.Errorf("failed to start PostgreSQL container: %w", err)
	}

	// 启动 Redis 容器
	redisContainer, err := rediscontainer.RunContainer(ctx,
		testcontainers.WithImage("redis/redis-stack-server:7.4.0-v8"),
	)
	if err != nil {
		_ = postgresContainer.Terminate(ctx)
		return nil, fmt.Errorf("failed to start Redis container: %w", err)
	}

	env := &TestEnvironment{
		Ctx:               ctx,
		PostgresContainer: postgresContainer,
		RedisContainer:    redisContainer,
	}
	err = env.initGormDB()
	if err != nil {
		return nil, fmt.Errorf("failed to connection DB: %w", err)
	}
	err = env.initRedisClient()
	if err != nil {
		return nil, fmt.Errorf("failed to connection Redis: %w", err)
	}
	return env, nil
}

// GetPostgresConnectionString 获取 PostgreSQL 连接字符串
func (e *TestEnvironment) GetPostgresConnectionString() (string, error) {
	return e.PostgresContainer.ConnectionString(e.Ctx, "sslmode=disable")
}

// GetRedisConnectionString 获取 Redis 连接字符串
func (e *TestEnvironment) GetRedisConnectionString() (string, error) {
	return e.RedisContainer.ConnectionString(e.Ctx)
}

// Cleanup 清理测试环境
func (e *TestEnvironment) Cleanup() {
	if g.DB != nil {
		sqlDB, err := g.DB.DB()
		if err == nil {
			sqlDB.Close()
		}
	}
	if err := e.PostgresContainer.Terminate(e.Ctx); err != nil {
		log.Printf("Warning: Failed to terminate PostgreSQL container: %v", err)
	}
	if err := e.RedisContainer.Terminate(e.Ctx); err != nil {
		log.Printf("Warning: Failed to terminate Redis container: %v", err)
	}
}

// initGormDB 初始化 gorm 数据库连接
func (e *TestEnvironment) initGormDB() error {
	dsn, err := e.GetPostgresConnectionString()
	if err != nil {
		return err
	}

	fmt.Printf("PostgreSQL connection string: %s\n", dsn)

	// 等待 PostgreSQL 容器完全启动
	time.Sleep(2 * time.Second)

	db, err := gorm.Open(postgres.Open(dsn), &gorm.Config{})
	if err != nil {
		return err
	}

	g.DB = db.Debug()

	// 自动迁移模型
	if err := g.DB.AutoMigrate(model.Models()...); err != nil {
		sqlDB, _ := db.DB()
		if sqlDB != nil {
			sqlDB.Close()
		}
		return err
	}

	return nil
}

// initRedisClient 初始化 Redis 客户端
func (e *TestEnvironment) initRedisClient() error {
	redisAddr, err := e.GetRedisConnectionString()
	if err != nil {
		return err
	}

	fmt.Printf("Redis connection string: %s\n", redisAddr)

	if len(redisAddr) > 8 && redisAddr[:8] == "redis://" {
		redisAddr = redisAddr[8:]
	}

	time.Sleep(1 * time.Second)

	g.Redis = redis.NewClient(&redis.Options{
		Addr: redisAddr,
	})

	if err := g.Redis.Ping(e.Ctx).Err(); err != nil {
		return fmt.Errorf("failed to ping redis: %w", err)
	}

	// 注册 Redis Functions
	if err := cache.RegisterFunctions(e.Ctx); err != nil {
		return fmt.Errorf("failed to register redis functions: %w", err)
	}

	return nil
}
