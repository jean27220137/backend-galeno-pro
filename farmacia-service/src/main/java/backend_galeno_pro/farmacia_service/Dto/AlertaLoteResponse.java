package backend_galeno_pro.farmacia_service.Dto;

import backend_galeno_pro.farmacia_service.Model.EstadoLote;
import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaLoteResponse {
    private Integer loteId;
    private Integer productoId;
    private String nombreProducto;
    private String numeroLote;
    private LocalDate fechaVencimiento;
    private Integer cantidadDisponible;
    private EstadoLote estado;
    private long diasParaVencer;
}
