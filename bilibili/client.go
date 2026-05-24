package bilibili

import (
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"strconv"
	"time"
)

// UserAgent B站请求User-Agent
const UserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

// B站API地址（变量以便测试替换）
var (
	WbiInitURL          = "https://api.bilibili.com/x/web-interface/nav"
	DanmuServerConfURL  = "https://api.live.bilibili.com/xlive/web-room/v1/index/getDanmuInfo"
	UserRelationStatURL = "https://api.bilibili.com/x/relation/stat"
)

// biliBaseResponse B站API基础响应
type biliBaseResponse struct {
	Code    int    `json:"code"`
	Message string `json:"message"`
}

// checkCode 检查B站API响应码
func (r *biliBaseResponse) checkCode() error {
	if r.Code != 0 {
		return &BiliApiError{Code: r.Code, Message: r.Message}
	}
	return nil
}

// --- WBI 信息响应 ---

// WbiImg WBI图片信息
type WbiImg struct {
	ImgURL string `json:"img_url"`
	SubURL string `json:"sub_url"`
}

// WbiData WBI数据
type WbiData struct {
	WbiImg *WbiImg `json:"wbi_img"`
}

// WbiInfoResponse WBI信息响应
type WbiInfoResponse struct {
	biliBaseResponse
	Data *WbiData `json:"data"`
}

// --- 弹幕服务器信息响应 ---

// DanmuHost 弹幕服务器主机
type DanmuHost struct {
	Host    string `json:"host"`
	Port    int    `json:"port"`
	WsPort  int    `json:"ws_port"`
	WssPort int    `json:"wss_port"`
}

// DanmuInfoData 弹幕服务器信息数据
type DanmuInfoData struct {
	Group            string       `json:"group,omitempty"`
	BusinessID       int64        `json:"business_id,omitempty"`
	RefreshRowFactor float64      `json:"refresh_row_factor,omitempty"`
	RefreshRate      int64        `json:"refresh_rate,omitempty"`
	MaxDelay         int64        `json:"max_delay,omitempty"`
	Token            string       `json:"token,omitempty"`
	HostList         []*DanmuHost `json:"host_list,omitempty"`
}

// DanmuInfoResponse 弹幕服务器信息响应
type DanmuInfoResponse struct {
	biliBaseResponse
	Data *DanmuInfoData `json:"data"`
}

// --- 用户关系响应 ---

// UserRelationData 用户关系数据
type UserRelationData struct {
	Following int `json:"following"`
	Follower  int `json:"follower"`
}

// UserRelationResponse 用户关系响应
type UserRelationResponse struct {
	biliBaseResponse
	Data *UserRelationData `json:"data"`
}

// Client B站HTTP客户端
type Client struct {
	httpClient *http.Client
	cookie     string
	wbiSignKey string
}

// NewClient 创建B站HTTP客户端
// cookie 可选，用于身份认证
func NewClient(cookie string) *Client {
	return &Client{
		httpClient: &http.Client{Timeout: 10 * time.Second},
		cookie:     cookie,
	}
}

// get 执行GET请求并解析JSON响应
func (c *Client) get(url string, result any) error {
	req, err := http.NewRequest(http.MethodGet, url, nil)
	if err != nil {
		return fmt.Errorf("创建请求失败: %w", err)
	}
	req.Header.Set("User-Agent", UserAgent)
	if c.cookie != "" {
		req.Header.Set("Cookie", c.cookie)
	}

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return fmt.Errorf("请求失败: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("HTTP %d", resp.StatusCode)
	}

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return fmt.Errorf("读取响应失败: %w", err)
	}

	if err := json.Unmarshal(body, result); err != nil {
		return fmt.Errorf("解析JSON失败: %w", err)
	}

	// 检查B站API业务码
	if base, ok := result.(interface{ checkCode() error }); ok {
		if err := base.checkCode(); err != nil {
			return err
		}
	}

	return nil
}

// GetWbiSignKey 获取WBI签名密钥（带缓存）
func (c *Client) GetWbiSignKey() (string, error) {
	if c.wbiSignKey != "" {
		return c.wbiSignKey, nil
	}

	var resp WbiInfoResponse
	if err := c.get(WbiInitURL, &resp); err != nil {
		return "", fmt.Errorf("获取WBI信息失败: %w", err)
	}

	if resp.Data == nil || resp.Data.WbiImg == nil {
		return "", fmt.Errorf("获取WBI图片信息失败")
	}

	key := WbiSign(resp.Data.WbiImg.ImgURL, resp.Data.WbiImg.SubURL)
	c.wbiSignKey = key
	return key, nil
}

// FetchDanmuServerInfo 获取弹幕服务器信息
func (c *Client) FetchDanmuServerInfo(roomID int64) (*DanmuInfoResponse, error) {
	sign, err := c.GetWbiSignKey()
	if err != nil {
		return nil, err
	}

	params := map[string]string{
		"id":   strconv.FormatInt(roomID, 10),
		"type": "0",
	}
	queryStr := BuildWbiQueryString(params, sign)
	url := DanmuServerConfURL + "?" + queryStr

	var resp DanmuInfoResponse
	if err := c.get(url, &resp); err != nil {
		return nil, err
	}
	return &resp, nil
}

// FetchUserRelation 获取用户关系数据（关注数、粉丝数）
func (c *Client) FetchUserRelation(uid int64) (*UserRelationResponse, error) {
	sign, err := c.GetWbiSignKey()
	if err != nil {
		return nil, err
	}

	params := map[string]string{
		"vmid": strconv.FormatInt(uid, 10),
	}
	queryStr := BuildWbiQueryString(params, sign)
	url := UserRelationStatURL + "?" + queryStr

	var resp UserRelationResponse
	if err := c.get(url, &resp); err != nil {
		return nil, err
	}
	return &resp, nil
}
