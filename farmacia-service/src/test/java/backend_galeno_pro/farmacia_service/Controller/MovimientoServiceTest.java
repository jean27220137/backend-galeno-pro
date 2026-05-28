package backend_galeno_pro.farmacia_service.Controller;

import backend_galeno_pro.farmacia_service.Dto.*;
import backend_galeno_pro.farmacia_service.Exception.ResourceNotFoundException;
import backend_galeno_pro.farmacia_service.Exception.StockInsuficienteException;
import backend_galeno_pro.farmacia_service.Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovimientoServiceTest {

    @Mock private MovimientoCabeceraRepository cabeceraRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private LoteRepository loteRepository;
    @InjectMocks private MovimientoService movimientoService;

    private Producto productoActivo;
    private Producto productoInactivo;
    private Lote loteVigente;

    @BeforeEach
    void setUp() {
        productoActivo = Producto.builder()
                .id(1).codigo("MED-001").nombre("Paracetamol")
                .stockActual(100).stockMinimo(10)
                .estado(EstadoProducto.ACTIVO).build();

        productoInactivo = Producto.builder()
                .id(2).codigo("MED-002").nombre("Ibuprofeno")
                .stockActual(50).stockMinimo(10)
                .estado(EstadoProducto.INACTIVO).build();

        loteVigente = Lote.builder()
                .id(10).producto(productoActivo)
                .numeroLote("L-001")
                .fechaVencimiento(LocalDate.of(2027, 12, 31))
                .cantidadInicial(100).cantidadDisponible(80)
                .estado(EstadoLote.VIGENTE).build();
    }

    private EntradaRequest buildEntrada(Integer productoId) {
        EntradaDetalleRequest det = new EntradaDetalleRequest();
        det.setProductoId(productoId);
        det.setNumeroLote("L-NEW");
        det.setFechaVencimiento(LocalDate.of(2027, 1, 1));
        det.setCantidad(50);
        det.setPrecioUnitario(BigDecimal.valueOf(0.50));
        EntradaRequest req = new EntradaRequest();
        req.setNumeroDocumento("FAC-001");
        req.setObservacion("obs");
        req.setDetalles(List.of(det));
        return req;
    }

    private SalidaRequest buildSalida(Integer productoId, Integer loteId, Integer cantidad) {
        SalidaDetalleRequest det = new SalidaDetalleRequest();
        det.setProductoId(productoId);
        det.setLoteId(loteId);
        det.setCantidad(cantidad);
        det.setPrecioUnitario(BigDecimal.valueOf(0.50));
        SalidaRequest req = new SalidaRequest();
        req.setDetalles(List.of(det));
        return req;
    }

    private MovimientoCabecera buildCabecera(TipoMovimiento tipo) {
        MovimientoDetalle det = MovimientoDetalle.builder()
                .id(1).producto(productoActivo).lote(loteVigente)
                .cantidad(10).precioUnitario(BigDecimal.ONE).build();
        return MovimientoCabecera.builder()
                .id(100).tipoMovimiento(tipo)
                .fecha(LocalDateTime.now())
                .usuarioId("testuser")
                .detalles(List.of(det)).build();
    }

    // ── registrarEntrada ──────────────────────────────────────────

    @Test
    void registrarEntrada_happyPath_creaLoteAumentaStockYRetornaResponse() {
        when(productoRepository.findById(1)).thenReturn(Optional.of(productoActivo));
        when(loteRepository.save(any(Lote.class))).thenReturn(loteVigente);
        when(productoRepository.save(any(Producto.class))).thenReturn(productoActivo);
        when(cabeceraRepository.save(any(MovimientoCabecera.class)))
                .thenAnswer(inv -> {
                    MovimientoCabecera c = inv.getArgument(0);
                    c.setId(100);
                    return c;
                });

        MovimientoResponse result = movimientoService.registrarEntrada(buildEntrada(1), "admin");

        assertThat(result.getTipoMovimiento()).isEqualTo(TipoMovimiento.ENTRADA);
        assertThat(productoActivo.getStockActual()).isEqualTo(150);
        verify(loteRepository).save(any(Lote.class));
        verify(productoRepository, atLeastOnce()).save(productoActivo);
    }

    @Test
    void registrarEntrada_productoNoEncontrado_throwsResourceNotFoundException() {
        when(productoRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                movimientoService.registrarEntrada(buildEntrada(99), "admin"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void registrarEntrada_productoInactivo_throwsIllegalArgumentException() {
        when(productoRepository.findById(2)).thenReturn(Optional.of(productoInactivo));

        assertThatThrownBy(() ->
                movimientoService.registrarEntrada(buildEntrada(2), "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inactivo");
    }

    // ── registrarSalida ───────────────────────────────────────────

    @Test
    void registrarSalida_happyPath_decreaseStockAndLote() {
        when(productoRepository.findById(1)).thenReturn(Optional.of(productoActivo));
        when(loteRepository.findById(10)).thenReturn(Optional.of(loteVigente));
        when(loteRepository.save(any(Lote.class))).thenReturn(loteVigente);
        when(productoRepository.save(any(Producto.class))).thenReturn(productoActivo);
        when(cabeceraRepository.save(any(MovimientoCabecera.class)))
                .thenAnswer(inv -> {
                    MovimientoCabecera c = inv.getArgument(0);
                    c.setId(101);
                    return c;
                });

        movimientoService.registrarSalida(buildSalida(1, 10, 30), "admin");

        assertThat(loteVigente.getCantidadDisponible()).isEqualTo(50);
        assertThat(productoActivo.getStockActual()).isEqualTo(70);
    }

    @Test
    void registrarSalida_cantidadExacta_setsLoteAgotado() {
        loteVigente.setCantidadDisponible(30);
        when(productoRepository.findById(1)).thenReturn(Optional.of(productoActivo));
        when(loteRepository.findById(10)).thenReturn(Optional.of(loteVigente));
        when(loteRepository.save(any())).thenReturn(loteVigente);
        when(productoRepository.save(any())).thenReturn(productoActivo);
        when(cabeceraRepository.save(any())).thenAnswer(inv -> {
            MovimientoCabecera c = inv.getArgument(0);
            c.setId(102);
            return c;
        });

        movimientoService.registrarSalida(buildSalida(1, 10, 30), "admin");

        assertThat(loteVigente.getEstado()).isEqualTo(EstadoLote.AGOTADO);
    }

    @Test
    void registrarSalida_productoInactivo_throwsIllegalArgumentException() {
        when(productoRepository.findById(2)).thenReturn(Optional.of(productoInactivo));

        assertThatThrownBy(() ->
                movimientoService.registrarSalida(buildSalida(2, 10, 5), "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inactivo");
    }

    @Test
    void registrarSalida_loteNoVigente_throwsIllegalArgumentException() {
        loteVigente.setEstado(EstadoLote.AGOTADO);
        when(productoRepository.findById(1)).thenReturn(Optional.of(productoActivo));
        when(loteRepository.findById(10)).thenReturn(Optional.of(loteVigente));

        assertThatThrownBy(() ->
                movimientoService.registrarSalida(buildSalida(1, 10, 5), "admin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vigente");
    }

    @Test
    void registrarSalida_stockInsuficiente_throwsStockInsuficienteException() {
        when(productoRepository.findById(1)).thenReturn(Optional.of(productoActivo));
        when(loteRepository.findById(10)).thenReturn(Optional.of(loteVigente));

        assertThatThrownBy(() ->
                movimientoService.registrarSalida(buildSalida(1, 10, 999), "admin"))
                .isInstanceOf(StockInsuficienteException.class)
                .hasMessageContaining("insuficiente");
    }

    @Test
    void registrarSalida_loteNoEncontrado_throwsResourceNotFoundException() {
        when(productoRepository.findById(1)).thenReturn(Optional.of(productoActivo));
        when(loteRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                movimientoService.registrarSalida(buildSalida(1, 99, 5), "admin"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── historial ─────────────────────────────────────────────────

    @Test
    void historial_conTipo_filtraByTipo() {
        Page<MovimientoCabecera> page = new PageImpl<>(List.of(buildCabecera(TipoMovimiento.ENTRADA)));
        when(cabeceraRepository.findByTipoMovimiento(eq(TipoMovimiento.ENTRADA), any(Pageable.class)))
                .thenReturn(page);

        Page<MovimientoResponse> result = movimientoService.historial(TipoMovimiento.ENTRADA, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTipoMovimiento()).isEqualTo(TipoMovimiento.ENTRADA);
        verify(cabeceraRepository).findByTipoMovimiento(eq(TipoMovimiento.ENTRADA), any());
        verify(cabeceraRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void historial_sinTipo_retornaTodos() {
        Page<MovimientoCabecera> page = new PageImpl<>(List.of(buildCabecera(TipoMovimiento.SALIDA)));
        when(cabeceraRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<MovimientoResponse> result = movimientoService.historial(null, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        verify(cabeceraRepository).findAll(any(Pageable.class));
    }

    // ── obtener ───────────────────────────────────────────────────

    @Test
    void obtener_found_returnsResponse() {
        when(cabeceraRepository.findById(100)).thenReturn(Optional.of(buildCabecera(TipoMovimiento.ENTRADA)));

        MovimientoResponse result = movimientoService.obtener(100);

        assertThat(result.getId()).isEqualTo(100);
        assertThat(result.getUsuarioId()).isEqualTo("testuser");
        assertThat(result.getDetalles()).hasSize(1);
    }

    @Test
    void obtener_notFound_throwsResourceNotFoundException() {
        when(cabeceraRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movimientoService.obtener(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
