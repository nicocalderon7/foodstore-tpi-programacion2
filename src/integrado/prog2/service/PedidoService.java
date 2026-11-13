package integrado.prog2.service;

import integrado.prog2.dao.ProductoDAO;
import integrado.prog2.entities.DetallePedido;
import integrado.prog2.entities.Pedido;
import integrado.prog2.entities.Producto;
import integrado.prog2.entities.Usuario;
import integrado.prog2.enums.Estado;
import integrado.prog2.enums.FormaPago;
import integrado.prog2.exception.EntidadNoEncontradaException;
import integrado.prog2.exception.FoodStoreException;
import integrado.prog2.exception.ReglaNegocioException;
import integrado.prog2.exception.StockInvalidoException;
import integrado.prog2.exception.ValidacionException;
import integrado.prog2.repository.PedidoConUsuario;
import integrado.prog2.repository.PedidoRepository;
import integrado.prog2.repository.UsuarioRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Casos de uso de Pedido y DetallePedido.
 *
 * Es el punto de contacto entre los dos almacenamientos: el pedido vive en
 * MEMORIA, pero los productos se leen de la BASE DE DATOS y su stock se
 * actualiza alli dentro de una transaccion.
 *
 * Flujo de alta: nuevoPedido() crea un borrador que NO esta registrado;
 * agregarDetalle() le suma lineas; confirmar() descuenta el stock en la base
 * y, solo si eso salio bien, registra el pedido en memoria.
 */
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoDAO productoDAO;

    public PedidoService(PedidoRepository pedidoRepository, UsuarioRepository usuarioRepository,
                         ProductoDAO productoDAO) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoDAO = productoDAO;
    }

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    public List<PedidoConUsuario> listar() {
        return pedidoRepository.buscarActivos();
    }

    /** Filtra por usuario. Admite usuarios dados de baja para consultar su historial. */
    public List<PedidoConUsuario> listarPorUsuario(Long idUsuario) throws EntidadNoEncontradaException {
        boolean existe = usuarioRepository.buscarTodos().stream().anyMatch(u -> u.getId().equals(idUsuario));
        if (!existe) {
            throw new EntidadNoEncontradaException("el usuario", idUsuario);
        }
        return pedidoRepository.buscarActivosPorUsuario(idUsuario);
    }

    public PedidoConUsuario buscar(Long idPedido) throws EntidadNoEncontradaException {
        return pedidoRepository.buscarPorId(idPedido)
                .orElseThrow(() -> new EntidadNoEncontradaException("el pedido", idPedido));
    }

    // ------------------------------------------------------------------
    // Alta de pedido con detalles
    // ------------------------------------------------------------------

    /** Crea un borrador de pedido para un usuario activo. Todavia no se registra. */
    public Pedido nuevoPedido(Long idUsuario, FormaPago formaPago) throws FoodStoreException {
        buscarUsuarioActivo(idUsuario);
        if (formaPago == null) {
            throw new ValidacionException("La forma de pago es obligatoria.");
        }
        return new Pedido(formaPago);
    }

    /**
     * Agrega una linea al borrador. El producto se lee de la base: debe existir,
     * estar disponible y tener stock suficiente (contando lo que ya esta en el pedido).
     */
    public DetallePedido agregarDetalle(Pedido pedido, Long idProducto, int cantidad) throws FoodStoreException {
        if (cantidad <= 0) {
            throw new ValidacionException("La cantidad debe ser mayor a 0.");
        }
        Producto producto = productoDAO.findById(idProducto)
                .orElseThrow(() -> new EntidadNoEncontradaException("el producto", idProducto));
        if (!Boolean.TRUE.equals(producto.getDisponible())) {
            throw new ReglaNegocioException("El producto '" + producto.getNombre() + "' no esta disponible.");
        }
        DetallePedido existente = pedido.findeDetallePedidoByProducto(producto);
        int yaPedido = existente == null ? 0 : existente.getCantidad();
        if (yaPedido + cantidad > producto.getStock()) {
            throw new StockInvalidoException("Stock insuficiente para '" + producto.getNombre()
                    + "': hay " + producto.getStock() + " y el pedido tendria " + (yaPedido + cantidad) + ".");
        }
        double subtotal = cantidad * producto.getPrecio();
        pedido.addDetallePedido(cantidad, subtotal, producto); // metodo obligatorio del UML
        return pedido.findeDetallePedidoByProducto(producto);
    }

    /** Quita (baja logica) la linea de un producto del borrador. */
    public void quitarDetalle(Pedido pedido, Long idProducto) throws EntidadNoEncontradaException {
        Producto producto = pedido.getDetallesActivos().stream()
                .map(DetallePedido::getProducto)
                .filter(p -> p.getId().equals(idProducto))
                .findFirst()
                .orElseThrow(() -> new EntidadNoEncontradaException("el producto dentro del pedido", idProducto));
        pedido.deleteDetallePedidoByProducto(producto);
    }

    /**
     * Confirma el pedido: descuenta el stock en la BASE (una sola transaccion)
     * y, si no hubo errores, lo registra en MEMORIA dentro del usuario.
     * Si falla el descuento, la base hace rollback y el pedido no se registra.
     */
    public Pedido confirmar(Long idUsuario, Pedido pedido) throws FoodStoreException {
        Usuario usuario = buscarUsuarioActivo(idUsuario);
        if (pedido.getDetallesActivos().isEmpty()) {
            throw new ValidacionException("El pedido debe tener al menos un producto.");
        }
        pedido.calcularTotal(); // interfaz Calculable
        productoDAO.descontarStock(cantidadesPorProducto(pedido));
        return pedidoRepository.registrar(usuario, pedido);
    }

    // ------------------------------------------------------------------
    // Modificacion y baja
    // ------------------------------------------------------------------

    /**
     * Cambia estado y/o forma de pago (null = sin cambios). Un pedido TERMINADO o
     * CANCELADO no admite cambios. Al cancelar se repone el stock en la base.
     */
    public Pedido actualizar(Long idPedido, Estado nuevoEstado, FormaPago nuevaFormaPago) throws FoodStoreException {
        Pedido pedido = buscar(idPedido).pedido();
        if (!pedido.isVigente()) {
            throw new ReglaNegocioException("El pedido #" + idPedido + " esta " + pedido.getEstado()
                    + " y ya no admite modificaciones.");
        }
        if (nuevoEstado == Estado.CANCELADO) {
            // Primero la base: si falla, la memoria queda como estaba.
            productoDAO.reponerStock(cantidadesPorProducto(pedido));
        }
        if (nuevoEstado != null) pedido.setEstado(nuevoEstado);
        if (nuevaFormaPago != null) pedido.setFormaPago(nuevaFormaPago);
        return pedido;
    }

    /** Baja logica del pedido y de sus detalles. Si estaba vigente, repone el stock. */
    public void eliminar(Long idPedido) throws FoodStoreException {
        Pedido pedido = buscar(idPedido).pedido();
        if (pedido.isVigente()) {
            productoDAO.reponerStock(cantidadesPorProducto(pedido));
        }
        pedido.setEliminado(true); // marca tambien los detalles (composicion)
    }

    // ------------------------------------------------------------------

    private Usuario buscarUsuarioActivo(Long idUsuario) throws EntidadNoEncontradaException {
        return usuarioRepository.buscarPorId(idUsuario)
                .orElseThrow(() -> new EntidadNoEncontradaException("el usuario", idUsuario));
    }

    private Map<Long, Integer> cantidadesPorProducto(Pedido pedido) {
        Map<Long, Integer> cantidades = new HashMap<>();
        for (DetallePedido d : pedido.getDetallesActivos()) {
            cantidades.merge(d.getProducto().getId(), d.getCantidad(), Integer::sum);
        }
        return cantidades;
    }
}
