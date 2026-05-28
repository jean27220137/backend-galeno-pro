package backend_galeno_pro.farmacia_service.Dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaResponse {
    private Integer id;
    private String nombre;
    private String descripcion;
    private boolean activo;
}
