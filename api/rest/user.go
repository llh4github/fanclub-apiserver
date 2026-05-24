package rest

import (
	"fanclub-apiserver/api/wrapper"
	"fanclub-apiserver/dto/req"
	"fanclub-apiserver/errs"
	"fanclub-apiserver/g"
	"fanclub-apiserver/middleware"
	"fanclub-apiserver/services"

	"github.com/gofiber/fiber/v3"
)

func registerUser(router fiber.Router) {
	apis := new(user)
	router.Group("user").
		Put("/password", middleware.AuthWriteRateLimiter, jwtHandler, apis.UpdatePassword)
}

type user struct {
}

// UpdatePassword 修改密码
//
//	@Summary		修改密码
//	@Description	用户修改自身密码，管理员可修改任意用户密码
//	@Tags			用户
//	@Security		BearerAuth
//	@Accept			json
//	@Produce		json
//	@Param			body	body		req.UpdatePassword	true	"修改密码请求"
//	@Success		200		{object}	wrapper.JsonResp[string]
//	@Router			/user/password [put]
func (c *user) UpdatePassword(ctx fiber.Ctx) error {
	var req req.UpdatePassword
	if err := ctx.Bind().Body(&req); err != nil {
		return errs.WrapError(err, errMsgInvalidBody, string(errs.ReqParamValidFailed))
	}

	if err := validate.Struct(req); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}

	appCtx := g.FromFiberCtx(ctx)

	decryptedNewPassword, err := services.Crypto.DecryptWithAES(appCtx, req.SessionID, req.NewPassword)
	if err != nil {
		return errs.WrapError(err, "新密码解密失败", string(errs.AESDecryptFailed))
	}

	if err := services.SysUser.UpdatePassword(appCtx, req.UserID, decryptedNewPassword); err != nil {
		return err
	}

	return ctx.JSON(wrapper.Success("密码修改成功"))
}
