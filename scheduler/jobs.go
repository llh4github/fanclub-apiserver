package scheduler

import (
	"context"
	"time"

	"fanclub-apiserver/ai"
	"fanclub-apiserver/bilibili"
	"fanclub-apiserver/consts"
	"fanclub-apiserver/g"
	"fanclub-apiserver/services"
	"fmt"

	"github.com/go-co-op/gocron/v2"
	"go.uber.org/zap"
)

// initMonitorWS 创建并连接监控 WebSocket 客户端
func initMonitorWS() {
	var clients []*bilibili.WSClient
	var err error

	for retry := 0; retry < 3; retry++ {
		clients, err = bilibili.CreateMonitorWSClients(context.Background())
		if err != nil {
			g.Error("创建监控 WebSocket 客户端失败，3秒后重试",
				zap.Int("retry", retry+1),
				zap.Error(err),
			)
			time.Sleep(3 * time.Second)
			continue
		}

		connectedCount := 0
		for _, c := range clients {
			if connErr := c.Connect(); connErr != nil {
				g.Error("连接弹幕服务器失败",
					zap.Int64("roomID", c.Config().RoomID),
					zap.Error(connErr),
				)
				continue
			}
			connectedCount++
		}

		if connectedCount > 0 {
			g.Info("创建并连接监控 WebSocket 客户端成功",
				zap.Int("total_clients", len(clients)),
				zap.Int("connected", connectedCount),
				zap.Int("retry", retry+1),
			)
			bilibili.WSManager.Add(clients...)
			return
		}

		g.Error("所有客户端连接失败，3秒后重试",
			zap.Int("retry", retry+1),
		)
		time.Sleep(3 * time.Second)
	}

	if err != nil {
		g.Error("创建监控 WebSocket 客户端最终失败", zap.Error(err))
		return
	}
	g.Error("连接监控 WebSocket 客户端最终失败")
}

// cleanupInvalidWSClients 剔除并销毁无效的 WebSocket 客户端
func cleanupInvalidWSClients() {
	removed := bilibili.WSManager.RemoveAndClose()
	if removed > 0 {
		g.Info("清理无效 WebSocket 客户端", zap.Int("removed", removed))
	}
}

// fetchFollowerNum 获取并更新主播粉丝数
func fetchFollowerNum() {
	if err := bilibili.FetchAndUpsertFollowerNum(context.Background()); err != nil {
		g.Error("获取主播粉丝数任务失败", zap.Error(err))
	}
}

// recognizeWeeklySchedule 识别每周日程
func recognizeWeeklySchedule() {
	url := "https://www.bilibili.com/opus/1159880489834643463" // 暂时先写死吧
	if err := ai.RecognizeScheduleFromURL(context.Background(), url, consts.Liko.BID); err != nil {
		g.Error("识别每周日程任务失败", zap.Error(err))
	}
}

// deactivateExpiredTopics 关闭已过期的主题
func deactivateExpiredTopics() {
	count, err := services.TreeholeTopic.DeactivateExpiredTopics(context.Background())
	if err != nil {
		g.Error("关闭过期主题任务失败", zap.Error(err))
		return
	}
	if count > 0 {
		g.Info("定时关闭过期主题完成", zap.Int64("affected", count))
	}
}

// registerJob 注册定时任务并打印任务 ID
func registerJob(s gocron.Scheduler, def gocron.JobDefinition, task gocron.Task, opts ...gocron.JobOption) error {
	j, err := s.NewJob(def, task, opts...)
	if err != nil {
		return err
	}
	g.Info("注册定时任务", zap.String("jobID", fmt.Sprintf("%v", j.ID())))
	return nil
}
