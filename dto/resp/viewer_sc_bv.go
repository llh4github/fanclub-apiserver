package resp

// ViewerScBvCheckResult SC点播BV查询结果
type ViewerScBvCheckResult struct {
	// BV号是否存在相关记录
	Exists bool `json:"exists"`
	// 相关记录数量
	Count int64 `json:"count"`
}
