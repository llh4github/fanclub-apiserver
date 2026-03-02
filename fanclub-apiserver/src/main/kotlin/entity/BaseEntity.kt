package llh.fanclubvup.apiserver.entity

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import llh.fanclubvup.common.consts.DatetimeConstant
import org.babyfish.jimmer.sql.MappedSuperclass
import java.time.LocalDateTime

@MappedSuperclass
interface BaseEntity : LongEntityId {

    @get:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DatetimeConstant.DATE_TIME_FORMAT)
    @get:Schema(
        title = "创建时间",
        description = "数据创建时间",
        example = DatetimeConstant.DATE_TIME_FORMAT,
        accessMode = Schema.AccessMode.READ_ONLY
    )
    val createdTime: LocalDateTime?

    @get:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DatetimeConstant.DATE_TIME_FORMAT)
    @get:Schema(
        title = "更新时间", description = "数据更新时间", example = DatetimeConstant.DATE_TIME_FORMAT,
        accessMode = Schema.AccessMode.READ_ONLY
    )
    val updatedTime: LocalDateTime?
}
