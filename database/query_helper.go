package database

import (
	"context"

	"fanclub-apiserver/dto/resp"
	"fanclub-apiserver/g"

	"gorm.io/cli/gorm/typed"
	"gorm.io/gorm/clause"
)

// Page 分页查询，基于 typed.G[T] 实现
func Page[T any](ctx context.Context, page, pageSize int, conds ...clause.Expression) (*resp.PageResult[*T], error) {
	if page <= 0 {
		page = 1
	}
	if pageSize <= 0 {
		pageSize = 10
	}

	q := typed.G[T](g.DB, conds...)

	// 统计总数
	total, err := q.Count(ctx, "id")
	if err != nil {
		return nil, err
	}

	if total == 0 {
		return &resp.PageResult[*T]{
			TotalRowCount: 0,
			TotalPage:     0,
			Records:       []*T{},
		}, nil
	}

	totalPage := (total + int64(pageSize) - 1) / int64(pageSize)
	offset := (page - 1) * pageSize

	records, err := typed.G[T](g.DB, conds...).Offset(offset).Limit(pageSize).Find(ctx)
	if err != nil {
		return nil, err
	}

	// 转换为指针切片
	recordPtrs := make([]*T, len(records))
	for i := range records {
		recordPtrs[i] = &records[i]
	}

	return &resp.PageResult[*T]{
		TotalRowCount: int(total),
		TotalPage:     int(totalPage),
		Records:       recordPtrs,
	}, nil
}

// GetOne 根据条件获取单条记录
func GetOne[T any](ctx context.Context, conds ...clause.Expression) (*T, error) {
	result, err := typed.G[T](g.DB, conds...).First(ctx)
	if err != nil {
		return nil, err
	}
	return &result, nil
}

// ScanList 扫描查询结果到自定义类型列表
func ScanList[R any, T any](ctx context.Context, conds ...clause.Expression) ([]*R, error) {
	var records []*R
	err := typed.G[T](g.DB, conds...).Scan(ctx, &records)
	if err != nil {
		return nil, err
	}
	return records, nil
}
