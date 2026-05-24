package bilibili

import (
	"context"
	"fmt"
	"math/rand"
	"time"

	"fanclub-apiserver/cache"
	"fanclub-apiserver/database/generated"
	"fanclub-apiserver/database/model"
	"fanclub-apiserver/dto/req"
	"fanclub-apiserver/g"
	"fanclub-apiserver/services"

	"go.uber.org/zap"
	"gorm.io/cli/gorm/typed"
	"gorm.io/gorm/clause"
)

// FetchAndUpsertFollowerNum 获取启用粉丝数监控的主播的粉丝数并写入数据库
// 查询所有启用 Follower 特性的主播，调用B站API获取粉丝数，upsert到数据库，并清除对应缓存
func FetchAndUpsertFollowerNum(ctx context.Context) error {
	// 1. 查询启用粉丝数监控的主播 bid
	var followerResults []struct {
		// B站UID
		Bid int64 `json:"bid"`
	}
	if err := typed.G[model.SysScraperFeature](g.DB).
		Select(
			generated.AnchorInfo.Bid.WithTable("Anchor").As("bid"),
		).
		Where(generated.SysScraperFeature.Follower.Eq(true)).
		Joins(clause.LeftJoin.Association("Anchor"), func(_ typed.JoinBuilder, _ clause.Table, _ clause.Table) error {
			return nil
		}).
		Scan(ctx, &followerResults); err != nil {
		return fmt.Errorf("查询启用粉丝数监控的主播失败: %w", err)
	}

	if len(followerResults) == 0 {
		g.Info("没有启用粉丝数监控的主播")
		return nil
	}

	// 2. 随机获取一个 UID 的 cookies 数据列表
	cookieQ := typed.G[model.SysScraperCookie](g.DB)
	var uidResults []struct {
		UID int64 `json:"uid"`
	}
	if err := cookieQ.
		Select(generated.SysScraperCookie.UID).
		Distinct(generated.SysScraperCookie.UID).
		Scan(ctx, &uidResults); err != nil {
		return fmt.Errorf("查询 Cookie UID 列表失败: %w", err)
	}
	if len(uidResults) == 0 {
		return fmt.Errorf("没有可用的 Cookie 数据")
	}

	// 随机选取一个 UID
	selectedUID := uidResults[rand.Intn(len(uidResults))].UID

	// 获取该 UID 的所有 Cookie
	cookies, err := cookieQ.
		Where(generated.SysScraperCookie.UID.Eq(selectedUID)).
		Find(ctx)
	if err != nil {
		return fmt.Errorf("查询 UID=%d 的 Cookie 列表失败: %w", selectedUID, err)
	}

	// 构建 cookie 字符串
	cookieStr := buildCookieString(cookies)
	biliClient := NewClient(cookieStr)

	// 3. 逐个获取粉丝数并 upsert
	appCtx := &g.AppCtx{C: ctx}
	today := time.Now().Format("2006-01-02")

	for _, r := range followerResults {
		if r.Bid <= 0 {
			continue
		}

		resp, err := biliClient.FetchUserRelation(r.Bid)
		if err != nil {
			g.Error("获取主播粉丝数失败",
				zap.Int64("bid", r.Bid),
				zap.Error(err),
			)
			continue
		}
		if resp.Data == nil {
			g.Warn("主播粉丝数响应数据为空", zap.Int64("bid", r.Bid))
			continue
		}

		// upsert 到数据库
		upsertReq := &req.CreateAnchorFollowerNum{
			FollowerNum: resp.Data.Follower,
			CntDate:     today,
			Bid:         r.Bid,
		}
		if err := services.AnchorFollowerNum.Upsert(appCtx, upsertReq); err != nil {
			g.Error("Upsert 主播粉丝数失败",
				zap.Int64("bid", r.Bid),
				zap.Error(err),
			)
			continue
		}

		// 4. 清除该主播的粉丝数缓存
		// 写入位置: services/anchor_follower_num.go GetPastFollowerNum
		pastPattern := fmt.Sprintf("%s:past:%d:*", cache.AnchorFollowerNum, r.Bid)
		// 写入位置: services/anchor_follower_num.go GetLatestFollowerNum
		latestKey := fmt.Sprintf("%s:latest:%d", cache.AnchorFollowerNum, r.Bid)
		deleted, err := cache.ScanUnlinkKeys(ctx, pastPattern, latestKey)
		if err != nil {
			g.Warn("清除粉丝数缓存失败",
				zap.Int64("bid", r.Bid),
				zap.Error(err),
			)
		} else if deleted > 0 {
			g.Debug("清除粉丝数缓存",
				zap.Int64("bid", r.Bid),
				zap.Int64("deleted", deleted),
			)
		}
	}

	return nil
}
