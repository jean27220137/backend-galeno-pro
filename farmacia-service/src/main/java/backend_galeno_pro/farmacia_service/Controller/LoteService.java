package backend_galeno_pro.farmacia_service.Controller;

import backend_galeno_pro.farmacia_service.Dto.LoteResponse;
import backend_galeno_pro.farmacia_service.Exception.ResourceNotFoundException;
import backend_galeno_pro.farmacia_service.Model.Lote;
import backend_galeno_pro.farmacia_service.Model.LoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoteService {

    private final LoteRepository loteRepository;

    @Transactional(readOnly = true)
    public List<LoteResponse> listar(Integer productoId) {
        List<Lote> lotes = (productoId != null)
                ? loteRepository.findByProductoId(productoId)
                : loteRepository.findAll();
        return lotes.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LoteResponse obtener(Integer id) {
        Lote lote = loteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado con id: " + id));
        return toResponse(lote);
    }

    public LoteResponse toResponse(Lote l) {
        return LoteResponse.builder()
                .id(l.getId())
                .productoId(l.getProducto().getId())
                .nombreProducto(l.getProducto().getNombre())
                .numeroLote(l.getNumeroLote())
                .fechaVencimiento(l.getFechaVencimiento())
                .cantidadInicial(l.getCantidadInicial())
                .cantidadDisponible(l.getCantidadDisponible())
                .estado(l.getEstado())
                .build();
    }
}
