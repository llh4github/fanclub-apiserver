package rest

import (
	"encoding/json"
	"time"

	"fanclub-apiserver/database/model"
	"fanclub-apiserver/dto/req"
	"fanclub-apiserver/g"
	"fanclub-apiserver/services"
	"fanclub-apiserver/storage"

	"github.com/gofiber/fiber/v3"
	"go.uber.org/zap"
)

// Deprecated: 暂不使用
func registerOssCallback(router fiber.Router) {
	apis := new(ossCallback)
	router.Group("oss").
		Post("/callback", apis.HandleCallback)
}

// Deprecated: 暂不使用
type ossCallback struct {
}

// QiniuCallbackResponse 七牛云回调响应
type QiniuCallbackResponse struct {
	Success bool   `json:"success"`
	Error   string `json:"error,omitempty"`
}

// HandleCallback 处理七牛云上传回调
//
//	@Summary		处理七牛云上传回调
//	@Description	接收并处理七牛云上传完成后的回调请求，验证签名并保存回调数据
//	@Deprecated		暂不使用
//	@Tags			对象存储回调
//	@Accept			application/json
//	@Produce		application/json
//	@Param			Authorization	header		string					true	"七牛云回调签名"
//	@Param			Content-Type	header		string					true	"Content-Type: application/json 或 application/x-www-form-urlencoded"
//	@Param			body			body		req.QiniuCallbackBody	true	"回调请求体"
//	@Success		200				{object}	QiniuCallbackResponse
//	@Router			/oss/callback [post]
func (c *ossCallback) HandleCallback(ctx fiber.Ctx) error {
	appCtx := g.FromFiberCtx(ctx)
	ctxVal := appCtx.C

	authorization := ctx.Get("Authorization")
	requestPath := string(ctx.Request().URI().Path())
	rawBody := string(ctx.Body())

	g.Logger.Info("接收到七牛云上传回调",
		zap.String("ip", ctx.IP()),
		zap.String("path", requestPath),
		zap.String("content_type", string(ctx.Request().Header.ContentType())),
	)

	if authorization == "" {
		g.Logger.Warn("七牛云回调验证失败：Authorization 头为空")
		return c.sendCallbackResponse(ctx, false, "missing authorization header")
	}

	valid, err := storage.VerifyQiniuCallback(authorization, requestPath, rawBody)
	if err != nil {
		g.Logger.Warn("七牛云回调签名验证失败",
			zap.Error(err),
			zap.String("ip", ctx.IP()),
		)
		return c.sendCallbackResponse(ctx, false, "invalid authorization: "+err.Error())
	}

	if !valid {
		g.Logger.Warn("七牛云回调签名不匹配",
			zap.String("ip", ctx.IP()),
			zap.String("authorization", authorization),
		)
		return c.sendCallbackResponse(ctx, false, "signature mismatch")
	}

	g.Logger.Info("七牛云回调签名验证通过")

	contentType := string(ctx.Request().Header.ContentType())
	var callbackBody req.QiniuCallbackBody

	if contentType == "application/json" {
		if err := json.Unmarshal([]byte(rawBody), &callbackBody); err != nil {
			g.Logger.Error("解析回调 JSON 数据失败",
				zap.Error(err),
				zap.String("body", rawBody),
			)
			return c.sendCallbackResponse(ctx, false, "invalid json body")
		}
	} else {
		values := ctx.Req().FormValue("key")
		if values == "" {
			g.Logger.Warn("表单数据中未找到 key 字段")
			return c.sendCallbackResponse(ctx, false, "missing key field")
		}

		callbackBody.Key = values

		if hash := ctx.Req().FormValue("hash"); hash != "" {
			callbackBody.Hash = hash
		}
		if fsize := ctx.Req().FormValue("fsize"); fsize != "" {
			var size int64
			if err := json.Unmarshal([]byte(fsize), &size); err == nil {
				callbackBody.Fsize = size
			}
		}
		if fname := ctx.Req().FormValue("fname"); fname != "" {
			callbackBody.Fname = fname
		}
		if mimeType := ctx.Req().FormValue("mimeType"); mimeType != "" {
			callbackBody.MimeType = mimeType
		}
	}

	if callbackBody.Key == "" {
		g.Logger.Warn("回调数据中缺少 key 字段")
		return c.sendCallbackResponse(ctx, false, "missing key field")
	}

	existing, err := services.OssUploadCallback.GetCallbackByKey(ctxVal, callbackBody.Key)
	if err != nil {
		g.Logger.Error("查询回调记录失败",
			zap.Error(err),
			zap.String("key", callbackBody.Key),
		)
	}

	var callback *model.OssUploadCallback
	if existing != nil {
		callback = existing
		callback.FileHash = callbackBody.Hash
		callback.FileSize = callbackBody.Fsize
		callback.MimeType = callbackBody.MimeType
		callback.FileName = callbackBody.Fname
		callback.UploadedAt = time.Now()
		callback.Status = int(model.CallbackStatusSuccess)
		callback.RawData = rawBody
		callback.ErrorMsg = ""
	} else {
		callback = &model.OssUploadCallback{
			FileKey:     callbackBody.Key,
			FileHash:    callbackBody.Hash,
			FileSize:    callbackBody.Fsize,
			MimeType:    callbackBody.MimeType,
			FileName:    callbackBody.Fname,
			UploadedAt:  time.Now(),
			Status:      int(model.CallbackStatusSuccess),
			CallbackURL: requestPath,
			RawData:     rawBody,
		}
	}

	if existing != nil {
		if err := g.DB.WithContext(ctxVal).Save(callback).Error; err != nil {
			g.Logger.Error("更新回调记录失败",
				zap.Error(err),
				zap.String("key", callbackBody.Key),
			)
			return c.sendCallbackResponse(ctx, false, "database update failed")
		}
	} else {
		if err := services.OssUploadCallback.SaveCallback(ctxVal, callback); err != nil {
			g.Logger.Error("保存回调记录失败",
				zap.Error(err),
				zap.String("key", callbackBody.Key),
			)
			return c.sendCallbackResponse(ctx, false, "database save failed")
		}
	}

	g.Logger.Info("七牛云回调处理成功",
		zap.String("key", callbackBody.Key),
		zap.String("hash", callbackBody.Hash),
		zap.Int64("size", callbackBody.Fsize),
		zap.String("filename", callbackBody.Fname),
	)

	return c.sendCallbackResponse(ctx, true, "")
}

// sendCallbackResponse 发送七牛云回调响应
//
// 七牛云要求回调响应必须为合法的 JSON 格式
func (c *ossCallback) sendCallbackResponse(ctx fiber.Ctx, success bool, errorMsg string) error {
	ctx.Set("Content-Type", "application/json")

	response := QiniuCallbackResponse{
		Success: success,
		Error:   errorMsg,
	}

	return ctx.JSON(response)
}
