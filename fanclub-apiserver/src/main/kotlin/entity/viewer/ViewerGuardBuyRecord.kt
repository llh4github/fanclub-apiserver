package llh.fanclubvup.apiserver.entity.viewer

import io.swagger.v3.oas.annotations.media.Schema
import llh.fanclubvup.apiserver.consts.enums.GuardLevel
import llh.fanclubvup.apiserver.entity.BaseEntity
import llh.fanclubvup.common.BID
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Table
import java.time.LocalDateTime

@Entity
@Table(name = "viewer_guard_buy_record")
@Schema(title = "观众的舰长购买记录")
interface ViewerGuardBuyRecord : BaseEntity {
    val bid: BID

    @get:Schema(title = "购买数量", description = "购买数量", example = "114514")
    val num: Int

    @get:Schema(title = "舰长等级", description = "舰长等级", example = "1")
    val guardType: GuardLevel

    @get:Schema(title = "舰长价格", description = "舰长价格", example = "114514")
    val price: Int

    @get:Schema(title = "舰长开始时间", description = "舰长开始时间", example = "114514")
    val startTime: LocalDateTime?

    @get:Schema(title = "舰长结束时间", description = "舰长结束时间", example = "114514")
    val endTime: LocalDateTime?
}
