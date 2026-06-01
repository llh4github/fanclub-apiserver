package services

import (
	"context"
	"fmt"
	"strings"
	"time"

	"fanclub-apiserver/auth"
	"fanclub-apiserver/database/generated"
	"fanclub-apiserver/database/model"
	"fanclub-apiserver/dto/req"
	"fanclub-apiserver/dto/resp"
	"fanclub-apiserver/g"

	"go.uber.org/zap"
)

// cookieService Cookie 管理服务
type cookieService struct {
}

var Cookie = new(cookieService)

// List 获取 Cookie 列表（分页）
func (s *cookieService) List(ctx context.Context, req *req.CookieListReq) ([]resp.CookieInfo, int64, error) {
	query := g.DB.WithContext(ctx).Model(&model.SysScraperCookie{})

	// 按是否需要刷新筛选
	if req.NeedRefresh != nil {
		query = query.Where("need_refresh = ?", *req.NeedRefresh)
	}

	// 计算总数
	var total int64
	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	// 分页查询
	pageSize := req.PageSize
	if pageSize <= 0 {
		pageSize = 10
	}
	pageIndex := req.PageIndex
	if pageIndex < 0 {
		pageIndex = 0
	}
	offset := pageIndex * pageSize

	var cookies []model.SysScraperCookie
	if err := query.Order("updated_at DESC").Offset(offset).Limit(pageSize).Find(&cookies).Error; err != nil {
		return nil, 0, err
	}

	// 转换为响应结构
	result := make([]resp.CookieInfo, len(cookies))
	for i, c := range cookies {
		result[i] = resp.CookieInfo{
			ID:              c.ID,
			Name:            c.Name,
			Value:           maskCookieValue(c.Name, c.Value),
			Domain:          c.Domain,
			ExpiresAt:       c.ExpiresAt,
			UID:             c.UID,
			NeedRefresh:     c.NeedRefresh,
			LastRefreshTime: c.LastRefreshTime,
			CreatedAt:       c.CreatedTime.UnixMilli(),
			UpdatedAt:       c.UpdatedTime.UnixMilli(),
		}
	}

	return result, total, nil
}

// GetByID 根据 ID 获取 Cookie
func (s *cookieService) GetByID(ctx context.Context, id int64) (*model.SysScraperCookie, error) {
	var cookie model.SysScraperCookie
	if err := g.DB.WithContext(ctx).First(&cookie, id).Error; err != nil {
		return nil, err
	}
	return &cookie, nil
}

// Create 创建 Cookie
func (s *cookieService) Create(ctx context.Context, req *req.CookieCreateReq) (*model.SysScraperCookie, error) {
	cookie := &model.SysScraperCookie{
		Name:        req.Name,
		Value:       req.Value,
		Domain:      req.Domain,
		ExpiresAt:   req.ExpiresAt,
		UID:         req.UID,
		NeedRefresh: true,
	}

	if err := g.DB.WithContext(ctx).Create(cookie).Error; err != nil {
		return nil, err
	}
	return cookie, nil
}

// Update 更新 Cookie
func (s *cookieService) Update(ctx context.Context, id int64, req *req.CookieUpdateReq) error {
	updates := map[string]interface{}{
		"name":         req.Name,
		"value":        req.Value,
		"domain":       req.Domain,
		"need_refresh": req.NeedRefresh,
	}
	if req.ExpiresAt != nil {
		updates["expires_at"] = *req.ExpiresAt
	}

	return g.DB.WithContext(ctx).Model(&model.SysScraperCookie{}).
		Where(generated.BaseModel.ID.Eq(id)).
		Updates(updates).Error
}

// Delete 删除 Cookie
func (s *cookieService) Delete(ctx context.Context, id int64) error {
	return g.DB.WithContext(ctx).Delete(&model.SysScraperCookie{}, id).Error
}

// MarkRefreshed 标记 Cookie 已刷新
func (s *cookieService) MarkRefreshed(ctx context.Context, id int64) error {
	now := time.Now().UnixMilli()
	return g.DB.WithContext(ctx).Model(&model.SysScraperCookie{}).
		Where(generated.BaseModel.ID.Eq(id)).
		Updates(map[string]interface{}{
			"need_refresh":      false,
			"last_refresh_time": now,
		}).Error
}

// maskCookieValue 对 Cookie 值进行脱敏处理
func maskCookieValue(name, value string) string {
	// 特殊处理 SESSDATA 和 RefreshToken
	if strings.EqualFold(name, "SESSDATA") && len(value) > 16 {
		return value[:8] + "..." + value[len(value)-8:]
	}
	if strings.EqualFold(name, "RefreshToken") && len(value) > 16 {
		return value[:8] + "..." + value[len(value)-8:]
	}
	// 其他值保留原样
	if len(value) <= 8 {
		return strings.Repeat("*", len(value))
	}
	return value[:4] + "..." + value[len(value)-4:]
}

// cookieRefreshService Cookie 刷新服务
type cookieRefreshService struct {
}

var CookieRefresh = new(cookieRefreshService)

// ShouldRefresh 检查是否需要刷新 Cookie
func (s *cookieRefreshService) ShouldRefresh(cookies *auth.Cookies) (bool, int64, error) {
	refresher := auth.NewCookieRefresher(cookies)
	return refresher.ShouldRefresh()
}

// Refresh 刷新 Cookie
func (s *cookieRefreshService) Refresh(cookies *auth.Cookies) (*auth.RefreshResult, error) {
	refresher := auth.NewCookieRefresher(cookies)
	return refresher.Refresh(), nil
}

// RefreshByID 根据 ID 刷新指定 Cookie
func (s *cookieRefreshService) RefreshByID(ctx context.Context, cookieID int64) (*resp.CookieRefreshResp, error) {
	cookie, err := Cookie.GetByID(ctx, cookieID)
	if err != nil {
		return &resp.CookieRefreshResp{
			ID:      cookieID,
			Success: false,
			Message: fmt.Sprintf("获取Cookie失败: %v", err),
		}, err
	}

	// 如果没有 RefreshToken，无法刷新
	if cookie.Name != "RefreshToken" {
		// 需要找到对应的 RefreshToken Cookie
		var refreshTokenCookie model.SysScraperCookie
		if err := g.DB.WithContext(ctx).
			Where("uid = ? AND name = ?", cookie.UID, "RefreshToken").
			First(&refreshTokenCookie).Error; err != nil {
			return &resp.CookieRefreshResp{
				ID:      cookieID,
				Success: false,
				Message: "未找到 RefreshToken Cookie",
			}, nil
		}

		// 组合 cookies
		cookies := buildCookiesFromDBRecord(cookie, &refreshTokenCookie)
		result, err := s.Refresh(cookies)
		if err != nil || result.Error != nil {
			return &resp.CookieRefreshResp{
				ID:      cookieID,
				Success: false,
				Message: fmt.Sprintf("刷新失败: %v", result.Error),
			}, err
		}

		// 更新 Cookie 值
		if err := s.updateCookiesFromResult(ctx, cookie.UID, result.NewCookies, result.NewRefreshToken); err != nil {
			return &resp.CookieRefreshResp{
				ID:      cookieID,
				Success: false,
				Message: fmt.Sprintf("更新Cookie失败: %v", err),
			}, err
		}

		Cookie.MarkRefreshed(ctx, cookieID)
		Cookie.MarkRefreshed(ctx, refreshTokenCookie.ID)

		return &resp.CookieRefreshResp{
			ID:              cookieID,
			Success:         true,
			Message:         "刷新成功",
			NewRefreshToken: result.NewRefreshToken,
		}, nil
	}

	// 直接刷新 RefreshToken Cookie
	cookies := &auth.Cookies{
		SESSDATA:     getCookieValue(cookie, "SESSDATA"),
		BiliJct:      getCookieValue(cookie, "bili_jct"),
		DedeUserID:   getCookieValue(cookie, "DedeUserID"),
		RefreshToken: cookie.Value,
	}

	result, err := s.Refresh(cookies)
	if err != nil || result.Error != nil {
		return &resp.CookieRefreshResp{
			ID:      cookieID,
			Success: false,
			Message: fmt.Sprintf("刷新失败: %v", result.Error),
		}, err
	}

	// 更新 Cookie 值
	if err := s.updateCookiesFromResult(ctx, cookie.UID, result.NewCookies, result.NewRefreshToken); err != nil {
		return &resp.CookieRefreshResp{
			ID:      cookieID,
			Success: false,
			Message: fmt.Sprintf("更新Cookie失败: %v", err),
		}, err
	}

	Cookie.MarkRefreshed(ctx, cookieID)

	return &resp.CookieRefreshResp{
		ID:              cookieID,
		Success:         true,
		Message:         "刷新成功",
		NewRefreshToken: result.NewRefreshToken,
	}, nil
}

// RefreshAll 刷新所有需要刷新的 Cookie
func (s *cookieRefreshService) RefreshAll(ctx context.Context) (*resp.CookieBatchRefreshResp, error) {
	// 查找所有需要刷新的 Cookie
	var cookies []model.SysScraperCookie
	if err := g.DB.WithContext(ctx).
		Where("need_refresh = ?", true).
		Find(&cookies).Error; err != nil {
		return nil, err
	}

	resp := &resp.CookieBatchRefreshResp{
		Total:   len(cookies),
		Results: make([]resp.CookieRefreshResp, 0, len(cookies)),
	}

	for _, cookie := range cookies {
		result, err := s.RefreshByID(ctx, cookie.ID)
		if err != nil {
			g.Error("刷新Cookie失败",
				zap.Int64("cookieID", cookie.ID),
				zap.Error(err),
			)
			resp.FailedCount++
		} else {
			if result.Success {
				resp.SuccessCount++
			} else {
				resp.FailedCount++
			}
		}
		resp.Results = append(resp.Results, *result)
	}

	g.Info("批量刷新Cookie完成",
		zap.Int("total", resp.Total),
		zap.Int("success", resp.SuccessCount),
		zap.Int("failed", resp.FailedCount),
	)

	return resp, nil
}

// buildCookiesFromDBRecord 从数据库记录构建 Cookies 对象
func buildCookiesFromDBRecord(sessdataCookie, refreshTokenCookie *model.SysScraperCookie) *auth.Cookies {
	return &auth.Cookies{
		SESSDATA:     sessdataCookie.Value,
		BiliJct:      getCookieValue(sessdataCookie, "bili_jct"),
		DedeUserID:   getCookieValue(sessdataCookie, "DedeUserID"),
		RefreshToken: refreshTokenCookie.Value,
	}
}

// getCookieValue 根据名称从数据库记录中获取 Cookie 值
func getCookieValue(cookie *model.SysScraperCookie, name string) string {
	if cookie.Name == name {
		return cookie.Value
	}
	return ""
}

// updateCookiesFromResult 根据刷新结果更新数据库中的 Cookie
func (s *cookieRefreshService) updateCookiesFromResult(ctx context.Context, uid int64, newCookies *auth.Cookies, newRefreshToken string) error {
	if newCookies != nil {
		if newCookies.SESSDATA != "" {
			if err := g.DB.WithContext(ctx).Model(&model.SysScraperCookie{}).
				Where("uid = ? AND name = ?", uid, "SESSDATA").
				Update("value", newCookies.SESSDATA).Error; err != nil {
				return err
			}
		}
	}
	if newRefreshToken != "" {
		if err := g.DB.WithContext(ctx).Model(&model.SysScraperCookie{}).
			Where("uid = ? AND name = ?", uid, "RefreshToken").
			Update("value", newRefreshToken).Error; err != nil {
			return err
		}
	}
	return nil
}
