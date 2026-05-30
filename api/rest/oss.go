package rest

import (
	"context"
	"fanclub-apiserver/api/wrapper"
	"fanclub-apiserver/dto/req"
	"fanclub-apiserver/dto/resp"
	"fanclub-apiserver/errs"
	"fanclub-apiserver/g"
	"fanclub-apiserver/middleware"
	"fanclub-apiserver/storage"
	"path"
	"time"

	"github.com/gofiber/fiber/v3"
)

func registerOSS(router fiber.Router) {
	apis := new(oss)
	router.Group("oss").
		Get("/image/upload", middleware.OSSRateLimiter, apis.GenerateImageUploadCredential)
}

type oss struct {
}

// GenerateImageUploadCredential 生成图片上传凭证
//
//	@Summary		生成图片上传凭证
//	@Description	生成一个5分钟有效的图片上传凭证，供客户端使用七牛 JS SDK 直传图片到 OSS
//	@Tags			对象存储
//	@Accept			json
//	@Produce		json
//	@Param			filename	query		string	true	"文件名（不含路径）"
//	@Success		200			{object}	wrapper.JsonResp[resp.ImageUploadCredential]
//	@Router			/oss/image/upload [get]
func (o *oss) GenerateImageUploadCredential(ctx fiber.Ctx) error {
	var r req.GenerateImageUploadCredential
	if err := ctx.Bind().Query(&r); err != nil {
		return errs.WrapError(err, errMsgInvalidParams, string(errs.ReqParamValidFailed))
	}
	if err := validate.Struct(r); err != nil {
		return errs.WrapError(err, err.Error(), string(errs.ReqParamValidFailed))
	}

	shortID, err := g.NextIDStr()
	if err != nil {
		return errs.WrapError(err, "生成ID失败", string(errs.UnkonwError))
	}
	key := "treehole/" + time.Now().Format("2006/01/") + shortID + path.Ext(r.Filename)
	uploadToken, err := storage.GenerateUploadToken(context.Background(), storage.PresignedPutObjectInput{
		Key:         key,
		ContentType: "image/*",
		ExpiresIn:   5 * 60e9,        // 上传凭证有效期5分钟
		MaxSize:     6 * 1024 * 1024, // 6 MB
	})
	if err != nil {
		return err
	}

	return ctx.JSON(wrapper.Success(resp.ImageUploadCredential{
		Token:     uploadToken.Token,
		ObjectKey: uploadToken.ObjectKey,
		Region:    uploadToken.Region,
		ExpiresAt: uploadToken.ExpiresAt,
	}))
}
