package llh.fanclubvup.apiserver.components.interceptors

import llh.fanclubvup.apiserver.entity.AnchorBidAware
import llh.fanclubvup.apiserver.entity.AnchorRoomIdAware
import llh.fanclubvup.apiserver.entity.bid
import llh.fanclubvup.apiserver.entity.roomId
import llh.fanclubvup.apiserver.utils.SecurityContextUtil
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.filter.KFilter
import org.babyfish.jimmer.sql.kt.filter.KFilterArgs
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class AnchorDataScopeFilter {

    @Bean
    fun bidDataScope() = object : KFilter<AnchorBidAware> {
        override fun filter(args: KFilterArgs<AnchorBidAware>) {
            if (SecurityContextUtil.isAnchor()) {
                args.apply {
                    where(table.bid eq SecurityContextUtil.bidOrThrow())
                }
            }
        }
    }

    @Bean
    fun roomIdDataScope() = object : KFilter<AnchorRoomIdAware> {
        override fun filter(args: KFilterArgs<AnchorRoomIdAware>) {

            if (SecurityContextUtil.isAnchor()) {
                args.apply {
                    where(table.roomId eq SecurityContextUtil.roomIdOrThrow())
                }
            }
        }
    }
}
