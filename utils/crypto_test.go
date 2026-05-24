package utils

import (
	"encoding/base64"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestAES(t *testing.T) {
	t.Run("GenerateAESKey", func(t *testing.T) {
		key, err := GenerateAESKey()
		assert.NoError(t, err)
		assert.Len(t, key, AESKeySize/8)
	})

	t.Run("GenerateAESKeyBase64", func(t *testing.T) {
		keyBase64, err := GenerateAESKeyBase64()
		assert.NoError(t, err)
		assert.NotEmpty(t, keyBase64)
	})

	t.Run("AESEncryptDecrypt", func(t *testing.T) {
		key, err := GenerateAESKey()
		require.NoError(t, err)

		plaintext := []byte("Hello, World!")
		encrypted, err := AESEncrypt(key, plaintext)
		assert.NoError(t, err)
		assert.NotEmpty(t, encrypted)
		assert.NotEqual(t, plaintext, encrypted)

		decrypted, err := AESDecrypt(key, encrypted)
		assert.NoError(t, err)
		assert.Equal(t, plaintext, decrypted)
	})

	t.Run("AESEncryptDecryptBase64", func(t *testing.T) {
		keyBase64, err := GenerateAESKeyBase64()
		require.NoError(t, err)

		plaintext := "Hello, AES-GCM!"
		encrypted, err := AESEncryptBase64(keyBase64, plaintext)
		assert.NoError(t, err)
		assert.NotEmpty(t, encrypted)

		decrypted, err := AESDecryptBase64(keyBase64, encrypted)
		assert.NoError(t, err)
		assert.Equal(t, plaintext, decrypted)
	})

	t.Run("AESDecryptInvalidKey", func(t *testing.T) {
		key := []byte("invalid_key_12345678901234567890123456") // 32 bytes but not generated properly
		encrypted := []byte("invalid_data")
		_, err := AESDecrypt(key, encrypted)
		assert.Error(t, err)
	})

	t.Run("AESDecryptShortData", func(t *testing.T) {
		key, err := GenerateAESKey()
		require.NoError(t, err)
		_, err = AESDecrypt(key, []byte("short"))
		assert.Error(t, err)
	})

	t.Run("AESEncryptDecryptFixedKey", func(t *testing.T) {
		key := []byte("1234567890abcdef1234567890abcdef")
		plaintext := "123456qaz"
		encrypted, err := AESEncryptBase64(base64.StdEncoding.EncodeToString(key), plaintext)
		assert.NoError(t, err)
		t.Logf("Encrypted (base64): %s", encrypted)

		decrypted, err := AESDecryptBase64(base64.StdEncoding.EncodeToString(key), encrypted)
		assert.NoError(t, err)
		assert.Equal(t, plaintext, decrypted)
		t.Logf("Decrypted: %s", decrypted)
	})
}

func TestRSA(t *testing.T) {
	t.Run("GenerateRSAKeyPair", func(t *testing.T) {
		privateKey, publicKey, err := GenerateRSAKeyPair()
		assert.NoError(t, err)
		assert.NotNil(t, privateKey)
		assert.NotNil(t, publicKey)
		assert.Equal(t, privateKey.PublicKey, *publicKey)
	})

	t.Run("RSAPublicKeyToPEMAndBack", func(t *testing.T) {
		_, publicKey, err := GenerateRSAKeyPair()
		require.NoError(t, err)

		pemData, err := RSAPublicKeyToPEM(publicKey)
		assert.NoError(t, err)
		assert.NotEmpty(t, pemData)

		decodedKey, err := PEMToRSAPublicKey(pemData)
		assert.NoError(t, err)
		assert.NotNil(t, decodedKey)
		assert.Equal(t, publicKey.N, decodedKey.N)
		assert.Equal(t, publicKey.E, decodedKey.E)
	})

	t.Run("RSAPrivateKeyToPEMAndBack", func(t *testing.T) {
		privateKey, _, err := GenerateRSAKeyPair()
		require.NoError(t, err)

		pemData, err := RSAPrivateKeyToPEM(privateKey)
		assert.NoError(t, err)
		assert.NotEmpty(t, pemData)

		decodedKey, err := PEMToRSAPrivateKey(pemData)
		assert.NoError(t, err)
		assert.NotNil(t, decodedKey)
		assert.Equal(t, privateKey.N, decodedKey.N)
	})

	t.Run("RSAEncryptDecrypt", func(t *testing.T) {
		privateKey, publicKey, err := GenerateRSAKeyPair()
		require.NoError(t, err)

		plaintext := []byte("Hello, RSA!")
		encrypted, err := RSAEncrypt(publicKey, plaintext)
		assert.NoError(t, err)
		assert.NotEmpty(t, encrypted)
		assert.NotEqual(t, plaintext, encrypted)

		decrypted, err := RSADecrypt(privateKey, encrypted)
		assert.NoError(t, err)
		assert.Equal(t, plaintext, decrypted)
	})

	t.Run("RSAEncryptDecryptBase64", func(t *testing.T) {
		privateKey, publicKey, err := GenerateRSAKeyPair()
		require.NoError(t, err)

		publicPEM, err := RSAPublicKeyToPEM(publicKey)
		require.NoError(t, err)
		publicBase64 := base64.StdEncoding.EncodeToString(publicPEM)

		privatePEM, err := RSAPrivateKeyToPEM(privateKey)
		require.NoError(t, err)
		privateBase64 := base64.StdEncoding.EncodeToString(privatePEM)

		plaintext := "Hello, RSA OAEP!"
		encrypted, err := RSAEncryptBase64(publicBase64, plaintext)
		assert.NoError(t, err)
		assert.NotEmpty(t, encrypted)

		decrypted, err := RSADecryptBase64(privateBase64, encrypted)
		assert.NoError(t, err)
		assert.Equal(t, plaintext, decrypted)
	})

	t.Run("PEMToRSAPublicKeyInvalid", func(t *testing.T) {
		_, err := PEMToRSAPublicKey([]byte("invalid pem data"))
		assert.Error(t, err)
	})

	t.Run("PEMToRSAPrivateKeyInvalid", func(t *testing.T) {
		_, err := PEMToRSAPrivateKey([]byte("invalid pem data"))
		assert.Error(t, err)
	})
}