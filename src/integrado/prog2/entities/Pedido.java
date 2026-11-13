package integrado.prog2.entities;

import integrado.prog2.enums.Estado;
import integrado.prog2.enums.FormaPago;
import integrado.prog2.interfaces.Calculable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pedido. Vive en MEMORIA, dentro de la lista de pedidos de su Usuario.
 * Relacion de COMPOSICION 1:N con DetallePedido: los detalles solo existen
 * dentro del pedido y se crean a traves de addDetallePedido().
 * Implementa Calculable para obtener su total.
 */
public class Pedido extends Base implements Calculable {

    private LocalDate fecha;
    private Estado estado;
    private Double total;
    private FormaPago formaPago;
    private final List<DetallePedido> detalles = new ArrayList<>();

    public Pedido() {
        super();
        this.fecha = LocalDate.now();
        this.estado = Estado.PENDIENTE;
        this.total = 0.0;
    }

    public Pedido(FormaPago formaPago) {
        this();
        this.formaPago = formaPago;
    }

    // ---------------------------------------------------------------
    // Metodos obligatorios del UML
    // ---------------------------------------------------------------

    /**
     * Agrega una linea al pedido (composicion: el detalle se crea aca adentro).
     * Si el producto ya estaba en el pedido, se acumula la cantidad y el subtotal
     * en la misma linea en lugar de duplicarla.
     *
     * @param cantidad cantidad pedida (debe ser mayor a 0)
     * @param subtotal importe de la linea (cantidad * precio del producto)
     * @param producto producto leido de la base de datos
     */
    public void addDetallePedido(int cantidad, Double subtotal, Producto producto) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
        if (producto == null) {
            throw new IllegalArgumentException("El detalle debe tener un producto");
        }
        DetallePedido existente = findeDetallePedidoByProducto(producto);
        if (existente != null) {
            existente.setCantidad(existente.getCantidad() + cantidad);
            existente.setSubtotal(existente.getSubtotal() + subtotal);
        } else {
            detalles.add(new DetallePedido(cantidad, subtotal, producto));
        }
        calcularTotal();
    }

    /**
     * Busca la linea activa del pedido que corresponde a un producto.
     * (El nombre conserva la grafia exacta del UML: "finde".)
     *
     * @return el detalle, o null si el producto no esta en el pedido
     */
    public DetallePedido findeDetallePedidoByProducto(Producto producto) {
        for (DetallePedido d : detalles) {
            if (!d.isEliminado() && d.getProducto().equals(producto)) {
                return d;
            }
        }
        return null;
    }

    /** Baja logica de la linea correspondiente al producto y recalculo del total. */
    public void deleteDetallePedidoByProducto(Producto producto) {
        DetallePedido d = findeDetallePedidoByProducto(producto);
        if (d != null) {
            d.setEliminado(true);
            calcularTotal();
        }
    }

    /** Implementacion de Calculable: total = suma de subtotales de las lineas activas. */
    @Override
    public void calcularTotal() {
        double suma = 0.0;
        for (DetallePedido d : getDetallesActivos()) {
            suma += d.getSubtotal();
        }
        this.total = suma;
    }

    // ---------------------------------------------------------------
    // Otros metodos
    // ---------------------------------------------------------------

    /** Lineas no eliminadas del pedido. */
    public List<DetallePedido> getDetallesActivos() {
        return detalles.stream().filter(d -> !d.isEliminado()).toList();
    }

    /** Todas las lineas, incluidas las eliminadas (historial). */
    public List<DetallePedido> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }

    /** Baja logica en cascada: al eliminar el pedido se eliminan sus detalles (composicion). */
    @Override
    public void setEliminado(boolean eliminado) {
        super.setEliminado(eliminado);
        if (eliminado) {
            detalles.forEach(d -> d.setEliminado(true));
        }
    }

    /** Un pedido es "vigente" mientras no esta terminado ni cancelado. */
    public boolean isVigente() {
        return estado == Estado.PENDIENTE || estado == Estado.CONFIRMADO;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public Double getTotal() {
        return total;
    }

    public FormaPago getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(FormaPago formaPago) {
        this.formaPago = formaPago;
    }

    @Override
    public String toString() {
        return String.format("Pedido #%s | %s | %-10s | %-13s | Total: $ %.2f",
                getId() == null ? "(nuevo)" : getId(), fecha, estado, formaPago, total);
    }
}
