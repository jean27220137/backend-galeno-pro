package backend_galeno_pro.farmacia_service.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "movimientos_cabecera")
public class MovimientoCabecera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false, length = 20)
    private TipoMovimiento tipoMovimiento;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "numero_documento", length = 50)
    private String numeroDocumento;

    @Column
    private String observacion;

    @NotBlank
    @Column(name = "usuario_id", nullable = false)
    private String usuarioId;

    @ToString.Exclude
    @OneToMany(mappedBy = "cabecera", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MovimientoDetalle> detalles;
}
