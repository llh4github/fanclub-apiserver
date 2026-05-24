package cache

import (
	"context"
	"fmt"
	"time"

	"fanclub-apiserver/g"

	"go.uber.org/zap"
)

const summaryLockTTL = 5 * time.Minute

func formatInt64(n int64) string {
	return fmt.Sprintf("%d", n)
}

const summaryLockPrefix = "submission_summary_lock:"

// AcquireSummaryLock 获取投稿总结的分布式锁
//
// 使用 Redis SETNX 实现简单的分布式锁，避免重复请求
//
// Parameters:
//   - ctx: 上下文
//   - submissionID: 投稿ID
//
// Returns:
//   - bool: 是否获取成功（true=获取到锁，false=未获取到，已有请求在处理中）
func AcquireSummaryLock(ctx context.Context, submissionID int64) bool {
	if g.Redis == nil {
		return true
	}

	lockKey := summaryLockPrefix + formatInt64(submissionID)
	result, err := g.Redis.SetNX(ctx, lockKey, "1", summaryLockTTL).Result()
	if err != nil {
		g.Logger.Warn("Failed to acquire summary lock", zap.Int64("submission_id", submissionID), zap.Error(err))
		return true
	}

	return result
}

// ReleaseSummaryLock 释放投稿总结的分布式锁
//
// Parameters:
//   - ctx: 上下文
//   - submissionID: 投稿ID
func ReleaseSummaryLock(ctx context.Context, submissionID int64) {
	if g.Redis == nil {
		return
	}

	lockKey := summaryLockPrefix + formatInt64(submissionID)
	if err := g.Redis.Del(ctx, lockKey).Err(); err != nil {
		g.Logger.Warn("Failed to release summary lock", zap.Int64("submission_id", submissionID), zap.Error(err))
	}
}
