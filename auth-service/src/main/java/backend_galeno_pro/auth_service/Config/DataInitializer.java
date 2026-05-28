package backend_galeno_pro.auth_service.Config;

import backend_galeno_pro.auth_service.Model.ERole;
import backend_galeno_pro.auth_service.Model.Role;
import backend_galeno_pro.auth_service.Model.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args){
        for (ERole eRole: ERole.values()){
            if (!roleRepository.existsByName(eRole)){
                roleRepository.save(Role.builder().name(eRole).build());
                log.info("Rol Creado: {}",eRole);
            }
            else{
                log.info("Rol ya existe ,omitiendo: {}",eRole);
            }

        }
    }

}
