package bilibili

import (
	"crypto/md5"
	"encoding/hex"
	"fmt"
	"net/url"
	"path"
	"sort"
	"strconv"
	"strings"
	"time"
)

// WBI密钥索引表
var wbiKeyIndexTable = [...]int{
	46, 47, 18, 2, 53, 8, 23, 32, 15, 50, 10, 31, 58, 3, 45, 35, 27, 43, 5, 49, 33, 9, 42, 19, 29,
	28, 14, 39, 12, 38, 41, 13,
}

// extractWbiKey 从图片URL中提取WBI密钥
// URL格式如 https://i0.hdslb.com/bfs/wbi/xxx.png，提取xxx部分
func extractWbiKey(imgURL string) string {
	_, filename := path.Split(imgURL)
	if i := strings.LastIndex(filename, "."); i >= 0 {
		return filename[:i]
	}
	return filename
}

// WbiSign 根据WBI图片URL生成签名密钥
func WbiSign(imgURL, subURL string) string {
	concat := extractWbiKey(imgURL) + extractWbiKey(subURL)

	var sb strings.Builder
	for _, idx := range wbiKeyIndexTable {
		if idx < len(concat) {
			sb.WriteByte(concat[idx])
		}
	}
	return sb.String()
}

// BuildWbiQueryString 构建WBI签名的查询字符串
// params 会被按key排序后拼接，追加wts时间戳和w_rid签名
func BuildWbiQueryString(params map[string]string, wbiSignKey string) string {
	// 追加时间戳
	params["wts"] = strconv.FormatInt(time.Now().Unix(), 10)

	// 按key排序
	keys := make([]string, 0, len(params))
	for k := range params {
		keys = append(keys, k)
	}
	sort.Strings(keys)

	// 拼接查询字符串
	var buf strings.Builder
	for i, k := range keys {
		if i > 0 {
			buf.WriteByte('&')
		}
		buf.WriteString(k)
		buf.WriteByte('=')
		buf.WriteString(url.QueryEscape(params[k]))
	}
	queryStr := buf.String()

	// 计算w_rid签名
	hash := md5.Sum([]byte(queryStr + wbiSignKey))
	wRid := hex.EncodeToString(hash[:])

	return queryStr + "&w_rid=" + wRid
}

// BiliApiError B站API业务错误
type BiliApiError struct {
	Code    int
	Message string
}

func (e *BiliApiError) Error() string {
	return fmt.Sprintf("B站API错误 [%d]: %s", e.Code, e.Message)
}
