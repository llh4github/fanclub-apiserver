package utils

import (
	"errors"
	"fmt"
	"strings"
	"testing"
)

func TestProcessMarkdown(t *testing.T) {
	tests := []struct {
		name    string
		input   string
		maxLen  int
		wantErr bool
		checkFn func(*MarkdownResult) error
	}{
		{
			name:    "标题",
			input:   "# 一级标题\n## 二级标题\n### 三级标题",
			maxLen:  800,
			wantErr: false,
			checkFn: func(r *MarkdownResult) error {
				if r.HTMLContent == "" {
					return fmt.Errorf("HTMLContent is empty")
				}
				fmt.Printf("标题 HTML: %s\n", r.HTMLContent)
				return nil
			},
		},
		{
			name:    "粗体和斜体",
			input:   "这是**粗体**和*斜体*，还有***粗体斜体***",
			maxLen:  800,
			wantErr: false,
			checkFn: func(r *MarkdownResult) error {
				fmt.Printf("粗体斜体 HTML: %s\n", r.HTMLContent)
				return nil
			},
		},
		{
			name:    "链接",
			input:   "这是一个[链接](https://example.com)，指向示例网站",
			maxLen:  800,
			wantErr: false,
			checkFn: func(r *MarkdownResult) error {
				fmt.Printf("链接 HTML: %s\n", r.HTMLContent)
				fmt.Printf("链接 PlainText: %s\n", r.PlainText)
				return nil
			},
		},
		{
			name:    "图片",
			input:   "这是一个图片 ![描述](https://img.likofan.com/pic.jpg)",
			maxLen:  800,
			wantErr: false,
			checkFn: func(r *MarkdownResult) error {
				fmt.Printf("图片 HTML: %s\n", r.HTMLContent)
				return nil
			},
		},
		{
			name:    "无序列表",
			input:   "- 列表项1\n- 列表项2\n- 列表项3",
			maxLen:  800,
			wantErr: false,
			checkFn: func(r *MarkdownResult) error {
				fmt.Printf("列表 HTML: %s\n", r.HTMLContent)
				return nil
			},
		},
		{
			name:    "有序列表",
			input:   "1. 第一项\n2. 第二项\n3. 第三项",
			maxLen:  800,
			wantErr: false,
			checkFn: func(r *MarkdownResult) error {
				fmt.Printf("有序列表 HTML: %s\n", r.HTMLContent)
				return nil
			},
		},
		{
			name:    "引用",
			input:   "> 这是一段引用\n> 多行引用",
			maxLen:  800,
			wantErr: false,
			checkFn: func(r *MarkdownResult) error {
				fmt.Printf("引用 HTML: %s\n", r.HTMLContent)
				return nil
			},
		},
		{
			name:    "代码块",
			input:   "```go\nfunc main() {\n    fmt.Println(\"Hello\")\n}\n```",
			maxLen:  800,
			wantErr: false,
			checkFn: func(r *MarkdownResult) error {
				fmt.Printf("代码块 HTML: %s\n", r.HTMLContent)
				return nil
			},
		},
		{
			name:    "行内代码",
			input:   "使用 `fmt.Println()` 打印内容",
			maxLen:  800,
			wantErr: false,
			checkFn: func(r *MarkdownResult) error {
				fmt.Printf("行内代码 HTML: %s\n", r.HTMLContent)
				return nil
			},
		},
		{
			name:    "删除线",
			input:   "这是~~删除线~~文本",
			maxLen:  800,
			wantErr: false,
			checkFn: func(r *MarkdownResult) error {
				fmt.Printf("删除线 HTML: %s\n", r.HTMLContent)
				return nil
			},
		},
		{
			name:    "表格",
			input:   "| 列1 | 列2 | 列3 |\n|---|---|---|\n| 单元格1 | 单元格2 | 单元格3 |",
			maxLen:  800,
			wantErr: false,
			checkFn: func(r *MarkdownResult) error {
				fmt.Printf("表格 HTML: %s\n", r.HTMLContent)
				return nil
			},
		},
		{
			name:    "任务列表",
			input:   "- [x] 已完成任务\n- [ ] 未完成任务",
			maxLen:  800,
			wantErr: false,
			checkFn: func(r *MarkdownResult) error {
				fmt.Printf("任务列表 HTML: %s\n", r.HTMLContent)
				return nil
			},
		},
		{
			name:    "水平线",
			input:   "上文\n\n---\n\n下文",
			maxLen:  800,
			wantErr: false,
			checkFn: func(r *MarkdownResult) error {
				fmt.Printf("水平线 HTML: %s\n", r.HTMLContent)
				return nil
			},
		},
		{
			name:    "混合内容",
			input:   "# 标题\n\n这是**粗体**，*斜体*，[链接](https://example.com)\n\n- 列表项1\n- 列表项2\n\n> 引用内容",
			maxLen:  800,
			wantErr: false,
			checkFn: func(r *MarkdownResult) error {
				fmt.Printf("=== 混合内容 ===\n")
				fmt.Printf("HTML: %s\n", r.HTMLContent)
				fmt.Printf("PlainText: %s\n", r.PlainText)
				fmt.Printf("Summary: %s\n", r.Summary)
				fmt.Printf("=== 结束 ===\n")
				return nil
			},
		},
		{
			name:    "自定义属性扩展",
			input:   "# 标题\n\n段落内容\n{.custom-paragraph}\n\n> 引用内容\n{.custom-quote}",
			maxLen:  800,
			wantErr: false,
			checkFn: func(r *MarkdownResult) error {
				fmt.Printf("=== 自定义属性 ===\n")
				fmt.Printf("HTML: %s\n", r.HTMLContent)
				fmt.Printf("=== 结束 ===\n")
				return nil
			},
		},
		{
			name:    "内容超长错误",
			input:   "这是一段很长的内容，重复repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat repeat",
			maxLen:  50,
			wantErr: true,
			checkFn: func(r *MarkdownResult) error {
				return nil
			},
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result, err := ProcessMarkdown(tt.input, 0, tt.maxLen)
			if (err != nil) != tt.wantErr {
				t.Errorf("ProcessMarkdown() error = %v, wantErr %v", err, tt.wantErr)
				return
			}
			if !tt.wantErr && tt.checkFn != nil {
				if err := tt.checkFn(result); err != nil {
					t.Errorf("checkFn failed: %v", err)
				}
			}
		})
	}
}

func TestExtractPlainText(t *testing.T) {
	htmlSamples := []struct {
		name     string
		input    string
		expected string
	}{
		{
			name:     "简单标签",
			input:    "<p>Hello World</p>",
			expected: "Hello World",
		},
		{
			name:     "嵌套标签",
			input:    "<div><p>Nested <strong>text</strong></p></div>",
			expected: "Nested text",
		},
		{
			name:     "HTML实体",
			input:    "&lt;div&gt;&amp;&quot;&nbsp;内容&#39;",
			expected: "<>\" 内容'",
		},
	}

	for _, tt := range htmlSamples {
		t.Run(tt.name, func(t *testing.T) {
			result := ExtractPlainText(tt.input)
			fmt.Printf("输入: %s\n", tt.input)
			fmt.Printf("输出: %s\n", result)
		})
	}
}

func TestGenerateSummary(t *testing.T) {
	tests := []struct {
		name     string
		text     string
		length   int
		expected string
	}{
		{
			name:     "短文本",
			text:     "Hello",
			length:   10,
			expected: "Hello",
		},
		{
			name:     "正好长度",
			text:     "Hello",
			length:   5,
			expected: "Hello",
		},
		{
			name:     "截断",
			text:     "Hello World",
			length:   5,
			expected: "Hello...",
		},
		{
			name:     "中文截断",
			text:     "你好世界",
			length:   2,
			expected: "你好...",
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result := GenerateSummary(tt.text, tt.length)
			if result != tt.expected {
				t.Errorf("GenerateSummary() = %v, expected %v", result, tt.expected)
			}
		})
	}
}

func TestExtractImageURLs(t *testing.T) {
	tests := []struct {
		name     string
		input    string
		expected []string
	}{
		{
			name:     "提取likofan域名图片",
			input:    `<p><img src="https://img.likofan.com/pic.jpg"></p>`,
			expected: []string{"https://img.likofan.com/pic.jpg"},
		},
		{
			name:     "提取多个likofan域名图片",
			input:    `<p><img src="https://img.likofan.com/1.jpg"><img src="https://cdn.likofan.com/2.jpg"></p>`,
			expected: []string{"https://img.likofan.com/1.jpg", "https://cdn.likofan.com/2.jpg"},
		},
		{
			name:     "过滤非likofan域名图片",
			input:    `<p><img src="https://img.likofan.com/pic.jpg"><img src="https://other.com/pic.jpg"></p>`,
			expected: []string{"https://img.likofan.com/pic.jpg"},
		},
		{
			name:     "无图片",
			input:    `<p>纯文本</p>`,
			expected: []string{},
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result := extractImageURLs(tt.input)
			if len(result) != len(tt.expected) {
				t.Errorf("extractImageURLs() got %d items, want %d", len(result), len(tt.expected))
				return
			}
			for i, v := range result {
				if v != tt.expected[i] {
					t.Errorf("extractImageURLs()[%d] = %v, want %v", i, v, tt.expected[i])
				}
			}
		})
	}
}

func TestProcessMarkdownWithImages(t *testing.T) {
	tests := []struct {
		name     string
		input    string
		maxLen   int
		wantErr  bool
		errType  error
		wantImgs int
	}{
		{
			name:     "正常提取likofan图片",
			input:    "图片 ![img](https://img.likofan.com/pic.jpg)",
			maxLen:   800,
			wantErr:  false,
			wantImgs: 1,
		},
		{
			name:     "提取多个likofan图片",
			input:    "图片1 ![img1](https://img.likofan.com/1.jpg) 图片2 ![img2](https://img.likofan.com/2.jpg)",
			maxLen:   800,
			wantErr:  false,
			wantImgs: 2,
		},
		{
			name:     "超过5张图片报错",
			input:    "图片 ![img](https://img.likofan.com/1.jpg) ![img](https://img.likofan.com/2.jpg) ![img](https://img.likofan.com/3.jpg) ![img](https://img.likofan.com/4.jpg) ![img](https://img.likofan.com/5.jpg) ![img](https://img.likofan.com/6.jpg)",
			maxLen:   800,
			wantErr:  true,
			errType:  ErrTooManyImages,
			wantImgs: 0,
		},
		{
			name:     "刚好5张图片不报错",
			input:    "图片 ![img](https://img.likofan.com/1.jpg) ![img](https://img.likofan.com/2.jpg) ![img](https://img.likofan.com/3.jpg) ![img](https://img.likofan.com/4.jpg) ![img](https://img.likofan.com/5.jpg)",
			maxLen:   800,
			wantErr:  false,
			wantImgs: 5,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result, err := ProcessMarkdown(tt.input, 0, tt.maxLen)
			if (err != nil) != tt.wantErr {
				t.Errorf("ProcessMarkdown() error = %v, wantErr %v", err, tt.wantErr)
				return
			}
			if tt.wantErr && !errors.Is(err, tt.errType) {
				t.Errorf("ProcessMarkdown() error = %v, want %v", err, tt.errType)
				return
			}
			if !tt.wantErr {
				if len(result.ImageURLs) != tt.wantImgs {
					t.Errorf("ProcessMarkdown() got %d images, want %d", len(result.ImageURLs), tt.wantImgs)
				}
			}
		})
	}
}

func TestProcessImageLinks(t *testing.T) {
	tests := []struct {
		name        string
		input       string
		wantSrc     string
		wantPreview string
	}{
		{
			name:        "添加-thumbnail和-webp后缀",
			input:       `<img src="https://img.likofan.com/pic.jpg">`,
			wantSrc:     `src="https://img.likofan.com/pic.jpg-thumbnail"`,
			wantPreview: `data-preview-src="https://img.likofan.com/pic.jpg-webp"`,
		},
		{
			name:        "已有-thumbnail后缀保持不变",
			input:       `<img src="https://img.likofan.com/pic.jpg-thumbnail">`,
			wantSrc:     `src="https://img.likofan.com/pic.jpg-thumbnail"`,
			wantPreview: `data-preview-src="https://img.likofan.com/pic.jpg-thumbnail-webp"`,
		},
		{
			name:        "已有-webp后缀保持不变",
			input:       `<img src="https://img.likofan.com/pic.jpg-webp">`,
			wantSrc:     `src="https://img.likofan.com/pic.jpg-webp-thumbnail"`,
			wantPreview: `data-preview-src="https://img.likofan.com/pic.jpg-webp"`,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result := processImageLinks(tt.input)
			if !strings.Contains(result, tt.wantSrc) {
				t.Errorf("result 不含 src 后缀, want %s, got %s", tt.wantSrc, result)
			}
			if !strings.Contains(result, tt.wantPreview) {
				t.Errorf("result 不含 preview 后缀, want %s, got %s", tt.wantPreview, result)
			}
		})
	}
}
