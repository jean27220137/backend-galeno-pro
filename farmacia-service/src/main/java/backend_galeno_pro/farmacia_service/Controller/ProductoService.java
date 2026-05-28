package backend_galeno_pro.farmacia_service.Controller;

import backend_galeno_pro.farmacia_service.Dto.ProductoRequest;
import backend_galeno_pro.farmacia_service.Dto.ProductoResponse;
import backend_galeno_pro.farmacia_service.Exception.ResourceNotFoundException;
import backend_galeno_pro.farmacia_service.Model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final CategoriaService categoriaService;

    @Transactional(readOnly = true)
    public Page<ProductoResponse> listar(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (search != null && !search.isBlank()) {
            return productoRepository.search(search, EstadoProducto.ACTIVO, pageable)
                    .map(this::toResponse);
        }
        return productoRepository.findByEstado(EstadoProducto.ACTIVO, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));
        return toResponse(producto);
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + request.getCategoriaId()));

        Producto producto = Producto.builder()
                .codigo(request.getCodigo())
                .nombre(request.getNombre())
                .principioActivo(request.getPrincipioActivo())
                .laboratorio(request.getLaboratorio())
                .concentracion(request.getConcentracion())
                .formaFarmaceutica(request.getFormaFarmaceutica())
                .unidadMedida(request.getUnidadMedida())
                .precioUnitario(request.getPrecioUnitario())
                .stockMinimo(request.getStockMinimo() != null ? request.getStockMinimo() : 10)
                .categoria(categoria)
                .build();

        return toResponse(productoRepository.save(producto));
    }

    @Transactional
    public ProductoResponse actualizar(Integer id, ProductoRequest request) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));
        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + request.getCategoriaId()));

        producto.setCodigo(request.getCodigo());
        producto.setNombre(request.getNombre());
        producto.setPrincipioActivo(request.getPrincipioActivo());
        producto.setLaboratorio(request.getLaboratorio());
        producto.setConcentracion(request.getConcentracion());
        producto.setFormaFarmaceutica(request.getFormaFarmaceutica());
        producto.setUnidadMedida(request.getUnidadMedida());
        producto.setPrecioUnitario(request.getPrecioUnitario());
        if (request.getStockMinimo() != null) {
            producto.setStockMinimo(request.getStockMinimo());
        }
        producto.setCategoria(categoria);

        return toResponse(productoRepository.save(producto));
    }

    @Transactional
    public void cambiarEstado(Integer id, EstadoProducto estado) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));
        producto.setEstado(estado);
        productoRepository.save(producto);
    }

    public ProductoResponse toResponse(Producto p) {
        return ProductoResponse.builder()
                .id(p.getId())
                .codigo(p.getCodigo())
                .nombre(p.getNombre())
                .principioActivo(p.getPrincipioActivo())
                .laboratorio(p.getLaboratorio())
                .concentracion(p.getConcentracion())
                .formaFarmaceutica(p.getFormaFarmaceutica())
                .unidadMedida(p.getUnidadMedida())
                .precioUnitario(p.getPrecioUnitario())
                .stockActual(p.getStockActual())
                .stockMinimo(p.getStockMinimo())
                .estado(p.getEstado())
                .categoria(categoriaService.toResponse(p.getCategoria()))
                .build();
    }
}
