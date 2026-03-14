/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.components.schedule

import llh.fanclubvup.apiserver.service.sys.ScraperCookieService
import llh.fanclubvup.bilisdk.cache.PersistentCookieJarManager
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class AppStartEventSchedule(
    private val persistentCookieJarManager: PersistentCookieJarManager,
    private val service: ScraperCookieService,
) {
    @EventListener(ApplicationStartedEvent::class)
    fun resetCookies() {
        persistentCookieJarManager.resetCookies {
            service.listQuery().map { it.toCookie() }.toList()
        }
    }
}
