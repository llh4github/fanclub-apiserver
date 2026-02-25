/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.components

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import java.util.Properties

/**
 * 启动时打印构建信息
 */
@Component
class AppStartupLogger
    : ApplicationListener<ApplicationReadyEvent>, InfoContributor {

    private val logger = KotlinLogging.logger {}
    private val properties: Properties = Properties()

    override fun contribute(builder: Info.Builder) {
        builder.withDetail("build-info", properties)
    }

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        loadProperties()
        if (properties.isEmpty) {
            logger.info { "App Build Info is empty" }
        } else {
            logger.info { "App Build Info: $properties" }
        }
    }

    private fun loadProperties() {
        if (properties.isEmpty) {
            javaClass.classLoader.getResourceAsStream("git.properties")?.use {
                properties.load(it)
            }
        }
    }
}