package backend_galeno_pro.farmacia_service.Dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntradaRequest {

    private String numeroDocumento;
    private String observacion;

    @NotNull
    @NotEmpty(message = "Debe incluir al menos un detalle")
    @Valid
    private List<EntradaDetalleRequest> detalles;
}
