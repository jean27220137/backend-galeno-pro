package backend_galeno_pro.farmacia_service.Dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalidaDetalleRequest {

    @NotNull(message = "El producto es obligatorio")
    private Integer productoId;

    @NotNull(message = "El lote es obligatorio")
    private Integer loteId;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    @NotNull(message = "El precio unitario es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    private BigDecimal precioUnitario;
}
