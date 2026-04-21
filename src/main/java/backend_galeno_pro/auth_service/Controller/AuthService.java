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
                .fullName(user.getFirstname() + " " + user.getLastname())
                .email("")
                .role(role)
                .build();

        return AuthResponse.builder()
                .token(jwtToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .user(userDto)
                .build();
    }

    public AuthResponse register(RegisterRequest request) {

        if (request.getUsername() == null || request.getRoles().isEmpty()) {
            throw new RuntimeException("El nombre del usuario es obligatorio");
        }
        if(userRepository.findByUsername(request.getUsername()).isPresent()){
            throw new RuntimeException("El nombre de usuario " + request.getUsername() + "ya esta en uso.");
        }

        Set<Role> roles = new HashSet<>();
        roles.add(roleRepository.findByName(ERole.FARMACEUTICO).orElseThrow());
        User user = User.builder()
                .username(request.getUsername())
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .build();

        userRepository.save(user);

        return AuthResponse.builder()
                .token(jwtService.getToken(user))
                .build();
    }
}
