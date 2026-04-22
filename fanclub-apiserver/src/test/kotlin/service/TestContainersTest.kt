package llh.fanclubvup.apiserver.service

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@SpringBootTest
class TestContainersTest {

    companion object {
        init {
            System.setProperty("docker.host", "npipe:////./pipe/docker_engine")
        }

        @Container
        val postgreSQLContainer: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("fanclub_dev")
            .withUsername("postgres")
            .withPassword("123456")

        @JvmStatic
        @DynamicPropertySource
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgreSQLContainer::getUsername)
            registry.add("spring.datasource.password", postgreSQLContainer::getPassword)
        }
    }

    @Test
    fun testPostgreSQLContainer() {
        // 测试PostgreSQL容器是否正常运行
        assert(postgreSQLContainer.isRunning)
        assert(postgreSQLContainer.databaseName == "fanclub_dev")
    }
}