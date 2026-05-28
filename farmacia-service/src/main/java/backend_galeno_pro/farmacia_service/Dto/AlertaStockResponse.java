package backend_galeno_pro.farmacia_service.Dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertaStockResponse {
    private Integer productoId;
    private String codigo;
    private String nombre;
    private Integer stockActual;
    private Integer stockMinimo;
    private Integer deficit;
}
