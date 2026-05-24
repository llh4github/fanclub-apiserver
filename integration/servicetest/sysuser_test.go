package servicetest

import (
	"testing"

	"fanclub-apiserver/consts"
	"fanclub-apiserver/database/model"
	"fanclub-apiserver/errs"
	"fanclub-apiserver/g"
	"fanclub-apiserver/integration/testenv"
	"fanclub-apiserver/services"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"gorm.io/cli/gorm/typed"
)

func TestSysUserQueryWithAnchorInfo(t *testing.T) {
	env, err := testenv.GetTestEnvironment()
	if err != nil {
		t.Fatalf("Failed to create test environment: %v", err)
	}
	defer env.Cleanup()

	userQ := typed.G[model.SysUser](g.DB)
	anchorQ := typed.G[model.AnchorInfo](g.DB)

	t.Run("QueryUserWithAnchorInfo_HasAnchor", func(t *testing.T) {
		user := &model.SysUser{
			Username: "test_user",
			Nickname: "Test User",
			Password: "password123",
			Role:     "anchor",
		}
		err := userQ.Create(env.Ctx, user)
		require.NoError(t, err)

		anchor := &model.AnchorInfo{
			Bid:      12345678,
			BiliName: "test_anchor",
			RoomID:   123456,
			UserID:   &user.ID,
		}
		err = anchorQ.Create(env.Ctx, anchor)
		require.NoError(t, err)

		results, err := services.SysUser.QueryUserWithAnchorInfo(env.Ctx)
		assert.NoError(t, err)
		assert.Len(t, results, 1)

		result := results[0]
		assert.Equal(t, user.ID, result.ID)
		assert.Equal(t, user.Username, result.Username)
		assert.Equal(t, user.Role, result.Role)
		assert.NotNil(t, result.BID)
		assert.Equal(t, anchor.Bid, *result.BID)
		assert.NotNil(t, result.RoomID)
		assert.Equal(t, anchor.RoomID, *result.RoomID)
	})

	t.Run("QueryUserWithAnchorInfo_NoAnchor", func(t *testing.T) {
		user := &model.SysUser{
			Username: "no_anchor_user",
			Nickname: "No Anchor",
			Password: "password123",
			Role:     "admin",
		}
		err := userQ.Create(env.Ctx, user)
		require.NoError(t, err)

		results, err := services.SysUser.QueryUserWithAnchorInfo(env.Ctx)
		assert.NoError(t, err)
		assert.Len(t, results, 2)

		var noAnchorResult *services.UserAnchorInfo
		for _, r := range results {
			if r.Username == "no_anchor_user" {
				noAnchorResult = r
				break
			}
		}
		assert.NotNil(t, noAnchorResult)
		assert.Equal(t, user.ID, noAnchorResult.ID)
		assert.Equal(t, user.Username, noAnchorResult.Username)
		assert.Equal(t, user.Role, noAnchorResult.Role)
		assert.Nil(t, noAnchorResult.BID)
		assert.Nil(t, noAnchorResult.RoomID)
	})
}

func TestSysUserUpdatePassword(t *testing.T) {
	env, err := testenv.GetTestEnvironment()
	if err != nil {
		t.Fatalf("Failed to create test environment: %v", err)
	}
	defer env.Cleanup()

	userQ := typed.G[model.SysUser](g.DB)

	t.Run("UpdatePassword_PermissionDenied_AnchorModifyOthers", func(t *testing.T) {
		user := &model.SysUser{
			Username: "anchor_user",
			Nickname: "Anchor User",
			Password: "old_password",
			Role:     string(consts.RoleAnchor),
		}
		err := userQ.Create(env.Ctx, user)
		require.NoError(t, err)

		targetUser := &model.SysUser{
			Username: "target_user",
			Nickname: "Target User",
			Password: "old_password",
			Role:     string(consts.RoleAnchor),
		}
		err = userQ.Create(env.Ctx, targetUser)
		require.NoError(t, err)

		appCtx := &g.AppCtx{C: env.Ctx, UserID: user.ID, Role: consts.RoleAnchor}

		err = services.SysUser.UpdatePassword(appCtx, targetUser.ID, "new_password")

		require.Error(t, err)
		assert.Equal(t, errs.PermissionDeniedError, err)
	})

	t.Run("UpdatePassword_PermissionDenied_GuestModifyOthers", func(t *testing.T) {
		user := &model.SysUser{
			Username: "guest_user",
			Nickname: "Guest User",
			Password: "old_password",
			Role:     string(consts.RoleGuest),
		}
		err := userQ.Create(env.Ctx, user)
		require.NoError(t, err)

		targetUser := &model.SysUser{
			Username: "target_user2",
			Nickname: "Target User 2",
			Password: "old_password",
			Role:     string(consts.RoleAnchor),
		}
		err = userQ.Create(env.Ctx, targetUser)
		require.NoError(t, err)

		appCtx := &g.AppCtx{C: env.Ctx, UserID: user.ID, Role: consts.RoleGuest}

		err = services.SysUser.UpdatePassword(appCtx, targetUser.ID, "new_password")

		require.Error(t, err)
		assert.Equal(t, errs.PermissionDeniedError, err)
	})

	t.Run("UpdatePassword_AdminCanModifySelf", func(t *testing.T) {
		admin := &model.SysUser{
			Username: "admin_user",
			Nickname: "Admin User",
			Password: "old_password",
			Role:     string(consts.RoleAdmin),
		}
		err := userQ.Create(env.Ctx, admin)
		require.NoError(t, err)

		appCtx := &g.AppCtx{C: env.Ctx, UserID: admin.ID, Role: consts.RoleAdmin}

		err = services.SysUser.UpdatePassword(appCtx, admin.ID, "new_password")

		require.NoError(t, err)
	})

	t.Run("UpdatePassword_UserNotFound", func(t *testing.T) {
		admin := &model.SysUser{
			Username: "admin_for_notfound",
			Nickname: "Admin For NotFound",
			Password: "old_password",
			Role:     string(consts.RoleAdmin),
		}
		err := userQ.Create(env.Ctx, admin)
		require.NoError(t, err)

		appCtx := &g.AppCtx{C: env.Ctx, UserID: admin.ID, Role: consts.RoleAdmin}

		err = services.SysUser.UpdatePassword(appCtx, 999999, "new_password")

		require.Error(t, err)
		assert.Equal(t, errs.UserNotFoundError, err)
	})
}
