package llh.fanclubvup.apiserver.entity

import llh.fanclubvup.common.BID
import org.babyfish.jimmer.sql.MappedSuperclass

@MappedSuperclass
interface AnchorBidAware {
    val bid: BID
}
