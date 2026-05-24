package services

import (
	"context"
	"fmt"
	"time"

	"fanclub-apiserver/cache"
	"fanclub-apiserver/dto"
	"fanclub-apiserver/g"

	"go.uber.org/zap"
)

var JWT = new(jwtService)

type jwtService struct {
}

const (
	jwtKeyPrefix = cache.ServiceLayer + "jwt:"
)

func (s *jwtService) GenerateTokens(ctx context.Context, userInfo *g.UserInfoJwt) (*dto.TokenPair, error) {
	// 生成 access token
	accessToken, accessTokenID, err := g.GenerateToken(userInfo)
	if err != nil {
		return nil, fmt.Errorf("failed to generate access token: %w", err)
	}

	// 生成 refresh token
	refreshToken, refreshTokenID, err := g.GenerateRefreshToken(userInfo)
	if err != nil {
		return nil, fmt.Errorf("failed to generate refresh token: %w", err)
	}

	// 计算过期时间
	accessTTL := time.Duration(g.Cfg.JWT.AccessTokenExpiration) * time.Minute
	refreshTTL := time.Duration(g.Cfg.JWT.RefreshTokenExpiration) * time.Minute

	// 构建 Redis key: jwt:userID:sid:tokenID
	accessKey := fmt.Sprintf("%s%d:%s:%s", jwtKeyPrefix, userInfo.UserID, userInfo.Sid, accessTokenID)
	refreshKey := fmt.Sprintf("%s%d:%s:%s", jwtKeyPrefix, userInfo.UserID, userInfo.Sid, refreshTokenID)

	// 将 access token 存入 Redis
	if err := g.Redis.Set(ctx, accessKey, accessToken, accessTTL).Err(); err != nil {
		return nil, fmt.Errorf("failed to store access token in redis: %w", err)
	}

	// 将 refresh token 存入 Redis
	if err := g.Redis.Set(ctx, refreshKey, refreshToken, refreshTTL).Err(); err != nil {
		return nil, fmt.Errorf("failed to store refresh token in redis: %w", err)
	}

	return &dto.TokenPair{
		AccessToken:    accessToken,
		RefreshToken:   refreshToken,
		ExpirationTime: time.Now().Add(accessTTL),
	}, nil
}

func (s *jwtService) ValidateToken(ctx context.Context, tokenString string) (*g.Claims, error) {
	claims, err := g.ValidateToken(tokenString)
	if err != nil {
		return nil, fmt.Errorf("invalid token: %w", err)
	}

	tokenKey := fmt.Sprintf("%s%d:%s:%s", jwtKeyPrefix, claims.UserID, claims.Sid, claims.ID)

	storedToken, err := g.Redis.Get(ctx, tokenKey).Result()
	if err != nil {
		return nil, fmt.Errorf("token not found or expired: %w", err)
	}

	if storedToken != tokenString {
		return nil, fmt.Errorf("token mismatch")
	}

	return claims, nil
}

func (s *jwtService) InvalidateTokens(ctx context.Context, userID int64) error {
	keyPattern := fmt.Sprintf("%s%d:*", jwtKeyPrefix, userID)
	deletedCount, err := cache.ScanUnlinkKeys(ctx, keyPattern)
	if err != nil {
		return err
	}
	g.Info("Invalidated tokens",
		zap.String("keyPattern", keyPattern),
		zap.Int64("deletedCount", deletedCount))
	return nil
}

func (s *jwtService) InvalidateTokensBySid(ctx context.Context, userID int64, sid string) error {
	keyPattern := fmt.Sprintf("%s%d:%s:*", jwtKeyPrefix, userID, sid)
	deletedCount, err := cache.ScanUnlinkKeys(ctx, keyPattern)
	if err != nil {
		return err
	}
	g.Info("Invalidated tokens by sid",
		zap.String("keyPattern", keyPattern),
		zap.Int64("deletedCount", deletedCount))
	return nil
}
