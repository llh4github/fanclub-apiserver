package services

import (
	"encoding/base64"
	"time"

	"fanclub-apiserver/cache"
	"fanclub-apiserver/dto/req"
	"fanclub-apiserver/dto/resp"
	"fanclub-apiserver/errs"
	"fanclub-apiserver/g"
	"fanclub-apiserver/utils"

	"github.com/google/uuid"
	"go.uber.org/zap"
)

const (
	rsaKeyPrefix       = string(cache.CryptoKey + "rsa:")
	aesKeyPrefix       = string(cache.CryptoKey + "aes:")
	keyExpireDuration  = 24 * time.Hour
	initExpireDuration = 1 * time.Minute
)

var Crypto = new(cryptoService)

type cryptoService struct {
}

// InitKeyExchange 初始化密钥交换，生成RSA密钥对并返回公钥
func (s *cryptoService) InitKeyExchange(appCtx *g.AppCtx) (*resp.InitKeyExchangeResp, error) {
	privateKey, publicKey, err := utils.GenerateRSAKeyPair()
	if err != nil {
		return nil, errs.WrapError(err, "Failed to generate RSA key pair", string(errs.RSAKeyGenerateFailed))
	}

	sessionID := uuid.New().String()

	publicPEM, err := utils.RSAPublicKeyToPEM(publicKey)
	if err != nil {
		return nil, errs.WrapError(err, "Failed to marshal public key", string(errs.RSAKeyGenerateFailed))
	}

	privatePEM, err := utils.RSAPrivateKeyToPEM(privateKey)
	if err != nil {
		return nil, errs.WrapError(err, "Failed to marshal private key", string(errs.RSAKeyGenerateFailed))
	}

	rsaKey := rsaKeyPrefix + sessionID
	err = g.Redis.Set(appCtx.C, rsaKey, privatePEM, initExpireDuration).Err()
	if err != nil {
		return nil, errs.WrapError(err, "Failed to store RSA private key", string(errs.DataCreateFailed))
	}

	expireAt := time.Now().Add(initExpireDuration).Unix()

	g.Info("Key exchange initialized",
		zap.String("session_id", sessionID),
		zap.Int64("expire_at", expireAt))

	return &resp.InitKeyExchangeResp{
		SessionID:    sessionID,
		RSAPublicKey: base64.StdEncoding.EncodeToString(publicPEM),
		ExpireAt:     expireAt,
	}, nil
}

// CompleteKeyExchange 完成密钥交换，解密AES密钥并保存
func (s *cryptoService) CompleteKeyExchange(appCtx *g.AppCtx, req *req.CompleteKeyExchange) (*resp.CompleteKeyExchangeResp, error) {
	rsaKey := rsaKeyPrefix + req.SessionID
	privatePEM, err := g.Redis.Get(appCtx.C, rsaKey).Bytes()
	if err != nil {
		g.Warn("RSA private key not found or expired",
			zap.String("session_id", req.SessionID),
			zap.Error(err))
		return nil, errs.WrapError(err, "Session not found or expired", string(errs.DataNotFound))
	}

	err = g.Redis.Del(appCtx.C, rsaKey).Err()
	if err != nil {
		g.Warn("Failed to delete RSA private key",
			zap.String("session_id", req.SessionID),
			zap.Error(err))
	}

	privateKey, err := utils.PEMToRSAPrivateKey(privatePEM)
	if err != nil {
		return nil, errs.WrapError(err, "Failed to parse private key", string(errs.RSADecryptFailed))
	}

	encryptedAESKey, err := base64.StdEncoding.DecodeString(req.EncryptedAESKey)
	if err != nil {
		return nil, errs.WrapError(err, "Invalid encrypted AES key", string(errs.ReqParamValidFailed))
	}

	g.Debug("Decrypting AES key",
		zap.String("session_id", req.SessionID),
		zap.Int("encrypted_key_length", len(encryptedAESKey)))

	aesKey, err := utils.RSADecrypt(privateKey, encryptedAESKey)
	if err != nil {
		g.Error("RSA decryption failed",
			zap.String("session_id", req.SessionID),
			zap.Int("encrypted_key_length", len(encryptedAESKey)),
			zap.Error(err))
		return nil, errs.WrapError(err, "Failed to decrypt AES key", string(errs.RSADecryptFailed))
	}

	g.Debug("RSA 解密成功，获取到 AES 密钥",
		zap.String("session_id", req.SessionID),
		zap.Int("aes_key_length", len(aesKey)),
		zap.String("aes_key_base64", base64.StdEncoding.EncodeToString(aesKey)))

	aesKeyKey := aesKeyPrefix + req.SessionID
	err = g.Redis.Set(appCtx.C, aesKeyKey, aesKey, 0).Err()
	if err != nil {
		return nil, errs.WrapError(err, "Failed to store AES key", string(errs.DataCreateFailed))
	}

	_, err = cache.SetExpire(appCtx.C, int64(keyExpireDuration.Seconds()), aesKeyKey)
	if err != nil {
		g.Warn("Failed to set AES key expire time",
			zap.String("session_id", req.SessionID),
			zap.Error(err))
	}

	g.Info("Key exchange completed",
		zap.String("session_id", req.SessionID))

	keyExpireAt := time.Now().Add(keyExpireDuration).Unix()
	return &resp.CompleteKeyExchangeResp{
		Success:     true,
		KeyExpireAt: keyExpireAt,
	}, nil
}

// GetAESKey 获取会话对应的AES密钥
func (s *cryptoService) GetAESKey(appCtx *g.AppCtx, sessionID string) ([]byte, error) {
	aesKeyKey := aesKeyPrefix + sessionID
	aesKey, err := g.Redis.Get(appCtx.C, aesKeyKey).Bytes()
	if err != nil {
		g.Warn("从 Redis 获取 AES 密钥失败",
			zap.String("session_id", sessionID),
			zap.String("redis_key", aesKeyKey),
			zap.Error(err))
		return nil, errs.WrapError(err, "AES key not found or expired", string(errs.DataNotFound))
	}

	g.Debug("从 Redis 获取到 AES 密钥",
		zap.String("session_id", sessionID),
		zap.Int("aes_key_length", len(aesKey)),
		zap.String("aes_key_base64", base64.StdEncoding.EncodeToString(aesKey)))

	return aesKey, nil
}

// EncryptWithAES 使用AES密钥加密数据
func (s *cryptoService) EncryptWithAES(appCtx *g.AppCtx, sessionID string, plaintext string) (string, error) {
	aesKey, err := s.GetAESKey(appCtx, sessionID)
	if err != nil {
		return "", err
	}

	return utils.AESEncryptBase64(base64.StdEncoding.EncodeToString(aesKey), plaintext)
}

// DecryptWithAES 使用AES密钥解密数据
func (s *cryptoService) DecryptWithAES(appCtx *g.AppCtx, sessionID string, encrypted string) (string, error) {
	aesKey, err := s.GetAESKey(appCtx, sessionID)
	if err != nil {
		return "", err
	}

	g.Debug("获取到 AES 密钥",
		zap.String("session_id", sessionID),
		zap.Int("aes_key_length", len(aesKey)),
		zap.String("aes_key_base64", base64.StdEncoding.EncodeToString(aesKey)))

	return utils.AESDecryptBase64(base64.StdEncoding.EncodeToString(aesKey), encrypted)
}

// DeleteAESKey 删除会话对应的AES密钥
func (s *cryptoService) DeleteAESKey(appCtx *g.AppCtx, sessionID string) error {
	aesKeyKey := aesKeyPrefix + sessionID
	return g.Redis.Del(appCtx.C, aesKeyKey).Err()
}
