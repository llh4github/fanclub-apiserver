package bilibili

import (
	"context"
	"fmt"
	"strconv"

	"fanclub-apiserver/cache"
	"fanclub-apiserver/g"

	"go.uber.org/zap"
)

// danmuHandler DANMU_MSG 命令处理器，处理弹幕消息并进行去重计数统计
type danmuHandler struct {
}

// DanmuHandler DANMU_MSG 命令处理器实例
var DanmuHandler = new(danmuHandler)

// SupportedCmd 返回支持的命令类型
func (h *danmuHandler) SupportedCmd() string {
	return "DANMU_MSG"
}

// Handle 处理弹幕消息，调用 Redis Function 进行去重计数
func (h *danmuHandler) Handle(cmd Command, roomID int64) error {
	danmu, ok := cmd.(*DanmuMsg)
	if !ok {
		return fmt.Errorf("invalid command type for DANMU_MSG handler")
	}

	uid := danmu.UID()
	if uid <= 0 {
		return nil
	}

	timestamp := danmu.Timestamp()
	key := string(cache.DanmuStatistics) + strconv.FormatInt(roomID, 10)
	uidStr := strconv.FormatInt(uid, 10)
	tsStr := strconv.FormatInt(timestamp, 10)

	result, err := cache.StatisticsDanmu(context.Background(), key, uidStr, tsStr)
	if err != nil {
		g.Error("弹幕统计计数失败",
			zap.Int64("room_id", roomID),
			zap.Int64("uid", uid),
			zap.Error(err),
		)
		return fmt.Errorf("弹幕统计计数失败: %w", err)
	}

	// result=1 计数成功，result=0 重复已存在
	if result == 1 {
		g.Debug("弹幕统计计数成功",
			zap.Int64("room_id", roomID),
			zap.Int64("uid", uid),
		)
	}

	return nil
}
