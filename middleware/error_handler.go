package middleware

import (
	"fanclub-apiserver/api/wrapper"
	"fanclub-apiserver/errs"
	"fanclub-apiserver/g"
	"fmt"
	"strings"

	"github.com/gofiber/fiber/v3"
	"go.uber.org/zap"
)

func ErrorHandler() fiber.ErrorHandler {
	return func(c fiber.Ctx, err error) error {
		var appErr *errs.AppError
		if ae, ok := err.(*errs.AppError); ok {
			appErr = ae
		} else {
			uri := c.Request().URI().RequestURI()
			errMsg := fmt.Sprintf("%s Not Found", uri)
			bizCode := string(errs.UnkonwError)
			if strings.Contains(errMsg, "Not Found") {
				bizCode = string(errs.NotFound)
			}

			appErr = errs.WrapError(err, errMsg, bizCode)
		}

		logError(appErr)

		return c.JSON(wrapper.Error[string](appErr.BizCode, appErr.Message))
	}
}

func logError(err *errs.AppError) {
	var stackStr strings.Builder
	stackStr.WriteString("stacktrace:\n")
	for _, stack := range err.CallStack {
		fmt.Fprintf(&stackStr, "    %s\n", stack)
	}

	g.Error(err.Error(),
		zap.String("callStack", stackStr.String()),
		zap.Int("statusCode", err.StatusCode),
	)
}
