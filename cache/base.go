package cache

import (
	"context"
	"embed"
	"encoding/json"
	"fanclub-apiserver/g"
	"io/fs"
	"strings"
	"time"

	"go.uber.org/zap"
)

//go:embed luas/*.lua
var luaScripts embed.FS

// CacheData 从缓存中获取数据，如果缓存中没有数据，则调用 dataFunc 获取并缓存
// T 可以是任意类型：结构体、切片、指针等
func CacheData[T any](
	ttl time.Duration,
	ctx context.Context,
	cacheKey string,
	dataFunc func() (data T, err error),
) (T, error) {
	// 尝试从缓存中获取数据
	cached, err := g.Redis.Get(ctx, cacheKey).Bytes()
	if err == nil {
		// 缓存命中，反序列化数据
		var rs T
		if err := json.Unmarshal(cached, &rs); err == nil {
			return rs, nil
		}
		// 反序列化失败，继续获取新数据
	}

	// 缓存未命中或反序列化失败，调用 dataFunc 获取数据
	data, err := dataFunc()
	if err != nil {
		var zero T
		return zero, err
	}

	// 将数据序列化并存入缓存
	dataBytes, err := json.Marshal(data)
	if err != nil {
		// 序列化失败，返回数据但不缓存
		g.Warn("Failed to marshal data for cache",
			zap.String("cacheKey", cacheKey),
			zap.Error(err))
		return data, nil
	}

	// 存入缓存
	if err := g.Redis.Set(ctx, cacheKey, dataBytes, ttl).Err(); err != nil {
		// 缓存失败，返回数据但不缓存
		g.Warn("Failed to set data in cache",
			zap.String("cacheKey", cacheKey),
			zap.Duration("ttl", ttl),
			zap.Error(err))
		return data, nil
	}

	// 缓存成功
	g.Debug("Data cached successfully",
		zap.String("cacheKey", cacheKey),
		zap.Duration("ttl", ttl))

	// 成功缓存，返回数据
	return data, nil
}

// ScanUnlinkKeys 使用 Redis Function 扫描并删除指定 pattern 的 key
// patterns - key 的匹配模式，支持通配符，可传入多个 pattern
// 返回删除的键数量
func ScanUnlinkKeys(ctx context.Context, patterns ...string) (int64, error) {
	var allArgs []any
	allArgs = append(allArgs, "FCALL", "scan_unlink", 0)
	for _, p := range patterns {
		allArgs = append(allArgs, p)
	}
	result, err := g.Redis.Do(ctx, allArgs...).Result()
	if err != nil {
		return 0, err
	}
	return result.(int64), nil
}

// SetExpire 为多个 key 设置统一的过期时间
// Parameters:
//   - keys  要设置过期时间的 key 列表
//   - expireSeconds  过期时间（秒）
//
// 返回成功设置过期时间的 key 数量
func SetExpire(ctx context.Context, expireSeconds int64, keys ...string) (int64, error) {
	var allArgs []any
	allArgs = append(allArgs, "FCALL", "set_expire", len(keys))
	for _, key := range keys {
		allArgs = append(allArgs, key)
	}
	allArgs = append(allArgs, expireSeconds)
	result, err := g.Redis.Do(ctx, allArgs...).Result()
	if err != nil {
		return 0, err
	}
	return result.(int64), nil
}

// StatisticsDanmu 弹幕数据统计去重计数。
// 内部自动生成当前时间的小时分钟字符串作为分钟级时间分片。
//
// Parameters:
//   - ctx: 上下文
//   - key: 基础键名
//   - uid: 用户UID
//   - timestamp: 时间戳字符串
//
// Returns:
//   - 1 表示计数成功，0 表示重复已存在
//   - error 调用 Redis 异常时返回错误
func StatisticsDanmu(ctx context.Context, key string, uid string, timestamp string) (int64, error) {
	minuteTime := time.Now().Format("1504")
	result, err := g.Redis.Do(ctx, "FCALL", "statistics_danmu", 2, key, minuteTime, uid, timestamp).Result()
	if err != nil {
		return 0, err
	}
	return result.(int64), nil
}

// CheckImageExists 检查图片是否已存在于缓存集合中
// Parameters:
//   - setKey: Redis set 的键名
//   - imageURLs: 要检查的图片 URL 列表
//
// 返回值:
//   - 不存在的图片 URL 列表
//   - 错误信息
func CheckImageExists(ctx context.Context, setKey string, imageURLs []string) ([]string, error) {
	if len(imageURLs) == 0 {
		return []string{}, nil
	}

	if g.Redis == nil {
		return imageURLs, nil
	}

	result, err := g.Redis.SMIsMember(ctx, setKey, imageURLs).Result()
	if err != nil {
		return nil, err
	}

	var notExists []string
	for i, exists := range result {
		if !exists {
			notExists = append(notExists, imageURLs[i])
		}
	}

	return notExists, nil
}

// MarkImageProcessed 将处理过的图片标记为已处理
// Parameters:
//   - setKey: Redis set 的键名
//   - imageURLs: 要标记的图片 URL 列表
func MarkImageProcessed(ctx context.Context, setKey string, imageURLs []string) error {
	if len(imageURLs) == 0 {
		return nil
	}

	if g.Redis == nil {
		return nil
	}

	_, err := g.Redis.SAdd(ctx, setKey, imageURLs).Result()
	return err
}

// RegisterFunctions 注册所有 Redis Functions
func RegisterFunctions(ctx context.Context) error {
	files, err := fs.Glob(luaScripts, "luas/*.lua")
	if err != nil {
		return err
	}

	var combinedScript strings.Builder
	combinedScript.WriteString("#!lua name=fanclub_apiserver\n\n")

	for _, filePath := range files {
		scriptContent, err := luaScripts.ReadFile(filePath)
		if err != nil {
			return err
		}

		contentStr := string(scriptContent)
		contentStr = strings.TrimPrefix(contentStr, "#!lua name=fanclub_apiserver\n")
		contentStr = strings.TrimSpace(contentStr)
		combinedScript.WriteString(contentStr)
		combinedScript.WriteString("\n\n")
	}

	rs, err := g.Redis.Do(ctx, "FUNCTION", "LOAD", "REPLACE", combinedScript.String()).Result()
	if err != nil {
		return err
	}
	g.Info("register functions OK", zap.Any("rs", rs))

	return nil
}
