package llh.fanclubvup.apiserver.entity.sys

import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import llh.fanclubvup.apiserver.entity.BaseEntity
import llh.fanclubvup.apiserver.utils.CreateGroup
import llh.fanclubvup.apiserver.utils.CreateUpdateGroup
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Table
import org.hibernate.validator.constraints.Length

@Entity
@Table(name = "sys_user")
interface User : BaseEntity {
    @get:NotEmpty(groups = [CreateGroup::class], message = "用户名不能为空")
    @get:Length(
        max = 30,
        min = 3,
        message = "用户名长度在 {min} 至 {max} 个字符之间",
        groups = [CreateUpdateGroup::class]
    )
    @get:Schema(title = "用户名", description = "用户名", example = "Tom")
    val username: String

    @get:NotEmpty(groups = [CreateGroup::class], message = "昵称不能为空")
    @get:Length(
        max = 30,
        min = 3,
        message = "昵称长度在 {min} 至 {max} 个字符之间",
        groups = [CreateUpdateGroup::class]
    )
    @get:Schema(title = "昵称", description = "昵称", example = "Tom")
    val nickname: String

    @get:JsonIgnore
    val password: String
}
