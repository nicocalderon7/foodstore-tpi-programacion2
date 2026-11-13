package integrado.prog2.ui;

import integrado.prog2.exception.FoodStoreException;

import java.util.List;

/**
 * Base de los menus de consola. Centraliza la captura de excepciones:
 * cualquier error de una operacion se muestra como mensaje y se vuelve al menu.
 */
public abstract class Menu {

    protected final Consola consola;

    protected Menu(Consola consola) {
        this.consola = consola;
    }

    /** Muestra el menu hasta que el usuario elige volver. */
    public abstract void mostrar();

    /** Una operacion del menu que puede fallar con una excepcion propia. */
    @FunctionalInterface
    protected interface Accion {
        void ejecutar() throws FoodStoreException;
    }

    /** Ejecuta la operacion y transforma cualquier excepcion en un mensaje claro. */
    protected void ejecutar(Accion accion) {
        try {
            accion.ejecutar();
        } catch (FoodStoreException e) {
            error(e.getMessage());
        } catch (Consola.EntradaFinalizadaException e) {
            throw e; // la maneja Main
        } catch (RuntimeException e) {
            error("Error inesperado: " + e.getMessage());
        }
    }

    protected void titulo(String texto) {
        System.out.println();
        System.out.println("=== " + texto + " ===");
    }

    protected void exito(String mensaje) {
        System.out.println("[OK] " + mensaje);
    }

    protected void error(String mensaje) {
        System.out.println("[ERROR] " + mensaje);
    }

    protected void info(String mensaje) {
        System.out.println("[i] " + mensaje);
    }

    /** Imprime una lista; si esta vacia muestra el mensaje indicado. Devuelve false si estaba vacia. */
    protected boolean imprimir(List<?> elementos, String mensajeVacio) {
        if (elementos.isEmpty()) {
            info(mensajeVacio);
            return false;
        }
        elementos.forEach(System.out::println);
        return true;
    }

    /** Texto para preguntar un campo editable mostrando su valor actual. */
    protected String actual(String campo, Object valor) {
        return "  " + campo + " [" + (valor == null ? "" : valor) + "] (Enter = sin cambios): ";
    }
}
