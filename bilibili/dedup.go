package bilibili

import (
	"time"

	goCache "github.com/patrickmn/go-cache"
)

// dedupCache 消息去重缓存，基于TTL过期策略
type dedupCache struct {
	c *goCache.Cache
}

// newDedupCache 创建去重缓存
func newDedupCache(ttl time.Duration) *dedupCache {
	return &dedupCache{
		c: goCache.New(ttl, ttl*2),
	}
}

// isDuplicate 检查消息是否在TTL内已处理过
func (c *dedupCache) isDuplicate(key string) bool {
	_, found := c.c.Get(key)
	if found {
		return true
	}
	c.c.Set(key, true, goCache.DefaultExpiration)
	return false
}
