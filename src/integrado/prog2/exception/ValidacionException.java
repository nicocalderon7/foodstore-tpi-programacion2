package integrado.prog2.exception;

/** Dato de entrada invalido (campo vacio, precio negativo, mail mal formado, etc.). */
public class ValidacionException extends FoodStoreException {

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
