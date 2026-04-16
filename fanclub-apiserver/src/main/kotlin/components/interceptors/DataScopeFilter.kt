package llh.fanclubvup.apiserver.components.interceptors

import llh.fanclubvup.apiserver.entity.CreatorAware
import llh.fanclubvup.apiserver.entity.createdById
import llh.fanclubvup.apiserver.utils.SecurityContextUtil
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.filter.KFilter
import org.babyfish.jimmer.sql.kt.filter.KFilterArgs
import org.springframework.stereotype.Component

@Component
class DataScopeFilter : KFilter<CreatorAware> {
    override fun filter(args: KFilterArgs<CreatorAware>) {
        if (SecurityContextUtil.isAnchor()) {
            args.apply {
                where(table.createdById eq SecurityContextUtil.currentUserId())
            }
        }
    }
}
