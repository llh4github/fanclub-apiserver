package utils

import (
	"regexp"
	"strings"
	"unicode/utf8"

	"github.com/microcosm-cc/bluemonday"
	"github.com/yuin/goldmark"
	"github.com/yuin/goldmark/extension"
	"github.com/yuin/goldmark/parser"
	"github.com/yuin/goldmark/renderer/html"
)

const (
	// MinContentLength 最小内容字数
	MinContentLength = 10
	// MaxContentLength 最大内容字数
	MaxContentLength = 800
	// SummaryLength 摘要长度
	SummaryLength = 100
	// MaxImageCount 最大允许的图片数量
	MaxImageCount = 5
)

// MarkdownResult Markdown 处理结果
type MarkdownResult struct {
	// HTMLContent HTML 格式的内容
	HTMLContent string
	// PlainText 纯文本内容
	PlainText string
	// Summary 摘要（前100字）
	Summary string
	// ImageURLs 允许域名下的图片链接列表
	ImageURLs []string
}

// markdown Markdown 解析器（全局单例）
var markdown = goldmark.New(
	goldmark.WithParserOptions(
		parser.WithAutoHeadingID(),
	),
	goldmark.WithRendererOptions(
		html.WithHardWraps(),
		html.WithXHTML(),
	),
	goldmark.WithExtensions(
		extension.GFM,           // GitHub 风格 Markdown（表格、任务列表、删除线等）
		extension.Strikethrough, // 删除线
	),
)

// sanitzier HTML 清理器（只允许安全的标签和属性）
var sanitzier = bluemonday.UGCPolicy()

func init() {
	sanitzier.AllowAttrs("class").Matching(regexp.MustCompile(`^[\w\s-]+$`)).OnElements("h1", "h2", "h3", "h4", "h5", "h6", "p", "div", "span", "ul", "ol", "li", "blockquote", "pre", "code")
	sanitzier.AllowAttrs("type").Matching(regexp.MustCompile(`^(checkbox|radio)$`)).OnElements("input")
	sanitzier.AllowAttrs("disabled", "checked").OnElements("input")
	sanitzier.AllowAttrs("class").Matching(regexp.MustCompile(`^[\w\s-]+$`)).OnElements("li")
	sanitzier.AllowAttrs("src", "alt", "data-preview-src").OnElements("img")
}

// ProcessMarkdown 处理 Markdown 内容
//
// 处理流程：
//  1. Markdown 转 HTML（使用 Goldmark）
//  2. HTML 清理和安全过滤（使用 Bluemonday）
//  3. 提取纯文本（去除 HTML 和 Markdown 标记）
//  4. 验证纯文本长度是否在限制范围内（不小于最小长度，不超过最大长度）
//
// Parameters:
//   - markdownContent: Markdown 格式的内容
//   - minLength: 最小字数限制
//   - maxLength: 最大字数限制
//
// Returns:
//   - *MarkdownResult: 处理结果
//   - error: 错误信息，如果字数超过限制或不足最小长度则返回错误
func ProcessMarkdown(markdownContent string, minLength, maxLength int) (*MarkdownResult, error) {
	// 1. Markdown 转 HTML（使用 Goldmark）
	var buf strings.Builder
	if err := markdown.Convert([]byte(markdownContent), &buf); err != nil {
		return nil, err
	}
	rawHTML := buf.String()

	// 2. HTML 清理和安全过滤（使用 Bluemonday）
	safeHTML := sanitzier.Sanitize(rawHTML)

	// 3. 处理图片链接（在清理之后处理，确保属性正确）
	safeHTML = processImageLinks(safeHTML)
	safeHTML = processAnchorLinks(safeHTML)

	// 4. 提取允许域名下的图片链接并校验数量
	imageURLs := extractImageURLs(safeHTML)
	if len(imageURLs) > MaxImageCount {
		return nil, ErrTooManyImages
	}

	// 5. 提取纯文本
	plainText := ExtractPlainText(safeHTML)

	// 6. 验证字数
	if utf8.RuneCountInString(plainText) < minLength {
		return nil, ErrContentTooShort
	}
	if utf8.RuneCountInString(plainText) > maxLength {
		return nil, ErrContentTooLong
	}

	// 7. 生成摘要
	summary := GenerateSummary(plainText, SummaryLength)

	return &MarkdownResult{
		HTMLContent: safeHTML,
		PlainText:   plainText,
		Summary:     summary,
		ImageURLs:   imageURLs,
	}, nil
}

// ExtractPlainText 提取纯文本
//
// 去除所有 HTML 标签和 Markdown 残留标记
//
// Parameters:
//   - htmlContent: HTML 格式的内容
//
// Returns:
//   - 纯文本内容
func ExtractPlainText(htmlContent string) string {
	// 去除 HTML 标签
	htmlTagRegex := regexp.MustCompile(`<[^>]*>`)
	plainText := htmlTagRegex.ReplaceAllString(htmlContent, "")

	// 替换常见的 HTML 实体
	htmlEntityMap := map[string]string{
		"&lt;":     "<",
		"&gt;":     ">",
		"&amp;":    "&",
		"&quot;":   "\"",
		"&nbsp;":   " ",
		"&#39;":    "'",
		"&apos;":   "'",
		"&mdash;":  "—",
		"&ndash;":  "-",
		"&hellip;": "...",
	}

	for entity, char := range htmlEntityMap {
		plainText = strings.ReplaceAll(plainText, entity, char)
	}

	// 去除其他 HTML 实体
	htmlEntityRegex := regexp.MustCompile(`&[a-zA-Z]+;|&#\d+;`)
	plainText = htmlEntityRegex.ReplaceAllString(plainText, "")

	// 去除 Markdown 残留标记
	mdResidualMarkers := []string{
		`^#{1,6}\s*`,              // 标题标记
		`\*\*`,                    // 粗体
		`__`,                      // 粗体
		`\*`,                      // 斜体
		`_`,                       // 斜体
		`~~`,                      // 删除线
		"`",                       // 行内代码
		"```.*?```",               // 代码块
		`\[([^\]]+)\]\([^\)]+\)`,  // 链接
		`!\[([^\]]*)\]\([^\)]+\)`, // 图片
		`^\s*[-*+]\s*`,            // 无序列表
		`^\s*\d+\.\s*`,            // 有序列表
		`^\s*>\s*`,                // 引用
		`\n{3,}`,                  // 多个换行
	}

	for _, pattern := range mdResidualMarkers {
		re := regexp.MustCompile(`(?m)` + pattern)
		plainText = re.ReplaceAllString(plainText, "")
	}

	// 去除多余空白字符
	spaceRegex := regexp.MustCompile(`\s{2,}`)
	plainText = spaceRegex.ReplaceAllString(plainText, " ")

	// 去除首尾空白
	plainText = strings.TrimSpace(plainText)

	return plainText
}

// GenerateSummary 生成摘要
//
// 取纯文本的前N个字作为摘要
//
// Parameters:
//   - plainText: 纯文本内容
//   - length: 摘要长度
//
// Returns:
//   - 摘要文本
func GenerateSummary(plainText string, length int) string {
	runes := []rune(plainText)
	if len(runes) <= length {
		return plainText
	}
	return string(runes[:length]) + "..."
}

// processLinks 处理 Markdown 转换后的链接和图片
//
// 处理策略：
//  1. 图片链接（<img src="...">）：
//     - 保留 *.likofan 域名，并添加预览钩子
//     - 其他域名直接删除
//  2. 普通链接（<a href="...">text</a>）：
//     - 保留 *.likofan 域名的链接
//     - 其他外部链接转换为纯文本
func processLinks(htmlContent string) string {
	htmlContent = processImageLinks(htmlContent)
	htmlContent = processAnchorLinks(htmlContent)
	return htmlContent
}

// extractImageURLs 提取允许域名下的图片链接
//
// Parameters:
//   - htmlContent: HTML 内容
//
// Returns:
//   - 允许域名下的图片链接列表
func extractImageURLs(htmlContent string) []string {
	var urls []string
	imgRegex := regexp.MustCompile(`<img\s+[^>]*src\s*=\s*["']([^"']*)["'][^>]*>`)
	matches := imgRegex.FindAllStringSubmatch(htmlContent, -1)
	for _, match := range matches {
		if len(match) >= 2 && isLikofanDomain(match[1]) {
			urls = append(urls, match[1])
		}
	}
	return urls
}

// processImageLinks 处理图片链接
//
// 处理策略：
//   - 仅处理 likofan 域名的图片
//   - src 链接：若不以 -thumbnail 结尾则添加
//   - data-preview-src 链接：若不以 -webp 结尾则添加
func processImageLinks(htmlContent string) string {
	imgRegex := regexp.MustCompile(`<img\s+([^>]*)src\s*=\s*["']([^"']*)["']([^>]*)>`)
	return imgRegex.ReplaceAllStringFunc(htmlContent, func(match string) string {
		srcRegex := regexp.MustCompile(`src\s*=\s*["']([^"']*)["']`)
		srcMatch := srcRegex.FindStringSubmatch(match)
		if len(srcMatch) < 2 {
			return ""
		}
		src := srcMatch[1]
		if !isLikofanDomain(src) {
			return ""
		}

		thumbnailSrc := ensureSuffix(src, "-thumbnail")
		webpPreview := ensureSuffix(src, "-webp")

		return `<img src="` + thumbnailSrc + `" data-preview-src="` + webpPreview + `">`
	})
}

// ensureSuffix 确保字符串以指定后缀结尾，若已存在则保持不变
func ensureSuffix(s, suffix string) string {
	if strings.HasSuffix(s, suffix) {
		return s
	}
	return s + suffix
}

// processAnchorLinks 处理普通链接
func processAnchorLinks(htmlContent string) string {
	linkRegex := regexp.MustCompile(`<a\s+[^>]*href\s*=\s*["']([^"']*)["'][^>]*>([^<]*)</a>`)
	return linkRegex.ReplaceAllStringFunc(htmlContent, func(match string) string {
		hrefRegex := regexp.MustCompile(`href\s*=\s*["']([^"']*)["']`)
		hrefMatch := hrefRegex.FindStringSubmatch(match)
		if len(hrefMatch) < 2 {
			return extractLinkText(match)
		}
		href := hrefMatch[1]
		if isLikofanDomain(href) {
			return match
		}
		return extractLinkText(match)
	})
}

// isLikofanDomain 检查链接是否为 likofan 域名
func isLikofanDomain(href string) bool {
	if !strings.HasPrefix(href, "http://") && !strings.HasPrefix(href, "https://") {
		return false
	}
	return strings.Contains(href, ".likofan")
}

// extractLinkText 提取链接文本
func extractLinkText(htmlTag string) string {
	textRegex := regexp.MustCompile(`>([^<]*)</a>`)
	matches := textRegex.FindStringSubmatch(htmlTag)
	if len(matches) >= 2 {
		return matches[1]
	}
	return ""
}
