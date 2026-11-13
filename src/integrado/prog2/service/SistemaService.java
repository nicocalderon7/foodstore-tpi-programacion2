package integrado.prog2.service;

import integrado.prog2.config.ConexionDB;
import integrado.prog2.config.ConfiguracionConexion;
import integrado.prog2.dao.EsquemaDAO;
import integrado.prog2.exception.PersistenciaException;

import java.util.List;

/** Configuracion y diagnostico de la conexion (Epica 5). */
public class SistemaService {

    private final EsquemaDAO esquemaDAO;

    public SistemaService(EsquemaDAO esquemaDAO) {
        this.esquemaDAO = esquemaDAO;
    }

    public ConfiguracionConexion configuracion() throws PersistenciaException {
        return ConexionDB.getConfiguracion();
    }

    /**
     * Prueba la conexion y la existencia de las tablas.
     *
     * @return descripcion del motor conectado
     * @throws PersistenciaException con la causa probable si algo falla
     */
    public String diagnosticar() throws PersistenciaException {
        String motor = esquemaDAO.probarConexion();
        List<String> faltantes = esquemaDAO.tablasFaltantes();
        if (!faltantes.isEmpty()) {
            throw new PersistenciaException("Conectado a " + motor + ", pero faltan las tablas " + faltantes + "."
                    + System.lineSeparator() + "  -> Ejecute el script db/01_crear_esquema.sql.");
        }
        return motor;
    }
}
