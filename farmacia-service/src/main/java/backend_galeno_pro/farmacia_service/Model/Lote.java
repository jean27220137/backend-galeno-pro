package backend_galeno_pro.farmacia_service.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lotes")
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    @ToString.Exclude
    private Producto producto;

    @NotBlank
    @Size(max = 50)
    @Column(name = "numero_lote", nullable = false, length = 50)
    private String numeroLote;

    @NotNull
    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @NotNull
    @Positive
    @Column(name = "cantidad_inicial", nullable = false)
    private Integer cantidadInicial;

    @NotNull
    @Min(0)
    @Column(name = "cantidad_disponible", nullable = false)
    private Integer cantidadDisponible;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private EstadoLote estado = EstadoLote.VIGENTE;
}
