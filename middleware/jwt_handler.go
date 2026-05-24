package middleware

import (
	"errors"
	"strings"
	"time"

	"fanclub-apiserver/errs"
	"fanclub-apiserver/services"

	"github.com/gofiber/fiber/v3"
	"github.com/golang-jwt/jwt/v5"
)

// isTokenExpiredError 检查错误是否为 token 过期错误
func isTokenExpiredError(err error) bool {
	if err == nil {
		return false
	}
	if errors.Is(err, jwt.ErrTokenExpired) {
		return true
	}
	errStr := err.Error()
	return strings.Contains(errStr, "token is expired") ||
		strings.Contains(errStr, "expired")
}

func JwtHandler() fiber.Handler {
	missToken := "缺少认证凭证"
	return func(c fiber.Ctx) error {
		authHeader := c.Get("Authorization")
		if authHeader == "" {
			return c.Status(fiber.StatusOK).JSON(jwtErrResponse{
				Code:    "AuthFailed",
				Message: missToken,
				Data:    "",
				Ts:      time.Now().UnixMilli(),
			})
		}

		if !strings.HasPrefix(authHeader, "Bearer ") {
			return c.Status(fiber.StatusOK).JSON(jwtErrResponse{
				Code:    "AuthFailed",
				Message: missToken,
				Data:    "",
				Ts:      time.Now().UnixMilli(),
			})
		}

		token := strings.TrimPrefix(authHeader, "Bearer ")
		if token == "" {
			return c.Status(fiber.StatusUnauthorized).JSON(jwtErrResponse{
				Code:    "AuthFailed",
				Message: missToken,
				Data:    "",
				Ts:      time.Now().UnixMilli(),
			})
		}

		claims, err := services.JWT.ValidateToken(c.Context(), token)
		if err != nil {
			errMsg := "无效的token"
			if isTokenExpiredError(err) {
				return c.Status(fiber.StatusUnauthorized).JSON(jwtErrResponse{
					Code:    string(errs.TokenExpiredError.BizCode),
					Message: errs.TokenExpiredError.Message,
					Data:    "",
					Ts:      time.Now().UnixMilli(),
				})
			}
			return c.Status(fiber.StatusUnauthorized).JSON(jwtErrResponse{
				Code:    "AuthFailed",
				Message: errMsg,
				Data:    "",
				Ts:      time.Now().UnixMilli(),
			})
		}

		c.Locals("claims", claims)

		return c.Next()
	}
}

type jwtErrResponse struct {
	Code    string `json:"code"`
	Message string `json:"msg"`
	Data    string `json:"data"`
	Ts      int64  `json:"ts"`
}
