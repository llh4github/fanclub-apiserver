package bilibili

import (
	"context"
	"fmt"
	"strconv"
	"time"

	"fanclub-apiserver/cache"
	"fanclub-apiserver/consts"
	"fanclub-apiserver/database/generated"
	"fanclub-apiserver/database/model"
	"fanclub-apiserver/g"

	"go.uber.org/zap"
	"gorm.io/cli/gorm/typed"
	"gorm.io/gorm/clause"
)

// liveHandler LIVE 命令处理器，处理开播事件并保存直播记录
type liveHandler struct {
}

// LiveHandler LIVE 命令处理器实例
var LiveHandler = new(liveHandler)

// SupportedCmd 返回支持的命令类型
func (h *liveHandler) SupportedCmd() string {
	return "LIVE"
}

// Handle 处理 LIVE 命令，将直播记录 insert_only 写入数据库
// live_key 唯一，冲突时跳过插入
func (h *liveHandler) Handle(cmd Command, roomID int64) error {
	live, ok := cmd.(*Live)
	if !ok {
		return fmt.Errorf("invalid command type for LIVE handler")
	}

	// 使用 roomID 参数（来自 WebSocket 连接），而非 Live 结构体中的 RoomID
	// 因为网络波动可能重复发送同一条开播消息，使用 insert_only 策略
	return insertOnlyLiveRecord(context.Background(), roomID, live)
}

// insertOnlyLiveRecord 插入直播记录，live_key 冲突时跳过
func insertOnlyLiveRecord(ctx context.Context, roomID int64, live *Live) error {
	liveTime := time.Unix(live.LiveTime, 0)
	record := &model.AnchorLiveRecord{
		RoomID:     roomID,
		LiveKey:    live.LiveKey,
		LiveTime:   liveTime,
		LiveStatus: consts.LIVING,
	}

	q := typed.G[model.AnchorLiveRecord](g.DB, clause.OnConflict{
		Columns:   []clause.Column{generated.AnchorLiveRecord.RoomID.Column(), generated.AnchorLiveRecord.LiveKey.Column()},
		DoNothing: true,
	})

	if err := q.Create(ctx, record); err != nil {
		g.Error("保存直播记录失败",
			zap.Int64("room_id", roomID),
			zap.String("live_key", live.LiveKey),
			zap.Error(err),
		)
		return fmt.Errorf("保存直播记录失败: %w", err)
	}

	g.Info("保存直播记录成功",
		zap.Int64("room_id", roomID),
		zap.String("live_key", live.LiveKey),
	)

	// 清理直播记录缓存
	cleanLiveRecordCache(context.Background(), roomID)

	return nil
}

// cleanLiveRecordCache 清理直播记录相关缓存
func cleanLiveRecordCache(ctx context.Context, roomID int64) {
	pattern := string(cache.AnchorLiveRecord) + strconv.FormatInt(roomID, 10) + "*"
	if _, err := cache.ScanUnlinkKeys(ctx, pattern); err != nil {
		g.Warn("清理直播记录缓存失败",
			zap.Int64("room_id", roomID),
			zap.Error(err),
		)
	}
}
