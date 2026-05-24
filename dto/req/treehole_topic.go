package req

import "time"

// CreateTreeholeTopic 创建树洞主题请求
type CreateTreeholeTopic struct {
	// 主播B站UID
	Bid int64 `json:"bid,string" example:"12345678" validate:"required,min=1"`
	// 树洞主题标题
	Title string `json:"title" example:"520特别投稿" validate:"required,min=1,max=125"`
	// 树洞主题描述
	Description string `json:"description" example:"520特别投稿活动" validate:"omitempty,max=300"`
	// 投稿开放时间窗口
	OpenAt time.Time `json:"open_at" example:"2026-05-20T00:00:00Z" validate:"required"`
	// 投稿关闭时间窗口
	CloseAt time.Time `json:"close_at" example:"2026-05-21T00:00:00Z" validate:"required"`
	// 是否启用
	IsActive bool `json:"is_active" example:"true" validate:"omitempty"`
}

// UpdateTreeholeTopic 更新树洞主题请求
type UpdateTreeholeTopic struct {
	ID int64 `json:"id,string" validate:"required,min=1"`
	// 树洞主题标题
	Title string `json:"title" example:"520特别投稿" validate:"omitempty,min=1,max=125"`
	// 树洞主题描述
	Description string `json:"description" example:"520特别投稿活动" validate:"omitempty,max=300"`
	// 投稿开放时间窗口
	OpenAt time.Time `json:"open_at" example:"2026-05-20T00:00:00Z" validate:"omitempty"`
	// 投稿关闭时间窗口
	CloseAt time.Time `json:"close_at" example:"2026-05-21T00:00:00Z" validate:"omitempty"`
	// 是否启用
	IsActive *bool `json:"is_active" example:"true" validate:"omitempty"`
}

// GetTreeholeTopic 获取树洞主题请求
type GetTreeholeTopic struct {
	// 主题ID
	ID int64 `json:"id,string" form:"id" query:"id" example:"123" validate:"omitempty,min=1"`
	// 主播B站UID
	Bid int64 `json:"bid,string" form:"bid" query:"bid" example:"12345678" validate:"omitempty,min=1"`
}

// PageTreeholeTopic 分页查询树洞主题请求
type PageTreeholeTopic struct {
	// 主播B站UID
	Bid int64 `json:"bid,string" form:"bid" query:"bid" example:"12345678" validate:"omitempty,min=1"`
	// 树洞主题标题（模糊查询）
	Title string `json:"title" form:"title" query:"title" example:"520" validate:"omitempty"`
	// 是否启用
	IsActive *bool `json:"is_active" form:"is_active" query:"is_active" example:"true" validate:"omitempty"`
	// 分页参数
	PageParam
}

// PageTreeholeTopicAdmin 后台分页查询树洞主题请求
type PageTreeholeTopicAdmin struct {
	// 主播B站UID
	Bid int64 `json:"bid,string" form:"bid" query:"bid" example:"12345678" validate:"omitempty,min=1"`
	// 树洞主题标题（模糊查询）
	Title string `json:"title" form:"title" query:"title" example:"520" validate:"omitempty"`
	// 是否启用
	IsActive *bool `json:"is_active" form:"is_active" query:"is_active" example:"true" validate:"omitempty"`
	// 分页参数
	PageParam
}

// SetTopicStatusReq 设置主题状态请求
type SetTopicStatusReq struct {
	// 主题ID
	ID int64 `json:"id,string" example:"123" validate:"required,min=1"`
	// 是否启用
	IsActive *bool `json:"is_active" example:"true" validate:"required"`
}
