package req

// QiniuCallbackBody 七牛云上传回调请求体
type QiniuCallbackBody struct {
	// 对象 key
	Key string `json:"key"`
	// 文件 ETag (hash)
	Hash string `json:"hash"`
	// 文件大小（字节）
	Fsize int64 `json:"fsize"`
	// 原始文件名
	Fname string `json:"fname"`
	// 文件 MIME 类型
	MimeType string `json:"mimeType"`
	// 回调来源 Host
	FusedHost string `json:"fusedHost"`
}
