package bilibili

import (
	"encoding/json"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestParseCommand_DanmuMsg(t *testing.T) {
	jsonStr := `{"cmd":"DANMU_MSG","info":[[0,1,25,5566168,1671985429,0,0,"","","",0,0,0,"",0,0,0],"hello world",[456789,"username",0,0,0,0,10000,1,""]]}`
	cmd := ParseCommand(jsonStr)
	require.NotNil(t, cmd)

	danmu, ok := cmd.(*DanmuMsg)
	require.True(t, ok)
	assert.Equal(t, "DANMU_MSG", danmu.CmdName())
	assert.Equal(t, "hello world", danmu.Content())
	assert.Equal(t, "username", danmu.Username())
	assert.Equal(t, int64(456789), danmu.UID())
}

func TestParseCommand_SendGift(t *testing.T) {
	jsonStr := `{"cmd":"SEND_GIFT","data":{"action":"送出","batch_combo_id":"","coin_type":"gold","combo_total_coin":0,"discount_price":0,"giftId":1,"giftName":"辣条","giftType":1,"guard_level":0,"is_first":true,"num":1,"price":100,"super_batch_gift_num":0,"super_gift_num":0,"timestamp":1671985429,"total_coin":100,"uid":123456,"uname":"testuser","wealth_level":0}}`
	cmd := ParseCommand(jsonStr)
	require.NotNil(t, cmd)

	gift, ok := cmd.(*SendGift)
	require.True(t, ok)
	assert.Equal(t, "SEND_GIFT", gift.CmdName())
	require.NotNil(t, gift.Data)
	assert.Equal(t, "辣条", gift.Data.GiftName)
	assert.Equal(t, int64(123456), gift.Data.UID)
	assert.Equal(t, "testuser", gift.Data.Uname)
	assert.Equal(t, 1, gift.Data.Num)
	assert.Equal(t, 100, gift.Data.Price)
}

func TestParseCommand_SuperChat(t *testing.T) {
	jsonStr := `{"cmd":"SUPER_CHAT_MESSAGE","data":{"id":12345,"message":"你好","price":30,"rate":1000,"ts":1671985429,"uid":789012,"uinfo":{"base":{"name":"scuser","face":"http://example.com/face.jpg"},"guard":{"level":1},"uid":789012}},"send_time":1671985430000}`
	cmd := ParseCommand(jsonStr)
	require.NotNil(t, cmd)

	sc, ok := cmd.(*SuperChat)
	require.True(t, ok)
	assert.Equal(t, "SUPER_CHAT_MESSAGE", sc.CmdName())
	require.NotNil(t, sc.Data)
	assert.Equal(t, int64(12345), sc.Data.ID)
	assert.Equal(t, "你好", sc.Data.Message)
	assert.Equal(t, 30, sc.Data.Price)
	require.NotNil(t, sc.Data.Uinfo)
	require.NotNil(t, sc.Data.Uinfo.Base)
	assert.Equal(t, "scuser", sc.Data.Uinfo.Base.Name)
}

func TestParseCommand_GuardBuy(t *testing.T) {
	jsonStr := `{"cmd":"GUARD_BUY","data":{"uid":111,"username":"guarduser","guard_level":3,"num":1,"price":198000,"gift_id":100034,"gift_name":"舰长","start_time":1671985429,"end_time":1674577429}}`
	cmd := ParseCommand(jsonStr)
	require.NotNil(t, cmd)

	guard, ok := cmd.(*GuardBuy)
	require.True(t, ok)
	assert.Equal(t, "GUARD_BUY", guard.CmdName())
	require.NotNil(t, guard.Data)
	assert.Equal(t, int64(111), guard.Data.UID)
	assert.Equal(t, "guarduser", guard.Data.Username)
	assert.Equal(t, 3, guard.Data.GuardLevel)
	assert.Equal(t, "舰长", guard.Data.GiftName)
}

func TestParseCommand_Live(t *testing.T) {
	jsonStr := `{"cmd":"LIVE","live_key":"live_key_123","voice_background":"","sub_session_key":"sub_key","live_platform":"web","live_model":0,"roomid":12345,"live_time":1671985429}`
	cmd := ParseCommand(jsonStr)
	require.NotNil(t, cmd)

	live, ok := cmd.(*Live)
	require.True(t, ok)
	assert.Equal(t, "LIVE", live.CmdName())
	assert.Equal(t, "live_key_123", live.LiveKey)
	assert.Equal(t, int64(12345), live.RoomID)
}

func TestParseCommand_OnlineRankCount(t *testing.T) {
	jsonStr := `{"cmd":"ONLINE_RANK_COUNT","data":{"count":1234,"count_text":"1234","online_count":5678,"online_count_text":"5678"}}`
	cmd := ParseCommand(jsonStr)
	require.NotNil(t, cmd)

	rank, ok := cmd.(*OnlineRankCount)
	require.True(t, ok)
	assert.Equal(t, "ONLINE_RANK_COUNT", rank.CmdName())
	require.NotNil(t, rank.Data)
	assert.Equal(t, 1234, rank.Data.Count)
	assert.Equal(t, 5678, rank.Data.OnlineCount)
}

func TestParseCommand_RoomRealTimeMsgUpdate(t *testing.T) {
	jsonStr := `{"cmd":"ROOM_REAL_TIME_MESSAGE_UPDATE","data":{"roomid":12345,"fans":100000,"red_notice":-1,"fans_club":5000}}`
	cmd := ParseCommand(jsonStr)
	require.NotNil(t, cmd)

	update, ok := cmd.(*RoomRealTimeMsgUpdate)
	require.True(t, ok)
	assert.Equal(t, "ROOM_REAL_TIME_MESSAGE_UPDATE", update.CmdName())
	require.NotNil(t, update.Data)
	assert.Equal(t, int64(12345), update.Data.RoomID)
	assert.Equal(t, 100000, update.Data.Fans)
	assert.Equal(t, 5000, update.Data.FansClub)
}

func TestParseCommand_Preparing(t *testing.T) {
	jsonStr := `{"cmd":"PREPARING","msg_id":"msg123","p_is_ack":true,"p_msg_type":1,"roomid":12345,"send_time":1671985429000}`
	cmd := ParseCommand(jsonStr)
	require.NotNil(t, cmd)

	prep, ok := cmd.(*Preparing)
	require.True(t, ok)
	assert.Equal(t, "PREPARING", prep.CmdName())
	assert.Equal(t, "msg123", prep.MsgID)
	assert.Equal(t, int64(12345), prep.RoomID)
}

// TestParseCommand_PreparingReal 用户提供的真实 PREPARING 数据
// roomid 为字符串类型，需要能被正确解析
func TestParseCommand_PreparingReal(t *testing.T) {
	jsonStr := `{"cmd":"PREPARING","msg_id":"89987847643274240:1000:1000","p_is_ack":true,"p_msg_type":1,"roomid":"1713548468","send_time":1774372979086}`
	cmd := ParseCommand(jsonStr)
	require.NotNil(t, cmd, "PREPARING 命令解析失败，检查 roomid 字段类型是否兼容字符串")

	prep, ok := cmd.(*Preparing)
	require.True(t, ok, "类型断言失败")
	assert.Equal(t, "PREPARING", prep.CmdName())
	assert.Equal(t, "89987847643274240:1000:1000", prep.MsgID)
	assert.True(t, prep.PIsAck)
	assert.Equal(t, 1, prep.PMsgType)
	assert.Equal(t, int64(1713548468), prep.RoomID, "roomid 字符串应能被正确解析为 int64")
	assert.Equal(t, int64(1774372979086), prep.SendTime)
}

func TestParseCommand_UserToastV2(t *testing.T) {
	jsonStr := `{"cmd":"USER_TOAST_MSG_V2","data":{"guard_info":{"end_time":1674577429,"guard_level":3,"role_name":"舰长","room_guard_count":100,"start_time":1671985429},"pay_info":{"num":1,"price":198000,"unit":"月"},"sender_uinfo":{"base":{"name":"toastuser"},"uid":222}},"msg_id":"toast_msg_1","send_time":1671985429000}`
	cmd := ParseCommand(jsonStr)
	require.NotNil(t, cmd)

	toast, ok := cmd.(*UserToastV2)
	require.True(t, ok)
	assert.Equal(t, "USER_TOAST_MSG_V2", toast.CmdName())
	require.NotNil(t, toast.Data)
	require.NotNil(t, toast.Data.GuardInfo)
	assert.Equal(t, 3, toast.Data.GuardInfo.GuardLevel)
	assert.Equal(t, "舰长", toast.Data.GuardInfo.RoleName)
	require.NotNil(t, toast.Data.SenderUinfo)
	assert.Equal(t, int64(222), toast.Data.SenderUinfo.UID)
}

func TestParseCommand_SuperChatJpn(t *testing.T) {
	jsonStr := `{"cmd":"SUPER_CHAT_MESSAGE_JPN","data":{"id":999,"uid":333,"price":50,"message":"こんにちは","message_jpn":"こんにちは","medal_info":{"target_id":444,"medal_level":20,"medal_name":"粉丝牌"},"time":60}}`
	cmd := ParseCommand(jsonStr)
	require.NotNil(t, cmd)

	sc, ok := cmd.(*SuperChatJpn)
	require.True(t, ok)
	assert.Equal(t, "SUPER_CHAT_MESSAGE_JPN", sc.CmdName())
	require.NotNil(t, sc.Data)
	assert.Equal(t, "こんにちは", sc.Data.Message)
	require.NotNil(t, sc.Data.MedalInfo)
	assert.Equal(t, "粉丝牌", sc.Data.MedalInfo.MedalName)
}

func TestParseCommand_IgnoreCmd(t *testing.T) {
	for _, jsonStr := range []string{
		`{"cmd":"ENTRY_EFFECT","data":{}}`,
		`{"cmd":"COMBO_SEND","data":{}}`,
		`{"cmd":"INTERACT_WORD","data":{}}`,
		`{"cmd":"_HEARTBEAT","data":{}}`,
	} {
		assert.Nil(t, ParseCommand(jsonStr), "应该忽略命令: %s", jsonStr)
	}
}

func TestParseCommand_UnknownCmd(t *testing.T) {
	assert.Nil(t, ParseCommand(`{"cmd":"SOME_UNKNOWN_CMD","data":{}}`))
}

func TestParseCommand_InvalidJSON(t *testing.T) {
	assert.Nil(t, ParseCommand("not a json"))
}

func TestParseCommand_NoCmdField(t *testing.T) {
	assert.Nil(t, ParseCommand(`{"data":"hello"}`))
}

func TestParseCommand_CmdWithSuffix(t *testing.T) {
	jsonStr := `{"cmd":"DANMU_MSG:4:0:2:2:2:0","info":[[0,1,25,0,0,0,0,"","","",0,0,0,"",0,0,0],"hello",[0,"",0,0,0,0,0,0,""]]}`
	cmd := ParseCommand(jsonStr)
	require.NotNil(t, cmd)
	danmu, ok := cmd.(*DanmuMsg)
	require.True(t, ok)
	assert.Equal(t, "hello", danmu.Content())
}

// --- 分发器测试 ---

// mockHandler 测试用模拟处理器
type mockHandler struct {
	cmdName   string
	lastCmd   Command
	lastRoomID int64
	callCount int
	err       error
}

func (m *mockHandler) SupportedCmd() string { return m.cmdName }
func (m *mockHandler) Handle(cmd Command, roomID int64) error {
	m.lastCmd = cmd
	m.lastRoomID = roomID
	m.callCount++
	return m.err
}

func TestDispatcher_Dispatch(t *testing.T) {
	danmuHandler := &mockHandler{cmdName: "DANMU_MSG"}
	giftHandler := &mockHandler{cmdName: "SEND_GIFT"}

	d := NewDispatcher(danmuHandler, giftHandler)

	danmu := &DanmuMsg{}
	danmu.Cmd = "DANMU_MSG"
	ok, _ := d.Dispatch(danmu, 12345)
	require.True(t, ok)

	assert.Equal(t, 1, danmuHandler.callCount)
	assert.Equal(t, int64(12345), danmuHandler.lastRoomID)
	assert.Equal(t, 0, giftHandler.callCount)

	gift := &SendGift{}
	gift.Cmd = "SEND_GIFT"
	ok, _ = d.Dispatch(gift, 67890)
	require.True(t, ok)

	assert.Equal(t, 1, giftHandler.callCount)
	assert.Equal(t, int64(67890), giftHandler.lastRoomID)
}

func TestDispatcher_UnhandledCmd(t *testing.T) {
	d := NewDispatcher()
	cmd := &DanmuMsg{}
	cmd.Cmd = "DANMU_MSG"
	ok, _ := d.Dispatch(cmd, 12345)
	require.False(t, ok)
}

func TestDispatcher_Register(t *testing.T) {
	d := NewDispatcher()
	handler := &mockHandler{cmdName: "GUARD_BUY"}
	d.Register(handler)

	guard := &GuardBuy{}
	guard.Cmd = "GUARD_BUY"
	ok, _ := d.Dispatch(guard, 12345)
	require.True(t, ok)

	assert.Equal(t, 1, handler.callCount)
}

func TestDispatcher_Process(t *testing.T) {
	danmuHandler := &mockHandler{cmdName: "DANMU_MSG"}
	giftHandler := &mockHandler{cmdName: "SEND_GIFT"}
	d := NewDispatcher(danmuHandler, giftHandler)

	messages := []*DanmuMessage{
		{Cmd: "DANMU_MSG", RawData: json.RawMessage(`{"cmd":"DANMU_MSG","info":[[],"hello",[]]}`)},
		{Cmd: "SEND_GIFT", RawData: json.RawMessage(`{"cmd":"SEND_GIFT","data":{"giftName":"辣条"}}`)},
		{Cmd: "ENTRY_EFFECT", RawData: json.RawMessage(`{"cmd":"ENTRY_EFFECT"}`)},
	}

	require.NoError(t, d.Process(messages, 12345))
	assert.Equal(t, 1, danmuHandler.callCount)
	assert.Equal(t, 1, giftHandler.callCount)
}

func TestDispatcher_ProcessError(t *testing.T) {
	danmuHandler := &mockHandler{cmdName: "DANMU_MSG", err: assert.AnError}
	d := NewDispatcher(danmuHandler)

	messages := []*DanmuMessage{
		{Cmd: "DANMU_MSG", RawData: json.RawMessage(`{"cmd":"DANMU_MSG","info":[]}`)},
	}

	err := d.Process(messages, 12345)
	require.Error(t, err)
	assert.Contains(t, err.Error(), "DANMU_MSG")
}

func TestParseMessages(t *testing.T) {
	messages := []*DanmuMessage{
		{Cmd: "DANMU_MSG", RawData: json.RawMessage(`{"cmd":"DANMU_MSG","info":[[],"hello",[]]}`)},
		{Cmd: "SEND_GIFT", RawData: json.RawMessage(`{"cmd":"SEND_GIFT","data":{"giftName":"辣条"}}`)},
		{Cmd: "ENTRY_EFFECT", RawData: json.RawMessage(`{"cmd":"ENTRY_EFFECT"}`)},
		{Cmd: "UNKNOWN_CMD", RawData: json.RawMessage(`{"cmd":"UNKNOWN_CMD"}`)},
	}

	cmds := ParseMessages(messages)
	require.Len(t, cmds, 2)
	assert.Equal(t, "DANMU_MSG", cmds[0].CmdName())
	assert.Equal(t, "SEND_GIFT", cmds[1].CmdName())
}

func TestRegisterCmd(t *testing.T) {
	type customCmd struct {
		baseCmd
		Data string `json:"data,omitempty"`
	}

	err := RegisterCmd("CUSTOM_CMD", func() Command { return &customCmd{} })
	require.NoError(t, err)

	jsonStr := `{"cmd":"CUSTOM_CMD","data":"custom_data"}`
	cmd := ParseCommand(jsonStr)
	require.NotNil(t, cmd)
	assert.Equal(t, "CUSTOM_CMD", cmd.CmdName())

	err = RegisterCmd("CUSTOM_CMD", func() Command { return &customCmd{} })
	assert.Error(t, err)
}

func TestCommandJSONRoundTrip(t *testing.T) {
	tests := []struct {
		name string
		cmd  Command
	}{
		{
			"GuardBuy",
			&GuardBuy{
				baseCmd: baseCmd{Cmd: "GUARD_BUY"},
				Data: &GuardBuyData{
					UID:        111,
					Username:   "test",
					GuardLevel: 3,
					Num:        1,
					Price:      198000,
					GiftName:   "舰长",
				},
			},
		},
		{
			"Live",
			&Live{
				baseCmd:  baseCmd{Cmd: "LIVE"},
				LiveKey:  "key123",
				RoomID:   12345,
				LiveTime: 1671985429,
			},
		},
		{
			"Preparing",
			&Preparing{
				baseCmd:  baseCmd{Cmd: "PREPARING"},
				MsgID:    "msg1",
				RoomID:   12345,
				SendTime: 1671985429000,
			},
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			data, err := json.Marshal(tt.cmd)
			require.NoError(t, err)

			parsed := ParseCommand(string(data))
			require.NotNil(t, parsed)
			assert.Equal(t, tt.cmd.CmdName(), parsed.CmdName())
		})
	}
}
