package bilibili_auth

import (
	"crypto/rand"
	"crypto/rsa"
	"crypto/sha256"
	"crypto/x509"
	"encoding/hex"
	"encoding/pem"
	"encoding/xml"
	"fmt"
	"io"
	"net/http"
	"strings"
	"time"
)

// UserAgent B站请求User-Agent
const UserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

// Cookies B站登录所需的 cookies
type Cookies struct {
	SESSDATA     string
	BiliJct      string
	DedeUserID   string
	RefreshToken string
	Buvid3       string
}

// NewCookies 从原始 cookie 字符串创建 Cookies 对象
func NewCookies(cookieStr string) *Cookies {
	c := &Cookies{}
	for _, part := range strings.Split(cookieStr, ";") {
		part = strings.TrimSpace(part)
		if val, ok := strings.CutPrefix(part, "SESSDATA="); ok {
			c.SESSDATA = val
		} else if val, ok := strings.CutPrefix(part, "bili_jct="); ok {
			c.BiliJct = val
		} else if val, ok := strings.CutPrefix(part, "DedeUserID="); ok {
			c.DedeUserID = val
		} else if val, ok := strings.CutPrefix(part, "RefreshToken="); ok {
			c.RefreshToken = val
		} else if val, ok := strings.CutPrefix(part, "buvid3="); ok {
			c.Buvid3 = val
		}
	}
	return c
}

// String 返回用于 HTTP 请求的 cookie 字符串
func (c *Cookies) String() string {
	var parts []string
	if c.SESSDATA != "" {
		parts = append(parts, fmt.Sprintf("SESSDATA=%s", c.SESSDATA))
	}
	if c.BiliJct != "" {
		parts = append(parts, fmt.Sprintf("bili_jct=%s", c.BiliJct))
	}
	if c.DedeUserID != "" {
		parts = append(parts, fmt.Sprintf("DedeUserID=%s", c.DedeUserID))
	}
	if c.Buvid3 != "" {
		parts = append(parts, fmt.Sprintf("buvid3=%s", c.Buvid3))
	}
	return strings.Join(parts, "; ")
}

// RefreshResult 刷新结果
type RefreshResult struct {
	Success         bool
	NewCookies      *Cookies
	NewRefreshToken string
	Error           error
}

// CookieRefresher 处理 cookie 刷新逻辑
type CookieRefresher struct {
	cookies *Cookies
	client  *http.Client
}

// NewCookieRefresher 创建 CookieRefresher
func NewCookieRefresher(cookies *Cookies) *CookieRefresher {
	return &CookieRefresher{
		cookies: cookies,
		client: &http.Client{
			Timeout: 30 * time.Second,
		},
	}
}

// ShouldRefresh 检查 cookie 是否需要刷新
func (r *CookieRefresher) ShouldRefresh() (bool, int64, error) {
	req, err := http.NewRequest("GET", "https://passport.bilibili.com/x/passport-login/web/cookie/info", nil)
	if err != nil {
		return false, 0, err
	}
	req.Header.Set("Cookie", r.cookies.String())
	req.Header.Set("User-Agent", UserAgent)

	resp, err := r.client.Do(req)
	if err != nil {
		return false, 0, err
	}
	defer resp.Body.Close()

	body, _ := io.ReadAll(resp.Body)

	type CookieInfoResult struct {
		Data struct {
			Refresh   bool  `xml:"refresh"`
			Timestamp int64 `xml:"timestamp"`
		} `xml:"data"`
	}

	var result CookieInfoResult
	if err := xml.Unmarshal(body, &result); err != nil {
		return r.shouldRefreshJSON(body)
	}

	return result.Data.Refresh, result.Data.Timestamp, nil
}

func (r *CookieRefresher) shouldRefreshJSON(body []byte) (bool, int64, error) {
	s := string(body)
	idx := strings.Index(s, `"refresh":`)
	if idx == -1 {
		return false, 0, nil
	}
	refresh := s[idx+9] == 't'

	tsIdx := strings.Index(s, `"timestamp":`)
	if tsIdx == -1 {
		return refresh, 0, nil
	}
	ts := int64(0)
	for i := tsIdx + 10; i < len(s) && i < tsIdx+30; i++ {
		if s[i] >= '0' && s[i] <= '9' {
			ts = ts*10 + int64(s[i]-'0')
		} else if ts > 0 {
			break
		}
	}
	return refresh, ts, nil
}

// getCorrespondPath 生成对应路径
func getCorrespondPath(ts int64) string {
	const publicKeyPEM = `
-----BEGIN PUBLIC KEY-----
MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDLgd2OAkcGVtoE3ThUREbio0Eg
Uc/prcajMKXvkCKFCWhJYJcLkcM2DKKcSeFpD/j6Boy538YXnR6VhcuUJOhH2x71
nzPjfdTcqMz7djHum0qSZA0AyCBDABUqCrfNgCiJ00Ra7GmRj+YCK1NJEuewlb40
JNrRuoEUXpabUzGB8QIDAQAB
-----END PUBLIC KEY-----
`
	pubKeyBlock, _ := pem.Decode([]byte(publicKeyPEM))
	hash := sha256.New()
	random := rand.Reader
	msg := fmt.Sprintf("refresh_%d", ts)

	pubInterface, _ := x509.ParsePKIXPublicKey(pubKeyBlock.Bytes)
	pub := pubInterface.(*rsa.PublicKey)

	encryptedData, _ := rsa.EncryptOAEP(hash, random, pub, []byte(msg), nil)
	return hex.EncodeToString(encryptedData)
}

// Refresh 刷新 cookie 并返回新的 cookies
func (r *CookieRefresher) Refresh() *RefreshResult {
	needRefresh, ts, err := r.ShouldRefresh()
	if err != nil {
		return &RefreshResult{Error: err}
	}
	if !needRefresh {
		return &RefreshResult{Success: true}
	}

	path := getCorrespondPath(ts)
	req, _ := http.NewRequest("GET", "https://www.bilibili.com/correspond/1/"+path, nil)
	req.Header.Set("Cookie", r.cookies.String())
	req.Header.Set("User-Agent", UserAgent)

	resp, err := r.client.Do(req)
	if err != nil {
		return &RefreshResult{Error: err}
	}
	defer resp.Body.Close()

	body, _ := io.ReadAll(resp.Body)
	csrf := r.extractCSRF(string(body))
	if csrf == "" {
		return &RefreshResult{Error: fmt.Errorf("failed to extract CSRF")}
	}

	refreshURL := fmt.Sprintf("csrf=%s&refresh_csrf=%s&source=main_web&refresh_token=%s",
		r.cookies.BiliJct, csrf, r.cookies.RefreshToken)

	req2, _ := http.NewRequest("POST", "https://passport.bilibili.com/x/passport-login/web/cookie/refresh?"+refreshURL, nil)
	req2.Header.Set("Cookie", r.cookies.String())
	req2.Header.Set("User-Agent", UserAgent)

	resp2, err := r.client.Do(req2)
	if err != nil {
		return &RefreshResult{Error: err}
	}
	defer resp2.Body.Close()

	body2, _ := io.ReadAll(resp2.Body)

	var newCookieStr strings.Builder
	for _, v := range resp2.Header.Values("Set-Cookie") {
		if part, _, found := strings.Cut(v, ";"); found {
			newCookieStr.WriteString(part)
			newCookieStr.WriteString(";")
		}
	}

	newRefreshToken := r.cookies.RefreshToken
	if strings.Contains(string(body2), "refresh_token") {
		idx := strings.Index(string(body2), `"refresh_token":"`)
		if idx != -1 {
			start := idx + 17
			end := start
			for end < len(string(body2)) && string(body2)[end] != '"' {
				end++
			}
			newRefreshToken = string(body2)[start:end]
		}
	}

	if newCookieStr.String() == "" || newRefreshToken == "" {
		return &RefreshResult{Error: fmt.Errorf("refresh failed: %s", string(body2))}
	}

	newCookies := &Cookies{
		SESSDATA:     r.extractCookiePart(newCookieStr.String(), "SESSDATA"),
		BiliJct:      r.extractCookiePart(newCookieStr.String(), "bili_jct"),
		DedeUserID:   r.extractCookiePart(newCookieStr.String(), "DedeUserID"),
		Buvid3:       r.extractCookiePart(newCookieStr.String(), "buvid3"),
		RefreshToken: newRefreshToken,
	}

	return &RefreshResult{
		Success:         true,
		NewCookies:      newCookies,
		NewRefreshToken: newRefreshToken,
	}
}

func (r *CookieRefresher) extractCSRF(html string) string {
	idx := strings.Index(html, `id="1-name"`)
	if idx == -1 {
		return ""
	}
	start := idx + 11
	end := start
	for end < len(html) && html[end:end+1] != "<" {
		end++
	}
	return strings.TrimSpace(html[start:end])
}

func (r *CookieRefresher) extractCookiePart(cookieStr, name string) string {
	_, val, found := strings.Cut(cookieStr, name+"=")
	if !found {
		return ""
	}
	if end, _, found := strings.Cut(val, ";"); found {
		return end
	}
	return val
}
