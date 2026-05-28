package backend_galeno_pro.farmacia_service.Controller;

import backend_galeno_pro.farmacia_service.Dto.AlertaLoteResponse;
import backend_galeno_pro.farmacia_service.Dto.AlertaStockResponse;
import backend_galeno_pro.farmacia_service.Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertaServiceTest {

    @Mock private ProductoRepository productoRepository;
    @Mock private LoteRepository loteRepository;
    @InjectMocks private AlertaService alertaService;

    private Producto productoConStockBajo;
    private Lote lotePorVencer;

    @BeforeEach
    void setUp() {
        productoConStockBajo = Producto.builder()
                .id(1).codigo("MED-001").nombre("Paracetamol")
                .stockActual(5).stockMinimo(20)
                .estado(EstadoProducto.ACTIVO).build();

        Producto productoRef = Producto.builder()
                .id(2).nombre("Amoxicilina").build();

        lotePorVencer = Lote.builder()
                .id(10).producto(productoRef)
                .numeroLote("L-001")
                .fechaVencimiento(LocalDate.now().plusDays(15))
                .cantidadInicial(100).cantidadDisponible(80)
                .estado(EstadoLote.VIGENTE).build();
    }

    // ── getStockBajo ──────────────────────────────────────────────

    @Test
    void getStockBajo_returnsMappedResponsesWithDeficit() {
        when(productoRepository.findStockBajo(EstadoProducto.ACTIVO))
                .thenReturn(List.of(productoConStockBajo));

        List<AlertaStockResponse> result = alertaService.getStockBajo();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCodigo()).isEqualTo("MED-001");
        assertThat(result.get(0).getStockActual()).isEqualTo(5);
        assertThat(result.get(0).getStockMinimo()).isEqualTo(20);
        assertThat(result.get(0).getDeficit()).isEqualTo(15);
    }

    @Test
    void getStockBajo_emptyResult_returnsEmptyList() {
        when(productoRepository.findStockBajo(EstadoProducto.ACTIVO)).thenReturn(List.of());

        assertThat(alertaService.getStockBajo()).isEmpty();
    }

    // ── getPorVencer ──────────────────────────────────────────────

    @Test
    void getPorVencer_callsQueryWithVigenteAndLimite30Dias() {
        when(loteRepository.findPorVencer(eq(EstadoLote.VIGENTE), any(LocalDate.class)))
                .thenReturn(List.of(lotePorVencer));

        List<AlertaLoteResponse> result = alertaService.getPorVencer();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombreProducto()).isEqualTo("Amoxicilina");
        assertThat(result.get(0).getDiasParaVencer()).isPositive();
        verify(loteRepository).findPorVencer(eq(EstadoLote.VIGENTE), any(LocalDate.class));
    }

    // ── getVencidos ───────────────────────────────────────────────

    @Test
    void getVencidos_callsQueryWithFechaHoy() {
        Lote loteVencido = Lote.builder()
                .id(20).producto(productoConStockBajo)
                .numeroLote("L-OLD")
                .fechaVencimiento(LocalDate.now().minusDays(10))
                .cantidadInicial(50).cantidadDisponible(30)
                .estado(EstadoLote.VENCIDO).build();

        when(loteRepository.findVencidos(any(LocalDate.class))).thenReturn(List.of(loteVencido));

        List<AlertaLoteResponse> result = alertaService.getVencidos();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDiasParaVencer()).isNegative();
        verify(loteRepository).findVencidos(any(LocalDate.class));
    }
}
