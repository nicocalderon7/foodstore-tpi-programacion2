package integrado.prog2.ui;

import integrado.prog2.exception.FoodStoreException;
import integrado.prog2.service.SistemaService;

/** Menu principal: indica para cada opcion donde se guardan los datos. */
public class MenuPrincipal extends Menu {

    private final MenuCategorias menuCategorias;
    private final MenuProductos menuProductos;
    private final MenuUsuarios menuUsuarios;
    private final MenuPedidos menuPedidos;
    private final SistemaService sistemaService;

    public MenuPrincipal(Consola consola, MenuCategorias menuCategorias, MenuProductos menuProductos,
                         MenuUsuarios menuUsuarios, MenuPedidos menuPedidos, SistemaService sistemaService) {
        super(consola);
        this.menuCategorias = menuCategorias;
        this.menuProductos = menuProductos;
        this.menuUsuarios = menuUsuarios;
        this.menuPedidos = menuPedidos;
        this.sistemaService = sistemaService;
    }

    @Override
    public void mostrar() {
        int opcion;
        do {
            System.out.println();
            System.out.println("==============================================");
            System.out.println("      SISTEMA DE PEDIDOS (FOOD STORE)");
            System.out.println("==============================================");
            System.out.println(" 1) Categorias .......... [BASE DE DATOS]");
            System.out.println(" 2) Productos ........... [BASE DE DATOS]");
            System.out.println(" 3) Usuarios ............ [MEMORIA]");
            System.out.println(" 4) Pedidos y detalles .. [MEMORIA]");
            System.out.println(" 5) Ver configuracion de conexion");
            System.out.println(" 0) Salir");
            opcion = consola.leerOpcion("Seleccione: ", 0, 5);
            switch (opcion) {
                case 1 -> menuCategorias.mostrar();
                case 2 -> menuProductos.mostrar();
                case 3 -> menuUsuarios.mostrar();
                case 4 -> menuPedidos.mostrar();
                case 5 -> ejecutar(this::verConfiguracion);
                default -> { }
            }
        } while (opcion != 0);
    }

    /** HU-CFG-01: unidad de persistencia activa y URL en uso (mas una prueba de conexion). */
    private void verConfiguracion() throws FoodStoreException {
        titulo("CONFIGURACION DE CONEXION (META-INF/persistence.xml)");
        System.out.println(sistemaService.configuracion());
        exito("Conexion verificada: " + sistemaService.diagnosticar());
    }
}
