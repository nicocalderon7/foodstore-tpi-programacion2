package integrado.prog2.dao;

import integrado.prog2.config.ConexionDB;
import integrado.prog2.exception.PersistenciaException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Diagnostico del esquema al iniciar: comprueba la conexion y que existan
 * las tablas categoria y producto (usando los metadatos de JDBC).
 */
public class EsquemaDAO {

    private static final String[] TABLAS = {"categoria", "producto"};

    /** Devuelve la version del motor si la conexion funciona. */
    public String probarConexion() throws PersistenciaException {
        try (Connection con = ConexionDB.getConnection()) {
            DatabaseMetaData meta = con.getMetaData();
            return meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion();
        } catch (SQLException e) {
            throw ErroresSQL.traducir("consultar el servidor", e);
        }
    }

    /** Devuelve los nombres de las tablas obligatorias que no existen (lista vacia = todo bien). */
    public List<String> tablasFaltantes() throws PersistenciaException {
        List<String> faltantes = new ArrayList<>();
        try (Connection con = ConexionDB.getConnection()) {
            DatabaseMetaData meta = con.getMetaData();
            for (String tabla : TABLAS) {
                if (!existe(meta, con.getCatalog(), tabla) && !existe(meta, con.getCatalog(), tabla.toUpperCase())) {
                    faltantes.add(tabla);
                }
            }
            return faltantes;
        } catch (SQLException e) {
            throw ErroresSQL.traducir("verificar las tablas", e);
        }
    }

    private boolean existe(DatabaseMetaData meta, String catalogo, String tabla) throws SQLException {
        try (ResultSet rs = meta.getTables(catalogo, null, tabla, new String[]{"TABLE"})) {
            return rs.next();
        }
    }
}
