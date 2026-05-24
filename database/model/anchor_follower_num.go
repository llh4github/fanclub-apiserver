package model

import (
	"time"
)

// AnchorFollowerNum 主播粉丝数
type AnchorFollowerNum struct {
	BaseModel
	// 粉丝数
	FollowerNum int `json:"follower_num" gorm:"not null"`
	// 统计日期
	CntDate time.Time `json:"cnt_date" gorm:"type:date;not null;uniqueIndex:idx_bid_cntdate"`
	// B 站 UID
	Bid int64 `json:"bid" gorm:"not null;uniqueIndex:idx_bid_cntdate"`
}

// TableName 指定表名
func (AnchorFollowerNum) TableName() string {
	return "anchor_follower_num"
}
