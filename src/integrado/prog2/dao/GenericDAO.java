package integrado.prog2.dao;

import integrado.prog2.entities.Base;
import integrado.prog2.exception.PersistenciaException;

import java.util.List;
import java.util.Optional;

/**
 * Contrato generico de los DAO JDBC.
 *
 * El alta (insert) no forma parte del contrato generico porque no tiene la misma
 * firma en todas las entidades: el producto necesita ademas el id de la categoria
 * (insert(Producto, Long idCategoria)), ya que el objeto Producto no lo conoce.
 *
 * @param <T> entidad del dominio que hereda de Base
 */
public interface GenericDAO<T extends Base> {

    /** Devuelve todas las entidades activas (eliminado = FALSE). */
    List<T> findAll() throws PersistenciaException;

    /** Busca una entidad activa por id. Optional vacio si no existe o esta eliminada. */
    Optional<T> findById(Long id) throws PersistenciaException;

    /** Actualiza los atributos propios. Devuelve true si se modifico un registro. */
    boolean update(T entidad) throws PersistenciaException;

    /** Baja logica: UPDATE ... SET eliminado = TRUE (nunca DELETE). */
    boolean softDelete(Long id) throws PersistenciaException;
}
