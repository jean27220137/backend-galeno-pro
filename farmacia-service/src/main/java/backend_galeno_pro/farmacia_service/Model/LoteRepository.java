package backend_galeno_pro.farmacia_service.Model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface LoteRepository extends JpaRepository<Lote, Integer> {

    List<Lote> findByProductoId(Integer productoId);

    @Query("SELECT l FROM Lote l WHERE l.estado = :estado AND l.fechaVencimiento <= :fechaLimite")
    List<Lote> findPorVencer(@Param("estado") EstadoLote estado,
                             @Param("fechaLimite") LocalDate fechaLimite);

    @Query("SELECT l FROM Lote l WHERE l.fechaVencimiento < :hoy")
    List<Lote> findVencidos(@Param("hoy") LocalDate hoy);
}
