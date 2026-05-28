package backend_galeno_pro.farmacia_service.Dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntradaDetalleRequest {

    @NotNull(message = "El producto es obligatorio")
    private Integer productoId;

    @NotBlank(message = "El número de lote es obligatorio")
    @Size(max = 50, message = "El número de lote no puede superar 50 caracteres")
    private String numeroLote;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    @FutureOrPresent(message = "La fecha de vencimiento debe ser hoy o una fecha futura")
    private LocalDate fechaVencimiento;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    @NotNull(message = "El precio unitario es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    private BigDecimal precioUnitario;
}
