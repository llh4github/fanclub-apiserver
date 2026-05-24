package servicetest

import (
	"testing"

	"fanclub-apiserver/g"
	"fanclub-apiserver/integration/testenv"
	"fanclub-apiserver/services"
)

func setupJWTConfig() {
	g.Cfg = &g.Config{
		JWT: g.JWTConfig{
			SecretKey:              "test-jwt-secret-key",
			AccessTokenExpiration:  60,
			RefreshTokenExpiration: 10080,
			Issuer:                 "test-issuer",
		},
	}
}

func TestJWTService(t *testing.T) {
	env, err := testenv.GetTestEnvironment()
	if err != nil {
		t.Fatalf("Failed to create test environment: %v", err)
	}
	defer env.Cleanup()

	setupJWTConfig()

	userInfo := &g.UserInfoJwt{
		UserID: 123456,
		Role:   "admin",
		BID:    654321,
		RoomID: 111222,
	}

	t.Run("GenerateTokens", func(t *testing.T) {
		tokenPair, err := services.JWT.GenerateTokens(env.Ctx, userInfo)
		if err != nil {
			t.Fatalf("GenerateTokens should not return error: %v", err)
		}
		if tokenPair.AccessToken == "" {
			t.Error("AccessToken should not be empty")
		}
		if tokenPair.RefreshToken == "" {
			t.Error("RefreshToken should not be empty")
		}
		if tokenPair.AccessToken == tokenPair.RefreshToken {
			t.Error("AccessToken and RefreshToken should be different")
		}
	})

	t.Run("ValidateToken", func(t *testing.T) {
		tokenPair, err := services.JWT.GenerateTokens(env.Ctx, userInfo)
		if err != nil {
			t.Fatalf("GenerateTokens should not return error: %v", err)
		}

		claims, err := services.JWT.ValidateToken(env.Ctx, tokenPair.AccessToken)
		if err != nil {
			t.Errorf("ValidateToken should not return error for valid access token: %v", err)
		}
		if claims == nil {
			t.Error("ValidateToken should return non-nil claims")
		}
		if claims.UserID != userInfo.UserID {
			t.Errorf("Claims UserID should match: expected %d, got %d", userInfo.UserID, claims.UserID)
		}
		if claims.Role != userInfo.Role {
			t.Errorf("Claims Role should match: expected %s, got %s", userInfo.Role, claims.Role)
		}

		claims, err = services.JWT.ValidateToken(env.Ctx, tokenPair.RefreshToken)
		if err != nil {
			t.Errorf("ValidateToken should not return error for valid refresh token: %v", err)
		}
		if claims == nil {
			t.Error("ValidateToken should return non-nil claims")
		}

		_, err = services.JWT.ValidateToken(env.Ctx, "invalid-token-string")
		if err == nil {
			t.Error("ValidateToken should return error for invalid token")
		}

		err = services.JWT.InvalidateTokens(env.Ctx, userInfo.UserID)
		if err != nil {
			t.Fatalf("InvalidateTokens should not return error: %v", err)
		}

		_, err = services.JWT.ValidateToken(env.Ctx, tokenPair.AccessToken)
		if err == nil {
			t.Error("ValidateToken should return error for invalidated token")
		}
	})

	t.Run("InvalidateTokens", func(t *testing.T) {
		for i := 0; i < 3; i++ {
			_, err := services.JWT.GenerateTokens(env.Ctx, userInfo)
			if err != nil {
				t.Fatalf("GenerateTokens should not return error: %v", err)
			}
		}

		err := services.JWT.InvalidateTokens(env.Ctx, userInfo.UserID)
		if err != nil {
			t.Errorf("InvalidateTokens should not return error: %v", err)
		}

		tokenPair, err := services.JWT.GenerateTokens(env.Ctx, userInfo)
		if err != nil {
			t.Fatalf("GenerateTokens should not return error: %v", err)
		}

		_, err = services.JWT.ValidateToken(env.Ctx, tokenPair.AccessToken)
		if err != nil {
			t.Errorf("Newly generated token should be valid: %v", err)
		}
	})
}
