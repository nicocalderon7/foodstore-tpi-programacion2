package integrado.prog2.entities;

/**
 * Linea de un pedido. Vive en MEMORIA como parte (composicion) de un Pedido.
 * Relacion N:1 de asociacion unidireccional con Producto: el detalle conoce
 * su producto, pero el producto NO conoce los detalles en los que aparece.
 */
public class DetallePedido extends Base {

    private int cantidad;
    private Double subtotal;
    private Producto producto;

    public DetallePedido() {
        super();
    }

    public DetallePedido(int cantidad, Double subtotal, Producto producto) {
        super();
        this.cantidad = cantidad;
        this.subtotal = subtotal;
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    @Override
    public String toString() {
        return String.format("   - %3d x %-25s $ %10.2f", cantidad, producto.getNombre(), subtotal);
    }
}
