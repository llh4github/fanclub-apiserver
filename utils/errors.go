package utils

import "errors"

var (
	// ErrContentTooLong 内容超出长度限制
	ErrContentTooLong = errors.New("内容超出长度限制")
	// ErrContentTooShort 内容少于最小长度限制
	ErrContentTooShort = errors.New("内容少于最小长度限制")
	// ErrTooManyImages 图片数量超出限制
	ErrTooManyImages = errors.New("图片数量超出限制")
)
