package integrado.prog2.entities;

import java.time.LocalDateTime;

/**
 * Producto del catalogo. Se persiste en la BASE DE DATOS (tabla producto).
 *
 * IMPORTANTE: no tiene atributo "categoria" ni "categoriaId". La columna
 * categoria_id existe solo en la base (la conoce unicamente el DAO), porque
 * en el UML la relacion es unidireccional Categoria -> Producto.
 */
public class Producto extends Base {

    private String nombre;
    private Double precio;
    private String descripcion;
    private int stock;
    private String imagen;
    private Boolean disponible;

    public Producto() {
        super();
    }

    public Producto(String nombre, Double precio, String descripcion, int stock, String imagen, Boolean disponible) {
        super();
        this.nombre = nombre;
        this.precio = precio;
        this.descripcion = descripcion;
        this.stock = stock;
        this.imagen = imagen;
        this.disponible = disponible;
    }

    /** Constructor usado por el DAO al reconstruir el objeto desde un ResultSet. */
    public Producto(Long id, String nombre, Double precio, String descripcion, int stock, String imagen,
                    Boolean disponible, boolean eliminado, LocalDateTime createdAt) {
        super(id, eliminado, createdAt);
        this.nombre = nombre;
        this.precio = precio;
        this.descripcion = descripcion;
        this.stock = stock;
        this.imagen = imagen;
        this.disponible = disponible;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public Boolean getDisponible() {
        return disponible;
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }

    @Override
    public String toString() {
        return String.format("[%d] %-25s | $ %10.2f | stock: %4d | %s",
                getId(), nombre, precio, stock,
                Boolean.TRUE.equals(disponible) ? "disponible" : "NO disponible");
    }
}
