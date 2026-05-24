package bilibili

import (
	"encoding/json"
	"fanclub-apiserver/g"
	"os"
	"strconv"
	"sync/atomic"
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

// --- 单元测试 ---

// TestNewDanmuAuth 测试创建默认弹幕认证对象
func TestNewDanmuAuth(t *testing.T) {
	auth := NewDanmuAuth(12345, "test-token", 67890, "buvid123")

	assert.Equal(t, int64(67890), auth.UID)
	assert.Equal(t, int64(12345), auth.RoomID)
	assert.Equal(t, 3, auth.ProtoVer)
	assert.Equal(t, "buvid123", auth.Buvid)
	assert.True(t, auth.SupportAck)
	assert.Equal(t, "room", auth.Scene)
	assert.Equal(t, "web", auth.Platform)
	assert.Equal(t, 2, auth.Type)
	assert.Equal(t, "test-token", auth.Key)
}

// TestDanmuAuthJSON 测试认证对象JSON序列化（键名须与B站API一致）
func TestDanmuAuthJSON(t *testing.T) {
	auth := NewDanmuAuth(12345, "test-token", 67890, "buvid123")
	data, err := json.Marshal(auth)
	require.NoError(t, err)

	var m map[string]any
	err = json.Unmarshal(data, &m)
	require.NoError(t, err)

	// 验证B站API要求的键名
	assert.Contains(t, m, "uid")
	assert.Contains(t, m, "roomid") // 注意：不是roomId
	assert.Contains(t, m, "protover")
	assert.Contains(t, m, "buvid")
	assert.Contains(t, m, "supportAck") // 注意：不是support_ack
	assert.Contains(t, m, "scene")
	assert.Contains(t, m, "platform")
	assert.Contains(t, m, "type")
	assert.Contains(t, m, "key")
}

// TestDedupCache 测试消息去重缓存
func TestDedupCache(t *testing.T) {
	cache := newDedupCache(100 * time.Millisecond)

	// 首次不是重复
	assert.False(t, cache.isDuplicate("msg1"))
	// TTL内是重复
	assert.True(t, cache.isDuplicate("msg1"))

	// 不同消息不是重复
	assert.False(t, cache.isDuplicate("msg2"))
	assert.True(t, cache.isDuplicate("msg2"))

	// 等待过期
	time.Sleep(150 * time.Millisecond)
	// 过期后不是重复
	assert.False(t, cache.isDuplicate("msg1"))
}

// TestWSClientConfigDefaults 测试客户端配置默认值
func TestWSClientConfigDefaults(t *testing.T) {
	client := NewWSClient(WSClientConfig{
		HostList: []*DanmuHost{{Host: "test", WssPort: 443}},
		RoomID:   123,
		Token:    "token",
	})

	assert.Equal(t, defaultMaxRetry, client.config.MaxRetry)
	assert.Equal(t, defaultHeartbeatInterval, client.config.HeartbeatInterval)
}

// TestWSClientCloseBeforeConnect 测试在连接前关闭客户端
func TestWSClientCloseBeforeConnect(t *testing.T) {
	client := NewWSClient(WSClientConfig{
		HostList: []*DanmuHost{{Host: "test", WssPort: 443}},
		RoomID:   123,
		Token:    "token",
	})

	// 关闭未连接的客户端不应报错
	err := client.Close()
	assert.NoError(t, err)

	// 关闭后连接应报错
	err = client.Connect()
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "已关闭")
}

// --- 集成测试（需要真实B站账号环境变量，未设置时跳过） ---

// TestWSClientRealConnection 测试真实WebSocket连接
func TestWSClientRealConnection(t *testing.T) {
	roomID, err := strconv.ParseInt(os.Getenv("BILI_ROOM_ID"), 10, 64)
	if err != nil || roomID <= 0 {
		t.Skip("跳过: 需要设置环境变量 BILI_ROOM_ID")
	}

	uid, _ := strconv.ParseInt(os.Getenv("BILI_UID"), 10, 64)
	sessdata := os.Getenv("BILI_SESSDATA")

	_ = g.InitLogger("debug")
	// 创建HTTP客户端获取弹幕服务器信息
	cookie := ""
	if sessdata != "" {
		cookie = "SESSDATA=" + sessdata
	}
	client := NewClient(cookie)

	info, err := client.FetchDanmuServerInfo(roomID)
	require.NoError(t, err, "获取弹幕服务器信息失败")
	require.NotNil(t, info.Data, "未获取到弹幕服务器数据")
	require.NotEmpty(t, info.Data.HostList, "主机列表为空")
	require.NotEmpty(t, info.Data.Token, "未获取到Token")

	// 创建WebSocket客户端
	var msgCount atomic.Int32
	wsClient := NewWSClient(WSClientConfig{
		HostList: info.Data.HostList,
		RoomID:   roomID,
		Token:    info.Data.Token,
		UID:      uid,
	})

	// 连接
	err = wsClient.Connect()
	require.NoError(t, err, "连接弹幕服务器失败")
	defer wsClient.Close()

	// 等待接收消息
	time.Sleep(24 * time.Second)
	count := msgCount.Load()
	t.Logf("收到 %d 条消息", count)
}
