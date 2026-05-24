package utils

import "regexp"

// bvPattern B站BV号正则匹配模式
var bvPattern = regexp.MustCompile(`BV[a-zA-Z0-9]{10}`)

// ExtractBV 从文本中提取B站BV号，多个取第一个，未找到返回空字符串
func ExtractBV(text string) string {
	return bvPattern.FindString(text)
}
