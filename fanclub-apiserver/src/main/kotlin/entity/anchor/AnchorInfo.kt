package llh.fanclubvup.apiserver.entity.anchor

import io.swagger.v3.oas.annotations.media.Schema
import llh.fanclubvup.apiserver.entity.BaseEntity
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Table

@Entity
@Table(name = "anchor_info")
@Schema(title = "主播基础信息")
interface AnchorInfo : BaseEntity {
    @get:Schema(title = "B站ID", description = "通常称为UID", example = "114514")
    val biliId: Long
}
