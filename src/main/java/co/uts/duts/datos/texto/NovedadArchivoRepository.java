package co.uts.duts.datos.texto;

import co.uts.duts.novedades.Novedad;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/** CRUD de novedades almacenado en datos-texto/novedades.txt. */
public class NovedadArchivoRepository {
    private static final Path ARCHIVO = Paths.get("datos-texto", "novedades.txt");

    public synchronized Novedad crear(String titulo, String descripcion, String condiciones,
            LocalDateTime inicio, LocalDateTime fin, int duts, Integer cupo) throws IOException {
        validar(titulo, descripcion, condiciones, inicio, fin, duts, cupo);
        Novedad novedad = new Novedad(siguienteId(), titulo.trim(), descripcion.trim(), condiciones.trim(),
                inicio, fin, duts, cupo);
        List<Novedad> novedades = listar();
        novedades.add(novedad);
        guardarTodas(novedades);
        return novedad;
    }

    public List<Novedad> listar() throws IOException {
        asegurarArchivo();
        List<Novedad> novedades = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(ARCHIVO, StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (!linea.isBlank()) novedades.add(desdeLinea(linea));
            }
        }
        return novedades;
    }

    public Novedad consultar(long id) throws IOException {
        for (Novedad novedad : listar()) {
            if (novedad.getId() == id) return novedad;
        }
        return null;
    }

    public synchronized boolean actualizar(long id, String titulo, String descripcion, String condiciones,
            LocalDateTime inicio, LocalDateTime fin, int duts, Integer cupo) throws IOException {
        validar(titulo, descripcion, condiciones, inicio, fin, duts, cupo);
        List<Novedad> novedades = listar();
        boolean encontrada = false;
        for (int i = 0; i < novedades.size(); i++) {
            if (novedades.get(i).getId() == id) {
                novedades.set(i, new Novedad(id, titulo.trim(), descripcion.trim(), condiciones.trim(),
                        inicio, fin, duts, cupo));
                encontrada = true;
                break;
            }
        }
        if (encontrada) guardarTodas(novedades);
        return encontrada;
    }

    public synchronized boolean eliminar(long id) throws IOException {
        List<Novedad> novedades = listar();
        boolean eliminada = novedades.removeIf(novedad -> novedad.getId() == id);
        if (eliminada) guardarTodas(novedades);
        return eliminada;
    }

    private long siguienteId() throws IOException {
        long mayor = 0;
        for (Novedad novedad : listar()) mayor = Math.max(mayor, novedad.getId());
        return mayor + 1;
    }

    private void guardarTodas(List<Novedad> novedades) throws IOException {
        asegurarArchivo();
        try (BufferedWriter writer = Files.newBufferedWriter(ARCHIVO, StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Novedad novedad : novedades) {
                writer.write(aLinea(novedad));
                writer.newLine();
            }
        }
    }

    private void asegurarArchivo() throws IOException {
        Files.createDirectories(ARCHIVO.getParent());
        if (Files.notExists(ARCHIVO)) Files.createFile(ARCHIVO);
    }

    private void validar(String titulo, String descripcion, String condiciones, LocalDateTime inicio,
            LocalDateTime fin, int duts, Integer cupo) {
        if (titulo == null || titulo.isBlank() || descripcion == null || descripcion.isBlank()
                || condiciones == null || condiciones.isBlank()) {
            throw new IllegalArgumentException("Completa el título, la descripción y las condiciones.");
        }
        if (inicio == null || fin == null || !fin.isAfter(inicio)) {
            throw new IllegalArgumentException("La fecha final debe ser posterior a la fecha inicial.");
        }
        if (duts < 0) throw new IllegalArgumentException("La recompensa no puede ser negativa.");
        if (cupo != null && cupo <= 0) throw new IllegalArgumentException("El cupo debe ser mayor que cero.");
    }

    private String aLinea(Novedad novedad) {
        return novedad.getId() + ";" + codificar(novedad.getTitulo()) + ";"
                + codificar(novedad.getDescripcion()) + ";" + codificar(novedad.getCondiciones()) + ";"
                + novedad.getFechaInicio() + ";" + novedad.getFechaFin() + ";"
                + novedad.getDutsRecompensa() + ";"
                + (novedad.getCupoMaximo() == null ? "" : novedad.getCupoMaximo());
    }

    private Novedad desdeLinea(String linea) throws IOException {
        String[] datos = linea.split(";", -1);
        if (datos.length != 8) throw new IOException("Registro inválido en " + ARCHIVO + ".");
        try {
            return new Novedad(Long.parseLong(datos[0]), decodificar(datos[1]), decodificar(datos[2]),
                    decodificar(datos[3]), LocalDateTime.parse(datos[4]), LocalDateTime.parse(datos[5]),
                    Integer.parseInt(datos[6]), datos[7].isBlank() ? null : Integer.valueOf(datos[7]));
        } catch (RuntimeException ex) {
            throw new IOException("No se pudo leer una novedad de " + ARCHIVO + ".", ex);
        }
    }

    private String codificar(String texto) {
        return Base64.getEncoder().encodeToString(texto.getBytes(StandardCharsets.UTF_8));
    }

    private String decodificar(String texto) {
        return new String(Base64.getDecoder().decode(texto), StandardCharsets.UTF_8);
    }
}
