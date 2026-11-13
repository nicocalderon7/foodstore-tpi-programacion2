package integrado.prog2.exception;

/**
 * Excepcion base (checked) de la aplicacion. Todas las excepciones propias
 * heredan de esta, asi la capa de UI puede capturarlas en un solo catch
 * y mostrar el mensaje sin cortar el programa.
 */
public class FoodStoreException extends Exception {

    public FoodStoreException(String mensaje) {
        super(mensaje);
    }

    public FoodStoreException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
