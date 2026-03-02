/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service

import jakarta.annotation.Resource
import llh.fanclubvup.apiserver.dto.PageQueryParamTrait
import llh.fanclubvup.apiserver.dto.PageQueryRequest
import llh.fanclubvup.apiserver.dto.PageResponse
import llh.fanclubvup.apiserver.entity.BaseEntity
import llh.fanclubvup.apiserver.exception.DbOperateException
import org.babyfish.jimmer.View
import org.babyfish.jimmer.sql.ast.mutation.AssociatedSaveMode
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.KExecutable
import org.babyfish.jimmer.sql.kt.ast.expression.constant
import org.babyfish.jimmer.sql.kt.ast.expression.eq
import org.babyfish.jimmer.sql.kt.ast.expression.valueIn
import org.babyfish.jimmer.sql.kt.ast.mutation.KDeleteResult
import org.babyfish.jimmer.sql.kt.ast.mutation.KMutableDelete
import org.babyfish.jimmer.sql.kt.ast.mutation.KMutableUpdate
import org.babyfish.jimmer.sql.kt.ast.mutation.KSimpleSaveResult
import org.babyfish.jimmer.sql.kt.ast.query.KConfigurableRootQuery
import org.babyfish.jimmer.sql.kt.ast.query.KMutableRootQuery
import org.babyfish.jimmer.sql.kt.ast.query.specification.KSpecification
import org.babyfish.jimmer.sql.kt.ast.table.KNonNullTable
import org.babyfish.jimmer.sql.kt.ast.table.makeOrders
import org.babyfish.jimmer.sql.kt.exists
import org.springframework.transaction.support.TransactionTemplate
import kotlin.reflect.KClass

abstract class BaseDatabaseServiceImpl<E : BaseEntity>(
    private val entityType: KClass<E>,
    private val sqlClient: KSqlClient,
) : BaseDatabaseService<E> {

    /**
     * 手动事务。在复杂操作中使用。
     */
    @Resource
    protected lateinit var transactionTemplate: TransactionTemplate

    override fun getById(id: Long): E? {
        return sqlClient.findById(entityType, id)
    }

    override fun getByIds(ids: List<Long>): List<E> {
        return sqlClient.findByIds(entityType, ids)
    }


    override fun <S : View<E>> getByIds(
        staticType: KClass<S>,
        ids: List<Long>,
    ): List<S> {
        return createQuery {
            where(table.getId<Long>() valueIn ids)
            select(table.fetch(staticType))
        }.execute()
    }

    override fun existById(id: Long): Boolean {
        return sqlClient.exists(entityType) {
            where(table.getId<Long>() eq id)
        }
    }

    override fun existBySpec(
        querySpec: KSpecification<E>,
    ): Boolean {
        return createQuery {
            where(querySpec)
            select(constant(1))
        }.limit(1).execute().isNotEmpty()
    }

    override fun <S : View<E>> getById(
        staticType: KClass<S>,
        id: Long,
    ): S? {
        return createQuery {
            where(table.getId<Long>() eq id)
            select(table.fetch(staticType))
        }.fetchOneOrNull()
    }

    override fun <S : View<E>> pageQuery(
        staticType: KClass<S>,
        querySpec: KSpecification<E>,
        pageQueryRequest: PageQueryRequest,
        sortField: String,
    ): PageResponse<S> {
        return createQuery {
            orderBy(table.makeOrders(sortField))
            where(querySpec)
            select(table.fetch(staticType))
        }.fetchCustomPage(pageQueryRequest)
    }

    override fun <S : View<E>> listQuery(
        staticType: KClass<S>,
        querySpec: KSpecification<E>?,
        sortField: String,
        limit: Int?,
    ): List<S> {
        val condition = createQuery {
            orderBy(table.makeOrders(sortField))
            querySpec?.let {
                where(querySpec)
            }
            select(table.fetch(staticType))
        }
        return if (limit == null) {
            condition.execute()
        } else {
            condition.limit(limit).execute()
        }
    }

    override fun save(
        entity: E,
        existBySpec: KSpecification<E>?,
        duplicateTip: String,
    ): E? {
        return sqlClient.transaction {
            if (existBySpec != null) {
                val exist = createQuery {
                    where(existBySpec)
                    select(constant(1))
                }.limit(1).execute().isNotEmpty()
                if (exist) {
                    throw DbOperateException.DataExists(message = duplicateTip)
                }
            }

            val rs = sqlClient.save(entity) {
                setMode(SaveMode.INSERT_ONLY)
                setAssociatedModeAll(AssociatedSaveMode.APPEND_IF_ABSENT)
            }
            checkAddDbResult(rs)
            rs.modifiedEntity
        }
    }

    override fun removeByIds(ids: List<Long>): Boolean {
        return sqlClient.transaction {
            val rs = sqlClient.deleteById(entityType, ids)
            checkDeleteDbResult(rs)
            true
        }
    }


    override fun updateById(
        entity: E,
        existBySpec: KSpecification<E>?,
        duplicateTip: String,
    ): E? {
        return sqlClient.transaction {
            if (existBySpec != null) {
                val exist = createQuery {
                    where(existBySpec)
                    select(constant(1))
                }.limit(1).execute().isNotEmpty()
                if (exist) {
                    throw DbOperateException.DataExists(message = duplicateTip)
                }
            }

            val rs = sqlClient.save(entity) {
                setMode(SaveMode.UPDATE_ONLY)
                setAssociatedModeAll(AssociatedSaveMode.UPDATE)
            }
            checkUpdateDbResult(rs)
            rs.modifiedEntity
        }
    }


    //region 便捷方法
    protected fun <R> createQuery(
        client: KSqlClient = sqlClient,
        block: KMutableRootQuery.ForEntity<E>.() -> KConfigurableRootQuery<KNonNullTable<E>, R>
    ): KConfigurableRootQuery<KNonNullTable<E>, R> =
        client.queries.forEntity(entityType, block)


    protected fun createUpdate(
        block: KMutableUpdate<E>.() -> Unit
    ): KExecutable<Int> =
        sqlClient.createUpdate(entityType, block)

    protected fun createDelete(
        block: KMutableDelete<E>.() -> Unit
    ): KExecutable<Int> =
        sqlClient.createDelete(entityType, block)

    //endregion

    /**
     * 检查新增数据结果. 如果新增数据失败, 则抛出异常
     * @throws DbOperateException 新增数据失败
     */
    protected fun checkAddDbResult(
        rs: KSimpleSaveResult<*>,
        message: String = "新增数据失败"
    ) {
        if (!rs.isModified) {
            throw DbOperateException.addFailed(message = message)
        }
    }

    /**
     * 检查更新数据结果. 如果更新数据失败, 则抛出异常
     * @throws DbOperateException 更新数据失败
     */
    protected fun checkUpdateDbResult(
        rs: KSimpleSaveResult<*>,
        message: String = "未更新任何数据"
    ) {
        if (!rs.isModified) {
            throw DbOperateException.updateFailed(message = message)
        }
    }

    /**
     * 检查删除数据结果. 如果删除数据失败, 则抛出异常
     * @throws DbOperateException 删除数据失败
     */
    protected fun checkDeleteDbResult(
        rs: KDeleteResult,
        message: String = "没有数据被删除"
    ) {
        if (rs.totalAffectedRowCount == 0) {
            throw DbOperateException.deleteFailed(message = message)
        }
    }


    protected fun <R> KConfigurableRootQuery<KNonNullTable<E>, R>.fetchCustomPage(
        pageParam: PageQueryParamTrait
    ): PageResponse<R> {
        return fetchPage(pageParam.pageNum(), pageParam.pageSize).let {
            PageResponse(
                totalRowCount = it.totalRowCount,
                totalPage = it.totalPageCount,
                records = it.rows
            )
        }
    }
}