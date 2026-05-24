package services

import (
	"context"

	"fanclub-apiserver/consts"
	"fanclub-apiserver/database/generated"
	"fanclub-apiserver/database/model"
	"fanclub-apiserver/errs"
	"fanclub-apiserver/g"
	"fanclub-apiserver/utils"

	"go.uber.org/zap"
	"gorm.io/cli/gorm/typed"
	"gorm.io/gorm/clause"
)

var SysUser = new(sysUserService)

type sysUserService struct {
}

// UserAnchorInfo 用户与主播信息的组合
type UserAnchorInfo struct {
	ID       int64  `json:"id"`
	Username string `json:"username"`
	Role     string `json:"role"`
	BID      *int64 `json:"bid"`
	RoomID   *int64 `json:"room_id"`
}

// QueryUserWithAnchorInfo 查询用户及其关联的主播信息
func (s *sysUserService) QueryUserWithAnchorInfo(ctx context.Context) ([]*UserAnchorInfo, error) {
	q := typed.G[model.SysUser](g.DB)
	var results []*model.SysUser
	if err := q.
		Select(
			generated.BaseModel.ID,
			generated.SysUser.Username,
			generated.SysUser.Role,
			generated.AnchorInfo.Bid,
			generated.AnchorInfo.RoomID,
		).
		Joins(clause.LeftJoin.Association("Anchor"), func(_ typed.JoinBuilder, _ clause.Table, _ clause.Table) error {
			return nil
		}).
		Scan(ctx, &results); err != nil {
		return nil, err
	}

	infos := make([]*UserAnchorInfo, 0, len(results))
	for _, user := range results {
		var bid, roomID *int64
		if user.Anchor != nil {
			bid = &user.Anchor.Bid
			roomID = &user.Anchor.RoomID
		}
		infos = append(infos, &UserAnchorInfo{
			ID:       user.ID,
			Username: user.Username,
			Role:     user.Role,
			BID:      bid,
			RoomID:   roomID,
		})
	}

	return infos, nil
}

// UpdatePassword 修改用户密码
//
// Parameters:
//   - appCtx: 应用上下文（包含操作人ID和角色）
//   - targetUserID: 目标用户ID
//   - newPasswordPlain: 新密码原文（AES解密后）
//
// Returns:
//   - error: 错误信息
func (s *sysUserService) UpdatePassword(appCtx *g.AppCtx, targetUserID int64, newPasswordPlain string) error {
	if appCtx.Role != consts.RoleAdmin && appCtx.UserID != targetUserID {
		g.Warn("权限不足：非管理员试图修改他人密码",
			zap.Int64("operatorID", appCtx.UserID),
			zap.Int64("targetUserID", targetUserID),
			zap.String("operatorRole", string(appCtx.Role)))
		return errs.PermissionDeniedError
	}

	q := typed.G[model.SysUser](g.DB)
	var user struct {
		ID       int64
		Password string
		Username string
	}
	if err := q.
		Select(
			generated.BaseModel.ID,
			generated.SysUser.Password,
			generated.SysUser.Username,
		).
		Where(generated.BaseModel.ID.Eq(targetUserID)).
		Scan(appCtx.C, &user); err != nil || user.ID == 0 {
		g.Warn("用户不存在", zap.Int64("userID", targetUserID))
		return errs.UserNotFoundError
	}

	newHashedPassword, err := utils.HashPassword(newPasswordPlain)
	if err != nil {
		g.Error("密码哈希失败", zap.Error(err), zap.Int64("userID", targetUserID))
		return errs.PasswordUpdateErr
	}

	if err := g.DB.WithContext(appCtx.C).
		Model(&model.SysUser{}).
		Where(generated.BaseModel.ID.Eq(targetUserID)).
		Update("password", newHashedPassword).Error; err != nil {
		g.Error("密码更新失败",
			zap.Error(err),
			zap.Int64("userID", targetUserID),
			zap.Int64("operatorID", appCtx.UserID))
		return errs.PasswordUpdateErr
	}

	if err := JWT.InvalidateTokens(appCtx.C, targetUserID); err != nil {
		g.Error("清除用户token缓存失败",
			zap.Error(err),
			zap.Int64("userID", targetUserID))
	}

	g.Info("密码修改成功",
		zap.Int64("userID", targetUserID),
		zap.String("username", user.Username),
		zap.Int64("operatorID", appCtx.UserID),
		zap.String("operatorRole", string(appCtx.Role)))

	return nil
}
