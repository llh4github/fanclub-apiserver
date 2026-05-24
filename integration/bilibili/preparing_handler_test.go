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

// TestPreparingHandler_Handle 测试 PREPARING 命令处理器
func TestPreparingHandler_Handle(t *testing.T) {
	env, err := testenv.GetTestEnvironment()
	if err != nil {
		t.Fatalf("Failed to create test environment: %v", err)
	}
	defer env.Cleanup()

	t.Run("EndLiveRecord_WithSendTime_ShouldSuccess", func(t *testing.T) {
		roomID := int64(10001)
		liveKey := "test_preparing_key_001"
		liveTime := time.Now().Add(-2 * time.Hour).Truncate(time.Millisecond)

		// 先插入一条直播中的记录
		insertLiveRecord(t, env, roomID, liveKey, liveTime)

		// 发送 PREPARING 命令结束直播，SendTime 为毫秒时间戳
		expectedEndTime := time.Now().Add(-30 * time.Minute).Truncate(time.Millisecond)
		cmd := &bilibili.Preparing{
			RoomID:   roomID,
			SendTime: expectedEndTime.UnixMilli(),
		}

		err := bilibili.PreparingHandler.Handle(cmd, roomID)
		require.NoError(t, err, "Handle should not return error")

		// 验证直播记录已更新为结束状态
		record, err := database.GetOne[model.AnchorLiveRecord](env.Ctx,
			generated.AnchorLiveRecord.RoomID.Eq(roomID),
			generated.AnchorLiveRecord.LiveKey.Eq(liveKey),
		)
		require.NoError(t, err, "Should find the record")
		assert.Equal(t, consts.END_LIVING, record.LiveStatus, "LiveStatus should be END_LIVING")
		require.NotNil(t, record.EndLiveTime, "EndLiveTime should not be nil")
		require.NotNil(t, record.LiveDuration, "LiveDuration should not be nil")

		// 验证结束时间为 SendTime 对应的时间
		assert.WithinDuration(t, expectedEndTime, *record.EndLiveTime, time.Second, "EndLiveTime should match SendTime")

		// 验证直播时长 = 结束时间 - 开播时间（秒）
		expectedDuration := int(expectedEndTime.Sub(liveTime).Seconds())
		assert.Equal(t, expectedDuration, *record.LiveDuration, "LiveDuration should equal endTime - liveTime in seconds")
	})

	t.Run("EndLiveRecord_ZeroSendTime_ShouldUseCurrentTime", func(t *testing.T) {
		roomID := int64(10002)
		liveKey := "test_preparing_key_002"
		liveTime := time.Now().Add(-1 * time.Hour).Truncate(time.Millisecond)

		// 先插入一条直播中的记录
		insertLiveRecord(t, env, roomID, liveKey, liveTime)

		handleTime := time.Now()

		// SendTime 为零值，应使用当前时间作为结束时间
		cmd := &bilibili.Preparing{
			RoomID:   roomID,
			SendTime: 0,
		}

		err := bilibili.PreparingHandler.Handle(cmd, roomID)
		require.NoError(t, err, "Handle should not return error")

		// 验证直播记录已更新为结束状态
		record, err := database.GetOne[model.AnchorLiveRecord](env.Ctx,
			generated.AnchorLiveRecord.RoomID.Eq(roomID),
			generated.AnchorLiveRecord.LiveKey.Eq(liveKey),
		)
		require.NoError(t, err, "Should find the record")
		assert.Equal(t, consts.END_LIVING, record.LiveStatus, "LiveStatus should be END_LIVING")
		require.NotNil(t, record.EndLiveTime, "EndLiveTime should not be nil")
		require.NotNil(t, record.LiveDuration, "LiveDuration should not be nil")

		// 验证结束时间接近当前时间
		assert.WithinDuration(t, handleTime, *record.EndLiveTime, 5*time.Second, "EndLiveTime should be close to current time")

		// 验证直播时长合理（约 1 小时）
		assert.Greater(t, *record.LiveDuration, 3500, "LiveDuration should be around 1 hour")
		assert.Less(t, *record.LiveDuration, 3700, "LiveDuration should be around 1 hour")
	})

	t.Run("EndLiveRecord_NoLivingRecord_ShouldReturnError", func(t *testing.T) {
		roomID := int64(10003)

		// 不插入任何直播记录，直接发送 PREPARING 命令
		cmd := &bilibili.Preparing{
			RoomID:   roomID,
			SendTime: time.Now().UnixMilli(),
		}

		err := bilibili.PreparingHandler.Handle(cmd, roomID)
		require.Error(t, err, "Handle should return error when no living record found")
	})

	t.Run("EndLiveRecord_AlreadyEnded_ShouldReturnError", func(t *testing.T) {
		roomID := int64(10004)
		liveKey := "test_preparing_key_004"
		liveTime := time.Now().Add(-3 * time.Hour)

		// 先插入一条直播中的记录
		insertLiveRecord(t, env, roomID, liveKey, liveTime)

		// 第一次结束直播
		cmd := &bilibili.Preparing{
			RoomID:   roomID,
			SendTime: time.Now().UnixMilli(),
		}
		err := bilibili.PreparingHandler.Handle(cmd, roomID)
		require.NoError(t, err, "First Handle should not return error")

		// 第二次结束同一条直播记录，应报错（没有直播中的记录了）
		cmd2 := &bilibili.Preparing{
			RoomID:   roomID,
			SendTime: time.Now().UnixMilli(),
		}
		err = bilibili.PreparingHandler.Handle(cmd2, roomID)
		require.Error(t, err, "Second Handle should return error when no living record found")
	})

	t.Run("EndLiveRecord_MultipleLivingRecords_ShouldEndLatest", func(t *testing.T) {
		roomID := int64(10005)

		// 插入两条直播中的记录（不同场次）
		insertLiveRecord(t, env, roomID, "test_preparing_key_005a", time.Now().Add(-5*time.Hour))
		insertLiveRecord(t, env, roomID, "test_preparing_key_005b", time.Now().Add(-1*time.Hour))

		// 发送 PREPARING 命令
		cmd := &bilibili.Preparing{
			RoomID:   roomID,
			SendTime: time.Now().UnixMilli(),
		}
		err := bilibili.PreparingHandler.Handle(cmd, roomID)
		require.NoError(t, err, "Handle should not return error")

		// 验证最新的那条记录被结束（live_time 更晚的那条）
		newerRecord, err := database.GetOne[model.AnchorLiveRecord](env.Ctx,
			generated.AnchorLiveRecord.RoomID.Eq(roomID),
			generated.AnchorLiveRecord.LiveKey.Eq("test_preparing_key_005b"),
		)
		require.NoError(t, err, "Should find the newer record")
		assert.Equal(t, consts.END_LIVING, newerRecord.LiveStatus, "Newer record should be ended")

		// 较早的那条记录仍应为直播中状态
		olderRecord, err := database.GetOne[model.AnchorLiveRecord](env.Ctx,
			generated.AnchorLiveRecord.RoomID.Eq(roomID),
			generated.AnchorLiveRecord.LiveKey.Eq("test_preparing_key_005a"),
		)
		require.NoError(t, err, "Should find the older record")
		assert.Equal(t, consts.LIVING, olderRecord.LiveStatus, "Older record should still be living")
	})

	t.Run("Handle_InvalidCommandType_ShouldReturnError", func(t *testing.T) {
		// 传入非 Preparing 类型的 Command
		cmd := &bilibili.Live{
			LiveKey:  "invalid_cmd_type",
			RoomID:   99999,
			LiveTime: time.Now().Unix(),
		}

		err := bilibili.PreparingHandler.Handle(cmd, 99999)
		require.Error(t, err, "Handle should return error for invalid command type")
	})

	_ = g.DB // ensure g.DB is used reference
}

// insertLiveRecord 插入一条直播中的记录用于测试
func insertLiveRecord(t *testing.T, _ *testenv.TestEnvironment, roomID int64, liveKey string, liveTime time.Time) {
	t.Helper()
	record := &model.AnchorLiveRecord{
		RoomID:     roomID,
		LiveKey:    liveKey,
		LiveTime:   liveTime,
		LiveStatus: consts.LIVING,
	}
	err := g.DB.Create(record).Error
	require.NoError(t, err, "插入直播记录应成功")
}
