package bilibili

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"strconv"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

// --- WBI 签名测试 ---

func TestExtractWbiKey(t *testing.T) {
	tests := []struct {
		name string
		url  string
		want string
	}{
		{"带.png后缀", "https://i0.hdslb.com/bfs/wbi/7cd08a5bdc13.png", "7cd08a5bdc13"},
		{"带.jpg后缀", "https://i0.hdslb.com/bfs/wbi/abc123.jpg", "abc123"},
		{"无后缀", "https://i0.hdslb.com/bfs/wbi/noext", "noext"},
		{"空路径", "noext", "noext"},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			assert.Equal(t, tt.want, extractWbiKey(tt.url))
		})
	}
}

func TestWbiSign(t *testing.T) {
	// 使用已知URL测试签名生成的一致性
	imgURL := "https://i0.hdslb.com/bfs/wbi/7cd08a5bdc13.png"
	subURL := "https://i0.hdslb.com/bfs/wbi/3e3ab0f9ef43.png"

	key1 := WbiSign(imgURL, subURL)
	key2 := WbiSign(imgURL, subURL)
	assert.Equal(t, key1, key2, "相同输入应产生相同签名")
	assert.NotEmpty(t, key1)

	// 不同输入应产生不同签名
	key3 := WbiSign(subURL, imgURL)
	assert.NotEqual(t, key1, key3)
}

func TestBuildWbiQueryString(t *testing.T) {
	params := map[string]string{
		"id":   "12345",
		"type": "0",
	}
	wbiKey := "test_wbi_key_123456789012"

	qs := BuildWbiQueryString(params, wbiKey)

	// 必须包含wts和w_rid
	assert.Contains(t, qs, "wts=")
	assert.Contains(t, qs, "w_rid=")
	assert.Contains(t, qs, "id=12345")
	assert.Contains(t, qs, "type=0")

	// w_rid应为32位MD5
	// 解析w_rid
	var wRid string
	for _, part := range splitQueryString(qs) {
		if k, v := splitKV(part); k == "w_rid" {
			wRid = v
		}
	}
	assert.Len(t, wRid, 32, "w_rid应为32位hex字符串")
}

func splitQueryString(qs string) []string {
	return splitBy(qs, '&')
}

func splitKV(s string) (string, string) {
	for i := 0; i < len(s); i++ {
		if s[i] == '=' {
			return s[:i], s[i+1:]
		}
	}
	return s, ""
}

func splitBy(s string, sep byte) []string {
	var result []string
	start := 0
	for i := 0; i < len(s); i++ {
		if s[i] == sep {
			result = append(result, s[start:i])
			start = i + 1
		}
	}
	result = append(result, s[start:])
	return result
}

// --- BiliApiError 测试 ---

func TestBiliApiError(t *testing.T) {
	err := &BiliApiError{Code: -403, Message: "访问权限不足"}
	assert.Equal(t, "B站API错误 [-403]: 访问权限不足", err.Error())
}

// --- HTTP 客户端测试 ---

// mockBiliServer 创建模拟B站API服务器
func mockBiliServer() *httptest.Server {
	mux := http.NewServeMux()

	// WBI初始化接口
	mux.HandleFunc("/x/web-interface/nav", func(w http.ResponseWriter, r *http.Request) {
		resp := WbiInfoResponse{
			biliBaseResponse: biliBaseResponse{Code: 0, Message: "0"},
			Data: &WbiData{
				WbiImg: &WbiImg{
					ImgURL: "https://i0.hdslb.com/bfs/wbi/7cd08a5bdc13.png",
					SubURL: "https://i0.hdslb.com/bfs/wbi/3e3ab0f9ef43.png",
				},
			},
		}
		writeJSON(w, resp)
	})

	// 弹幕服务器信息接口
	mux.HandleFunc("/xlive/web-room/v1/index/getDanmuInfo", func(w http.ResponseWriter, r *http.Request) {
		roomID := r.URL.Query().Get("id")
		resp := DanmuInfoResponse{
			biliBaseResponse: biliBaseResponse{Code: 0, Message: "0"},
			Data: &DanmuInfoData{
				Token:  "test_token_" + roomID,
				Group:  "live",
				HostList: []*DanmuHost{
					{Host: "broadcastlv.chat.bilibili.com", Port: 2243, WsPort: 2244, WssPort: 2245},
				},
			},
		}
		writeJSON(w, resp)
	})

	// 用户关系接口
	mux.HandleFunc("/x/relation/stat", func(w http.ResponseWriter, r *http.Request) {
		uid := r.URL.Query().Get("vmid")
		uidInt, _ := strconv.ParseInt(uid, 10, 64)
		resp := UserRelationResponse{
			biliBaseResponse: biliBaseResponse{Code: 0, Message: "0"},
			Data: &UserRelationData{
				Following: 100,
				Follower:  int(uidInt % 10000),
			},
		}
		writeJSON(w, resp)
	})

	// API错误接口
	mux.HandleFunc("/api/error", func(w http.ResponseWriter, r *http.Request) {
		resp := biliBaseResponse{Code: -403, Message: "访问权限不足"}
		writeJSON(w, resp)
	})

	// HTTP错误接口
	mux.HandleFunc("/api/http-error", func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusServiceUnavailable)
	})

	return httptest.NewServer(mux)
}

func writeJSON(w http.ResponseWriter, v any) {
	w.Header().Set("Content-Type", "application/json")
	data, _ := json.Marshal(v)
	w.Write(data)
}

func TestClient_GetWbiSignKey(t *testing.T) {
	server := mockBiliServer()
	defer server.Close()

	// 替换URL常量
	origURL := WbiInitURL
	WbiInitURL = server.URL + "/x/web-interface/nav"
	defer func() { WbiInitURL = origURL }()

	client := NewClient("")
	key, err := client.GetWbiSignKey()
	require.NoError(t, err)
	assert.NotEmpty(t, key)

	// 缓存：第二次调用应返回相同值
	key2, err := client.GetWbiSignKey()
	require.NoError(t, err)
	assert.Equal(t, key, key2)
}

func TestClient_FetchDanmuServerInfo(t *testing.T) {
	server := mockBiliServer()
	defer server.Close()

	// 替换URL常量
	origWbiURL := WbiInitURL
	origDanmuURL := DanmuServerConfURL
	WbiInitURL = server.URL + "/x/web-interface/nav"
	DanmuServerConfURL = server.URL + "/xlive/web-room/v1/index/getDanmuInfo"
	defer func() {
		WbiInitURL = origWbiURL
		DanmuServerConfURL = origDanmuURL
	}()

	client := NewClient("")
	resp, err := client.FetchDanmuServerInfo(12345)
	require.NoError(t, err)
	require.NotNil(t, resp.Data)

	assert.Equal(t, "test_token_12345", resp.Data.Token)
	require.Len(t, resp.Data.HostList, 1)
	assert.Equal(t, "broadcastlv.chat.bilibili.com", resp.Data.HostList[0].Host)
	assert.Equal(t, 2244, resp.Data.HostList[0].WsPort)
}

func TestClient_FetchUserRelation(t *testing.T) {
	server := mockBiliServer()
	defer server.Close()

	// 替换URL常量
	origWbiURL := WbiInitURL
	origRelationURL := UserRelationStatURL
	WbiInitURL = server.URL + "/x/web-interface/nav"
	UserRelationStatURL = server.URL + "/x/relation/stat"
	defer func() {
		WbiInitURL = origWbiURL
		UserRelationStatURL = origRelationURL
	}()

	client := NewClient("")
	resp, err := client.FetchUserRelation(67890)
	require.NoError(t, err)
	require.NotNil(t, resp.Data)

	assert.Equal(t, 100, resp.Data.Following)
	assert.Equal(t, 7890, resp.Data.Follower) // 67890 % 10000
}

func TestClient_ApiError(t *testing.T) {
	server := mockBiliServer()
	defer server.Close()

	// 直接使用mock服务器URL请求错误接口
	client := NewClient("")
	var resp biliBaseResponse
	err := client.get(server.URL+"/api/error", &resp)
	require.Error(t, err)

	apiErr, ok := err.(*BiliApiError)
	require.True(t, ok)
	assert.Equal(t, -403, apiErr.Code)
	assert.Equal(t, "访问权限不足", apiErr.Message)
}

func TestClient_HttpError(t *testing.T) {
	server := mockBiliServer()
	defer server.Close()

	client := NewClient("")
	var resp biliBaseResponse
	err := client.get(server.URL+"/api/http-error", &resp)
	require.Error(t, err)
	assert.Contains(t, err.Error(), "HTTP 503")
}

func TestNewClient(t *testing.T) {
	client := NewClient("SESSDATA=abc123")
	assert.Equal(t, "SESSDATA=abc123", client.cookie)
	assert.NotNil(t, client.httpClient)
}

func TestClient_WithCookie(t *testing.T) {
	server := mockBiliServer()
	defer server.Close()

	// 使用带cookie的客户端请求，验证cookie被发送
	var receivedCookie string
	mux := http.NewServeMux()
	mux.HandleFunc("/test", func(w http.ResponseWriter, r *http.Request) {
		receivedCookie = r.Header.Get("Cookie")
		writeJSON(w, biliBaseResponse{Code: 0, Message: "0"})
	})

	ts := httptest.NewServer(mux)
	defer ts.Close()

	client := NewClient("SESSDATA=test123")
	var resp biliBaseResponse
	err := client.get(ts.URL+"/test", &resp)
	require.NoError(t, err)
	assert.Equal(t, "SESSDATA=test123", receivedCookie)
}

func TestClient_NoCookie(t *testing.T) {
	server := mockBiliServer()
	defer server.Close()

	var receivedCookie string
	mux := http.NewServeMux()
	mux.HandleFunc("/test", func(w http.ResponseWriter, r *http.Request) {
		receivedCookie = r.Header.Get("Cookie")
		writeJSON(w, biliBaseResponse{Code: 0, Message: "0"})
	})

	ts := httptest.NewServer(mux)
	defer ts.Close()

	client := NewClient("")
	var resp biliBaseResponse
	err := client.get(ts.URL+"/test", &resp)
	require.NoError(t, err)
	assert.Empty(t, receivedCookie)
}

func TestClient_WbiSignCache(t *testing.T) {
	callCount := 0
	mux := http.NewServeMux()
	mux.HandleFunc("/x/web-interface/nav", func(w http.ResponseWriter, r *http.Request) {
		callCount++
		resp := WbiInfoResponse{
			biliBaseResponse: biliBaseResponse{Code: 0, Message: "0"},
			Data: &WbiData{
				WbiImg: &WbiImg{
					ImgURL: "https://i0.hdslb.com/bfs/wbi/7cd08a5bdc13.png",
					SubURL: "https://i0.hdslb.com/bfs/wbi/3e3ab0f9ef43.png",
				},
			},
		}
		writeJSON(w, resp)
	})

	ts := httptest.NewServer(mux)
	defer ts.Close()

	origURL := WbiInitURL
	WbiInitURL = ts.URL + "/x/web-interface/nav"
	defer func() { WbiInitURL = origURL }()

	client := NewClient("")

	// 第一次调用
	_, err := client.GetWbiSignKey()
	require.NoError(t, err)
	assert.Equal(t, 1, callCount)

	// 第二次调用应使用缓存
	_, err = client.GetWbiSignKey()
	require.NoError(t, err)
	assert.Equal(t, 1, callCount, "WBI签名应被缓存，不应重复请求")
}
