package co.uts.duts;

import java.util.List;

/**
 * Usuario de la plataforma (clase abstracta). Estudiante y Administrador heredan de ella y definen
 * su propio rol y permisos (polimorfismo).
 */
public abstract class Usuario implements Autenticable {
    private long id;
    protected String nombre;
    protected String codigo;
    protected String correo;
    private String contrasenaHash;
    protected boolean activo;

    protected Usuario(long id, String nombre, String codigo, String correo, String contrasenaHash, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.codigo = codigo;
        this.correo = correo;
        this.contrasenaHash = contrasenaHash;
        this.activo = activo;
    }

    @Override
    public boolean autenticar(String contrasena) {
        return contrasena != null && Seguridad.hash(contrasena).equals(contrasenaHash);
    }

    @Override
    public boolean cambiarContrasena(String actual, String nueva) {
        if (!autenticar(actual) || nueva == null || nueva.length() < 6) {
            return false;
        }
        this.contrasenaHash = Seguridad.hash(nueva);
        return true;
    }

    /** Cambia nombre y correo si ambos son válidos; devuelve false sin modificar nada si no lo son. */
    public boolean actualizarPerfil(String nombre, String correo) {
        try {
            Validador.noVacio(nombre, "nombre");
            Validador.noVacio(correo, "correo");
        } catch (ValidacionException ex) {
            return false;
        }
        if (!Validador.correoValido(correo)) {
            return false;
        }
        this.nombre = nombre.trim();
        this.correo = correo.trim();
        return true;
    }

    /** "ESTUDIANTE" o "ADMIN" (mismo valor que la columna rol). */
    public abstract String getRol();

    public abstract List<String> getPermisos();

    public boolean tienePermiso(String permiso) {
        return getPermisos().contains(permiso);
    }

    public long getId() { return id; }
    void setId(long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public String getCodigo() { return codigo; }
    public String getCorreo() { return correo; }
    public String getContrasenaHash() { return contrasenaHash; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
