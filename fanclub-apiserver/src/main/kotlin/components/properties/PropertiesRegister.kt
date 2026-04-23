/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.components.properties

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration


@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(
    JwtProperty::class,
    WebConfigProperty::class,
    FanclubSupportProperty::class,
)
class PropertiesRegister {
}
