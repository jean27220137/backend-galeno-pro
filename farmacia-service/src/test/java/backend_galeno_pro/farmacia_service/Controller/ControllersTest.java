package backend_galeno_pro.farmacia_service.Controller;

import backend_galeno_pro.farmacia_service.Dto.*;
import backend_galeno_pro.farmacia_service.Model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ControllersTest {

    // ── CategoriaController ───────────────────────────────────────
    @Mock CategoriaService categoriaService;
    @InjectMocks CategoriaController categoriaController;

    @Test
    void categoria_listar_delegates() {
        when(categoriaService.listar()).thenReturn(List.of());
        ResponseEntity<List<CategoriaResponse>> r = categoriaController.listar();
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void categoria_crear_returns201() {
        CategoriaResponse resp = CategoriaResponse.builder().id(1).nombre("A").build();
        when(categoriaService.crear(any())).thenReturn(resp);
        ResponseEntity<CategoriaResponse> r = categoriaController.crear(new CategoriaRequest("A", null));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void categoria_actualizar_returns200() {
        CategoriaResponse resp = CategoriaResponse.builder().id(1).nombre("B").build();
        when(categoriaService.actualizar(eq(1), any())).thenReturn(resp);
        ResponseEntity<CategoriaResponse> r = categoriaController.actualizar(1, new CategoriaRequest("B", null));
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void categoria_desactivar_returns204() {
        doNothing().when(categoriaService).desactivar(1);
        ResponseEntity<Void> r = categoriaController.desactivar(1);
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    // ── ProductoController ────────────────────────────────────────
    @Mock ProductoService productoService;
    @InjectMocks ProductoController productoController;

    @Test
    void producto_listar_returns200() {
        when(productoService.listar(any(), eq(0), eq(10))).thenReturn(new PageImpl<>(List.of()));
        assertThat(productoController.listar(null, 0, 10).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void producto_obtener_returns200() {
        ProductoResponse resp = ProductoResponse.builder().id(1).nombre("P").build();
        when(productoService.obtener(1)).thenReturn(resp);
        assertThat(productoController.obtener(1).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void producto_crear_returns201() {
        ProductoResponse resp = ProductoResponse.builder().id(1).nombre("P").build();
        when(productoService.crear(any())).thenReturn(resp);
        ProductoRequest req = new ProductoRequest();
        req.setCodigo("C"); req.setNombre("P"); req.setPrincipioActivo("P");
        req.setUnidadMedida("U"); req.setPrecioUnitario(BigDecimal.ONE); req.setCategoriaId(1);
        assertThat(productoController.crear(req).getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void producto_actualizar_returns200() {
        ProductoResponse resp = ProductoResponse.builder().id(1).nombre("P").build();
        when(productoService.actualizar(eq(1), any())).thenReturn(resp);
        ProductoRequest req = new ProductoRequest();
        req.setCodigo("C"); req.setNombre("P"); req.setPrincipioActivo("P");
        req.setUnidadMedida("U"); req.setPrecioUnitario(BigDecimal.ONE); req.setCategoriaId(1);
        assertThat(productoController.actualizar(1, req).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void producto_cambiarEstado_returns204() {
        doNothing().when(productoService).cambiarEstado(1, EstadoProducto.INACTIVO);
        assertThat(productoController.cambiarEstado(1, EstadoProducto.INACTIVO).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
    }

    // ── LoteController ────────────────────────────────────────────
    @Mock LoteService loteService;
    @InjectMocks LoteController loteController;

    @Test
    void lote_listar_returns200() {
        when(loteService.listar(null)).thenReturn(List.of());
        assertThat(loteController.listar(null).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void lote_obtener_returns200() {
        LoteResponse resp = LoteResponse.builder().id(1).numeroLote("L-001")
                .fechaVencimiento(LocalDate.now()).build();
        when(loteService.obtener(1)).thenReturn(resp);
        assertThat(loteController.obtener(1).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ── MovimientoController ──────────────────────────────────────
    @Mock MovimientoService movimientoService;
    @InjectMocks MovimientoController movimientoController;

    @Test
    void movimiento_registrarEntrada_returns201() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin");
        MovimientoResponse resp = buildMovimientoResponse(TipoMovimiento.ENTRADA);
        when(movimientoService.registrarEntrada(any(), eq("admin"))).thenReturn(resp);
        EntradaRequest req = new EntradaRequest();
        req.setDetalles(List.of());
        assertThat(movimientoController.registrarEntrada(req, auth).getStatusCode())
                .isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void movimiento_registrarSalida_returns201() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin");
        MovimientoResponse resp = buildMovimientoResponse(TipoMovimiento.SALIDA);
        when(movimientoService.registrarSalida(any(), eq("admin"))).thenReturn(resp);
        SalidaRequest req = new SalidaRequest();
        req.setDetalles(List.of());
        assertThat(movimientoController.registrarSalida(req, auth).getStatusCode())
                .isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void movimiento_historial_returns200() {
        when(movimientoService.historial(any(), eq(0), eq(10)))
                .thenReturn(new PageImpl<>(List.of()));
        assertThat(movimientoController.historial(null, 0, 10).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    void movimiento_obtener_returns200() {
        when(movimientoService.obtener(1)).thenReturn(buildMovimientoResponse(TipoMovimiento.ENTRADA));
        assertThat(movimientoController.obtener(1).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ── AlertaController ──────────────────────────────────────────
    @Mock AlertaService alertaService;
    @InjectMocks AlertaController alertaController;

    @Test
    void alerta_stockBajo_returns200() {
        when(alertaService.getStockBajo()).thenReturn(List.of());
        assertThat(alertaController.stockBajo().getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void alerta_porVencer_returns200() {
        when(alertaService.getPorVencer()).thenReturn(List.of());
        assertThat(alertaController.porVencer().getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void alerta_vencidos_returns200() {
        when(alertaService.getVencidos()).thenReturn(List.of());
        assertThat(alertaController.vencidos().getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ── helper ────────────────────────────────────────────────────

    private MovimientoResponse buildMovimientoResponse(TipoMovimiento tipo) {
        return MovimientoResponse.builder()
                .id(1).tipoMovimiento(tipo)
                .fecha(LocalDateTime.now())
                .usuarioId("admin")
                .detalles(List.of()).build();
    }
}
