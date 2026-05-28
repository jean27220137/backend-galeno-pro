package backend_galeno_pro.farmacia_service.Controller;

import backend_galeno_pro.farmacia_service.Dto.CategoriaRequest;
import backend_galeno_pro.farmacia_service.Dto.CategoriaResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/farmacia/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> listar() {
        return ResponseEntity.ok(categoriaService.listar());
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','QUIMICO_FARMACEUTICO','JEFE_FARMACIA')")
    public ResponseEntity<CategoriaResponse> crear(@Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','QUIMICO_FARMACEUTICO','JEFE_FARMACIA')")
    public ResponseEntity<CategoriaResponse> actualizar(@PathVariable Integer id,
                                                        @Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.ok(categoriaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','QUIMICO_FARMACEUTICO','JEFE_FARMACIA')")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id) {
        categoriaService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
