package backend_galeno_pro.auth_service.Dto;

import backend_galeno_pro.auth_service.Model.AreaAsignada;
import backend_galeno_pro.auth_service.Model.EstadoUsuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Integer id;
    private String username;
    private String fullName;
    private String correoInstitucional;
    private String numeroDocumento;
    private String role;
    private AreaAsignada areaAsignada;
    private EstadoUsuario estado;
    private String colegiatura;
}
