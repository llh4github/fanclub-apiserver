package llh.fanclubvup.apiserver.entity

import io.swagger.v3.oas.annotations.media.Schema
import llh.fanclubvup.common.BID
import org.babyfish.jimmer.sql.MappedSuperclass

@MappedSuperclass
interface AnchorBidAware {

    @get:Schema(title = "主播BID", description = "B站ID", example = "114514")
    val bid: BID
}
