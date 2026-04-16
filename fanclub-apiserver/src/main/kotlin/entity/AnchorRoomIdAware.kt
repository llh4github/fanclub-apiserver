package llh.fanclubvup.apiserver.entity

import org.babyfish.jimmer.sql.MappedSuperclass

@MappedSuperclass
interface AnchorRoomIdAware {
    val roomId: Long
}
