package bilibili

import (
	"context"
	"fmt"
	"time"

	"fanclub-apiserver/database/generated"
	"fanclub-apiserver/database/model"
	"fanclub-apiserver/g"
	"fanclub-apiserver/utils"

	"go.uber.org/zap"
	"gorm.io/cli/gorm/typed"
	"gorm.io/gorm/clause"
)

// scHandler SUPER_CHAT_MESSAGE 命令处理器，处理超级留言中的BV号点播
type scHandler struct {
}

// ScHandler SUPER_CHAT_MESSAGE 命令处理器实例
var ScHandler = new(scHandler)

// SupportedCmd 返回支持的命令类型
func (h *scHandler) SupportedCmd() string {
	return "SUPER_CHAT_MESSAGE"
}

// Handle 处理超级留言命令，从SC消息中提取BV号并保存记录
// sc_id 唯一，冲突时跳过插入（insert_only）
func (h *scHandler) Handle(cmd Command, roomID int64) error {
	sc, ok := cmd.(*SuperChat)
	if !ok {
		return fmt.Errorf("invalid command type for SUPER_CHAT_MESSAGE handler")
	}

	// Data 为空时跳过
	if sc.Data == nil {
		return nil
	}

	// 从消息中提取BV号，未找到则跳过
	bv := utils.ExtractBV(sc.Data.Message)
	if bv == "" {
		return nil
	}

	return insertOnlyScBvRecord(context.Background(), roomID, sc, bv)
}

// insertOnlyScBvRecord 插入SC点播BV记录，sc_id 冲突时跳过
func insertOnlyScBvRecord(ctx context.Context, roomID int64, sc *SuperChat, bv string) error {
	// 确定发送时间：SendTime 为毫秒时间戳，零值使用当前时间
	var sendTime time.Time
	if sc.SendTime > 0 {
		sendTime = time.UnixMilli(sc.SendTime)
	} else {
		sendTime = time.Now()
	}

	record := &model.ViewerScBvRecord{
		SCID:     sc.Data.ID,
		RoomID:   roomID,
		BV:       bv,
		BID:      sc.Data.UID,
		SendTime: sendTime,
	}

	q := typed.G[model.ViewerScBvRecord](g.DB, clause.OnConflict{
		Columns:   []clause.Column{generated.ViewerScBvRecord.SCID.Column()},
		DoNothing: true,
	})

	if err := q.Create(ctx, record); err != nil {
		g.Error("保存SC点播BV记录失败",
			zap.Int64("room_id", roomID),
			zap.Int64("sc_id", sc.Data.ID),
			zap.String("bv", bv),
			zap.Error(err),
		)
		return fmt.Errorf("保存SC点播BV记录失败: %w", err)
	}

	g.Info("保存SC点播BV记录成功",
		zap.Int64("room_id", roomID),
		zap.Int64("sc_id", sc.Data.ID),
		zap.String("bv", bv),
	)
	return nil
}
