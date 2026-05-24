package ai

import (
	"context"
	"encoding/json"
	"fmt"
	"strings"
	"time"

	"fanclub-apiserver/cache"
	"fanclub-apiserver/consts"
	"fanclub-apiserver/database/generated"
	"fanclub-apiserver/database/model"
	"fanclub-apiserver/g"
	"fanclub-apiserver/services"

	"github.com/cloudwego/eino/schema"
	"github.com/gocolly/colly/v2"
	"go.uber.org/zap"
	"gorm.io/cli/gorm/typed"
)

const processedImageSetKey = string(cache.AiLayer) + "processed_images"

// RecognizeScheduleFromURL 从网页识别日程信息并保存到数据库
//
// Parameters:
//   - ctx: 上下文
//   - url: B站动态网页URL
//   - bid: 主播B站UID，传入0时使用默认值(consts.Liko.BID)
//
// Returns:
//   - error: 错误信息
func RecognizeScheduleFromURL(ctx context.Context, url string, bid int64) error {
	if ChatModel == nil {
		return fmt.Errorf("ARK client not initialized")
	}

	if url == "" {
		return fmt.Errorf("url is empty")
	}

	anchorBID := consts.Liko.BID
	if bid > 0 {
		anchorBID = bid
	}

	g.Debug("RecognizeScheduleFromURL start", zap.Int64("bid", anchorBID))
	if hasWeekSchedule(ctx, anchorBID) {
		g.Info("Weekly schedule already exists, skipping recognition",
			zap.Int64("bid", anchorBID))
		return nil
	}

	imageURLs, err := ExtractImageURLs(url)
	if err != nil {
		return fmt.Errorf("failed to extract image URLs: %w", err)
	}

	if len(imageURLs) == 0 {
		return fmt.Errorf("no images found in the page")
	}

	newImageURLs, err := cache.CheckImageExists(ctx, processedImageSetKey, imageURLs)
	if err != nil {
		newImageURLs = imageURLs
	}

	if len(newImageURLs) == 0 {
		return fmt.Errorf("all images have been processed")
	}

	items, err := recognizeScheduleFromImages(ctx, newImageURLs)
	if err != nil {
		return err
	}

	if len(items) == 0 {
		return nil
	}

	if err := saveScheduleToDB(items, anchorBID); err != nil {
		return fmt.Errorf("failed to save schedule to database: %w", err)
	}

	services.AnchorLiveSchedule.InvalidateCache(ctx, anchorBID)

	if err := cache.MarkImageProcessed(ctx, processedImageSetKey, newImageURLs); err != nil {
		g.Logger.Warn("Failed to mark images as processed", zap.Error(err))
	}

	return nil
}

func saveScheduleToDB(items []scheduleItem, bid int64) error {
	schedules := make([]model.AnchorLiveSchedule, 0, len(items))

	for _, item := range items {
		schedules = append(schedules, model.AnchorLiveSchedule{
			Bid:       bid,
			Topic:     item.Topic,
			Emoji:     item.Emoji,
			StartTime: item.StartTime,
			EndTime:   item.EndTime,
		})
	}

	if err := g.DB.Create(&schedules).Error; err != nil {
		return err
	}

	return nil
}

func ExtractImageURLs(url string) ([]string, error) {
	var imageURLs []string
	seen := make(map[string]bool)

	c := colly.NewCollector()

	c.OnRequest(func(r *colly.Request) {
		r.Headers.Set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
		r.Headers.Set("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
		r.Headers.Set("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
		r.Headers.Set("Referer", "https://www.bilibili.com/")
	})

	c.OnHTML("img", func(e *colly.HTMLElement) {
		src := e.Request.AbsoluteURL(e.Attr("src"))
		if src == "" {
			return
		}

		if !strings.Contains(src, "bfs/new_dyn/") {
			return
		}
		if !strings.Contains(src, "@") {
			return
		}
		if !strings.Contains(src, ".png") {
			return
		}

		cleanURL := strings.Split(src, "@")[0]

		if !seen[cleanURL] {
			seen[cleanURL] = true
			imageURLs = append(imageURLs, cleanURL)
		}
	})

	err := c.Visit(url)
	if err != nil {
		return nil, err
	}

	return imageURLs, nil
}

func recognizeScheduleFromImages(ctx context.Context, imageURLs []string) ([]scheduleItem, error) {
	currentYear := time.Now().Year()

	prompt := fmt.Sprintf(`分析图片中的日程信息，提取结构化数据。
要求：
1. 如果图片中没有日程信息，则忽略
2. 日程安排为 %d 年的日程安排
3. 提取每个日程的：topic(直播主题)、emoji、start_time(开始时间)、end_time(结束时间)
4. emoji字段为与直播主题匹配的表情符号，如：🎮 游戏、💬 杂谈、🎵 歌回、🎬 观影、🧘 健身、🎨 绘画、🎤 唱歌、📚 读书、🎯 互动、🎲 桌游、🍽️ 美食
5. 时间格式：RFC3339 格式，中国时区（如 "2026-05-04T14:30:00+08:00"）
6. 如果没有结束时间，则设为开始时间后2小时
7. 忽略"休"、"旅途"等没有具体时间的项目
8. 所有图片都没有日程信息，则返回[]`, currentYear)

	inMsgs := []*schema.Message{
		schema.SystemMessage(prompt),
	}

	userMsg := &schema.Message{
		Role:                  schema.User,
		UserInputMultiContent: make([]schema.MessageInputPart, 0, len(imageURLs)),
	}

	for _, imgURL := range imageURLs {
		userMsg.UserInputMultiContent = append(userMsg.UserInputMultiContent, schema.MessageInputPart{
			Type: schema.ChatMessagePartTypeImageURL,
			Image: &schema.MessageInputImage{
				MessagePartCommon: schema.MessagePartCommon{
					URL: &imgURL,
				},
			},
		})
	}

	inMsgs = append(inMsgs, userMsg)

	msg, err := ChatModel.Generate(ctx, inMsgs)
	if err != nil {
		return nil, fmt.Errorf("failed to call ARK API: %w", err)
	}

	result := msg.Content
	if result == "[]" || result == "" {
		return []scheduleItem{}, nil
	}

	g.Info("Received schedule items:", zap.String("result", result))
	var items []scheduleItem
	if err := json.Unmarshal([]byte(result), &items); err != nil {
		return nil, fmt.Errorf("failed to parse schedule items: %w", err)
	}

	return items, nil
}

// hasWeekSchedule 检查当前周是否已有日程数据
//
// Parameters:
//   - ctx: 上下文
//   - bid: 主播B站UID
//
// Returns:
//   - bool: true表示已有数据，false表示没有
func hasWeekSchedule(ctx context.Context, bid int64) bool {
	now := time.Now()
	weekday := now.Weekday()
	daysToMonday := (weekday + 6) % 7

	startOfWeek := now.AddDate(0, 0, -int(daysToMonday))
	startOfWeek = time.Date(startOfWeek.Year(), startOfWeek.Month(), startOfWeek.Day(), 0, 0, 0, 0, startOfWeek.Location())

	endOfWeek := startOfWeek.AddDate(0, 0, 6)
	endOfWeek = time.Date(endOfWeek.Year(), endOfWeek.Month(), endOfWeek.Day(), 23, 59, 59, 999999999, endOfWeek.Location())

	count, err := typed.G[model.AnchorLiveSchedule](g.DB,
		generated.AnchorLiveSchedule.Bid.Eq(bid),
		generated.AnchorLiveSchedule.StartTime.Gte(startOfWeek),
		generated.AnchorLiveSchedule.StartTime.Lte(endOfWeek),
	).Count(ctx, "id")

	if err != nil {
		return false
	}

	return count > 0
}
