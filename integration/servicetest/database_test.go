package servicetest

import (
	"context"
	"fmt"
	"testing"
	"time"

	"fanclub-apiserver/database"
	"fanclub-apiserver/database/generated"
	"fanclub-apiserver/database/model"
	"fanclub-apiserver/dto/req"
	"fanclub-apiserver/g"
	"fanclub-apiserver/integration/testenv"
	"fanclub-apiserver/services"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"gorm.io/cli/gorm/typed"
)

// anchorFollowerNumRow 主播粉丝数行结构
type anchorFollowerNumRow struct {
	// 粉丝数
	FollowerNum int `json:"follower_num"`
	// 统计日期
	CntDate *time.Time `json:"cnt_date"`
	// B站 UID
	Bid int64 `json:"bid"`
}

// listByBid 根据 B 站 UID 查询主播粉丝数列表
func listByBid[R any](ctx context.Context, bid int64) ([]*R, error) {
	return database.ScanList[R, model.AnchorFollowerNum](ctx,
		generated.AnchorFollowerNum.Bid.Eq(bid),
	)
}

// TestAnchorFollowerNumListByBid 根据 B 站 UID 查询主播粉丝数测试
func TestAnchorFollowerNumListByBid(t *testing.T) {
	env, err := testenv.GetTestEnvironment()
	if err != nil {
		t.Fatalf("Failed to create test environment: %v", err)
	}
	defer env.Cleanup()

	t.Run("QueryDataFound_ShouldReturnRecords", func(t *testing.T) {
		bid := int64(888888)
		// 创建一条记录
		q := typed.G[model.AnchorFollowerNum](g.DB)
		anchor := &model.AnchorFollowerNum{
			FollowerNum: 5000,
			CntDate:     time.Now(),
			Bid:         bid,
		}
		err := q.Create(env.Ctx, anchor)
		require.NoError(t, err, "Failed to create anchor follower num")

		// 查询记录
		rs, err := listByBid[anchorFollowerNumRow](env.Ctx, bid)

		require.NoError(t, err, "Failed to query anchor follower num")
		assert.NotEmpty(t, rs, "Should return at least one record")
		assert.Equal(t, bid, rs[0].Bid, "Bid should match")
		assert.Equal(t, 5000, rs[0].FollowerNum, "FollowerNum should match")
	})

	t.Run("QueryDataNotFound_ShouldReturnEmpty", func(t *testing.T) {
		nonExistentBid := int64(999999999)
		rs, err := listByBid[anchorFollowerNumRow](env.Ctx, nonExistentBid)

		require.NoError(t, err, "Query should not return error for empty result")
		assert.Empty(t, rs, "Should return empty slice for non-existent bid")
	})
}

// TestDatabaseCRUD 数据库增删改查测试
func TestDatabaseCRUD(t *testing.T) {
	env, err := testenv.GetTestEnvironment()
	if err != nil {
		t.Fatalf("Failed to create test environment: %v", err)
	}
	defer env.Cleanup()

	t.Run("Create_ShouldSuccess", func(t *testing.T) {
		q := typed.G[model.AnchorFollowerNum](g.DB)
		anchor := &model.AnchorFollowerNum{
			FollowerNum: 1000,
			CntDate:     time.Now(),
			Bid:         123456,
		}
		err := q.Create(env.Ctx, anchor)

		require.NoError(t, err, "Failed to create anchor follower num")
		assert.Equal(t, 1000, anchor.FollowerNum, "FollowerNum should match")
		assert.Equal(t, int64(123456), anchor.Bid, "Bid should match")
	})

	t.Run("GetByID_ShouldSuccess", func(t *testing.T) {
		q := typed.G[model.AnchorFollowerNum](g.DB)
		createdAnchor := &model.AnchorFollowerNum{
			FollowerNum: 2000,
			CntDate:     time.Now(),
			Bid:         654321,
		}
		err := q.Create(env.Ctx, createdAnchor)
		require.NoError(t, err, "Failed to create anchor follower num")

		queriedAnchor, err := database.GetOne[model.AnchorFollowerNum](env.Ctx, generated.BaseModel.ID.Eq(createdAnchor.ID))

		require.NoError(t, err, "Failed to query anchor follower num")
		assert.Equal(t, createdAnchor.ID, queriedAnchor.ID, "ID should match")
		assert.Equal(t, 2000, queriedAnchor.FollowerNum, "FollowerNum should match")
	})

	t.Run("GetByID_NotFound_ShouldReturnError", func(t *testing.T) {
		nonExistentID := int64(999999999)
		_, err := database.GetOne[model.AnchorFollowerNum](env.Ctx, generated.BaseModel.ID.Eq(nonExistentID))

		assert.Error(t, err, "Should return error for non-existent ID")
	})

	t.Run("Update_ShouldSuccess", func(t *testing.T) {
		createdAnchor := &model.AnchorFollowerNum{
			FollowerNum: 3000,
			CntDate:     time.Now(),
			Bid:         987654,
		}
		err := services.AnchorFollowerNum.Create(&g.AppCtx{C: env.Ctx}, createdAnchor)
		require.NoError(t, err, "Failed to create anchor follower num")

		createdAnchor.FollowerNum = 4000
		err = services.AnchorFollowerNum.Update(&g.AppCtx{C: env.Ctx}, createdAnchor)

		require.NoError(t, err, "Failed to update anchor follower num")
		assert.Equal(t, 4000, createdAnchor.FollowerNum, "FollowerNum should be updated")
	})

	t.Run("Delete_ShouldSuccess", func(t *testing.T) {
		createdAnchor := &model.AnchorFollowerNum{
			FollowerNum: 5000,
			CntDate:     time.Now(),
			Bid:         456789,
		}
		err := services.AnchorFollowerNum.Create(&g.AppCtx{C: env.Ctx}, createdAnchor)
		require.NoError(t, err, "Failed to create anchor follower num")

		err = services.AnchorFollowerNum.Delete(&g.AppCtx{C: env.Ctx}, createdAnchor.ID)
		require.NoError(t, err, "Failed to delete anchor follower num")

		_, err = database.GetOne[model.AnchorFollowerNum](env.Ctx, generated.BaseModel.ID.Eq(createdAnchor.ID))
		assert.Error(t, err, "Should return error after deletion")
	})

	t.Run("Upsert_Insert_ShouldSuccess", func(t *testing.T) {
		cntDate := time.Now().Format("2006-01-02")
		createReq := &req.CreateAnchorFollowerNum{
			FollowerNum: 6000,
			CntDate:     cntDate,
			Bid:         112233,
		}

		err := services.AnchorFollowerNum.Upsert(&g.AppCtx{C: env.Ctx}, createReq)
		require.NoError(t, err, "Failed to upsert anchor follower num")

		parsedDate, _ := time.Parse("2006-01-02", cntDate)
		createdAnchor, err := database.GetOne[model.AnchorFollowerNum](env.Ctx,
			generated.AnchorFollowerNum.Bid.Eq(int64(createReq.Bid)),
			generated.AnchorFollowerNum.CntDate.Eq(parsedDate),
		)

		require.NoError(t, err, "Failed to query upserted record")
		assert.Equal(t, createReq.FollowerNum, createdAnchor.FollowerNum, "FollowerNum should match")
	})

	t.Run("Upsert_Update_ShouldSuccess", func(t *testing.T) {
		cntDate := time.Now().Format("2006-01-02")
		bid := int64(445566)

		// 首次插入
		createReq := &req.CreateAnchorFollowerNum{
			FollowerNum: 6000,
			CntDate:     cntDate,
			Bid:         bid,
		}
		err := services.AnchorFollowerNum.Upsert(&g.AppCtx{C: env.Ctx}, createReq)
		require.NoError(t, err, "Failed to upsert anchor follower num")

		parsedDate, _ := time.Parse("2006-01-02", cntDate)
		createdAnchor, err := database.GetOne[model.AnchorFollowerNum](env.Ctx,
			generated.AnchorFollowerNum.Bid.Eq(bid),
			generated.AnchorFollowerNum.CntDate.Eq(parsedDate),
		)
		require.NoError(t, err, "Failed to query upserted record")

		// 更新操作
		updateReq := &req.CreateAnchorFollowerNum{
			FollowerNum: 7000,
			CntDate:     cntDate,
			Bid:         bid,
		}
		err = services.AnchorFollowerNum.Upsert(&g.AppCtx{C: env.Ctx}, updateReq)
		require.NoError(t, err, "Failed to upsert (update) anchor follower num")

		updatedAnchor, err := database.GetOne[model.AnchorFollowerNum](env.Ctx,
			generated.AnchorFollowerNum.Bid.Eq(bid),
			generated.AnchorFollowerNum.CntDate.Eq(parsedDate),
		)

		require.NoError(t, err, "Failed to query updated record")
		assert.Equal(t, updateReq.FollowerNum, updatedAnchor.FollowerNum, "FollowerNum should be updated")
		assert.Equal(t, createdAnchor.ID, updatedAnchor.ID, "ID should remain the same")
	})

	fmt.Println("All database tests passed!")
}
