package g

// 变量需大写导出，供外部读取
var (
	Version   = "dev"     // 默认开发版本
	Branch    = "unknown" // Git 分支名称
	GitCommit = "unknown" // Git 提交哈希
	BuildTime = "unknown" // 构建时间
)
