package errs

const statusCode = 200

// WrapError 包装错误并添加消息和业务错误码，自动捕获调用栈
// 如果 err 参数已经是 AppError 实例，则直接更新其字段并返回原对象
// 参数:
//
//	err - 原始错误
//	opts - 可选参数，opts[0] 为错误消息，opts[1] 为业务错误码
func WrapError(err error, opts ...string) *AppError {
	if appErr, ok := err.(*AppError); ok {
		if len(opts) > 0 && opts[0] != "" {
			appErr.Message = opts[0]
		}
		if len(opts) > 1 && opts[1] != "" {
			appErr.BizCode = opts[1]
		}
		appErr.CallStack = captureCallStack(2)
		return appErr
	}

	var msg, bz string
	if len(opts) > 0 && opts[0] != "" {
		msg = opts[0]
	} else {
		msg = err.Error()
	}

	if len(opts) > 1 && opts[1] != "" {
		bz = opts[1]
	} else {
		bz = string(UnkonwError)
	}
	return &AppError{
		Err:        err,
		Message:    msg,
		CallStack:  captureCallStack(2),
		StatusCode: statusCode,
		BizCode:    bz,
	}
}

// WrapErrorWithCode 包装错误并添加消息和 HTTP 状态码，自动捕获调用栈
// 如果 err 参数已经是 AppError 实例，则直接更新其字段并返回原对象
// 参数:
//
//	err - 原始错误
//	message - 错误消息
//	statusCode - HTTP 状态码
func WrapErrorWithCode(err error, message string, statusCode int) *AppError {
	if appErr, ok := err.(*AppError); ok {
		if message != "" {
			appErr.Message = message
		}
		appErr.StatusCode = statusCode
		appErr.CallStack = captureCallStack(2)
		return appErr
	}
	return &AppError{
		Err:        err,
		Message:    message,
		CallStack:  captureCallStack(2),
		StatusCode: statusCode,
		BizCode:    "server_error",
	}
}

// New 创建一个新的 AppError，不包装其他错误
func New(message string) *AppError {
	return &AppError{
		Err:        nil,
		Message:    message,
		CallStack:  captureCallStack(2),
		StatusCode: statusCode,
		BizCode:    "server_error",
	}
}

// NewAppError 创建一个新的 AppError，不包装其他错误，指定业务错误码
func NewAppError(message string, code ErorrCode) *AppError {
	return &AppError{
		Err:        nil,
		Message:    message,
		CallStack:  captureCallStack(2),
		StatusCode: statusCode,
		BizCode:    string(code),
	}
}
