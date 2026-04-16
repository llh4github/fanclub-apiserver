package llh.fanclubvup.apiserver.entity

import llh.fanclubvup.apiserver.entity.sys.User
import org.babyfish.jimmer.sql.DissociateAction
import org.babyfish.jimmer.sql.ManyToOne
import org.babyfish.jimmer.sql.MappedSuperclass
import org.babyfish.jimmer.sql.OnDissociate

@MappedSuperclass
interface CreatorAware {

    @ManyToOne
    @OnDissociate(DissociateAction.SET_NULL)
    val createdBy: User?

    @ManyToOne
    @OnDissociate(DissociateAction.SET_NULL)
    val updatedBy: User?
}
