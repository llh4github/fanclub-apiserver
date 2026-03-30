package llh.fanclubvup.apiserver.components.properties

import llh.fanclubvup.common.consts.PropsKeys
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = PropsKeys.APP_PROP_KEY + ".websocket")
data class WebsocketProperty(
    /**
     * 允许的域名
     */
    val domain: String = "*"
)
