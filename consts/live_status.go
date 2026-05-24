package consts

import (
	"database/sql/driver"
	"fmt"
)

// LiveRecordStatus 直播记录数据状态
type LiveRecordStatus int

const (
	// 未知
	UNKNOWN LiveRecordStatus = 0
	// 直播中
	LIVING LiveRecordStatus = 1
	// 正常结束直播
	END_LIVING LiveRecordStatus = 2
	// 超时系统结束直播
	OVER_TIME_END LiveRecordStatus = 3
)

// Scan 实现 sql.Scanner 接口，将数据库值扫描到结构体
func (s *LiveRecordStatus) Scan(value interface{}) error {
	if value == nil {
		*s = UNKNOWN
		return nil
	}

	var intVal int64
	switch v := value.(type) {
	case int64:
		intVal = v
	case int32:
		intVal = int64(v)
	case int:
		intVal = int64(v)
	case []byte:
		fmt.Sscanf(string(v), "%d", &intVal)
	default:
		return fmt.Errorf("cannot scan type %T into LiveRecordStatus", value)
	}

	*s = LiveRecordStatus(intVal)
	return nil
}

// Value 实现 driver.Valuer 接口，将结构体值写入数据库
func (s LiveRecordStatus) Value() (driver.Value, error) {
	return int64(s), nil
}

// String 返回状态的文字描述
func (s LiveRecordStatus) String() string {
	switch s {
	case UNKNOWN:
		return "未知"
	case LIVING:
		return "直播中"
	case END_LIVING:
		return "已结束"
	case OVER_TIME_END:
		return "超时结束"
	default:
		return "未知"
	}
}
