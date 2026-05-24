package bilibili

import (
	"bytes"
	"compress/flate"
	"encoding/binary"
	"encoding/json"
	"errors"
	"fmt"
	"io"

	"github.com/andybalholm/brotli"
)

const headerSize = 16

// 协议版本
type ProtoVer int16

const (
	ProtoVerNormal    ProtoVer = 0 // 普通文本
	ProtoVerHeartbeat ProtoVer = 1 // 心跳
	ProtoVerDeflate   ProtoVer = 2 // deflate压缩
	ProtoVerBrotli    ProtoVer = 3 // brotli压缩
)

func (v ProtoVer) String() string {
	switch v {
	case ProtoVerNormal:
		return "normal"
	case ProtoVerHeartbeat:
		return "heartbeat"
	case ProtoVerDeflate:
		return "deflate"
	case ProtoVerBrotli:
		return "brotli"
	default:
		return fmt.Sprintf("unknown(%d)", v)
	}
}

// WebSocket操作码
type WsOperation int32

const (
	OpHandshake       WsOperation = 0 // 握手
	OpHandshakeReply  WsOperation = 1 // 握手回复
	OpHeartbeat       WsOperation = 2 // 心跳
	OpHeartbeatReply  WsOperation = 3 // 心跳回复
	OpSendMsg         WsOperation = 4 // 发送消息
	OpSendMsgReply    WsOperation = 5 // 发送消息回复
	OpDisconnectReply WsOperation = 6 // 断开回复
	OpAuth            WsOperation = 7 // 认证
	OpAuthReply       WsOperation = 8 // 认证回复
)

func (op WsOperation) String() string {
	switch op {
	case OpHandshake:
		return "handshake"
	case OpHandshakeReply:
		return "handshake_reply"
	case OpHeartbeat:
		return "heartbeat"
	case OpHeartbeatReply:
		return "heartbeat_reply"
	case OpSendMsg:
		return "send_msg"
	case OpSendMsgReply:
		return "send_msg_reply"
	case OpDisconnectReply:
		return "disconnect_reply"
	case OpAuth:
		return "auth"
	case OpAuthReply:
		return "auth_reply"
	default:
		return fmt.Sprintf("unknown(%d)", op)
	}
}

// PacketHeader 数据包头部（16字节）
type PacketHeader struct {
	PackLen    int32       // 数据包总长度
	HeaderSize int16       // 头部大小
	Ver        ProtoVer    // 协议版本
	Operation  WsOperation // 操作码
	SeqID      int32       // 序列号
}

// 编解码错误
var (
	ErrHeaderTooShort = errors.New("data too short for packet header")
	ErrUnknownProto   = errors.New("unknown protocol version")
)

// MakePacket 构造数据包
func MakePacket(data []byte, op WsOperation) []byte {
	packLen := int32(headerSize + len(data))
	buf := make([]byte, headerSize+len(data))

	binary.BigEndian.PutUint32(buf[0:4], uint32(packLen))
	binary.BigEndian.PutUint16(buf[4:6], uint16(headerSize))
	binary.BigEndian.PutUint16(buf[6:8], uint16(ProtoVerNormal))
	binary.BigEndian.PutUint32(buf[8:12], uint32(op))
	binary.BigEndian.PutUint32(buf[12:16], 1)
	copy(buf[headerSize:], data)

	return buf
}

// ParseHeader 解析数据包头部
func ParseHeader(data []byte) (PacketHeader, error) {
	if len(data) < headerSize {
		return PacketHeader{}, fmt.Errorf("%w: got %d bytes, need %d", ErrHeaderTooShort, len(data), headerSize)
	}

	return PacketHeader{
		PackLen:    int32(binary.BigEndian.Uint32(data[0:4])),
		HeaderSize: int16(binary.BigEndian.Uint16(data[4:6])),
		Ver:        ProtoVer(int16(binary.BigEndian.Uint16(data[6:8]))),
		Operation:  WsOperation(int32(binary.BigEndian.Uint32(data[8:12]))),
		SeqID:      int32(binary.BigEndian.Uint32(data[12:16])),
	}, nil
}

// DanmuMessage 弹幕消息
type DanmuMessage struct {
	Cmd     string          `json:"cmd"`
	RawData json.RawMessage `json:"rawData"`
}

// HeartbeatMessage 心跳回复消息（人气值）
type HeartbeatMessage struct {
	Popularity int32 `json:"popularity"`
}

// ParsePacket 解析数据包，返回弹幕消息列表
func ParsePacket(packet []byte) ([]*DanmuMessage, error) {
	var messages []*DanmuMessage
	offset := 0

	for offset < len(packet) {
		if offset+headerSize > len(packet) {
			break
		}

		header, err := ParseHeader(packet[offset : offset+headerSize])
		if err != nil {
			break
		}

		bodyStart := offset + int(header.HeaderSize)
		bodyEnd := offset + int(header.PackLen)
		if bodyEnd > len(packet) {
			bodyEnd = len(packet)
		}
		body := packet[bodyStart:bodyEnd]

		switch header.Operation {
		case OpHeartbeatReply:
			if len(body) >= 4 {
				popularity := int32(binary.BigEndian.Uint32(body[0:4]))
				raw, _ := json.Marshal(&HeartbeatMessage{Popularity: popularity})
				messages = append(messages, &DanmuMessage{Cmd: "_HEARTBEAT", RawData: raw})
			}

		case OpSendMsgReply:
			subMsgs, err := parseMessageBody(header.Ver, body)
			if err != nil {
				offset += int(header.PackLen)
				continue
			}
			messages = append(messages, subMsgs...)
		}

		offset += int(header.PackLen)
	}

	return messages, nil
}

// parseMessageBody 根据协议版本解析消息体
func parseMessageBody(ver ProtoVer, body []byte) ([]*DanmuMessage, error) {
	switch ver {
	case ProtoVerDeflate:
		decompressed, err := decompressDeflate(body)
		if err != nil {
			return nil, err
		}
		return ParsePacket(decompressed)

	case ProtoVerNormal:
		if len(body) == 0 {
			return nil, nil
		}
		msg, err := parseJSONMessage(body)
		if err != nil {
			return nil, err
		}
		return []*DanmuMessage{msg}, nil

	case ProtoVerBrotli:
		decompressed, err := decompressBrotli(body)
		if err != nil {
			return nil, err
		}
		return ParsePacket(decompressed)

	case ProtoVerHeartbeat:
		return nil, nil

	default:
		return nil, fmt.Errorf("%w: %s", ErrUnknownProto, ver)
	}
}

// parseJSONMessage 从JSON字节数据中解析弹幕消息
func parseJSONMessage(body []byte) (*DanmuMessage, error) {
	var dj struct {
		Cmd string `json:"cmd"`
	}
	if err := json.Unmarshal(body, &dj); err != nil {
		return &DanmuMessage{Cmd: "UNKNOWN", RawData: json.RawMessage(body)}, nil
	}
	return &DanmuMessage{Cmd: dj.Cmd, RawData: json.RawMessage(body)}, nil
}

// decompressDeflate 解压deflate数据
func decompressDeflate(data []byte) ([]byte, error) {
	r := flate.NewReader(bytes.NewReader(data))
	defer r.Close()
	var buf bytes.Buffer
	if _, err := io.Copy(&buf, r); err != nil {
		return nil, fmt.Errorf("deflate decompress: %w", err)
	}
	return buf.Bytes(), nil
}

// decompressBrotli 解压brotli数据
func decompressBrotli(data []byte) ([]byte, error) {
	r := brotli.NewReader(bytes.NewReader(data))
	var buf bytes.Buffer
	if _, err := io.Copy(&buf, r); err != nil {
		return nil, fmt.Errorf("brotli decompress: %w", err)
	}
	return buf.Bytes(), nil
}
