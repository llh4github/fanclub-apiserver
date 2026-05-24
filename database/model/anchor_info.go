package model

// AnchorInfo 主播基础信息
type AnchorInfo struct {
	BaseModel
	// B 站 ID，通常称为 UID
	Bid int64 `json:"bid" gorm:"not null"`
	// B 站昵称
	BiliName string `json:"bili_name" gorm:"type:varchar(255);not null"`
	// 直播间 ID
	RoomID int64 `json:"room_id" gorm:"not null"`
	// 关联的系统用户 ID（外键）
	UserID *int64 `json:"user_id" gorm:"column:user_id"`
	// 关联的系统用户
	User *SysUser `json:"user,omitempty" gorm:"foreignKey:UserID"`
}

// TableName 指定表名
func (AnchorInfo) TableName() string {
	return "anchor_info"
}
