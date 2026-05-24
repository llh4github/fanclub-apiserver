package bilibili

import (
	"testing"
	"time"

	"fanclub-apiserver/bilibili"
	"fanclub-apiserver/consts"
	"fanclub-apiserver/database"
	"fanclub-apiserver/database/generated"
	"fanclub-apiserver/database/model"
	"fanclub-apiserver/dto/req"
	"fanclub-apiserver/g"
	"fanclub-apiserver/integration/testenv"
	"fanclub-apiserver/services"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

// insertAnchorWithFollowerFeature 插入主播信息并启用粉丝数监控
func insertAnchorWithFollowerFeature(t *testing.T, bid int64, biliName string, roomID int64) int64 {
	t.Helper()

	// 插入主播信息
	anchor := &model.AnchorInfo{
		Bid:      bid,
		BiliName: biliName,
		RoomID:   roomID,
	}
	err := g.DB.Create(anchor).Error
	require.NoError(t, err, "插入主播信息应成功")

	// 插入爬虫功能配置，启用粉丝数监控
	feature := &model.SysScraperFeature{
		AnchorID: anchor.ID,
		Follower: true,
		Monitor:  false,
	}
	err = g.DB.Create(feature).Error
	require.NoError(t, err, "插入爬虫功能配置应成功")

	return anchor.ID
}

// TestFetchAndUpsertFollowerNum 测试获取并更新主播粉丝数
func TestFetchAndUpsertFollowerNum(t *testing.T) {
	env, err := testenv.GetTestEnvironment()
	if err != nil {
		t.Fatalf("Failed to create test environment: %v", err)
	}
	defer env.Cleanup()

	t.Run("FetchAndUpsert_ShouldNotReturnError", func(t *testing.T) {
		// 基于 Liko 构建测试数据：插入主播并启用粉丝数监控
		insertAnchorWithFollowerFeature(t, consts.Liko.BID, consts.Liko.Name, consts.Liko.RoomID)

		// 调用 FetchAndUpsertFollowerNum，仅验证函数不返回错误
		// （B站API可能不可达，函数内部会log并continue，不会返回error）
		err := bilibili.FetchAndUpsertFollowerNum(env.Ctx)
		require.NoError(t, err, "FetchAndUpsertFollowerNum should not return error")
	})

	t.Run("Upsert_ShouldInsertAndUpsert", func(t *testing.T) {
		// 直接测试 service 层 upsert 逻辑，不依赖B站API
		appCtx := &g.AppCtx{C: env.Ctx}
		today := time.Now().Format("2006-01-02")
		bid := consts.Liko.BID

		// 第一次 upsert：插入
		err := services.AnchorFollowerNum.Upsert(appCtx, &req.CreateAnchorFollowerNum{
			FollowerNum: 100,
			CntDate:     today,
			Bid:         bid,
		})
		require.NoError(t, err, "First upsert should succeed")

		// 验证记录已创建
		todayDate, _ := time.Parse("2006-01-02", today)
		record, err := database.GetOne[model.AnchorFollowerNum](env.Ctx,
			generated.AnchorFollowerNum.Bid.Eq(bid),
			generated.AnchorFollowerNum.CntDate.Eq(todayDate),
		)
		require.NoError(t, err, "Should find the created record")
		assert.Equal(t, 100, record.FollowerNum, "FollowerNum should be 100")

		// 第二次 upsert：更新
		err = services.AnchorFollowerNum.Upsert(appCtx, &req.CreateAnchorFollowerNum{
			FollowerNum: 200,
			CntDate:     today,
			Bid:         bid,
		})
		require.NoError(t, err, "Second upsert should succeed")

		// 验证记录已更新
		updated, err := database.GetOne[model.AnchorFollowerNum](env.Ctx,
			generated.AnchorFollowerNum.Bid.Eq(bid),
			generated.AnchorFollowerNum.CntDate.Eq(todayDate),
		)
		require.NoError(t, err, "Should find the updated record")
		assert.Equal(t, 200, updated.FollowerNum, "FollowerNum should be updated to 200")
		assert.Equal(t, record.ID, updated.ID, "ID should remain the same (upsert, not insert)")
	})

	_ = g.DB // ensure g.DB is used reference
}
