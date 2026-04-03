/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.entity.anchor

import io.swagger.v3.oas.annotations.media.Schema
import llh.fanclubvup.apiserver.entity.BaseEntity
import org.babyfish.jimmer.sql.*
import java.time.LocalDate

@Entity
@Schema(title = "主播每日直播时长统计")
@Table(name = "anchor_live_duration")
@KeyUniqueConstraint
interface AnchorLiveDuration : BaseEntity {

    @Key(group = "live_stat_date_uk")
    @Column(name = "room_id")
    @get:Schema(title = "直播间ID", description = "直播间ID", example = "114514")
    val roomId: Long

    @Key(group = "live_stat_date_uk")
    @Column(name = "stat_date")
    @get:Schema(title = "统计日期", description = "统计日期", example = "2023-01-01")
    val statDate: LocalDate

    @Column(name = "live_duration")
    @get:Schema(title = "直播时长(秒)", example = "114514")
    val liveDuration: Long
}
