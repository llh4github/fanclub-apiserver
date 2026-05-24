package g

import (
	"errors"
	"strconv"
	"time"

	"github.com/golang-jwt/jwt/v5"
)

// UserInfoJwt 用于生成jwt的用户信息
type UserInfoJwt struct {
	UserID int64  `json:"user_id"`
	Role   string `json:"role"`
	BID    int64  `json:"bid"`
	RoomID int64  `json:"room_id"`
	Sid    string `json:"sid"`
}

// Claims 自定义JWT claims
type Claims struct {
	Type   string `json:"typ"`
	UserID int64  `json:"user_id"`
	Role   string `json:"role"`
	BID    int64  `json:"bid"`
	RoomID int64  `json:"room_id"`
	Sid    string `json:"sid"`
	jwt.RegisteredClaims
}

// GenerateToken 生成JWT访问token
func GenerateToken(userInfo *UserInfoJwt) (tokenString string, tokenID string, err error) {
	// 设置token的过期时间
	expDuration := time.Duration(Cfg.JWT.AccessTokenExpiration) * time.Minute
	expirationTime := time.Now().Add(expDuration)

	// 生成token的唯一ID
	tokenID, err = NextIDStr()
	if err != nil {
		return
	}

	// 创建claims
	claims := &Claims{
		Type:   "access",
		UserID: userInfo.UserID,
		Role:   userInfo.Role,
		BID:    userInfo.BID,
		RoomID: userInfo.RoomID,
		Sid:    userInfo.Sid,
		RegisteredClaims: jwt.RegisteredClaims{
			ExpiresAt: jwt.NewNumericDate(expirationTime),
			IssuedAt:  jwt.NewNumericDate(time.Now()),
			NotBefore: jwt.NewNumericDate(time.Now()),
			Issuer:    Cfg.JWT.Issuer,
			Subject:   strconv.FormatInt(userInfo.UserID, 10),
			ID:        tokenID,
		},
	}

	// 创建token
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)

	// 签名token
	tokenString, err = token.SignedString([]byte(Cfg.JWT.SecretKey))
	if err != nil {
		return
	}

	return
}

// GenerateRefreshToken 生成JWT刷新token
func GenerateRefreshToken(userInfo *UserInfoJwt) (tokenString string, tokenID string, err error) {
	// 设置token的过期时间
	expDuration := time.Duration(Cfg.JWT.RefreshTokenExpiration) * time.Minute
	expirationTime := time.Now().Add(expDuration)

	// 生成token的唯一ID
	tokenID, err = NextIDStr()
	if err != nil {
		return
	}

	// 创建claims
	claims := &Claims{
		Type:   "refresh",
		UserID: userInfo.UserID,
		Role:   userInfo.Role,
		BID:    userInfo.BID,
		RoomID: userInfo.RoomID,
		Sid:    userInfo.Sid,
		RegisteredClaims: jwt.RegisteredClaims{
			ExpiresAt: jwt.NewNumericDate(expirationTime),
			IssuedAt:  jwt.NewNumericDate(time.Now()),
			NotBefore: jwt.NewNumericDate(time.Now()),
			Issuer:    Cfg.JWT.Issuer,
			Subject:   strconv.FormatInt(userInfo.UserID, 10),
			ID:        tokenID,
		},
	}

	// 创建token
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)

	// 签名token
	tokenString, err = token.SignedString([]byte(Cfg.JWT.SecretKey))
	if err != nil {
		return
	}

	return
}

// ValidateToken 验证JWT token
func ValidateToken(tokenString string) (*Claims, error) {
	// 解析token
	claims := &Claims{}
	token, err := jwt.ParseWithClaims(tokenString, claims, func(token *jwt.Token) (interface{}, error) {
		// 验证签名方法
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, errors.New("unexpected signing method")
		}
		return []byte(Cfg.JWT.SecretKey), nil
	})

	if err != nil {
		return nil, err
	}

	if !token.Valid {
		return nil, errors.New("invalid token")
	}

	return claims, nil
}

// ValidateRefreshToken 验证JWT刷新token
func ValidateRefreshToken(tokenString string) (*Claims, error) {
	claims, err := ValidateToken(tokenString)
	if err != nil {
		return nil, err
	}

	// 验证Type是否为refresh
	if claims.Type != "refresh" {
		return nil, errors.New("invalid refresh token")
	}

	return claims, nil
}

// GetUserIDFromToken 从token中获取用户ID
func GetUserIDFromToken(tokenString string) (int64, error) {
	claims, err := ValidateToken(tokenString)
	if err != nil {
		return 0, err
	}
	userID, err := strconv.ParseInt(claims.Subject, 10, 64)
	if err != nil {
		return 0, err
	}
	return userID, nil
}

// GetUserInfoFromToken 从token中获取用户信息
func GetUserInfoFromToken(tokenString string) (*UserInfoJwt, error) {
	claims, err := ValidateToken(tokenString)
	if err != nil {
		return nil, err
	}

	// 直接从Claims字段中构建用户信息
	userInfo := &UserInfoJwt{
		UserID: claims.UserID,
		Role:   claims.Role,
		BID:    claims.BID,
		RoomID: claims.RoomID,
		Sid:    claims.Sid,
	}

	return userInfo, nil
}
