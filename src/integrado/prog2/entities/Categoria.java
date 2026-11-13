package integrado.prog2.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Categoria del catalogo. Se persiste en la BASE DE DATOS (tabla categoria).
 * Relacion 1:N de AGREGACION con Producto: la categoria conoce sus productos,
 * pero el producto NO conoce su categoria (relacion unidireccional del UML).
 */
public class Categoria extends Base {

    private String nombre;
    private String descripcion;
    private final List<Producto> productos = new ArrayList<>();

    public Categoria() {
        super();
    }

    public Categoria(String nombre, String descripcion) {
        super();
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    /** Constructor usado por el DAO al reconstruir el objeto desde un ResultSet. */
    public Categoria(Long id, String nombre, String descripcion, boolean eliminado, LocalDateTime createdAt) {
        super(id, eliminado, createdAt);
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /** Devuelve una vista de solo lectura: la lista se modifica con los metodos de la clase. */
    public List<Producto> getProductos() {
        return Collections.unmodifiableList(productos);
    }

    public void addProducto(Producto producto) {
        if (producto != null && !productos.contains(producto)) {
            productos.add(producto);
        }
    }

    /** Reemplaza la lista de la agregacion (se usa al cargarla desde la base). */
    public void setProductos(List<Producto> nuevos) {
        productos.clear();
        if (nuevos != null) {
            nuevos.forEach(this::addProducto);
        }
    }

    @Override
    public String toString() {
        return String.format("[%d] %-20s | %s", getId(), nombre, descripcion);
    }
}
