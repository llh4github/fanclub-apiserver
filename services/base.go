package services

import (
	"fanclub-apiserver/errs"
	"fanclub-apiserver/g"
	"time"
)

// defaultCacheTTL  services层的默认缓存过期时间
const defaultCacheTTL = 1 * time.Hour

// getBidForQuery 根据角色获取用于查询的 BID
//
// 管理员返回 0（不添加 BID 筛选条件）
// 主播检查绑定信息，未绑定返回错误，已绑定返回 BID
//
// Parameters:
//   - appCtx: 应用上下文
//
// Returns:
//   - int64: BID（0表示不筛选，>0表示添加筛选条件）
//   - error: 错误信息
func getBidForQuery(appCtx *g.AppCtx) (int64, error) {
	if appCtx.Role.IsAdmin() {
		return 0, nil
	}
	if appCtx.Role.IsAnchor() {
		if appCtx.BID == nil {
			return 0, errs.NoBindAnchorError
		}
		return *appCtx.BID, nil
	}
	return 0, errs.WrapError(nil, "无权限访问", string(errs.PermissionDenied))
}
