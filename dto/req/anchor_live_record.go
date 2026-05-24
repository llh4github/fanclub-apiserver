package req

// GetLatestLiveRecord 获取主播最新直播记录请求
type GetLatestLiveRecord struct {
	// 直播间ID
	RoomID int64 `json:"room_id" form:"room_id" query:"room_id" example:"12345" validate:"required,min=1"`
}

// GetWeekLiveRecords 获取主播本周直播记录请求
type GetWeekLiveRecords struct {
	// 直播间ID
	RoomID int64 `json:"room_id" form:"room_id" query:"room_id" example:"12345" validate:"required,min=1"`
}
