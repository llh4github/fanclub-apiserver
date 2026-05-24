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

func rigsterAnchorFollowerNum(router fiber.Router) {
	apis := new(anchorFollowerNum)
	router.Group("anchor/followerNum").
		Get("/past", middleware.PublicReadRateLimiter, apis.GetPastFollowerNum).
		Get("/latest", middleware.PublicReadRateLimiter, apis.GetLatestFollowerNum)
}

// anchorFollowerNum 主播粉丝数控制器
type anchorFollowerNum struct {
}

// GetPastFollowerNum 获取过去x天的粉丝数
//
//	@Summary		获取过去x天的粉丝数
//	@Description	根据B站UID查询过去指定天数的粉丝数记录，默认30天
//	@Tags			主播粉丝数
//	@Accept			json
//	@Produce		json
//	@Param			req	query		req.GetPastFollowerNum	true	"查询参数"
//	@Success		200	{object}	wrapper.JsonResp[[]resp.DailyFollowerNum]
//	@Router			/anchor/followerNum/past [get]
func (c *anchorFollowerNum) GetPastFollowerNum(ctx fiber.Ctx) error {
	var req req.GetPastFollowerNum
	if err := ctx.Bind().Query(&req); err != nil {
		return errs.WrapError(err, errMsgInvalidParams, string(errs.ReqParamValidFailed))
	}

	if err := validate.Struct(req); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}

	// 默认查询过去30天
	pastDays := req.PastDays
	if pastDays <= 0 {
		pastDays = 30
	}

	appCtx := g.FromFiberCtx(ctx)
	result, err := services.AnchorFollowerNum.GetPastFollowerNum(appCtx, req.Bid, pastDays)
	if err != nil {
		return err
	}

	return ctx.JSON(wrapper.Success(result))
}

// GetLatestFollowerNum 获取最新粉丝数
//
//	@Summary		获取最新粉丝数
//	@Description	根据B站UID查询最新的粉丝数记录
//	@Tags			主播粉丝数
//	@Accept			json
//	@Produce		json
//	@Param			req	query		req.GetLatestFollowerNum	true	"查询参数"
//	@Success		200	{object}	wrapper.JsonResp[resp.DailyFollowerNum]
//	@Router			/anchor/followerNum/latest [get]
func (c *anchorFollowerNum) GetLatestFollowerNum(ctx fiber.Ctx) error {
	var req req.GetLatestFollowerNum
	if err := ctx.Bind().Query(&req); err != nil {
		return errs.WrapError(err, errMsgInvalidParams, string(errs.ReqParamValidFailed))
	}

	if err := validate.Struct(req); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}

	appCtx := g.FromFiberCtx(ctx)
	result, err := services.AnchorFollowerNum.GetLatestFollowerNum(appCtx, req.Bid)
	if err != nil {
		return err
	}

	return ctx.JSON(wrapper.Success(result))
}
