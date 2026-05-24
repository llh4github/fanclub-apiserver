package model

import (
	"time"

	"fanclub-apiserver/consts"
)

// AnchorLiveRecord 主播直播记录
type AnchorLiveRecord struct {
	BaseModel
	// 直播间 ID
	RoomID int64 `json:"room_id" gorm:"not null;uniqueIndex:anchor_live_record_room_live_record_uk;check:room_id >= 0"`
	// 直播场次 key
	LiveKey string `json:"live_key" gorm:"type:varchar(255);not null;uniqueIndex:anchor_live_record_room_live_record_uk"`
	// 直播开始时间
	LiveTime time.Time `json:"live_time" gorm:"not null;index:anchor_live_record_anchor_live_record_live_time_index,priority:1,sort:desc"`
	// 直播状态
	LiveStatus consts.LiveRecordStatus `json:"live_status" gorm:"type:smallint;not null;check:live_status >= 0"`
	// 直播结束时间
	EndLiveTime *time.Time `json:"end_live_time" gorm:""`
	// 直播时长 (秒)
	LiveDuration *int `json:"live_duration" gorm:"check:live_duration >= 0"`
}

// TableName 指定表名
func (AnchorLiveRecord) TableName() string {
	return "anchor_live_record"
}
