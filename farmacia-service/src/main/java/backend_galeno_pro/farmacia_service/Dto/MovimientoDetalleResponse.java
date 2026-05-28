package backend_galeno_pro.farmacia_service.Dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoDetalleResponse {
    private Integer id;
    private Integer productoId;
    private String nombreProducto;
    private Integer loteId;
    private String numeroLote;
    private Integer cantidad;
    private BigDecimal precioUnitario;
}
