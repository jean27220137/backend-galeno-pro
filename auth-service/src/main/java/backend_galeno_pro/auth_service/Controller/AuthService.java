package backend_galeno_pro.auth_service.Controller;

import backend_galeno_pro.auth_service.Dto.AuthResponse;
import backend_galeno_pro.auth_service.Dto.LoginRequest;
import backend_galeno_pro.auth_service.Dto.RegisterRequest;
import backend_galeno_pro.auth_service.Dto.UserDto;
import backend_galeno_pro.auth_service.Jwt.JwtService;
import backend_galeno_pro.auth_service.Model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername()).orElseThrow();
        String jwtToken = jwtService.getToken(user);

        String role = user.getRoles().stream()
                .findFirst()
                .map(r -> r.getName().name())
                .orElse("USER");

        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getNombres() + " " + user.getApellidoPaterno() + " " + user.getApellidoMaterno())
                .correoInstitucional(user.getCorreoInstitucional())
                .role(role)
                .areaAsignada(user.getAreaAsignada())
                .estado(user.getEstado())
                .colegiatura(user.getColegiatura())
                .build();

        return AuthResponse.builder()
                .token(jwtToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .user(userDto)
                .build();
    }
    public AuthResponse register(RegisterRequest request){
        return registerInternal(request, false);
    }

    public AuthResponse registerAdmin(RegisterRequest request){
        return registerInternal(request, true);
    }

    private AuthResponse registerInternal(RegisterRequest request,boolean allowAdmin)  {

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new RuntimeException("El nombre del usuario es obligatorio");
        }
        if (request.getRoles() == null || request.getRoles().isEmpty()){
            throw new RuntimeException("Debe asignar al menos un rol");
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("El nombre de usuario " + request.getUsername() + "ya esta en uso.");
        }
        Set<Role> roles = new HashSet<>();

        for (String roleName: request.getRoles()){
            ERole eRole;
            try {
                eRole = ERole.valueOf(roleName);
            }catch (IllegalArgumentException e){
                throw new RuntimeException("El rol " + roleName + " no es valido. "  );
            }
            if (!allowAdmin && eRole == ERole.ADMIN){
                throw new RuntimeException("No esta permitido registrar usuarios con rol ADMIN desde / register");
            }
            Role role = roleRepository.findByName(eRole)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado en la Base de datos: " + eRole));
            roles.add(role);
        }
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .correoInstitucional(request.getCorreoInstitucional())
                .estado(request.getEstado() != null ? request.getEstado() : EstadoUsuario.ACTIVO)
                .tipoDocumento(request.getTipoDocumento())
                .nombres(request.getNombres())
                .numeroDocumento(request.getNumeroDocumento())
                .apellidoPaterno(request.getApellidoPaterno())
                .apellidoMaterno(request.getApellidoMaterno())
                .fechaNacimiento(request.getFechaNacimiento())
                .telefono(request.getTelefono())
                .direccionDomicilio(request.getDireccionDomicilio())
                .colegiatura(request.getColegiatura())
                .condicionLaboral(request.getCondicionLaboral())
                .areaAsignada(request.getAreaAsignada())
                .fechaIngreso(request.getFechaIngreso() != null
                    ? request.getFechaIngreso() : LocalDate.now())
                .firmaDigital(request.getFirmaDigital())
                .roles(roles)
                .build();

        userRepository.save(user);

        return AuthResponse.builder()
                .token(jwtService.getToken(user))
                .tokenType("Bearer")
                .expiresIn(86400L)
                .build();

    }
}