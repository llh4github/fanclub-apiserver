/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.entity.sys

import io.swagger.v3.oas.annotations.media.Schema
import llh.fanclubvup.apiserver.entity.BaseEntity
import llh.fanclubvup.apiserver.entity.anchor.AnchorInfo
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.JoinColumn
import org.babyfish.jimmer.sql.OneToOne
import org.babyfish.jimmer.sql.Table

@Entity
@Table(name = "sys_scraper_feature")
@Schema(title = "爬虫功能配置")
interface ScraperFeature : BaseEntity {

    @get:Schema(title = "是否获取粉丝数", description = "是否获取粉丝数", example = "true")
    val follower: Boolean

    @get:Schema(title = "是否数据监控", description = "是否数据监控", example = "true")
    val monitor: Boolean

    @OneToOne
    @JoinColumn(name = "anchor_id")
    val anchorInfo: AnchorInfo
}
