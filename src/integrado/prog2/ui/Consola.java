package integrado.prog2.ui;

import java.util.Scanner;

/**
 * Lectura validada de datos desde el teclado. Ante un dato mal ingresado
 * (letras donde va un numero, opcion fuera de rango) muestra un aviso y
 * vuelve a preguntar, en lugar de cortar el programa.
 */
public class Consola {

    private final Scanner scanner;

    public Consola(Scanner scanner) {
        this.scanner = scanner;
    }

    /** Lee una linea (puede ser vacia). Si se cierra la entrada, termina el programa ordenadamente. */
    public String leerTexto(String mensaje) {
        System.out.print(mensaje);
        if (!scanner.hasNextLine()) {
            throw new EntradaFinalizadaException();
        }
        return scanner.nextLine().trim();
    }

    /** Opcion de menu entre min y max (inclusive). */
    public int leerOpcion(String mensaje, int min, int max) {
        while (true) {
            Integer valor = parsearEntero(leerTexto(mensaje));
            if (valor != null && valor >= min && valor <= max) {
                return valor;
            }
            System.out.println("  Opcion invalida. Ingrese un numero entre " + min + " y " + max + ".");
        }
    }

    /** Id numerico positivo. */
    public Long leerId(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje);
            try {
                long id = Long.parseLong(texto);
                if (id > 0) {
                    return id;
                }
            } catch (NumberFormatException ignorada) {
                // se informa abajo
            }
            System.out.println("  Id invalido. Ingrese un numero entero positivo.");
        }
    }

    /** Entero obligatorio (puede ser negativo: la regla de negocio lo valida el servicio). */
    public int leerEntero(String mensaje) {
        while (true) {
            Integer valor = parsearEntero(leerTexto(mensaje));
            if (valor != null) {
                return valor;
            }
            System.out.println("  Valor invalido. Ingrese un numero entero.");
        }
    }

    /** Entero opcional: Enter devuelve null (conservar valor anterior). */
    public Integer leerEnteroOpcional(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje);
            if (texto.isEmpty()) {
                return null;
            }
            Integer valor = parsearEntero(texto);
            if (valor != null) {
                return valor;
            }
            System.out.println("  Valor invalido. Ingrese un numero entero o Enter para no modificar.");
        }
    }

    /** Decimal obligatorio. Acepta coma o punto como separador. */
    public double leerDecimal(String mensaje) {
        while (true) {
            Double valor = parsearDecimal(leerTexto(mensaje));
            if (valor != null) {
                return valor;
            }
            System.out.println("  Valor invalido. Ingrese un numero (ej: 1500 o 1500.50).");
        }
    }

    /** Decimal opcional: Enter devuelve null (conservar valor anterior). */
    public Double leerDecimalOpcional(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje);
            if (texto.isEmpty()) {
                return null;
            }
            Double valor = parsearDecimal(texto);
            if (valor != null) {
                return valor;
            }
            System.out.println("  Valor invalido. Ingrese un numero o Enter para no modificar.");
        }
    }

    /** Pregunta S/N obligatoria. */
    public boolean confirmar(String mensaje) {
        while (true) {
            String r = leerTexto(mensaje + " (S/N): ").toUpperCase();
            if (r.equals("S") || r.equals("SI")) return true;
            if (r.equals("N") || r.equals("NO")) return false;
            System.out.println("  Responda S o N.");
        }
    }

    /** S/N opcional: Enter devuelve null (conservar valor anterior). */
    public Boolean confirmarOpcional(String mensaje) {
        while (true) {
            String r = leerTexto(mensaje + " (S/N, Enter = sin cambios): ").toUpperCase();
            if (r.isEmpty()) return null;
            if (r.equals("S") || r.equals("SI")) return true;
            if (r.equals("N") || r.equals("NO")) return false;
            System.out.println("  Responda S, N o Enter.");
        }
    }

    /**
     * Elige un valor de un enum por opcion numerica.
     *
     * @param opcional si es true, la opcion 0 significa "sin cambios" y devuelve null
     */
    public <E extends Enum<E>> E elegirEnum(String titulo, E[] valores, boolean opcional) {
        System.out.println(titulo);
        for (int i = 0; i < valores.length; i++) {
            System.out.println("  " + (i + 1) + ") " + valores[i]);
        }
        if (opcional) {
            System.out.println("  0) Sin cambios");
        }
        int opcion = leerOpcion("Seleccione: ", opcional ? 0 : 1, valores.length);
        return opcion == 0 ? null : valores[opcion - 1];
    }

    private Integer parsearEntero(String texto) {
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parsearDecimal(String texto) {
        try {
            double d = Double.parseDouble(texto.replace(',', '.'));
            return Double.isFinite(d) ? d : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Se lanza cuando la entrada estandar se cierra (por ej. Ctrl+D / Ctrl+Z). */
    public static class EntradaFinalizadaException extends RuntimeException {
        public EntradaFinalizadaException() {
            super("Entrada finalizada");
        }
    }
}
