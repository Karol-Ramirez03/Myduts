package co.uts.duts.seguridad;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilidad de seguridad: convierte contraseñas y PIN en un hash SHA-256 para no guardarlos en texto plano.
 */
public final class Seguridad {
    private Seguridad() { }

    public static String hash(String texto) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));
            StringBuilder resultado = new StringBuilder();
            for (byte b : bytes) resultado.append(String.format("%02x", b));
            return resultado.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no disponible", ex);
        }
    }
}
