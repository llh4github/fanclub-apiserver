package errs

import (
	"fmt"
	"runtime"
	"strings"
)

type AppError struct {
	Err        error
	Message    string
	CallStack  []string
	StatusCode int
	BizCode    string
}

func (e *AppError) Error() string {
	if e.Err != nil {
		return fmt.Sprintf("%s: %v", e.Message, e.Err)
	}
	return e.Message
}

func (e *AppError) Unwrap() error {
	return e.Err
}

func captureCallStack(skip int) []string {
	var stack []string
	for i := skip; ; i++ {
		pc, file, line, ok := runtime.Caller(i)
		if !ok {
			break
		}
		funcName := runtime.FuncForPC(pc).Name()
		if !strings.Contains(funcName, "fanclub-apiserver/errs") {
			stack = append(stack, fmt.Sprintf("%s\n\t%s:%d", funcName, file, line))
		}
	}
	return stack
}
