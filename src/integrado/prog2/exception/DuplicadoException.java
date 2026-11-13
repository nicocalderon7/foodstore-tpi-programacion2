package integrado.prog2.exception;

/** Violacion de unicidad: nombre de categoria activa repetido, mail de usuario repetido. */
public class DuplicadoException extends FoodStoreException {

    public DuplicadoException(String mensaje) {
        super(mensaje);
    }
}
