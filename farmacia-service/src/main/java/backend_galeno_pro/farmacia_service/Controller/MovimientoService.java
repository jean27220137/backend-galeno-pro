package backend_galeno_pro.farmacia_service.Controller;

import backend_galeno_pro.farmacia_service.Dto.*;
import backend_galeno_pro.farmacia_service.Exception.ResourceNotFoundException;
import backend_galeno_pro.farmacia_service.Exception.StockInsuficienteException;
import backend_galeno_pro.farmacia_service.Model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovimientoService {

    private final MovimientoCabeceraRepository cabeceraRepository;
    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;

    @Transactional
    public MovimientoResponse registrarEntrada(EntradaRequest request, String usuarioId) {
        List<MovimientoDetalle> detalles = new ArrayList<>();

        for (EntradaDetalleRequest det : request.getDetalles()) {
            Producto producto = productoRepository.findById(det.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto no encontrado con id: " + det.getProductoId()));

            if (producto.getEstado() == EstadoProducto.INACTIVO) {
                throw new IllegalArgumentException(
                        "El producto '" + producto.getNombre() + "' está inactivo");
            }

            Lote lote = Lote.builder()
                    .producto(producto)
                    .numeroLote(det.getNumeroLote())
                    .fechaVencimiento(det.getFechaVencimiento())
                    .cantidadInicial(det.getCantidad())
                    .cantidadDisponible(det.getCantidad())
                    .estado(EstadoLote.VIGENTE)
                    .build();
            loteRepository.save(lote);

            producto.setStockActual(producto.getStockActual() + det.getCantidad());
            productoRepository.save(producto);

            detalles.add(MovimientoDetalle.builder()
                    .producto(producto)
                    .lote(lote)
                    .cantidad(det.getCantidad())
                    .precioUnitario(det.getPrecioUnitario())
                    .build());
        }

        MovimientoCabecera cabecera = MovimientoCabecera.builder()
                .tipoMovimiento(TipoMovimiento.ENTRADA)
                .numeroDocumento(request.getNumeroDocumento())
                .observacion(request.getObservacion())
                .usuarioId(usuarioId)
                .detalles(detalles)
                .build();
        detalles.forEach(d -> d.setCabecera(cabecera));

        return toResponse(cabeceraRepository.save(cabecera));
    }

    @Transactional
    public MovimientoResponse registrarSalida(SalidaRequest request, String usuarioId) {
        List<MovimientoDetalle> detalles = new ArrayList<>();

        for (SalidaDetalleRequest det : request.getDetalles()) {
            Producto producto = productoRepository.findById(det.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto no encontrado con id: " + det.getProductoId()));

            if (producto.getEstado() == EstadoProducto.INACTIVO) {
                throw new IllegalArgumentException(
                        "El producto '" + producto.getNombre() + "' está inactivo");
            }

            Lote lote = loteRepository.findById(det.getLoteId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Lote no encontrado con id: " + det.getLoteId()));

            if (lote.getEstado() != EstadoLote.VIGENTE) {
                throw new IllegalArgumentException(
                        "El lote '" + lote.getNumeroLote() + "' no está vigente (estado: " + lote.getEstado() + ")");
            }

            if (det.getCantidad() > lote.getCantidadDisponible()) {
                throw new StockInsuficienteException(
                        "Stock insuficiente en lote '" + lote.getNumeroLote() +
                        "'. Disponible: " + lote.getCantidadDisponible() +
                        ", solicitado: " + det.getCantidad());
            }

            lote.setCantidadDisponible(lote.getCantidadDisponible() - det.getCantidad());
            if (lote.getCantidadDisponible() == 0) {
                lote.setEstado(EstadoLote.AGOTADO);
            }
            loteRepository.save(lote);

            producto.setStockActual(producto.getStockActual() - det.getCantidad());
            productoRepository.save(producto);

            detalles.add(MovimientoDetalle.builder()
                    .producto(producto)
                    .lote(lote)
                    .cantidad(det.getCantidad())
                    .precioUnitario(det.getPrecioUnitario())
                    .build());
        }

        MovimientoCabecera cabecera = MovimientoCabecera.builder()
                .tipoMovimiento(TipoMovimiento.SALIDA)
                .numeroDocumento(request.getNumeroDocumento())
                .observacion(request.getObservacion())
                .usuarioId(usuarioId)
                .detalles(detalles)
                .build();
        detalles.forEach(d -> d.setCabecera(cabecera));

        return toResponse(cabeceraRepository.save(cabecera));
    }

    @Transactional(readOnly = true)
    public Page<MovimientoResponse> historial(TipoMovimiento tipo, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fecha").descending());
        Page<MovimientoCabecera> cabeceras = (tipo != null)
                ? cabeceraRepository.findByTipoMovimiento(tipo, pageable)
                : cabeceraRepository.findAll(pageable);
        return cabeceras.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public MovimientoResponse obtener(Integer id) {
        MovimientoCabecera cabecera = cabeceraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado con id: " + id));
        return toResponse(cabecera);
    }

    private MovimientoResponse toResponse(MovimientoCabecera c) {
        List<MovimientoDetalleResponse> detalleResponses = c.getDetalles().stream()
                .map(d -> MovimientoDetalleResponse.builder()
                        .id(d.getId())
                        .productoId(d.getProducto().getId())
                        .nombreProducto(d.getProducto().getNombre())
                        .loteId(d.getLote().getId())
                        .numeroLote(d.getLote().getNumeroLote())
                        .cantidad(d.getCantidad())
                        .precioUnitario(d.getPrecioUnitario())
                        .build())
                .collect(Collectors.toList());

        return MovimientoResponse.builder()
                .id(c.getId())
                .tipoMovimiento(c.getTipoMovimiento())
                .fecha(c.getFecha())
                .numeroDocumento(c.getNumeroDocumento())
                .observacion(c.getObservacion())
                .usuarioId(c.getUsuarioId())
                .detalles(detalleResponses)
                .build();
    }
}
