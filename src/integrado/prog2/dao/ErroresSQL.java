package integrado.prog2.dao;

import integrado.prog2.exception.PersistenciaException;

import java.sql.SQLException;

/**
 * Traduce las SQLException a PersistenciaException con un mensaje comprensible.
 * La SQLException original se conserva como causa (para depurar), pero nunca
 * se muestra un stack trace al usuario.
 *
 * Los codigos corresponden a MySQL 8.
 */
final class ErroresSQL {

    private ErroresSQL() {
    }

    static PersistenciaException traducir(String operacion, SQLException e) {
        String estado = e.getSQLState() == null ? "" : e.getSQLState();
        String detalle = switch (e.getErrorCode()) {
            case 1062 -> "ya existe un registro con ese valor unico.";
            case 1451 -> "el registro esta referenciado por otros datos.";
            case 1452 -> "la categoria indicada no existe (violacion de clave foranea).";
            case 3819 -> "un valor no cumple las restricciones de la tabla (CHECK).";
            case 1406 -> "un texto supera la longitud maxima permitida.";
            case 1048 -> "falta un dato obligatorio.";
            case 1146 -> "no existen las tablas. Ejecute el script db/01_crear_esquema.sql.";
            case 1213 -> "se detecto un interbloqueo (deadlock). Intente nuevamente.";
            case 1205 -> "se agoto el tiempo de espera de un bloqueo. Intente nuevamente.";
            default -> estado.startsWith("08")
                    ? "se perdio la conexion con la base de datos. Verifique que el servidor este activo."
                    : e.getMessage();
        };
        return new PersistenciaException("Error al " + operacion + ": " + detalle, e);
    }
}
