package services

import (
	"context"
	"fmt"
	"time"

	"fanclub-apiserver/cache"
	"fanclub-apiserver/database/generated"
	"fanclub-apiserver/database/model"
	"fanclub-apiserver/dto/resp"
	"fanclub-apiserver/errs"
	"fanclub-apiserver/g"

	"go.uber.org/zap"
	"gorm.io/cli/gorm/typed"
	"gorm.io/gorm/clause"
)

var AnchorLiveSchedule = new(anchorLiveScheduleService)

type anchorLiveScheduleService struct {
}

// GetWeeklySchedule 获取主播本周直播日程
//
// Parameters:
//   - appCtx: 应用上下文
//   - bid: 主播B站UID
//
// Returns:
//   - []*resp.AnchorLiveSchedule: 本周直播日程列表
//   - error: 错误信息
func (s *anchorLiveScheduleService) GetWeeklySchedule(appCtx *g.AppCtx, bid int64) ([]*resp.AnchorLiveSchedule, error) {
	cacheKey := fmt.Sprintf("%s%d", cache.AnchorLiveSchedule, bid)
	ttl := time.Hour

	return cache.CacheData(ttl, appCtx.C, cacheKey, func() ([]*resp.AnchorLiveSchedule, error) {
		now := time.Now()
		weekday := now.Weekday()

		startOfWeek := now.AddDate(0, 0, -int(weekday))
		startOfWeek = time.Date(startOfWeek.Year(), startOfWeek.Month(), startOfWeek.Day(), 0, 0, 0, 0, startOfWeek.Location())

		endOfWeek := startOfWeek.AddDate(0, 0, 6)
		endOfWeek = time.Date(endOfWeek.Year(), endOfWeek.Month(), endOfWeek.Day(), 23, 59, 59, 999999999, endOfWeek.Location())

		var results []*resp.AnchorLiveSchedule
		q := typed.G[model.AnchorLiveSchedule](g.DB).
			Select(
				generated.AnchorLiveSchedule.Topic,
				generated.AnchorLiveSchedule.Emoji,
				generated.AnchorLiveSchedule.StartTime,
				generated.AnchorLiveSchedule.EndTime,
			).
			Where(generated.AnchorLiveSchedule.Bid.Eq(bid)).
			Where(generated.AnchorLiveSchedule.StartTime.Gte(startOfWeek)).
			Where(generated.AnchorLiveSchedule.StartTime.Lte(endOfWeek)).
			Order(clause.OrderBy{
				Columns: []clause.OrderByColumn{
					generated.AnchorLiveSchedule.StartTime.Asc(),
				},
			})

		if err := q.Scan(appCtx.C, &results); err != nil {
			return nil, errs.WrapError(err, "Failed to get weekly schedule", string(errs.DataNotFound))
		}

		return results, nil
	})
}

// InvalidateCache 清除主播直播日程缓存
//
// Parameters:
//   - ctx: 上下文
//   - bid: 主播B站UID
func (s *anchorLiveScheduleService) InvalidateCache(ctx context.Context, bid int64) {
	cacheKey := fmt.Sprintf("%s%d", cache.AnchorLiveSchedule, bid)
	if err := g.Redis.Del(ctx, cacheKey).Err(); err != nil {
		g.Warn("清除直播日程缓存失败", zap.Error(err), zap.Int64("bid", bid))
	}
}
