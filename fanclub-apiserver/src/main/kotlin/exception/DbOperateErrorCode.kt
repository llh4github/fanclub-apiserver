package llh.fanclubvup.apiserver.exception

import org.babyfish.jimmer.error.ErrorFamily
import org.babyfish.jimmer.error.ErrorField

@ErrorFamily
enum class DbOperateErrorCode {
    /**
     * 新增数据失败
     */
    ADD_FAILED,

    /**
     * 更新数据失败
     */
    UPDATE_FAILED,

    /**
     * 删除数据失败
     */
    DELETE_FAILED,

    /**
     * 数据已存在
     */
    @ErrorField(name = "exists", type = String::class, nullable = true)
    DATA_EXISTS,

    /**
     * 数据不存在
     */
    @ErrorField(name = "notExists", type = String::class, nullable = true)
    DATA_NOT_EXISTS,

    /**
     * 数据库连接获取失败
     */
    CONN_FETCH_FAILED,
}
