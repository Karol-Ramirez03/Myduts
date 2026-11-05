package co.uts.duts;

/**
 * Se lanza cuando un dato ingresado por el usuario no es válido (campo vacío, PIN mal formado, monto inválido...).
 */
public class ValidacionException extends Exception {
    private static final long serialVersionUID = 1L;
    private final String campo;

    public ValidacionException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCampo() { return campo; }
}
