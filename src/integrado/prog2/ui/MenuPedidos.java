package integrado.prog2.ui;

import integrado.prog2.entities.DetallePedido;
import integrado.prog2.entities.Pedido;
import integrado.prog2.entities.Producto;
import integrado.prog2.entities.Usuario;
import integrado.prog2.enums.Estado;
import integrado.prog2.enums.FormaPago;
import integrado.prog2.exception.FoodStoreException;
import integrado.prog2.repository.PedidoConUsuario;
import integrado.prog2.service.PedidoService;
import integrado.prog2.service.ProductoService;
import integrado.prog2.service.UsuarioService;

import java.util.List;

/** Epica 4 - Gestion de Pedidos y Detalles [MEMORIA; el stock se actualiza en la BASE]. */
public class MenuPedidos extends Menu {

    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;
    private final ProductoService productoService;

    public MenuPedidos(Consola consola, PedidoService pedidoService, UsuarioService usuarioService,
                       ProductoService productoService) {
        super(consola);
        this.pedidoService = pedidoService;
        this.usuarioService = usuarioService;
        this.productoService = productoService;
    }

    @Override
    public void mostrar() {
        int opcion;
        do {
            titulo("PEDIDOS Y DETALLES (MEMORIA)");
            System.out.println("Los pedidos se pierden al cerrar el programa; el stock se descuenta en la base.");
            System.out.println("1) Listar todos                4) Actualizar estado / forma de pago");
            System.out.println("2) Listar por usuario          5) Eliminar (baja logica)");
            System.out.println("3) Crear pedido");
            System.out.println("0) Volver al menu principal");
            opcion = consola.leerOpcion("Seleccione: ", 0, 5);
            switch (opcion) {
                case 1 -> ejecutar(this::listar);
                case 2 -> ejecutar(this::listarPorUsuario);
                case 3 -> ejecutar(this::crear);
                case 4 -> ejecutar(this::actualizar);
                case 5 -> ejecutar(this::eliminar);
                default -> { }
            }
        } while (opcion != 0);
    }

    // ------------------------------------------------------------------
    // HU-PED-01: listados
    // ------------------------------------------------------------------

    private boolean listar() {
        titulo("Pedidos activos (memoria)");
        return imprimirPedidos(pedidoService.listar());
    }

    private void listarPorUsuario() throws FoodStoreException {
        if (!listarUsuarios()) return;
        Long id = consola.leerId("Id del usuario: ");
        titulo("Pedidos del usuario " + id);
        imprimirPedidos(pedidoService.listarPorUsuario(id));
    }

    private boolean imprimirPedidos(List<PedidoConUsuario> pedidos) {
        if (pedidos.isEmpty()) {
            info("No hay pedidos cargados en memoria.");
            return false;
        }
        for (PedidoConUsuario pu : pedidos) {
            Usuario u = pu.usuario();
            System.out.println(pu.pedido() + " | Usuario: " + u.getNombreCompleto()
                    + (u.isEliminado() ? " (usuario dado de baja)" : ""));
            pu.pedido().getDetallesActivos().forEach(System.out::println);
        }
        return true;
    }

    private boolean listarUsuarios() {
        titulo("Usuarios activos");
        return imprimir(usuarioService.listar(), "No hay usuarios cargados en memoria. Cree un usuario primero.");
    }

    // ------------------------------------------------------------------
    // HU-PED-02: alta de pedido con detalles
    // ------------------------------------------------------------------

    private void crear() throws FoodStoreException {
        if (!listarUsuarios()) return;
        Long idUsuario = consola.leerId("Id del usuario que realiza el pedido: ");
        Usuario usuario = usuarioService.buscar(idUsuario);
        FormaPago formaPago = consola.elegirEnum("Forma de pago:", FormaPago.values(), false);
        Pedido pedido = pedidoService.nuevoPedido(idUsuario, formaPago);
        info("Pedido en armado para " + usuario.getNombreCompleto()
                + ". No se registra nada hasta confirmarlo.");

        while (true) {
            titulo("ARMADO DEL PEDIDO - " + pedido.getDetallesActivos().size() + " linea(s) - Total $ "
                    + String.format("%.2f", pedido.getTotal()));
            System.out.println("1) Agregar producto      3) Ver resumen");
            System.out.println("2) Quitar producto       4) Confirmar pedido");
            System.out.println("0) Cancelar pedido");
            int opcion = consola.leerOpcion("Seleccione: ", 0, 4);
            switch (opcion) {
                case 1 -> {
                    if (!agregarLinea(pedido)) {
                        info("Pedido cancelado. No se registro nada.");
                        return;
                    }
                }
                case 2 -> ejecutar(() -> quitarLinea(pedido));
                case 3 -> resumen(pedido);
                case 4 -> {
                    if (confirmar(idUsuario, pedido)) return;
                }
                default -> {
                    if (consola.confirmar("Descartar el pedido en armado?")) {
                        info("Pedido cancelado. No se registro nada.");
                        return;
                    }
                }
            }
        }
    }

    /**
     * Agrega una linea. Si falla, informa el error y pregunta si se sigue armando
     * el pedido o se cancela.
     *
     * @return false si el usuario decide cancelar el pedido
     */
    private boolean agregarLinea(Pedido pedido) {
        try {
            titulo("Productos (base de datos)");
            List<Producto> productos = productoService.listar();
            if (!imprimir(productos, "No hay productos cargados en la base.")) {
                return true;
            }
            Long idProducto = consola.leerId("Id del producto: ");
            int cantidad = consola.leerEntero("Cantidad: ");
            DetallePedido d = pedidoService.agregarDetalle(pedido, idProducto, cantidad);
            exito("Linea agregada:" + System.lineSeparator() + d);
            return true;
        } catch (FoodStoreException e) {
            error(e.getMessage());
            return consola.confirmar("Desea seguir armando el pedido?");
        }
    }

    private void quitarLinea(Pedido pedido) throws FoodStoreException {
        if (pedido.getDetallesActivos().isEmpty()) {
            info("El pedido no tiene productos.");
            return;
        }
        resumen(pedido);
        Long idProducto = consola.leerId("Id del producto a quitar: ");
        pedidoService.quitarDetalle(pedido, idProducto);
        exito("Producto quitado del pedido.");
    }

    private void resumen(Pedido pedido) {
        titulo("Resumen del pedido");
        System.out.println("Fecha: " + pedido.getFecha() + " | Forma de pago: " + pedido.getFormaPago());
        if (pedido.getDetallesActivos().isEmpty()) {
            info("Todavia no tiene productos.");
            return;
        }
        for (DetallePedido d : pedido.getDetallesActivos()) {
            System.out.println(d + "   (id producto " + d.getProducto().getId() + ")");
        }
        System.out.printf("   TOTAL: $ %.2f%n", pedido.getTotal());
    }

    /** @return true si el pedido termino (registrado o cancelado por error) */
    private boolean confirmar(Long idUsuario, Pedido pedido) {
        if (pedido.getDetallesActivos().isEmpty()) {
            error("Agregue al menos un producto antes de confirmar.");
            return false;
        }
        resumen(pedido);
        if (!consola.confirmar("Confirma el pedido? Se descontara el stock en la base de datos")) {
            return false;
        }
        try {
            Pedido registrado = pedidoService.confirmar(idUsuario, pedido);
            exito("Pedido #" + registrado.getId() + " registrado en memoria. Total: $ "
                    + String.format("%.2f", registrado.getTotal()) + ". Stock actualizado en la base.");
        } catch (FoodStoreException e) {
            error(e.getMessage());
            info("El pedido fue cancelado: no se registro en memoria y la base quedo sin cambios (rollback).");
        }
        return true;
    }

    // ------------------------------------------------------------------
    // HU-PED-03 y HU-PED-04
    // ------------------------------------------------------------------

    private void actualizar() throws FoodStoreException {
        if (!listar()) return;
        Long id = consola.leerId("Id del pedido: ");
        Pedido pedido = pedidoService.buscar(id).pedido();
        if (!pedido.isVigente()) {
            error("El pedido #" + id + " esta " + pedido.getEstado() + " y ya no admite modificaciones.");
            return;
        }
        Estado estado = consola.elegirEnum("Nuevo estado [" + pedido.getEstado() + "]:", Estado.values(), true);
        FormaPago fp = consola.elegirEnum("Nueva forma de pago [" + pedido.getFormaPago() + "]:",
                FormaPago.values(), true);
        if (estado == null && fp == null) {
            info("No se realizaron cambios.");
            return;
        }
        if (estado == Estado.CANCELADO
                && !consola.confirmar("Al cancelar se repone el stock y el pedido no podra modificarse. Continuar?")) {
            info("Operacion cancelada.");
            return;
        }
        pedidoService.actualizar(id, estado, fp);
        exito("Pedido #" + id + " actualizado." + (estado == Estado.CANCELADO ? " Stock repuesto en la base." : ""));
    }

    private void eliminar() throws FoodStoreException {
        if (!listar()) return;
        Long id = consola.leerId("Id del pedido a eliminar: ");
        Pedido pedido = pedidoService.buscar(id).pedido();
        String aviso = pedido.isVigente() ? " Se repondra el stock de sus productos." : "";
        if (!consola.confirmar("Confirma la baja del pedido #" + id + "?" + aviso)) {
            info("Operacion cancelada.");
            return;
        }
        pedidoService.eliminar(id);
        exito("Pedido #" + id + " eliminado (baja logica, incluidos sus detalles).");
    }
}
