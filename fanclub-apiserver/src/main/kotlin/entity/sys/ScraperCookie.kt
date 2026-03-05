package llh.fanclubvup.apiserver.entity.sys

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import llh.fanclubvup.apiserver.entity.BaseEntity
import llh.fanclubvup.apiserver.utils.CreateGroup
import okhttp3.Cookie
import org.babyfish.jimmer.sql.Entity
import org.babyfish.jimmer.sql.Table
import org.hibernate.validator.constraints.Length

@Entity
@Table(name = "sys_scraper_cookie")
interface ScraperCookie : BaseEntity {
    @get:NotEmpty(groups = [CreateGroup::class], message = "Cookie 名称不能为空")
    @get:Length(
        max = 50,
        min = 1,
        message = "Cookie 名称长度在 {min} 至 {max} 个字符之间",
        groups = [CreateGroup::class]
    )
    @get:Schema(title = "Cookie 名称", description = "Cookie 名称", example = "SESSDATA")
    val name: String

    @get:NotEmpty(groups = [CreateGroup::class], message = "Cookie 值不能为空")
    @get:Schema(title = "Cookie 值", description = "Cookie 值", example = "abc123...")
    val value: String

    @get:NotEmpty(groups = [CreateGroup::class], message = "域名不能为空")
    @get:Schema(title = "域名", description = "Cookie 所属域名", example = ".bilibili.com")
    val domain: String

    @get:Schema(title = "过期时间", description = "Cookie 过期时间戳 (毫秒)", example = "1804003200000")
    val expiresAt: Long?

    /**
     * 转换为 OkHttp Cookie 对象
     */
    fun toCookie(): Cookie {
        return Cookie.Builder()
            .name(name)
            .value(value)
            .domain(domain)
            .expiresAt(expiresAt ?: Long.MAX_VALUE)
            .build()
    }
}
