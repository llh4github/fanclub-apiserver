package consts

// AuditStatus 树洞投稿审核状态
type AuditStatus int

// 树洞投稿审核状态常量
const (
	// AuditStatusHidden 不宜展示（审核未通过）
	AuditStatusHidden AuditStatus = 0
	// AuditStatusPending 未审核（待审核）
	AuditStatusPending AuditStatus = 1
	// AuditStatusApproved 可以展示（审核通过）
	AuditStatusApproved AuditStatus = 2
)

// IsPending 是否处于待审核状态
func (a AuditStatus) IsPending() bool {
	return a == AuditStatusPending
}

// IsApproved 是否已审核通过
func (a AuditStatus) IsApproved() bool {
	return a == AuditStatusApproved
}

// IsHidden 是否不宜展示（审核未通过）
func (a AuditStatus) IsHidden() bool {
	return a == AuditStatusHidden
}
