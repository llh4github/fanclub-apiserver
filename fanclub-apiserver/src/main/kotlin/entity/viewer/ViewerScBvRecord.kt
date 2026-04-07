/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.entity.viewer

import io.swagger.v3.oas.annotations.media.Schema
import llh.fanclubvup.apiserver.entity.BaseEntity
import llh.fanclubvup.common.BID
import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.Table
import java.time.LocalDateTime


@Entity
@Table(name = "viewer_sc_bv_record")
@Schema(title = "观众SC点播的BV号记录")
interface ViewerScBvRecord : BaseEntity {

    @Key
    @get:Schema(title = "SC的ID")
    val scId: Long

    @Column(name = "room_id")
    @get:Schema(title = "房间ID")
    val roomId: Long

    @get:Schema(title = "发送的BV号")
    val bv: String

    @get:Schema(title = "发送者的BID")
    @Column(name = "bid")
    val bid: BID

    @get:Schema(title = "发送时间")
    @Column(name = "send_time")
    val sendTime: LocalDateTime
}
