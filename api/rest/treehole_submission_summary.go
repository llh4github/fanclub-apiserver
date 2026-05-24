package rest

import (
	"bufio"
	"fmt"
	"strings"

	"fanclub-apiserver/ai"
	"fanclub-apiserver/api/wrapper"
	"fanclub-apiserver/cache"
	"fanclub-apiserver/dto/req"
	"fanclub-apiserver/errs"
	"fanclub-apiserver/g"

	"github.com/gofiber/fiber/v3"
)

func registerTreeholeSubmissionSummary(router fiber.Router) {
	apis := new(treeholeSubmissionSummary)
	apisStream := new(treeholeSubmissionSummaryStream)
	router.Group("treehole/submission").
		Get("/summary", apis.Get).
		Get("/summary/stream", apisStream.Stream)
}

type treeholeSubmissionSummary struct {
}

type treeholeSubmissionSummaryStream struct {
}

func (c *treeholeSubmissionSummary) Get(ctx fiber.Ctx) error {
	var r req.GetSubmissionSummary
	if err := ctx.Bind().Query(&r); err != nil {
		return errs.WrapError(err, errMsgInvalidParams, string(errs.ReqParamValidFailed))
	}

	if err := validate.Struct(r); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}

	appCtx := g.FromFiberCtx(ctx)
	submissionID := int64(r.SubmissionID)

	result, err := ai.SubmissionSummary.GetFromDB(
		appCtx.C,
		submissionID,
	)
	if err != nil {
		return err
	}

	source := "none"
	if result != nil {
		source = "cache"
	}
	ctx.Set("X-Summary-Source", source)

	return ctx.JSON(wrapper.Success(result))
}

func (c *treeholeSubmissionSummaryStream) Stream(ctx fiber.Ctx) error {
	var r req.GetSubmissionSummary
	if err := ctx.Bind().Query(&r); err != nil {
		return errs.WrapError(err, errMsgInvalidParams, string(errs.ReqParamValidFailed))
	}

	if err := validate.Struct(r); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}

	appCtx := g.FromFiberCtx(ctx)
	submissionID := int64(r.SubmissionID)

	locked := cache.AcquireSummaryLock(appCtx.C, submissionID)
	if !locked {
		return errs.WrapError(nil, "总结正在生成中，请稍后再试", string(errs.ConcurrentOperation))
	}

	ctx.Set("Content-Type", "text/event-stream")
	ctx.Set("Cache-Control", "no-cache")
	ctx.Set("Connection", "keep-alive")
	ctx.Set("X-Summary-Source", "stream")

	return ctx.SendStreamWriter(func(w *bufio.Writer) {
		defer cache.ReleaseSummaryLock(appCtx.C, submissionID)

		sendEvent := func(eventType, data string) error {
			escapedData := strings.ReplaceAll(data, "\n", "\\n")
			_, err := fmt.Fprintf(w, "event: %s\ndata: {\"content\":\"%s\"}\n\n", eventType, escapedData)
			if err != nil {
				return err
			}
			return w.Flush()
		}

		err := ai.SubmissionSummary.StreamGenerateWithCallback(
			appCtx.C,
			submissionID,
			func(content string) error {
				return sendEvent("content", content)
			},
		)

		if err != nil {
			errMsg := fmt.Sprintf("生成总结失败: %v", err)
			sendEvent("error", errMsg)
			return
		}

		sendEvent("done", "")
	})
}
