package rest

import (
	"strconv"

	"fanclub-apiserver/api/wrapper"
	"fanclub-apiserver/dto/req"
	"fanclub-apiserver/dto/resp"
	"fanclub-apiserver/errs"
	"fanclub-apiserver/g"
	"fanclub-apiserver/services"

	"github.com/gofiber/fiber/v3"
	"go.uber.org/zap"
)

// registerCookie 注册 Cookie 管理相关路由
func registerCookie(router fiber.Router) {
	apis := new(cookie)
	cookieGroup := router.Group("admin/cookies")
	cookieGroup.Use(jwtHandler)
	cookieGroup.Get("", apis.List)
	cookieGroup.Post("", apis.Create)
	cookieGroup.Get("/:id", apis.GetByID)
	cookieGroup.Put("/:id", apis.Update)
	cookieGroup.Delete("/:id", apis.Delete)
	cookieGroup.Post("/refresh/:id", apis.RefreshByID)
	cookieGroup.Post("/refresh-all", apis.RefreshAll)
}

// cookie Cookie 管理控制器
type cookie struct {
}

// List 获取 Cookie 列表
//
//	@Summary		获取 Cookie 列表
//	@Description	分页获取 Cookie 列表，支持按类型和刷新状态筛选
//	@Tags			Cookie管理
//	@Security		BearerAuth
//	@Accept			json
//	@Produce		json
//	@Param			req		query		req.CookieListReq	true	"查询参数"
//	@Success		200		{object}	wrapper.JsonPageResp[resp.CookieInfo]
//	@Router			/admin/cookies [get]
func (c *cookie) List(ctx fiber.Ctx) error {
	var r req.CookieListReq
	if err := ctx.Bind().Query(&r); err != nil {
		return errs.WrapError(err, errMsgInvalidParams, string(errs.ReqParamValidFailed))
	}

	appCtx := g.FromFiberCtx(ctx)
	cookies, total, err := services.Cookie.List(appCtx.C, &r)
	if err != nil {
		return err
	}

	pageSize := r.PageSize
	if pageSize <= 0 {
		pageSize = 10
	}
	totalPage := int((total + int64(pageSize) - 1) / int64(pageSize))

	return ctx.JSON(wrapper.Success(resp.PageResult[resp.CookieInfo]{
		TotalRowCount: int(total),
		TotalPage:     totalPage,
		Records:       cookies,
	}))
}

// Create 创建 Cookie
//
//	@Summary		创建 Cookie
//	@Description	创建新的 Cookie 记录
//	@Tags			Cookie管理
//	@Security		BearerAuth
//	@Accept			json
//	@Produce		json
//	@Param			body	body		req.CookieCreateReq	true	"创建请求"
//	@Success		200		{object}	wrapper.JsonResp[resp.CookieInfo]
//	@Router			/admin/cookies [post]
func (c *cookie) Create(ctx fiber.Ctx) error {
	var r req.CookieCreateReq
	if err := ctx.Bind().Body(&r); err != nil {
		return errs.WrapError(err, errMsgInvalidBody, string(errs.ReqParamValidFailed))
	}

	// 验证请求参数
	if err := validate.Struct(r); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}

	appCtx := g.FromFiberCtx(ctx)
	cookie, err := services.Cookie.Create(appCtx.C, &r)
	if err != nil {
		return err
	}

	g.Info("创建Cookie记录",
		zap.Int64("cookieID", cookie.ID),
		zap.String("name", cookie.Name),
		zap.Int64("uid", cookie.UID))

	return ctx.JSON(wrapper.Success(resp.CookieInfo{
		ID:          cookie.ID,
		Name:        cookie.Name,
		Value:       maskCookieValue(cookie.Name, cookie.Value),
		Domain:      cookie.Domain,
		ExpiresAt:   cookie.ExpiresAt,
		UID:         cookie.UID,
		NeedRefresh: cookie.NeedRefresh,
	}))
}

// GetByID 获取单个 Cookie
//
//	@Summary		获取 Cookie
//	@Description	根据 ID 获取 Cookie 详情
//	@Tags			Cookie管理
//	@Security		BearerAuth
//	@Accept			json
//	@Produce		json
//	@Param			id	path		int	true	"Cookie ID"
//	@Success		200	{object}	wrapper.JsonResp[resp.CookieInfo]
//	@Router			/admin/cookies/{id} [get]
func (c *cookie) GetByID(ctx fiber.Ctx) error {
	idStr := ctx.Params("id")
	id, err := strconv.ParseInt(idStr, 10, 64)
	if err != nil {
		return errs.WrapError(err, "无效的Cookie ID", string(errs.ReqParamValidFailed))
	}

	appCtx := g.FromFiberCtx(ctx)
	cookie, err := services.Cookie.GetByID(appCtx.C, id)
	if err != nil {
		return err
	}

	return ctx.JSON(wrapper.Success(resp.CookieInfo{
		ID:              cookie.ID,
		Name:            cookie.Name,
		Value:           maskCookieValue(cookie.Name, cookie.Value),
		Domain:          cookie.Domain,
		ExpiresAt:       cookie.ExpiresAt,
		UID:             cookie.UID,
		NeedRefresh:     cookie.NeedRefresh,
		LastRefreshTime: cookie.LastRefreshTime,
		CreatedAt:       cookie.CreatedTime.UnixMilli(),
		UpdatedAt:       cookie.UpdatedTime.UnixMilli(),
	}))
}

// Update 更新 Cookie
//
//	@Summary		更新 Cookie
//	@Description	根据 ID 更新 Cookie 信息
//	@Tags			Cookie管理
//	@Security		BearerAuth
//	@Accept			json
//	@Produce		json
//	@Param			id		path		int					true	"Cookie ID"
//	@Param			body	body		req.CookieUpdateReq	true	"更新请求"
//	@Success		200		{object}	wrapper.JsonResp[string]
//	@Router			/admin/cookies/{id} [put]
func (c *cookie) Update(ctx fiber.Ctx) error {
	idStr := ctx.Params("id")
	id, err := strconv.ParseInt(idStr, 10, 64)
	if err != nil {
		return errs.WrapError(err, "无效的Cookie ID", string(errs.ReqParamValidFailed))
	}

	var r req.CookieUpdateReq
	if err := ctx.Bind().Body(&r); err != nil {
		return errs.WrapError(err, errMsgInvalidBody, string(errs.ReqParamValidFailed))
	}

	// 验证请求参数
	if err := validate.Struct(r); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}

	appCtx := g.FromFiberCtx(ctx)
	if err := services.Cookie.Update(appCtx.C, id, &r); err != nil {
		return err
	}

	g.Info("更新Cookie记录",
		zap.Int64("cookieID", id),
		zap.String("name", r.Name))

	return ctx.JSON(wrapper.Success("更新成功"))
}

// Delete 删除 Cookie
//
//	@Summary		删除 Cookie
//	@Description	根据 ID 删除 Cookie
//	@Tags			Cookie管理
//	@Security		BearerAuth
//	@Accept			json
//	@Produce		json
//	@Param			id	path	int	true	"Cookie ID"
//	@Success		200	{object}	wrapper.JsonResp[string]
//	@Router			/admin/cookies/{id} [delete]
func (c *cookie) Delete(ctx fiber.Ctx) error {
	idStr := ctx.Params("id")
	id, err := strconv.ParseInt(idStr, 10, 64)
	if err != nil {
		return errs.WrapError(err, "无效的Cookie ID", string(errs.ReqParamValidFailed))
	}

	appCtx := g.FromFiberCtx(ctx)
	if err := services.Cookie.Delete(appCtx.C, id); err != nil {
		return err
	}

	g.Info("删除Cookie记录",
		zap.Int64("cookieID", id))

	return ctx.JSON(wrapper.Success("删除成功"))
}

// RefreshByID 刷新指定 Cookie
//
//	@Summary		刷新 Cookie
//	@Description	手动刷新指定的 Cookie，从 B站 获取新的授权令牌
//	@Tags			Cookie管理
//	@Security		BearerAuth
//	@Accept			json
//	@Produce		json
//	@Param			id	path	int	true	"Cookie ID"
//	@Success		200	{object}	wrapper.JsonResp[resp.CookieRefreshResp]
//	@Router			/admin/cookies/refresh/{id} [post]
func (c *cookie) RefreshByID(ctx fiber.Ctx) error {
	idStr := ctx.Params("id")
	id, err := strconv.ParseInt(idStr, 10, 64)
	if err != nil {
		return errs.WrapError(err, "无效的Cookie ID", string(errs.ReqParamValidFailed))
	}

	g.Info("手动刷新Cookie开始",
		zap.Int64("cookieID", id))

	appCtx := g.FromFiberCtx(ctx)
	result, err := services.CookieRefresh.RefreshByID(appCtx.C, id)
	if err != nil {
		g.Error("刷新Cookie失败",
			zap.Int64("cookieID", id),
			zap.Error(err))
		return err
	}

	if result.Success {
		g.Info("刷新Cookie成功",
			zap.Int64("cookieID", id))
	} else {
		g.Warn("刷新Cookie未成功",
			zap.Int64("cookieID", id),
			zap.String("message", result.Message))
	}

	return ctx.JSON(wrapper.Success(result))
}

// RefreshAll 刷新所有需要刷新的 Cookie
//
//	@Summary		批量刷新 Cookie
//	@Description	刷新所有标记为需要刷新的 Cookie
//	@Tags			Cookie管理
//	@Security		BearerAuth
//	@Accept			json
//	@Produce		json
//	@Success		200	{object}	wrapper.JsonResp[resp.CookieBatchRefreshResp]
//	@Router			/admin/cookies/refresh-all [post]
func (c *cookie) RefreshAll(ctx fiber.Ctx) error {
	g.Info("开始批量刷新Cookie")

	appCtx := g.FromFiberCtx(ctx)
	result, err := services.CookieRefresh.RefreshAll(appCtx.C)
	if err != nil {
		g.Error("批量刷新Cookie失败", zap.Error(err))
		return err
	}

	g.Info("批量刷新Cookie完成",
		zap.Int("total", result.Total),
		zap.Int("success", result.SuccessCount),
		zap.Int("failed", result.FailedCount))

	return ctx.JSON(wrapper.Success(result))
}

// maskCookieValue 对 Cookie 值进行脱敏处理
func maskCookieValue(name, value string) string {
	// 特殊处理 SESSDATA 和 RefreshToken
	if name == "SESSDATA" || name == "sessdata" {
		if len(value) > 16 {
			return value[:8] + "..." + value[len(value)-8:]
		}
	}
	if name == "RefreshToken" || name == "refresh_token" {
		if len(value) > 16 {
			return value[:8] + "..." + value[len(value)-8:]
		}
	}
	// 其他值保留原样
	if len(value) <= 8 {
		return "********"
	}
	return value[:4] + "..." + value[len(value)-4:]
}
