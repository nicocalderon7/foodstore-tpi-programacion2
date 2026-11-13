package integrado.prog2.ui;

import integrado.prog2.entities.Categoria;
import integrado.prog2.entities.Producto;
import integrado.prog2.exception.FoodStoreException;
import integrado.prog2.service.CategoriaService;
import integrado.prog2.service.ProductoService;

/** Epica 2 - Gestion de Productos [BASE DE DATOS]. */
public class MenuProductos extends Menu {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;

    public MenuProductos(Consola consola, ProductoService productoService, CategoriaService categoriaService) {
        super(consola);
        this.productoService = productoService;
        this.categoriaService = categoriaService;
    }

    @Override
    public void mostrar() {
        int opcion;
        do {
            titulo("PRODUCTOS (BASE DE DATOS)");
            System.out.println("1) Listar                      4) Editar");
            System.out.println("2) Listar por categoria        5) Cambiar de categoria");
            System.out.println("3) Crear                       6) Eliminar (baja logica)");
            System.out.println("0) Volver al menu principal");
            opcion = consola.leerOpcion("Seleccione: ", 0, 6);
            switch (opcion) {
                case 1 -> ejecutar(this::listar);
                case 2 -> ejecutar(this::listarPorCategoria);
                case 3 -> ejecutar(this::crear);
                case 4 -> ejecutar(this::editar);
                case 5 -> ejecutar(this::cambiarCategoria);
                case 6 -> ejecutar(this::eliminar);
                default -> { }
            }
        } while (opcion != 0);
    }

    /** HU-PROD-01. El listado general no muestra la categoria: el Producto no la conoce. */
    private boolean listar() throws FoodStoreException {
        titulo("Productos activos");
        return imprimir(productoService.listar(), "No hay productos cargados.");
    }

    private boolean listarCategorias() throws FoodStoreException {
        titulo("Categorias activas");
        return imprimir(categoriaService.listar(), "No hay categorias cargadas. Cree una categoria primero.");
    }

    /** HU-PROD-02: navegacion Categoria -> Productos. */
    private void listarPorCategoria() throws FoodStoreException {
        if (!listarCategorias()) return;
        Long id = consola.leerId("Id de la categoria: ");
        Categoria c = categoriaService.buscarConProductos(id);
        titulo("Productos de la categoria '" + c.getNombre() + "'");
        imprimir(c.getProductos(), "La categoria no tiene productos activos.");
    }

    /** HU-PROD-03 */
    private void crear() throws FoodStoreException {
        if (!listarCategorias()) return;
        Long idCategoria = consola.leerId("Id de la categoria del nuevo producto: ");
        categoriaService.buscar(idCategoria); // se valida antes de pedir el resto de los datos
        titulo("Nuevo producto");
        String nombre = consola.leerTexto("  Nombre: ");
        String descripcion = consola.leerTexto("  Descripcion (opcional): ");
        double precio = consola.leerDecimal("  Precio: ");
        int stock = consola.leerEntero("  Stock: ");
        String imagen = consola.leerTexto("  Imagen (nombre de archivo o URL, opcional): ");
        boolean disponible = consola.confirmar("  Disponible para la venta?");
        Producto p = productoService.crear(nombre, descripcion, precio, stock, imagen, disponible, idCategoria);
        exito("Producto guardado en la base de datos con id " + p.getId() + ".");
    }

    /** HU-PROD-04 (atributos propios; la categoria se cambia con la opcion 5). */
    private void editar() throws FoodStoreException {
        if (!listar()) return;
        Long id = consola.leerId("Id del producto a editar: ");
        Producto p = productoService.buscar(id);
        System.out.println("Deje vacio el campo que no quiera modificar.");
        String nombre = consola.leerTexto(actual("Nombre", p.getNombre()));
        String descripcion = consola.leerTexto(actual("Descripcion", p.getDescripcion()));
        Double precio = consola.leerDecimalOpcional(actual("Precio", p.getPrecio()));
        Integer stock = consola.leerEnteroOpcional(actual("Stock", p.getStock()));
        String imagen = consola.leerTexto(actual("Imagen", p.getImagen()));
        Boolean disponible = consola.confirmarOpcional("  Disponible [" + (p.getDisponible() ? "S" : "N") + "]");
        productoService.editar(id, nombre, descripcion, precio, stock, imagen, disponible);
        exito("Producto " + id + " actualizado en la base de datos.");
    }

    /** HU-PROD-04: mover a otra categoria (solo actualiza la clave foranea). */
    private void cambiarCategoria() throws FoodStoreException {
        if (!listar()) return;
        Long idProducto = consola.leerId("Id del producto: ");
        Producto p = productoService.buscar(idProducto);
        if (!listarCategorias()) return;
        Long idCategoria = consola.leerId("Id de la nueva categoria para '" + p.getNombre() + "': ");
        productoService.cambiarCategoria(idProducto, idCategoria);
        exito("Producto " + idProducto + " movido a la categoria " + idCategoria + ".");
    }

    /** HU-PROD-05 */
    private void eliminar() throws FoodStoreException {
        if (!listar()) return;
        Long id = consola.leerId("Id del producto a eliminar: ");
        Producto p = productoService.buscar(id);
        if (!consola.confirmar("Confirma la baja del producto '" + p.getNombre() + "'?")) {
            info("Operacion cancelada.");
            return;
        }
        productoService.eliminar(id);
        exito("Producto " + id + " eliminado (baja logica).");
    }
}
