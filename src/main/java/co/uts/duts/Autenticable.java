package co.uts.duts;

/**
 * Contrato de todo lo que puede iniciar sesión en la plataforma.
 */
public interface Autenticable {
    boolean autenticar(String contrasena);

    boolean cambiarContrasena(String actual, String nueva);
}
