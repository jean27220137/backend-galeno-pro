package backend_galeno_pro.farmacia_service.Controller;

import backend_galeno_pro.farmacia_service.Dto.CategoriaRequest;
import backend_galeno_pro.farmacia_service.Dto.CategoriaResponse;
import backend_galeno_pro.farmacia_service.Exception.ResourceNotFoundException;
import backend_galeno_pro.farmacia_service.Model.Categoria;
import backend_galeno_pro.farmacia_service.Model.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categoriaRepository.findByActivoTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoriaResponse crear(CategoriaRequest request) {
        Categoria categoria = Categoria.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .build();
        return toResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaResponse actualizar(Integer id, CategoriaRequest request) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));
        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());
        return toResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public void desactivar(Integer id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));
        categoria.setActivo(false);
        categoriaRepository.save(categoria);
    }

    public CategoriaResponse toResponse(Categoria c) {
        return CategoriaResponse.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .descripcion(c.getDescripcion())
                .activo(c.isActivo())
                .build();
    }
}
