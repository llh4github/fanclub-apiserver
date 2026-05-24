package utils

import (
	"testing"
)

func TestProcessLinks(t *testing.T) {
	tests := []struct {
		name     string
		input    string
		expected string
	}{
		{
			name:     "外部链接被移除",
			input:    `<p>这是一个<a href="https://example.com">外部链接</a></p>`,
			expected: `<p>这是一个外部链接</p>`,
		},
		{
			name:     "多个外部链接都被移除",
			input:    `<p><a href="http://unsafe.com">链接1</a>和<a href="https://malicious.com">链接2</a></p>`,
			expected: `<p>链接1和链接2</p>`,
		},
		{
			name:     "带target属性的链接",
			input:    `<a href="https://bad.com" target="_blank">跳转链接</a>`,
			expected: `跳转链接`,
		},
		{
			name:     "没有链接的HTML保持不变",
			input:    `<p>纯文本内容</p>`,
			expected: `<p>纯文本内容</p>`,
		},
		{
			name:     "空链接文本",
			input:    `<a href="https://test.com"></a>`,
			expected: ``,
		},
		{
			name:     "likofan域名链接被保留",
			input:    `<p><a href="https://www.likofan.com/page">likofan链接</a></p>`,
			expected: `<p><a href="https://www.likofan.com/page">likofan链接</a></p>`,
		},
		{
			name:     "imglikofan域名图片被保留并添加预览钩子",
			input:    `<p><img src="https://img.likofan.com/pic.jpg"></p>`,
			expected: `<p><img src="https://img.likofan.com/pic.jpg-thumbnail" data-preview-src="https://img.likofan.com/pic.jpg-webp"></p>`,
		},
		{
			name:     "非likofan域名图片被删除",
			input:    `<p><img src="https://other.com/pic.jpg"></p>`,
			expected: `<p></p>`,
		},
		{
			name:     "likofan子域名图片被保留",
			input:    `<p><img src="https://cdn.img.likofan.com/image.png"></p>`,
			expected: `<p><img src="https://cdn.img.likofan.com/image.png-thumbnail" data-preview-src="https://cdn.img.likofan.com/image.png-webp"></p>`,
		},
		{
			name:     "likofan链接和外部链接混合",
			input:    `<p><a href="https://www.likofan.com">保留</a>和<a href="https://bad.com">移除</a></p>`,
			expected: `<p><a href="https://www.likofan.com">保留</a>和移除</p>`,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			result := processLinks(tt.input)
			if result != tt.expected {
				t.Errorf("processLinks() = %q, want %q", result, tt.expected)
			}
		})
	}
}

func TestIsLikofanDomain(t *testing.T) {
	tests := []struct {
		href     string
		expected bool
	}{
		{"https://www.likofan.com/page", true},
		{"http://img.likofan.com/pic.jpg", true},
		{"https://cdn.likofan.com/image.png", true},
		{"https://www.likofan.com.cn/page", true},
		{"https://example.com", false},
		{"https://likofan.com.fake.com", false},
		{"/relative/path", false},
		{"javascript:alert(1)", false},
	}

	for _, tt := range tests {
		t.Run(tt.href, func(t *testing.T) {
			result := isLikofanDomain(tt.href)
			if result != tt.expected {
				t.Errorf("isLikofanDomain(%q) = %v, want %v", tt.href, result, tt.expected)
			}
		})
	}
}
