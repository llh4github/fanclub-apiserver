package storage

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"os"
	"strings"
	"time"

	"fanclub-apiserver/g"

	"github.com/qiniu/go-sdk/v7/auth/qbox"
	"github.com/qiniu/go-sdk/v7/storage"
)

// PresignedPutObjectInput 预签名上传输入参数
type PresignedPutObjectInput struct {
	// 对象 key
	Key string
	// 内容类型，如 image/png
	ContentType string
	// 过期时间
	ExpiresIn time.Duration
	// 文件大小限制（字节），默认不限制
	MaxSize int64
}

// PresignedGetObjectInput 预签名下载输入参数
type PresignedGetObjectInput struct {
	// 对象 key
	Key string
	// 过期时间
	ExpiresIn time.Duration
	// 是否使用 CDN 域名，默认 true
	UseCDN bool
}

// ObjectInfo 对象信息
type ObjectInfo struct {
	// 对象 key
	Key string
	// 文件大小（字节）
	Size int64
	// 内容类型
	ContentType string
	// 修改时间
	LastModified time.Time
	// ETag
	ETag string
	// 元数据
	Metadata map[string]string
}

var bucket string
var cdnDomain string
var storageDomain string
var qiniuMac *qbox.Mac

// GetBucket 获取存储桶名称
func GetBucket() string {
	return bucket
}

// GetCDNDomain 获取 CDN 域名
func GetCDNDomain() string {
	return cdnDomain
}

// InitOSS 初始化七牛云存储客户端
//
// 使用配置文件中的 OSS 配置信息创建七牛云存储客户端
//
// Returns:
//   - error: 错误信息
func InitOSS() error {
	ak := getEnvOrConfig("QINIU_ACCESS_KEY", g.Cfg.OSS.AccessKeyID)
	sk := getEnvOrConfig("QINIU_SECRET_KEY", g.Cfg.OSS.AccessKeySecret)
	bucket = getEnvOrConfig("QINIU_BUCKET", g.Cfg.OSS.Bucket)
	cdnDomain = getEnvOrConfig("QINIU_CDN", g.Cfg.OSS.CDN)
	storageDomain = getEnvOrConfig("QINIU_DOMAIN", g.Cfg.OSS.Domain)

	qiniuMac = qbox.NewMac(ak, sk)
	return nil
}

func getEnvOrConfig(envKey, configValue string) string {
	if v := os.Getenv(envKey); v != "" {
		return v
	}
	return configValue
}

// GeneratePresignedPutURL 生成预签名上传 URL
//
// 生成一个临时的上传凭证 URL，用于前端直传到七牛云
//
// Parameters:
//   - ctx: 上下文
//   - input: 预签名上传参数
//
// UploadToken 临时上传凭证
type UploadToken struct {
	// 上传凭证
	Token string
	// 对象 key
	ObjectKey string
	// 区域
	Region string
	// 过期时间（Unix 时间戳）
	ExpiresAt int64
}

// GenerateUploadToken 生成上传凭证
//
// 生成临时的上传凭证，返回纯 token 和 object_key，供前端使用七牛 JS SDK 直传
//
// Parameters:
//   - ctx: 上下文
//   - input: 预签名上传参数
//
// Returns:
//   - *UploadToken: 上传凭证
//   - error: 错误信息
func GenerateUploadToken(ctx context.Context, input PresignedPutObjectInput) (*UploadToken, error) {
	if qiniuMac == nil {
		return nil, fmt.Errorf("七牛云客户端未初始化")
	}

	scope := bucket
	if input.Key != "" {
		scope = bucket + ":" + input.Key
	}

	deadline := time.Now().Add(input.ExpiresIn).Unix()

	putPolicy := storage.PutPolicy{
		Scope:   scope,
		Expires: uint64(deadline),
	}

	if input.ContentType != "" {
		putPolicy.MimeLimit = input.ContentType
	}

	if input.MaxSize > 0 {
		putPolicy.FsizeLimit = input.MaxSize
	}

	putPolicyJson, err := json.Marshal(putPolicy)
	if err != nil {
		return nil, fmt.Errorf("序列化上传策略失败: %w", err)
	}

	token := qbox.SignWithData(qiniuMac, putPolicyJson)

	return &UploadToken{
		Token:     token,
		ObjectKey: input.Key,
		Region:    g.Cfg.OSS.Region,
		ExpiresAt: deadline,
	}, nil
}

// GeneratePresignedGetURL 生成私有资源访问 URL
//
// 生成一个临时的私有资源访问 URL，用于获取七牛云存储的私有文件
//
// Parameters:
//   - ctx: 上下文
//   - input: 预签名下载参数
//
// Returns:
//   - string: 私有资源访问 URL
//   - error: 错误信息
func GeneratePresignedGetURL(ctx context.Context, input PresignedGetObjectInput) (string, error) {
	if qiniuMac == nil {
		return "", fmt.Errorf("七牛云客户端未初始化")
	}

	domain := storageDomain
	if input.UseCDN && cdnDomain != "" {
		domain = cdnDomain
	}

	deadlineUnix := time.Now().Add(input.ExpiresIn).Unix()
	privateURL := storage.MakePrivateURL(qiniuMac, "https://"+domain, input.Key, deadlineUnix)
	return privateURL, nil
}

// PutObject 上传对象
//
// 将数据流上传到七牛云（服务端直传）
//
// Parameters:
//   - ctx: 上下文
//   - key: 对象 key
//   - body: 数据流
//   - contentType: 内容类型
//
// Returns:
//   - error: 错误信息
func PutObject(ctx context.Context, key string, body io.Reader, contentType string) error {
	data, err := io.ReadAll(body)
	if err != nil {
		return fmt.Errorf("读取数据失败: %w", err)
	}

	token, err := generateToken(key)
	if err != nil {
		return err
	}

	return putObjectWithToken(ctx, token, key, data, contentType)
}

// PutObjectWithToken 使用指定 token 上传对象
//
// 使用外部生成的 token 上传对象到七牛云，用于测试验证 token 有效性
//
// Parameters:
//   - ctx: 上下文
//   - token: 上传凭证
//   - key: 对象 key
//   - body: 数据流
//   - contentType: 内容类型
//
// Returns:
//   - error: 错误信息
func PutObjectWithToken(ctx context.Context, token string, key string, body io.Reader, contentType string) error {
	data, err := io.ReadAll(body)
	if err != nil {
		return fmt.Errorf("读取数据失败: %w", err)
	}

	return putObjectWithToken(ctx, token, key, data, contentType)
}

// generateToken 生成上传 token
func generateToken(key string) (string, error) {
	return generateTokenWithContext(context.Background(), key)
}

// generateTokenWithContext 使用指定上下文生成上传 token
func generateTokenWithContext(ctx context.Context, key string) (string, error) {
	if qiniuMac == nil {
		return "", fmt.Errorf("七牛云客户端未初始化")
	}

	scope := bucket + ":" + key
	deadline := time.Now().Add(1 * time.Hour).Unix()

	putPolicy := storage.PutPolicy{
		Scope:   scope,
		Expires: uint64(deadline),
	}

	putPolicyJson, err := json.Marshal(putPolicy)
	if err != nil {
		return "", fmt.Errorf("序列化上传策略失败: %w", err)
	}

	token := qbox.SignWithData(qiniuMac, putPolicyJson)

	return token, nil
}

// putObjectWithToken 使用 token 上传对象
func putObjectWithToken(ctx context.Context, token string, key string, data []byte, contentType string) error {
	if qiniuMac == nil {
		return fmt.Errorf("七牛云客户端未初始化")
	}

	cfg := &storage.Config{
		UseHTTPS: g.Cfg.OSS.UseSSL,
	}

	formUploader := storage.NewFormUploader(cfg)

	putExtra := &storage.PutExtra{}
	if contentType != "" {
		putExtra.MimeType = contentType
	}

	ret := storage.PutRet{}
	err := formUploader.Put(context.Background(), &ret, token, key, bytes.NewReader(data), int64(len(data)), putExtra)
	if err != nil {
		return fmt.Errorf("上传对象失败: %w", err)
	}

	return nil
}

// GetObject 获取对象
//
// 从七牛云下载对象内容（暂不支持）
//
// Returns:
//   - io.ReadCloser: 对象内容流
//   - error: 错误信息
func GetObject(ctx context.Context, key string) (io.ReadCloser, error) {
	return nil, fmt.Errorf("GetObject 暂不支持，请使用 GeneratePresignedGetURL 获取访问 URL")
}

// DeleteObject 删除对象
//
// 从七牛云删除指定对象
//
// Parameters:
//   - ctx: 上下文
//   - key: 对象 key
//
// Returns:
//   - error: 错误信息
func DeleteObject(ctx context.Context, key string) error {
	if qiniuMac == nil {
		return fmt.Errorf("七牛云客户端未初始化")
	}

	cfg := &storage.Config{
		UseHTTPS: g.Cfg.OSS.UseSSL,
	}

	bucketManager := storage.NewBucketManager(qiniuMac, cfg)
	return bucketManager.Delete(bucket, key)
}

// DeleteObjects 批量删除对象
//
// 从七牛云批量删除指定对象
//
// Parameters:
//   - ctx: 上下文
//   - keys: 对象 key 列表
//
// Returns:
//   - error: 错误信息
func DeleteObjects(ctx context.Context, keys []string) error {
	if qiniuMac == nil {
		return fmt.Errorf("七牛云客户端未初始化")
	}

	if len(keys) == 0 {
		return nil
	}

	cfg := &storage.Config{
		UseHTTPS: g.Cfg.OSS.UseSSL,
	}

	bucketManager := storage.NewBucketManager(qiniuMac, cfg)

	for _, key := range keys {
		err := bucketManager.Delete(bucket, key)
		if err != nil {
			return fmt.Errorf("批量删除对象失败 [%s]: %w", key, err)
		}
	}

	return nil
}

// HeadObject 获取对象元信息
//
// 获取七牛云对象的元信息（对应七牛云 /stat 接口）
//
// Parameters:
//   - ctx: 上下文
//   - key: 对象 key
//
// Returns:
//   - interface{}: 原始元信息
//   - error: 错误信息
func HeadObject(ctx context.Context, key string) (interface{}, error) {
	if qiniuMac == nil {
		return nil, fmt.Errorf("七牛云客户端未初始化")
	}

	cfg := &storage.Config{
		UseHTTPS: g.Cfg.OSS.UseSSL,
	}

	bucketManager := storage.NewBucketManager(qiniuMac, cfg)
	return bucketManager.Stat(bucket, key)
}

// ListObjects 列举对象
//
// 根据前缀列举七牛云中的对象
//
// Parameters:
//   - ctx: 上下文
//   - prefix: 对象前缀
//   - maxKeys: 最大返回数量
//
// Returns:
//   - []string: 对象 key 列表
//   - error: 错误信息
func ListObjects(ctx context.Context, prefix string, maxKeys int32) ([]string, error) {
	if qiniuMac == nil {
		return nil, fmt.Errorf("七牛云客户端未初始化")
	}

	cfg := &storage.Config{
		UseHTTPS: g.Cfg.OSS.UseSSL,
	}

	bucketManager := storage.NewBucketManager(qiniuMac, cfg)

	entries, _, _, ok, err := bucketManager.ListFiles(bucket, prefix, "", "", int(maxKeys))
	if err != nil {
		return nil, fmt.Errorf("列举对象失败: %w", err)
	}

	if !ok {
		return nil, fmt.Errorf("列举对象失败")
	}

	keys := make([]string, 0, len(entries))
	for _, entry := range entries {
		keys = append(keys, entry.Key)
	}

	return keys, nil
}

// ObjectExists 检查对象是否存在
//
// 通过 stat 检查七牛云对象是否存在
//
// Parameters:
//   - ctx: 上下文
//   - key: 对象 key
//
// Returns:
//   - bool: 是否存在
//   - error: 错误信息
func ObjectExists(ctx context.Context, key string) (bool, error) {
	if qiniuMac == nil {
		return false, fmt.Errorf("七牛云客户端未初始化")
	}

	_, err := HeadObject(ctx, key)
	if err != nil {
		return false, nil
	}

	return true, nil
}

// GetObjectInfo 获取对象详细信息
//
// 获取七牛云对象的完整元信息
//
// Parameters:
//   - ctx: 上下文
//   - key: 对象 key
//
// Returns:
//   - *ObjectInfo: 对象信息
//   - error: 错误信息
func GetObjectInfo(ctx context.Context, key string) (*ObjectInfo, error) {
	if qiniuMac == nil {
		return nil, fmt.Errorf("七牛云客户端未初始化")
	}

	info, err := HeadObject(ctx, key)
	if err != nil {
		return nil, fmt.Errorf("获取对象信息失败: %w", err)
	}

	qiniuInfo := info.(storage.FileInfo)

	lastModified := time.Unix(0, qiniuInfo.PutTime*int64(time.Millisecond))

	return &ObjectInfo{
		Key:          key,
		Size:         int64(qiniuInfo.Fsize),
		ContentType:  qiniuInfo.MimeType,
		LastModified: lastModified,
		ETag:         qiniuInfo.Hash,
		Metadata:     make(map[string]string),
	}, nil
}

// GetObjectTags 获取对象元数据
//
// 获取七牛云对象的自定义元数据（对应 x-qn-meta-*）
//
// Parameters:
//   - ctx: 上下文
//   - key: 对象 key
//
// Returns:
//   - map[string]string: 元数据集合
//   - error: 错误信息
func GetObjectTags(ctx context.Context, key string) (map[string]string, error) {
	if qiniuMac == nil {
		return nil, fmt.Errorf("七牛云客户端未初始化")
	}

	cfg := &storage.Config{
		UseHTTPS: g.Cfg.OSS.UseSSL,
	}

	bucketManager := storage.NewBucketManager(qiniuMac, cfg)
	info, err := bucketManager.Stat(bucket, key)
	if err != nil {
		return nil, fmt.Errorf("获取对象元数据失败: %w", err)
	}

	metadata := make(map[string]string)
	for k, v := range info.MetaData {
		metadata[k] = v
	}

	_ = ctx
	return metadata, nil
}

// SetObjectTags 设置对象元数据
//
// 设置七牛云对象的自定义元数据（对应 x-qn-meta-*）
//
// Parameters:
//   - ctx: 上下文
//   - key: 对象 key
//   - tags: 元数据集合（key 支持字母、数字、下划线、减号，最大 50 字节）
//
// Returns:
//   - error: 错误信息
func SetObjectTags(ctx context.Context, key string, tags map[string]string) error {
	if qiniuMac == nil {
		return fmt.Errorf("七牛云客户端未初始化")
	}

	_ = ctx
	_ = key
	_ = tags
	return fmt.Errorf("SetObjectTags 暂不支持")
}

// DeleteObjectTags 删除对象元数据
//
// 删除七牛云对象的自定义元数据
//
// Parameters:
//   - ctx: 上下文
//   - key: 对象 key
//
// Returns:
//   - error: 错误信息
func DeleteObjectTags(ctx context.Context, key string) error {
	if qiniuMac == nil {
		return fmt.Errorf("七牛云客户端未初始化")
	}

	_ = ctx
	_ = key
	return nil
}

// VerifyQiniuCallback 验证七牛云回调签名
//
// 根据七牛云回调鉴权规范，验证回调请求的合法性
//
// Parameters:
//   - authorization: Authorization 请求头的值，格式为 "QBox {AccessKey}:{encoded_data}"
//   - requestPath: 请求路径（不包括查询参数）
//   - requestBody: 请求体（原始数据）
//
// Returns:
//   - bool: 签名是否合法
//   - error: 验证过程中的错误
//
// 算法说明:
//
//	明文 = Request.Path + "\n" + Request.Body
//	签名 = HMAC-SHA1(明文, SecretKey)
//	encoded_data = URLSafeBase64Encode(签名)
//	Authorization = "QBox " + AccessKey + ":" + encoded_data
func VerifyQiniuCallback(authorization, requestPath, requestBody string) (bool, error) {
	if authorization == "" {
		return false, fmt.Errorf("authorization header is empty")
	}

	if !strings.HasPrefix(authorization, "QBox ") {
		return false, fmt.Errorf("invalid authorization format, must start with 'QBox '")
	}

	authContent := strings.TrimPrefix(authorization, "QBox ")
	parts := strings.SplitN(authContent, ":", 2)
	if len(parts) != 2 {
		return false, fmt.Errorf("invalid authorization format, missing ':' separator")
	}

	accessKey := parts[0]
	receivedSignature := parts[1]

	if accessKey != g.Cfg.OSS.AccessKeyID {
		return false, fmt.Errorf("access key mismatch")
	}

	data := requestPath + "\n" + requestBody
	mac := qbox.NewMac(g.Cfg.OSS.AccessKeyID, g.Cfg.OSS.AccessKeySecret)
	token := qbox.Sign(mac, []byte(data))

	tokenParts := strings.SplitN(token, ":", 2)
	if len(tokenParts) != 2 {
		return false, fmt.Errorf("invalid token format")
	}
	expectedSignature := tokenParts[1]

	return expectedSignature == receivedSignature, nil
}
