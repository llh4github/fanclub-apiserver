package llh.fanclubvup.ksp.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.writeTo
import llh.fanclubvup.ksp.annon.PublicAccessUrl

class PublicAccessUrlSymbolProcessor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("PublicAccessUrlSymbolProcessor 插件开始处理")

        // 获取所有被 @PublicAccessUrl 注解标记的函数
        val annotatedFunctions = resolver.getSymbolsWithAnnotation(PublicAccessUrl::class.qualifiedName!!)
            .filterIsInstance<KSFunctionDeclaration>()
            .toList()

        if (annotatedFunctions.isEmpty()) {
            logger.info("没有被标记 @PublicAccessUrl 的方法")
            return emptyList()
        }

        // 提取每个方法的 URL 路径
        val urlPaths = mutableListOf<String>()
        annotatedFunctions.forEach { function ->
            val urlPath = extractUrlPath(function)
            logger.info("方法：${function.simpleName.asString()}, URL 路径：$urlPath")
            urlPaths.add(urlPath)
        }

        // 使用 KotlinPoet 生成代码
        generatePublicAccessUrlsFile(urlPaths)

        logger.info("PublicAccessUrlSymbolProcessor 插件处理结束，共处理 ${annotatedFunctions.size} 个方法")
        return emptyList()
    }

    /**
     * 从 KSFunctionDeclaration 提取完整的 URL 路径
     */
    private fun extractUrlPath(function: KSFunctionDeclaration): String {
        // 1. 获取方法上的 RequestMapping 注解（如 @GetMapping, @PostMapping 等）
        val methodPath = getMethodPath(function).trim('/')

        // 2. 获取类上的 @RequestMapping 路径
        val classPath = getClassPath(function).trim('/')

        // 3. 拼接完整路径
        return buildString {
            append('/')
            if (classPath.isNotEmpty()) {
                append(classPath)
            }
            if (methodPath.isNotEmpty()) {
                if (isNotEmpty() && length > 1) {
                    append('/')
                }
                append(methodPath)
            }
        }
    }

    /**
     * 获取方法级别的路径（从 @GetMapping, @PostMapping 等注解）
     */
    private fun getMethodPath(function: KSFunctionDeclaration): String {
        val requestMappingAnnotations = listOf(
            "org.springframework.web.bind.annotation.RequestMapping",
            "org.springframework.web.bind.annotation.GetMapping",
            "org.springframework.web.bind.annotation.PostMapping",
            "org.springframework.web.bind.annotation.PutMapping",
            "org.springframework.web.bind.annotation.DeleteMapping",
            "org.springframework.web.bind.annotation.PatchMapping"
        )

        for (annotation in function.annotations) {
            val annotationName = annotation.annotationType.resolve().declaration.qualifiedName?.asString()
            if (requestMappingAnnotations.contains(annotationName)) {
                // 获取 value 或 path 参数
                val pathArg =
                    annotation.arguments.find { it.name?.asString() == "value" || it.name?.asString() == "path" }
                if (pathArg != null) {
                    val paths = pathArg.value as? List<*>
                    if (!paths.isNullOrEmpty()) {
                        return paths.firstOrNull()?.toString()?.trim('"') ?: ""
                    }
                }
            }
        }
        return ""
    }

    /**
     * 获取类级别的路径（从类的 @RequestMapping 注解）
     */
    private fun getClassPath(function: KSFunctionDeclaration): String {
        val parentClass = function.parentDeclaration as? KSClassDeclaration ?: return ""

        val requestMappingAnnotation = parentClass.annotations.find {
            it.annotationType.resolve().declaration.qualifiedName?.asString() ==
                    "org.springframework.web.bind.annotation.RequestMapping"
        }

        if (requestMappingAnnotation != null) {
            val pathArg = requestMappingAnnotation.arguments.find {
                it.name?.asString() == "value" || it.name?.asString() == "path"
            }
            if (pathArg != null) {
                val paths = pathArg.value as? List<*>
                if (!paths.isNullOrEmpty()) {
                    return paths.firstOrNull()?.toString()?.trim('"') ?: ""
                }
            }
        }
        return ""
    }

    /**
     * 使用 KotlinPoet 生成包含公共访问 URL 列表的 Kotlin 文件
     */
    private fun generatePublicAccessUrlsFile(urlPaths: List<String>) {
        // 创建 object 对象
        val publicAccessUrlsObject = TypeSpec.objectBuilder("PublicAccessUrls")
            .addProperty(
                PropertySpec.builder(
                    "URLS",
                    List::class.asClassName()
                        .parameterizedBy(String::class.asTypeName())
                )
                    .initializer("listOf(${urlPaths.joinToString(", ") { "\"$it\"" }})")
                    .mutable(false)
                    .addKdoc("所有标记了 @PublicAccessUrl 注解的 URL 路径列表\n\n此列表在编译时自动生成，包含项目中所有被 [llh.fanclubvup.ksp.annon.PublicAccessUrl]\n注解标记的方法的完整 URL 路径。\n\n@return 不可变的 URL 字符串列表")
                    .build()
            )
            .addKdoc("公共访问 URL 集合\n\n此对象在编译时由 KSP (Kotlin Symbol Processing) 自动生成，\n收集了所有被 [llh.fanclubvup.ksp.annon.PublicAccessUrl] 注解标记的方法路径。\n\n@see llh.fanclubvup.ksp.annon.PublicAccessUrl\n@see URLS")
            .build()

        // 创建文件
        val fileSpec = FileSpec.builder("llh.fanclubvup.ksp.generated", "PublicAccessUrls")
            .addType(publicAccessUrlsObject)
            .build()

        // 写入文件
        fileSpec.writeTo(codeGenerator, aggregating = false)
        logger.info("已生成 PublicAccessUrls.kt 文件，包含 ${urlPaths.size} 个 URL 路径")
    }
}
