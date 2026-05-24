package model

import "time"

// AnchorLiveSchedule 主播直播日程安排
type AnchorLiveSchedule struct {
	BaseModel
	// B站ID，通常称为UID
	Bid int64 `json:"bid" gorm:"not null;check:bid >= 0;index:idx_anchor_live_schedule_idx,priority:1"`
	// 直播主题
	Topic string `json:"topic" gorm:"type:varchar(255);not null;index:idx_anchor_live_schedule_idx,priority:2"`
	// 直播主题emoji
	Emoji string `json:"emoji" gorm:"type:varchar(10);not null;default:''"`
	// 直播开始时间
	StartTime time.Time `json:"start_time" gorm:"type:timestamptz;not null;index:idx_anchor_live_schedule_idx,priority:3"`
	// 直播结束时间
	EndTime time.Time `json:"end_time" gorm:"type:timestamptz;not null;index:idx_anchor_live_schedule_idx,priority:4"`
}

// TableName 指定表名
func (AnchorLiveSchedule) TableName() string {
	return "anchor_live_schedule"
}
