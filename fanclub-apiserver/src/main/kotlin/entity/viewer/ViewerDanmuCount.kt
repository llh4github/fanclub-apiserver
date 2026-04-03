/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.entity.viewer

import io.swagger.v3.oas.annotations.media.Schema
import llh.fanclubvup.apiserver.entity.BaseEntity
import llh.fanclubvup.common.BID
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.Table
import java.time.LocalDate


@Entity
@Table(name = "viewer_danmu_count")
@Schema(title = "观众弹幕数量统计")
interface ViewerDanmuCount : BaseEntity {

    @Key(group = "bid-date")
    @get:Schema(title = "B站UID", example = "114514")
    val bid: BID

    @Key(group = "bid-date")
    @get:Schema(title = "接收者的B站UID", example = "114514")
    val rbid:BID

    @get:Schema(title = "统计时间", example = "2023-07-01")
    @Key(group = "bid-date")
    val cntDate: LocalDate

    @get:Schema(title = "弹幕数量", example = "114514")
    val cnt: Int

}
