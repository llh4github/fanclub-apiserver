package servicetest

import (
	"testing"
	"time"

	"fanclub-apiserver/cache"
	"fanclub-apiserver/g"
	"fanclub-apiserver/integration/testenv"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestCacheData(t *testing.T) {
	env, err := testenv.GetTestEnvironment()
	if err != nil {
		t.Fatalf("Failed to create test environment: %v", err)
	}
	defer env.Cleanup()

	testKey := "test:cache:data"
	testData := "test-value"

	t.Run("CacheMiss_ShouldCallDataFuncAndCache", func(t *testing.T) {
		dataFuncCalled := false
		dataFunc := func() (data *string, err error) {
			dataFuncCalled = true
			return &testData, nil
		}

		result, err := cache.CacheData(5*time.Minute, env.Ctx, testKey+"1", dataFunc)

		require.NoError(t, err)
		assert.True(t, dataFuncCalled, "dataFunc should be called on cache miss")
		assert.NotNil(t, result)
		assert.Equal(t, testData, *result)
	})

	t.Run("CacheHit_ShouldReturnFromCache", func(t *testing.T) {
		cacheKey := testKey + "2"
		_ = g.Redis.Set(env.Ctx, cacheKey, `"cached-value"`, 5*time.Minute).Err()

		dataFuncCalled := false
		dataFunc := func() (data *string, err error) {
			dataFuncCalled = true
			return nil, nil
		}

		result, err := cache.CacheData(5*time.Minute, env.Ctx, cacheKey, dataFunc)

		require.NoError(t, err)
		assert.False(t, dataFuncCalled, "dataFunc should not be called on cache hit")
		assert.NotNil(t, result)
		assert.Equal(t, "cached-value", *result)

		_ = g.Redis.Del(env.Ctx, cacheKey).Err()
	})

	t.Run("DataFuncError_ShouldReturnError", func(t *testing.T) {
		expectedErr := assert.AnError
		dataFunc := func() (data *string, err error) {
			return nil, expectedErr
		}

		result, err := cache.CacheData(5*time.Minute, env.Ctx, testKey+"3", dataFunc)

		assert.Error(t, err)
		assert.Nil(t, result)
		assert.Equal(t, expectedErr, err)
	})

	t.Run("MarshalError_ShouldReturnDataWithoutCaching", func(t *testing.T) {
		dataFuncCalled := false
		dataFunc := func() (data *chan int, err error) {
			dataFuncCalled = true
			return new(make(chan int)), nil
		}

		result, err := cache.CacheData(5*time.Minute, env.Ctx, testKey+"4", dataFunc)

		require.NoError(t, err)
		assert.True(t, dataFuncCalled, "dataFunc should be called")
		assert.NotNil(t, result)
	})
}
