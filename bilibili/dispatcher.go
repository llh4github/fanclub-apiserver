package bilibili

import (
	"encoding/json"
	"fmt"
	"strings"
	"sync/atomic"
	"time"

	"fanclub-apiserver/g"

	"go.uber.org/zap"
)

// Handler 弹幕命令处理器接口
type Handler interface {
	// SupportedCmd 返回处理器支持的命令类型
	SupportedCmd() string
	// Handle 处理命令
	Handle(cmd Command, roomID int64) error
}

// shouldIgnoreCmds 应当忽略的命令类型
var shouldIgnoreCmds = map[string]struct{}{
	"ENTRY_EFFECT":                 {},
	"USER_TOAST_MSG":               {},
	"ROOM_SKIN_MSG":                {},
	"COMBO_SEND":                   {},
	"VOICE_JOIN_ROOM_COUNT_INFO":   {},
	"VOICE_JOIN_LIST":              {},
	"WIDGET_GIFT_STAR_PROCESS":     {},
	"WIDGET_GIFT_STAR_PROCESS_V2":  {},
	"MESSAGEBOX_USER_MEDAL_CHANGE": {},
	"WIDGET_BANNER":                {},
	"WATCHED_CHANGE":               {},
	"NOTICE_MSG":                   {},
	"RANK_CHANGED_V2":              {},
	"DM_INTERACTION":               {},
	"INTERACT_WORD_V2":             {},
	"LIVE_PANEL_CHANGE_CONTENT":    {},
	"LIKE_INFO_V3_CLICK":           {},
	"LIKE_INFO_V3_UPDATE":          {},
	"INTERACT_WORD":                {},
	"ONLINE_RANK_V3":               {},
	"ONLINE_RANK_COUNT":            {},
	"TRADING_SCORE":                {},
	"COMMON_NOTICE_DANMAKU":        {},
	"STOP_LIVE_ROOM_LIST":          {},
	"RANK_CHANGED":                 {},
	"POPULAR_RANK_CHANGED":         {},
	"_HEARTBEAT":                   {},
	"SEND_GIFT":                    {},
	"SUPER_CHAT_MESSAGE_JPN":       {},
}

// cmdParsers 命令类型到解析工厂的映射
var cmdParsers = map[string]func() Command{
	"DANMU_MSG":                     func() Command { return &DanmuMsg{} },
	"SUPER_CHAT_MESSAGE":            func() Command { return &SuperChat{} },
	"GUARD_BUY":                     func() Command { return &GuardBuy{} },
	"LIVE":                          func() Command { return &Live{} },
	"ONLINE_RANK_COUNT":             func() Command { return &OnlineRankCount{} },
	"ROOM_REAL_TIME_MESSAGE_UPDATE": func() Command { return &RoomRealTimeMsgUpdate{} },
	"PREPARING":                     func() Command { return &Preparing{} },
	"USER_TOAST_MSG_V2":             func() Command { return &UserToastV2{} },
}

// ParseCommand 解析JSON字符串为具体的Command对象
// 返回 nil 表示该命令应被忽略或无法识别
func ParseCommand(jsonStr string) Command {
	var raw map[string]any
	if err := json.Unmarshal([]byte(jsonStr), &raw); err != nil {
		return nil
	}

	cmdVal, _ := raw["cmd"].(string)
	if cmdVal == "" {
		return nil
	}

	// 处理带冒号的cmd，如 "DANMU_MSG:4:0:2:2:2:0"
	cmd := strings.SplitN(cmdVal, ":", 2)[0]

	if _, ignore := shouldIgnoreCmds[cmd]; ignore {
		return nil
	}

	parser, ok := cmdParsers[cmd]
	if !ok {
		return nil
	}

	target := parser()
	if err := json.Unmarshal([]byte(jsonStr), target); err != nil {
		g.Error("解析命令失败", zap.String("jsonStr", jsonStr), zap.Error(err))
		return nil
	}

	return target
}

// dispatchTask 异步分发任务
type dispatchTask struct {
	handler Handler
	cmd     Command
	roomID  int64
}

// dispatchChan 异步分发通道
var dispatchChan = make(chan dispatchTask, 256)

// dispatchStats 分发统计
var dispatchStats struct {
	total     int64 // 总处理数
	dropped   int64 // 丢弃数
	chanLenHi int   // 通道使用量峰值
}

func init() {
	go dispatchWorker()
	go dispatchStatsReporter()
}

// dispatchWorker 消费通道中的任务，异步执行处理器
func dispatchWorker() {
	for task := range dispatchChan {
		start := time.Now()
		err := task.handler.Handle(task.cmd, task.roomID)
		elapsed := time.Since(start)

		// 记录处理耗时，超过 200ms 视为慢处理
		if elapsed > 200*time.Millisecond {
			g.Warn("命令处理耗时过长",
				zap.String("cmd", task.cmd.CmdName()),
				zap.Int64("room_id", task.roomID),
				zap.Duration("elapsed", elapsed),
			)
		}

		if err != nil {
			g.Error("异步处理命令失败",
				zap.String("cmd", task.cmd.CmdName()),
				zap.Int64("room_id", task.roomID),
				zap.Error(err),
			)
		}

		// 更新统计
		curLen := len(dispatchChan)
		if curLen > dispatchStats.chanLenHi {
			dispatchStats.chanLenHi = curLen
		}
		atomic.AddInt64(&dispatchStats.total, 1)
	}
}

// dispatchStatsReporter 定期输出分发统计，帮助判断是否需要增加 worker
func dispatchStatsReporter() {
	ticker := time.NewTicker(5 * time.Minute)
	for range ticker.C {
		chanLen := len(dispatchChan)
		chanCap := cap(dispatchChan)
		total := atomic.LoadInt64(&dispatchStats.total)
		dropped := atomic.LoadInt64(&dispatchStats.dropped)

		g.Info("分发通道统计",
			zap.Int64("total", total),
			zap.Int64("dropped", dropped),
			zap.Int("chan_len", chanLen),
			zap.Int("chan_cap", chanCap),
			zap.Int("chan_peak", dispatchStats.chanLenHi),
		)

		// 重置峰值
		dispatchStats.chanLenHi = chanLen
	}
}

// Dispatcher 弹幕命令分发器
type Dispatcher struct {
	handlers map[string]Handler
	logger   *zap.Logger
}

// NewDispatcher 创建分发器
func NewDispatcher(handlers ...Handler) *Dispatcher {
	h := make(map[string]Handler, len(handlers))
	for _, handler := range handlers {
		h[handler.SupportedCmd()] = handler
	}
	return &Dispatcher{
		handlers: h,
		logger:   g.Named("bilibili.dispatcher"),
	}
}

// Register 注册命令处理器
func (d *Dispatcher) Register(handler Handler) {
	d.handlers[handler.SupportedCmd()] = handler
}

// Dispatch 异步分发命令到对应处理器，避免阻塞消息读取循环
// 返回 error 和是否匹配到 handler 的 ok 值
func (d *Dispatcher) Dispatch(cmd Command, roomID int64) (bool, error) {
	handler, ok := d.handlers[cmd.CmdName()]
	if !ok {
		return false, nil
	}

	// 异步写入通道，满时丢弃并记录警告
	select {
	case dispatchChan <- dispatchTask{handler: handler, cmd: cmd, roomID: roomID}:
	default:
		atomic.AddInt64(&dispatchStats.dropped, 1)
		g.Warn("命令分发通道已满，丢弃消息",
			zap.String("cmd", cmd.CmdName()),
			zap.Int64("room_id", roomID),
			zap.Int("chan_len", len(dispatchChan)),
		)
	}

	return true, nil
}

// Process 批量解析并分发弹幕消息
func (d *Dispatcher) Process(messages []*DanmuMessage, roomID int64) error {
	for _, msg := range messages {
		cmd := ParseCommand(string(msg.RawData))
		if cmd == nil {
			continue
		}
		if ok, _ := d.Dispatch(cmd, roomID); !ok {
			continue
		}
	}
	return nil
}

// ParseMessages 批量解析弹幕消息，返回解析成功的Command列表
func ParseMessages(messages []*DanmuMessage) []Command {
	cmds := make([]Command, 0, len(messages))
	for _, msg := range messages {
		if cmd := ParseCommand(string(msg.RawData)); cmd != nil {
			cmds = append(cmds, cmd)
		}
	}
	return cmds
}

// RegisterCmd 注册自定义命令类型解析器
func RegisterCmd(name string, factory func() Command) error {
	if _, exists := cmdParsers[name]; exists {
		return fmt.Errorf("命令类型已注册: %s", name)
	}
	cmdParsers[name] = factory
	return nil
}
