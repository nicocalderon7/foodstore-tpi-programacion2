package integrado.prog2.exception;

/**
 * Error de acceso a la base de datos. Encapsula la SQLException original
 * (como causa) con un mensaje comprensible para el usuario.
 */
public class PersistenciaException extends FoodStoreException {

    public PersistenciaException(String mensaje) {
        super(mensaje);
    }

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
