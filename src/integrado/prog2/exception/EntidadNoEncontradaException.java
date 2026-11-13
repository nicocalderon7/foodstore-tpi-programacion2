package integrado.prog2.exception;

/** Se lanza cuando un id no existe o corresponde a una entidad dada de baja. */
public class EntidadNoEncontradaException extends FoodStoreException {

    /**
     * @param entidad descripcion con articulo, por ej. "la categoria" o "el producto"
     */
    public EntidadNoEncontradaException(String entidad, Long id) {
        super("No se encontro " + entidad + " con id " + id + " (no existe o fue dado de baja).");
    }
}
