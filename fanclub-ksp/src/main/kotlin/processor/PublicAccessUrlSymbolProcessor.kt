package llh.fanclubvup.ksp.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import llh.fanclubvup.ksp.annon.PublicAccessUrl

class PublicAccessUrlSymbolProcessor(
    private val logger: KSPLogger,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("PublicAccessUrlSymbolProcessor 插件开始处理")
        val list = resolver.getSymbolsWithAnnotation(PublicAccessUrl::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.validate() && it.classKind == ClassKind.ENUM_CLASS }
            .toList()
        if (list.isEmpty()) {
            logger.info("没有被标记注解的方法")
        }
        logger.info("PublicAccessUrlSymbolProcessor 插件处理结束")
        return emptyList()
    }
}
