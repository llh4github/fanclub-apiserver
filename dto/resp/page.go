package resp

// PageResult[T] 分页结果
type PageResult[T any] struct {
	// 总记录数
	TotalRowCount int `json:"total_row_count"`
	// 总页数
	TotalPage int `json:"total_page"`
	// 数据列表
	Records []T `json:"records"`
}
