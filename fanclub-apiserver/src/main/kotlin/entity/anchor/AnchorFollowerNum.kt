/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.entity.anchor

import io.swagger.v3.oas.annotations.media.Schema
import llh.fanclubvup.apiserver.entity.AnchorBidAware
import llh.fanclubvup.apiserver.entity.BaseEntity
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.Table
import java.time.LocalDate

@Entity
@Table(name = "anchor_follower_num")
@Schema(title = "主播粉丝数")
interface AnchorFollowerNum : BaseEntity, AnchorBidAware {

    @get:Schema(title = "粉丝数", description = "粉丝数", example = "114514")
    val followerNum: Int


    @Key(group = "uid_cnt_date_uniq")
    @get:Schema(title = "统计日期", description = "统计日期", example = "2023-01-01")
    val cntDate: LocalDate

}
