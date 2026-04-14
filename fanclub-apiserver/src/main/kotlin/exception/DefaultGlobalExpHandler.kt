/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.exception

import io.github.oshai.kotlinlogging.KotlinLogging
import llh.fanclubvup.apiserver.dto.JsonWrapper
import llh.fanclubvup.common.excptions.AppRuntimeException
import org.babyfish.jimmer.error.CodeBasedRuntimeException
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@ConditionalOnWebApplication
class DefaultGlobalExpHandler {
    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(CodeBasedRuntimeException::class)
    @ConditionalOnProperty(name = ["jimmer.language"], havingValue = "kotlin")
    fun handleException(
        e: CodeBasedRuntimeException
    ): JsonWrapper<Map<String, Any?>> {
        return JsonWrapper.fail(
            data = e.fields,
            code = e.code,
            module = e.family,
            msg = e.message ?: "请求处理异常"
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): JsonWrapper<Map<String, String>> {
        val errors = e.bindingResult.fieldErrors.associate { fieldError ->
            fieldError.field to (fieldError.defaultMessage ?: "参数验证失败")
        }
        logger.warn { "参数验证失败: $errors" }
        return JsonWrapper.fail(
            code = "400",
            msg = "参数验证失败",
            data = errors
        )
    }

    @ExceptionHandler(AppRuntimeException::class)
    fun handleException(e: AppRuntimeException): JsonWrapper<String> {
        logger.error(e) { "应用运行出错: ${e.message}" }
        return JsonWrapper.fail(data = e.message)
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleException(e: RuntimeException): JsonWrapper<String> {
        logger.error(e) { "系统出现未知错误: ${e.message}" }
        return JsonWrapper.fail(data = e.message)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): JsonWrapper<String> {
        logger.error(e) { "系统出现未知错误: ${e.message}" }
        return JsonWrapper.fail(data = e.message)
    }
}
