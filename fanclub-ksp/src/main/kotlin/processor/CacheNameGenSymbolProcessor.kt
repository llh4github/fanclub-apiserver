/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.ksp.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.writeTo
import llh.fanclubvup.ksp.annon.CacheNameGen

class CacheNameGenSymbolProcessor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("CacheNameGenSymbolProcessor 插件开始处理")

        // 获取所有被 @CacheNameGen 注解标记的函数
        val annotatedFunctions = resolver.getSymbolsWithAnnotation(CacheNameGen::class.qualifiedName!!)
            .filterIsInstance<KSFunctionDeclaration>()
            .toList()

        if (annotatedFunctions.isEmpty()) {
            logger.info("没有被标记 @CacheNameGen 的方法")
            return emptyList()
        }

        // 按类分组处理方法
        val methodsByClass = mutableMapOf<String, MutableList<MethodInfo>>()
        
        annotatedFunctions.forEach { function ->
            val containingClass = function.parentDeclaration as? KSClassDeclaration
            if (containingClass != null) {
                val className = containingClass.simpleName.asString()
                // 去掉 Impl 后缀
                val serviceName = className.removeSuffix("Impl")
                
                val containingClassPackage = containingClass.packageName.asString()
                val methodInfo = MethodInfo(
                    methodName = function.simpleName.asString(),
                    parameters = function.parameters.map { param ->
                        val resolvedType = param.type.resolve()
                        val typeName = resolvedType.declaration.simpleName.asString()
                        val qualifiedName = resolvedType.declaration.qualifiedName?.asString()
                        logger.info("参数: ${param.name}, 类型: $typeName, 完全限定名: $qualifiedName")
                        ParamInfo(
                            name = param.name?.asString() ?: "param",
                            type = typeName,
                            qualifiedName = qualifiedName,
                            containingClassPackage = containingClassPackage
                        )
                    }
                )
                
                methodsByClass.getOrPut(serviceName) { mutableListOf() }.add(methodInfo)
                logger.info("处理方法: $serviceName.${methodInfo.methodName}, 参数: ${methodInfo.parameters.joinToString { it.name }}")
            } else {
                logger.warn("方法 ${function.simpleName.asString()} 不在类中，跳过")
            }
        }

        // 为每个服务类生成缓存名称方法
        generateCacheNameMethods(methodsByClass)

        logger.info("CacheNameGenSymbolProcessor 插件处理结束，共处理 ${annotatedFunctions.size} 个方法")
        return emptyList()
    }

    /**
     * 生成缓存名称方法
     */
    private fun generateCacheNameMethods(methodsByClass: Map<String, List<MethodInfo>>) {
        methodsByClass.forEach { (serviceName, methods) ->
            val objectBuilder = TypeSpec.objectBuilder("${serviceName}CacheHelper")
            
            methods.forEach { method ->
                val functionName = "${method.methodName}CacheName"
                val funBuilder = FunSpec.builder(functionName)
                    .returns(String::class)
                    .addKdoc("生成 ${method.methodName} 方法的缓存名称")
                
                // 添加参数 - 使用原始参数类型
                method.parameters.forEach { param ->
                    val paramType: TypeName = when (param.type) {
                        "String" -> String::class.asTypeName()
                        "Int" -> Int::class.asTypeName()
                        "Long" -> Long::class.asTypeName()
                        "Boolean" -> Boolean::class.asTypeName()
                        "Double" -> Double::class.asTypeName()
                        "Float" -> Float::class.asTypeName()
                        "BID" -> Long::class.asTypeName() // 假设 BID 是 Long 类型的别名
                        "LocalDate" -> java.time.LocalDate::class.asTypeName()
                        else -> {
                            // 处理自定义类型
                            if (param.qualifiedName != null && !param.type.startsWith("<ERROR TYPE:")) {
                                ClassName.bestGuess(param.qualifiedName)
                            } else if (param.type.startsWith("<ERROR TYPE:")) {
                                // 处理KSP生成的类型，尝试从错误信息中提取类型名
                                val typeName = param.type.substringAfter("<ERROR TYPE: ").substringBefore(">")
                                // 尝试构建可能的包名
                                val possiblePackage = "llh.fanclubvup.apiserver.entity.anchor.dto"
                                ClassName.bestGuess("$possiblePackage.$typeName")
                            } else {
                                Any::class.asTypeName()
                            }
                        }
                    }
                    funBuilder.addParameter(param.name, paramType)
                }
                
                // 构建返回字符串模板
                val cacheNameExpression = buildString {
                    append("\"")
                    append(serviceName)
                    append(":")
                    append(method.methodName)
                    method.parameters.forEach { param ->
                        append(":")
                        when (param.type) {
                            "String", "Int", "Long", "Boolean", "Double", "Float" -> {
                                append("$")
                                append(param.name)
                            }
                            "BID" -> {
                                append("$")
                                append(param.name)
                            }
                            "LocalDate" -> {
                                append("$")
                                append(param.name)
                            }
                            else -> {
                                // 对于自定义类型，尝试展开其字段
                                append("${'$'}{${param.name}.biliId ?: \"biliId\"}")
                                append(":")
                                append("${'$'}{${param.name}.cntDate ?: \"cntDate\"}")
                            }
                        }
                    }
                    append("\"")
                }
                
                // 直接使用字符串模板
                funBuilder.addStatement("return $cacheNameExpression")
                objectBuilder.addFunction(funBuilder.build())
            }
            
            objectBuilder.addKdoc("$serviceName 的缓存名称生成器\n\n此对象在编译时由 KSP 自动生成，提供缓存名称生成方法。")
            
            val fileSpec = FileSpec.builder("llh.fanclubvup.ksp.generated", "${serviceName}CacheNames")
                .addType(objectBuilder.build())
                .build()
            
            fileSpec.writeTo(codeGenerator, aggregating = false)
            logger.info("已生成 ${serviceName}CacheNames.kt 文件，包含 ${methods.size} 个方法")
        }
    }

    /**
     * 方法信息数据类
     */
    private data class MethodInfo(
        val methodName: String,
        val parameters: List<ParamInfo>
    )

    /**
     * 参数信息数据类
     */
    private data class ParamInfo(
        val name: String,
        val type: String,
        val qualifiedName: String?,
        val containingClassPackage: String? = null
    )
}
