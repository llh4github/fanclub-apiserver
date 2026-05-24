package bilibili

import (
	"encoding/json"
	"time"
)

// Command 弹幕命令接口，所有具体命令都实现此接口
type Command interface {
	CmdName() string
}

// baseCmd 命令基础结构
type baseCmd struct {
	Cmd string `json:"cmd"`
}

func (b baseCmd) CmdName() string { return b.Cmd }

// --- 弹幕消息 DANMU_MSG ---

// DanmuMsg 弹幕消息
type DanmuMsg struct {
	baseCmd
	Info []any  `json:"info,omitempty"`
	DmV2 string `json:"dm_v2,omitempty"`
}

// Content 获取弹幕内容（info[1]）
func (d *DanmuMsg) Content() string {
	if len(d.Info) < 2 {
		return ""
	}
	s, _ := d.Info[1].(string)
	return s
}

// Username 获取用户名（info[2][1]）
func (d *DanmuMsg) Username() string {
	if len(d.Info) < 3 {
		return ""
	}
	arr, ok := d.Info[2].([]any)
	if !ok || len(arr) < 2 {
		return ""
	}
	s, _ := arr[1].(string)
	return s
}

// UID 获取用户UID（info[2][0]）
func (d *DanmuMsg) UID() int64 {
	if len(d.Info) < 3 {
		return 0
	}
	arr, ok := d.Info[2].([]any)
	if !ok || len(arr) < 1 {
		return 0
	}
	return toInt64(arr[0])
}

// RoomID 获取目标房间ID（info[3][3]）
func (d *DanmuMsg) RoomID() int64 {
	if len(d.Info) < 4 {
		return 0
	}
	arr, ok := d.Info[3].([]any)
	if !ok || len(arr) < 4 {
		return 0
	}
	return toInt64(arr[3])
}

// AnchorUID 获取目标主播UID（info[3][12]）
func (d *DanmuMsg) AnchorUID() int64 {
	if len(d.Info) < 4 {
		return 0
	}
	arr, ok := d.Info[3].([]any)
	if !ok || len(arr) < 13 {
		return 0
	}
	return toInt64(arr[12])
}

// Timestamp 获取发送时间，单位秒（info[9]["ts"]），无法获取时返回当前时间
func (d *DanmuMsg) Timestamp() int64 {
	if len(d.Info) >= 10 {
		if m, ok := d.Info[9].(map[string]any); ok {
			if ts := toInt64(m["ts"]); ts > 0 {
				return ts
			}
		}
	}
	return time.Now().Unix()
}

// --- 礼物消息 SEND_GIFT ---

// SendGift 礼物消息
type SendGift struct {
	baseCmd
	Data  *GiftData  `json:"data,omitempty"`
	Danmu *DanmuInfo `json:"danmu,omitempty"`
}

// GiftData 礼物数据
type GiftData struct {
	Action            string `json:"action,omitempty"`
	BatchComboID      string `json:"batch_combo_id,omitempty"`
	CoinType          string `json:"coin_type,omitempty"`
	ComboTotalCoin    int    `json:"combo_total_coin,omitempty"`
	DiscountPrice     int    `json:"discount_price,omitempty"`
	GiftID            int64  `json:"giftId,omitempty"`
	GiftName          string `json:"giftName,omitempty"`
	GiftType          int    `json:"giftType,omitempty"`
	GuardLevel        int    `json:"guard_level,omitempty"`
	IsFirst           bool   `json:"is_first,omitempty"`
	Num               int    `json:"num,omitempty"`
	Price             int    `json:"price,omitempty"`
	SuperBatchGiftNum int    `json:"super_batch_gift_num,omitempty"`
	SuperGiftNum      int    `json:"super_gift_num,omitempty"`
	Timestamp         int64  `json:"timestamp,omitempty"`
	TotalCoin         int    `json:"total_coin,omitempty"`
	UID               int64  `json:"uid,omitempty"`
	Uname             string `json:"uname,omitempty"`
	WealthLevel       int    `json:"wealth_level,omitempty"`
}

// DanmuInfo 弹幕区域信息
type DanmuInfo struct {
	Area int `json:"area,omitempty"`
}

// --- 超级留言 SUPER_CHAT_MESSAGE ---

// SuperChat 超级留言
type SuperChat struct {
	baseCmd
	Data     *SuperChatData `json:"data,omitempty"`
	SendTime int64          `json:"send_time,omitempty"`
}

// SuperChatData 超级留言数据
type SuperChatData struct {
	ID       int64         `json:"id,omitempty"`
	Message  string        `json:"message,omitempty"`
	Price    int           `json:"price,omitempty"`
	Rate     int           `json:"rate,omitempty"`
	Ts       int64         `json:"ts,omitempty"`
	UID      int64         `json:"uid,omitempty"`
	Uinfo    *SCUserInfo   `json:"uinfo,omitempty"`
	UserInfo *SCUserSimple `json:"user_info,omitempty"`
	SendTime int64         `json:"send_time,omitempty"`
}

// SCUserInfo 超级留言用户详细信息
type SCUserInfo struct {
	Base  *SCUserBase  `json:"base,omitempty"`
	Guard *SCGuardInfo `json:"guard,omitempty"`
	UID   int64        `json:"uid,omitempty"`
}

// SCUserBase 超级留言用户基础信息
type SCUserBase struct {
	Face      string `json:"face,omitempty"`
	Name      string `json:"name,omitempty"`
	NameColor int    `json:"name_color,omitempty"`
}

// SCGuardInfo 大航海信息
type SCGuardInfo struct {
	ExpiredStr string `json:"expired_str,omitempty"`
	Level      int    `json:"level,omitempty"`
}

// SCUserSimple 超级留言用户简单信息
type SCUserSimple struct {
	GuardLevel int    `json:"guard_level,omitempty"`
	Title      string `json:"title,omitempty"`
	Uname      string `json:"uname,omitempty"`
	UserLevel  int    `json:"user_level,omitempty"`
}

// --- 日语醒目留言 SUPER_CHAT_MESSAGE_JPN ---

// SuperChatJpn 日语醒目留言
type SuperChatJpn struct {
	baseCmd
	Data *SuperChatJpnData `json:"data,omitempty"`
}

// SuperChatJpnData 日语醒目留言数据
type SuperChatJpnData struct {
	ID         int64     `json:"id,omitempty"`
	UID        int64     `json:"uid,omitempty"`
	Price      int       `json:"price,omitempty"`
	Rate       int       `json:"rate,omitempty"`
	Message    string    `json:"message,omitempty"`
	MessageJpn string    `json:"message_jpn,omitempty"`
	Ts         int64     `json:"ts,omitempty"`
	Token      string    `json:"token,omitempty"`
	MedalInfo  *JpnMedal `json:"medal_info,omitempty"`
	Time       int       `json:"time,omitempty"`
	StartTime  int64     `json:"start_time,omitempty"`
	EndTime    int64     `json:"end_time,omitempty"`
}

// JpnMedal 粉丝牌信息
type JpnMedal struct {
	TargetID     int64  `json:"target_id,omitempty"`
	AnchorRoomid int64  `json:"anchor_roomid,omitempty"`
	MedalLevel   int    `json:"medal_level,omitempty"`
	MedalName    string `json:"medal_name,omitempty"`
}

// --- 舰长购买 GUARD_BUY ---

// GuardBuy 舰长购买
type GuardBuy struct {
	baseCmd
	Data *GuardBuyData `json:"data,omitempty"`
}

// GuardBuyData 舰长购买数据
type GuardBuyData struct {
	UID        int64  `json:"uid,omitempty"`
	Username   string `json:"username,omitempty"`
	GuardLevel int    `json:"guard_level,omitempty"` // 1:总督, 2:提督, 3:舰长
	Num        int    `json:"num,omitempty"`
	Price      int64  `json:"price,omitempty"`
	GiftID     int64  `json:"gift_id,omitempty"`
	GiftName   string `json:"gift_name,omitempty"`
	StartTime  int64  `json:"start_time,omitempty"`
	EndTime    int64  `json:"end_time,omitempty"`
}

// --- 直播开始 LIVE ---

// Live 直播开始
type Live struct {
	baseCmd
	LiveKey         string   `json:"live_key,omitempty"`
	VoiceBackground string   `json:"voice_background,omitempty"`
	SubSessionKey   string   `json:"sub_session_key,omitempty"`
	LivePlatform    string   `json:"live_platform,omitempty"`
	LiveModel       int      `json:"live_model,omitempty"`
	RoomID          int64    `json:"roomid,omitempty"`
	LiveTime        int64    `json:"live_time,omitempty"`
	SpecialTypes    []string `json:"special_types,omitempty"`
}

// --- 同接数 ONLINE_RANK_COUNT ---

// OnlineRankCount 同接数
type OnlineRankCount struct {
	baseCmd
	Data *OnlineRankCountData `json:"data,omitempty"`
}

// OnlineRankCountData 同接数数据
type OnlineRankCountData struct {
	Count           int    `json:"count,omitempty"`
	CountText       string `json:"count_text,omitempty"`
	OnlineCount     int    `json:"online_count,omitempty"`
	OnlineCountText string `json:"online_count_text,omitempty"`
}

// --- 房间实时消息更新 ROOM_REAL_TIME_MESSAGE_UPDATE ---

// RoomRealTimeMsgUpdate 房间实时消息更新
type RoomRealTimeMsgUpdate struct {
	baseCmd
	Data *RoomRealTimeData `json:"data,omitempty"`
}

// RoomRealTimeData 房间实时消息数据
type RoomRealTimeData struct {
	RoomID    int64 `json:"roomid,omitempty"`
	Fans      int   `json:"fans,omitempty"`
	RedNotice int   `json:"red_notice,omitempty"` // -1:无通知
	FansClub  int   `json:"fans_club,omitempty"`
}

// --- 直播准备 PREPARING ---

// Preparing 直播准备
type Preparing struct {
	baseCmd
	MsgID    string `json:"msg_id,omitempty"`
	PIsAck   bool   `json:"p_is_ack,omitempty"`
	PMsgType int    `json:"p_msg_type,omitempty"`
	RoomID   int64  `json:"roomid,string,omitempty"`
	SendTime int64  `json:"send_time,omitempty"`
}

// --- 用户开通大航海V2 USER_TOAST_MSG_V2 ---

// UserToastV2 用户开通大航海V2
type UserToastV2 struct {
	baseCmd
	Data     *UserToastV2Data `json:"data,omitempty"`
	MsgID    string           `json:"msg_id,omitempty"`
	SendTime int64            `json:"send_time,omitempty"`
}

// UserToastV2Data 用户大航海V2数据
type UserToastV2Data struct {
	GuardInfo   *ToastGuard  `json:"guard_info,omitempty"`
	PayInfo     *ToastPay    `json:"pay_info,omitempty"`
	SenderUinfo *ToastSender `json:"sender_uinfo,omitempty"`
	ToastMsg    string       `json:"toast_msg,omitempty"`
}

// ToastGuard 大航海信息
type ToastGuard struct {
	EndTime        int64  `json:"end_time,omitempty"`
	GuardLevel     int    `json:"guard_level,omitempty"`
	OpType         int    `json:"op_type,omitempty"`
	RoleName       string `json:"role_name,omitempty"`
	RoomGuardCount int    `json:"room_guard_count,omitempty"`
	StartTime      int64  `json:"start_time,omitempty"`
}

// ToastPay 支付信息
type ToastPay struct {
	Num       int    `json:"num,omitempty"`
	PayflowID string `json:"payflow_id,omitempty"`
	Price     int64  `json:"price,omitempty"`
	Unit      string `json:"unit,omitempty"`
}

// ToastSender 发送者用户信息
type ToastSender struct {
	Base *ToastUserBase `json:"base,omitempty"`
	UID  int64          `json:"uid,omitempty"`
}

// ToastUserBase 用户基础信息
type ToastUserBase struct {
	Name string `json:"name,omitempty"`
}

// toInt64 将any类型安全转换为int64
func toInt64(v any) int64 {
	switch n := v.(type) {
	case int64:
		return n
	case int:
		return int64(n)
	case float64:
		return int64(n)
	case json.Number:
		i, _ := n.Int64()
		return i
	default:
		return 0
	}
}
