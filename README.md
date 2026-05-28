# Galenos Pro — Backend

Backend del sistema hospitalario **Galenos Pro**, implementado como arquitectura de
microservicios con Spring Boot 4. Gestiona la autenticación de usuarios con JWT y el
inventario farmacéutico del sistema hospitalario.

---

## Tecnologías

| Tecnología | Versión |
|---|---|
| Java | 21 |
| Spring Boot | 4.0.5 |
| Spring Data JPA | (gestionado por Spring Boot) |
| Spring Security | (gestionado por Spring Boot) |
| JJWT | 0.11.5 |
| Jakarta Validation | (gestionado por Spring Boot) |
| Lombok | (gestionado por Spring Boot) |
| Microsoft SQL Server JDBC | (gestionado por Spring Boot) |
| PostgreSQL JDBC | (gestionado por Spring Boot) |
| JaCoCo | 0.8.12 |

---

## Microservicios

| Servicio | Puerto | Descripción |
|---|---|---|
| `auth-service` | `8085` | Registro, login y emisión de tokens JWT; gestión de usuarios y roles del sistema. |
| `farmacia-service` | `8081` | Gestión del inventario de productos farmacéuticos: categorías, lotes, movimientos de entrada/salida y alertas de stock. |

---

## Requisitos previos

- **JDK 21** o superior
- **Apache Maven 3.9+**
- **SQL Server** — para `auth-service` (base de datos `GalenosProDB`)
- **PostgreSQL 15+** — para `farmacia-service` (base de datos `galenos_farmacia`)

---

## Instalación

```bash
git clone https://github.com/tu-usuario/backend-galeno-pro.git
cd backend-galeno-pro
```

Instalar dependencias de cada microservicio:

```bash
cd auth-service
mvn dependency:resolve

cd ../farmacia-service
mvn dependency:resolve
```

---

## Configuración

### auth-service — `auth-service/src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=GalenosProDB;encrypt=true;trustServerCertificate=true;
spring.datasource.username=[usuario]
spring.datasource.password=[contraseña]
```

El **secret JWT** se encuentra en
`auth-service/src/main/java/.../Jwt/JwtService.java`, constante `SECRET_KEY`.
Debe ser una clave hexadecimal de al menos 256 bits y coincidir con el secret
configurado en `farmacia-service`.

### farmacia-service — `farmacia-service/src/main/resources/application.properties`

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/galenos_farmacia
spring.datasource.username=[usuario]
spring.datasource.password=[contraseña]

jwt.secret=[mismo-secret-hexadecimal-que-auth-service]
```

> **Importante:** el valor de `jwt.secret` en `farmacia-service` debe ser
> idéntico al `SECRET_KEY` definido en `auth-service`, ya que ambos servicios
> comparten el mismo firmante HS256.

---

## Ejecución

Cada microservicio se ejecuta de forma independiente:

```bash
# auth-service — http://localhost:8085
cd auth-service
mvn spring-boot:run

# farmacia-service — http://localhost:8081
cd farmacia-service
mvn spring-boot:run
```

Para generar el JAR ejecutable:

```bash
mvn clean package -DskipTests
java -jar target/<nombre>-0.0.1-SNAPSHOT.jar
```

---

## Pruebas y cobertura

Ambos microservicios cuentan con pruebas unitarias (JUnit 5 + Mockito) con umbral
mínimo de cobertura del 80 % verificado por JaCoCo.

```bash
mvn verify
# El reporte HTML queda en: target/site/jacoco/index.html
```

---

## Estructura de carpetas

```
backend-galeno-pro/
├── auth-service/
│   └── src/main/java/.../auth_service/
│       ├── Config/          # SecurityConfig, ApplicationConfig, DataInitializer
│       ├── Controller/      # AuthController, UserController, AuthService
│       ├── Dto/             # LoginRequest, RegisterRequest, AuthResponse, UserDto…
│       ├── Jwt/             # JwtService, JwtAuthenticationFilter
│       └── Model/           # User, Role, ERole, UserRepository, RoleRepository…
│
└── farmacia-service/
    └── src/main/java/.../farmacia_service/
        ├── Config/          # SecurityConfig
        ├── Controller/      # ProductoController/Service, CategoriaController/Service,
        │                    # LoteController/Service, MovimientoController/Service,
        │                    # AlertaController/Service
        ├── Dto/             # ProductoRequest/Response, CategoriaRequest/Response,
        │                    # EntradaRequest, SalidaRequest, LoteResponse,
        │                    # MovimientoResponse, AlertaStockResponse, AlertaLoteResponse…
        ├── Exception/       # GlobalExceptionHandler, ResourceNotFoundException,
        │                    # StockInsuficienteException
        ├── Jwt/             # JwtService, JwtAuthenticationFilter
        └── Model/           # Producto, Categoria, Lote, MovimientoCabecera,
                             # MovimientoDetalle, enums (EstadoProducto, EstadoLote,
                             # TipoMovimiento, FormaFarmaceutica) y Repositories
```

---

## Documentación adicional

La especificación de diseño detallada del módulo de farmacia se encuentra en:

```
farmacia-service/ESPECIFICACION-farmacia-service.md
```

---

## Roles del sistema

Los siguientes roles son inicializados automáticamente al arrancar `auth-service`:

| Rol | Descripción |
|---|---|
| `ADMIN` | Administrador del sistema |
| `TECNICO_FARMACIA` | Técnico de farmacia |
| `QUIMICO_FARMACEUTICO` | Químico farmacéutico |
| `JEFE_FARMACIA` | Jefe de farmacia |
| `AUXILIAR_ALMACEN` | Auxiliar de almacén |
