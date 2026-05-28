package backend_galeno_pro.farmacia_service.Model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoCabeceraRepository extends JpaRepository<MovimientoCabecera, Integer> {

    Page<MovimientoCabecera> findByTipoMovimiento(TipoMovimiento tipo, Pageable pageable);
}
