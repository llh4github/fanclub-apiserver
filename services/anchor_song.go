package services

import (
	"fanclub-apiserver/database"
	"fanclub-apiserver/database/generated"
	"fanclub-apiserver/database/model"
	"fanclub-apiserver/dto/req"
	"fanclub-apiserver/dto/resp"
	"fanclub-apiserver/errs"
	"fanclub-apiserver/g"
	"fmt"

	"go.uber.org/zap"
	"gorm.io/cli/gorm/typed"
	"gorm.io/gorm"
	"gorm.io/gorm/clause"
)

var AnchorSong = new(anchorSongService)

type anchorSongService struct {
}

// Create 创建主播歌曲
func (s *anchorSongService) Create(appCtx *g.AppCtx, song *model.AnchorSong) error {
	q := typed.G[model.AnchorSong](g.DB)
	if err := q.Create(appCtx.C, song); err != nil {
		return errs.WrapError(err, "创建主播歌曲失败", string(errs.DataCreateFailed))
	}
	return nil
}

// GetByBidAndName 根据B站ID和歌曲名称获取主播歌曲
func (s *anchorSongService) GetByBidAndName(appCtx *g.AppCtx, bid int64, name string) (*model.AnchorSong, error) {
	song, err := database.GetOne[model.AnchorSong](appCtx.C,
		generated.AnchorSong.Bid.Eq(bid),
		generated.AnchorSong.Name.Eq(name),
	)
	if err != nil {
		return nil, errs.WrapError(err, "查询主播歌曲失败", string(errs.DataNotFound))
	}
	return song, nil
}

// Update 更新主播歌曲
func (s *anchorSongService) Update(appCtx *g.AppCtx, song *model.AnchorSong) error {
	if err := g.DB.WithContext(appCtx.C).Save(song).Error; err != nil {
		return errs.WrapError(err, "更新主播歌曲失败", string(errs.DataUdpateFailed))
	}
	return nil
}

// UpdateByID 根据ID更新主播歌曲
func (s *anchorSongService) UpdateByID(appCtx *g.AppCtx, req *req.UpdateAnchorSong) error {
	return g.DB.WithContext(appCtx.C).Transaction(func(tx *gorm.DB) error {
		q := typed.G[model.AnchorSong](tx)
		song, err := q.Where(generated.BaseModel.ID.Eq(req.ID)).First(appCtx.C)
		if err != nil {
			return errs.WrapError(err, "主播歌曲不存在", string(errs.DataNotFound))
		}

		if req.Name != "" {
			count, err := q.
				Where(generated.BaseModel.ID.Neq(req.ID)).
				Where(generated.AnchorSong.Name.Eq(req.Name)).
				Where(generated.AnchorSong.Bid.Neq(req.Bid)).
				Count(appCtx.C, "id")
			if err != nil {
				return errs.WrapError(err, "检查歌曲名称失败", string(errs.DataUdpateFailed))
			}
			if count > 0 {
				return errs.WrapError(fmt.Errorf("song name conflict"), "该歌曲名称已存在", string(errs.DataCreateFailed))
			}
			song.Name = req.Name
		}
		if req.Price >= 0 {
			song.Price = req.Price
		}
		if req.Bv != "" {
			song.Bv = req.Bv
		}
		if req.Bid > 0 {
			song.Bid = req.Bid
		}

		if err := tx.Save(song).Error; err != nil {
			return errs.WrapError(err, "更新主播歌曲失败", string(errs.DataUdpateFailed))
		}
		return nil
	})
}

// Delete 根据ID删除主播歌曲
func (s *anchorSongService) Delete(appCtx *g.AppCtx, id int64) error {
	q := typed.G[model.AnchorSong](g.DB)
	_, err := q.Where(generated.BaseModel.ID.Eq(id)).Delete(appCtx.C)
	if err != nil {
		return errs.WrapError(err, "删除主播歌曲失败", string(errs.DataDeleteFailed))
	}
	return nil
}

// DeleteBatch 根据ID列表批量删除主播歌曲
func (s *anchorSongService) DeleteBatch(appCtx *g.AppCtx, ids []int64, bid int64) error {
	q := typed.G[model.AnchorSong](g.DB)
	query := q.Where(generated.BaseModel.ID.In(ids...))
	if bid > 0 {
		query = query.Where(generated.AnchorSong.Bid.Eq(bid))
	}
	result, err := query.Delete(appCtx.C)
	if err != nil {
		return errs.WrapError(err, "批量删除主播歌曲失败", string(errs.DataDeleteFailed))
	}
	g.Info("批量删除主播歌曲",
		zap.Int("count", result),
	)
	return nil
}

// Page 分页查询主播歌曲，支持按歌名模糊查询，返回简要信息
func (s *anchorSongService) Page(appCtx *g.AppCtx, bid int64, name string, pageParam req.PageParam) (*resp.PageResult[*resp.AnchorSongSimple], error) {
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
		conds = append(conds, generated.AnchorSong.Bid.Eq(bid))
	}
	if name != "" {
		conds = append(conds, generated.AnchorSong.Name.Like("%"+name+"%"))
	}

	result, err := database.Page[model.AnchorSong](appCtx.C, page, pageSize, conds...)
	if err != nil {
		return nil, errs.WrapError(err, "分页查询主播歌曲失败", string(errs.DataNotFound))
	}

	// 转换为简要信息
	simpleRecords := make([]*resp.AnchorSongSimple, 0, len(result.Records))
	for _, r := range result.Records {
		simpleRecords = append(simpleRecords, &resp.AnchorSongSimple{
			Price: r.Price,
			Name:  r.Name,
			Bv:    r.Bv,
		})
	}

	return &resp.PageResult[*resp.AnchorSongSimple]{
		TotalRowCount: result.TotalRowCount,
		TotalPage:     result.TotalPage,
		Records:       simpleRecords,
	}, nil
}

// PageAdmin 后台分页查询主播歌曲，支持按歌名模糊查询，返回全部字段
func (s *anchorSongService) PageAdmin(appCtx *g.AppCtx, bid int64, name string, pageParam req.PageParam) (*resp.PageResult[*model.AnchorSong], error) {
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
		conds = append(conds, generated.AnchorSong.Bid.Eq(bid))
	}
	if name != "" {
		conds = append(conds, generated.AnchorSong.Name.Like("%"+name+"%"))
	}

	result, err := database.Page[model.AnchorSong](appCtx.C, page, pageSize, conds...)
	if err != nil {
		return nil, errs.WrapError(err, "后台分页查询主播歌曲失败", string(errs.DataNotFound))
	}

	return &resp.PageResult[*model.AnchorSong]{
		TotalRowCount: result.TotalRowCount,
		TotalPage:     result.TotalPage,
		Records:       result.Records,
	}, nil
}
