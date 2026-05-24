package utils

import (
	"crypto/aes"
	"crypto/cipher"
	"crypto/rand"
	"crypto/rsa"
	"crypto/sha256"
	"crypto/x509"
	"encoding/base64"
	"encoding/pem"
	"errors"
	"fmt"
)

const (
	AESKeySize   = 256
	GCMIVLength  = 12
	GCMTagLength = 128
	RSAKeySize   = 2048
)

func GenerateAESKey() ([]byte, error) {
	key := make([]byte, AESKeySize/8)
	_, err := rand.Read(key)
	if err != nil {
		return nil, fmt.Errorf("failed to generate AES key: %w", err)
	}
	return key, nil
}

func GenerateAESKeyBase64() (string, error) {
	key, err := GenerateAESKey()
	if err != nil {
		return "", err
	}
	return base64.StdEncoding.EncodeToString(key), nil
}

func AESEncrypt(key []byte, plaintext []byte) ([]byte, error) {
	block, err := aes.NewCipher(key)
	if err != nil {
		return nil, fmt.Errorf("failed to create AES cipher: %w", err)
	}

	gcm, err := cipher.NewGCM(block)
	if err != nil {
		return nil, fmt.Errorf("failed to create GCM: %w", err)
	}

	iv := make([]byte, GCMIVLength)
	_, err = rand.Read(iv)
	if err != nil {
		return nil, fmt.Errorf("failed to generate IV: %w", err)
	}

	ciphertext := gcm.Seal(nil, iv, plaintext, nil)
	result := make([]byte, len(iv)+len(ciphertext))
	copy(result, iv)
	copy(result[len(iv):], ciphertext)

	return result, nil
}

func AESEncryptBase64(keyBase64 string, plaintext string) (string, error) {
	key, err := base64.StdEncoding.DecodeString(keyBase64)
	if err != nil {
		return "", fmt.Errorf("invalid key base64: %w", err)
	}

	result, err := AESEncrypt(key, []byte(plaintext))
	if err != nil {
		return "", err
	}

	return base64.StdEncoding.EncodeToString(result), nil
}

func AESDecrypt(key []byte, encrypted []byte) ([]byte, error) {
	if len(encrypted) < GCMIVLength {
		return nil, errors.New("encrypted data too short")
	}

	iv := encrypted[:GCMIVLength]
	ciphertext := encrypted[GCMIVLength:]

	block, err := aes.NewCipher(key)
	if err != nil {
		return nil, fmt.Errorf("failed to create AES cipher: %w", err)
	}

	gcm, err := cipher.NewGCM(block)
	if err != nil {
		return nil, fmt.Errorf("failed to create GCM: %w", err)
	}

	plaintext, err := gcm.Open(nil, iv, ciphertext, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to decrypt: %w", err)
	}

	return plaintext, nil
}

func AESDecryptBase64(keyBase64 string, encryptedBase64 string) (string, error) {
	key, err := base64.StdEncoding.DecodeString(keyBase64)
	if err != nil {
		return "", fmt.Errorf("invalid key base64: %w", err)
	}

	encrypted, err := base64.StdEncoding.DecodeString(encryptedBase64)
	if err != nil {
		return "", fmt.Errorf("invalid encrypted base64: %w", err)
	}

	result, err := AESDecrypt(key, encrypted)
	if err != nil {
		return "", err
	}

	return string(result), nil
}

func GenerateRSAKeyPair() (*rsa.PrivateKey, *rsa.PublicKey, error) {
	privateKey, err := rsa.GenerateKey(rand.Reader, RSAKeySize)
	if err != nil {
		return nil, nil, fmt.Errorf("failed to generate RSA key pair: %w", err)
	}
	return privateKey, &privateKey.PublicKey, nil
}

func RSAPublicKeyToPEM(publicKey *rsa.PublicKey) ([]byte, error) {
	der, err := x509.MarshalPKIXPublicKey(publicKey)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal public key: %w", err)
	}
	return pem.EncodeToMemory(&pem.Block{
		Type:  "RSA PUBLIC KEY",
		Bytes: der,
	}), nil
}

func RSAPrivateKeyToPEM(privateKey *rsa.PrivateKey) ([]byte, error) {
	der, err := x509.MarshalPKCS8PrivateKey(privateKey)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal private key: %w", err)
	}
	return pem.EncodeToMemory(&pem.Block{
		Type:  "RSA PRIVATE KEY",
		Bytes: der,
	}), nil
}

func PEMToRSAPublicKey(pemData []byte) (*rsa.PublicKey, error) {
	block, _ := pem.Decode(pemData)
	if block == nil {
		return nil, errors.New("failed to decode PEM block")
	}

	pub, err := x509.ParsePKIXPublicKey(block.Bytes)
	if err != nil {
		return nil, fmt.Errorf("failed to parse public key: %w", err)
	}

	publicKey, ok := pub.(*rsa.PublicKey)
	if !ok {
		return nil, errors.New("not an RSA public key")
	}

	return publicKey, nil
}

func PEMToRSAPrivateKey(pemData []byte) (*rsa.PrivateKey, error) {
	block, _ := pem.Decode(pemData)
	if block == nil {
		return nil, errors.New("failed to decode PEM block")
	}

	priv, err := x509.ParsePKCS8PrivateKey(block.Bytes)
	if err != nil {
		return nil, fmt.Errorf("failed to parse private key: %w", err)
	}

	privateKey, ok := priv.(*rsa.PrivateKey)
	if !ok {
		return nil, errors.New("not an RSA private key")
	}

	return privateKey, nil
}

// RSAEncrypt 使用RSA OAEP加密
//
// Parameters:
//   - publicKey: RSA公钥
//   - plaintext: 待加密的明文
//
// Returns:
//   - 加密后的密文
func RSAEncrypt(publicKey *rsa.PublicKey, plaintext []byte) ([]byte, error) {
	ciphertext, err := rsa.EncryptOAEP(sha256.New(), rand.Reader, publicKey, plaintext, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to encrypt: %w", err)
	}
	return ciphertext, nil
}

func RSAEncryptBase64(publicKeyBase64 string, plaintext string) (string, error) {
	pemData, err := base64.StdEncoding.DecodeString(publicKeyBase64)
	if err != nil {
		return "", fmt.Errorf("invalid public key base64: %w", err)
	}

	publicKey, err := PEMToRSAPublicKey(pemData)
	if err != nil {
		return "", err
	}

	ciphertext, err := RSAEncrypt(publicKey, []byte(plaintext))
	if err != nil {
		return "", err
	}

	return base64.StdEncoding.EncodeToString(ciphertext), nil
}

// RSADecrypt 使用RSA OAEP解密
//
// Parameters:
//   - privateKey: RSA私钥
//   - ciphertext: 待解密的密文
//
// Returns:
//   - 解密后的明文
func RSADecrypt(privateKey *rsa.PrivateKey, ciphertext []byte) ([]byte, error) {
	plaintext, err := rsa.DecryptOAEP(sha256.New(), rand.Reader, privateKey, ciphertext, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to decrypt: %w", err)
	}
	return plaintext, nil
}

func RSADecryptBase64(privateKeyBase64 string, ciphertextBase64 string) (string, error) {
	pemData, err := base64.StdEncoding.DecodeString(privateKeyBase64)
	if err != nil {
		return "", fmt.Errorf("invalid private key base64: %w", err)
	}

	privateKey, err := PEMToRSAPrivateKey(pemData)
	if err != nil {
		return "", err
	}

	ciphertext, err := base64.StdEncoding.DecodeString(ciphertextBase64)
	if err != nil {
		return "", fmt.Errorf("invalid ciphertext base64: %w", err)
	}

	plaintext, err := RSADecrypt(privateKey, ciphertext)
	if err != nil {
		return "", err
	}

	return string(plaintext), nil
}
