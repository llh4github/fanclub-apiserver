package g

import (
	"os"
	"path/filepath"
	"time"

	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"
)

func InitLogger(level string) error {
	// 确保logs目录存在
	if err := os.MkdirAll("logs", 0755); err != nil {
		return err
	}

	// 配置编码器
	encoderConfig := zapcore.EncoderConfig{
		TimeKey:        "time",
		LevelKey:       "level",
		NameKey:        "logger",
		CallerKey:      "caller",
		MessageKey:     "msg",
		StacktraceKey:  "stacktrace",
		LineEnding:     zapcore.DefaultLineEnding,
		EncodeLevel:    zapcore.CapitalLevelEncoder,
		EncodeTime:     zapcore.ISO8601TimeEncoder,
		EncodeDuration: zapcore.StringDurationEncoder,
		EncodeCaller:   zapcore.ShortCallerEncoder,
	}

	// 控制台输出
	consoleEncoder := zapcore.NewConsoleEncoder(encoderConfig)
	consoleWriter := zapcore.AddSync(os.Stdout)

	// 通用日志文件输出（每日滚动）
	logFileName := filepath.Join("logs", time.Now().Format("2006-01-02")+".log")
	file, err := os.OpenFile(logFileName, os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0644)
	if err != nil {
		return err
	}
	fileEncoder := zapcore.NewJSONEncoder(encoderConfig)
	fileWriter := zapcore.AddSync(file)

	// Error日志文件输出（每日滚动）
	errorLogFileName := filepath.Join("logs", time.Now().Format("2006-01-02")+"_error.log")
	errorFile, err := os.OpenFile(errorLogFileName, os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0644)
	if err != nil {
		return err
	}
	errorFileEncoder := zapcore.NewJSONEncoder(encoderConfig)
	errorFileWriter := zapcore.AddSync(errorFile)

	// 配置日志级别（从配置文件读取，默认为info）
	logLevel := zapcore.InfoLevel
	if level == "debug" {
		logLevel = zapcore.DebugLevel
	}

	// 创建核心：控制台输出、通用日志文件、error日志文件
	core := zapcore.NewTee(
		zapcore.NewCore(consoleEncoder, consoleWriter, logLevel),
		zapcore.NewCore(fileEncoder, fileWriter, logLevel),
		zapcore.NewCore(errorFileEncoder, errorFileWriter, zapcore.ErrorLevel),
	)

	// 创建Logger
	Logger = zap.New(core, zap.AddCaller(), zap.AddCallerSkip(1))
	Init(Logger)

	return nil
}
