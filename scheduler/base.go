package scheduler

import (
	"time"

	"github.com/go-co-op/gocron/v2"
)

func Init() error {
	s, err := gocron.NewScheduler()
	if err != nil {
		return err
	}

	// 项目启动后执行一次：为启用数据监控的主播创建 WebSocket 客户端
	if err := registerJob(s,
		gocron.OneTimeJob(gocron.OneTimeJobStartImmediately()),
		gocron.NewTask(initMonitorWS),
	); err != nil {
		return err
	}

	// 每3小时剔除、销毁无效的 WebSocket 客户端实例
	if err := registerJob(s,
		gocron.DurationJob(3*time.Hour),
		gocron.NewTask(cleanupInvalidWSClients),
	); err != nil {
		return err
	}

	// 每4小时获取并更新主播粉丝数，启动时立即执行一次
	if err := registerJob(s,
		gocron.DurationJob(4*time.Hour),
		gocron.NewTask(fetchFollowerNum),
		gocron.WithStartAt(gocron.WithStartImmediately()),
	); err != nil {
		return err
	}

	// 每天5:30识别每周日程，启动时立即执行一次
	if err := registerJob(s,
		gocron.DailyJob(1, gocron.NewAtTimes(gocron.NewAtTime(5, 30, 0))),
		gocron.NewTask(recognizeWeeklySchedule),
	); err != nil {
		return err
	}

	// 每天1:00关闭已过期的树洞主题
	if err := registerJob(s,
		gocron.DailyJob(1, gocron.NewAtTimes(gocron.NewAtTime(1, 0, 0))),
		gocron.NewTask(deactivateExpiredTopics),
	); err != nil {
		return err
	}

	s.Start()
	return nil
}
