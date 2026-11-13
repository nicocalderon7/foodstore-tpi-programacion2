package integrado.prog2.config;

/**
 * Datos de conexion leidos de META-INF/persistence.xml (unidad de persistencia activa).
 */
public record ConfiguracionConexion(String unidad, String driver, String url, String usuario, String password) {

    /** Representacion para mostrar en pantalla: nunca muestra la contrasena. */
    @Override
    public String toString() {
        return "Unidad de persistencia : " + unidad + System.lineSeparator()
             + "Driver JDBC            : " + driver + System.lineSeparator()
             + "URL JDBC               : " + url + System.lineSeparator()
             + "Usuario                : " + usuario + System.lineSeparator()
             + "Contrasena             : " + (password == null || password.isEmpty() ? "(vacia)" : "********");
    }
}
