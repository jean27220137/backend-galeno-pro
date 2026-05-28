package backend_galeno_pro.farmacia_service.Controller;

import backend_galeno_pro.farmacia_service.Dto.ProductoRequest;
import backend_galeno_pro.farmacia_service.Dto.ProductoResponse;
import backend_galeno_pro.farmacia_service.Exception.ResourceNotFoundException;
import backend_galeno_pro.farmacia_service.Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock private ProductoRepository productoRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private CategoriaService categoriaService;
    @InjectMocks private ProductoService productoService;

    private Categoria categoria;
    private Producto producto;

    @BeforeEach
    void setUp() {
        categoria = Categoria.builder().id(1).nombre("Analgésicos").activo(true).build();
        producto = Producto.builder()
                .id(1)
                .codigo("MED-001")
                .nombre("Paracetamol")
                .principioActivo("Paracetamol")
                .laboratorio("Lab X")
                .concentracion("500mg")
                .formaFarmaceutica(FormaFarmaceutica.TABLETA)
                .unidadMedida("Unidad")
                .precioUnitario(BigDecimal.valueOf(0.50))
                .stockActual(100)
                .stockMinimo(20)
                .estado(EstadoProducto.ACTIVO)
                .categoria(categoria)
                .build();
    }

    private ProductoRequest buildRequest(Integer stockMinimo) {
        ProductoRequest req = new ProductoRequest();
        req.setCodigo("MED-001");
        req.setNombre("Paracetamol");
        req.setPrincipioActivo("Paracetamol");
        req.setLaboratorio("Lab X");
        req.setConcentracion("500mg");
        req.setFormaFarmaceutica(FormaFarmaceutica.TABLETA);
        req.setUnidadMedida("Unidad");
        req.setPrecioUnitario(BigDecimal.valueOf(0.50));
        req.setStockMinimo(stockMinimo);
        req.setCategoriaId(1);
        return req;
    }

    // ── listar ────────────────────────────────────────────────────

    @Test
    void listar_withoutSearch_returnsActivePage() {
        Page<Producto> page = new PageImpl<>(List.of(producto));
        when(productoRepository.findByEstado(eq(EstadoProducto.ACTIVO), any(Pageable.class)))
                .thenReturn(page);
        when(categoriaService.toResponse(categoria)).thenCallRealMethod();

        Page<ProductoResponse> result = productoService.listar(null, 0, 10);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getCodigo()).isEqualTo("MED-001");
    }

    @Test
    void listar_withSearch_callsSearchQuery() {
        Page<Producto> page = new PageImpl<>(List.of(producto));
        when(productoRepository.search(eq("para"), eq(EstadoProducto.ACTIVO), any(Pageable.class)))
                .thenReturn(page);
        when(categoriaService.toResponse(categoria)).thenCallRealMethod();

        Page<ProductoResponse> result = productoService.listar("para", 0, 10);

        assertThat(result.getContent()).hasSize(1);
        verify(productoRepository).search(eq("para"), eq(EstadoProducto.ACTIVO), any());
        verify(productoRepository, never()).findByEstado(any(), any());
    }

    @Test
    void listar_blankSearch_treatedAsNoSearch() {
        Page<Producto> page = new PageImpl<>(List.of());
        when(productoRepository.findByEstado(eq(EstadoProducto.ACTIVO), any(Pageable.class)))
                .thenReturn(page);

        productoService.listar("   ", 0, 10);

        verify(productoRepository).findByEstado(eq(EstadoProducto.ACTIVO), any());
        verify(productoRepository, never()).search(any(), any(), any());
    }

    // ── obtener ───────────────────────────────────────────────────

    @Test
    void obtener_found_returnsResponse() {
        when(productoRepository.findById(1)).thenReturn(Optional.of(producto));
        when(categoriaService.toResponse(categoria)).thenCallRealMethod();

        ProductoResponse result = productoService.obtener(1);

        assertThat(result.getNombre()).isEqualTo("Paracetamol");
        assertThat(result.getEstado()).isEqualTo(EstadoProducto.ACTIVO);
    }

    @Test
    void obtener_notFound_throwsResourceNotFoundException() {
        when(productoRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.obtener(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── crear ─────────────────────────────────────────────────────

    @Test
    void crear_happyPath_savesAndReturnsResponse() {
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);
        when(categoriaService.toResponse(categoria)).thenCallRealMethod();

        ProductoResponse result = productoService.crear(buildRequest(20));

        assertThat(result.getCodigo()).isEqualTo("MED-001");
        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    void crear_nullStockMinimo_defaultsTo10() {
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));
        when(categoriaService.toResponse(any())).thenCallRealMethod();

        productoService.crear(buildRequest(null));

        verify(productoRepository).save(argThat(p -> p.getStockMinimo() == 10));
    }

    @Test
    void crear_categoriaNotFound_throwsResourceNotFoundException() {
        when(categoriaRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.crear(buildRequest(10)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Categoría");
    }

    // ── actualizar ────────────────────────────────────────────────

    @Test
    void actualizar_happyPath_updatesAndReturnsResponse() {
        ProductoRequest req = buildRequest(30);
        when(productoRepository.findById(1)).thenReturn(Optional.of(producto));
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(productoRepository.save(producto)).thenReturn(producto);
        when(categoriaService.toResponse(categoria)).thenCallRealMethod();

        ProductoResponse result = productoService.actualizar(1, req);

        assertThat(producto.getStockMinimo()).isEqualTo(30);
        assertThat(result).isNotNull();
    }

    @Test
    void actualizar_nullStockMinimo_doesNotChangeExistingValue() {
        ProductoRequest req = buildRequest(null);
        when(productoRepository.findById(1)).thenReturn(Optional.of(producto));
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(productoRepository.save(producto)).thenReturn(producto);
        when(categoriaService.toResponse(any())).thenCallRealMethod();

        productoService.actualizar(1, req);

        assertThat(producto.getStockMinimo()).isEqualTo(20);
    }

    @Test
    void actualizar_productoNotFound_throwsResourceNotFoundException() {
        when(productoRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.actualizar(99, buildRequest(10)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── cambiarEstado ─────────────────────────────────────────────

    @Test
    void cambiarEstado_setsEstadoAndSaves() {
        when(productoRepository.findById(1)).thenReturn(Optional.of(producto));

        productoService.cambiarEstado(1, EstadoProducto.INACTIVO);

        assertThat(producto.getEstado()).isEqualTo(EstadoProducto.INACTIVO);
        verify(productoRepository).save(producto);
    }

    @Test
    void cambiarEstado_notFound_throwsResourceNotFoundException() {
        when(productoRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.cambiarEstado(99, EstadoProducto.INACTIVO))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
