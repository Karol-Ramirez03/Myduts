package co.uts.duts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class Estudiante implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String ARCHIVO = "usuarios.dat";

    private String nombre;
    private String codigoEstudiantil;
    private String correo;
    private String contrasena;
    private long saldoDuts;

    public Estudiante(String nombre, String codigoEstudiantil, String correo, String contrasena) {
        this.nombre = nombre;
        this.codigoEstudiantil = codigoEstudiantil;
        this.correo = correo;
        this.contrasena = contrasena;
        this.saldoDuts = 0;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCodigoEstudiantil() { return codigoEstudiantil; }
    public void setCodigoEstudiantil(String codigoEstudiantil) { this.codigoEstudiantil = codigoEstudiantil; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public long getSaldoDuts() { return saldoDuts; }
    public void setSaldoDuts(long saldoDuts) {
        if (saldoDuts < 0) throw new IllegalArgumentException("El saldo no puede ser negativo.");
        this.saldoDuts = saldoDuts;
    }

    // Devuelve todos los estudiantes guardados en el archivo.
    @SuppressWarnings("unchecked")
    public static ArrayList<Estudiante> consultar() throws IOException, ClassNotFoundException {
        File archivo = new File(ARCHIVO);
        if (!archivo.exists()) return new ArrayList<>();

        try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(archivo))) {
            return (ArrayList<Estudiante>) entrada.readObject();
        }
    }

    // Registra un estudiante. No permite repetir código ni correo.
    public static boolean registrar(Estudiante estudiante) throws IOException, ClassNotFoundException {
        if (estudiante.nombre.isBlank() || estudiante.codigoEstudiantil.isBlank()
                || estudiante.correo.isBlank() || estudiante.contrasena.isBlank()) {
            throw new IllegalArgumentException("Completa todos los campos.");
        }
        if (!estudiante.correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Escribe un correo electrónico válido.");
        }
        if (estudiante.contrasena.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
        }
        ArrayList<Estudiante> estudiantes = consultar();
        for (Estudiante actual : estudiantes) {
            if (actual.codigoEstudiantil.equalsIgnoreCase(estudiante.codigoEstudiantil)
                    || actual.correo.equalsIgnoreCase(estudiante.correo)) {
                return false;
            }
        }
        estudiantes.add(estudiante);
        guardar(estudiantes);
        return true;
    }

    // Busca el usuario por código o correo y comprueba la contraseña.
    public static Estudiante iniciarSesion(String usuario, String contrasena)
            throws IOException, ClassNotFoundException {
        for (Estudiante estudiante : consultar()) {
            boolean usuarioCorrecto = estudiante.codigoEstudiantil.equalsIgnoreCase(usuario.trim())
                    || estudiante.correo.equalsIgnoreCase(usuario.trim());
            if (usuarioCorrecto && estudiante.contrasena.equals(contrasena)) return estudiante;
        }
        return null;
    }

    // Actualiza los datos del estudiante identificado por su código actual.
    public static boolean actualizar(String codigoActual, Estudiante datosNuevos)
            throws IOException, ClassNotFoundException {
        ArrayList<Estudiante> estudiantes = consultar();
        for (int i = 0; i < estudiantes.size(); i++) {
            Estudiante actual = estudiantes.get(i);
            if (actual.codigoEstudiantil.equalsIgnoreCase(codigoActual)) {
                for (int j = 0; j < estudiantes.size(); j++) {
                    if (i == j) continue;
                    Estudiante otro = estudiantes.get(j);
                    if (otro.codigoEstudiantil.equalsIgnoreCase(datosNuevos.codigoEstudiantil)
                            || otro.correo.equalsIgnoreCase(datosNuevos.correo)) return false;
                }
                datosNuevos.saldoDuts = actual.saldoDuts;
                estudiantes.set(i, datosNuevos);
                guardar(estudiantes);
                return true;
            }
        }
        return false;
    }

    // Elimina el estudiante indicado por su código.
    public static boolean eliminar(String codigo) throws IOException, ClassNotFoundException {
        ArrayList<Estudiante> estudiantes = consultar();
        boolean eliminado = estudiantes.removeIf(
                estudiante -> estudiante.codigoEstudiantil.equalsIgnoreCase(codigo.trim()));
        if (eliminado) guardar(estudiantes);
        return eliminado;
    }

    // Guarda la lista completa siguiendo el ejemplo de ObjectOutputStream.
    private static void guardar(ArrayList<Estudiante> estudiantes) throws IOException {
        try (ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(ARCHIVO))) {
            salida.writeObject(estudiantes);
        }
    }
}
