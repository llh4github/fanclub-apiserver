package services

import (
	"errors"
	"time"

	"fanclub-apiserver/database/generated"
	"fanclub-apiserver/database/model"
	"fanclub-apiserver/dto/req"
	"fanclub-apiserver/dto/resp"
	"fanclub-apiserver/errs"
	"fanclub-apiserver/g"
	"fanclub-apiserver/utils"

	"go.uber.org/zap"
	"gorm.io/cli/gorm/typed"
	"gorm.io/gorm/clause"
)

var Auth = new(authService)

type authService struct {
}

type userLogin struct {
	ID       int64
	Username string
	Password string
	Role     string
	Bid      int64
	RoomID   int64
}

// Login 登录
func (s *authService) Login(appCtx *g.AppCtx, loginReq *req.Login) (*resp.Login, error) {
	if loginReq.Username == "" || loginReq.Password == "" {
		return nil, errors.New("username and password are required")
	}

	var user userLogin
	q := typed.G[model.SysUser](g.DB)
	if err := q.
		Select(
			generated.BaseModel.ID.WithTable("sys_user"),
			generated.SysUser.Username,
			generated.SysUser.Password,
			generated.SysUser.Role,
			generated.AnchorInfo.Bid,
			generated.AnchorInfo.RoomID,
		).
		Where(generated.SysUser.Username.Eq(loginReq.Username)).
		Joins(clause.LeftJoin.Association("Anchor"), func(_ typed.JoinBuilder, _ clause.Table, _ clause.Table) error {
			return nil
		}).
		Scan(appCtx.C, &user); err != nil {
		g.Debug("用户名不存在", zap.String("Username", loginReq.Username))
		return nil, errs.LoginError
	}

	if user.ID == 0 {
		g.Debug("用户名不存在", zap.String("Username", loginReq.Username))
		return nil, errs.LoginError
	}

	if !utils.CheckPasswordHash(loginReq.Password, user.Password) {
		return nil, errs.LoginError
	}

	userInfo := &g.UserInfoJwt{
		UserID: user.ID,
		Role:   user.Role,
		BID:    user.Bid,
		RoomID: user.RoomID,
	}

	sid, err := g.NextIDStr()
	if err != nil {
		return nil, err
	}
	userInfo.Sid = sid

	tokens, err := JWT.GenerateTokens(appCtx.C, userInfo)
	if err != nil {
		return nil, err
	}

	if err := s.updateLastLoginTime(appCtx, user.ID); err != nil {
		g.Warn("更新最后登录时间失败", zap.Int64("user_id", user.ID), zap.Error(err))
	}

	return &resp.Login{
		ID:             user.ID,
		Username:       user.Username,
		AccessToken:    tokens.AccessToken,
		RefreshToken:   tokens.RefreshToken,
		ExpirationTime: tokens.ExpirationTime,
	}, nil
}

// RefreshToken 刷新令牌
func (s *authService) RefreshToken(appCtx *g.AppCtx, refreshToken string) (*resp.Login, error) {
	if refreshToken == "" {
		return nil, errors.New("refresh token is required")
	}

	claims, err := g.ValidateRefreshToken(refreshToken)
	if err != nil {
		g.Debug("刷新令牌验证失败", zap.Error(err))
		return nil, errs.WrapError(err, "令牌无效或已过期", string(errs.TokenRefreshFailed))
	}

	_, err = JWT.ValidateToken(appCtx.C, refreshToken)
	if err != nil {
		g.Debug("刷新令牌未在Redis中找到", zap.Error(err))
		return nil, errs.WrapError(err, "令牌已被撤销", string(errs.TokenRefreshFailed))
	}

	userInfo := &g.UserInfoJwt{
		UserID: claims.UserID,
		Role:   claims.Role,
		BID:    claims.BID,
		RoomID: claims.RoomID,
		Sid:    claims.Sid,
	}

	if err := JWT.InvalidateTokensBySid(appCtx.C, userInfo.UserID, userInfo.Sid); err != nil {
		return nil, errs.WrapError(err, "旧令牌作废失败", string(errs.TokenRefreshFailed))
	}

	tokens, err := JWT.GenerateTokens(appCtx.C, userInfo)
	if err != nil {
		return nil, errs.WrapError(err, "生成新令牌失败", string(errs.TokenRefreshFailed))
	}

	if err := s.updateLastLoginTime(appCtx, userInfo.UserID); err != nil {
		g.Warn("更新最后登录时间失败", zap.Int64("user_id", userInfo.UserID), zap.Error(err))
	}

	return &resp.Login{
		ID:             userInfo.UserID,
		AccessToken:    tokens.AccessToken,
		RefreshToken:   tokens.RefreshToken,
		ExpirationTime: tokens.ExpirationTime,
	}, nil
}

// updateLastLoginTime 更新用户最后登录时间
func (s *authService) updateLastLoginTime(appCtx *g.AppCtx, userID int64) error {
	updates := model.SysUser{LastLoginTime: new(time.Time)}
	*updates.LastLoginTime = time.Now()
	return g.DB.WithContext(appCtx.C).Model(&model.SysUser{}).
		Where(generated.BaseModel.ID.Eq(userID)).
		Update("last_login_time", updates.LastLoginTime).
		Error
}
