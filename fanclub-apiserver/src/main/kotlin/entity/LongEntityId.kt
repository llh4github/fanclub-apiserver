/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.entity

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import llh.fanclubvup.apiserver.utils.UpdateGroup
import org.babyfish.jimmer.sql.GeneratedValue
import org.babyfish.jimmer.sql.Id
import org.babyfish.jimmer.sql.MappedSuperclass

@MappedSuperclass
interface LongEntityId {
    @Id
    @GeneratedValue(generatorType = EntityIdGenerator::class)
    @get:Schema(title = "数据主键", description = "数据主键", example = "114514")
    @get:NotNull(groups = [UpdateGroup::class], message = "数据主键不能为空")
    @get:Min(value = 1, groups = [UpdateGroup::class], message = "数据主键不能为负数")
    val id: Long
}
