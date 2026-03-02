package llh.fanclubvup.apiserver.components.properties

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration


@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(JwtProperty::class)
class PropertiesRegister {
}
