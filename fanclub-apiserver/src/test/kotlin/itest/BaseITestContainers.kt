/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.apiserver.itest

import org.junit.jupiter.api.Test
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

/**
 * 基础测试容器类，提供 PostgreSQL 和 Redis 容器支持
 * 其他测试类可以继承此类以获得容器环境
 */
@Testcontainers
@ActiveProfiles("itest")
abstract class BaseITestContainers {

    companion object {

        /**
         * PostgreSQL 容器实例
         */
        @Container
        protected val postgreSQLContainer: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("fanclub_dev")
            .withUsername("postgres")
            .withPassword("123456")

        /**
         * Redis 容器实例
         */
        @Container
        protected val redisContainer: GenericContainer<*> = GenericContainer(DockerImageName.parse("redis:7.4.0-alpine"))
            .withExposedPorts(6379)
            .withReuse(true)

        /**
         * 注册动态属性，将容器配置注入到 Spring 环境
         */
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

    /**
     * 测试 PostgreSQL 容器是否正常运行
     */
    @Test
    fun testPostgreSQLContainer() {
        assert(postgreSQLContainer.isRunning)
        assert(postgreSQLContainer.databaseName == "fanclub_dev")
    }

    /**
     * 测试 Redis 容器是否正常运行
     */
    @Test
    fun testRedisContainer() {
        assert(redisContainer.isRunning)
    }
}