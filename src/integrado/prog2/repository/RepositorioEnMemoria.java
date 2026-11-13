package integrado.prog2.repository;

import integrado.prog2.entities.Base;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de los repositorios que guardan entidades en una Coleccion (memoria).
 * Los datos se pierden al cerrar el programa.
 */
public interface RepositorioEnMemoria<T extends Base> {

    /** Agrega la entidad, le asigna un id generado por el programa y la devuelve. */
    T guardar(T entidad);

    /** Entidades no eliminadas. */
    List<T> buscarActivos();

    /** Entidad no eliminada con ese id. */
    Optional<T> buscarPorId(Long id);

    /** Baja logica: marca eliminado = true sin quitar el objeto de la coleccion. */
    boolean eliminar(Long id);
}
