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

func registerAnchorLiveRecord(router fiber.Router) {
	apis := new(anchorLiveRecord)
	router.Group("anchor/liveRecord").
		Get("/latest", middleware.PublicReadRateLimiter, apis.GetLatestLiveRecord).
		Get("/week", middleware.PublicReadRateLimiter, apis.GetWeekLiveRecords)
}

type anchorLiveRecord struct {
}

// GetLatestLiveRecord 获取主播最新直播记录
//
//	@Summary		获取主播最新直播记录
//	@Description	根据直播间ID查询该主播最新的一场直播记录
//	@Tags			主播直播记录
//	@Accept			json
//	@Produce		json
//	@Param			req	query		req.GetLatestLiveRecord	true	"查询参数"
//	@Success		200	{object}	wrapper.JsonResp[resp.LatestLiveRecord]
//	@Router			/anchor/liveRecord/latest [get]
func (c *anchorLiveRecord) GetLatestLiveRecord(ctx fiber.Ctx) error {
	var req req.GetLatestLiveRecord
	if err := ctx.Bind().Query(&req); err != nil {
		return errs.WrapError(err, errMsgInvalidParams, string(errs.ReqParamValidFailed))
	}

	if err := validate.Struct(req); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}

	appCtx := g.FromFiberCtx(ctx)
	result, err := services.AnchorLiveRecord.GetLatestByRoomID(appCtx, req.RoomID)
	if err != nil {
		return err
	}

	return ctx.JSON(wrapper.Success(result))
}

// GetWeekLiveRecords 获取主播本周直播记录
//
//	@Summary		获取主播本周直播记录
//	@Description	根据直播间ID查询当前周（周一到周日）的所有直播记录
//	@Tags			主播直播记录
//	@Accept			json
//	@Produce		json
//	@Param			req	query		req.GetWeekLiveRecords	true	"查询参数"
//	@Success		200	{object}	wrapper.JsonResp[[]resp.WeekLiveRecord]
//	@Router			/anchor/liveRecord/week [get]
func (c *anchorLiveRecord) GetWeekLiveRecords(ctx fiber.Ctx) error {
	var req req.GetWeekLiveRecords
	if err := ctx.Bind().Query(&req); err != nil {
		return errs.WrapError(err, errMsgInvalidParams, string(errs.ReqParamValidFailed))
	}

	if err := validate.Struct(req); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}

	appCtx := g.FromFiberCtx(ctx)
	result, err := services.AnchorLiveRecord.GetCurrentWeekByRoomID(appCtx, req.RoomID)
	if err != nil {
		return err
	}

	return ctx.JSON(wrapper.Success(result))
}
