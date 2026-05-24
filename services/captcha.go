package services

import (
	"context"
	"encoding/json"
	"fmt"
	"image"
	"strconv"
	"strings"
	"time"

	"fanclub-apiserver/cache"
	"fanclub-apiserver/consts"
	"fanclub-apiserver/dto/req"
	"fanclub-apiserver/dto/resp"
	"fanclub-apiserver/errs"
	"fanclub-apiserver/g"

	"github.com/golang/freetype"
	"github.com/golang/freetype/truetype"
	"github.com/wenlng/go-captcha-assets/bindata/fonts/fzshengsksjw_cu"
	"github.com/wenlng/go-captcha-assets/bindata/images/image_1"
	"github.com/wenlng/go-captcha-assets/bindata/images/image_2"
	"github.com/wenlng/go-captcha-assets/bindata/images/image_3"
	"github.com/wenlng/go-captcha-assets/bindata/images/image_4"
	"github.com/wenlng/go-captcha-assets/bindata/images/image_5"
	"github.com/wenlng/go-captcha-assets/resources/imagesv2"
	"github.com/wenlng/go-captcha-assets/resources/tiles"
	"github.com/wenlng/go-captcha/v2/base/codec"
	"github.com/wenlng/go-captcha/v2/base/option"
	"github.com/wenlng/go-captcha/v2/click"
	"github.com/wenlng/go-captcha/v2/slide"
	"go.uber.org/zap"
)

// 验证码服务
var Captcha = new(captchaService)

// clickCapt 点选验证码实例
var clickCapt click.Captcha

// slideCapt 滑动验证码实例
var slideCapt slide.Captcha

type captchaService struct {
}

func init() {
	clickBuilder := click.NewBuilder(
		click.WithImageSize(option.Size{Width: 300, Height: 220}),
		click.WithRangeLen(option.RangeVal{Min: 4, Max: 6}),
		click.WithRangeVerifyLen(option.RangeVal{Min: 2, Max: 4}),
		click.WithDisplayShadow(true),
	)

	fonts := loadClickFonts()
	bgs := loadClickBackgrounds()

	clickBuilder.SetResources(
		click.WithFonts(fonts),
		click.WithBackgrounds(bgs),
	)

	clickCapt = clickBuilder.Make()

	slideBgs := loadSlideBackgrounds()
	slideGraphs := loadSlideGraphs()

	slideBuilder := slide.NewBuilder(
		slide.WithImageSize(option.Size{Width: 280, Height: 150}),
	)
	slideBuilder.SetResources(
		slide.WithBackgrounds(slideBgs),
		slide.WithGraphImages(slideGraphs),
	)
	slideCapt = slideBuilder.Make()
}

// loadClickFonts 加载点选验证码字体
func loadClickFonts() []*truetype.Font {
	fontBytes, err := fzshengsksjw_cu.Asset("sourcedata/fonts/fzshengsksjw_cu/font.ttf")
	if err != nil {
		g.Error("加载验证码字体失败", zap.Error(err))
		return nil
	}
	font, err := freetype.ParseFont(fontBytes)
	if err != nil {
		g.Error("解析验证码字体失败", zap.Error(err))
		return nil
	}
	return []*truetype.Font{font}
}

// loadClickBackgrounds 加载点选验证码背景图片
func loadClickBackgrounds() []image.Image {
	var bgs []image.Image

	imgBytes, err := image_1.Asset("sourcedata/images/image-1/image.jpg")
	if err == nil {
		img, err := codec.DecodeByteToJpeg(imgBytes)
		if err == nil {
			bgs = append(bgs, img)
		}
	}

	imgBytes, err = image_2.Asset("sourcedata/images/image-2/image.jpg")
	if err == nil {
		img, err := codec.DecodeByteToJpeg(imgBytes)
		if err == nil {
			bgs = append(bgs, img)
		}
	}

	imgBytes, err = image_3.Asset("sourcedata/images/image-3/image.jpg")
	if err == nil {
		img, err := codec.DecodeByteToJpeg(imgBytes)
		if err == nil {
			bgs = append(bgs, img)
		}
	}

	imgBytes, err = image_4.Asset("sourcedata/images/image-4/image.jpg")
	if err == nil {
		img, err := codec.DecodeByteToJpeg(imgBytes)
		if err == nil {
			bgs = append(bgs, img)
		}
	}

	imgBytes, err = image_5.Asset("sourcedata/images/image-5/image.jpg")
	if err == nil {
		img, err := codec.DecodeByteToJpeg(imgBytes)
		if err == nil {
			bgs = append(bgs, img)
		}
	}

	g.Info(fmt.Sprintf("点选验证码背景图片加载成功，共 %d 张", len(bgs)))
	return bgs
}

// loadSlideBackgrounds 加载滑动验证码背景图片
func loadSlideBackgrounds() []image.Image {
	imgs, err := imagesv2.GetImages()
	if err != nil {
		g.Error("加载滑动验证码背景图片失败", zap.Error(err))
		return nil
	}

	g.Info(fmt.Sprintf("滑动验证码背景图片加载成功，共 %d 张", len(imgs)))

	if len(imgs) == 0 {
		g.Warn("滑动验证码没有加载到任何背景图片，验证码生成可能会失败")
	}

	return imgs
}

// loadSlideGraphs 加载滑动验证码拼图块图片
func loadSlideGraphs() []*slide.GraphImage {
	graphs, err := tiles.GetTiles()
	if err != nil {
		g.Error("加载滑动验证码拼图块失败", zap.Error(err))
		return nil
	}

	var newGraphs = make([]*slide.GraphImage, 0, len(graphs))
	for _, graph := range graphs {
		newGraphs = append(newGraphs, &slide.GraphImage{
			OverlayImage: graph.OverlayImage,
			MaskImage:    graph.MaskImage,
			ShadowImage:  graph.ShadowImage,
		})
	}

	g.Info(fmt.Sprintf("滑动验证码拼图块加载成功，共 %d 个", len(newGraphs)))

	if len(newGraphs) == 0 {
		g.Warn("滑动验证码没有加载到任何拼图块，验证码生成可能会失败")
	}

	return newGraphs
}

// GenerateClickCaptcha 生成点选验证码
//
// Parameters:
//   - ctx: 上下文
//   - scene: 验证码使用场景
//
// Returns:
//   - 生成的验证码数据
//   - 错误信息
func (s *captchaService) GenerateClickCaptcha(ctx context.Context, scene consts.CaptchaScene) (*resp.ClickCaptcha, error) {
	captData, err := clickCapt.Generate()
	if err != nil {
		return nil, errs.WrapError(err, "生成点选验证码失败", string(errs.CaptchaGenerateFailed))
	}

	dotData := captData.GetData()
	if dotData == nil {
		return nil, errs.WrapError(fmt.Errorf("验证码数据为空"), "生成点选验证码失败", string(errs.CaptchaGenerateFailed))
	}

	captchaKey, err := g.NextIDStr()
	if err != nil {
		return nil, errs.WrapError(err, "生成验证码键失败", string(errs.CaptchaGenerateFailed))
	}

	dotBytes, err := json.Marshal(dotData)
	if err != nil {
		return nil, errs.WrapError(err, "序列化验证码数据失败", string(errs.CaptchaGenerateFailed))
	}

	cacheKey := string(cache.Captcha) + "click:" + string(scene) + ":" + captchaKey
	if err := g.Redis.Set(ctx, cacheKey, dotBytes, 5*time.Minute).Err(); err != nil {
		return nil, errs.WrapError(err, "缓存验证码数据失败", string(errs.CaptchaGenerateFailed))
	}

	masterBase64, err := captData.GetMasterImage().ToBase64()
	if err != nil {
		return nil, errs.WrapError(err, "获取验证码主图失败", string(errs.CaptchaGenerateFailed))
	}

	thumbBase64, err := captData.GetThumbImage().ToBase64()
	if err != nil {
		return nil, errs.WrapError(err, "获取验证码缩略图失败", string(errs.CaptchaGenerateFailed))
	}

	return &resp.ClickCaptcha{
		CaptchaKey:  captchaKey,
		MasterImage: masterBase64,
		ThumbImage:  thumbBase64,
	}, nil
}

// VerifyClickCaptcha 验证点选验证码
//
// Parameters:
//   - appCtx: 应用上下文
//   - r: 验证请求
//
// Returns:
//   - 验证结果
//   - 错误信息
func (s *captchaService) VerifyClickCaptcha(appCtx *g.AppCtx, r *req.VerifyClickCaptcha) (*resp.CaptchaVerifyResult, error) {
	cacheKey := string(cache.Captcha) + "click:" + r.Scene + ":" + r.CaptchaKey
	dotBytes, err := g.Redis.Get(appCtx.C, cacheKey).Bytes()
	if err != nil {
		g.Error("验证码已过期", zap.String("captcha_key", r.CaptchaKey), zap.String("scene", r.Scene), zap.Error(err))
		return &resp.CaptchaVerifyResult{
			Success: false,
			Token:   "",
		}, nil
	}

	var dotData map[int]*click.Dot
	if err := json.Unmarshal(dotBytes, &dotData); err != nil {
		g.Error("验证码数据解析失败", zap.String("captcha_key", r.CaptchaKey), zap.String("scene", r.Scene), zap.Error(err))
		return &resp.CaptchaVerifyResult{
			Success: false,
			Token:   "",
		}, nil
	}

	userDots, err := parseClickDots(r.Dots)
	if err != nil {
		g.Error("验证码坐标格式错误", zap.String("captcha_key", r.CaptchaKey), zap.String("scene", r.Scene), zap.Error(err))
		return &resp.CaptchaVerifyResult{
			Success: false,
			Token:   "",
		}, nil
	}

	for i, ud := range userDots {
		dot, ok := dotData[i]
		if !ok {
			g.Error("验证码验证失败：缺少坐标", zap.Int("index", i), zap.Any("user_dots", userDots))
			return &resp.CaptchaVerifyResult{
				Success: false,
				Token:   "",
			}, nil
		}
		if !click.Validate(ud.X, ud.Y, dot.X, dot.Y, dot.Width, dot.Height, 5) {
			g.Error("验证码验证失败：坐标不匹配", zap.Int("index", i), zap.Any("user_dots", userDots), zap.Any("expected_dot", dot))
			return &resp.CaptchaVerifyResult{
				Success: false,
				Token:   "",
			}, nil
		}
	}

	g.Redis.Del(appCtx.C, cacheKey)

	captchaToken, err := g.NextIDStr()
	if err != nil {
		g.Error("生成验证码 Token 失败", zap.Error(err))
		return nil, errs.WrapError(err, "生成验证码 Token 失败", string(errs.CaptchaGenerateFailed))
	}

	tokenCacheKey := string(cache.Captcha) + "token:" + r.Scene + ":" + captchaToken
	if err := g.Redis.Set(appCtx.C, tokenCacheKey, "1", 1*time.Minute).Err(); err != nil {
		g.Error("缓存验证码 Token 失败", zap.String("token", captchaToken), zap.String("scene", r.Scene), zap.Error(err))
		return nil, errs.WrapError(err, "缓存验证码 Token 失败", string(errs.CaptchaGenerateFailed))
	}

	return &resp.CaptchaVerifyResult{
		Success: true,
		Token:   captchaToken,
	}, nil
}

// parseClickDots 解析点选验证码坐标 "x1,y1;x2,y2;..."
func parseClickDots(dotsStr string) ([]struct{ X, Y int }, error) {
	parts := strings.Split(dotsStr, ";")
	result := make([]struct{ X, Y int }, 0, len(parts))
	for _, part := range parts {
		coords := strings.Split(part, ",")
		if len(coords) != 2 {
			return nil, fmt.Errorf("invalid dot format: %s", part)
		}
		x, err := strconv.Atoi(strings.TrimSpace(coords[0]))
		if err != nil {
			return nil, fmt.Errorf("invalid x coordinate: %s", coords[0])
		}
		y, err := strconv.Atoi(strings.TrimSpace(coords[1]))
		if err != nil {
			return nil, fmt.Errorf("invalid y coordinate: %s", coords[1])
		}
		result = append(result, struct{ X, Y int }{X: x, Y: y})
	}
	return result, nil
}

// VerifyCaptchaToken 验证验证码 Token
//
// Parameters:
//   - appCtx: 应用上下文
//   - captchaToken: 验证码 Token
//   - scene: 验证码使用场景
//
// Returns:
//   - 错误信息
func (s *captchaService) VerifyCaptchaToken(appCtx *g.AppCtx, captchaToken string, scene consts.CaptchaScene) error {
	tokenCacheKey := string(cache.Captcha) + "token:" + string(scene) + ":" + captchaToken
	_, err := g.Redis.Get(appCtx.C, tokenCacheKey).Result()
	if err != nil {
		return errs.WrapError(err, "验证码已过期或无效", string(errs.CaptchaVerifyFailed))
	}

	g.Redis.Del(appCtx.C, tokenCacheKey)

	return nil
}

// GenerateSlideCaptcha 生成滑动验证码
//
// Parameters:
//   - ctx: 上下文
//   - scene: 验证码使用场景
//
// Returns:
//   - 生成的验证码数据
//   - 错误信息
func (s *captchaService) GenerateSlideCaptcha(ctx context.Context, scene consts.CaptchaScene) (*resp.SlideCaptcha, error) {
	captData, err := slideCapt.Generate()
	if err != nil {
		return nil, errs.WrapError(err, "生成滑动验证码失败", string(errs.CaptchaGenerateFailed))
	}

	captchaKey, err := g.NextIDStr()
	if err != nil {
		return nil, errs.WrapError(err, "生成验证码键失败", string(errs.CaptchaGenerateFailed))
	}

	verifyData := captData.GetData()
	verifyBytes, err := json.Marshal(verifyData)
	if err != nil {
		return nil, errs.WrapError(err, "序列化验证数据失败", string(errs.CaptchaGenerateFailed))
	}

	cacheKey := string(cache.Captcha) + "slide:" + string(scene) + ":" + captchaKey
	if err := g.Redis.Set(ctx, cacheKey, verifyBytes, 5*time.Minute).Err(); err != nil {
		return nil, errs.WrapError(err, "缓存验证数据失败", string(errs.CaptchaGenerateFailed))
	}

	bgBase64, err := captData.GetMasterImage().ToBase64()
	if err != nil {
		return nil, errs.WrapError(err, "获取背景图失败", string(errs.CaptchaGenerateFailed))
	}

	tileBase64, err := captData.GetTileImage().ToBase64()
	if err != nil {
		return nil, errs.WrapError(err, "获取滑块图失败", string(errs.CaptchaGenerateFailed))
	}

	return &resp.SlideCaptcha{
		CaptchaKey:  captchaKey,
		ImageBase64: bgBase64,
		TileBase64:  tileBase64,
		TileWidth:   verifyData.Width,
		TileHeight:  verifyData.Height,
		TileX:       verifyData.DX,
		TileY:       verifyData.DY,
	}, nil
}

// VerifySlideCaptcha 验证滑动验证码
//
// Parameters:
//   - appCtx: 应用上下文
//   - r: 验证请求
//
// Returns:
//   - 验证结果
//   - 错误信息
func (s *captchaService) VerifySlideCaptcha(appCtx *g.AppCtx, r *req.VerifySlideCaptcha) (*resp.CaptchaVerifyResult, error) {
	cacheKey := string(cache.Captcha) + "slide:" + r.Scene + ":" + r.CaptchaKey
	verifyBytes, err := g.Redis.Get(appCtx.C, cacheKey).Bytes()
	if err != nil {
		g.Error("滑动验证码已过期", zap.String("captcha_key", r.CaptchaKey), zap.String("scene", r.Scene), zap.Error(err))
		return &resp.CaptchaVerifyResult{
			Success: false,
			Token:   "",
		}, nil
	}

	var blockData slide.Block
	if err := json.Unmarshal(verifyBytes, &blockData); err != nil {
		g.Error("滑动验证码数据解析失败", zap.String("captcha_key", r.CaptchaKey), zap.String("scene", r.Scene), zap.Error(err))
		return &resp.CaptchaVerifyResult{
			Success: false,
			Token:   "",
		}, nil
	}

	ok := slide.Validate(r.SlideDistance, 0, blockData.DX, blockData.DY, 10)
	if !ok {
		g.Warn("滑动验证失败",
			zap.Int("user_x", r.SlideDistance),
			zap.Int("target_x", blockData.DX),
			zap.Int("target_y", blockData.DY),
			zap.String("scene", r.Scene))
		return &resp.CaptchaVerifyResult{
			Success: false,
			Token:   "",
		}, nil
	}

	g.Redis.Del(appCtx.C, cacheKey)

	captchaToken, err := g.NextIDStr()
	if err != nil {
		g.Error("生成 Token 失败", zap.Error(err))
		return nil, errs.WrapError(err, "生成 Token 失败", string(errs.CaptchaGenerateFailed))
	}

	tokenCacheKey := string(cache.Captcha) + "token:" + r.Scene + ":" + captchaToken
	if err := g.Redis.Set(appCtx.C, tokenCacheKey, "1", 1*time.Minute).Err(); err != nil {
		g.Error("缓存 Token 失败", zap.String("token", captchaToken), zap.String("scene", r.Scene), zap.Error(err))
		return nil, errs.WrapError(err, "缓存 Token 失败", string(errs.CaptchaGenerateFailed))
	}

	return &resp.CaptchaVerifyResult{
		Success: true,
		Token:   captchaToken,
	}, nil
}
