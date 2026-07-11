package backend_galeno_pro.auth_service.Integracion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("Prueba de integración con SQL Server - Testcontainers")
class UsuarioRepositoryIntegrationTest extends AbstractAuthIntegrationTest {

    @Test
    @DisplayName("TC-01: Contenedor SQL Server debe estar corriendo")
    void contenedorDebeEstarActivo() {
        assertThat(SQL_SERVER.isRunning()).isTrue();
        System.out.println("✓ SQL Server corriendo en: " + SQL_SERVER.getJdbcUrl());
    }

    @Test
    @DisplayName("TC-02: URL JDBC del contenedor debe ser válida")
    void urlJdbcDebeSerValida() {
        String url = SQL_SERVER.getJdbcUrl();
        assertThat(url).contains("sqlserver").contains("localhost");
        System.out.println("✓ JDBC URL: " + url);
    }
}