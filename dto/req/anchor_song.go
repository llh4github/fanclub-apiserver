package req

// CreateAnchorSong 创建主播歌曲请求
type CreateAnchorSong struct {
	// 歌曲价格(元)
	Price int `json:"price" form:"price" example:"30" validate:"required,min=0,max=10000"`
	// 主播B站ID
	Bid int64 `json:"bid" form:"bid" example:"12345678" validate:"required,min=1"`
	// 歌曲名称
	Name string `json:"name" form:"name" example:"晴天" validate:"required,min=1,max=100"`
	// BV号
	Bv string `json:"bv" form:"bv" example:"BV1xK4y1b7NP" validate:"omitempty,regexp=^BV[a-zA-Z0-9]{10}$"`
}

// UpdateAnchorSong 更新主播歌曲请求
type UpdateAnchorSong struct {
	ID int64 `json:"id,string" validate:"required,min=1"`
	// 主播B站ID
	Bid int64 `json:"bid" form:"bid" example:"12345678" validate:"required,min=1"`
	// 歌曲名称
	Name string `json:"name" form:"name" example:"晴天" validate:"required,min=1,max=100"`
	// 歌曲价格(元)
	Price int `json:"price" form:"price" example:"30" validate:"omitempty,min=0,max=10000"`
	// BV号
	Bv string `json:"bv" form:"bv" example:"BV1xK4y1b7NP" validate:"omitempty,regexp=^BV[a-zA-Z0-9]{10}$"`
}

// GetAnchorSong 根据B站ID和歌曲名称获取主播歌曲请求
type GetAnchorSong struct {
	// 主播B站ID
	Bid int64 `json:"bid" form:"bid" query:"bid" example:"12345678" validate:"required,min=1"`
	// 歌曲名称
	Name string `json:"name" form:"name" query:"name" example:"晴天" validate:"required"`
}

// DeleteAnchorSong 删除主播歌曲请求
type DeleteAnchorSong struct {
	// 主播B站ID
	Bid int64 `json:"bid" form:"bid" example:"12345678" validate:"required,min=1"`
	// 歌曲名称
	Name string `json:"name" form:"name" example:"晴天" validate:"required"`
}

// PageAnchorSong 分页查询主播歌曲请求
type PageAnchorSong struct {
	// 主播B站ID
	Bid int64 `json:"bid" form:"bid" query:"bid" example:"12345678" validate:"omitempty,min=1"`
	// 歌曲名称（模糊查询）
	Name string `json:"name" form:"name" query:"name" example:"晴天" validate:"omitempty"`
	// 分页参数
	PageParam
}

// PageAnchorSongAdmin 后台分页查询主播歌曲请求
type PageAnchorSongAdmin struct {
	// 主播B站ID
	Bid int64 `json:"bid" form:"bid" query:"bid" example:"12345678" validate:"omitempty,min=1"`
	// 歌曲名称（模糊查询）
	Name string `json:"name" form:"name" query:"name" example:"晴天" validate:"omitempty"`
	// 分页参数
	PageParam
}
