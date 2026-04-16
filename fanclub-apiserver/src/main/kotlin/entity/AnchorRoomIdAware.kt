package llh.fanclubvup.apiserver.entity

import io.swagger.v3.oas.annotations.media.Schema
import org.babyfish.jimmer.sql.Column
import org.babyfish.jimmer.sql.MappedSuperclass

@MappedSuperclass
interface AnchorRoomIdAware {

    @Column(name = "room_id")
    @get:Schema(title = "直播间ID", description = "直播间ID", example = "114514")
    val roomId: Long
}
