/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import llh.fanclubvup.ksp.processor.CacheNameGenSymbolProcessor
import llh.fanclubvup.ksp.processor.PublicAccessUrlSymbolProcessor

class PublicAccessUrlProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return PublicAccessUrlSymbolProcessor(
            environment.logger,
            environment.codeGenerator
        )
    }
}
class CacheNameGenProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return CacheNameGenSymbolProcessor(
            environment.logger,
            environment.codeGenerator
        )
    }
}
