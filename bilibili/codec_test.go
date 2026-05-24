package bilibili

import (
	"bytes"
	"compress/flate"
	"encoding/binary"
	"encoding/json"
	"testing"

	"github.com/andybalholm/brotli"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestMakePacket_MakeAndParse(t *testing.T) {
	data := []byte(`{"cmd":"TEST","data":123}`)
	pkt := MakePacket(data, OpSendMsg)

	assert.Equal(t, headerSize+len(data), len(pkt))

	header, err := ParseHeader(pkt[:headerSize])
	require.NoError(t, err)

	assert.Equal(t, int32(headerSize+len(data)), header.PackLen)
	assert.Equal(t, int16(headerSize), header.HeaderSize)
	assert.Equal(t, ProtoVerNormal, header.Ver)
	assert.Equal(t, OpSendMsg, header.Operation)
	assert.Equal(t, int32(1), header.SeqID)
	assert.Equal(t, data, pkt[headerSize:])
}

func TestMakePacket_Heartbeat(t *testing.T) {
	pkt := MakePacket(nil, OpHeartbeat)
	assert.Equal(t, headerSize, len(pkt))

	header, err := ParseHeader(pkt)
	require.NoError(t, err)
	assert.Equal(t, OpHeartbeat, header.Operation)
	assert.Equal(t, int32(headerSize), header.PackLen)
}

func TestParseHeader_TooShort(t *testing.T) {
	_, err := ParseHeader([]byte{0, 0, 0, 0, 0, 0, 0, 0})
	assert.ErrorIs(t, err, ErrHeaderTooShort)
}

func TestParsePacket_HeartbeatReply(t *testing.T) {
	popularity := int32(12345)
	body := make([]byte, 4)
	binary.BigEndian.PutUint32(body, uint32(popularity))

	buf := buildPacket(body, ProtoVerNormal, OpHeartbeatReply)

	msgs, err := ParsePacket(buf)
	require.NoError(t, err)
	require.Len(t, msgs, 1)

	assert.Equal(t, "_HEARTBEAT", msgs[0].Cmd)
	var hb HeartbeatMessage
	require.NoError(t, json.Unmarshal(msgs[0].RawData, &hb))
	assert.Equal(t, int32(12345), hb.Popularity)
}

func TestParsePacket_NormalMessage(t *testing.T) {
	jsonData := `{"cmd":"DANMU_MSG","data":"hello"}`
	pkt := MakePacket([]byte(jsonData), OpSendMsgReply)

	msgs, err := ParsePacket(pkt)
	require.NoError(t, err)
	require.Len(t, msgs, 1)

	assert.Equal(t, "DANMU_MSG", msgs[0].Cmd)
	assert.Equal(t, jsonData, string(msgs[0].RawData))
}

func TestParsePacket_DeflateMessage(t *testing.T) {
	innerJSON := `{"cmd":"SEND_GIFT","data":{"gift":"flower"}}`
	innerPkt := MakePacket([]byte(innerJSON), OpSendMsgReply)

	var compressed bytes.Buffer
	writer, _ := flate.NewWriter(&compressed, flate.DefaultCompression)
	writer.Write(innerPkt)
	writer.Close()

	buf := buildPacket(compressed.Bytes(), ProtoVerDeflate, OpSendMsgReply)

	msgs, err := ParsePacket(buf)
	require.NoError(t, err)
	require.Len(t, msgs, 1)

	assert.Equal(t, "SEND_GIFT", msgs[0].Cmd)
	assert.Contains(t, string(msgs[0].RawData), "flower")
}

func TestParsePacket_BrotliMessage(t *testing.T) {
	innerJSON := `{"cmd":"GUARD_BUY","data":{"gift":"guard"}}`
	innerPkt := MakePacket([]byte(innerJSON), OpSendMsgReply)

	var compressed bytes.Buffer
	brotliWriter := brotli.NewWriter(&compressed)
	brotliWriter.Write(innerPkt)
	brotliWriter.Close()

	buf := buildPacket(compressed.Bytes(), ProtoVerBrotli, OpSendMsgReply)

	msgs, err := ParsePacket(buf)
	require.NoError(t, err)
	require.Len(t, msgs, 1)

	assert.Equal(t, "GUARD_BUY", msgs[0].Cmd)
	assert.Contains(t, string(msgs[0].RawData), "guard")
}

func TestParsePacket_InvalidJSON(t *testing.T) {
	invalidJSON := `not a json`
	pkt := MakePacket([]byte(invalidJSON), OpSendMsgReply)

	msgs, err := ParsePacket(pkt)
	require.NoError(t, err)
	require.Len(t, msgs, 1)

	assert.Equal(t, "UNKNOWN", msgs[0].Cmd)
	assert.Equal(t, invalidJSON, string(msgs[0].RawData))
}

func TestParsePacket_MultiplePackets(t *testing.T) {
	json1 := `{"cmd":"CMD1","data":1}`
	json2 := `{"cmd":"CMD2","data":2}`

	pkt1 := MakePacket([]byte(json1), OpSendMsgReply)
	pkt2 := MakePacket([]byte(json2), OpSendMsgReply)

	msgs, err := ParsePacket(append(pkt1, pkt2...))
	require.NoError(t, err)
	require.Len(t, msgs, 2)

	assert.Equal(t, "CMD1", msgs[0].Cmd)
	assert.Equal(t, "CMD2", msgs[1].Cmd)
}

func TestParsePacket_EmptyBody(t *testing.T) {
	pkt := MakePacket([]byte{}, OpSendMsgReply)

	msgs, err := ParsePacket(pkt)
	require.NoError(t, err)
	assert.Empty(t, msgs)
}

func TestParsePacket_DeflateMultipleInner(t *testing.T) {
	json1 := `{"cmd":"WELCOME","data":"user1"}`
	json2 := `{"cmd":"ENTRY_EFFECT","data":"effect1"}`

	innerPkt1 := MakePacket([]byte(json1), OpSendMsgReply)
	innerPkt2 := MakePacket([]byte(json2), OpSendMsgReply)
	innerCombined := append(innerPkt1, innerPkt2...)

	var compressed bytes.Buffer
	writer, _ := flate.NewWriter(&compressed, flate.DefaultCompression)
	writer.Write(innerCombined)
	writer.Close()

	buf := buildPacket(compressed.Bytes(), ProtoVerDeflate, OpSendMsgReply)

	msgs, err := ParsePacket(buf)
	require.NoError(t, err)
	require.Len(t, msgs, 2)

	assert.Equal(t, "WELCOME", msgs[0].Cmd)
	assert.Equal(t, "ENTRY_EFFECT", msgs[1].Cmd)
}

func TestMakePacket_RoundTrip(t *testing.T) {
	tests := []struct {
		name string
		data []byte
		op   WsOperation
	}{
		{"心跳包", nil, OpHeartbeat},
		{"认证包", []byte(`{"uid":0,"roomid":12345}`), OpAuth},
		{"发送消息", []byte(`{"cmd":"TEST"}`), OpSendMsg},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			pkt := MakePacket(tt.data, tt.op)

			header, err := ParseHeader(pkt[:headerSize])
			require.NoError(t, err)
			assert.Equal(t, tt.op, header.Operation)
			assert.Equal(t, int32(len(pkt)), header.PackLen)

			if tt.data != nil {
				assert.Equal(t, tt.data, pkt[headerSize:])
			}
		})
	}
}

func TestProtoVer_String(t *testing.T) {
	assert.Equal(t, "normal", ProtoVerNormal.String())
	assert.Equal(t, "heartbeat", ProtoVerHeartbeat.String())
	assert.Equal(t, "deflate", ProtoVerDeflate.String())
	assert.Equal(t, "brotli", ProtoVerBrotli.String())
	assert.Equal(t, "unknown(99)", ProtoVer(99).String())
}

func TestWsOperation_String(t *testing.T) {
	assert.Equal(t, "heartbeat", OpHeartbeat.String())
	assert.Equal(t, "auth", OpAuth.String())
	assert.Equal(t, "unknown(99)", WsOperation(99).String())
}

func TestParseJSONMessage_ValidJSON(t *testing.T) {
	body := []byte(`{"cmd":"DANMU_MSG","info":[]}`)
	msg, err := parseJSONMessage(body)
	require.NoError(t, err)
	assert.Equal(t, "DANMU_MSG", msg.Cmd)
}

func TestParseJSONMessage_NoCmdField(t *testing.T) {
	body := []byte(`{"data":"hello"}`)
	msg, err := parseJSONMessage(body)
	require.NoError(t, err)
	assert.Equal(t, "", msg.Cmd)
}

func Test_deflateRoundTrip(t *testing.T) {
	original := []byte(`{"cmd":"TEST","data":{"key":"value"}}`)

	var compressed bytes.Buffer
	writer, _ := flate.NewWriter(&compressed, flate.DefaultCompression)
	_, err := writer.Write(original)
	require.NoError(t, err)
	require.NoError(t, writer.Close())

	decompressed, err := decompressDeflate(compressed.Bytes())
	require.NoError(t, err)
	assert.Equal(t, original, decompressed)
}

func Test_brotliRoundTrip(t *testing.T) {
	original := []byte(`{"cmd":"TEST","data":{"key":"value"}}`)

	var compressed bytes.Buffer
	brotliWriter := brotli.NewWriter(&compressed)
	_, err := brotliWriter.Write(original)
	require.NoError(t, err)
	require.NoError(t, brotliWriter.Close())

	decompressed, err := decompressBrotli(compressed.Bytes())
	require.NoError(t, err)
	assert.Equal(t, original, decompressed)
}

func TestDanmuMessageJSON(t *testing.T) {
	msg := &DanmuMessage{Cmd: "DANMU_MSG", RawData: json.RawMessage(`{"cmd":"DANMU_MSG"}`)}

	data, err := json.Marshal(msg)
	require.NoError(t, err)
	assert.Contains(t, string(data), `"cmd"`)
	assert.Contains(t, string(data), `"rawData"`)

	var parsed DanmuMessage
	require.NoError(t, json.Unmarshal(data, &parsed))
	assert.Equal(t, msg.Cmd, parsed.Cmd)
	assert.Equal(t, string(msg.RawData), string(parsed.RawData))
}

// buildPacket 构造指定协议版本的数据包
func buildPacket(body []byte, ver ProtoVer, op WsOperation) []byte {
	packLen := int32(headerSize + len(body))
	buf := make([]byte, headerSize+len(body))

	binary.BigEndian.PutUint32(buf[0:4], uint32(packLen))
	binary.BigEndian.PutUint16(buf[4:6], uint16(headerSize))
	binary.BigEndian.PutUint16(buf[6:8], uint16(ver))
	binary.BigEndian.PutUint32(buf[8:12], uint32(op))
	binary.BigEndian.PutUint32(buf[12:16], 1)
	copy(buf[headerSize:], body)

	return buf
}
