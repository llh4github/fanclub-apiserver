package llh.fanclubvup.apiserver.service

import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorFollowerDateNumQuerySpec
import llh.fanclubvup.apiserver.entity.anchor.dto.AnchorFollowerNumInput
import llh.fanclubvup.apiserver.service.anchor.AnchorFollowerNumService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest
@ActiveProfiles("docker")
class AnchorFollowerNumServiceTest {

    @Autowired
    private lateinit var service: AnchorFollowerNumService

    @Test
    fun test_upsertFollowerNum() {
        service.upsert(
            AnchorFollowerNumInput(114514, 114514, LocalDate.now()),
            AnchorFollowerNumService.UniqueKeys.defaultUniqueKeys
        )
        service.upsert(
            AnchorFollowerNumInput(114514, 114516, LocalDate.now()),
            AnchorFollowerNumService.UniqueKeys.defaultUniqueKeys
        )
        assertEquals(
            114516,
            service.queryNum(AnchorFollowerDateNumQuerySpec(114514, LocalDate.now()))
        )
    }
}
