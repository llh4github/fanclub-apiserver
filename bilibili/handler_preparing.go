package bilibili

import (
	"context"
	"fmt"
	"time"

	"fanclub-apiserver/consts"
	"fanclub-apiserver/database/generated"
	"fanclub-apiserver/database/model"
	"fanclub-apiserver/g"

	"go.uber.org/zap"
	"gorm.io/cli/gorm/typed"
	"gorm.io/gorm"
)

// preparingHandler PREPARING 命令处理器，处理下播事件并更新直播记录
type preparingHandler struct {
}

// PreparingHandler PREPARING 命令处理器实例
var PreparingHandler = new(preparingHandler)

// SupportedCmd 返回支持的命令类型
func (h *preparingHandler) SupportedCmd() string {
	return "PREPARING"
}

// Handle 处理 PREPARING 命令，将当前 room_id 下最近一条直播中的记录更新为结束直播
// SendTime 为毫秒时间戳，零值时使用当前时间作为结束时间
func (h *preparingHandler) Handle(cmd Command, roomID int64) error {
	preparing, ok := cmd.(*Preparing)
	if !ok {
		return fmt.Errorf("invalid command type for PREPARING handler")
	}

	return endLiveRecord(context.Background(), roomID, preparing)
}

// endLiveRecord 结束直播记录，更新最近一条直播中状态为结束
func endLiveRecord(ctx context.Context, roomID int64, preparing *Preparing) error {
	// 查询当前 room_id 下最近一条直播中的记录（按 live_time 降序取第一条）
	q := typed.G[model.AnchorLiveRecord](g.DB)
	record, err := q.
		Select(generated.BaseModel.ID, generated.AnchorLiveRecord.LiveTime, generated.AnchorLiveRecord.LiveKey).
		Where(generated.AnchorLiveRecord.RoomID.Eq(roomID)).
		Where(generated.AnchorLiveRecord.LiveStatus.Eq(consts.LIVING)).
		Scopes(func(db *gorm.Statement) {
			db.DB = db.DB.Order("live_time DESC")
		}).
		First(ctx)
	if err != nil {
		g.Warn("未找到直播中的记录",
			zap.Int64("room_id", roomID),
			zap.Error(err),
		)
		return fmt.Errorf("未找到 room_id=%d 直播中的记录: %w", roomID, err)
	}

	// 确定结束时间：SendTime 为毫秒时间戳，零值使用当前时间
	var endTime time.Time
	if preparing.SendTime > 0 {
		endTime = time.UnixMilli(preparing.SendTime)
	} else {
		endTime = time.Now()
	}

	// 计算直播时长（秒）
	duration := int(endTime.Sub(record.LiveTime).Seconds())

	// 更新直播记录：状态、结束时间、直播时长
	_, err = typed.G[model.AnchorLiveRecord](g.DB).
		Where(generated.BaseModel.ID.Eq(record.ID)).
		Updates(ctx, model.AnchorLiveRecord{
			LiveStatus:   consts.END_LIVING,
			EndLiveTime:  &endTime,
			LiveDuration: &duration,
		})
	if err != nil {
		g.Error("更新直播记录失败",
			zap.Int64("room_id", roomID),
			zap.Int64("record_id", record.ID),
			zap.Error(err),
		)
		return fmt.Errorf("更新直播记录失败: %w", err)
	}

	g.Info("结束直播记录成功",
		zap.Int64("room_id", roomID),
		zap.String("live_key", record.LiveKey),
		zap.Time("end_time", endTime),
		zap.Int("duration_sec", duration),
	)

	// 清理直播记录缓存
	cleanLiveRecordCache(context.Background(), roomID)

	return nil
}
