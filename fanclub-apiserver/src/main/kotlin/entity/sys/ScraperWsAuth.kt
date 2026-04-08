package llh.fanclubvup.apiserver.entity.sys

import io.swagger.v3.oas.annotations.media.Schema
import llh.fanclubvup.apiserver.entity.BaseEntity
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.Table

/**
 * B站弹幕WS认证字符串
 *
 * 不知道为什么之前认证字符串组装失败了，这里给一个兜底机制
 */
@Entity
@Schema(title = "爬虫 WebSocket 鉴权信息")
@Table(name = "sys_scraper_ws_auth")
interface ScraperWsAuth : BaseEntity {
    @Key(group = "room_auth")
    val roomId: Long

    @Key(group = "room_auth")
    val auth: String
}
