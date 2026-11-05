package co.uts.duts.validacion;

import java.util.regex.Pattern;

/**
 * Validaciones reutilizables para el ingreso de datos. Los métodos que no devuelven boolean
 * lanzan {@link ValidacionException} con un mensaje listo para mostrar al usuario.
 */
public final class Validador {
    private static final Pattern CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private Validador() { }

    public static void noVacio(String valor, String campo) throws ValidacionException {
        if (valor == null || valor.trim().isEmpty()) {
            throw new ValidacionException(campo, "El campo " + campo + " no puede estar vacío.");
        }
    }

    public static boolean correoValido(String correo) {
        return correo != null && CORREO.matcher(correo.trim()).matches();
    }

    public static void pinValido(String pin) throws ValidacionException {
        if (pin == null || !pin.matches("\\d{4}")) {
            throw new ValidacionException("pin", "El PIN debe tener exactamente 4 dígitos.");
        }
    }

    public static void montoValido(int monto) throws ValidacionException {
        if (monto <= 0) {
            throw new ValidacionException("monto", "El monto debe ser mayor que cero.");
        }
    }

    public static void calificacionValida(int estrellas) throws ValidacionException {
        if (estrellas < 1 || estrellas > 5) {
            throw new ValidacionException("calificación", "La calificación debe estar entre 1 y 5 estrellas.");
        }
    }
}
