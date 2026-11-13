package integrado.prog2.exception;

/** Stock negativo al cargar un producto o stock insuficiente al armar/confirmar un pedido. */
public class StockInvalidoException extends FoodStoreException {

    public StockInvalidoException(String mensaje) {
        super(mensaje);
    }
}
