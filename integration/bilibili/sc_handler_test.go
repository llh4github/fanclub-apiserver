package bilibili

import (
	"testing"
	"time"

	"fanclub-apiserver/bilibili"
	"fanclub-apiserver/database"
	"fanclub-apiserver/database/generated"
	"fanclub-apiserver/database/model"
	"fanclub-apiserver/g"
	"fanclub-apiserver/integration/testenv"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

// TestScHandler_Handle 测试 SUPER_CHAT_MESSAGE 命令处理器
func TestScHandler_Handle(t *testing.T) {
	env, err := testenv.GetTestEnvironment()
	if err != nil {
		t.Fatalf("Failed to create test environment: %v", err)
	}
	defer env.Cleanup()

	t.Run("InsertScBvRecord_WithBVInMessage_ShouldSuccess", func(t *testing.T) {
		roomID := int64(20001)
		scID := int64(100001)
		bid := int64(888888)
		sendTime := time.Now().Add(-10 * time.Minute).Truncate(time.Millisecond)

		cmd := &bilibili.SuperChat{
			Data: &bilibili.SuperChatData{
				ID:       scID,
				Message:  "帮我点播 BV1xK4y1b7NP 这个视频",
				UID:      bid,
				SendTime: sendTime.UnixMilli(),
			},
			SendTime: sendTime.UnixMilli(),
		}

		err := bilibili.ScHandler.Handle(cmd, roomID)
		require.NoError(t, err, "Handle should not return error")

		// 验证数据已写入数据库
		record, err := database.GetOne[model.ViewerScBvRecord](env.Ctx,
			generated.ViewerScBvRecord.SCID.Eq(scID),
		)
		require.NoError(t, err, "Should find the inserted record")
		assert.Equal(t, scID, record.SCID, "SCID should match")
		assert.Equal(t, roomID, record.RoomID, "RoomID should match")
		assert.Equal(t, "BV1xK4y1b7NP", record.BV, "BV should match")
		assert.Equal(t, bid, record.BID, "BID should match")
		assert.WithinDuration(t, sendTime, record.SendTime, time.Second, "SendTime should match")
	})

	t.Run("InsertOnly_DuplicateSCID_ShouldSkip", func(t *testing.T) {
		roomID := int64(20002)
		scID := int64(100002)
		bid := int64(777777)

		// 第一次插入
		cmd1 := &bilibili.SuperChat{
			Data: &bilibili.SuperChatData{
				ID:      scID,
				Message: "点播 BV1AB2CD3EF4",
				UID:     bid,
			},
			SendTime: time.Now().UnixMilli(),
		}
		err := bilibili.ScHandler.Handle(cmd1, roomID)
		require.NoError(t, err, "First insert should succeed")

		// 查询第一次的记录
		firstRecord, err := database.GetOne[model.ViewerScBvRecord](env.Ctx,
			generated.ViewerScBvRecord.SCID.Eq(scID),
		)
		require.NoError(t, err, "Should find the first record")

		// 第二次插入（模拟重复消息）
		cmd2 := &bilibili.SuperChat{
			Data: &bilibili.SuperChatData{
				ID:      scID,
				Message: "点播 BV1AB2CD3EF4",
				UID:     bid,
			},
			SendTime: time.Now().UnixMilli(),
		}
		err = bilibili.ScHandler.Handle(cmd2, roomID)
		require.NoError(t, err, "Duplicate insert should not return error")

		// 验证数据未被覆盖，仍是第一次的记录
		secondRecord, err := database.GetOne[model.ViewerScBvRecord](env.Ctx,
			generated.ViewerScBvRecord.SCID.Eq(scID),
		)
		require.NoError(t, err, "Should find the record")
		assert.Equal(t, firstRecord.ID, secondRecord.ID, "ID should remain the same (insert_only)")
	})

	t.Run("Handle_NoBVInMessage_ShouldSkip", func(t *testing.T) {
		roomID := int64(20003)

		// SC 消息中不包含 BV 号
		cmd := &bilibili.SuperChat{
			Data: &bilibili.SuperChatData{
				ID:      int64(100003),
				Message: "主播加油！",
				UID:     int64(666666),
			},
			SendTime: time.Now().UnixMilli(),
		}

		err := bilibili.ScHandler.Handle(cmd, roomID)
		require.NoError(t, err, "Handle should not return error when no BV found")
	})

	t.Run("Handle_NilData_ShouldSkip", func(t *testing.T) {
		roomID := int64(20004)

		// Data 为 nil
		cmd := &bilibili.SuperChat{
			Data:     nil,
			SendTime: time.Now().UnixMilli(),
		}

		err := bilibili.ScHandler.Handle(cmd, roomID)
		require.NoError(t, err, "Handle should not return error when Data is nil")
	})

	t.Run("Handle_InvalidCommandType_ShouldReturnError", func(t *testing.T) {
		// 传入非 SuperChat 类型的 Command
		cmd := &bilibili.Live{
			LiveKey:  "invalid_sc_cmd",
			RoomID:   99999,
			LiveTime: time.Now().Unix(),
		}

		err := bilibili.ScHandler.Handle(cmd, 99999)
		require.Error(t, err, "Handle should return error for invalid command type")
	})

	t.Run("Handle_ZeroSendTime_ShouldUseCurrentTime", func(t *testing.T) {
		roomID := int64(20005)
		scID := int64(100005)
		bid := int64(555555)
		handleTime := time.Now()

		// SendTime 为零值，应使用当前时间
		cmd := &bilibili.SuperChat{
			Data: &bilibili.SuperChatData{
				ID:      scID,
				Message: "播放 BV1ZZ99YY88X",
				UID:     bid,
			},
			SendTime: 0,
		}

		err := bilibili.ScHandler.Handle(cmd, roomID)
		require.NoError(t, err, "Handle should not return error")

		// 验证数据已写入数据库
		record, err := database.GetOne[model.ViewerScBvRecord](env.Ctx,
			generated.ViewerScBvRecord.SCID.Eq(scID),
		)
		require.NoError(t, err, "Should find the inserted record")
		assert.WithinDuration(t, handleTime, record.SendTime, 5*time.Second, "SendTime should be close to current time when zero")
	})

	t.Run("Handle_MultipleBVInMessage_ShouldExtractFirst", func(t *testing.T) {
		roomID := int64(20006)
		scID := int64(100006)
		bid := int64(444444)

		// 消息中包含多个 BV 号，应提取第一个
		cmd := &bilibili.SuperChat{
			Data: &bilibili.SuperChatData{
				ID:      scID,
				Message: "帮我播 BV1aAbBcC1dD 和 BV2eEfFgG2hH",
				UID:     bid,
			},
			SendTime: time.Now().UnixMilli(),
		}

		err := bilibili.ScHandler.Handle(cmd, roomID)
		require.NoError(t, err, "Handle should not return error")

		// 验证只提取了第一个 BV 号
		record, err := database.GetOne[model.ViewerScBvRecord](env.Ctx,
			generated.ViewerScBvRecord.SCID.Eq(scID),
		)
		require.NoError(t, err, "Should find the inserted record")
		assert.Equal(t, "BV1aAbBcC1dD", record.BV, "Should extract the first BV")
	})

	_ = g.DB // ensure g.DB is used reference
}
