// storage 对象存储模块
//
// 本模块封装了七牛云存储的操作，提供统一的文件管理接口。
//
// 支持的功能：
//   - 生成上传凭证（前端直传）
//   - 生成私有资源访问 URL
//   - 服务端直传文件
//   - 文件管理（删除、列举、查询）
//
// 使用前提
//
//  1. 确保配置文件中配置了七牛云相关参数
//
//     [oss]
//     endpoint = "s3-cn-east-1.qiniucs.com"
//     accessKeyID = "your_access_key"
//     accessKeySecret = "your_secret_key"
//     bucket = "your_bucket"
//     useSSL = true
//
//  2. 应用启动时调用 InitOSS() 初始化客户端
//
//     import "fanclub-apiserver/storage"
//
//     func main() {
//     if err := storage.InitOSS(); err != nil {
//     log.Fatal(err)
//     }
//     }
//
// 主要功能
//
// # 上传凭证（推荐用于前端直传）
//
// 生成上传凭证：
//
//	input := storage.PresignedPutObjectInput{
//	    Key:       "avatars/user123.png",
//	    ContentType: "image/png",
//	    ExpiresIn:  10 * time.Minute,
//	}
//	uploadURL, _ := storage.GeneratePresignedPutURL(ctx, input)
//
//	// 前端使用 uploadURL 上传文件到七牛云
//
// 生成私有资源访问 URL：
//
//	input := storage.PresignedGetObjectInput{
//	    Key:       "avatars/user123.png",
//	    ExpiresIn: 1 * time.Hour,
//	}
//	url, _ := storage.GeneratePresignedGetURL(ctx, input)
//
// # 服务端直传
//
//	file, _ := os.Open("image.jpg")
//	defer file.Close()
//	err := storage.PutObject(ctx, "images/banner.jpg", file, "image/jpeg")
//
// # 文件管理
//
// 删除文件：
//
//	err := storage.DeleteObject(ctx, "images/banner.jpg")
//
// 批量删除：
//
//	err := storage.DeleteObjects(ctx, []string{"images/1.jpg", "images/2.jpg"})
//
// 检查文件是否存在：
//
//	exists, _ := storage.ObjectExists(ctx, "images/banner.jpg")
//
// 获取文件元信息：
//
//	info, _ := storage.GetObjectInfo(ctx, "images/banner.jpg")
//	fmt.Println(info.Size, info.ContentType, info.LastModified)
//
// 列举文件：
//
//	keys, _ := storage.ListObjects(ctx, "images/", 100)
//
// 配置说明
//
// | 配置项 | 说明 | 示例值 |
// |--------|------|--------|
// | endpoint | 七牛云存储区域 endpoint | s3-cn-east-1.qiniucs.com |
// | accessKeyID | 访问密钥 ID | your_access_key |
// | accessKeySecret | 访问密钥密钥 | your_secret_key |
// | bucket | 存储空间名称 | your_bucket |
// | useSSL | 是否使用 HTTPS | true |
//
// 注意事项
//
//   - 本模块不提供存储空间管理操作（创建空间、删除空间等）
//   - 所有方法都需要先调用 InitOSS() 初始化
//   - 上传凭证有效期根据 ExpiresIn 参数设置
//   - GetObject 暂不支持（请使用 GeneratePresignedGetURL 获取访问 URL）
package storage
