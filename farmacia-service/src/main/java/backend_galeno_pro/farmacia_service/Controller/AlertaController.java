package backend_galeno_pro.farmacia_service.Controller;

import backend_galeno_pro.farmacia_service.Dto.AlertaLoteResponse;
import backend_galeno_pro.farmacia_service.Dto.AlertaStockResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/farmacia/alertas")
@RequiredArgsConstructor
public class AlertaController {

    private final AlertaService alertaService;

    @GetMapping("/stock-bajo")
    public ResponseEntity<List<AlertaStockResponse>> stockBajo() {
        return ResponseEntity.ok(alertaService.getStockBajo());
    }

    @GetMapping("/por-vencer")
    public ResponseEntity<List<AlertaLoteResponse>> porVencer() {
        return ResponseEntity.ok(alertaService.getPorVencer());
    }

    @GetMapping("/vencidos")
    public ResponseEntity<List<AlertaLoteResponse>> vencidos() {
        return ResponseEntity.ok(alertaService.getVencidos());
    }
}
