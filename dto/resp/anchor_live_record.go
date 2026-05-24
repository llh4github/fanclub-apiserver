package resp

import (
	"time"

	"fanclub-apiserver/consts"
)

// LatestLiveRecord 最新直播记录简要信息
type LatestLiveRecord struct {
	// 直播标识
	LiveKey string `json:"live_key"`
	// 开播时间
	LiveTime time.Time `json:"live_time"`
	// 直播状态: 0=未知, 1=直播中, 2=已结束, 3=超时结束
	LiveStatus consts.LiveRecordStatus `json:"live_status"`
	// 结束直播时间
	EndLiveTime *time.Time `json:"end_live_time"`
	// 直播时长（秒）
	LiveDuration int `json:"live_duration"`
}

// WeekLiveRecord 本周直播记录简要信息
type WeekLiveRecord struct {
	// 直播标识
	LiveKey string `json:"live_key"`
	// 开播时间
	LiveTime time.Time `json:"live_time"`
	// 直播状态: 0=未知, 1=直播中, 2=已结束, 3=超时结束
	LiveStatus consts.LiveRecordStatus `json:"live_status"`
	// 结束直播时间
	EndLiveTime *time.Time `json:"end_live_time"`
	// 直播时长（秒）
	LiveDuration int `json:"live_duration"`
}
