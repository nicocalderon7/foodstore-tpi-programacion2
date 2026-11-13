package integrado.prog2;

import integrado.prog2.dao.CategoriaDAO;
import integrado.prog2.dao.EsquemaDAO;
import integrado.prog2.dao.ProductoDAO;
import integrado.prog2.exception.PersistenciaException;
import integrado.prog2.repository.PedidoRepository;
import integrado.prog2.repository.UsuarioRepository;
import integrado.prog2.service.CategoriaService;
import integrado.prog2.service.PedidoService;
import integrado.prog2.service.ProductoService;
import integrado.prog2.service.SistemaService;
import integrado.prog2.service.UsuarioService;
import integrado.prog2.ui.Consola;
import integrado.prog2.ui.MenuCategorias;
import integrado.prog2.ui.MenuPedidos;
import integrado.prog2.ui.MenuPrincipal;
import integrado.prog2.ui.MenuProductos;
import integrado.prog2.ui.MenuUsuarios;

import java.util.Scanner;

/**
 * Punto de entrada. Solo arma las dependencias (DAO, repositorios, servicios
 * y menus), hace el diagnostico de conexion y abre el menu principal.
 * No contiene reglas de negocio ni SQL.
 */
public class Main {

    public static void main(String[] args) {
        // Acceso a datos: base de datos (JDBC) y memoria (Colecciones)
        CategoriaDAO categoriaDAO = new CategoriaDAO();
        ProductoDAO productoDAO = new ProductoDAO();
        UsuarioRepository usuarioRepository = new UsuarioRepository();
        PedidoRepository pedidoRepository = new PedidoRepository(usuarioRepository);

        // Servicios
        CategoriaService categoriaService = new CategoriaService(categoriaDAO, productoDAO);
        ProductoService productoService = new ProductoService(productoDAO, categoriaService);
        UsuarioService usuarioService = new UsuarioService(usuarioRepository);
        PedidoService pedidoService = new PedidoService(pedidoRepository, usuarioRepository, productoDAO);
        SistemaService sistemaService = new SistemaService(new EsquemaDAO());

        try (Scanner scanner = new Scanner(System.in)) {
            Consola consola = new Consola(scanner);
            MenuPrincipal menu = new MenuPrincipal(consola,
                    new MenuCategorias(consola, categoriaService),
                    new MenuProductos(consola, productoService, categoriaService),
                    new MenuUsuarios(consola, usuarioService),
                    new MenuPedidos(consola, pedidoService, usuarioService, productoService),
                    sistemaService);

            diagnosticoInicial(sistemaService);
            menu.mostrar();
            System.out.println("Hasta luego. (Los usuarios y pedidos en memoria se descartan al salir.)");
        } catch (Consola.EntradaFinalizadaException e) {
            System.out.println();
            System.out.println("Entrada finalizada. Cerrando el programa.");
        }
    }

    /** HU-CFG-02: al arrancar se prueba la conexion y se informa el resultado. */
    private static void diagnosticoInicial(SistemaService sistemaService) {
        System.out.println("Verificando conexion con la base de datos...");
        try {
            System.out.println("[OK] Conectado a " + sistemaService.diagnosticar()
                    + " (unidad: " + sistemaService.configuracion().unidad() + ")");
        } catch (PersistenciaException e) {
            System.out.println("[ERROR] " + e.getMessage());
            System.out.println("[!] Las opciones de BASE DE DATOS fallaran hasta resolver el problema."
                    + " Las de MEMORIA funcionan igual.");
        }
    }
}
