package services

import (
	"errors"
	"fmt"
	"time"

	"fanclub-apiserver/cache"
	"fanclub-apiserver/database/generated"
	"fanclub-apiserver/database/model"
	"fanclub-apiserver/dto/resp"
	"fanclub-apiserver/g"

	"gorm.io/cli/gorm/typed"
	"gorm.io/gorm"
	"gorm.io/gorm/clause"
)

var AnchorLiveRecord = new(anchorLiveRecordService)

type anchorLiveRecordService struct {
}

// GetLatestByRoomID 根据 roomID 查询最新一条直播记录
//
// Parameters:
//   - appCtx: 应用上下文
//   - roomID: 直播间ID
//
// Returns:
//   - *resp.LatestLiveRecord: 最新直播记录
//   - error: 错误信息
func (s *anchorLiveRecordService) GetLatestByRoomID(appCtx *g.AppCtx, roomID int64) (*resp.LatestLiveRecord, error) {
	cacheKey := fmt.Sprintf("%s%d:latest", cache.AnchorLiveRecord, roomID)
	ttl := 5 * time.Minute

	var record *resp.LatestLiveRecord
	cached, err := cache.CacheData(ttl, appCtx.C, cacheKey, func() (*resp.LatestLiveRecord, error) {
		q := typed.G[model.AnchorLiveRecord](g.DB).
			Select(
				generated.AnchorLiveRecord.LiveKey,
				generated.AnchorLiveRecord.LiveTime,
				generated.AnchorLiveRecord.LiveStatus,
				generated.AnchorLiveRecord.EndLiveTime,
				generated.AnchorLiveRecord.LiveDuration,
			).
			Where(generated.AnchorLiveRecord.RoomID.Eq(roomID)).
			Order(clause.OrderBy{
				Columns: []clause.OrderByColumn{
					generated.AnchorLiveRecord.LiveTime.Desc(),
				},
			})

		record = new(resp.LatestLiveRecord)
		if err := q.Scan(appCtx.C, record); err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				return nil, nil
			}
			return nil, err
		}

		return record, nil
	})
	if err != nil {
		return nil, err
	}

	return cached, nil
}

// GetCurrentWeekByRoomID 根据 roomID 查询今天所在周（周一到周日）的直播记录
//
// Parameters:
//   - appCtx: 应用上下文
//   - roomID: 直播间ID
//
// Returns:
//   - []*resp.WeekLiveRecord: 本周直播记录列表
//   - error: 错误信息
func (s *anchorLiveRecordService) GetCurrentWeekByRoomID(appCtx *g.AppCtx, roomID int64) ([]*resp.WeekLiveRecord, error) {
	now := time.Now()
	weekday := now.Weekday()
	startOfWeek := now.AddDate(0, 0, -int(weekday)+1)
	startOfWeek = time.Date(startOfWeek.Year(), startOfWeek.Month(), startOfWeek.Day(), 0, 0, 0, 0, startOfWeek.Location())

	endOfWeek := startOfWeek.AddDate(0, 0, 6)
	endOfWeek = time.Date(endOfWeek.Year(), endOfWeek.Month(), endOfWeek.Day(), 23, 59, 59, 999999999, endOfWeek.Location())

	cacheKey := fmt.Sprintf("%s%d:week:%s", cache.AnchorLiveRecord, roomID, startOfWeek.Format("2006-01-02"))
	ttl := 10 * time.Minute

	var results []*resp.WeekLiveRecord
	cached, err := cache.CacheData(ttl, appCtx.C, cacheKey, func() ([]*resp.WeekLiveRecord, error) {
		q := typed.G[model.AnchorLiveRecord](g.DB).
			Select(
				generated.AnchorLiveRecord.LiveKey,
				generated.AnchorLiveRecord.LiveTime,
				generated.AnchorLiveRecord.LiveStatus,
				generated.AnchorLiveRecord.EndLiveTime,
				generated.AnchorLiveRecord.LiveDuration,
			).
			Where(generated.AnchorLiveRecord.RoomID.Eq(roomID)).
			Where(generated.AnchorLiveRecord.LiveTime.Between(startOfWeek, endOfWeek)).
			Order(clause.OrderBy{
				Columns: []clause.OrderByColumn{
					generated.AnchorLiveRecord.LiveTime.Asc(),
				},
			})

		if err := q.Scan(appCtx.C, &results); err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				return results, nil
			}
			return nil, err
		}

		return results, nil
	})
	if err != nil {
		return nil, err
	}

	return cached, nil
}
