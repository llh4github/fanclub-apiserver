package resp

import "time"

// AnchorLiveSchedule 主播直播日程
type AnchorLiveSchedule struct {
	// 直播主题
	Topic string `json:"topic"`
	// 直播主题emoji
	Emoji string `json:"emoji"`
	// 直播开始时间
	StartTime time.Time `json:"start_time"`
	// 直播结束时间
	EndTime time.Time `json:"end_time"`
}
