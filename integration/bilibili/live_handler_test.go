package bilibili

import (
	"testing"
	"time"

	"fanclub-apiserver/bilibili"
	"fanclub-apiserver/consts"
	"fanclub-apiserver/database"
	"fanclub-apiserver/database/generated"
	"fanclub-apiserver/database/model"
	"fanclub-apiserver/g"
	"fanclub-apiserver/integration/testenv"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

// TestLiveHandler_Handle 测试 LIVE 命令处理器
func TestLiveHandler_Handle(t *testing.T) {
	env, err := testenv.GetTestEnvironment()
	if err != nil {
		t.Fatalf("Failed to create test environment: %v", err)
	}
	defer env.Cleanup()

	t.Run("InsertLiveRecord_ShouldSuccess", func(t *testing.T) {
		roomID := int64(12345)
		liveTime := time.Now().Unix()
		cmd := &bilibili.Live{
			LiveKey:  "test_live_key_001",
			RoomID:   roomID,
			LiveTime: liveTime,
		}

		err := bilibili.LiveHandler.Handle(cmd, roomID)
		require.NoError(t, err, "Handle should not return error")

		// 验证数据已写入数据库
		record, err := database.GetOne[model.AnchorLiveRecord](env.Ctx,
			generated.AnchorLiveRecord.RoomID.Eq(roomID),
			generated.AnchorLiveRecord.LiveKey.Eq("test_live_key_001"),
		)
		require.NoError(t, err, "Should find the inserted record")
		assert.Equal(t, roomID, record.RoomID, "RoomID should match")
		assert.Equal(t, "test_live_key_001", record.LiveKey, "LiveKey should match")
		assert.Equal(t, consts.LIVING, record.LiveStatus, "LiveStatus should be LIVING")
	})

	t.Run("InsertOnly_DuplicateLiveKey_ShouldSkip", func(t *testing.T) {
		roomID := int64(54321)
		liveKey := "test_live_key_dup_001"
		liveTime := time.Now().Unix()

		// 第一次插入
		cmd1 := &bilibili.Live{
			LiveKey:  liveKey,
			RoomID:   roomID,
			LiveTime: liveTime,
		}
		err := bilibili.LiveHandler.Handle(cmd1, roomID)
		require.NoError(t, err, "First insert should succeed")

		// 查询第一次的记录
		firstRecord, err := database.GetOne[model.AnchorLiveRecord](env.Ctx,
			generated.AnchorLiveRecord.RoomID.Eq(roomID),
			generated.AnchorLiveRecord.LiveKey.Eq(liveKey),
		)
		require.NoError(t, err, "Should find the first record")

		// 第二次插入（模拟网络波动重复发送）
		cmd2 := &bilibili.Live{
			LiveKey:  liveKey,
			RoomID:   roomID,
			LiveTime: liveTime,
		}
		err = bilibili.LiveHandler.Handle(cmd2, roomID)
		require.NoError(t, err, "Duplicate insert should not return error")

		// 验证数据未被覆盖，仍是第一次的记录
		secondRecord, err := database.GetOne[model.AnchorLiveRecord](env.Ctx,
			generated.AnchorLiveRecord.RoomID.Eq(roomID),
			generated.AnchorLiveRecord.LiveKey.Eq(liveKey),
		)
		require.NoError(t, err, "Should find the record")
		assert.Equal(t, firstRecord.ID, secondRecord.ID, "ID should remain the same (insert_only)")
	})

	t.Run("InsertMultipleLiveRecords_DifferentLiveKey_ShouldSuccess", func(t *testing.T) {
		roomID := int64(99999)

		// 同一房间不同场次的直播
		cmd1 := &bilibili.Live{
			LiveKey:  "test_multi_key_001",
			RoomID:   roomID,
			LiveTime: time.Now().Add(-2 * time.Hour).Unix(),
		}
		err := bilibili.LiveHandler.Handle(cmd1, roomID)
		require.NoError(t, err, "First live record should succeed")

		cmd2 := &bilibili.Live{
			LiveKey:  "test_multi_key_002",
			RoomID:   roomID,
			LiveTime: time.Now().Unix(),
		}
		err = bilibili.LiveHandler.Handle(cmd2, roomID)
		require.NoError(t, err, "Second live record should succeed")

		// 验证两条记录都存在
		r1, err := database.GetOne[model.AnchorLiveRecord](env.Ctx,
			generated.AnchorLiveRecord.RoomID.Eq(roomID),
			generated.AnchorLiveRecord.LiveKey.Eq("test_multi_key_001"),
		)
		require.NoError(t, err, "Should find first record")
		assert.Equal(t, "test_multi_key_001", r1.LiveKey)

		r2, err := database.GetOne[model.AnchorLiveRecord](env.Ctx,
			generated.AnchorLiveRecord.RoomID.Eq(roomID),
			generated.AnchorLiveRecord.LiveKey.Eq("test_multi_key_002"),
		)
		require.NoError(t, err, "Should find second record")
		assert.Equal(t, "test_multi_key_002", r2.LiveKey)
		assert.NotEqual(t, r1.ID, r2.ID, "Different records should have different IDs")
	})

	_ = g.DB // ensure g.DB is used reference
}
