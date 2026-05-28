package backend_galeno_pro.auth_service.Dto;

import backend_galeno_pro.auth_service.Model.AreaAsignada;
import backend_galeno_pro.auth_service.Model.CondicionLaboral;
import backend_galeno_pro.auth_service.Model.EstadoUsuario;
import backend_galeno_pro.auth_service.Model.TipoDocumento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    String username;
    String password;
    String correoInstitucional;
    TipoDocumento tipoDocumento;
    String numeroDocumento;
    String nombres;
    String apellidoPaterno;
    String apellidoMaterno;
    LocalDate fechaNacimiento;
    String telefono;
    String direccionDomicilio;
    String colegiatura;
    CondicionLaboral condicionLaboral;
    AreaAsignada areaAsignada;
    LocalDate fechaIngreso;
    String firmaDigital;
    EstadoUsuario estado;
    Set<String> roles;
}
