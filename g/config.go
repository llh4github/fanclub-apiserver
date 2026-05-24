package g

import (
	"os"
	"strings"
	"time"

	"github.com/spf13/viper"
)

type Config struct {
	Server   ServerConfig
	Database DatabaseConfig
	Redis    RedisConfig
	Logs     LogsConfig
	JWT      JWTConfig
	ARK      ARKConfig
	OSS      OSSConfig
}

type ServerConfig struct {
	Port                string
	Host                string
	SwaggerEnabled      bool
	RequestIDEnabled    bool
	ResponseTimeEnabled bool
	// 环境标识：dev | prod
	Env string
	// 域名（用于 Cookie），可选
	Domain string
}

type DatabaseConfig struct {
	Driver      string
	Path        string
	AutoMigrate bool
	Debug       bool
}

type LogsConfig struct {
	Level string
}

type RedisConfig struct {
	Host     string
	Port     string
	Password string
	DB       int
}

type JWTConfig struct {
	SecretKey              string
	AccessTokenExpiration  int    // 访问token过期时间（分钟）
	RefreshTokenExpiration int    // 刷新token过期时间（分钟）
	Issuer                 string // token发行者
}

type ARKConfig struct {
	APIKey    string
	BaseURL   string
	ModelName string
	Timeout   *time.Duration
}

type OSSConfig struct {
	// CDN 域名
	CDN string
	// 存储空间域名
	Domain          string
	AccessKeyID     string
	AccessKeySecret string
	Bucket          string
	UseSSL          bool
	// 存储区域
	Region string
}

var Cfg *Config

// IsProd 是否生产环境
func (c *Config) IsProd() bool {
	return c.Server.Env == "prod"
}

// IsDev 是否开发环境
func (c *Config) IsDev() bool {
	return c.Server.Env == "dev" || c.Server.Env == ""
}

func LoadConfig() error {
	env := os.Getenv("APP_ENV")

	// 根据APP_ENV的值决定使用哪个配置文件
	if env == "" {
		// 如果APP_ENV为空，使用默认的config.toml文件
		viper.SetConfigName("config")
	} else {
		// 如果APP_ENV不为空，使用对应环境的配置文件
		viper.SetConfigName("config-" + env)
	}

	viper.SetConfigType("toml")
	viper.AddConfigPath(".")

	// 启用环境变量自动绑定
	viper.SetEnvKeyReplacer(strings.NewReplacer(".", "_"))
	viper.AutomaticEnv()

	if err := viper.ReadInConfig(); err != nil {
		return err
	}

	if err := viper.Unmarshal(&Cfg); err != nil {
		return err
	}

	if apiKey := os.Getenv("ARK_API_KEY"); apiKey != "" {
		Cfg.ARK.APIKey = apiKey
	}

	return nil
}
