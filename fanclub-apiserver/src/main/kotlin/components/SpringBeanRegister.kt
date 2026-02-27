/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.components

import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient


@Component
class SpringBeanRegister {

    @Bean("biliLiveOpenApiRestClient")
    fun biliLiveOpenApiRestClient(builder: RestClient.Builder): RestClient {
        return builder
            .baseUrl("https://live-open.biliapi.com")
            .defaultHeader("Content-Type", "application/json")
            .defaultHeader("Accept", "application/json")
            .build()
    }
}