package backend_galeno_pro.auth_service.Config;

import backend_galeno_pro.auth_service.Model.ERole;
import backend_galeno_pro.auth_service.Model.Role;
import backend_galeno_pro.auth_service.Model.RoleRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.CommandLineRunner;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner initRoles(RoleRepository roleRepository){
        return args -> {
            for (ERole rolename : ERole.values()){
                roleRepository.save(Role.builder().name(rolename).build());
                System.out.println("Rol creado: " + rolename);
            }
        };
    }

}
