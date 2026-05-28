package backend_galeno_pro.farmacia_service.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String nombre;

    @NotBlank
    @Column(name = "principio_activo", nullable = false)
    private String principioActivo;

    @Column
    private String laboratorio;

    @Column
    private String concentracion;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_farmaceutica", length = 20)
    private FormaFarmaceutica formaFarmaceutica;

    @NotBlank
    @Column(name = "unidad_medida", nullable = false)
    private String unidadMedida;

    @NotNull
    @Positive
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Builder.Default
    @Min(0)
    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual = 0;

    @Builder.Default
    @Min(0)
    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo = 10;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private EstadoProducto estado = EstadoProducto.ACTIVO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    @ToString.Exclude
    private Categoria categoria;
}
