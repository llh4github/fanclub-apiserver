package model

import (
	"time"

	"gorm.io/gorm"
)

// NextIDFunc 生成 ID 的函数类型
type NextIDFunc func() (int64, error)

// GenerateID 全局 ID 生成函数，由 g 包初始化时设置
var GenerateID NextIDFunc

// CreatorModifier 创建者和修改者信息
type CreatorModifier struct {
	// 创建者
	Creator *SysUser `json:"creator,omitempty" gorm:"foreignKey:CreatedByID;references:ID;constraint:OnUpdate:CASCADE,OnDelete:SET NULL;"`
	// 创建者ID
	CreatedByID *int64 `json:"creator_id,string" gorm:"column:creator_id"`
	// 修改者
	Modifier *SysUser `json:"modifier,omitempty" gorm:"foreignKey:ModifierID;references:ID;constraint:OnUpdate:CASCADE,OnDelete:SET NULL;"`
	// 修改者ID
	ModifierID *int64 `json:"modifier_id,string" gorm:"column:modifier_id"`
}

// BaseModel 基础模型，包含主键和时间字段
type BaseModel struct {
	// 主键
	ID int64 `json:"id,string" gorm:"primaryKey;autoIncrement:false"`
	// 创建时间
	CreatedTime time.Time `json:"created_time" gorm:"autoCreateTime:milli"`
	// 更新时间
	UpdatedTime time.Time `json:"updated_time" gorm:"autoUpdateTime:milli"`
}

// BeforeCreate GORM hook，在创建前生成分布式 ID
func (m *BaseModel) BeforeCreate(tx *gorm.DB) error {
	if m.ID == 0 && GenerateID != nil {
		id, err := GenerateID()
		if err != nil {
			return err
		}
		m.ID = id
	}
	return nil
}

// Models 返回所有需要迁移的数据库模型列表
func Models() []any {
	return []any{
		&SysUser{},
		&AnchorInfo{},
		&AnchorSong{},
		&AnchorFollowerNum{},
		&AnchorLiveRecord{},
		&AnchorLiveSchedule{},
		&SysScraperCookie{},
		&SysScraperFeature{},
		&ViewerScBvRecord{},
		&TreeholeTopic{},
		&TreeholeSubmission{},
		&TreeholeSubmissionSummary{},
		&OssUploadCallback{},
	}
}
