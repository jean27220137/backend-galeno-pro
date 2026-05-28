package backend_galeno_pro.farmacia_service.Controller;

import backend_galeno_pro.farmacia_service.Dto.LoteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/farmacia/lotes")
@RequiredArgsConstructor
public class LoteController {

    private final LoteService loteService;

    @GetMapping
    public ResponseEntity<List<LoteResponse>> listar(
            @RequestParam(required = false) Integer productoId) {
        return ResponseEntity.ok(loteService.listar(productoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoteResponse> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(loteService.obtener(id));
    }
}
