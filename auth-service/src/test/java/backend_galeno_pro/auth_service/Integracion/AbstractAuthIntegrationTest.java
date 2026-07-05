package backend_galeno_pro.auth_service.Integracion;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public abstract class AbstractAuthIntegrationTest {

    @Container
    protected static final MSSQLServerContainer<?> SQL_SERVER =
            new MSSQLServerContainer<>(
                    DockerImageName.parse("mcr.microsoft.com/mssql/server:2022-latest"))
                    .acceptLicense()
                    .withPassword("GalenosPro@2024!");

    @DynamicPropertySource
    static void configurarPropiedades(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      SQL_SERVER::getJdbcUrl);
        registry.add("spring.datasource.username", SQL_SERVER::getUsername);
        registry.add("spring.datasource.password", SQL_SERVER::getPassword);
    }
}
