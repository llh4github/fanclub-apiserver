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

func registerCrypto(router fiber.Router) {
	apis := new(crypto)
	router.Group("crypto").
		Post("/init", middleware.CryptoRateLimiter, apis.InitKeyExchange).
		Post("/complete", middleware.CryptoRateLimiter, apis.CompleteKeyExchange)
}

type crypto struct {
}

// InitKeyExchange 初始化密钥交换
//
//	@Summary		初始化密钥交换
//	@Description	生成RSA密钥对，返回公钥用于客户端加密AES密钥，公钥24小时内有效
//	@Tags			加密解密
//	@Accept			json
//	@Produce		json
//	@Success		200	{object}	wrapper.JsonResp[resp.InitKeyExchangeResp]
//	@Router			/crypto/init [post]
func (c *crypto) InitKeyExchange(ctx fiber.Ctx) error {
	appCtx := g.FromFiberCtx(ctx)
	result, err := services.Crypto.InitKeyExchange(appCtx)
	if err != nil {
		return err
	}

	return ctx.JSON(wrapper.Success(result))
}

// CompleteKeyExchange 完成密钥交换
//
//	@Summary		完成密钥交换
//	@Description	上传用RSA公钥加密的AES密钥，服务端解密后保存AES密钥供后续加解密使用。成功时返回key_expire_at，前端据此重置过期时间
//	@Tags			加密解密
//	@Accept			json
//	@Produce		json
//	@Param			body	body		req.CompleteKeyExchange	true	"完成密钥交换请求"
//	@Success		200		{object}	wrapper.JsonResp[resp.CompleteKeyExchangeResp]
//	@Router			/crypto/complete [post]
func (c *crypto) CompleteKeyExchange(ctx fiber.Ctx) error {
	var req req.CompleteKeyExchange
	if err := ctx.Bind().Body(&req); err != nil {
		return errs.WrapError(err, errMsgInvalidBody, string(errs.ReqParamValidFailed))
	}

	if err := validate.Struct(req); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}

	appCtx := g.FromFiberCtx(ctx)
	result, err := services.Crypto.CompleteKeyExchange(appCtx, &req)
	if err != nil {
		return err
	}

	return ctx.JSON(wrapper.Success(result))
}
