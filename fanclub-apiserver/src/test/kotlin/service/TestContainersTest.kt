/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.service

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
@SpringBootTest
@ActiveProfiles("itest")
class TestContainersTest {

    companion object {

        @Container
        val postgreSQLContainer: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("fanclub_dev")
            .withUsername("postgres")
            .withPassword("123456")

        @Container
        val redisContainer: GenericContainer<*> = GenericContainer(DockerImageName.parse("redis:7.4.0-alpine"))
            .withExposedPorts(6379)
            .withReuse(true)

        @JvmStatic
        @DynamicPropertySource
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl)
            registry.add("spring.datasource.username", postgreSQLContainer::getUsername)
            registry.add("spring.datasource.password", postgreSQLContainer::getPassword)
            registry.add("spring.data.redis.host", redisContainer::getHost)
            registry.add("spring.data.redis.port") { redisContainer.getMappedPort(6379) }
        }
    }

    @Test
    fun testPostgreSQLContainer() {
        assert(postgreSQLContainer.isRunning)
        assert(postgreSQLContainer.databaseName == "fanclub_dev")
    }

    @Test
    fun testRedisContainer() {
        assert(redisContainer.isRunning)
    }
}