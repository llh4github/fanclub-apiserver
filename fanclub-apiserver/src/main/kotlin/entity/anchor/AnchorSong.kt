/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.entity.anchor

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern
import llh.fanclubvup.apiserver.entity.BaseEntity
import llh.fanclubvup.apiserver.utils.CreateUpdateGroup
import llh.fanclubvup.common.BID
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.Table
import org.hibernate.validator.constraints.Length
import org.hibernate.validator.constraints.Range

@Entity
@Schema(title = "主播歌曲")
@Table(name = "anchor_song")
interface AnchorSong : BaseEntity {

    @Key(group = "bid_name_uniq")
    @get:Schema(title = "主播BID", description = "B站ID", example = "114514")
    val bid: BID

    @Key(group = "bid_name_uniq")
    @get:Length(max = 255, groups = [CreateUpdateGroup::class])
    @get:Schema(title = "歌曲名", description = "歌曲名称", example = "Tom")
    val name: String

    @get:Range(min = 0, max = 1_0000, groups = [CreateUpdateGroup::class])
    @get:Schema(title = "歌曲价格(元)", description = "歌曲价格", example = "114514")
    val price: Int

    @get:Pattern(
        regexp = "^BV[A-Za-z0-9]{10}$",
        message = "Bv号必须以BV开头，后跟10位字母或数字",
        groups = [CreateUpdateGroup::class]
    )
    @get:Schema(title = "歌曲Bv", description = "歌曲Bv", example = "BV1K4411H7kK")
    val bv: String
}
