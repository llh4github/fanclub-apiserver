package bilibili

import (
	"context"
	"encoding/json"
	"fmt"
	"math/rand"
	"net/http"
	"sync"
	"time"

	"fanclub-apiserver/database/generated"
	"fanclub-apiserver/database/model"
	"fanclub-apiserver/g"

	"github.com/gorilla/websocket"
	"go.uber.org/zap"
	"gorm.io/cli/gorm/typed"
	"gorm.io/gorm/clause"
)

// WebSocket客户端常量
const (
	defaultHeartbeatInterval = 30 * time.Second
	defaultMaxRetry          = 5
	wsReadTimeout            = 60 * time.Second
	wsReconnectDelay         = 2 * time.Second
)

// OnConnectionFailed 连接失败（重连耗尽）回调函数类型
type OnConnectionFailed func(roomID int64)

// DanmuAuth 弹幕认证信息（JSON键名须与B站API一致，不使用snake_case）
type DanmuAuth struct {
	UID        int64  `json:"uid"`
	RoomID     int64  `json:"roomid"`
	ProtoVer   int    `json:"protover"`
	Buvid      string `json:"buvid"`
	SupportAck bool   `json:"supportAck"`
	Scene      string `json:"scene"`
	Platform   string `json:"platform"`
	Type       int    `json:"type"`
	Key        string `json:"key"`
}

// NewDanmuAuth 创建默认弹幕认证对象
func NewDanmuAuth(roomID int64, token string, uid int64, buvid string) *DanmuAuth {
	return &DanmuAuth{
		UID:        uid,
		RoomID:     roomID,
		ProtoVer:   3, // brotli压缩
		Buvid:      buvid,
		SupportAck: true,
		Scene:      "room",
		Platform:   "web",
		Type:       2,
		Key:        token,
	}
}

// WSClientConfig WebSocket客户端配置
type WSClientConfig struct {
	// HostList 弹幕服务器主机列表
	HostList []*DanmuHost
	// RoomID 房间ID
	RoomID int64
	// Token 认证令牌
	Token string
	// UID 用户ID
	UID int64
	// Buvid 设备ID
	Buvid string
	// MaxRetry 最大重连次数，默认5
	MaxRetry int
	// HeartbeatInterval 心跳间隔，默认30秒
	HeartbeatInterval time.Duration
	// OnConnectionFailed 连接失败（重连耗尽）回调
	OnConnectionFailed OnConnectionFailed
}

// WSClient B站弹幕WebSocket客户端
//
// 连接B站弹幕服务器，接收弹幕消息，通过Dispatcher分发到对应处理器。
// 支持自动重连、心跳保活、消息去重。
type WSClient struct {
	config     WSClientConfig
	dispatcher *Dispatcher
	dedup      *dedupCache
	conn       *websocket.Conn
	logger     *zap.Logger
	cancel     context.CancelFunc
	writeMu    sync.Mutex // 保护并发写入WebSocket
	mu         sync.Mutex // 保护conn、cancel、retryCount、closed
	retryCount int
	closed     bool
}

// NewWSClient 创建WebSocket客户端
func NewWSClient(config WSClientConfig, handlers ...Handler) *WSClient {
	if config.MaxRetry <= 0 {
		config.MaxRetry = defaultMaxRetry
	}
	if config.HeartbeatInterval <= 0 {
		config.HeartbeatInterval = defaultHeartbeatInterval
	}

	// 合并默认处理器与外部传入的处理器
	allHandlers := make([]Handler, 0, len(defaultHandlers)+len(handlers))
	allHandlers = append(allHandlers, defaultHandlers...)
	allHandlers = append(allHandlers, handlers...)

	return &WSClient{
		config:     config,
		dispatcher: NewDispatcher(allHandlers...),
		dedup:      newDedupCache(3 * time.Second),
		logger:     g.Named("bilibili.ws"),
	}
}

// IsConnected 检测 WebSocket 连通性
func (c *WSClient) IsConnected() bool {
	c.mu.Lock()
	defer c.mu.Unlock()
	if c.closed || c.conn == nil {
		return false
	}
	// 尝试发送一个无阻塞的 ping 探测连接是否存活
	err := c.conn.WriteControl(websocket.PingMessage, nil, time.Now().Add(time.Second))
	return err == nil
}

// Config 返回客户端配置
func (c *WSClient) Config() WSClientConfig {
	return c.config
}

// wsClientManager WebSocket 客户端管理器
type wsClientManager struct {
	mu      sync.RWMutex
	clients []*WSClient
}

// 全局客户端管理器实例
var WSManager = new(wsClientManager)

// Add 添加客户端到管理器
func (m *wsClientManager) Add(clients ...*WSClient) {
	m.mu.Lock()
	defer m.mu.Unlock()
	m.clients = append(m.clients, clients...)
}

// RemoveAndClose 剔除并关闭无效的客户端，返回被移除的数量
func (m *wsClientManager) RemoveAndClose() int {
	m.mu.Lock()
	defer m.mu.Unlock()

	var alive []*WSClient
	removed := 0
	for _, c := range m.clients {
		if !c.IsConnected() {
			_ = c.Close()
			removed++
		} else {
			alive = append(alive, c)
		}
	}
	m.clients = alive
	return removed
}

// CreateMonitorWSClients 为所有启用数据监控的主播创建WebSocket客户端
//
// 查询启用 monitor 特性的主播的 room_id，随机选取一个 UID 的 Cookie，
// 调用B站API获取弹幕服务器信息，创建 WSClient 实例列表。
func CreateMonitorWSClients(ctx context.Context, handlers ...Handler) ([]*WSClient, error) {
	// 1. 连表查询：启用了数据监控特性的主播的 room_id
	var monitorResults []struct {
		// 直播间 ID
		RoomID int64 `json:"room_id"`
	}
	if err := typed.G[model.SysScraperFeature](g.DB).
		Select(
			generated.AnchorInfo.RoomID.WithTable("Anchor").As("room_id"),
		).
		Where(generated.SysScraperFeature.Monitor.Eq(true)).
		Joins(clause.LeftJoin.Association("Anchor"), func(_ typed.JoinBuilder, _ clause.Table, _ clause.Table) error {
			return nil
		}).
		Scan(ctx, &monitorResults); err != nil {
		return nil, fmt.Errorf("查询启用了数据监控的主播失败: %w", err)
	}

	// 提取有效的 room_id
	var roomIDs []int64
	for _, r := range monitorResults {
		if r.RoomID > 0 {
			roomIDs = append(roomIDs, r.RoomID)
		}
	}
	if len(roomIDs) == 0 {
		return nil, fmt.Errorf("没有启用了数据监控的主播")
	}

	// 2. 随机获取一个 UID 的 cookies 数据列表
	cookieQ := typed.G[model.SysScraperCookie](g.DB)
	var uidResults []struct {
		UID int64 `json:"uid"`
	}
	if err := cookieQ.
		Select(generated.SysScraperCookie.UID).
		Distinct(generated.SysScraperCookie.UID).
		Scan(ctx, &uidResults); err != nil {
		return nil, fmt.Errorf("查询 Cookie UID 列表失败: %w", err)
	}
	if len(uidResults) == 0 {
		return nil, fmt.Errorf("没有可用的 Cookie 数据")
	}

	// 随机选取一个 UID
	selectedUID := uidResults[rand.Intn(len(uidResults))].UID

	// 获取该 UID 的所有 Cookie
	cookies, err := cookieQ.
		Where(generated.SysScraperCookie.UID.Eq(selectedUID)).
		Find(ctx)
	if err != nil {
		return nil, fmt.Errorf("查询 UID=%d 的 Cookie 列表失败: %w", selectedUID, err)
	}

	g.Info("使用 UID 的 Cookies 创建 WebSocket 连接",
		zap.Int64("uid", selectedUID),
		zap.Int("cookie_count", len(cookies)),
	)

	// 构建 cookie 字符串
	cookieStr := buildCookieString(cookies)

	// 3. 为每个房间创建 WSClient
	biliClient := NewClient(cookieStr)
	clients := make([]*WSClient, 0, len(roomIDs))

	for _, roomID := range roomIDs {
		danmuResp, err := biliClient.FetchDanmuServerInfo(roomID)
		if err != nil {
			g.Named("bilibili.ws").Error("获取弹幕服务器信息失败",
				zap.Int64("roomID", roomID),
				zap.Error(err),
			)
			continue
		}
		if danmuResp.Data == nil || len(danmuResp.Data.HostList) == 0 {
			g.Named("bilibili.ws").Warn("弹幕服务器信息为空",
				zap.Int64("roomID", roomID),
			)
			continue
		}

		config := WSClientConfig{
			HostList: danmuResp.Data.HostList,
			RoomID:   roomID,
			Token:    danmuResp.Data.Token,
			UID:      selectedUID,
		}

		clients = append(clients, NewWSClient(config, handlers...))
	}

	if len(clients) == 0 {
		return nil, fmt.Errorf("没有成功创建任何 WebSocket 客户端")
	}

	return clients, nil
}

// buildCookieString 将 Cookie 列表构建为 HTTP Cookie 字符串
func buildCookieString(cookies []model.SysScraperCookie) string {
	result := make([]byte, 0, len(cookies)*30)
	for i, c := range cookies {
		if i > 0 {
			result = append(result, "; "...)
		}
		result = append(result, c.Name...)
		result = append(result, '=')
		result = append(result, c.Value...)
	}
	return string(result)
}

// Connect 连接到弹幕服务器
func (c *WSClient) Connect() error {
	c.mu.Lock()
	defer c.mu.Unlock()

	if c.closed {
		return fmt.Errorf("客户端已关闭")
	}

	// 清理旧连接
	if c.conn != nil {
		c.conn.Close()
		c.conn = nil
	}
	if c.cancel != nil {
		c.cancel()
		c.cancel = nil
	}

	// 轮询选择服务器
	server := c.config.HostList[c.retryCount%len(c.config.HostList)]
	url := fmt.Sprintf("wss://%s:%d/sub", server.Host, server.WssPort)

	if c.logger != nil {
		c.logger.Info("正在连接弹幕服务器", zap.String("url", url))
	}

	// 建立WebSocket连接
	header := http.Header{}
	header.Set("User-Agent", UserAgent)
	conn, _, err := websocket.DefaultDialer.Dial(url, header)
	if err != nil {
		return fmt.Errorf("连接弹幕服务器失败: %w", err)
	}
	c.conn = conn

	// 发送认证信息
	auth := NewDanmuAuth(c.config.RoomID, c.config.Token, c.config.UID, c.config.Buvid)
	authJSON, err := json.Marshal(auth)
	if err != nil {
		conn.Close()
		c.conn = nil
		return fmt.Errorf("序列化认证信息失败: %w", err)
	}

	if c.logger != nil {
		c.logger.Debug("发送认证信息", zap.String("auth", string(authJSON)))
	}

	c.writeMu.Lock()
	err = conn.WriteMessage(websocket.BinaryMessage, MakePacket(authJSON, OpAuth))
	c.writeMu.Unlock()
	if err != nil {
		conn.Close()
		c.conn = nil
		return fmt.Errorf("发送认证信息失败: %w", err)
	}

	// 启动心跳和消息读取
	ctx, cancel := context.WithCancel(context.Background())
	c.cancel = cancel

	go c.heartbeat(ctx)
	go c.readMessages(ctx)

	return nil
}

// Close 关闭客户端
func (c *WSClient) Close() error {
	c.mu.Lock()
	defer c.mu.Unlock()

	c.closed = true
	if c.cancel != nil {
		c.cancel()
	}
	if c.conn != nil {
		c.conn.Close()
	}
	return nil
}

// heartbeat 心跳发送
func (c *WSClient) heartbeat(ctx context.Context) {
	ticker := time.NewTicker(c.config.HeartbeatInterval)
	defer ticker.Stop()

	for {
		select {
		case <-ctx.Done():
			return
		case <-ticker.C:
			c.mu.Lock()
			conn := c.conn
			closed := c.closed
			c.mu.Unlock()

			if closed || conn == nil {
				return
			}

			data := MakePacket([]byte("{}"), OpHeartbeat)
			c.writeMu.Lock()
			err := conn.WriteMessage(websocket.BinaryMessage, data)
			c.writeMu.Unlock()
			if err != nil && c.logger != nil {
				c.logger.Error("发送心跳失败", zap.Error(err))
			}
		}
	}
}

// readMessages 读取消息循环
func (c *WSClient) readMessages(ctx context.Context) {
	for {
		c.mu.Lock()
		conn := c.conn
		c.mu.Unlock()

		if conn == nil {
			return
		}

		conn.SetReadDeadline(time.Now().Add(wsReadTimeout))
		_, msg, err := conn.ReadMessage()
		if err != nil {
			select {
			case <-ctx.Done():
				return
			default:
				c.reconnect()
				return
			}
		}

		// 解析数据包
		messages, err := ParsePacket(msg)
		if err != nil {
			if c.logger != nil {
				c.logger.Error("解析数据包失败", zap.Error(err))
			}
			continue
		}

		// 处理每条消息：去重 → 解析命令 → 分发
		for _, dm := range messages {
			raw := string(dm.RawData)
			if c.dedup.isDuplicate(raw) {
				continue
			}
			cmd := ParseCommand(raw)
			if cmd == nil {
				continue
			}
			if ok, _ := c.dispatcher.Dispatch(cmd, c.config.RoomID); !ok {
				if c.logger != nil {
					c.logger.Info("未找到命令处理器", zap.String("cmd", cmd.CmdName()), zap.String("raw", raw))
				}
			}
		}
	}
}

// reconnect 重连，超过最大次数时调用OnConnectionFailed回调
func (c *WSClient) reconnect() {
	c.mu.Lock()

	if c.closed {
		c.mu.Unlock()
		return
	}

	c.retryCount++
	count := c.retryCount

	// 取消旧上下文，关闭旧连接
	if c.cancel != nil {
		c.cancel()
		c.cancel = nil
	}
	if c.conn != nil {
		c.conn.Close()
		c.conn = nil
	}

	c.mu.Unlock()

	if count > c.config.MaxRetry {
		if c.logger != nil {
			c.logger.Error("重连次数超过限制", zap.Int("maxRetry", c.config.MaxRetry))
		}
		if c.config.OnConnectionFailed != nil {
			c.config.OnConnectionFailed(c.config.RoomID)
		}
		return
	}

	if c.logger != nil {
		c.logger.Info("将在2秒后重连", zap.Int("retry", count))
	}

	time.AfterFunc(wsReconnectDelay, func() {
		if err := c.Connect(); err != nil {
			if c.logger != nil {
				c.logger.Error("重连失败", zap.Error(err))
			}
			c.reconnect()
		}
	})
}
