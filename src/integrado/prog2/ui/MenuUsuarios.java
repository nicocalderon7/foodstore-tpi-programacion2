package integrado.prog2.ui;

import integrado.prog2.entities.Usuario;
import integrado.prog2.enums.Rol;
import integrado.prog2.exception.FoodStoreException;
import integrado.prog2.service.UsuarioService;

/** Epica 3 - Gestion de Usuarios [MEMORIA]. */
public class MenuUsuarios extends Menu {

    private final UsuarioService usuarioService;

    public MenuUsuarios(Consola consola, UsuarioService usuarioService) {
        super(consola);
        this.usuarioService = usuarioService;
    }

    @Override
    public void mostrar() {
        int opcion;
        do {
            titulo("USUARIOS (MEMORIA)");
            System.out.println("Los usuarios NO se guardan en la base: se pierden al cerrar el programa.");
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

    /** HU-USR-01 */
    private boolean listar() {
        titulo("Usuarios activos (memoria)");
        return imprimir(usuarioService.listar(), "No hay usuarios cargados en memoria.");
    }

    /** HU-USR-02 */
    private void crear() throws FoodStoreException {
        titulo("Nuevo usuario");
        String nombre = consola.leerTexto("  Nombre: ");
        String apellido = consola.leerTexto("  Apellido: ");
        String mail = consola.leerTexto("  Mail: ");
        String celular = consola.leerTexto("  Celular: ");
        String contrasena = consola.leerTexto("  Contrasena: ");
        Rol rol = consola.elegirEnum("  Rol:", Rol.values(), false);
        Usuario u = usuarioService.crear(nombre, apellido, mail, celular, contrasena, rol);
        exito("Usuario agregado en MEMORIA con id " + u.getId() + ".");
    }

    /** HU-USR-03 */
    private void editar() throws FoodStoreException {
        if (!listar()) return;
        Long id = consola.leerId("Id del usuario a editar: ");
        Usuario u = usuarioService.buscar(id);
        System.out.println("Deje vacio el campo que no quiera modificar.");
        String nombre = consola.leerTexto(actual("Nombre", u.getNombre()));
        String apellido = consola.leerTexto(actual("Apellido", u.getApellido()));
        String mail = consola.leerTexto(actual("Mail", u.getMail()));
        String celular = consola.leerTexto(actual("Celular", u.getCelular()));
        String contrasena = consola.leerTexto("  Contrasena nueva (Enter = sin cambios): ");
        Rol rol = consola.elegirEnum("  Rol [" + u.getRol() + "]:", Rol.values(), true);
        usuarioService.editar(id, nombre, apellido, mail, celular, contrasena, rol);
        exito("Usuario " + id + " actualizado en memoria.");
    }

    /** HU-USR-04 */
    private void eliminar() throws FoodStoreException {
        if (!listar()) return;
        Long id = consola.leerId("Id del usuario a eliminar: ");
        Usuario u = usuarioService.buscar(id);
        if (!consola.confirmar("Confirma la baja de " + u.getNombreCompleto() + "?")) {
            info("Operacion cancelada.");
            return;
        }
        usuarioService.eliminar(id);
        exito("Usuario " + id + " eliminado (baja logica). Sus pedidos se conservan como historial.");
    }
}
