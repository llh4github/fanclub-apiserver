package cache

// KeyPrefix 缓存名的前缀
type KeyPrefix string

const (
	appName                   KeyPrefix = "fanclub-apiserver"
	ServiceLayer              KeyPrefix = appName + ":service:"
	Captcha                   KeyPrefix = appName + ":captcha:"
	AnchorFollowerNum         KeyPrefix = ServiceLayer + "anchor:follower_num:"
	DanmuStatistics           KeyPrefix = appName + "danmu:statistics:"
	AiLayer                   KeyPrefix = appName + ":ai-layer:"
	CryptoKey                 KeyPrefix = ServiceLayer + "crypto:"
	AnchorLiveSchedule        KeyPrefix = ServiceLayer + "anchor:live_schedule:"
	AnchorLiveRecord          KeyPrefix = ServiceLayer + "anchor:live_record:"
	TreeholeTopic             KeyPrefix = ServiceLayer + "treehole:topic:"
	TreeholeSubmissionSummary KeyPrefix = ServiceLayer + "treehole:submission:summary:"
)
