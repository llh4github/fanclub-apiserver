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

func registerAnchorLiveSchedule(router fiber.Router) {
	apis := new(anchorLiveSchedule)
	router.Group("anchor/liveSchedule").
		Get("/weekly", middleware.PublicReadRateLimiter, apis.GetWeeklySchedule)
}

type anchorLiveSchedule struct {
}

// GetWeeklySchedule 获取主播本周直播日程
//
//	@Summary		获取主播本周直播日程
//	@Description	根据B站UID查询当前周（周一00:00:00到周日23:59:59）的直播日程安排
//	@Tags			主播直播日程
//	@Accept			json
//	@Produce		json
//	@Param			req	query		req.GetWeeklySchedule	true	"查询参数"
//	@Success		200	{object}	wrapper.JsonResp[[]resp.AnchorLiveSchedule]
//	@Router			/anchor/liveSchedule/weekly [get]
func (c *anchorLiveSchedule) GetWeeklySchedule(ctx fiber.Ctx) error {
	var req req.GetWeeklySchedule
	if err := ctx.Bind().Query(&req); err != nil {
		return errs.WrapError(err, errMsgInvalidParams, string(errs.ReqParamValidFailed))
	}

	if err := validate.Struct(req); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}

	appCtx := g.FromFiberCtx(ctx)
	result, err := services.AnchorLiveSchedule.GetWeeklySchedule(appCtx, req.Bid)
	if err != nil {
		return err
	}

	return ctx.JSON(wrapper.Success(result))
}
