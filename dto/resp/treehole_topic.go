package resp

import "time"

// TopicBrief 话题简要信息
type TopicBrief struct {
	// 主键ID
	ID int64 `json:"id,string"`
	// 标题
	Title string `json:"title"`
	// 描述
	Description string `json:"description"`
	// 投稿开放时间
	OpenAt time.Time `json:"open_at"`
	// 投稿关闭时间
	CloseAt time.Time `json:"close_at"`
}

// TopicDetail 话题详细信息（包含统计数据）
type TopicDetail struct {
	// 主键ID
	ID int64 `json:"id,string"`
	// B站UID
	Bid int64 `json:"bid"`
	// 标题
	Title string `json:"title"`
	// 描述
	Description string `json:"description"`
	// 投稿开放时间
	OpenAt time.Time `json:"open_at"`
	// 投稿关闭时间
	CloseAt time.Time `json:"close_at"`
	// 是否启用
	IsActive bool `json:"is_active"`
	// 创建时间
	CreatedTime time.Time `json:"created_time"`
	// 更新时间
	UpdatedTime time.Time `json:"updated_time"`
	// 话题下审核通过的投稿总数
	TotalSubmissionCount int64 `json:"total_submission_count"`
	// 话题下不宜展示的投稿数量
	HiddenSubmissionCount int64 `json:"hidden_submission_count"`
}

// TopicPageItem 分页列表项（包含统计数据）
type TopicPageItem struct {
	// 主键ID
	ID int64 `json:"id,string"`
	// B站UID
	Bid int64 `json:"bid"`
	// 标题
	Title string `json:"title"`
	// 描述
	Description string `json:"description"`
	// 投稿开放时间
	OpenAt time.Time `json:"open_at"`
	// 投稿关闭时间
	CloseAt time.Time `json:"close_at"`
	// 是否启用
	IsActive bool `json:"is_active"`
	// 创建时间
	CreatedTime time.Time `json:"created_time"`
	// 更新时间
	UpdatedTime time.Time `json:"updated_time"`
	// 话题下审核通过的投稿总数
	TotalSubmissionCount int64 `json:"total_submission_count"`
	// 话题下不宜展示的投稿数量
	HiddenSubmissionCount int64 `json:"hidden_submission_count"`
}

// TopicStats 话题统计数据
type TopicStats struct {
	// 话题ID
	TopicID int64 `json:"topic_id"`
	// 投稿总数
	TotalCount int64 `json:"total_count"`
	// 不宜展示数量
	HiddenCount int64 `json:"hidden_count"`
}
