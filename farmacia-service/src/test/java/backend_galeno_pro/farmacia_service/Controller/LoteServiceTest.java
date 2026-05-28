package backend_galeno_pro.farmacia_service.Controller;

import backend_galeno_pro.farmacia_service.Dto.LoteResponse;
import backend_galeno_pro.farmacia_service.Exception.ResourceNotFoundException;
import backend_galeno_pro.farmacia_service.Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoteServiceTest {

    @Mock private LoteRepository loteRepository;
    @InjectMocks private LoteService loteService;

    private Producto producto;
    private Lote lote;

    @BeforeEach
    void setUp() {
        producto = Producto.builder().id(1).nombre("Paracetamol").codigo("MED-001").build();
        lote = Lote.builder()
                .id(10)
                .producto(producto)
                .numeroLote("L-001")
                .fechaVencimiento(LocalDate.of(2027, 1, 1))
                .cantidadInicial(200)
                .cantidadDisponible(150)
                .estado(EstadoLote.VIGENTE)
                .build();
    }

    // ── listar ────────────────────────────────────────────────────

    @Test
    void listar_withProductoId_callsFindByProductoId() {
        when(loteRepository.findByProductoId(1)).thenReturn(List.of(lote));

        List<LoteResponse> result = loteService.listar(1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNumeroLote()).isEqualTo("L-001");
        assertThat(result.get(0).getProductoId()).isEqualTo(1);
        verify(loteRepository).findByProductoId(1);
        verify(loteRepository, never()).findAll();
    }

    @Test
    void listar_withoutProductoId_callsFindAll() {
        when(loteRepository.findAll()).thenReturn(List.of(lote));

        List<LoteResponse> result = loteService.listar(null);

        assertThat(result).hasSize(1);
        verify(loteRepository).findAll();
        verify(loteRepository, never()).findByProductoId(any());
    }

    // ── obtener ───────────────────────────────────────────────────

    @Test
    void obtener_found_returnsMappedResponse() {
        when(loteRepository.findById(10)).thenReturn(Optional.of(lote));

        LoteResponse result = loteService.obtener(10);

        assertThat(result.getId()).isEqualTo(10);
        assertThat(result.getNombreProducto()).isEqualTo("Paracetamol");
        assertThat(result.getFechaVencimiento()).isEqualTo(LocalDate.of(2027, 1, 1));
        assertThat(result.getCantidadDisponible()).isEqualTo(150);
        assertThat(result.getEstado()).isEqualTo(EstadoLote.VIGENTE);
    }

    @Test
    void obtener_notFound_throwsResourceNotFoundException() {
        when(loteRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loteService.obtener(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
