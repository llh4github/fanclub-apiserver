package llh.fanclubvup.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import llh.fanclubvup.ksp.processor.PublicAccessUrlSymbolProcessor

class KspRegister {

}

class PublicAccessUrlProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return PublicAccessUrlSymbolProcessor(environment.logger)
    }
}
