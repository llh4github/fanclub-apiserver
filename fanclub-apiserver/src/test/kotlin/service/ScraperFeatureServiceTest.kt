package llh.fanclubvup.apiserver.service

import llh.fanclubvup.apiserver.service.sys.ScraperFeatureService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test


@SpringBootTest
@ActiveProfiles("docker")
class ScraperFeatureServiceTest {

    @Autowired
    private lateinit var service: ScraperFeatureService

    @Test
    fun a() {
        val followerEnabled = service.queryFollowerEnabled()
        println(followerEnabled)
    }

    @Test
    fun b() {
        val monitorEnabled = service.queryMonitorEnabled()
        println(monitorEnabled)
    }
}
