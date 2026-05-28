package backend_galeno_pro.farmacia_service.Dto;

import backend_galeno_pro.farmacia_service.Model.TipoMovimiento;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoResponse {
    private Integer id;
    private TipoMovimiento tipoMovimiento;
    private LocalDateTime fecha;
    private String numeroDocumento;
    private String observacion;
    private String usuarioId;
    private List<MovimientoDetalleResponse> detalles;
}
