package backend_galeno_pro.farmacia_service.Controller;

import backend_galeno_pro.farmacia_service.Dto.AlertaLoteResponse;
import backend_galeno_pro.farmacia_service.Dto.AlertaStockResponse;
import backend_galeno_pro.farmacia_service.Model.EstadoLote;
import backend_galeno_pro.farmacia_service.Model.EstadoProducto;
import backend_galeno_pro.farmacia_service.Model.Lote;
import backend_galeno_pro.farmacia_service.Model.LoteRepository;
import backend_galeno_pro.farmacia_service.Model.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlertaService {

    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;

    @Transactional(readOnly = true)
    public List<AlertaStockResponse> getStockBajo() {
        return productoRepository.findStockBajo(EstadoProducto.ACTIVO).stream()
                .map(p -> AlertaStockResponse.builder()
                        .productoId(p.getId())
                        .codigo(p.getCodigo())
                        .nombre(p.getNombre())
                        .stockActual(p.getStockActual())
                        .stockMinimo(p.getStockMinimo())
                        .deficit(p.getStockMinimo() - p.getStockActual())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlertaLoteResponse> getPorVencer() {
        LocalDate limite = LocalDate.now().plusDays(30);
        return loteRepository.findPorVencer(EstadoLote.VIGENTE, limite).stream()
                .map(this::toLoteAlerta)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AlertaLoteResponse> getVencidos() {
        return loteRepository.findVencidos(LocalDate.now()).stream()
                .map(this::toLoteAlerta)
                .collect(Collectors.toList());
    }

    private AlertaLoteResponse toLoteAlerta(Lote l) {
        long diasParaVencer = ChronoUnit.DAYS.between(LocalDate.now(), l.getFechaVencimiento());
        return AlertaLoteResponse.builder()
                .loteId(l.getId())
                .productoId(l.getProducto().getId())
                .nombreProducto(l.getProducto().getNombre())
                .numeroLote(l.getNumeroLote())
                .fechaVencimiento(l.getFechaVencimiento())
                .cantidadDisponible(l.getCantidadDisponible())
                .estado(l.getEstado())
                .diasParaVencer(diasParaVencer)
                .build();
    }
}
