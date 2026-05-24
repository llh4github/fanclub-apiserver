package rest

import (
	"fanclub-apiserver/api/wrapper"
	"fanclub-apiserver/consts"
	"fanclub-apiserver/dto/req"
	"fanclub-apiserver/dto/resp"
	"fanclub-apiserver/errs"
	"fanclub-apiserver/g"
	"fanclub-apiserver/middleware"
	"fanclub-apiserver/services"
	"fmt"

	"github.com/gofiber/fiber/v3"
	"go.uber.org/zap"
)

func rigsterAuth(router fiber.Router) {
	apis := new(auth)
	authGroup := router.Group("auth")
	authGroup.Post("/login", middleware.LoginRateLimiter, apis.Login)
	authGroup.Post("/refresh", apis.RefreshToken)
	authGroup.Post("/logout", jwtHandler, apis.Logout)
}

type auth struct {
}

// Login 登录
//
//	@Summary		登录
//	@Description	用户登录，返回访问令牌和刷新令牌
//	@Tags			认证
//	@Accept			json
//	@Produce		json
//	@Param			body	body		req.Login	true	"登录请求"
//	@Success		200		{object}	wrapper.JsonResp[resp.Login]
//	@Router			/auth/login [post]
func (c *auth) Login(ctx fiber.Ctx) error {
	var r req.Login
	if err := ctx.Bind().Body(&r); err != nil {
		return errs.WrapError(err, errMsgInvalidBody, string(errs.ReqParamValidFailed))
	}

	// 验证请求参数
	if err := validate.Struct(r); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}

	appCtx := g.FromFiberCtx(ctx)

	// 校验验证码 Token
	if err := services.Captcha.VerifyCaptchaToken(appCtx, r.CaptchaToken, consts.CaptchaSceneLogin); err != nil {
		return errs.WrapError(err, "验证码不正确或已过期", string(errs.CaptchaVerifyFailed))
	}

	// 使用AES密钥解密密码，还原密码原文
	g.Debug("准备解密密码",
		zap.String("session_id", r.SessionID),
		zap.String("encrypted_password_length", fmt.Sprintf("%d", len(r.Password))),
		zap.String("encrypted_password_preview", func() string {
			if len(r.Password) > 32 {
				return r.Password[:32] + "..."
			}
			return r.Password
		}()))

	decryptedPassword, err := services.Crypto.DecryptWithAES(appCtx, r.SessionID, r.Password)
	if err != nil {
		g.Error("密码解密失败",
			zap.String("session_id", r.SessionID),
			zap.String("encrypted_password_length", fmt.Sprintf("%d", len(r.Password))),
			zap.Error(err))
		return errs.WrapError(err, "Failed to decrypt password", string(errs.AESDecryptFailed))
	}
	r.Password = decryptedPassword

	result, err := services.Auth.Login(appCtx, &r)
	if err != nil {
		return err
	}

	c.setRefreshTokenCookie(ctx, result.RefreshToken, int(g.Cfg.JWT.RefreshTokenExpiration*60))

	respLogin := resp.Login{
		ID:             result.ID,
		Username:       result.Username,
		AccessToken:    result.AccessToken,
		RefreshToken:   "",
		ExpirationTime: result.ExpirationTime,
	}

	return ctx.JSON(wrapper.Success(respLogin))
}

func (c *auth) setRefreshTokenCookie(ctx fiber.Ctx, refreshToken string, maxAge int) {
	cookie := &fiber.Cookie{
		Name:     "refresh_token",
		Value:    refreshToken,
		Path:     "/",
		Domain:   g.Cfg.Server.Domain,
		Secure:   g.Cfg.IsProd(),
		HTTPOnly: true,
		SameSite: "Strict",
		MaxAge:   maxAge,
	}
	ctx.Cookie(cookie)
}

// Logout 登出
//
//	@Summary		登出
//	@Description	用户登出，清除Redis中的token
//	@Tags			认证
//	@Security		BearerAuth
//	@Accept			json
//	@Produce		json
//	@Success		200	{object}	wrapper.JsonResp[string]
//	@Router			/auth/logout [post]
func (c *auth) Logout(ctx fiber.Ctx) error {
	appCtx := g.FromFiberCtx(ctx)
	if !appCtx.IsLogin {
		return errs.WrapError(nil, "未登录", string(errs.AuthFailed))
	}

	if err := services.JWT.InvalidateTokens(appCtx.C, appCtx.UserID); err != nil {
		return errs.WrapError(err, "登出失败", string(errs.UnkonwError))
	}

	c.clearRefreshTokenCookie(ctx)

	return ctx.JSON(wrapper.Success("登出成功"))
}

func (c *auth) clearRefreshTokenCookie(ctx fiber.Ctx) {
	cookie := &fiber.Cookie{
		Name:     "refresh_token",
		Value:    "",
		Path:     "/",
		Domain:   g.Cfg.Server.Domain,
		Secure:   g.Cfg.IsProd(),
		HTTPOnly: true,
		MaxAge:   -1,
	}
	ctx.Cookie(cookie)
}

// RefreshToken 刷新令牌
//
//	@Summary		刷新令牌
//	@Description	使用 Refresh Token 获取新的访问令牌，新 Token 通过 HttpOnly Cookie 下发
//	@Tags			认证
//	@Accept			json
//	@Produce		json
//	@Param			Cookie	header		string	true	"Refresh Token from Cookie"
//	@Success		200		{object}	wrapper.JsonResp[resp.Login]
//	@Router			/auth/refresh [post]
func (c *auth) RefreshToken(ctx fiber.Ctx) error {
	refreshTokenFromCookie := ctx.Cookies("refresh_token")
	if refreshTokenFromCookie == "" {
		g.Debug("刷新令牌：Cookie 中缺少 refresh_token")
		return errs.WrapError(nil, "缺少 refresh_token", string(errs.AuthFailed))
	}

	g.Debug("刷新令牌：开始验证",
		zap.String("token_length", fmt.Sprintf("%d", len(refreshTokenFromCookie))))

	appCtx := g.FromFiberCtx(ctx)

	result, err := services.Auth.RefreshToken(appCtx, refreshTokenFromCookie)
	if err != nil {
		g.Debug("刷新令牌：验证失败", zap.Error(err))
		return err
	}

	g.Debug("刷新令牌：验证成功",
		zap.Int64("user_id", result.ID))

	c.setRefreshTokenCookie(ctx, result.RefreshToken, int(g.Cfg.JWT.RefreshTokenExpiration*60))

	respLogin := resp.Login{
		ID:             result.ID,
		Username:       result.Username,
		AccessToken:    result.AccessToken,
		RefreshToken:   "",
		ExpirationTime: result.ExpirationTime,
	}

	return ctx.JSON(wrapper.Success(respLogin))
}
