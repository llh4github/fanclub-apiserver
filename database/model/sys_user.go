package model

import "time"

// SysUser 系统用户
type SysUser struct {
	BaseModel
	// 用户名
	Username string `json:"username" gorm:"type:varchar(60);uniqueIndex;not null"`
	// 昵称
	Nickname string `json:"nickname" gorm:"type:varchar(60);default:''"`
	// 密码
	Password string `json:"password" gorm:"type:varchar(60);not null"`
	// 用户所属角色
	Role string `json:"role" gorm:"type:varchar(10);not null"`
	// 最后登录时间
	LastLoginTime *time.Time `json:"last_login_time" gorm:"autoUpdateTime:milli"`
	// 关联的主播信息（一对一）
	Anchor *AnchorInfo `json:"anchor,omitempty" gorm:"foreignKey:UserID"`

	CreatorModifier
}

// TableName 指定表名
func (SysUser) TableName() string {
	return "sys_user"
}
