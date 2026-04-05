/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.ksp.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
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
                // 获取返回类型信息
                val returnType = function.returnType?.resolve()
                val returnTypeName = returnType?.declaration?.simpleName?.asString() ?: "Unit"
                val returnTypeQualifiedName = returnType?.declaration?.qualifiedName?.asString()
                
                // 构建完整的返回类型字符串，包括泛型参数
                val fullReturnType = buildString {
                    if (returnTypeQualifiedName != null) {
                        append(returnTypeQualifiedName)
                        // 检查是否有泛型参数
                        if (returnType.arguments.isNotEmpty()) {
                            append("<")
                            returnType.arguments.forEachIndexed { index, arg ->
                                if (index > 0) append(", ")
                                val argType = arg.type?.resolve()
                                val argTypeQualifiedName = argType?.declaration?.qualifiedName?.asString()
                                if (argTypeQualifiedName != null) {
                                    append(argTypeQualifiedName)
                                } else {
                                    // 处理解析失败的类型
                                    val argTypeStr = arg.type?.toString() ?: "Any"
                                    if (argTypeStr.startsWith("<ERROR TYPE: ")) {
                                        // 从错误信息中提取类型名
                                        val typeName = argTypeStr.substringAfter("<ERROR TYPE: ").substringBefore(">")
                                        // 尝试构建可能的包名
                                        val possiblePackage = "llh.fanclubvup.apiserver.entity.anchor.dto"
                                        append("$possiblePackage.$typeName")
                                    } else {
                                        append(argTypeStr)
                                    }
                                }
                            }
                            append(">")
                        }
                    } else {
                        append(returnTypeName)
                    }
                }
                logger.info("返回类型: $returnTypeName, 完全限定名: $returnTypeQualifiedName, 完整类型: $fullReturnType")
                
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
                    },
                    returnType = returnTypeName,
                    returnTypeQualifiedName = returnTypeQualifiedName,
                    fullReturnType = fullReturnType
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
            // 生成完整的文件内容
            val fileContent = buildString {
                append("package llh.fanclubvup.ksp.generated\n\n")
                
                // 收集所有需要导入的包
                val imports = mutableSetOf<String>()
                imports.add("com.fasterxml.jackson.core.type.TypeReference")
                imports.add("kotlin.Long")
                imports.add("kotlin.String")
                
                // 添加自定义类型的导入
                methods.forEach { method ->
                    method.parameters.forEach { param ->
                        if (param.qualifiedName != null && !param.type.startsWith("<ERROR TYPE:")) {
                            imports.add(param.qualifiedName)
                        } else if (param.type.startsWith("<ERROR TYPE:")) {
                            val typeName = param.type.substringAfter("<ERROR TYPE: ").substringBefore(">")
                            val possiblePackage = "llh.fanclubvup.apiserver.entity.anchor.dto"
                            imports.add("$possiblePackage.$typeName")
                        }
                    }
                    
                    // 添加返回类型的导入
                    if (method.fullReturnType.contains(".")) {
                        // 提取返回类型中的所有类名
                        val typeRegex = Regex("""[a-zA-Z0-9_]+\.[a-zA-Z0-9_]+(\.[a-zA-Z0-9_]+)*""")
                        val matches = typeRegex.findAll(method.fullReturnType)
                        matches.forEach { match ->
                            val typeName = match.value
                            if (typeName.contains(".")) {
                                imports.add(typeName)
                            }
                        }
                    }
                }
                
                // 生成导入语句
                imports.forEach { import ->
                    append("import $import\n")
                }
                
                append("\n")
                append("/**\n")
                append(" * $serviceName 的缓存名称生成器\n")
                append(" *\n")
                append(" * 此对象在编译时由 KSP 自动生成，提供缓存名称生成方法。\n")
                append(" */\n")
                append("public object ${serviceName}CacheHelper {\n")
                
                // 生成缓存名称方法
                methods.forEach { method ->
                    append("  /**\n")
                    append("   * 生成 ${method.methodName} 方法的缓存名称\n")
                    append("   */\n")
                    append("  public fun ${method.methodName}CacheName(")
                    
                    // 添加参数
                    val params = method.parameters.map { param ->
                        val paramType = when (param.type) {
                            "String" -> "String"
                            "Int" -> "Int"
                            "Long" -> "Long"
                            "Boolean" -> "Boolean"
                            "Double" -> "Double"
                            "Float" -> "Float"
                            "BID" -> "Long" // 假设 BID 是 Long 类型的别名
                            "LocalDate" -> "LocalDate"
                            else -> {
                                if (param.qualifiedName != null && !param.type.startsWith("<ERROR TYPE:")) {
                                    param.type
                                } else if (param.type.startsWith("<ERROR TYPE:")) {
                                    param.type.substringAfter("<ERROR TYPE: ").substringBefore(">")
                                } else {
                                    "Any"
                                }
                            }
                        }
                        "${param.name}: $paramType"
                    }
                    append(params.joinToString(", "))
                    append("): String = \"$serviceName:${method.methodName}")
                    
                    // 添加参数值
                    method.parameters.forEach { param ->
                        append(":")
                        when (param.type) {
                            "String", "Int", "Long", "Boolean", "Double", "Float" -> {
                                append("${'$'}{${param.name}}")
                            }
                            "BID" -> {
                                append("${'$'}{${param.name}}")
                            }
                            "LocalDate" -> {
                                append("${'$'}{${param.name}}")
                            }
                            else -> {
                                // 对于自定义类型，尝试展开其字段
                                append("${'$'}{${param.name}.biliId ?: \"biliId\"}")
                                append(":")
                                append("${'$'}{${param.name}.cntDate ?: \"cntDate\"}")
                            }
                        }
                    }
                    append("\"\n\n")
                }
                
                // 生成 TypeReference 实现
                methods.forEach { method ->
                    val returnTypeObjectName = "${method.methodName.replaceFirstChar { it.uppercase() }}ReturnType"
                    append("  public object $returnTypeObjectName : TypeReference<")
                    append(method.fullReturnType)
                    append(">() {}\n\n")
                }
                
                append("}\n")
            }
            
            // 写入文件
            val file = codeGenerator.createNewFile(
                dependencies = Dependencies.ALL_FILES,
                packageName = "llh.fanclubvup.ksp.generated",
                fileName = "${serviceName}CacheNames"
            )
            file.write(fileContent.toByteArray())
            file.close()
            
            logger.info("已生成 ${serviceName}CacheNames.kt 文件，包含 ${methods.size} 个方法")
        }
    }

    /**
     * 方法信息数据类
     */
    private data class MethodInfo(
        val methodName: String,
        val parameters: List<ParamInfo>,
        val returnType: String,
        val returnTypeQualifiedName: String?,
        val fullReturnType: String
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
