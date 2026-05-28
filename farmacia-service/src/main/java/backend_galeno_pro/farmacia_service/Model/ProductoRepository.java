package backend_galeno_pro.farmacia_service.Model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    Page<Producto> findByEstado(EstadoProducto estado, Pageable pageable);

    @Query("SELECT p FROM Producto p WHERE p.estado = :estado AND (" +
           "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.codigo) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.principioActivo) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Producto> search(@Param("search") String search,
                          @Param("estado") EstadoProducto estado,
                          Pageable pageable);

    @Query("SELECT p FROM Producto p WHERE p.stockActual < p.stockMinimo AND p.estado = :estado")
    List<Producto> findStockBajo(@Param("estado") EstadoProducto estado);
}
