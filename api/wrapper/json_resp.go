package wrapper

import (
	"fanclub-apiserver/dto/resp"
	"time"
)

// JsonResp[T]
//
//	@Description	Some Generic Body
//
// JsonResp REST 接口返回的统一包装体
type JsonResp[T any] struct {
	// 业务错误代码
	Code string `json:"code" example:"OK"`
	// 消息
	Message string `json:"msg"`
	// 数据
	Data T `json:"data"`
	// 时间戳（毫秒）
	Ts int64 `json:"ts"`
}

// JsonPageResp[T] 分页结果响应
type JsonPageResp[T any] JsonResp[resp.PageResult[T]]

// NewJsonResp 创建一个新的 JsonResp 实例
func NewJsonResp[T any](code string, message string, data T) JsonResp[T] {
	return JsonResp[T]{
		Code:    code,
		Message: message,
		Data:    data,
		Ts:      time.Now().UnixMilli(),
	}
}

// Error 返回错误的 JsonResp
func Error[T any](code string, message string) JsonResp[T] {
	var data T
	return NewJsonResp(code, message, data)
}

// Success 返回成功的 JsonResp
func Success[T any](data T) JsonResp[T] {
	return JsonResp[T]{
		Code:    "OK",
		Message: "OK",
		Data:    data,
		Ts:      time.Now().UnixMilli(),
	}
}
