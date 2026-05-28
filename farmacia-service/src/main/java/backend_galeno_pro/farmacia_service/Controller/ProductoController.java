package backend_galeno_pro.farmacia_service.Controller;

import backend_galeno_pro.farmacia_service.Dto.ProductoRequest;
import backend_galeno_pro.farmacia_service.Dto.ProductoResponse;
import backend_galeno_pro.farmacia_service.Model.EstadoProducto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/farmacia/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    public ResponseEntity<Page<ProductoResponse>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productoService.listar(search, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(productoService.obtener(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','QUIMICO_FARMACEUTICO','JEFE_FARMACIA','TECNICO_FARMACIA')")
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','QUIMICO_FARMACEUTICO','JEFE_FARMACIA','TECNICO_FARMACIA')")
    public ResponseEntity<ProductoResponse> actualizar(@PathVariable Integer id,
                                                       @Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.ok(productoService.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyAuthority('ADMIN','QUIMICO_FARMACEUTICO','JEFE_FARMACIA','TECNICO_FARMACIA')")
    public ResponseEntity<Void> cambiarEstado(@PathVariable Integer id,
                                               @RequestParam EstadoProducto estado) {
        productoService.cambiarEstado(id, estado);
        return ResponseEntity.noContent().build();
    }
}
