package req

import "strconv"

// PageParam 分页参数
type PageParam struct {
	// 每页数量
	PageSize int `json:"page_size" form:"page_size" query:"page_size" example:"10" validate:"omitempty,min=1,max=100"`
	// 页码
	PageIndex int `json:"page_index" form:"page_index" query:"page_index" example:"0" validate:"omitempty,min=0"`
}

// DeleteBatchIDs 批量删除请求
type DeleteBatchIDs struct {
	// 数据ID列表（字符串形式，避免前端精度丢失）
	IDs []string `json:"ids" example:"[\"1234567890123456789\",\"9876543210987654321\"]" validate:"required,min=1,dive,numeric,min=1"`
}

// ToInt64Slice 将字符串 ID 数组转换为 int64 数组
func (r *DeleteBatchIDs) ToInt64Slice() ([]int64, error) {
	result := make([]int64, 0, len(r.IDs))
	for _, idStr := range r.IDs {
		id, err := strconv.ParseInt(idStr, 10, 64)
		if err != nil {
			return nil, err
		}
		result = append(result, id)
	}
	return result, nil
}
