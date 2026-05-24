package resp

// AnchorSongSimple 主播歌曲简要信息
type AnchorSongSimple struct {
	// 歌曲价格(元)
	Price int `json:"price"`
	// 歌曲名称
	Name string `json:"name"`
	// BV号
	Bv string `json:"bv"`
}
