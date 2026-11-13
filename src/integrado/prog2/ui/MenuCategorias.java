package integrado.prog2.ui;

import integrado.prog2.entities.Categoria;
import integrado.prog2.exception.FoodStoreException;
import integrado.prog2.service.CategoriaService;

/** Epica 1 - Gestion de Categorias [BASE DE DATOS]. */
public class MenuCategorias extends Menu {

    private final CategoriaService categoriaService;

    public MenuCategorias(Consola consola, CategoriaService categoriaService) {
        super(consola);
        this.categoriaService = categoriaService;
    }

    @Override
    public void mostrar() {
        int opcion;
        do {
            titulo("CATEGORIAS (BASE DE DATOS)");
            System.out.println("1) Listar        3) Editar");
            System.out.println("2) Crear         4) Eliminar (baja logica)");
            System.out.println("0) Volver al menu principal");
            opcion = consola.leerOpcion("Seleccione: ", 0, 4);
            switch (opcion) {
                case 1 -> ejecutar(this::listar);
                case 2 -> ejecutar(this::crear);
                case 3 -> ejecutar(this::editar);
                case 4 -> ejecutar(this::eliminar);
                default -> { }
            }
        } while (opcion != 0);
    }

    /** HU-CAT-01. Devuelve false si no hay categorias. */
    boolean listar() throws FoodStoreException {
        titulo("Categorias activas");
        return imprimir(categoriaService.listar(), "No hay categorias cargadas.");
    }

    /** HU-CAT-02 */
    private void crear() throws FoodStoreException {
        titulo("Nueva categoria");
        String nombre = consola.leerTexto("  Nombre: ");
        String descripcion = consola.leerTexto("  Descripcion: ");
        Categoria c = categoriaService.crear(nombre, descripcion);
        exito("Categoria guardada en la base de datos con id " + c.getId() + ".");
    }

    /** HU-CAT-03 */
    private void editar() throws FoodStoreException {
        if (!listar()) return;
        Long id = consola.leerId("Id de la categoria a editar: ");
        Categoria c = categoriaService.buscar(id);
        String nombre = consola.leerTexto(actual("Nombre", c.getNombre()));
        String descripcion = consola.leerTexto(actual("Descripcion", c.getDescripcion()));
        categoriaService.editar(id, nombre, descripcion);
        exito("Categoria " + id + " actualizada en la base de datos.");
    }

    /** HU-CAT-04 */
    private void eliminar() throws FoodStoreException {
        if (!listar()) return;
        Long id = consola.leerId("Id de la categoria a eliminar: ");
        Categoria c = categoriaService.buscar(id);
        if (!consola.confirmar("Confirma la baja de la categoria '" + c.getNombre() + "'?")) {
            info("Operacion cancelada.");
            return;
        }
        categoriaService.eliminar(id);
        exito("Categoria " + id + " eliminada (baja logica).");
    }
}
