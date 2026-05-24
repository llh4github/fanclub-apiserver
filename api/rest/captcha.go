package rest

import (
	"fanclub-apiserver/api/wrapper"
	"fanclub-apiserver/consts"
	"fanclub-apiserver/dto/req"
	"fanclub-apiserver/errs"
	"fanclub-apiserver/g"
	"fanclub-apiserver/middleware"
	"fanclub-apiserver/services"

	"github.com/gofiber/fiber/v3"
)

func registerCaptcha(router fiber.Router) {
	apis := new(captcha)
	router.Group("captcha").
		Use(middleware.CaptchaRateLimiter).
		Get("/click", apis.GenerateClickCaptcha).
		Post("/click/verify", apis.VerifyClickCaptcha).
		Get("/slide/generate", apis.GenerateSlideCaptcha).
		Post("/slide/verify", apis.VerifySlideCaptcha)
}

type captcha struct {
}

// GenerateClickCaptcha 生成点选验证码
//
//	@Summary		生成点选验证码
//	@Description	生成点选验证码，返回主图和缩略图用于前端展示
//	@Tags			验证码
//	@Accept			json
//	@Produce		json
//	@Param			scene	query		consts.CaptchaScene	true	"验证码使用场景 (login/submission)"
//	@Success		200		{object}	wrapper.JsonResp[resp.ClickCaptcha]
//	@Router			/captcha/click [get]
func (c *captcha) GenerateClickCaptcha(ctx fiber.Ctx) error {
	sceneStr := ctx.Query("scene")
	if sceneStr == "" {
		return errs.WrapError(nil, "scene 参数不能为空", string(errs.ReqParamValidFailed))
	}

	scene := consts.CaptchaScene(sceneStr)
	if scene != consts.CaptchaSceneLogin && scene != consts.CaptchaSceneSubmission {
		return errs.WrapError(nil, "无效的 scene 参数", string(errs.ReqParamValidFailed))
	}

	result, err := services.Captcha.GenerateClickCaptcha(ctx.Context(), scene)
	if err != nil {
		return err
	}
	return ctx.JSON(wrapper.Success(result))
}

// VerifyClickCaptcha 验证点选验证码
//
//	@Summary		验证点选验证码
//	@Description	验证用户点击的坐标是否正确，验证成功后返回Token
//	@Tags			验证码
//	@Accept			json
//	@Produce		json
//	@Param			body	body		req.VerifyClickCaptcha	true	"点选验证码验证请求"
//	@Success		200		{object}	wrapper.JsonResp[resp.CaptchaVerifyResult]
//	@Router			/captcha/click/verify [post]
func (c *captcha) VerifyClickCaptcha(ctx fiber.Ctx) error {
	var r req.VerifyClickCaptcha
	if err := ctx.Bind().Body(&r); err != nil {
		return errs.WrapError(err, errMsgInvalidBody, string(errs.ReqParamValidFailed))
	}
	if err := validate.Struct(r); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}
	appCtx := g.FromFiberCtx(ctx)
	result, err := services.Captcha.VerifyClickCaptcha(appCtx, &r)
	if err != nil {
		return err
	}
	return ctx.JSON(wrapper.Success(result))
}

// GenerateSlideCaptcha 生成滑动验证码
//
//	@Summary		生成滑动验证码
//	@Description	生成滑动验证码，返回背景图和滑块图用于前端展示
//	@Tags			验证码
//	@Accept			json
//	@Produce		json
//	@Param			scene	query		consts.CaptchaScene	true	"验证码使用场景 (login/submission)"
//	@Success		200		{object}	wrapper.JsonResp[resp.SlideCaptcha]
//	@Router			/captcha/slide/generate [get]
func (c *captcha) GenerateSlideCaptcha(ctx fiber.Ctx) error {
	sceneStr := ctx.Query("scene")
	if sceneStr == "" {
		return errs.WrapError(nil, "scene 参数不能为空", string(errs.ReqParamValidFailed))
	}

	scene := consts.CaptchaScene(sceneStr)
	if scene != consts.CaptchaSceneLogin && scene != consts.CaptchaSceneSubmission {
		return errs.WrapError(nil, "无效的 scene 参数", string(errs.ReqParamValidFailed))
	}

	result, err := services.Captcha.GenerateSlideCaptcha(ctx.Context(), scene)
	if err != nil {
		return err
	}
	return ctx.JSON(wrapper.Success(result))
}

// VerifySlideCaptcha 验证滑动验证码
//
//	@Summary		验证滑动验证码
//	@Description	验证用户滑动的距离是否正确，验证成功后返回Token
//	@Tags			验证码
//	@Accept			json
//	@Produce		json
//	@Param			body	body		req.VerifySlideCaptcha	true	"滑动验证码验证请求"
//	@Success		200		{object}	wrapper.JsonResp[resp.CaptchaVerifyResult]
//	@Router			/captcha/slide/verify [post]
func (c *captcha) VerifySlideCaptcha(ctx fiber.Ctx) error {
	var r req.VerifySlideCaptcha
	if err := ctx.Bind().Body(&r); err != nil {
		return errs.WrapError(err, errMsgInvalidBody, string(errs.ReqParamValidFailed))
	}
	if err := validate.Struct(r); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}
	appCtx := g.FromFiberCtx(ctx)
	result, err := services.Captcha.VerifySlideCaptcha(appCtx, &r)
	if err != nil {
		return err
	}
	return ctx.JSON(wrapper.Success(result))
}
