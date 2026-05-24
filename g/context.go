package g

import (
	"context"
	"fanclub-apiserver/consts"
	"fanclub-apiserver/errs"

	"github.com/gofiber/fiber/v3"
)

// AppCtx 应用上下文
type AppCtx struct {
	C       context.Context // 标准库上下文
	IsLogin bool            // 是否已登录
	UserID  int64           // 用户ID , 未登录时此值为0
	Role    consts.RoleType // 用户角色
	BID     *int64          // B站UID
	RoomID  *int64          // 直播间ID
}

// GetAnchorInfo 获取主播角色的 BID 和 RoomID
// 如果用户不是主播角色或未绑定主播信息，返回错误
func (ctx *AppCtx) GetAnchorInfo() (bid int64, roomID int64, err error) {
	if !ctx.Role.IsAnchor() {
		return 0, 0, errs.WrapError(nil, "当前用户不是主播角色", string(errs.AuthFailed))
	}
	if ctx.BID == nil {
		return 0, 0, errs.NoBindAnchorError
	}
	if ctx.RoomID == nil {
		return 0, 0, errs.WrapError(nil, "未绑定直播间ID", string(errs.AuthFailed))
	}
	return *ctx.BID, *ctx.RoomID, nil
}

// FromFiberCtx 从 fiber.Ctx 转换为 AppCtx
// 若无法获取 claims（未登录），则标记为未登录，其余字段为零值
func FromFiberCtx(c fiber.Ctx) *AppCtx {
	appCtx := &AppCtx{
		C:       c.Context(),
		IsLogin: false,
	}

	claims, ok := c.Locals("claims").(*Claims)
	if !ok || claims == nil {
		return appCtx
	}

	appCtx.IsLogin = true
	appCtx.UserID = claims.UserID
	appCtx.Role = consts.ConvertRoleType(claims.Role)
	if claims.BID != 0 {
		appCtx.BID = &claims.BID
	}
	if claims.RoomID != 0 {
		appCtx.RoomID = &claims.RoomID
	}

	return appCtx
}
