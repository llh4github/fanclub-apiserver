package services

import (
	"context"
	"errors"
	"fmt"
	"time"

	"fanclub-apiserver/cache"
	"fanclub-apiserver/consts"
	"fanclub-apiserver/database"
	"fanclub-apiserver/database/generated"
	"fanclub-apiserver/database/model"
	"fanclub-apiserver/dto/req"
	"fanclub-apiserver/dto/resp"
	"fanclub-apiserver/errs"
	"fanclub-apiserver/g"

	"go.uber.org/zap"
	"gorm.io/cli/gorm/typed"
	"gorm.io/gorm"
	"gorm.io/gorm/clause"
)

const (
	// maxActiveTopicsPerBid 单个BID下最大启用主题数
	maxActiveTopicsPerBid = 5
)

var TreeholeTopic = new(treeholeTopicService)

type treeholeTopicService struct {
}

// Create 创建树洞主题
func (s *treeholeTopicService) Create(appCtx *g.AppCtx, topic *model.TreeholeTopic) error {
	return g.DB.WithContext(appCtx.C).Transaction(func(tx *gorm.DB) error {
		// 如果为主播角色，使用 appCtx 中的 BID 覆盖
		if appCtx.Role.IsAnchor() {
			if appCtx.BID == nil {
				return errs.NoBindAnchorError
			}
			topic.Bid = *appCtx.BID
		}

		// 填充创建者ID
		topic.CreatedByID = &appCtx.UserID

		// 类型安全查询当前BID下已启用的主题数量
		q := typed.G[model.TreeholeTopic](tx)
		count, err := q.
			Where(generated.TreeholeTopic.Bid.Eq(topic.Bid)).
			Where(generated.TreeholeTopic.IsActive.Eq(true)).
			Count(appCtx.C, "id")
		if err != nil {
			return errs.WrapError(err, "查询启用主题数量失败", string(errs.DataNotFound))
		}

		// 校验是否超过最大数量限制
		if count >= maxActiveTopicsPerBid {
			return errs.WrapError(nil, fmt.Sprintf("该主播已启用%d个主题，达到最大数量限制（%d个）", count, maxActiveTopicsPerBid), string(errs.ReqParamValidFailed))
		}

		// 创建主题
		createQ := typed.G[model.TreeholeTopic](tx)
		if err := createQ.Create(appCtx.C, topic); err != nil {
			return errs.WrapError(err, "创建树洞主题失败", string(errs.DataCreateFailed))
		}

		// 清除该bid的缓存
		s.InvalidateCache(appCtx.C, topic.Bid)
		return nil
	})
}

// GetByID 根据ID获取树洞主题
func (s *treeholeTopicService) GetByID(appCtx *g.AppCtx, id int64) (*model.TreeholeTopic, error) {
	topic, err := database.GetOne[model.TreeholeTopic](appCtx.C,
		generated.BaseModel.ID.Eq(id),
	)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return topic, nil
}

// GetByBid 根据B站UID获取树洞主题
func (s *treeholeTopicService) GetByBid(appCtx *g.AppCtx, bid int64) (*model.TreeholeTopic, error) {
	topic, err := database.GetOne[model.TreeholeTopic](appCtx.C,
		generated.TreeholeTopic.Bid.Eq(int64(bid)),
	)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return topic, nil
}

// GetTopicDetailByID 根据ID获取话题详情（包含投稿统计数据）
func (s *treeholeTopicService) GetTopicDetailByID(appCtx *g.AppCtx, id int64) (*resp.TopicDetail, error) {
	// 查询话题
	topic, err := s.GetByID(appCtx, id)
	if err != nil {
		return nil, err
	}
	if topic == nil {
		return nil, nil
	}

	// 查询该话题下的投稿总数
	q := typed.G[model.TreeholeSubmission](g.DB)
	totalCount, err := q.
		Where(generated.TreeholeSubmission.TopicID.Eq(int64(id))).
		Count(appCtx.C, "id")
	if err != nil {
		return nil, errs.WrapError(err, "查询投稿总数失败", string(errs.DataNotFound))
	}

	// 查询不宜展示的投稿数量
	hiddenCount, err := q.
		Where(generated.TreeholeSubmission.TopicID.Eq(int64(id))).
		Where(generated.TreeholeSubmission.AuditStatus.Eq(int(consts.AuditStatusHidden))).
		Count(appCtx.C, "id")
	if err != nil {
		return nil, errs.WrapError(err, "查询不宜展示投稿数量失败", string(errs.DataNotFound))
	}

	return &resp.TopicDetail{
		ID:                    topic.ID,
		Bid:                   topic.Bid,
		Title:                 topic.Title,
		Description:           topic.Description,
		OpenAt:                topic.OpenAt,
		CloseAt:               topic.CloseAt,
		IsActive:              topic.IsActive,
		CreatedTime:           topic.CreatedTime,
		UpdatedTime:           topic.UpdatedTime,
		TotalSubmissionCount:  totalCount,
		HiddenSubmissionCount: hiddenCount,
	}, nil
}

// batchGetTopicStats 批量查询话题的投稿统计数据
func (s *treeholeTopicService) batchGetTopicStats(appCtx *g.AppCtx, topicIDs []int64) (map[int64]*resp.TopicStats, error) {
	if len(topicIDs) == 0 {
		return make(map[int64]*resp.TopicStats), nil
	}

	statsMap := make(map[int64]*resp.TopicStats)
	for _, id := range topicIDs {
		statsMap[id] = &resp.TopicStats{
			TopicID:     id,
			TotalCount:  0,
			HiddenCount: 0,
		}
	}

	type aggregationResult struct {
		TopicID     int64
		TotalCount  int64
		HiddenCount int64
	}

	var results []aggregationResult
	if err := g.DB.WithContext(appCtx.C).Model(&model.TreeholeSubmission{}).
		Select(`topic_id,
			COUNT(*) as total_count,
			COALESCE(SUM(CASE WHEN audit_status = 0 THEN 1 ELSE 0 END), 0) as hidden_count`).
		Where("topic_id IN ?", topicIDs).
		Group("topic_id").
		Scan(&results).Error; err != nil {
		return nil, errs.WrapError(err, "批量查询统计数据失败", string(errs.DataNotFound))
	}

	for _, r := range results {
		if stats, ok := statsMap[r.TopicID]; ok {
			stats.TotalCount = r.TotalCount
			stats.HiddenCount = r.HiddenCount
		}
	}

	return statsMap, nil
}

// UpdateByID 根据ID更新话题
func (s *treeholeTopicService) UpdateByID(appCtx *g.AppCtx, r *req.UpdateTreeholeTopic) error {
	var bid int64

	if err := g.DB.WithContext(appCtx.C).Transaction(func(tx *gorm.DB) error {
		var topic model.TreeholeTopic
		q := typed.G[model.TreeholeTopic](tx)
		result, err := q.Where(generated.BaseModel.ID.Eq(int64(r.ID))).First(appCtx.C)
		if err != nil {
			return errs.WrapError(err, "树洞主题不存在", string(errs.DataNotFound))
		}
		topic = result

		// 保存原始 bid 用于清除缓存
		bid = topic.Bid

		updates := make(map[string]interface{})
		if r.Title != "" {
			updates["title"] = r.Title
		}
		if r.Description != "" {
			updates["description"] = r.Description
		}
		if !r.OpenAt.IsZero() {
			updates["open_at"] = r.OpenAt
		}
		if !r.CloseAt.IsZero() {
			updates["close_at"] = r.CloseAt
		}
		if r.IsActive != nil {
			updates["is_active"] = *r.IsActive
		}

		// 填充修改者ID
		updates["modifier_id"] = appCtx.UserID

		if err := tx.Model(&topic).Updates(updates).Error; err != nil {
			return errs.WrapError(err, "更新树洞主题失败", string(errs.DataUdpateFailed))
		}
		return nil
	}); err != nil {
		return err
	}

	// 清除缓存
	s.InvalidateCache(appCtx.C, bid)
	return nil
}

// Delete 删除树洞主题
func (s *treeholeTopicService) Delete(appCtx *g.AppCtx, id int64) error {
	// 先查询主题获取 bid
	topic, err := s.GetByID(appCtx, id)
	if err != nil {
		return errs.WrapError(err, "删除树洞主题失败", string(errs.DataNotFound))
	}

	q := typed.G[model.TreeholeTopic](g.DB)
	_, err = q.Where(generated.BaseModel.ID.Eq(int64(id))).Delete(appCtx.C)
	if err != nil {
		return errs.WrapError(err, "删除树洞主题失败", string(errs.DataDeleteFailed))
	}

	// 清除该bid的缓存
	s.InvalidateCache(appCtx.C, topic.Bid)
	return nil
}

// DeleteBatch 批量删除树洞主题
func (s *treeholeTopicService) DeleteBatch(appCtx *g.AppCtx, ids []int64) error {
	// 先查询所有要删除的主题，获取它们的 bid
	q := typed.G[model.TreeholeTopic](g.DB)
	topics, err := q.Where(generated.BaseModel.ID.In(ids...)).Find(appCtx.C)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil
		}
		return err
	}

	// 收集所有需要清除缓存的 bid
	bidSet := make(map[int64]bool)
	for _, topic := range topics {
		bidSet[topic.Bid] = true
	}

	// 执行删除
	result, err := q.Where(generated.BaseModel.ID.In(ids...)).Delete(appCtx.C)
	if err != nil {
		return err
	}
	g.Info("批量删除树洞主题",
		zap.Int("count", result),
	)

	// 清除所有涉及 bid 的缓存
	for bid := range bidSet {
		s.InvalidateCache(appCtx.C, bid)
	}

	return nil
}

// Page 分页查询树洞主题
func (s *treeholeTopicService) Page(appCtx *g.AppCtx, bid int64, title string, isActive *bool, pageParam req.PageParam) (*resp.PageResult[*model.TreeholeTopic], error) {
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
		conds = append(conds, generated.TreeholeTopic.Bid.Eq(bid))
	}
	if title != "" {
		conds = append(conds, generated.TreeholeTopic.Title.Like("%"+title+"%"))
	}
	if isActive != nil {
		conds = append(conds, generated.TreeholeTopic.IsActive.Eq(*isActive))
	}

	result, err := database.Page[model.TreeholeTopic](appCtx.C, page, pageSize, conds...)
	if err != nil {
		return nil, errs.WrapError(err, "分页查询树洞主题失败", string(errs.DataNotFound))
	}

	return result, nil
}

// PageAdmin 后台分页查询话题（包含统计数据）
func (s *treeholeTopicService) PageAdmin(appCtx *g.AppCtx, bid int64, title string, isActive *bool, pageParam req.PageParam) (*resp.PageResult[*resp.TopicPageItem], error) {
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
		conds = append(conds, generated.TreeholeTopic.Bid.Eq(bid))
	}
	if title != "" {
		conds = append(conds, generated.TreeholeTopic.Title.Like("%"+title+"%"))
	}
	if isActive != nil {
		conds = append(conds, generated.TreeholeTopic.IsActive.Eq(*isActive))
	}

	result, err := database.Page[model.TreeholeTopic](appCtx.C, page, pageSize, conds...)
	if err != nil {
		return nil, errs.WrapError(err, "分页查询话题失败", string(errs.DataNotFound))
	}

	// 提取话题IDs
	topicIDs := make([]int64, 0, len(result.Records))
	for _, t := range result.Records {
		topicIDs = append(topicIDs, t.ID)
	}

	// 批量查询统计数据
	statsMap, err := s.batchGetTopicStats(appCtx, topicIDs)
	if err != nil {
		return nil, err
	}

	// 填充统计数据到结果
	pageItems := make([]*resp.TopicPageItem, 0, len(result.Records))
	for _, topic := range result.Records {
		stats := statsMap[topic.ID]
		pageItems = append(pageItems, &resp.TopicPageItem{
			ID:                    topic.ID,
			Bid:                   topic.Bid,
			Title:                 topic.Title,
			Description:           topic.Description,
			OpenAt:                topic.OpenAt,
			CloseAt:               topic.CloseAt,
			IsActive:              topic.IsActive,
			CreatedTime:           topic.CreatedTime,
			UpdatedTime:           topic.UpdatedTime,
			TotalSubmissionCount:  stats.TotalCount,
			HiddenSubmissionCount: stats.HiddenCount,
		})
	}

	return &resp.PageResult[*resp.TopicPageItem]{
		TotalRowCount: result.TotalRowCount,
		TotalPage:     result.TotalPage,
		Records:       pageItems,
	}, nil
}

// CountOpenTopics 查询指定bid在当前时间内有几个开放投稿话题
func (s *treeholeTopicService) CountOpenTopics(appCtx *g.AppCtx, bid int64) (int, error) {
	cacheKey := fmt.Sprintf("%s%d", cache.TreeholeTopic, bid)

	return cache.CacheData(defaultCacheTTL, appCtx.C, cacheKey, func() (int, error) {
		now := time.Now()
		q := typed.G[model.TreeholeTopic](g.DB)
		count, err := q.
			Where(generated.TreeholeTopic.Bid.Eq(bid)).
			Where(generated.TreeholeTopic.IsActive.Eq(true)).
			Where(generated.TreeholeTopic.OpenAt.Lte(now)).
			Where(generated.TreeholeTopic.CloseAt.Gte(now)).
			Count(appCtx.C, "id")
		if err != nil {
			return 0, err
		}

		return int(count), nil
	})
}

// GetLatestActiveTopics 查询指定bid最新5条启用状态的话题
func (s *treeholeTopicService) GetLatestActiveTopics(appCtx *g.AppCtx, bid int64) ([]resp.TopicBrief, error) {
	cacheKey := fmt.Sprintf("%s%d:list", cache.TreeholeTopic, bid)

	return cache.CacheData(defaultCacheTTL, appCtx.C, cacheKey, func() ([]resp.TopicBrief, error) {
		q := typed.G[model.TreeholeTopic](g.DB)
		var results []resp.TopicBrief
		if err := q.
			Select(
				generated.BaseModel.ID,
				generated.TreeholeTopic.Title,
				generated.TreeholeTopic.Description,
				generated.TreeholeTopic.OpenAt,
				generated.TreeholeTopic.CloseAt,
			).
			Where(generated.TreeholeTopic.Bid.Eq(bid)).
			Where(generated.TreeholeTopic.IsActive.Eq(true)).
			Order(clause.OrderBy{Columns: []clause.OrderByColumn{
				generated.BaseModel.CreatedTime.Desc(),
			}}).
			Limit(5).
			Scan(appCtx.C, &results); err != nil {
			return nil, err
		}
		return results, nil
	})
}

// InvalidateCache 清除指定bid的缓存
func (s *treeholeTopicService) InvalidateCache(ctx context.Context, bid int64) {
	cacheKey := fmt.Sprintf("%s%d", cache.TreeholeTopic, bid)
	if err := g.Redis.Del(ctx, cacheKey).Err(); err != nil {
		g.Warn("清除树洞主题缓存失败", zap.Error(err), zap.Int64("bid", bid))
	}
}

// invalidateCacheBatch 批量清除指定bids的缓存
func (s *treeholeTopicService) invalidateCacheBatch(ctx context.Context, bids []int64) {
	if len(bids) == 0 {
		return
	}
	keys := make([]string, len(bids))
	for i, bid := range bids {
		keys[i] = fmt.Sprintf("%s%d", cache.TreeholeTopic, bid)
	}
	if err := g.Redis.Del(ctx, keys...).Err(); err != nil {
		g.Warn("批量清除树洞主题缓存失败", zap.Error(err), zap.Int("count", len(bids)))
	}
}

// DeactivateExpiredTopics 将已过投稿结束时间的主题设为未激活状态
func (s *treeholeTopicService) DeactivateExpiredTopics(ctx context.Context) (int64, error) {
	now := time.Now()

	type bidResult struct {
		Bid int64
	}

	var bids []int64
	err := g.DB.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		var results []bidResult
		if err := typed.G[model.TreeholeTopic](tx,
			generated.TreeholeTopic.IsActive.Eq(true),
			generated.TreeholeTopic.CloseAt.Lt(now),
		).Scan(ctx, &results); err != nil {
			return errs.WrapError(err, "查询过期主题失败", string(errs.DataNotFound))
		}

		if len(results) == 0 {
			return nil
		}

		bids = make([]int64, len(results))
		for i, r := range results {
			bids[i] = r.Bid
		}

		result := tx.Model(&model.TreeholeTopic{}).
			Where("is_active = ? AND close_at < ?", true, now).
			Update("is_active", false)
		if result.Error != nil {
			return errs.WrapError(result.Error, "关闭过期主题失败", string(errs.DataUdpateFailed))
		}

		return nil
	})

	if err != nil {
		return 0, err
	}

	s.invalidateCacheBatch(ctx, bids)

	if len(bids) > 0 {
		g.Info("关闭过期主题", zap.Int("count", len(bids)))
	}

	return int64(len(bids)), nil
}

// SetTopicStatus 设置主题的启用/禁用状态
func (s *treeholeTopicService) SetTopicStatus(appCtx *g.AppCtx, id int64, isActive *bool) error {
	var bid int64
	var topic model.TreeholeTopic

	if err := g.DB.WithContext(appCtx.C).Transaction(func(tx *gorm.DB) error {
		// 查询获取必要字段（bid）
		q := typed.G[model.TreeholeTopic](tx)
		record, err := q.
			Select(generated.BaseModel.ID, generated.TreeholeTopic.Bid).
			Where(generated.BaseModel.ID.Eq(id)).
			First(appCtx.C)
		if err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				return errs.WrapError(nil, "主题不存在", string(errs.DataNotFound))
			}
			return errs.WrapError(err, "查询主题失败", string(errs.DataNotFound))
		}
		bid = record.Bid
		topic.ID = record.ID

		// 更新 is_active 状态
		updates := map[string]interface{}{
			"is_active":   *isActive,
			"modifier_id": appCtx.UserID,
		}
		if err := tx.Model(&topic).Updates(updates).Error; err != nil {
			return errs.WrapError(err, "更新主题状态失败", string(errs.DataUdpateFailed))
		}

		return nil
	}); err != nil {
		return err
	}

	// 清除该 bid 的缓存
	s.InvalidateCache(appCtx.C, bid)
	return nil
}
