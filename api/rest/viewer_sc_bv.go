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

func rigsterViewerScBv(router fiber.Router) {
	apis := new(viewerScBv)
	router.Group("viewer/scBv").
		Get("/check", middleware.PublicReadRateLimiter, apis.CheckByBV)
}

// viewerScBv 观众SC点播BV控制器
type viewerScBv struct {
}

// CheckByBV 根据BV号查询是否存在相关SC点播记录
//
//	@Summary		查询SC点播BV记录是否存在
//	@Description	根据BV号查询是否存在相关的SC点播记录
//	@Tags			观众SC点播
//	@Accept			json
//	@Produce		json
//	@Param			req	query		req.CheckViewerScBv	true	"查询参数"
//	@Success		200	{object}	wrapper.JsonResp[resp.ViewerScBvCheckResult]
//	@Router			/viewer/scBv/check [get]
func (c *viewerScBv) CheckByBV(ctx fiber.Ctx) error {
	var r req.CheckViewerScBv
	if err := ctx.Bind().Query(&r); err != nil {
		return errs.WrapError(err, errMsgInvalidParams, string(errs.ReqParamValidFailed))
	}

	// 验证请求参数
	if err := validate.Struct(r); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}

	appCtx := g.FromFiberCtx(ctx)
	result, err := services.ViewerScBv.CheckByBV(appCtx, r.RoomID, r.BV)
	if err != nil {
		return err
	}

	return ctx.JSON(wrapper.Success(result))
}
