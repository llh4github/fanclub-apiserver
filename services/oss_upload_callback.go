package services

import (
	"context"
	"database/sql"
	"time"

	"fanclub-apiserver/database"
	"fanclub-apiserver/database/generated"
	"fanclub-apiserver/database/model"
	"fanclub-apiserver/errs"
	"fanclub-apiserver/g"

	"gorm.io/cli/gorm/typed"
	"gorm.io/gorm/clause"
)

var OssUploadCallback = new(ossUploadCallbackService)

type ossUploadCallbackService struct {
}

// SaveCallback 保存七牛上传回调记录
func (s *ossUploadCallbackService) SaveCallback(ctx context.Context, callback *model.OssUploadCallback) error {
	if err := g.DB.WithContext(ctx).Create(callback).Error; err != nil {
		return errs.WrapError(err, "Failed to save oss upload callback", string(errs.DataCreateFailed))
	}
	return nil
}

// UpdateCallbackStatus 更新回调状态
func (s *ossUploadCallbackService) UpdateCallbackStatus(ctx context.Context, fileKey string, status model.CallbackStatus, errorMsg string) error {
	updates := map[string]any{
		generated.OssUploadCallback.Status.Column().Name: status,
	}
	if errorMsg != "" {
		updates[generated.OssUploadCallback.ErrorMsg.Column().Name] = errorMsg
	}

	if err := g.DB.WithContext(ctx).
		Model(&model.OssUploadCallback{}).
		Where(generated.OssUploadCallback.FileKey.Eq(fileKey)).
		Updates(updates).Error; err != nil {
		return errs.WrapError(err, "Failed to update callback status", string(errs.DataUdpateFailed))
	}
	return nil
}

// GetCallbackByKey 根据 key 查询回调记录
func (s *ossUploadCallbackService) GetCallbackByKey(ctx context.Context, fileKey string) (*model.OssUploadCallback, error) {
	callback, err := database.GetOne[model.OssUploadCallback](ctx,
		generated.OssUploadCallback.FileKey.Eq(fileKey),
	)
	if err != nil {
		if err == sql.ErrNoRows {
			return nil, nil
		}
		return nil, errs.WrapError(err, "Failed to get callback by key", string(errs.DataNotFound))
	}
	return callback, nil
}

// GetCallbackByID 根据 ID 获取回调记录
func (s *ossUploadCallbackService) GetCallbackByID(ctx context.Context, id int64) (*model.OssUploadCallback, error) {
	callback, err := database.GetOne[model.OssUploadCallback](ctx,
		generated.BaseModel.ID.Eq(id),
	)
	if err != nil {
		if err == sql.ErrNoRows {
			return nil, nil
		}
		return nil, errs.WrapError(err, "Failed to get callback by ID", string(errs.DataNotFound))
	}
	return callback, nil
}

// ListFailedCallbacks 获取失败的回调记录
func (s *ossUploadCallbackService) ListFailedCallbacks(ctx context.Context, limit int) ([]*model.OssUploadCallback, error) {
	if limit <= 0 {
		limit = 100
	}

	q := typed.G[model.OssUploadCallback](g.DB).
		Where(generated.OssUploadCallback.Status.Eq(int(model.CallbackStatusFailed))).
		Order(clause.OrderBy{
			Columns: []clause.OrderByColumn{
				generated.BaseModel.CreatedTime.Desc(),
			},
		}).
		Limit(limit)

	var results []*model.OssUploadCallback
	if err := q.Scan(ctx, &results); err != nil {
		return nil, errs.WrapError(err, "Failed to list failed callbacks", string(errs.DataNotFound))
	}
	return results, nil
}

// RetryCallback 重试处理失败的回调（更新状态为处理中）
func (s *ossUploadCallbackService) RetryCallback(ctx context.Context, fileKey string) error {
	return s.UpdateCallbackStatus(ctx, fileKey, model.CallbackStatusProcessing, "")
}

// CreateOrUpdateCallback 创建或更新回调记录（用于处理重复回调）
func (s *ossUploadCallbackService) CreateOrUpdateCallback(ctx context.Context, callback *model.OssUploadCallback) error {
	q := typed.G[model.OssUploadCallback](g.DB, clause.OnConflict{
		Columns: []clause.Column{generated.OssUploadCallback.FileKey.Column()},
		DoUpdates: clause.Assignments(map[string]any{
			generated.OssUploadCallback.FileHash.Column().Name:   callback.FileHash,
			generated.OssUploadCallback.FileSize.Column().Name:   callback.FileSize,
			generated.OssUploadCallback.MimeType.Column().Name:   callback.MimeType,
			generated.OssUploadCallback.FileName.Column().Name:   callback.FileName,
			generated.OssUploadCallback.UploadedAt.Column().Name: callback.UploadedAt,
			generated.OssUploadCallback.Status.Column().Name:     callback.Status,
			generated.OssUploadCallback.RawData.Column().Name:    callback.RawData,
			generated.BaseModel.UpdatedTime.Column().Name:        time.Now(),
		}),
	})
	if err := q.Create(ctx, callback); err != nil {
		return errs.WrapError(err, "Failed to create or update callback", string(errs.DataCreateFailed))
	}
	return nil
}
