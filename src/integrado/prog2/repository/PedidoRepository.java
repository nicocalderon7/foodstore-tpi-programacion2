package integrado.prog2.repository;

import integrado.prog2.entities.DetallePedido;
import integrado.prog2.entities.Pedido;
import integrado.prog2.entities.Usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Pedidos en MEMORIA.
 *
 * No tiene una coleccion propia: los pedidos se guardan dentro de la lista de
 * su Usuario (asociacion unidireccional del UML) y los detalles dentro del
 * Pedido (composicion). Este repositorio genera los ids y resuelve las
 * busquedas recorriendo los usuarios.
 */
public class PedidoRepository {

    private final UsuarioRepository usuarioRepository;
    private long ultimoIdPedido = 0;
    private long ultimoIdDetalle = 0;

    public PedidoRepository(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /** Asigna ids al pedido y a sus detalles y lo agrega a la lista del usuario. */
    public Pedido registrar(Usuario usuario, Pedido pedido) {
        pedido.setId(++ultimoIdPedido);
        for (DetallePedido d : pedido.getDetalles()) {
            d.setId(++ultimoIdDetalle);
        }
        usuario.addPedido(pedido);
        return pedido;
    }

    /**
     * Pedidos no eliminados de todos los usuarios. Incluye los de usuarios dados de
     * baja, porque sus pedidos se conservan como historial.
     */
    public List<PedidoConUsuario> buscarActivos() {
        List<PedidoConUsuario> resultado = new ArrayList<>();
        for (Usuario u : usuarioRepository.buscarTodos()) {
            agregarActivos(u, resultado);
        }
        return resultado;
    }

    /** Pedidos no eliminados de un usuario en particular. */
    public List<PedidoConUsuario> buscarActivosPorUsuario(Long idUsuario) {
        List<PedidoConUsuario> resultado = new ArrayList<>();
        for (Usuario u : usuarioRepository.buscarTodos()) {
            if (u.getId().equals(idUsuario)) {
                agregarActivos(u, resultado);
            }
        }
        return resultado;
    }

    public Optional<PedidoConUsuario> buscarPorId(Long idPedido) {
        return buscarActivos().stream()
                .filter(pu -> pu.pedido().getId().equals(idPedido))
                .findFirst();
    }

    private void agregarActivos(Usuario usuario, List<PedidoConUsuario> destino) {
        for (Pedido p : usuario.getPedidos()) {
            if (!p.isEliminado()) {
                destino.add(new PedidoConUsuario(usuario, p));
            }
        }
    }
}
