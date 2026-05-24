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

func registerTreeholeTopic(router fiber.Router) {
	apis := new(treeholeTopic)
	router.Group("treehole/topic").
		Post("/add", middleware.AuthWriteRateLimiter, jwtHandler, apis.Create).
		Put("/update", middleware.AuthWriteRateLimiter, jwtHandler, apis.Update).
		Put("/status", middleware.AuthWriteRateLimiter, jwtHandler, apis.SetStatus).
		Get("/get", middleware.PublicReadRateLimiter, apis.GetByID).
		Get("/admin/page", middleware.AuthWriteRateLimiter, jwtHandler, apis.PageAdmin).
		Get("/count", middleware.PublicReadRateLimiter, apis.CountOpenTopics).
		Get("/list", middleware.PublicReadRateLimiter, apis.GetLatestActiveTopics)
}

// treeholeTopic 树洞主题控制器
type treeholeTopic struct {
}

// Create 创建树洞主题
//
//	@Summary		创建树洞主题
//	@Description	创建新的树洞主题，需要管理员权限
//	@Tags			树洞主题
//	@Security		BearerAuth
//	@Accept			json
//	@Produce		json
//	@Param			topic	body		req.CreateTreeholeTopic	true	"树洞主题"
//	@Success		200		{object}	wrapper.JsonResp[string]
//	@Router			/treehole/topic/add [post]
func (c *treeholeTopic) Create(ctx fiber.Ctx) error {
	var r req.CreateTreeholeTopic
	if err := ctx.Bind().Body(&r); err != nil {
		return errs.WrapError(err, errMsgInvalidParams, string(errs.ReqParamValidFailed))
	}

	// 验证请求参数
	if err := validate.Struct(r); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}

	// 验证关闭时间必须晚于开放时间
	if !r.CloseAt.After(r.OpenAt) {
		return errs.WrapError(nil, "关闭时间必须晚于开放时间", string(errs.ReqParamValidFailed))
	}

	// 创建实体
	topic := &model.TreeholeTopic{
		Bid:         int64(r.Bid),
		Title:       r.Title,
		Description: r.Description,
		OpenAt:      r.OpenAt,
		CloseAt:     r.CloseAt,
		IsActive:    r.IsActive,
	}

	appCtx := g.FromFiberCtx(ctx)
	if err := services.TreeholeTopic.Create(appCtx, topic); err != nil {
		return err
	}

	return ctx.JSON(wrapper.Success(""))
}

// GetByID 根据ID获取树洞主题
//
//	@Summary		根据ID获取树洞主题
//	@Description	根据ID获取树洞主题记录，返回详细信息（包含投稿统计数据）
//	@Tags			树洞主题
//	@Accept			json
//	@Produce		json
//	@Param			id	query		int	true	"主题ID"
//	@Success		200	{object}	wrapper.JsonResp[resp.TopicDetail]
//	@Router			/treehole/topic/get [get]
func (c *treeholeTopic) GetByID(ctx fiber.Ctx) error {
	var r req.GetTreeholeTopic
	if err := ctx.Bind().Query(&r); err != nil {
		return errs.WrapError(err, errMsgInvalidParams, string(errs.ReqParamValidFailed))
	}

	if err := validate.Struct(r); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}

	appCtx := g.FromFiberCtx(ctx)

	if r.ID > 0 {
		// 使用 ID 查询，返回详细信息（包含统计数据）
		topicDetail, err := services.TreeholeTopic.GetTopicDetailByID(appCtx, r.ID)
		if err != nil {
			return err
		}
		return ctx.JSON(wrapper.Success(topicDetail))
	}

	if r.Bid > 0 {
		// 使用 BID 查询，返回基本信息
		topic, err := services.TreeholeTopic.GetByBid(appCtx, r.Bid)
		if err != nil {
			return err
		}
		return ctx.JSON(wrapper.Success(topic))
	}

	return errs.WrapError(nil, "id或bid必须提供一个", string(errs.ReqParamValidFailed))
}

// Update 更新树洞主题
//
//	@Summary		更新树洞主题
//	@Description	根据ID更新树洞主题记录
//	@Tags			树洞主题
//	@Security		BearerAuth
//	@Accept			json
//	@Produce		json
//	@Param			req	body		req.UpdateTreeholeTopic	true	"更新参数"
//	@Success		200	{object}	wrapper.JsonResp[string]
//	@Router			/treehole/topic/update [put]
func (c *treeholeTopic) Update(ctx fiber.Ctx) error {
	var r req.UpdateTreeholeTopic
	if err := ctx.Bind().Body(&r); err != nil {
		return errs.WrapError(err, errMsgInvalidBody, string(errs.ReqParamValidFailed))
	}

	if err := validate.Struct(r); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}

	// 验证关闭时间必须晚于开放时间
	if !r.CloseAt.IsZero() && !r.OpenAt.IsZero() && !r.CloseAt.After(r.OpenAt) {
		return errs.WrapError(nil, "关闭时间必须晚于开放时间", string(errs.ReqParamValidFailed))
	}

	appCtx := g.FromFiberCtx(ctx)

	if err := services.TreeholeTopic.UpdateByID(appCtx, &r); err != nil {
		return err
	}

	return ctx.JSON(wrapper.Success(""))
}

// PageAdmin 后台分页查询树洞主题
//
//	@Summary		后台分页查询树洞主题
//	@Description	后台分页查询树洞主题，返回话题信息及对应投稿统计数据
//	@Tags			树洞主题
//	@Security		BearerAuth
//	@Accept			json
//	@Produce		json
//	@Param			req	query		req.PageTreeholeTopicAdmin	true	"查询参数"
//	@Success		200	{object}	wrapper.JsonPageResp[resp.TopicPageItem]
//	@Router			/treehole/topic/admin/page [get]
func (c *treeholeTopic) PageAdmin(ctx fiber.Ctx) error {
	var r req.PageTreeholeTopicAdmin
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

	result, err := services.TreeholeTopic.PageAdmin(appCtx, r.Bid, r.Title, r.IsActive, r.PageParam)
	if err != nil {
		return err
	}

	return ctx.JSON(wrapper.Success(result))
}

// CountOpenTopics 查询指定bid在当前时间内有几个开放投稿话题
//
//	@Summary		查询开放投稿话题数量
//	@Description	查询指定bid在当前时间内有几个开放投稿话题，支持缓存
//	@Tags			树洞主题
//	@Accept			json
//	@Produce		json
//	@Param			bid	query		int	true	"主播B站UID"
//	@Success		200	{object}	wrapper.JsonResp[int]
//	@Router			/treehole/topic/count [get]
func (c *treeholeTopic) CountOpenTopics(ctx fiber.Ctx) error {
	var r req.GetTreeholeTopic
	if err := ctx.Bind().Query(&r); err != nil {
		return errs.WrapError(err, errMsgInvalidParams, string(errs.ReqParamValidFailed))
	}

	if err := validate.Struct(r); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}

	if r.Bid == 0 {
		return errs.WrapError(nil, "bid不能为空", string(errs.ReqParamValidFailed))
	}

	appCtx := g.FromFiberCtx(ctx)
	count, err := services.TreeholeTopic.CountOpenTopics(appCtx, r.Bid)
	if err != nil {
		return err
	}

	return ctx.JSON(wrapper.Success(count))
}

// GetLatestActiveTopics 查询指定bid最新5条启用状态的话题
//
//	@Summary		查询最新启用的话题
//	@Description	按创建时间查询指定bid最新5条启用状态的话题，返回主键ID、标题、描述、起止时间
//	@Tags			树洞主题
//	@Accept			json
//	@Produce		json
//	@Param			bid	query		int	true	"主播B站UID"
//	@Success		200	{object}	wrapper.JsonResp[[]resp.TopicBrief]
//	@Router			/treehole/topic/list [get]
func (c *treeholeTopic) GetLatestActiveTopics(ctx fiber.Ctx) error {
	var r req.GetTreeholeTopic
	if err := ctx.Bind().Query(&r); err != nil {
		return errs.WrapError(err, errMsgInvalidParams, string(errs.ReqParamValidFailed))
	}

	if err := validate.Struct(r); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}

	if r.Bid == 0 {
		return errs.WrapError(nil, "bid不能为空", string(errs.ReqParamValidFailed))
	}

	appCtx := g.FromFiberCtx(ctx)
	topics, err := services.TreeholeTopic.GetLatestActiveTopics(appCtx, r.Bid)
	if err != nil {
		return err
	}

	return ctx.JSON(wrapper.Success(topics))
}

// SetStatus 设置主题的启用/禁用状态
//
//	@Summary		设置主题状态
//	@Description	根据ID设置主题的启用/禁用状态，修改后清除对应缓存
//	@Tags			树洞主题
//	@Security		BearerAuth
//	@Accept			json
//	@Produce		json
//	@Param			req	body		req.SetTopicStatusReq	true	"设置状态参数"
//	@Success		200	{object}	wrapper.JsonResp[string]
//	@Router			/treehole/topic/status [put]
func (c *treeholeTopic) SetStatus(ctx fiber.Ctx) error {
	var r req.SetTopicStatusReq
	if err := ctx.Bind().Body(&r); err != nil {
		return errs.WrapError(err, errMsgInvalidBody, string(errs.ReqParamValidFailed))
	}

	if err := validate.Struct(r); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}

	appCtx := g.FromFiberCtx(ctx)
	if err := services.TreeholeTopic.SetTopicStatus(appCtx, r.ID, r.IsActive); err != nil {
		return err
	}

	return ctx.JSON(wrapper.Success(""))
}
