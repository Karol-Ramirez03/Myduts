package co.uts.duts.datos.texto;

import co.uts.duts.duts.Cuenta;
import co.uts.duts.usuarios.Administrador;
import co.uts.duts.usuarios.Estudiante;
import co.uts.duts.usuarios.Usuario;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/** Persistencia alternativa de usuarios en texto separado por punto y coma. */
public class EstudianteArchivoRepository {
    private static final Path ARCHIVO = Paths.get("datos-texto", "estudiantes.txt");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Registra un estudiante nuevo en datos-texto/estudiantes.txt. */
    public synchronized void registrar(String nombre, String codigo, String correo, String contrasena)
            throws IOException {
        if (existeCorreoOCodigo(correo, codigo)) {
            throw new IllegalArgumentException("El correo o código ya están registrados.");
        }
        Estudiante estudiante = new Estudiante(nombre, codigo, correo, contrasena);
        estudiante.setId(siguienteId());
        agregarLinea(aLinea(estudiante));
    }

    /** Crea el administrador inicial si todavía no está guardado en el archivo. */
    public synchronized void asegurarAdministradorInicial() throws IOException {
        for (Usuario usuario : listar()) {
            if ("ADMIN".equals(usuario.getRol())
                    || "admin@correo.uts.edu.co".equalsIgnoreCase(usuario.getCorreo())) {
                return;
            }
        }
        Administrador administrador = new Administrador(siguienteId(), "Administrador UTS", "ADMIN",
                "admin@correo.uts.edu.co", "admin", true, LocalDateTime.now());
        agregarLinea(aLinea(administrador));
    }

    public Usuario autenticar(String identificador, String contrasena) throws IOException {
        for (Usuario usuario : listar()) {
            if (usuario.isActivo()
                    && (usuario.getCorreo().equalsIgnoreCase(identificador)
                    || usuario.getCodigo().equalsIgnoreCase(identificador))
                    && usuario.autenticar(contrasena)) {
                return usuario;
            }
        }
        return null;
    }

    public boolean existeCorreoOCodigo(String correo, String codigo) throws IOException {
        for (Usuario usuario : listar()) {
            if (usuario.getCorreo().equalsIgnoreCase(correo)
                    || usuario.getCodigo().equalsIgnoreCase(codigo)) {
                return true;
            }
        }
        return false;
    }

    public List<Usuario> listar() throws IOException {
        asegurarArchivo();
        List<Usuario> usuarios = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(ARCHIVO, StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (!linea.isBlank()) {
                    usuarios.add(desdeLinea(linea));
                }
            }
        }
        return usuarios;
    }

    public List<Estudiante> listarEstudiantes() throws IOException {
        List<Estudiante> estudiantes = new ArrayList<>();
        for (Usuario usuario : listar()) {
            if (usuario instanceof Estudiante) {
                estudiantes.add((Estudiante) usuario);
            }
        }
        return estudiantes;
    }

    public Usuario consultar(long id) throws IOException {
        for (Usuario usuario : listar()) {
            if (usuario.getId() == id) {
                return usuario;
            }
        }
        return null;
    }

    public synchronized boolean editar(long id, String nuevoNombre, String nuevoCorreo) throws IOException {
        List<Usuario> usuarios = listar();
        boolean encontrado = false;
        for (Usuario usuario : usuarios) {
            if (usuario.getId() == id && usuario.actualizarPerfil(nuevoNombre, nuevoCorreo)) {
                encontrado = true;
                break;
            }
        }
        if (encontrado) {
            guardarTodos(usuarios);
        }
        return encontrado;
    }

    public synchronized boolean actualizarPerfil(long id, String nuevoNombre, String nuevoCorreo)
            throws IOException {
        List<Usuario> usuarios = listar();
        Usuario objetivo = null;
        for (Usuario usuario : usuarios) {
            if (usuario.getId() == id) {
                objetivo = usuario;
            } else if (usuario.getCorreo().equalsIgnoreCase(nuevoCorreo.trim())) {
                throw new IllegalArgumentException("Ese correo ya pertenece a otra cuenta.");
            }
        }
        if (objetivo == null || !objetivo.actualizarPerfil(nuevoNombre, nuevoCorreo)) {
            return false;
        }
        guardarTodos(usuarios);
        return true;
    }

    public synchronized void guardarUsuarios(List<Usuario> usuarios) throws IOException {
        guardarTodos(usuarios);
    }

    public synchronized void guardarUsuario(Usuario actualizado) throws IOException {
        List<Usuario> usuarios = listar();
        boolean encontrado = false;
        for (int i = 0; i < usuarios.size(); i++) {
            if (usuarios.get(i).getId() == actualizado.getId()) {
                usuarios.set(i, actualizado);
                encontrado = true;
                break;
            }
        }
        if (!encontrado) {
            throw new IOException("No se encontró el usuario que se quería guardar.");
        }
        guardarTodos(usuarios);
    }

    public synchronized boolean eliminar(long id) throws IOException {
        List<Usuario> usuarios = listar();
        boolean eliminado = usuarios.removeIf(usuario -> usuario.getId() == id);
        if (eliminado) {
            guardarTodos(usuarios);
        }
        return eliminado;
    }

    private long siguienteId() throws IOException {
        long mayor = 0;
        for (Usuario usuario : listar()) {
            mayor = Math.max(mayor, usuario.getId());
        }
        return mayor + 1;
    }

    private void asegurarArchivo() throws IOException {
        Files.createDirectories(ARCHIVO.getParent());
        if (Files.notExists(ARCHIVO)) {
            Files.createFile(ARCHIVO);
        }
    }

    private void agregarLinea(String linea) throws IOException {
        asegurarArchivo();
        try (BufferedWriter writer = Files.newBufferedWriter(ARCHIVO, StandardCharsets.UTF_8,
                StandardOpenOption.APPEND)) {
            writer.write(linea);
            writer.newLine();
        }
    }

    private void guardarTodos(List<Usuario> usuarios) throws IOException {
        asegurarArchivo();
        try (BufferedWriter writer = Files.newBufferedWriter(ARCHIVO, StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Usuario usuario : usuarios) {
                writer.write(aLinea(usuario));
                writer.newLine();
            }
        }
    }

    // Los campos de texto se codifican para que ';' y los saltos de línea no dañen las columnas.
    private String aLinea(Usuario usuario) {
        Cuenta cuenta = usuario instanceof Estudiante
                ? ((Estudiante) usuario).getCuenta() : new Cuenta();
        String fecha = usuario instanceof Estudiante
                ? ((Estudiante) usuario).getFechaRegistro().format(FORMATO_FECHA)
                : LocalDateTime.now().format(FORMATO_FECHA);
        String foto = usuario instanceof Estudiante ? ((Estudiante) usuario).getRutaFoto() : null;

        return usuario.getId() + ";" + codificar(usuario.getCodigo()) + ";"
                + codificar(usuario.getNombre()) + ";" + codificar(usuario.getCorreo()) + ";"
                + codificar(usuario.getContrasena()) + ";" + usuario.isActivo() + ";" + fecha + ";"
                + codificar(foto == null ? "" : foto) + ";" + cuenta.getSaldoDuts() + ";"
                + cuenta.getDutsAcumulados() + ";" + cuenta.getSaldoMinimoAlerta() + ";"
                + codificar(cuenta.getPin() == null ? "" : cuenta.getPin()) + ";" + usuario.getRol();
    }

    private Usuario desdeLinea(String linea) throws IOException {
        String[] datos = linea.split(";", -1);
        if (datos.length != 13) {
            throw new IOException("Registro inválido en " + ARCHIVO + ".");
        }
        try {
            long id = Long.parseLong(datos[0]);
            String codigo = decodificar(datos[1]);
            String nombre = decodificar(datos[2]);
            String correo = decodificar(datos[3]);
            String contrasena = decodificar(datos[4]);
            boolean activo = Boolean.parseBoolean(datos[5]);
            LocalDateTime fecha = datos[6].isBlank()
                    ? LocalDateTime.now() : LocalDateTime.parse(datos[6], FORMATO_FECHA);
            String foto = decodificar(datos[7]);
            Cuenta cuenta = new Cuenta(Integer.parseInt(datos[8]), Integer.parseInt(datos[9]),
                    Integer.parseInt(datos[10]), decodificar(datos[11]));

            if ("ADMIN".equals(datos[12])) {
                return new Administrador(id, nombre, codigo, correo, contrasena, activo, fecha);
            }
            return new Estudiante(id, nombre, codigo, correo, contrasena, activo, fecha,
                    foto.isEmpty() ? null : foto, cuenta);
        } catch (RuntimeException ex) {
            throw new IOException("No se pudo leer un registro de " + ARCHIVO + ".", ex);
        }
    }

    private String codificar(String texto) {
        return Base64.getEncoder().encodeToString(texto.getBytes(StandardCharsets.UTF_8));
    }

    private String decodificar(String texto) {
        return new String(Base64.getDecoder().decode(texto), StandardCharsets.UTF_8);
    }
}
