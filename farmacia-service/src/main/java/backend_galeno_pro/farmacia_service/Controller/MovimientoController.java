package backend_galeno_pro.farmacia_service.Controller;

import backend_galeno_pro.farmacia_service.Dto.EntradaRequest;
import backend_galeno_pro.farmacia_service.Dto.MovimientoResponse;
import backend_galeno_pro.farmacia_service.Dto.SalidaRequest;
import backend_galeno_pro.farmacia_service.Model.TipoMovimiento;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/farmacia/movimientos")
@RequiredArgsConstructor
public class MovimientoController {

    private final MovimientoService movimientoService;

    @PostMapping("/entradas")
    @PreAuthorize("hasAnyAuthority('ADMIN','QUIMICO_FARMACEUTICO','JEFE_FARMACIA','TECNICO_FARMACIA')")
    public ResponseEntity<MovimientoResponse> registrarEntrada(
            @Valid @RequestBody EntradaRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(movimientoService.registrarEntrada(request, authentication.getName()));
    }

    @PostMapping("/salidas")
    @PreAuthorize("hasAnyAuthority('ADMIN','QUIMICO_FARMACEUTICO','JEFE_FARMACIA','TECNICO_FARMACIA')")
    public ResponseEntity<MovimientoResponse> registrarSalida(
            @Valid @RequestBody SalidaRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(movimientoService.registrarSalida(request, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<Page<MovimientoResponse>> historial(
            @RequestParam(required = false) TipoMovimiento tipo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(movimientoService.historial(tipo, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovimientoResponse> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(movimientoService.obtener(id));
    }
}
