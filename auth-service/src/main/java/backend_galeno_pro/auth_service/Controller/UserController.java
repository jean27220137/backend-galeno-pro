package backend_galeno_pro.auth_service.Controller;

import backend_galeno_pro.auth_service.Dto.UpdateRolesRequest;
import backend_galeno_pro.auth_service.Dto.UserListDto;
import backend_galeno_pro.auth_service.Model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @GetMapping
    public ResponseEntity<List<UserListDto>> getAll() {
        List<UserListDto> users = userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream()
                        .noneMatch(r -> r.getName() == ERole.ADMIN))
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserListDto> getById(@PathVariable Integer id) {
        return userRepository.findById(id)
                .map(u -> ResponseEntity.ok(toDto(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/roles")
    public ResponseEntity<Void> updateRoles(@PathVariable Integer id,
                                            @RequestBody UpdateRolesRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));

        Set<Role> roles = new HashSet<>();
        for (String roleName : request.getRoles()) {
            ERole eRole = ERole.valueOf(roleName);
            Role role = roleRepository.findByName(eRole)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + eRole));
            roles.add(role);
        }
        user.setRoles(roles);
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(@PathVariable Integer id,
                                               @RequestParam EstadoUsuario estado) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));
        user.setEstado(estado);
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Void> toggleEstado(@PathVariable Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));
        user.setEstado(user.getEstado() == EstadoUsuario.ACTIVO
                ? EstadoUsuario.INACTIVO
                : EstadoUsuario.ACTIVO);
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    private UserListDto toDto(User u) {
        return UserListDto.builder()
                .id(u.getId())
                .username(u.getUsername())
                .nombres(u.getNombres())
                .apellidoPaterno(u.getApellidoPaterno())
                .apellidoMaterno(u.getApellidoMaterno())
                .correoInstitucional(u.getCorreoInstitucional())
                .numeroDocumento(u.getNumeroDocumento())
                .areaAsignada(u.getAreaAsignada())
                .estado(u.getEstado())
                .colegiatura(u.getColegiatura())
                .roles(u.getRoles().stream()
                        .map(r -> r.getName().name())
                        .collect(Collectors.toList()))
                .build();
    }
}
