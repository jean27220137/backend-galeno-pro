package backend_galeno_pro.farmacia_service.Controller;

import backend_galeno_pro.farmacia_service.Dto.CategoriaRequest;
import backend_galeno_pro.farmacia_service.Dto.CategoriaResponse;
import backend_galeno_pro.farmacia_service.Exception.ResourceNotFoundException;
import backend_galeno_pro.farmacia_service.Model.Categoria;
import backend_galeno_pro.farmacia_service.Model.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock private CategoriaRepository categoriaRepository;
    @InjectMocks private CategoriaService categoriaService;

    private Categoria categoria;

    @BeforeEach
    void setUp() {
        categoria = Categoria.builder()
                .id(1)
                .nombre("Analgésicos")
                .descripcion("Para el dolor")
                .activo(true)
                .build();
    }

    // ── listar ────────────────────────────────────────────────────

    @Test
    void listar_returnsMappedResponses() {
        when(categoriaRepository.findByActivoTrue()).thenReturn(List.of(categoria));

        List<CategoriaResponse> result = categoriaService.listar();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("Analgésicos");
        assertThat(result.get(0).isActivo()).isTrue();
    }

    @Test
    void listar_emptyRepository_returnsEmptyList() {
        when(categoriaRepository.findByActivoTrue()).thenReturn(List.of());

        assertThat(categoriaService.listar()).isEmpty();
    }

    // ── crear ─────────────────────────────────────────────────────

    @Test
    void crear_savesAndReturnsMappedResponse() {
        CategoriaRequest req = new CategoriaRequest("Antibióticos", "Agentes antimicrobianos");
        Categoria saved = Categoria.builder().id(2).nombre("Antibióticos")
                .descripcion("Agentes antimicrobianos").activo(true).build();
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(saved);

        CategoriaResponse result = categoriaService.crear(req);

        assertThat(result.getId()).isEqualTo(2);
        assertThat(result.getNombre()).isEqualTo("Antibióticos");
        verify(categoriaRepository).save(any(Categoria.class));
    }

    // ── actualizar ────────────────────────────────────────────────

    @Test
    void actualizar_found_updatesFieldsAndReturns() {
        CategoriaRequest req = new CategoriaRequest("Actualizado", "Nueva desc");
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.save(categoria)).thenReturn(categoria);

        CategoriaResponse result = categoriaService.actualizar(1, req);

        assertThat(categoria.getNombre()).isEqualTo("Actualizado");
        assertThat(categoria.getDescripcion()).isEqualTo("Nueva desc");
        assertThat(result).isNotNull();
    }

    @Test
    void actualizar_notFound_throwsResourceNotFoundException() {
        when(categoriaRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoriaService.actualizar(99, new CategoriaRequest("x", null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── desactivar ────────────────────────────────────────────────

    @Test
    void desactivar_found_setsActivoFalseAndSaves() {
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));

        categoriaService.desactivar(1);

        assertThat(categoria.isActivo()).isFalse();
        verify(categoriaRepository).save(categoria);
    }

    @Test
    void desactivar_notFound_throwsResourceNotFoundException() {
        when(categoriaRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoriaService.desactivar(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
