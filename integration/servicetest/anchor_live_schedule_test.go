package servicetest

import (
	"testing"
	"time"

	"fanclub-apiserver/database/model"
	"fanclub-apiserver/g"
	"fanclub-apiserver/integration/testenv"
	"fanclub-apiserver/services"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"gorm.io/cli/gorm/typed"
)

func TestAnchorLiveSchedule_GetWeeklySchedule(t *testing.T) {
	env, err := testenv.GetTestEnvironment()
	if err != nil {
		t.Fatalf("Failed to create test environment: %v", err)
	}
	defer env.Cleanup()

	scheduleQ := typed.G[model.AnchorLiveSchedule](g.DB)

	t.Run("GetWeeklySchedule_WithData", func(t *testing.T) {
		now := time.Now()
		weekday := now.Weekday()
		startOfWeek := now.AddDate(0, 0, -int(weekday))
		startOfWeek = time.Date(startOfWeek.Year(), startOfWeek.Month(), startOfWeek.Day(), 0, 0, 0, 0, startOfWeek.Location())

		testBid := int64(12345678)

		testSchedules := []*model.AnchorLiveSchedule{
			{
				Bid:       testBid,
				Topic:     "游戏直播",
				Emoji:     "🎮",
				StartTime: startOfWeek.Add(24 * time.Hour),
				EndTime:   startOfWeek.Add(26 * time.Hour),
			},
			{
				Bid:       testBid,
				Topic:     "歌回",
				Emoji:     "🎵",
				StartTime: startOfWeek.Add(48 * time.Hour),
				EndTime:   startOfWeek.Add(50 * time.Hour),
			},
			{
				Bid:       testBid,
				Topic:     "杂谈",
				Emoji:     "💬",
				StartTime: startOfWeek.Add(72 * time.Hour),
				EndTime:   startOfWeek.Add(74 * time.Hour),
			},
		}

		for _, s := range testSchedules {
			err := scheduleQ.Create(env.Ctx, s)
			require.NoError(t, err)
		}

		appCtx := &g.AppCtx{C: env.Ctx}
		results, err := services.AnchorLiveSchedule.GetWeeklySchedule(appCtx, testBid)
		assert.NoError(t, err)
		assert.Len(t, results, 3)

		assert.Equal(t, "游戏直播", results[0].Topic)
		assert.Equal(t, "🎮", results[0].Emoji)
		assert.Equal(t, "歌回", results[1].Topic)
		assert.Equal(t, "🎵", results[1].Emoji)
		assert.Equal(t, "杂谈", results[2].Topic)
		assert.Equal(t, "💬", results[2].Emoji)
	})

	t.Run("GetWeeklySchedule_NoData", func(t *testing.T) {
		appCtx := &g.AppCtx{C: env.Ctx}
		results, err := services.AnchorLiveSchedule.GetWeeklySchedule(appCtx, int64(99999999))
		assert.NoError(t, err)
		assert.Empty(t, results)
	})

	t.Run("GetWeeklySchedule_WithOutdatedData", func(t *testing.T) {
		testBid := int64(87654321)

		lastWeek := time.Now().AddDate(0, 0, -7)
		outdatedSchedule := &model.AnchorLiveSchedule{
			Bid:       testBid,
			Topic:     "过期日程",
			Emoji:     "📅",
			StartTime: lastWeek,
			EndTime:   lastWeek.Add(2 * time.Hour),
		}

		err := scheduleQ.Create(env.Ctx, outdatedSchedule)
		require.NoError(t, err)

		appCtx := &g.AppCtx{C: env.Ctx}
		results, err := services.AnchorLiveSchedule.GetWeeklySchedule(appCtx, testBid)
		assert.NoError(t, err)
		assert.Empty(t, results)
	})
}