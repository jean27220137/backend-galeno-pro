package backend_galeno_pro.farmacia_service.Dto;

import backend_galeno_pro.farmacia_service.Model.EstadoProducto;
import backend_galeno_pro.farmacia_service.Model.FormaFarmaceutica;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponse {
    private Integer id;
    private String codigo;
    private String nombre;
    private String principioActivo;
    private String laboratorio;
    private String concentracion;
    private FormaFarmaceutica formaFarmaceutica;
    private String unidadMedida;
    private BigDecimal precioUnitario;
    private Integer stockActual;
    private Integer stockMinimo;
    private EstadoProducto estado;
    private CategoriaResponse categoria;
}
