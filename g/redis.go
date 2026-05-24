package g

import (
	"context"
	"fmt"

	"github.com/redis/go-redis/v9"
)

// Redis 全局 Redis 客户端实例
var Redis *redis.Client

// InitRedis 初始化 Redis 连接
func InitRedis() error {
	// 构建 Redis 连接地址
	addr := fmt.Sprintf("%s:%s", Cfg.Redis.Host, Cfg.Redis.Port)

	// 创建 Redis 客户端
	Redis = redis.NewClient(&redis.Options{
		Addr:     addr,
		Password: Cfg.Redis.Password,
		DB:       Cfg.Redis.DB,
	})

	// 测试连接
	ctx := context.Background()
	_, err := Redis.Ping(ctx).Result()
	if err != nil {
		return fmt.Errorf("failed to connect to redis: %w", err)
	}
	Info("Redis connected successfully")
	return nil
}
