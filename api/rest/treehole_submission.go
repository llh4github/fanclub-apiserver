package rest

import (
	"fanclub-apiserver/api/wrapper"
	"fanclub-apiserver/consts"
	"fanclub-apiserver/dto/req"
	"fanclub-apiserver/dto/resp"
	"fanclub-apiserver/errs"
	"fanclub-apiserver/g"
	"fanclub-apiserver/middleware"
	"fanclub-apiserver/services"

	"github.com/gofiber/fiber/v3"
)

func registerTreeholeSubmission(router fiber.Router) {
	apis := new(treeholeSubmission)
	router.Group("treehole/submission").
		Post("/add", middleware.PublicReadRateLimiter, apis.Create).
		Get("/get", middleware.PublicReadRateLimiter, apis.GetBySubmissionID).
		Get("/admin/navigate", middleware.AuthWriteRateLimiter, jwtHandler, apis.Navigate).
		Get("/admin/page", middleware.AuthWriteRateLimiter, jwtHandler, apis.PageAdmin).
		Put("/admin/audit", middleware.AuthWriteRateLimiter, jwtHandler, apis.UpdateAuditStatus)
}

// treeholeSubmission 树洞投稿控制器
type treeholeSubmission struct {
}

// Create 创建树洞投稿
//
//	@Summary		创建树洞投稿
//	@Description	创建新的树洞投稿，系统自动处理Markdown转HTML并生成摘要
//	@Tags			树洞投稿
//	@Accept			json
//	@Produce		json
//	@Param			req	body		req.CreateTreeholeSubmission	true	"投稿内容"
//	@Success		200	{object}	wrapper.JsonResp[resp.CreateSubmission]
//	@Router			/treehole/submission/add [post]
func (c *treeholeSubmission) Create(ctx fiber.Ctx) error {
	var r req.CreateTreeholeSubmission
	if err := ctx.Bind().Body(&r); err != nil {
		return errs.WrapError(err, errMsgInvalidParams, string(errs.ReqParamValidFailed))
	}

	// 验证请求参数
	if err := validate.Struct(r); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}

	// 校验验证码 Token
	appCtx := g.FromFiberCtx(ctx)
	if err := services.Captcha.VerifyCaptchaToken(appCtx, r.CaptchaToken, consts.CaptchaSceneSubmission); err != nil {
		return errs.WrapError(err, "验证码不正确或已过期", string(errs.CaptchaVerifyFailed))
	}

	// 创建投稿（事务验证在 Service 层完成）
	submissionID, err := services.TreeholeSubmission.Create(appCtx, int64(r.TopicID), r.Content)
	if err != nil {
		return err
	}

	return ctx.JSON(wrapper.Success(&resp.CreateSubmission{
		SubmissionID: submissionID,
	}))
}

// GetBySubmissionID 根据投稿ID获取投稿详情
//
//	@Summary		根据投稿ID获取投稿详情
//	@Description	根据投稿ID获取审核通过的投稿详情，查不到返回空数据不报错
//	@Tags			树洞投稿
//	@Accept			json
//	@Produce		json
//	@Param			req	query		req.GetTreeholeSubmission	true	"查询参数"
//	@Success		200	{object}	wrapper.JsonResp[resp.SubmissionDetail]
//	@Router			/treehole/submission/get [get]
func (c *treeholeSubmission) GetBySubmissionID(ctx fiber.Ctx) error {
	var r req.GetTreeholeSubmission
	if err := ctx.Bind().Query(&r); err != nil {
		return errs.WrapError(err, errMsgInvalidParams, string(errs.ReqParamValidFailed))
	}

	if err := validate.Struct(r); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}

	appCtx := g.FromFiberCtx(ctx)
	submission, err := services.TreeholeSubmission.GetBySubmissionID(appCtx, r.SubmissionID)
	if err != nil {
		return err
	}

	return ctx.JSON(wrapper.Success(submission))
}

// PageAdmin 后台分页查询投稿列表
//
//	@Summary		后台分页查询投稿列表
//	@Description	后台分页查询所有投稿，支持按主题ID、审核状态和摘要筛选
//	@Tags			树洞投稿
//	@Security		BearerAuth
//	@Accept			json
//	@Produce		json
//	@Param			req	query		req.PageTreeholeSubmissionAdmin	true	"查询参数"
//	@Success		200	{object}	wrapper.JsonPageResp[resp.SubmissionListItem]
//	@Router			/treehole/submission/admin/page [get]
func (c *treeholeSubmission) PageAdmin(ctx fiber.Ctx) error {
	var r req.PageTreeholeSubmissionAdmin
	if err := ctx.Bind().Query(&r); err != nil {
		return errs.WrapError(err, errMsgInvalidParams, string(errs.ReqParamValidFailed))
	}

	if err := validate.Struct(r); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}

	appCtx := g.FromFiberCtx(ctx)

	// 转换审核状态
	var auditStatus *consts.AuditStatus
	if r.AuditStatus != nil {
		status := consts.AuditStatus(*r.AuditStatus)
		auditStatus = &status
	}

	result, err := services.TreeholeSubmission.PageAdmin(appCtx, r.TopicID, r.SubmissionID, auditStatus, r.PageParam)
	if err != nil {
		return err
	}

	return ctx.JSON(wrapper.Success(result))
}

// UpdateAuditStatus 修改投稿审核状态
//
//	@Summary		修改投稿审核状态
//	@Description	根据投稿ID修改审核状态，只能修改为通过或不通过状态
//	@Tags			树洞投稿
//	@Security		BearerAuth
//	@Accept			json
//	@Produce		json
//	@Param			req	body		req.UpdateSubmissionAuditStatus	true	"审核状态修改请求"
//	@Success		200	{object}	wrapper.JsonResp[string]
//	@Router			/treehole/submission/admin/audit [put]
func (c *treeholeSubmission) UpdateAuditStatus(ctx fiber.Ctx) error {
	var r req.UpdateSubmissionAuditStatus
	if err := ctx.Bind().Body(&r); err != nil {
		return errs.WrapError(err, errMsgInvalidBody, string(errs.ReqParamValidFailed))
	}

	if err := validate.Struct(r); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}

	appCtx := g.FromFiberCtx(ctx)

	if err := services.TreeholeSubmission.UpdateAuditStatusByID(appCtx, int64(r.ID), r.AuditStatus); err != nil {
		return err
	}

	return ctx.JSON(wrapper.Success("审核状态修改成功"))
}

// Navigate 树洞投稿导航
//
//	@Summary		树洞投稿导航
//	@Description	获取指定主题下的投稿列表（每页固定返回一条）。前端使用：<br>1. 首次加载：只传 topic_id，page_index=1；<br>2. 加载更多：page_index +1；<br>3. 当 record 为 null 时表示已无更多数据；<br>4. 按 submit_time 升序排列。<br>5. only_approved=true 时仅返回已审核通过的稿件；为 false 时返回已通过和待审核的稿件。
//	@Tags			树洞投稿
//	@Accept			json
//	@Produce		json
//	@Param			req	query		req.NavigateTreeholeSubmission	true	"导航请求参数"
//	@Success		200	{object}	wrapper.JsonResp[resp.TreeholeSubmissionNavResp]
//	@Router			/treehole/submission/admin/navigate [get]
//	@Security		BearerAuth
func (c *treeholeSubmission) Navigate(ctx fiber.Ctx) error {
	var r req.NavigateTreeholeSubmission
	if err := ctx.Bind().Query(&r); err != nil {
		return errs.WrapError(err, errMsgInvalidParams, string(errs.ReqParamValidFailed))
	}

	if err := validate.Struct(r); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}

	appCtx := g.FromFiberCtx(ctx)

	result, err := services.TreeholeSubmission.NavigateByTopicID(
		appCtx,
		r.TopicID,
		r.PageIndex,
		r.OnlyApproved,
	)
	if err != nil {
		return err
	}

	return ctx.JSON(wrapper.Success(result))
}
