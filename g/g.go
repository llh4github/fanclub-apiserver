package g

import (
	"context"

	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"
)

var (
	Logger *zap.Logger
	Ctx    = context.Background()
)

func Init(logger *zap.Logger) {
	Logger = logger
}

func Sync() error {
	if Logger == nil {
		return nil
	}
	return Logger.Sync()
}

func Debug(msg string, fields ...zap.Field) {
	if Logger != nil {
		Logger.Debug(msg, fields...)
	}
}

func Info(msg string, fields ...zap.Field) {
	if Logger != nil {
		Logger.Info(msg, fields...)
	}
}

func Warn(msg string, fields ...zap.Field) {
	if Logger != nil {
		Logger.Warn(msg, fields...)
	}
}

func Error(msg string, fields ...zap.Field) {
	if Logger != nil {
		Logger.Error(msg, fields...)
	}
}

func DPanic(msg string, fields ...zap.Field) {
	if Logger != nil {
		Logger.DPanic(msg, fields...)
	}
}

func Panic(msg string, fields ...zap.Field) {
	if Logger != nil {
		Logger.Panic(msg, fields...)
	}
}

func Fatal(msg string, fields ...zap.Field) {
	if Logger != nil {
		Logger.Fatal(msg, fields...)
	}
}

func Debugf(msg string, args ...any) {
	if Logger != nil {
		Logger.Sugar().Debugf(msg, args...)
	}
}

func Infof(msg string, args ...any) {
	if Logger != nil {
		Logger.Sugar().Infof(msg, args...)
	}
}

func Warnf(msg string, args ...any) {
	if Logger != nil {
		Logger.Sugar().Warnf(msg, args...)
	}
}

func Errorf(msg string, args ...any) {
	if Logger != nil {
		Logger.Sugar().Errorf(msg, args...)
	}
}

func DPanicf(msg string, args ...any) {
	if Logger != nil {
		Logger.Sugar().DPanicf(msg, args...)
	}
}

func Panicf(msg string, args ...any) {
	if Logger != nil {
		Logger.Sugar().Panicf(msg, args...)
	}
}

func Fatalf(msg string, args ...any) {
	if Logger != nil {
		Logger.Sugar().Fatalf(msg, args...)
	}
}

func With(fields ...zap.Field) *zap.Logger {
	if Logger == nil {
		return nil
	}
	return Logger.With(fields...)
}

func WithOptions(opts ...zap.Option) *zap.Logger {
	if Logger == nil {
		return nil
	}
	return Logger.WithOptions(opts...)
}

func Named(s string) *zap.Logger {
	if Logger == nil {
		return nil
	}
	return Logger.Named(s)
}

func Check(lvl zapcore.Level, msg string) *zapcore.CheckedEntry {
	if Logger == nil {
		return nil
	}
	return Logger.Check(lvl, msg)
}

func Core() zapcore.Core {
	if Logger == nil {
		return nil
	}
	return Logger.Core()
}
