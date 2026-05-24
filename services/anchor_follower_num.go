package services

import (
	"database/sql"
	"fmt"
	"time"

	"fanclub-apiserver/cache"
	"fanclub-apiserver/database"
	"fanclub-apiserver/database/generated"
	"fanclub-apiserver/database/model"
	"fanclub-apiserver/dto/req"
	"fanclub-apiserver/dto/resp"
	"fanclub-apiserver/errs"
	"fanclub-apiserver/g"

	"gorm.io/cli/gorm/typed"
	"gorm.io/gorm/clause"
)

var AnchorFollowerNum = new(anchorFollowerNumService)

type anchorFollowerNumService struct {
}

// Upsert 插入或更新主播粉丝数记录
func (s *anchorFollowerNumService) Upsert(appCtx *g.AppCtx, anchor *req.CreateAnchorFollowerNum) error {
	cntDate, err := time.Parse("2006-01-02", anchor.CntDate)
	if err != nil {
		return errs.WrapError(err, "Invalid count date format", string(errs.ReqParamValidFailed))
	}

	entity := &model.AnchorFollowerNum{
		FollowerNum: anchor.FollowerNum,
		CntDate:     cntDate,
		Bid:         int64(anchor.Bid),
	}

	q := typed.G[model.AnchorFollowerNum](g.DB, clause.OnConflict{
		Columns: []clause.Column{generated.AnchorFollowerNum.Bid.Column(), generated.AnchorFollowerNum.CntDate.Column()},
		DoUpdates: clause.Assignments(map[string]any{
			generated.AnchorFollowerNum.FollowerNum.Column().Name: anchor.FollowerNum,
			generated.BaseModel.UpdatedTime.Column().Name:         time.Now(),
		}),
	})
	err = q.Create(appCtx.C, entity)
	if err != nil {
		return errs.WrapError(err, "Failed to create anchor follower num", string(errs.DataCreateFailed))
	}
	return nil
}

// Create 创建主播粉丝数记录
func (s *anchorFollowerNumService) Create(appCtx *g.AppCtx, anchor *model.AnchorFollowerNum) error {
	q := typed.G[model.AnchorFollowerNum](g.DB)
	if err := q.Create(appCtx.C, anchor); err != nil {
		return errs.WrapError(err, "Failed to create anchor follower num", string(errs.DataCreateFailed))
	}
	return nil
}

// GetByID 根据 ID 获取主播粉丝数记录
func (s *anchorFollowerNumService) GetByID(appCtx *g.AppCtx, id int64) (*model.AnchorFollowerNum, error) {
	anchor, err := database.GetOne[model.AnchorFollowerNum](appCtx.C, generated.BaseModel.ID.Eq(id))
	if err != nil {
		return nil, errs.WrapError(err, "Failed to get anchor follower num by ID", string(errs.DataNotFound))
	}
	return anchor, nil
}

// GetByBiliIDAndDate 根据 B站 UID 和日期获取主播粉丝数记录
func (s *anchorFollowerNumService) GetByBiliIDAndDate(appCtx *g.AppCtx, bid int64, cntDate string) (*model.AnchorFollowerNum, error) {
	date, err := time.Parse("2006-01-02", cntDate)
	if err != nil {
		return nil, errs.WrapError(err, "Invalid count date", string(errs.UnkonwError))
	}

	anchor, err := database.GetOne[model.AnchorFollowerNum](appCtx.C,
		generated.AnchorFollowerNum.Bid.Eq(bid),
		generated.AnchorFollowerNum.CntDate.Eq(date),
	)
	if err != nil {
		return nil, errs.WrapError(err, "Failed to get anchor follower num by Bili ID and date", string(errs.DataNotFound))
	}
	return anchor, nil
}

// List 获取主播粉丝数记录列表
func (s *anchorFollowerNumService) List(appCtx *g.AppCtx, limit, offset int) ([]*resp.AnchorFollowerNumSimple, error) {
	q := typed.G[model.AnchorFollowerNum](g.DB).
		Select(
			generated.AnchorFollowerNum.FollowerNum,
			generated.AnchorFollowerNum.CntDate,
			generated.AnchorFollowerNum.Bid,
		)

	if limit > 0 {
		q = q.Limit(limit)
	}
	if offset > 0 {
		q = q.Offset(offset)
	}

	var rs []*resp.AnchorFollowerNumSimple
	if err := q.Scan(appCtx.C, &rs); err != nil {
		return nil, errs.WrapError(err, "Failed to get anchor follower num list", string(errs.DataNotFound))
	}
	return rs, nil
}

// Update 更新主播粉丝数记录
func (s *anchorFollowerNumService) Update(appCtx *g.AppCtx, anchor *model.AnchorFollowerNum) error {
	if err := g.DB.WithContext(appCtx.C).Save(anchor).Error; err != nil {
		return errs.WrapError(err, "Failed to update anchor follower num", string(errs.DataUdpateFailed))
	}
	return nil
}

// Delete 根据 ID 删除主播粉丝数记录
func (s *anchorFollowerNumService) Delete(appCtx *g.AppCtx, id int64) error {
	q := typed.G[model.AnchorFollowerNum](g.DB)
	_, err := q.Where(generated.BaseModel.ID.Eq(id)).Delete(appCtx.C)
	if err != nil {
		return errs.WrapError(err, "Failed to delete anchor follower num", string(errs.DataDeleteFailed))
	}
	return nil
}

// GetPastFollowerNum 获取过去 [pastDays] 天的粉丝数记录，结果缓存5分钟
func (s *anchorFollowerNumService) GetPastFollowerNum(appCtx *g.AppCtx, bid int64, pastDays int) ([]*resp.DailyFollowerNum, error) {
	cacheKey := fmt.Sprintf("%s:past:%d:%d", cache.AnchorFollowerNum, bid, pastDays)

	cached, err := cache.CacheData(defaultCacheTTL, appCtx.C, cacheKey, func() (*[]*resp.DailyFollowerNum, error) {
		return s.queryPastFollowerNum(appCtx, bid, pastDays)
	})
	if err != nil {
		return nil, err
	}
	return *cached, nil
}

// GetLatestFollowerNum 获取最新的粉丝数记录
func (s *anchorFollowerNumService) GetLatestFollowerNum(appCtx *g.AppCtx, bid int64) (*resp.DailyFollowerNum, error) {
	cacheKey := fmt.Sprintf("%s:%d:latest", cache.AnchorFollowerNum, bid)

	cached, err := cache.CacheData(defaultCacheTTL, appCtx.C, cacheKey, func() (*resp.DailyFollowerNum, error) {
		return s.queryLatestFollowerNum(appCtx, bid)
	})
	if err != nil {
		return nil, err
	}
	return cached, nil
}

// queryLatestFollowerNum 从数据库查询最新粉丝数记录
func (s *anchorFollowerNumService) queryLatestFollowerNum(appCtx *g.AppCtx, bid int64) (*resp.DailyFollowerNum, error) {
	type rawRecord struct {
		CntDate     time.Time
		FollowerNum int
	}

	var record rawRecord
	q := typed.G[model.AnchorFollowerNum](g.DB).
		Select(
			generated.AnchorFollowerNum.FollowerNum,
			generated.AnchorFollowerNum.CntDate,
		).
		Where(generated.AnchorFollowerNum.Bid.Eq(bid)).
		Order(clause.OrderBy{
			Columns: []clause.OrderByColumn{
				generated.AnchorFollowerNum.CntDate.Desc(),
			},
		}).
		Limit(1)

	err := q.Scan(appCtx.C, &record)
	if err != nil {
		if err == sql.ErrNoRows {
			return &resp.DailyFollowerNum{
				Date: time.Now().Format("2006-01-02"),
				Num:  0,
			}, nil
		}
		return nil, errs.WrapError(err, "Failed to get latest follower num", string(errs.DataNotFound))
	}

	return &resp.DailyFollowerNum{
		Date: record.CntDate.Format("2006-01-02"),
		Num:  record.FollowerNum,
	}, nil
}

// queryPastFollowerNum 从数据库查询过去 [pastDays] 天的粉丝数记录
func (s *anchorFollowerNumService) queryPastFollowerNum(appCtx *g.AppCtx, bid int64, pastDays int) (*[]*resp.DailyFollowerNum, error) {
	startDate := time.Now().AddDate(0, 0, -pastDays)

	type rawRecord struct {
		CntDate     time.Time
		FollowerNum int
	}
	var rawResults []rawRecord

	q := typed.G[model.AnchorFollowerNum](g.DB).
		Select(
			generated.AnchorFollowerNum.FollowerNum,
			generated.AnchorFollowerNum.CntDate,
		).
		Where(generated.AnchorFollowerNum.Bid.Eq(bid)).
		Where(generated.AnchorFollowerNum.CntDate.Gte(startDate))

	if err := q.Scan(appCtx.C, &rawResults); err != nil {
		return nil, errs.WrapError(err, "Failed to get past follower num", string(errs.DataNotFound))
	}

	results := make([]*resp.DailyFollowerNum, 0, len(rawResults))
	for _, r := range rawResults {
		results = append(results, &resp.DailyFollowerNum{
			Date: r.CntDate.Format("2006-01-02"),
			Num:  r.FollowerNum,
		})
	}
	return &results, nil
}

// Point 时间序列数据点
type Point struct {
	X float64
	Y float64
}

// LTTB downsamples time series data using the Largest Triangle Three Buckets algorithm.
//
// Parameters:
//   - data: ordered list of points sorted by X (timestamp)
//   - threshold: target number of points to retain (minimum 2)
//
// Returns:
//   - downsampled slice of Points
//
// Panics if threshold < 2 or data is empty.
func LTTB(data []Point, threshold int) []Point {
	if threshold < 2 || len(data) == 0 {
		return data
	}
	if len(data) <= threshold {
		return data
	}

	result := make([]Point, 0, threshold)
	result = append(result, data[0])

	avgBucketSize := float64(len(data)-2) / float64(threshold-2)

	var a int
	nextA := 0

	for i := 0; i < threshold-2; i++ {
		avgX := 0.0
		avgY := 0.0
		avgRangeStart := (i+1)*int(avgBucketSize) + 1
		avgRangeEnd := (i+2)*int(avgBucketSize) + 1
		if avgRangeEnd > len(data) {
			avgRangeEnd = len(data)
		}
		avgRangeLength := avgRangeEnd - avgRangeStart

		for ; avgRangeStart < avgRangeEnd; avgRangeStart++ {
			avgX += data[avgRangeStart].X
			avgY += data[avgRangeStart].Y
		}
		avgX /= float64(avgRangeLength)
		avgY /= float64(avgRangeLength)

		rangeStart := (i+1)*int(avgBucketSize) + 1
		rangeEnd := (i+2)*int(avgBucketSize) + 1
		if rangeEnd > len(data) {
			rangeEnd = len(data)
		}

		pointAX := data[a].X
		pointAY := data[a].Y

		maxArea := -1.0

		for ; rangeStart < rangeEnd; rangeStart++ {
			area := (pointAX-avgX)*(data[rangeStart].Y-pointAY) - (pointAX-data[rangeStart].X)*(avgY-pointAY)
			if area < 0 {
				area = -area
			}
			area *= 0.5
			if area > maxArea {
				maxArea = area
				nextA = rangeStart
			}
		}

		result = append(result, data[nextA])
		a = nextA
	}

	result = append(result, data[len(data)-1])
	return result
}

// DownsampleFollowerNum 使用 LTTB 算法对粉丝数数据进行降采样，反映全部数据趋势
func (s *anchorFollowerNumService) DownsampleFollowerNum(appCtx *g.AppCtx, bid int64, pastDays, threshold int) ([]*resp.DailyFollowerNum, error) {
	rawData, err := s.queryPastFollowerNum(appCtx, bid, pastDays)
	if err != nil {
		return nil, err
	}

	if len(*rawData) == 0 {
		return []*resp.DailyFollowerNum{}, nil
	}

	points := make([]Point, 0, len(*rawData))
	for _, d := range *rawData {
		parsedDate, _ := time.Parse("2006-01-02", d.Date)
		points = append(points, Point{
			X: float64(parsedDate.Unix()),
			Y: float64(d.Num),
		})
	}

	if threshold < 2 {
		threshold = 2
	}

	downsampled := LTTB(points, threshold)

	result := make([]*resp.DailyFollowerNum, 0, len(downsampled))
	for _, p := range downsampled {
		result = append(result, &resp.DailyFollowerNum{
			Date: time.Unix(int64(p.X), 0).Format("2006-01-02"),
			Num:  int(p.Y),
		})
	}

	return result, nil
}

// Page 分页查询主播粉丝数记录
func (s *anchorFollowerNumService) Page(appCtx *g.AppCtx, bid int64, pageParam req.PageParam) (*resp.PageResult[*model.AnchorFollowerNum], error) {
	page := pageParam.PageIndex
	if page <= 0 {
		page = 1
	}
	pageSize := pageParam.PageSize
	if pageSize <= 0 {
		pageSize = 10
	}

	var conds []clause.Expression
	if bid > 0 {
		conds = append(conds, generated.AnchorFollowerNum.Bid.Eq(bid))
	}

	result, err := database.Page[model.AnchorFollowerNum](appCtx.C, page, pageSize, conds...)
	if err != nil {
		return nil, errs.WrapError(err, "Failed to get anchor follower num page", string(errs.DataNotFound))
	}
	return result, nil
}
