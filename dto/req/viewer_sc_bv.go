package req

// CheckViewerScBv 查询SC点播BV记录是否存在请求
type CheckViewerScBv struct {
	// BV号
	BV string `json:"bv" form:"bv" query:"bv" example:"BV1xK4y1b7NP" validate:"required"`
	// 房间ID
	RoomID int64 `json:"room_id" form:"room_id" query:"room_id" example:"12345" validate:"required,min=1"`
}
