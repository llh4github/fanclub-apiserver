package g

import (
	"context"
	"fmt"
	"time"

	"fanclub-apiserver/database/model"

	"gorm.io/driver/postgres"
	"gorm.io/gorm"
)

// DB 全局 gorm 数据库实例
var DB *gorm.DB

// InitDB 初始化数据库连接
func InitDB() error {
	var err error

	// 设置 model 包的 ID 生成函数
	model.GenerateID = NextID

	dsn := Cfg.Database.Path

	// 配置 gorm
	gormConfig := &gorm.Config{}
	if Cfg.Database.Debug {
		Info("ORM启用 debug 功能")
		gormConfig = &gorm.Config{
			// 开启详细日志
		}
	}

	// 连接数据库
	DB, err = gorm.Open(postgres.Open(dsn), gormConfig)
	if err != nil {
		return fmt.Errorf("failed to connect to database: %w", err)
	}

	// 配置连接池
	sqlDB, err := DB.DB()
	if err != nil {
		return fmt.Errorf("failed to get sql.DB: %w", err)
	}

	// 测试数据库连接
	ttlCxt, cancel := context.WithDeadline(Ctx, time.Now().Add(time.Second*3))
	defer cancel()
	if err := sqlDB.PingContext(ttlCxt); err != nil {
		return fmt.Errorf("failed to test database connection: %w", err)
	}

	// 开启 debug 日志
	if Cfg.Database.Debug {
		DB = DB.Debug()
	}

	// 根据配置决定是否自动迁移
	if Cfg.Database.AutoMigrate {
		err = DB.AutoMigrate(model.Models()...)
		if err != nil {
			return fmt.Errorf("failed to auto migrate: %w", err)
		}
		Info("Database migrated successfully")
	}

	Info("Database connected successfully")
	return nil
}
