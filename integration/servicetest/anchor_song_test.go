package servicetest

import (
	"testing"

	"fanclub-apiserver/database/generated"
	"fanclub-apiserver/database/model"
	"fanclub-apiserver/dto/req"
	"fanclub-apiserver/g"
	"fanclub-apiserver/integration/testenv"
	"fanclub-apiserver/services"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"gorm.io/cli/gorm/typed"
)

func TestAnchorSongService(t *testing.T) {
	env, err := testenv.GetTestEnvironment()
	if err != nil {
		t.Fatalf("Failed to create test environment: %v", err)
	}
	defer env.Cleanup()

	songQ := typed.G[model.AnchorSong](g.DB)

	appCtx := &g.AppCtx{C: env.Ctx}

	t.Run("Create_ShouldCreateAnchorSong", func(t *testing.T) {
		song := &model.AnchorSong{
			Bid:   12345678,
			Name:  "晴天",
			Price: 30,
			Bv:    "BV1xK4y1b7NP",
		}

		err := services.AnchorSong.Create(appCtx, song)
		assert.NoError(t, err)
		assert.NotZero(t, song.ID)

		createdSong, err := songQ.Where(generated.BaseModel.ID.Eq(song.ID)).First(env.Ctx)
		assert.NoError(t, err)
		assert.Equal(t, int64(12345678), createdSong.Bid)
		assert.Equal(t, "晴天", createdSong.Name)
		assert.Equal(t, 30, createdSong.Price)
		assert.Equal(t, "BV1xK4y1b7NP", createdSong.Bv)
	})

	t.Run("GetByBidAndName_ShouldReturnSong", func(t *testing.T) {
		song := &model.AnchorSong{
			Bid:   87654321,
			Name:  "夜曲",
			Price: 50,
			Bv:    "BV2yK5y2b8NQ",
		}
		err := songQ.Create(env.Ctx, song)
		require.NoError(t, err)

		foundSong, err := services.AnchorSong.GetByBidAndName(appCtx, 87654321, "夜曲")
		assert.NoError(t, err)
		assert.NotNil(t, foundSong)
		assert.Equal(t, int64(87654321), foundSong.Bid)
		assert.Equal(t, "夜曲", foundSong.Name)
	})

	t.Run("GetByBidAndName_ShouldReturnErrorWhenNotFound", func(t *testing.T) {
		_, err := services.AnchorSong.GetByBidAndName(appCtx, 99999999, "不存在的歌")
		assert.Error(t, err)
	})

	t.Run("UpdateByID_ShouldUpdateSong", func(t *testing.T) {
		song := &model.AnchorSong{
			Bid:   11111111,
			Name:  "稻香",
			Price: 20,
			Bv:    "BV3zL6z3c9OR",
		}
		err := songQ.Create(env.Ctx, song)
		require.NoError(t, err)

		updateReq := &req.UpdateAnchorSong{
			ID:    song.ID,
			Name:  "稻香-新版",
			Price: 25,
		}

		err = services.AnchorSong.UpdateByID(appCtx, updateReq)
		assert.NoError(t, err)

		updatedSong, err := songQ.Where(generated.BaseModel.ID.Eq(song.ID)).First(env.Ctx)
		assert.NoError(t, err)
		assert.Equal(t, "稻香-新版", updatedSong.Name)
		assert.Equal(t, 25, updatedSong.Price)
		assert.Equal(t, "BV3zL6z3c9OR", updatedSong.Bv)
	})

	t.Run("UpdateByID_ShouldReturnErrorWhenSongNotFound", func(t *testing.T) {
		updateReq := &req.UpdateAnchorSong{
			ID:   99999999,
			Name: "test",
		}

		err := services.AnchorSong.UpdateByID(appCtx, updateReq)
		assert.Error(t, err)
	})

	t.Run("UpdateByID_ShouldReturnErrorWhenNameConflict", func(t *testing.T) {
		song1 := &model.AnchorSong{
			Bid:   22222222,
			Name:  "同一首歌",
			Price: 30,
		}
		err := songQ.Create(env.Ctx, song1)
		require.NoError(t, err)

		song2 := &model.AnchorSong{
			Bid:   33333333,
			Name:  "不同的歌",
			Price: 40,
		}
		err = songQ.Create(env.Ctx, song2)
		require.NoError(t, err)

		updateReq := &req.UpdateAnchorSong{
			ID:   song2.ID,
			Name: "同一首歌",
			Bid:  33333333,
		}

		err = services.AnchorSong.UpdateByID(appCtx, updateReq)
		assert.Error(t, err)
	})

	t.Run("Delete_ShouldDeleteSong", func(t *testing.T) {
		song := &model.AnchorSong{
			Bid:   44444444,
			Name:  "要删除的歌",
			Price: 10,
		}
		err := songQ.Create(env.Ctx, song)
		require.NoError(t, err)

		err = services.AnchorSong.Delete(appCtx, song.ID)
		assert.NoError(t, err)

		_, err = songQ.Where(generated.BaseModel.ID.Eq(song.ID)).First(env.Ctx)
		assert.Error(t, err)
	})

	t.Run("DeleteBatch_ShouldDeleteMultipleSongs", func(t *testing.T) {
		song1 := &model.AnchorSong{
			Bid:   55555555,
			Name:  "批量删除1",
			Price: 15,
		}
		err := songQ.Create(env.Ctx, song1)
		require.NoError(t, err)

		song2 := &model.AnchorSong{
			Bid:   55555555,
			Name:  "批量删除2",
			Price: 20,
		}
		err = songQ.Create(env.Ctx, song2)
		require.NoError(t, err)

		err = services.AnchorSong.DeleteBatch(appCtx, []int64{song1.ID, song2.ID}, 0)
		assert.NoError(t, err)

		count, err := songQ.Where(generated.BaseModel.ID.In(song1.ID, song2.ID)).Count(env.Ctx, "id")
		assert.NoError(t, err)
		assert.Equal(t, int64(0), count)
	})

	t.Run("DeleteBatch_ShouldOnlyDeleteSpecifiedBid", func(t *testing.T) {
		song1 := &model.AnchorSong{
			Bid:   66666666,
			Name:  "主播A的歌",
			Price: 25,
		}
		err := songQ.Create(env.Ctx, song1)
		require.NoError(t, err)

		song2 := &model.AnchorSong{
			Bid:   77777777,
			Name:  "主播B的歌",
			Price: 30,
		}
		err = songQ.Create(env.Ctx, song2)
		require.NoError(t, err)

		err = services.AnchorSong.DeleteBatch(appCtx, []int64{song1.ID, song2.ID}, 66666666)
		assert.NoError(t, err)

		remainingSong, err := songQ.Where(generated.BaseModel.ID.Eq(song2.ID)).First(env.Ctx)
		assert.NoError(t, err)
		assert.Equal(t, "主播B的歌", remainingSong.Name)

		_, err = songQ.Where(generated.BaseModel.ID.Eq(song1.ID)).First(env.Ctx)
		assert.Error(t, err)
	})
}
