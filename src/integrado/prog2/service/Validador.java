package integrado.prog2.service;

import integrado.prog2.exception.ValidacionException;

import java.util.regex.Pattern;

/** Validaciones de datos de entrada reutilizadas por los servicios. */
final class Validador {

    private static final Pattern MAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern CELULAR = Pattern.compile("^[0-9+\\- ]{6,20}$");

    private Validador() {
    }

    /** Devuelve el texto sin espacios en los extremos; error si esta vacio o es demasiado largo. */
    static String obligatorio(String valor, String campo, int maximo) throws ValidacionException {
        if (valor == null || valor.isBlank()) {
            throw new ValidacionException("El campo '" + campo + "' es obligatorio.");
        }
        return largo(valor.trim(), campo, maximo);
    }

    /** Texto opcional: vacio se convierte en null. */
    static String opcional(String valor, String campo, int maximo) throws ValidacionException {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return largo(valor.trim(), campo, maximo);
    }

    static String mail(String valor) throws ValidacionException {
        String mail = obligatorio(valor, "mail", 120).toLowerCase();
        if (!MAIL.matcher(mail).matches()) {
            throw new ValidacionException("El mail '" + mail + "' no tiene un formato valido (ej: nombre@dominio.com).");
        }
        return mail;
    }

    static String celular(String valor) throws ValidacionException {
        String cel = obligatorio(valor, "celular", 20);
        if (!CELULAR.matcher(cel).matches()) {
            throw new ValidacionException("El celular solo puede tener numeros, espacios, '+' o '-' (6 a 20 caracteres).");
        }
        return cel;
    }

    static double precio(Double precio) throws ValidacionException {
        if (precio == null || precio.isNaN() || precio.isInfinite()) {
            throw new ValidacionException("El precio es obligatorio.");
        }
        if (precio < 0) {
            throw new ValidacionException("El precio no puede ser negativo (ingresado: " + precio + ").");
        }
        return precio;
    }

    private static String largo(String valor, String campo, int maximo) throws ValidacionException {
        if (valor.length() > maximo) {
            throw new ValidacionException("El campo '" + campo + "' admite como maximo " + maximo + " caracteres.");
        }
        return valor;
    }
}
