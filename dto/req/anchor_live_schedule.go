package req

// GetWeeklySchedule 获取主播本周直播日程请求
type GetWeeklySchedule struct {
	// B站UID
	Bid int64 `json:"bid" form:"bid" query:"bid" example:"12345678" validate:"required,min=1"`
}
