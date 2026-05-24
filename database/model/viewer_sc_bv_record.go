package model

import "time"

// ViewerScBvRecord 观众SC点播的BV号记录
type ViewerScBvRecord struct {
	BaseModel
	// SC的ID
	SCID int64 `json:"sc_id" gorm:"column:sc_id;not null;uniqueIndex:uk_viewer_sc_bv_record_sc_id"`
	// 房间ID
	RoomID int64 `json:"room_id" gorm:"not null;check:room_id >= 0;index:idx_viewer_sc_bv_record_room_bv,priority:1"`
	// 发送的BV号
	BV string `json:"bv" gorm:"column:bv;type:varchar(12);not null;index:idx_viewer_sc_bv_record_room_bv,priority:2"`
	// 发送者的BID
	BID int64 `json:"bid" gorm:"column:bid;not null;check:bid >= 0"`
	// 发送时间
	SendTime time.Time `json:"send_time" gorm:"not null"`
}

// TableName 指定表名
func (ViewerScBvRecord) TableName() string {
	return "viewer_sc_bv_record"
}
