package g

import (
	"errors"
	"strconv"
	"time"

	"github.com/sony/sonyflake/v2"
)

var (
	sf          *sonyflake.Sonyflake
	sfStartTime = time.Date(2025, 1, 1, 1, 1, 1, 1, time.UTC)
)

func InitSonyflake() error {
	_sf, err := sonyflake.New(sonyflake.Settings{
		StartTime: sfStartTime,
	})
	if err != nil {
		return err
	}
	sf = _sf

	return nil
}

func NextID() (int64, error) {
	if sf == nil {
		return 0, errors.New("sonyflake not initialized")
	}
	id, err := sf.NextID()
	return id, err
}

// NextIDStr 生成ID并用0~9A~Z的36进制压缩为短字符串
func NextIDStr() (string, error) {
	id, err := NextID()
	if err != nil {
		return "", err
	}
	return strconv.FormatInt(id, 36), nil
}
