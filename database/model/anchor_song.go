package model

// AnchorSong 主播歌曲
type AnchorSong struct {
	BaseModel
	// 歌曲价格(元)
	Price int `json:"price" gorm:"default:0;check:price >= 0"`
	// 主播B站ID
	Bid int64 `json:"bid" gorm:"not null;uniqueIndex:anchor_song_bid_name_uindex,priority:1;check:bid >= 0"`
	// 歌曲名称
	Name string `json:"name" gorm:"type:varchar(255);not null;uniqueIndex:anchor_song_bid_name_uindex,priority:2"`
	// BV号
	Bv string `json:"bv" gorm:"type:varchar(15);default:''"`
}

// TableName 指定表名
func (AnchorSong) TableName() string {
	return "anchor_song"
}
