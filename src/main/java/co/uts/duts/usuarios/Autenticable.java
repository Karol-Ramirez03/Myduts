package co.uts.duts.usuarios;

/**
 * Contrato de todo lo que puede iniciar sesión en la plataforma.
 */
public interface Autenticable {
    boolean autenticar(String contrasena);

    boolean cambiarContrasena(String actual, String nueva);
}
