package g

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func setupJWTConfig() {
	// 初始化 JWT 配置
	Cfg = &Config{
		JWT: JWTConfig{
			SecretKey:              "test-secret-key",
			AccessTokenExpiration:  60,            // 60分钟
			RefreshTokenExpiration: 10080,         // 7天
			Issuer:                 "test-issuer", // 测试用token发行者
		},
	}
}

func TestGenerateToken(t *testing.T) {
	// 初始化配置
	setupJWTConfig()

	// 测试参数
	userInfo := &UserInfoJwt{
		UserID: 123456,
		Role:   "admin",
		BID:    654321,
		RoomID: 111222,
	}

	// 生成 token
	tokenString, tokenID, err := GenerateToken(userInfo)

	// 验证结果
	assert.NoError(t, err, "GenerateToken should not return error")
	assert.NotEmpty(t, tokenString, "GenerateToken should return non-empty token")
	assert.NotEmpty(t, tokenID, "GenerateToken should return non-empty tokenID")
}

func TestValidateToken(t *testing.T) {
	// 初始化配置
	setupJWTConfig()

	// 测试参数
	userInfo := &UserInfoJwt{
		UserID: 123456,
		Role:   "admin",
		BID:    654321,
		RoomID: 111222,
	}

	// 生成 token
	tokenString, _, err := GenerateToken(userInfo)
	assert.NoError(t, err, "GenerateToken should not return error")

	// 验证 token
	claims, err := ValidateToken(tokenString)

	// 验证结果
	assert.NoError(t, err, "ValidateToken should not return error")
	assert.NotNil(t, claims, "ValidateToken should return non-nil claims")
	assert.Equal(t, "access", claims.Type, "Type in claims should be access")
	assert.Equal(t, "123456", claims.Subject, "Subject in claims should be userID")
}

func TestGetUserIDFromToken(t *testing.T) {
	// 初始化配置
	setupJWTConfig()

	// 测试参数
	userInfo := &UserInfoJwt{
		UserID: 123456,
		Role:   "admin",
		BID:    654321,
		RoomID: 111222,
	}

	// 生成 token
	tokenString, _, err := GenerateToken(userInfo)
	assert.NoError(t, err, "GenerateToken should not return error")

	// 从 token 中获取用户 ID
	resultUserID, err := GetUserIDFromToken(tokenString)

	// 验证结果
	assert.NoError(t, err, "GetUserIDFromToken should not return error")
	assert.Equal(t, userInfo.UserID, resultUserID, "UserID should match")
}

func TestGetUserInfoFromToken(t *testing.T) {
	// 初始化配置
	setupJWTConfig()

	// 测试参数
	userInfo := &UserInfoJwt{
		UserID: 123456,
		Role:   "admin",
		BID:    654321,
		RoomID: 111222,
	}

	// 生成 token
	tokenString, _, err := GenerateToken(userInfo)
	assert.NoError(t, err, "GenerateToken should not return error")

	// 从 token 中获取用户信息
	resultUserInfo, err := GetUserInfoFromToken(tokenString)

	// 验证结果
	assert.NoError(t, err, "GetUserInfoFromToken should not return error")
	assert.NotNil(t, resultUserInfo, "GetUserInfoFromToken should return non-nil userInfo")
	assert.Equal(t, userInfo.UserID, resultUserInfo.UserID, "UserID should match")
	assert.Equal(t, userInfo.Role, resultUserInfo.Role, "Role should match")
	assert.Equal(t, userInfo.BID, resultUserInfo.BID, "BID should match")
	assert.Equal(t, userInfo.RoomID, resultUserInfo.RoomID, "RoomID should match")
}

func TestValidateToken_InvalidToken(t *testing.T) {
	// 初始化配置
	setupJWTConfig()

	// 测试无效 token
	invalidToken := "invalid-token"

	// 验证 token
	claims, err := ValidateToken(invalidToken)

	// 验证结果
	assert.Error(t, err, "ValidateToken should return error for invalid token")
	assert.Nil(t, claims, "ValidateToken should return nil claims for invalid token")
}

func TestGetUserIDFromToken_InvalidToken(t *testing.T) {
	// 初始化配置
	setupJWTConfig()

	// 测试无效 token
	invalidToken := "invalid-token"

	// 从 token 中获取用户 ID
	userID, err := GetUserIDFromToken(invalidToken)

	// 验证结果
	assert.Error(t, err, "GetUserIDFromToken should return error for invalid token")
	assert.Equal(t, int64(0), userID, "GetUserIDFromToken should return 0 for invalid token")
}

func TestGenerateRefreshToken(t *testing.T) {
	// 初始化配置
	setupJWTConfig()

	// 测试参数
	userInfo := &UserInfoJwt{
		UserID: 123456,
		Role:   "admin",
		BID:    654321,
		RoomID: 111222,
	}

	// 生成刷新 token
	tokenString, tokenID, err := GenerateRefreshToken(userInfo)

	// 验证结果
	assert.NoError(t, err, "GenerateRefreshToken should not return error")
	assert.NotEmpty(t, tokenString, "GenerateRefreshToken should return non-empty token")
	assert.NotEmpty(t, tokenID, "GenerateRefreshToken should return non-empty tokenID")
}

func TestValidateRefreshToken(t *testing.T) {
	// 初始化配置
	setupJWTConfig()

	// 测试参数
	userInfo := &UserInfoJwt{
		UserID: 123456,
		Role:   "admin",
		BID:    654321,
		RoomID: 111222,
	}

	// 生成刷新 token
	tokenString, _, err := GenerateRefreshToken(userInfo)
	assert.NoError(t, err, "GenerateRefreshToken should not return error")

	// 验证刷新 token
	claims, err := ValidateRefreshToken(tokenString)

	// 验证结果
	assert.NoError(t, err, "ValidateRefreshToken should not return error")
	assert.NotNil(t, claims, "ValidateRefreshToken should return non-nil claims")
	assert.Equal(t, "refresh", claims.Type, "Type in claims should be refresh")
	assert.Equal(t, "123456", claims.Subject, "Subject in claims should be userID")
}

func TestValidateRefreshToken_InvalidIssuer(t *testing.T) {
	// 初始化配置
	setupJWTConfig()

	// 测试参数
	userInfo := &UserInfoJwt{
		UserID: 123456,
		Role:   "admin",
		BID:    654321,
		RoomID: 111222,
	}

	// 生成访问 token（不是刷新 token）
	tokenString, _, err := GenerateToken(userInfo)
	assert.NoError(t, err, "GenerateToken should not return error")

	// 尝试用刷新 token 的验证方法来验证访问 token
	claims, err := ValidateRefreshToken(tokenString)

	// 验证结果
	assert.Error(t, err, "ValidateRefreshToken should return error for access token")
	assert.Nil(t, claims, "ValidateRefreshToken should return nil claims for access token")
}

func TestValidateRefreshToken_InvalidToken(t *testing.T) {
	// 初始化配置
	setupJWTConfig()

	// 测试无效 token
	invalidToken := "invalid-token"

	// 验证刷新 token
	claims, err := ValidateRefreshToken(invalidToken)

	// 验证结果
	assert.Error(t, err, "ValidateRefreshToken should return error for invalid token")
	assert.Nil(t, claims, "ValidateRefreshToken should return nil claims for invalid token")
}
