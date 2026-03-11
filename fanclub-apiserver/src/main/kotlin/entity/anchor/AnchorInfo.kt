/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.entity.anchor

import io.swagger.v3.oas.annotations.media.Schema
import llh.fanclubvup.apiserver.entity.BaseEntity
import llh.fanclubvup.apiserver.entity.sys.ScraperFeature
import llh.fanclubvup.apiserver.entity.sys.User
import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.OneToOne
import org.babyfish.jimmer.sql.Table

@Entity
@Table(name = "anchor_info")
@Schema(title = "主播基础信息")
interface AnchorInfo : BaseEntity {
    @get:Schema(title = "B站ID", description = "通常称为UID", example = "114514")
    val biliId: Long


    @Column(name = "bili_name")
    @get:Schema(title = "B站昵称", description = "B站昵称", example = "Tom")
    val biliName: String

    @Column(name = "room_id")
    @get:Schema(title = "直播间ID", description = "直播间ID", example = "114514")
    val roomId: Long

    @OneToOne
    @JoinColumn(name = "user_id")
    val user: User

    @OneToOne
    val feature: ScraperFeature
}
