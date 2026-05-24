package model

import (
	"time"
)

// OssUploadCallback 七牛云上传回调记录
// Deprecated: 暂不使用
type OssUploadCallback struct {
	BaseModel
	// 对象 key（上传文件路径）
	FileKey string `json:"file_key" gorm:"type:varchar(750);not null;uniqueIndex"`
	// 文件 ETag (hash)
	FileHash string `json:"file_hash" gorm:"type:varchar(64)"`
	// 文件大小（字节）
	FileSize int64 `json:"file_size" gorm:"type:bigint"`
	// 文件 MIME 类型
	MimeType string `json:"mime_type" gorm:"type:varchar(128)"`
	// 原始文件名
	FileName string `json:"file_name" gorm:"type:varchar(512)"`
	// 上传时间（七牛回调时间）
	UploadedAt time.Time `json:"uploaded_at" gorm:"type:timestamptz"`
	// 回调状态：0-处理中 1-成功 2-失败
	Status int `json:"status" gorm:"type:smallint;default:0"`
	// 回调来源 URL
	CallbackURL string `json:"callback_url" gorm:"type:varchar(512)"`
	// 原始回调数据（JSON）
	RawData string `json:"raw_data" gorm:"type:text"`
	// 错误信息（如有）
	ErrorMsg string `json:"error_msg" gorm:"type:text"`
}

// TableName 指定表名
func (OssUploadCallback) TableName() string {
	return "oss_upload_callback"
}

// CallbackStatus 回调状态枚举
type CallbackStatus int

const (
	CallbackStatusProcessing CallbackStatus = 0 // 处理中
	CallbackStatusSuccess    CallbackStatus = 1 // 成功
	CallbackStatusFailed     CallbackStatus = 2 // 失败
)
