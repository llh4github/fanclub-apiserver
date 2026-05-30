package services

import (
	"fmt"
	"time"

	"fanclub-apiserver/cache"
	"fanclub-apiserver/database/generated"
	"fanclub-apiserver/database/model"
	"fanclub-apiserver/dto/resp"
	"fanclub-apiserver/errs"
	"fanclub-apiserver/g"

	"gorm.io/cli/gorm/typed"
)

var ViewerScBv = new(viewerScBvService)

type viewerScBvService struct {
}

// CheckByBV 根据BV号和房间ID查询是否存在相关SC点播记录
func (s *viewerScBvService) CheckByBV(appCtx *g.AppCtx, roomID int64, bv string) (*resp.ViewerScBvCheckResult, error) {
	cacheKey := fmt.Sprintf("%s%d:%s", cache.ViewerScBv, roomID, bv)
	ttl := 5 * time.Minute

	result, err := cache.CacheData(ttl, appCtx.C, cacheKey, func() (*resp.ViewerScBvCheckResult, error) {
		count, err := typed.G[model.ViewerScBvRecord](g.DB,
			generated.ViewerScBvRecord.BV.Eq(bv),
			generated.ViewerScBvRecord.RoomID.Eq(roomID),
		).Count(appCtx.C, "id")
		if err != nil {
			return nil, errs.WrapError(err, "查询SC点播BV记录失败", string(errs.UnkonwError))
		}

		return &resp.ViewerScBvCheckResult{
			Exists: count > 0,
			Count:  count,
		}, nil
	})
	if err != nil {
		return nil, err
	}

	return result, nil
}
