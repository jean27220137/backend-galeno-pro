package backend_galeno_pro.auth_service.Controller;

import backend_galeno_pro.auth_service.Dto.AuthResponse;
import backend_galeno_pro.auth_service.Dto.LoginRequest;
import backend_galeno_pro.auth_service.Dto.RegisterRequest;
import backend_galeno_pro.auth_service.Jwt.JwtService;
import backend_galeno_pro.auth_service.Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User mockUser;
    private Role tecnicoRole;

    @BeforeEach
    void setUp() {
        tecnicoRole = Role.builder().id(2).name(ERole.TECNICO_FARMACIA).build();
        mockUser = User.builder()
                .id(1)
                .username("testuser")
                .password("encodedpass")
                .nombres("Juan")
                .apellidoPaterno("Pérez")
                .apellidoMaterno("García")
                .estado(EstadoUsuario.ACTIVO)
                .roles(new HashSet<>(Set.of(tecnicoRole)))
                .build();
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    void login_happyPath_returnsAuthResponseWithToken() {
        LoginRequest req = LoginRequest.builder().username("testuser").password("pass").build();
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken("testuser", "pass"));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(jwtService.getToken(mockUser)).thenReturn("jwt-token");

        AuthResponse response = authService.login(req);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUser().getUsername()).isEqualTo("testuser");
        assertThat(response.getUser().getRole()).isEqualTo("TECNICO_FARMACIA");
    }

    @Test
    void login_userWithNoRoles_defaultsToUserRole() {
        mockUser.setRoles(new HashSet<>());
        LoginRequest req = LoginRequest.builder().username("testuser").password("pass").build();
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(jwtService.getToken(mockUser)).thenReturn("token");

        AuthResponse response = authService.login(req);

        assertThat(response.getUser().getRole()).isEqualTo("USER");
    }

    @Test
    void login_userNotFound_throwsNoSuchElementException() {
        LoginRequest req = LoginRequest.builder().username("ghost").password("pass").build();
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(java.util.NoSuchElementException.class);
    }

    // ── register ──────────────────────────────────────────────────────────────

    @Test
    void register_happyPath_savesUserAndReturnsToken() {
        RegisterRequest req = buildRequest("newuser", Set.of("TECNICO_FARMACIA"));
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(roleRepository.findByName(ERole.TECNICO_FARMACIA)).thenReturn(Optional.of(tecnicoRole));
        when(passwordEncoder.encode(any())).thenReturn("encodedpass");
        when(jwtService.getToken(any())).thenReturn("new-token");

        AuthResponse response = authService.register(req);

        assertThat(response.getToken()).isEqualTo("new-token");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_defaultsFechaIngresoToToday_whenNull() {
        RegisterRequest req = buildRequest("newuser", Set.of("TECNICO_FARMACIA"));
        req.setFechaIngreso(null);
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(roleRepository.findByName(ERole.TECNICO_FARMACIA)).thenReturn(Optional.of(tecnicoRole));
        when(passwordEncoder.encode(any())).thenReturn("enc");
        when(jwtService.getToken(any())).thenReturn("tok");

        assertThatCode(() -> authService.register(req)).doesNotThrowAnyException();
    }

    @Test
    void register_blankUsername_throwsRuntimeException() {
        assertThatThrownBy(() -> authService.register(buildRequest("  ", Set.of("TECNICO_FARMACIA"))))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("obligatorio");
    }

    @Test
    void register_nullUsername_throwsRuntimeException() {
        assertThatThrownBy(() -> authService.register(buildRequest(null, Set.of("TECNICO_FARMACIA"))))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("obligatorio");
    }

    @Test
    void register_emptyRoles_throwsRuntimeException() {
        assertThatThrownBy(() -> authService.register(buildRequest("newuser", Set.of())))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("rol");
    }

    @Test
    void register_nullRoles_throwsRuntimeException() {
        RegisterRequest req = buildRequest("newuser", null);
        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("rol");
    }

    @Test
    void register_duplicateUsername_throwsRuntimeException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        assertThatThrownBy(() -> authService.register(buildRequest("testuser", Set.of("TECNICO_FARMACIA"))))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ya esta en uso");
    }

    @Test
    void register_invalidRoleName_throwsRuntimeException() {
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.register(buildRequest("newuser", Set.of("ROL_INEXISTENTE"))))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no es valido");
    }

    @Test
    void register_adminRoleViaPublicEndpoint_throwsRuntimeException() {
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.register(buildRequest("newuser", Set.of("ADMIN"))))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ADMIN");
    }

    @Test
    void register_roleNotFoundInDb_throwsRuntimeException() {
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(roleRepository.findByName(ERole.TECNICO_FARMACIA)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authService.register(buildRequest("newuser", Set.of("TECNICO_FARMACIA"))))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Rol no encontrado");
    }

    // ── registerAdmin ─────────────────────────────────────────────────────────

    @Test
    void registerAdmin_allowsAdminRole() {
        Role adminRole = Role.builder().id(1).name(ERole.ADMIN).build();
        when(userRepository.findByUsername("adminuser")).thenReturn(Optional.empty());
        when(roleRepository.findByName(ERole.ADMIN)).thenReturn(Optional.of(adminRole));
        when(passwordEncoder.encode(any())).thenReturn("enc");
        when(jwtService.getToken(any())).thenReturn("admin-token");

        AuthResponse response = authService.registerAdmin(buildRequest("adminuser", Set.of("ADMIN")));

        assertThat(response.getToken()).isEqualTo("admin-token");
    }

    @Test
    void registerAdmin_multipleRoles_savesAllRoles() {
        Role jefeRole = Role.builder().id(3).name(ERole.JEFE_FARMACIA).build();
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(roleRepository.findByName(ERole.TECNICO_FARMACIA)).thenReturn(Optional.of(tecnicoRole));
        when(roleRepository.findByName(ERole.JEFE_FARMACIA)).thenReturn(Optional.of(jefeRole));
        when(passwordEncoder.encode(any())).thenReturn("enc");
        when(jwtService.getToken(any())).thenReturn("tok");

        assertThatCode(() ->
            authService.registerAdmin(buildRequest("newuser", Set.of("TECNICO_FARMACIA", "JEFE_FARMACIA")))
        ).doesNotThrowAnyException();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private RegisterRequest buildRequest(String username, Set<String> roles) {
        return RegisterRequest.builder()
                .username(username)
                .password("pass123")
                .nombres("Juan")
                .apellidoPaterno("Pérez")
                .apellidoMaterno("García")
                .roles(roles)
                .build();
    }
}
