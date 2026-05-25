package rest

import (
	"fanclub-apiserver/api/wrapper"
	"fanclub-apiserver/database/model"
	"fanclub-apiserver/dto/req"
	"fanclub-apiserver/errs"
	"fanclub-apiserver/g"
	"fanclub-apiserver/middleware"
	"fanclub-apiserver/services"

	"github.com/gofiber/fiber/v3"
)

func rigsterAnchorSong(router fiber.Router) {
	apis := new(anchorSong)
	router.Group("anchor/song").
		Get("/get", middleware.PublicReadRateLimiter, apis.GetByBidAndName).
		Get("/page", middleware.PublicReadRateLimiter, apis.Page).
		Post("/add", middleware.AuthWriteRateLimiter, jwtHandler, apis.Create).
		Put("/update", middleware.AuthWriteRateLimiter, jwtHandler, apis.Update).
		Get("/admin/page", middleware.AuthWriteRateLimiter, jwtHandler, apis.PageAdmin).
		Delete("/delete", middleware.AuthWriteRateLimiter, jwtHandler, apis.Delete)
}

// anchorSong 主播歌曲控制器
type anchorSong struct {
}

// Create 创建主播歌曲
//
//	@Summary		创建主播歌曲
//	@Description	创建新的主播歌曲记录
//	@Tags			主播歌曲
//	@Security		BearerAuth
//	@Accept			json
//	@Produce		json
//	@Param			song	body		req.CreateAnchorSong	true	"主播歌曲"
//	@Success		200		{object}	wrapper.JsonResp[string]
//	@Router			/anchor/song/add [post]
func (c *anchorSong) Create(ctx fiber.Ctx) error {
	var r req.CreateAnchorSong
	if err := ctx.Bind().Body(&r); err != nil {
		return errs.WrapError(err, errMsgInvalidParams, string(errs.ReqParamValidFailed))
	}

	// 验证请求参数
	if err := validate.Struct(r); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}

	// 创建实体
	song := &model.AnchorSong{
		Price: r.Price,
		Bid:   r.Bid,
		Name:  r.Name,
		Bv:    r.Bv,
	}

	appCtx := g.FromFiberCtx(ctx)
	if err := services.AnchorSong.Create(appCtx, song); err != nil {
		return err
	}

	return ctx.JSON(wrapper.Success(""))
}

// GetByBidAndName 根据B站ID和歌曲名称获取主播歌曲
//
//	@Summary		根据B站ID和歌曲名称获取主播歌曲
//	@Description	根据B站ID和歌曲名称获取主播歌曲记录
//	@Tags			主播歌曲
//	@Accept			json
//	@Produce		json
//	@Param			req	query		req.GetAnchorSong	true	"查询参数"
//	@Success		200	{object}	wrapper.JsonResp[model.AnchorSong]
//	@Router			/anchor/song/get [get]
func (c *anchorSong) GetByBidAndName(ctx fiber.Ctx) error {
	var r req.GetAnchorSong
	if err := ctx.Bind().Query(&r); err != nil {
		return errs.WrapError(err, errMsgInvalidParams, string(errs.ReqParamValidFailed))
	}

	if err := validate.Struct(r); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}

	appCtx := g.FromFiberCtx(ctx)
	song, err := services.AnchorSong.GetByBidAndName(appCtx, r.Bid, r.Name)
	if err != nil {
		return err
	}

	return ctx.JSON(wrapper.Success(song))
}

// Update 更新主播歌曲
//
//	@Summary		更新主播歌曲
//	@Description	根据ID更新主播歌曲记录
//	@Tags			主播歌曲
//	@Security		BearerAuth
//	@Accept			json
//	@Produce		json
//	@Param			req	body		req.UpdateAnchorSong	true	"更新参数"
//	@Success		200	{object}	wrapper.JsonResp[string]
//	@Router			/anchor/song/update [put]
func (c *anchorSong) Update(ctx fiber.Ctx) error {
	var r req.UpdateAnchorSong
	if err := ctx.Bind().Body(&r); err != nil {
		return errs.WrapError(err, errMsgInvalidBody, string(errs.ReqParamValidFailed))
	}

	if err := validate.Struct(r); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}

	appCtx := g.FromFiberCtx(ctx)

	if appCtx.Role.IsAnchor() {
		bid, _, err := appCtx.GetAnchorInfo()
		if err != nil {
			return err
		}
		r.Bid = bid
	}

	if err := services.AnchorSong.UpdateByID(appCtx, &r); err != nil {
		return err
	}

	return ctx.JSON(wrapper.Success(""))
}

// Delete 批量删除主播歌曲
//
//	@Summary		批量删除主播歌曲
//	@Description	根据ID列表批量删除主播歌曲记录
//	@Tags			主播歌曲
//	@Security		BearerAuth
//	@Accept			json
//	@Produce		json
//	@Param			ids	body		req.DeleteBatchIDs	true	"ID列表"
//	@Success		200	{object}	wrapper.JsonResp[string]
//	@Router			/anchor/song/delete [delete]
func (c *anchorSong) Delete(ctx fiber.Ctx) error {
	var r req.DeleteBatchIDs
	if err := ctx.Bind().Body(&r); err != nil {
		return errs.WrapError(err, errMsgInvalidBody, string(errs.ReqParamValidFailed))
	}

	// 验证请求参数
	if err := validate.Struct(r); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}

	// 批量删除记录
	appCtx := g.FromFiberCtx(ctx)

	var bid int64
	if appCtx.Role.IsAnchor() {
		var err error
		bid, _, err = appCtx.GetAnchorInfo()
		if err != nil {
			return err
		}
	}

	ids, err := r.ToInt64Slice()
	if err != nil {
		return errs.WrapError(err, errMsgInvalidParams, string(errs.ReqParamValidFailed))
	}

	if err := services.AnchorSong.DeleteBatch(appCtx, ids, bid); err != nil {
		return err
	}

	return ctx.JSON(wrapper.Success("Anchor song deleted successfully"))
}

// Page 分页查询主播歌曲
//
//	@Summary		分页查询主播歌曲
//	@Description	根据B站ID和歌名模糊查询主播歌曲记录
//	@Tags			主播歌曲
//	@Accept			json
//	@Produce		json
//	@Param			req	query		req.PageAnchorSong	true	"查询参数"
//	@Success		200	{object}	wrapper.JsonPageResp[resp.AnchorSongSimple]
//	@Router			/anchor/song/page [get]
func (c *anchorSong) Page(ctx fiber.Ctx) error {
	var r req.PageAnchorSong
	if err := ctx.Bind().Query(&r); err != nil {
		return errs.WrapError(err, errMsgInvalidParams, string(errs.ReqParamValidFailed))
	}

	if err := validate.Struct(r); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}

	appCtx := g.FromFiberCtx(ctx)
	result, err := services.AnchorSong.Page(appCtx, r.Bid, r.Name, r.PageParam)
	if err != nil {
		return err
	}

	return ctx.JSON(wrapper.Success(result))
}

// PageAdmin 后台分页查询主播歌曲
//
//	@Summary		后台分页查询主播歌曲
//	@Description	后台分页查询主播歌曲，返回全部字段
//	@Tags			主播歌曲
//	@Security		BearerAuth
//	@Accept			json
//	@Produce		json
//	@Param			req	query		req.PageAnchorSongAdmin	true	"查询参数"
//	@Success		200	{object}	wrapper.JsonPageResp[model.AnchorSong]
//	@Router			/anchor/song/admin/page [get]
func (c *anchorSong) PageAdmin(ctx fiber.Ctx) error {
	var r req.PageAnchorSongAdmin
	if err := ctx.Bind().Query(&r); err != nil {
		return errs.WrapError(err, errMsgInvalidParams, string(errs.ReqParamValidFailed))
	}

	if err := validate.Struct(r); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}

	appCtx := g.FromFiberCtx(ctx)

	// 检查是否登录
	if !appCtx.IsLogin {
		return errs.LoginStatusError
	}

	// Anchor角色使用appCtx中的bid
	if appCtx.Role.IsAnchor() {
		if appCtx.BID == nil {
			return errs.NoBindAnchorError
		}
		r.Bid = *appCtx.BID
	}

	result, err := services.AnchorSong.PageAdmin(appCtx, r.Bid, r.Name, r.PageParam)
	if err != nil {
		return err
	}

	return ctx.JSON(wrapper.Success(result))
}
