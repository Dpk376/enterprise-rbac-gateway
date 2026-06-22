package com.enterprise.policy.gateway;

import com.enterprise.policy.gateway.admin.Role;
import com.enterprise.policy.gateway.admin.RoleRepository;
import io.r2dbc.postgresql.codec.Json;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class OpaIntegrationTest {

    @MockBean
    ReactiveJwtDecoder reactiveJwtDecoder;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("audit_db")
            .withUsername("admin")
            .withPassword("password");

    @Container
    static GenericContainer<?> opa = new GenericContainer<>("openpolicyagent/opa:latest")
            .withExposedPorts(8181)
            .withCommand("run", "--server", "--log-level=debug");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://" + postgres.getHost() + ":" + postgres.getFirstMappedPort() + "/" + postgres.getDatabaseName());
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
        
        registry.add("opa.base-url", () -> "http://" + opa.getHost() + ":" + opa.getFirstMappedPort());
        registry.add("opa.url", () -> "http://" + opa.getHost() + ":" + opa.getFirstMappedPort() + "/v1/data/authz/allow");
    }

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void testDatabaseAndContext() {
        Role r = new Role(UUID.randomUUID(), "ADMIN", "Admin role", Json.of("[{\"method\":\"GET\", \"path\":\"/api/test\"}]"), LocalDateTime.now());
        roleRepository.save(r).block();
    }
}
