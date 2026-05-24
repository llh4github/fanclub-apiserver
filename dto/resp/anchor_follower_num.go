package resp

import "time"

// AnchorFollowerNumSimple 只包含主要数据字段
type AnchorFollowerNumSimple struct {
	// 粉丝数
	FollowerNum int `json:"follower_num"`
	// 统计日期
	CntDate time.Time `json:"cnt_date"`
	// B 站 UID
	Bid int64 `json:"bid"`
}

// DailyFollowerNum 每日粉丝数
type DailyFollowerNum struct {
	// 统计日期
	Date string `json:"date"`
	// 粉丝数
	Num int `json:"num"`
}
