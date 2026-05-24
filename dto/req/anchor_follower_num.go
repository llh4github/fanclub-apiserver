package req

// CreateAnchorFollowerNum 创建主播粉丝数记录请求
type CreateAnchorFollowerNum struct {
	// 粉丝数
	FollowerNum int `json:"follower_num" form:"follower_num" example:"123456" validate:"required,min=0"`
	// 统计日期
	CntDate string `json:"cnt_date" form:"cnt_date" example:"2026-04-25" validate:"required,datetime=2006-01-02"`
	// B站UID
	Bid int64 `json:"bid,string" example:"12345678" validate:"required,min=1"`
}

// GetAnchorFollowerNum 根据B站UID和日期获取主播粉丝数记录请求
type GetAnchorFollowerNum struct {
	// B站UID
	Bid int64 `json:"bid,string" form:"bid" example:"12345678" validate:"required,min=1"`
	// 统计日期
	CntDate string `json:"cnt_date" form:"cnt_date" example:"2026-04-25" validate:"required,datetime=2006-01-02"`
}

// ListAnchorFollowerNum 获取主播粉丝数记录列表请求
type ListAnchorFollowerNum struct {
	// 每页数量
	PageSize int `json:"page_size" form:"limit" example:"10" validate:"omitempty,min=1,max=100"`
	// 偏移量
	PageIndex int `json:"page_index" form:"offset" example:"0" validate:"omitempty,min=0"`
}

// UpdateAnchorFollowerNum 更新主播粉丝数记录请求
type UpdateAnchorFollowerNum struct {
	// B站UID
	Bid int64 `json:"bid,string" form:"bid" example:"12345678" validate:"required,min=1"`
	// 统计日期
	CntDate string `json:"cnt_date" form:"cnt_date" example:"2026-04-25" validate:"required,datetime=2006-01-02"`
	// 粉丝数
	FollowerNum int `json:"follower_num" form:"follower_num" example:"123456" validate:"omitempty,min=0"`
}

// DeleteAnchorFollowerNum 删除主播粉丝数记录请求
type DeleteAnchorFollowerNum struct {
	// B站UID
	Bid int64 `json:"bid,string" form:"bid" example:"12345678" validate:"required,min=1"`
	// 统计日期
	CntDate string `json:"cnt_date" form:"cnt_date" example:"2026-04-25" validate:"required,datetime=2006-01-02"`
}

// GetPastFollowerNum 获取过去x天粉丝数请求
type GetPastFollowerNum struct {
	// B站UID
	Bid int64 `json:"bid,string" form:"bid" example:"12345678" validate:"required,min=1"`
	// 过去天数
	PastDays int `json:"past_days" form:"past_days" example:"30" validate:"omitempty,min=1,max=365"`
}

// GetLatestFollowerNum 获取最新粉丝数请求
type GetLatestFollowerNum struct {
	// B站UID
	Bid int64 `json:"bid,string" form:"bid" example:"12345678" validate:"required,min=1"`
}

// PageAnchorFollowerNum 分页查询主播粉丝数记录请求
type PageAnchorFollowerNum struct {
	// B站UID
	Bid int64 `json:"bid,string" form:"bid" example:"12345678" validate:"required,min=1"`
	// 分页参数
	PageParam
}
