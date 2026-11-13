package integrado.prog2.exception;

/**
 * Operacion no permitida por una regla del negocio (por ejemplo, dar de baja
 * una categoria con productos activos o modificar un pedido terminado).
 */
public class ReglaNegocioException extends FoodStoreException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
