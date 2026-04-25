package backend_galeno_pro.auth_service.Dto;

import backend_galeno_pro.auth_service.Model.AreaAsignada;
import backend_galeno_pro.auth_service.Model.EstadoUsuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserListDto {
    private Integer id;
    private String username;
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String correoInstitucional;
    private String numeroDocumento;
    private AreaAsignada areaAsignada;
    private EstadoUsuario estado;
    private String colegiatura;
    private List<String> roles;
}
