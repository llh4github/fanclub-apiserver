package llh.fanclubvup.apiserver.entity.viewer

import io.swagger.v3.oas.annotations.media.Schema
import llh.fanclubvup.apiserver.entity.BaseEntity
import llh.fanclubvup.common.BID
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Key
import org.babyfish.jimmer.sql.Table


@Entity
@Table(name = "viewer_danmu_count")
@Schema(title = "观众弹幕数量统计")
interface ViewerDanmuCount : BaseEntity {

    @Key(group = "bid-nkname")
    val bid: BID

}
