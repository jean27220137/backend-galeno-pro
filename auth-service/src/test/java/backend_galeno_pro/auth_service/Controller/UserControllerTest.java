package backend_galeno_pro.auth_service.Controller;

import backend_galeno_pro.auth_service.Dto.UpdateRolesRequest;
import backend_galeno_pro.auth_service.Dto.UserListDto;
import backend_galeno_pro.auth_service.Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;

    @InjectMocks
    private UserController userController;

    private Role tecnicoRole;
    private Role adminRole;
    private User regularUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        tecnicoRole = Role.builder().id(2).name(ERole.TECNICO_FARMACIA).build();
        adminRole   = Role.builder().id(1).name(ERole.ADMIN).build();

        regularUser = User.builder()
                .id(1)
                .username("tecnico")
                .password("enc")
                .nombres("Ana")
                .apellidoPaterno("Lopez")
                .apellidoMaterno("Torres")
                .estado(EstadoUsuario.ACTIVO)
                .roles(new HashSet<>(Set.of(tecnicoRole)))
                .build();

        adminUser = User.builder()
                .id(2)
                .username("admin")
                .password("enc")
                .nombres("Root")
                .apellidoPaterno("Sys")
                .apellidoMaterno("Tem")
                .estado(EstadoUsuario.ACTIVO)
                .roles(new HashSet<>(Set.of(adminRole)))
                .build();
    }

    // ── getAll ────────────────────────────────────────────────────────────────

    @Test
    void getAll_excludesAdminUsers_returnsOnlyNonAdmin() {
        when(userRepository.findAll()).thenReturn(List.of(regularUser, adminUser));

        ResponseEntity<List<UserListDto>> response = userController.getAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getUsername()).isEqualTo("tecnico");
    }

    @Test
    void getAll_emptyRepository_returnsEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        ResponseEntity<List<UserListDto>> response = userController.getAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void getAll_onlyAdminUsers_returnsEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of(adminUser));

        ResponseEntity<List<UserListDto>> response = userController.getAll();

        assertThat(response.getBody()).isEmpty();
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    void getById_found_returnsDtoWith200() {
        when(userRepository.findById(1)).thenReturn(Optional.of(regularUser));

        ResponseEntity<UserListDto> response = userController.getById(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUsername()).isEqualTo("tecnico");
        assertThat(response.getBody().getRoles()).contains("TECNICO_FARMACIA");
    }

    @Test
    void getById_notFound_returns404() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        ResponseEntity<UserListDto> response = userController.getById(99);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ── updateRoles ───────────────────────────────────────────────────────────

    @Test
    void updateRoles_happyPath_savesNewRoles() {
        when(userRepository.findById(1)).thenReturn(Optional.of(regularUser));
        when(roleRepository.findByName(ERole.JEFE_FARMACIA))
                .thenReturn(Optional.of(Role.builder().id(3).name(ERole.JEFE_FARMACIA).build()));
        UpdateRolesRequest req = UpdateRolesRequest.builder()
                .roles(Set.of("JEFE_FARMACIA")).build();

        ResponseEntity<Void> response = userController.updateRoles(1, req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(userRepository).save(regularUser);
    }

    @Test
    void updateRoles_userNotFound_throwsRuntimeException() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());
        UpdateRolesRequest req = UpdateRolesRequest.builder()
                .roles(Set.of("TECNICO_FARMACIA")).build();

        assertThatThrownBy(() -> userController.updateRoles(99, req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateRoles_roleNotFoundInDb_throwsRuntimeException() {
        when(userRepository.findById(1)).thenReturn(Optional.of(regularUser));
        when(roleRepository.findByName(ERole.TECNICO_FARMACIA)).thenReturn(Optional.empty());
        UpdateRolesRequest req = UpdateRolesRequest.builder()
                .roles(Set.of("TECNICO_FARMACIA")).build();

        assertThatThrownBy(() -> userController.updateRoles(1, req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Rol no encontrado");
    }

    // ── cambiarEstado ─────────────────────────────────────────────────────────

    @Test
    void cambiarEstado_setsNewEstado_andSaves() {
        when(userRepository.findById(1)).thenReturn(Optional.of(regularUser));

        ResponseEntity<Void> response = userController.cambiarEstado(1, EstadoUsuario.INACTIVO);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(regularUser.getEstado()).isEqualTo(EstadoUsuario.INACTIVO);
        verify(userRepository).save(regularUser);
    }

    @Test
    void cambiarEstado_userNotFound_throwsRuntimeException() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userController.cambiarEstado(99, EstadoUsuario.INACTIVO))
                .isInstanceOf(RuntimeException.class);
    }

    // ── toggleEstado ──────────────────────────────────────────────────────────

    @Test
    void toggleEstado_activoBecomesInactivo() {
        regularUser.setEstado(EstadoUsuario.ACTIVO);
        when(userRepository.findById(1)).thenReturn(Optional.of(regularUser));

        userController.toggleEstado(1);

        assertThat(regularUser.getEstado()).isEqualTo(EstadoUsuario.INACTIVO);
        verify(userRepository).save(regularUser);
    }

    @Test
    void toggleEstado_inactivoBecomesActivo() {
        regularUser.setEstado(EstadoUsuario.INACTIVO);
        when(userRepository.findById(1)).thenReturn(Optional.of(regularUser));

        userController.toggleEstado(1);

        assertThat(regularUser.getEstado()).isEqualTo(EstadoUsuario.ACTIVO);
        verify(userRepository).save(regularUser);
    }

    @Test
    void toggleEstado_userNotFound_throwsRuntimeException() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userController.toggleEstado(99))
                .isInstanceOf(RuntimeException.class);
    }
}
