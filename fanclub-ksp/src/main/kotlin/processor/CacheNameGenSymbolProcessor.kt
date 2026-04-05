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
                
                // 获取 @CacheNameGen 注解的 prefix 参数
                val cacheNameGenAnnotation = function.annotations.find { it.annotationType.resolve().declaration.qualifiedName?.asString() == CacheNameGen::class.qualifiedName }
                val prefix = cacheNameGenAnnotation?.arguments?.find { it.name?.asString() == "prefix" }?.value as? String ?: ""
                val methodName = function.simpleName.asString()
                logger.info("方法 $methodName 的 prefix: $prefix")
                
                val methodInfo = MethodInfo(
                    methodName = function.simpleName.asString(),
                    prefix = prefix
                )
                
                methodsByClass.getOrPut(serviceName) { mutableListOf() }.add(methodInfo)
                logger.info("处理方法: $serviceName.${methodInfo.methodName}")
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
            // 创建对象构建器
            val objectBuilder = TypeSpec.objectBuilder("${serviceName}CacheHelper")
                .addKdoc("$serviceName 的缓存名称生成器\n\n此对象在编译时由 KSP 自动生成，提供缓存名称生成方法。")
            
            // 收集所有需要导入的类型
            val typeNames = mutableSetOf<TypeName>()
            typeNames.add(String::class.asTypeName())
            typeNames.add(Long::class.asTypeName())
            
            // 生成缓存名称常量
            methods.forEach { method ->
                val constantName = "${method.methodName.replaceFirstChar { it.uppercase() }}CachePrefix"
                val constantValue = buildString {
                    if (method.prefix.isNotEmpty()) {
                        append("${method.prefix}:")
                    }
                    append("$serviceName:${method.methodName}")
                }
                
                objectBuilder.addProperty(
                    PropertySpec.builder(constantName, String::class)
                        .addKdoc("${method.methodName} 方法的缓存前缀")
                        .initializer("\"$constantValue\"")
                        .build()
                )
                
            }
            
            // 创建文件规范
            val fileSpec = FileSpec.builder("llh.fanclubvup.ksp.generated", "${serviceName}CacheNames")
                .addType(objectBuilder.build())
                .build()
            
            // 写入文件
            fileSpec.writeTo(codeGenerator, aggregating = false)
            logger.info("已生成 ${serviceName}CacheNames.kt 文件，包含 ${methods.size} 个方法")
        }
    }

    /**
     * 方法信息数据类
     */
    private data class MethodInfo(
        val methodName: String,
        val prefix: String = ""
    )
}
