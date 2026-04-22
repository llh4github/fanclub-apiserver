/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service

import llh.fanclubvup.apiserver.dto.PageQueryRequest
import llh.fanclubvup.apiserver.dto.PageResponse
import llh.fanclubvup.apiserver.entity.BaseEntity
import org.babyfish.jimmer.Input
import org.babyfish.jimmer.View
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.ast.query.specification.KSpecification
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

interface BaseDatabaseService<E : BaseEntity> {
    fun getById(id: Long): E?

    fun getByIds(ids: List<Long>): List<E>

    fun <S : View<E>> getByIds(
        staticType: KClass<S>, ids: List<Long>,
    ): List<S>

    fun <S : View<E>> getById(
        staticType: KClass<S>, id: Long,
    ): S?

    fun existById(id: Long): Boolean

    fun existBySpec(
        querySpec: KSpecification<E>,
    ): Boolean

    /**
     * 通用分页查询
     * @param staticType 分页查询结果的静态类型，需要符合jimmer相关要求的类
     * @param querySpec 查询条件。jimmer 的 QBE类
     * @param pageQueryRequest 分页参数 详见 [PageQueryRequest]
     * @param sortField 排序字段。默认按照更新时间倒序。格式为 "字段名 排序方式"，如 "updatedTime desc"。
     * 多个字段排序时用逗号分隔，如 "updatedTime desc, createdTime desc"。
     * 参考文档 [动态排序](https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/dynamic-order)
     * @return 分页查询结果，包含分页信息和查询结果
     */
    fun <S : View<E>> pageQuery(
        staticType: KClass<S>,
        querySpec: KSpecification<E>,
        pageQueryRequest: PageQueryRequest,
        sortField: String = "updatedTime desc",
    ): PageResponse<S>

    fun listQuery(
        querySpec: KSpecification<E>? = null,
        sortField: String = "updatedTime desc",
        limit: Int? = null,
    ): List<E>

    /**
     * 通用列表查询
     * @param staticType 分页查询结果的静态类型，需要符合jimmer相关要求的类
     * @param querySpec 查询条件。jimmer 的 QBE类
     * @param sortField 排序字段。默认按照更新时间倒序。格式为 "字段名 排序方式"，如 "updatedTime desc"。
     * 多个字段排序时用逗号分隔，如 "updatedTime desc, createdTime desc"。
     * 参考文档 [动态排序](https://babyfish-ct.github.io/jimmer-doc/zh/docs/query/dynamic-order)
     * @param limit 限制返回结果的数量
     * @return 查询结果
     */
    fun <S : View<E>> listQuery(
        staticType: KClass<S>,
        querySpec: KSpecification<E>? = null,
        sortField: String = "updatedTime desc",
        limit: Int? = null,
    ): List<S>

    /**
     * 保存数据
     * @param entity 要保存的数据
     * @param saveMode 保存模式。默认为 INSERT_ONLY
     * @param existBySpec 存在性检查条件。如果存在则抛出异常。jimmer 的 QBE类
     * @param duplicateTip 存在性检查提示信息
     * @return 保存后的数据
     */
    fun save(
        entity: E,
        saveMode: SaveMode = SaveMode.INSERT_ONLY,
        existBySpec: KSpecification<E>? = null,
        duplicateTip: String = "数据已存在",
    ): E?

    fun upsert(entity: Input<E>, keyProps: List<KProperty1<E, *>> = emptyList()): Boolean

    /**
     * 保存数据
     * @param entity 要保存的数据
     * @param saveMode 保存模式。默认为 INSERT_ONLY
     * @param existBySpec 存在性检查条件。如果存在则抛出异常。jimmer 的 QBE类
     * @return 保存后的数据
     */
    fun save(
        entity: Input<E>,
        saveMode: SaveMode = SaveMode.INSERT_ONLY,
        existBySpec: KSpecification<E>? = null,
        duplicateTip: String = "数据已存在",
    ): E? = save(entity.toEntity(), saveMode, existBySpec, duplicateTip)

    /**
     * 移除数据。真实删除或逻辑删除由具体配置决定。
     */
    fun removeByIds(ids: List<Long>): Boolean

    fun removeByIds(id: Long): Boolean = removeByIds(listOf(id))

    fun updateById(
        entity: E,
        existBySpec: KSpecification<E>? = null,
        duplicateTip: String = "数据已存在",
    ): E?

    fun updateById(
        entity: Input<E>,
        existBySpec: KSpecification<E>? = null,
        duplicateTip: String = "数据已存在",
    ): E? = updateById(entity.toEntity(), existBySpec, duplicateTip)
}
