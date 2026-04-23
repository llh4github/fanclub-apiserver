/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.ksp.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toTypeName
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
                try {
                    val cacheNameGenAnnotation = function.annotations.find { it.annotationType.resolve().declaration.qualifiedName?.asString() == CacheNameGen::class.qualifiedName }
                    val prefix = cacheNameGenAnnotation?.arguments?.find { it.name?.asString() == "prefix" }?.value as? String ?: ""
                    val methodName = function.simpleName.asString()
                    logger.info("方法 $methodName 的 prefix: $prefix")
                    
                    // 获取返回类型（安全处理）
                    val returnType = try {
                        val returnType = function.returnType
                        if (returnType != null) {
                            try {
                                val resolvedType = returnType.resolve()
                                val typeName = resolvedType.toTypeName()
                                typeName.toString()
                            } catch (e: Exception) {
                                // 当类型无法解析时，尝试获取原始类型字符串并移除 INVARIANT 关键字
                                var typeString = returnType.toString()
                                // 移除 INVARIANT 关键字
                                typeString = typeString.replace("INVARIANT ", "")
                                // 移除全限定名，只保留简单类名，因为我们会通过导入语句处理
                                typeString = typeString.replace("llh.fanclubvup.apiserver.entity.anchor.dto.", "")
                                typeString = typeString.replace("llh.fanclubvup.bilibili.props.", "")
                                logger.warn("无法解析返回类型，使用原始类型字符串: $typeString")
                                typeString
                            }
                        } else {
                            "Unit"
                        }
                    } catch (e: Exception) {
                        logger.error("无法解析返回类型: ${e.message}")
                        throw e
                    }
                    
                    // 获取参数列表（安全处理）
                    val parameters = function.parameters.mapNotNull { param ->
                        try {
                            ParameterInfo(
                                name = param.name?.asString() ?: "arg",
                                type = param.type.resolve().toTypeName().toString()
                            )
                        } catch (e: Exception) {
                            logger.warn("无法解析参数 ${param.name?.asString()} 的类型，跳过该参数")
                            null
                        }
                    }
                    
                    val methodInfo = MethodInfo(
                        methodName = methodName,
                        prefix = prefix,
                        returnType = returnType,
                        parameters = parameters
                    )
                    
                    methodsByClass.getOrPut(serviceName) { mutableListOf() }.add(methodInfo)
                    logger.info("处理方法: $serviceName.${methodInfo.methodName}, 返回类型: $returnType, 参数: ${parameters.size}")
                } catch (e: IllegalArgumentException) {
                    // 处理类型解析错误，抛出异常
                    logger.error("处理方法 ${function.simpleName.asString()} 时遇到类型解析错误: ${e.message}")
                    throw e
                } catch (e: Exception) {
                    // 处理其他错误，抛出异常
                    logger.error("处理方法 ${function.simpleName.asString()} 时遇到错误: ${e.message}")
                    throw e
                }
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
            // 收集所有需要的导入
            val imports = mutableSetOf<String>()
            imports.add("import kotlin.String")
            
            // 构建文件内容
            val fileContent = buildString {
                append("package llh.fanclubvup.ksp.generated\n\n")
                
                // 为所有方法生成内容
                append("/**\n")
                append(" * $serviceName 的缓存名称生成器\n")
                append(" *\n")
                append(" * 此对象在编译时由 KSP 自动生成，提供缓存名称生成方法。\n")
                append(" */\n")
                append("public object ${serviceName}CacheHelper {\n")
                
                // 为每个方法生成缓存前缀常量和 TypeReference
                methods.forEach { method ->
                    // 生成缓存前缀常量（使用 const val 和大写蛇形命名）
                    val constantName = "${method.methodName.replaceFirstChar { it.uppercase() }.replace(Regex("([a-z0-9])([A-Z])"), "$1_$2").uppercase()}_CACHE_PREFIX"
                    val constantValue = buildString {
                        if (method.prefix.isNotEmpty()) {
                            append("${method.prefix}:")
                        }
                        append("$serviceName:${method.methodName}")
                    }
                    
                    append("\n")
                    append("  /**\n")
                    append("   * ${method.methodName} 方法的缓存前缀\n")
                    append("   */\n")
                    append("  public const val $constantName: String = \"$constantValue\"\n")
                    
                    // 检查返回类型是否是基本类型或字符串类型
                    val isBasicType = method.returnType in listOf("Int", "Long", "Double", "Float", "Boolean", "String", "Unit")
                    
                    if (!isBasicType) {
                        // 添加 TypeReference 导入
                        imports.add("import tools.jackson.core.type.TypeReference")
                        
                        // 处理类型导入
                        // 处理泛型类型
                        if (method.returnType.contains("<")) {
                            // 提取泛型参数
                            val genericParamMatch = Regex("<(.*?)>").find(method.returnType)
                            if (genericParamMatch != null) {
                                val genericParam = genericParamMatch.groupValues[1]
                                        // 动态处理类型导入
                                // 提取泛型参数中的类型名
                                val typeNames = genericParam.split(",").map { it.trim() }
                                typeNames.forEach { typeName ->
                                    // 跳过基本类型
                                    val basicTypes = listOf("String", "Int", "Long", "Double", "Float", "Boolean", "Unit")
                                    if (!basicTypes.contains(typeName) && !typeName.startsWith("kotlin.")) {
                                        // 尝试为常见的DTO类型添加导入
                                        if (typeName.contains("Anchor")) {
                                            imports.add("import llh.fanclubvup.apiserver.entity.anchor.dto.$typeName")
                                        } else if (typeName.contains("Bili")) {
                                            imports.add("import llh.fanclubvup.bilibili.props.$typeName")
                                        }
                                    }
                                }
                            }
                        }
                        
                        // 处理非泛型类型
                        if (method.returnType.contains("BiliClientConfig")) {
                            imports.add("import llh.fanclubvup.bilibili.props.BiliClientConfig")
                        }
                        
                        // 处理返回类型，确保使用简单类名
                        var processedReturnType = method.returnType
                        // 移除全限定名前缀
                        processedReturnType = processedReturnType.replace("llh.fanclubvup.apiserver.entity.anchor.dto.", "")
                        processedReturnType = processedReturnType.replace("llh.fanclubvup.bilibili.props.", "")
                        
                        // 为非基本类型生成 TypeReference 实现子类
                        append("\n")
                        val nestedObjectName = "${method.methodName.replaceFirstChar { it.uppercase() }}TypeRef"
                        append("  /**\n")
                        append("   * ${method.methodName} 方法的 TypeReference 生成器\n")
                        append("   */\n")
                        append("  public object $nestedObjectName : TypeReference<$processedReturnType>() {\n")
                        append("  }\n")
                    }
                }
                
                append("}\n")
            }
            
            // 插入导入语句
            val importsString = imports.joinToString("\n")
            val finalContent = "package llh.fanclubvup.ksp.generated\n\n" + importsString + "\n\n" + fileContent.substringAfter("\n\n")
            
            // 直接写入文件
            val file = codeGenerator.createNewFile(
                Dependencies(false),
                "llh.fanclubvup.ksp.generated",
                "${serviceName}CacheNames"
            )
            file.write(finalContent.toByteArray())
            file.close()
            
            logger.info("已生成 ${serviceName}CacheNames.kt 文件，包含 ${methods.size} 个方法")
        }
    }
    
    /**
     * 将字符串类型转换为 Kotlin 类型
     */
    private fun String.toKotlinType(): TypeName {
        return when (this) {
            "Long" -> Long::class.asTypeName()
            "Int" -> Int::class.asTypeName()
            "String" -> String::class.asTypeName()
            "Boolean" -> Boolean::class.asTypeName()
            "Unit" -> Unit::class.asTypeName()
            else -> {
                // 处理复杂类型，如 List<...>
                if (this.startsWith("List<")) {
                    val genericType = this.substring(5, this.length - 1).toKotlinType()
                    List::class.asTypeName().parameterizedBy(genericType)
                } else {
                    // 默认为 Any 类型
                    Any::class.asTypeName()
                }
            }
        }
    }

    /**
     * 构建 cacheKey 方法的函数体
     */
    private fun buildCacheKeyBody(method: MethodInfo): CodeBlock {
        val codeBlock = CodeBlock.builder()
            .add("return buildString {\n")
        
        if (method.prefix.isNotEmpty()) {
            codeBlock.add("    append(\"${method.prefix}:\")\n")
        }
        
        codeBlock.add("    append(\"${method.methodName}\")\n")
        
        method.parameters.forEach { param ->
            codeBlock.add("    append(\":\$${param.name}\")\n")
        }
        
        codeBlock.add("}\n")
        
        return codeBlock.build()
    }

    /**
     * 从完整类型名中提取简单类名
     */
    private fun extractSimpleTypeName(fullTypeName: String): String {
        // 处理泛型类型，如 List<AnchorLiveRecordLiveStatus>
        val genericMatch = Regex("<(.*?)>").find(fullTypeName)
        val baseType = if (genericMatch != null) {
            // 提取泛型参数
            val typeArg = genericMatch.groupValues[1]
            val simpleArg = typeArg.substringAfterLast(".")
            val containerType = fullTypeName.substringBefore("<").substringAfterLast(".")
            "$containerType${simpleArg}"
        } else {
            // 非泛型类型，直接取最后一部分
            fullTypeName.substringAfterLast(".")
        }
        return baseType
    }

    /**
     * 方法信息数据类
     */
    private data class MethodInfo(
        val methodName: String,
        val prefix: String = "",
        val returnType: String = "",
        val parameters: List<ParameterInfo> = emptyList()
    )

    /**
     * 参数信息数据类
     */
    private data class ParameterInfo(
        val name: String,
        val type: String
    )
}
