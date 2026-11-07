package co.uts.duts.datos.texto;

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
import java.util.List;

/** Inscripciones activas guardadas en datos-texto/inscripciones.txt. */
public class InscripcionArchivoRepository {
    private static final Path ARCHIVO = Paths.get("datos-texto", "inscripciones.txt");

    public synchronized void inscribir(long idEstudiante, long idNovedad) throws IOException {
        if (estaInscrito(idEstudiante, idNovedad)) {
            throw new IllegalArgumentException("Ya tienes una inscripción activa para esta novedad.");
        }
        asegurarArchivo();
        String linea = idEstudiante + ";" + idNovedad + ";" + LocalDateTime.now();
        try (BufferedWriter writer = Files.newBufferedWriter(ARCHIVO, StandardCharsets.UTF_8,
                StandardOpenOption.APPEND)) {
            writer.write(linea);
            writer.newLine();
        }
    }

    public synchronized boolean cancelar(long idEstudiante, long idNovedad) throws IOException {
        List<String> registros = leerTodos();
        boolean eliminada = registros.removeIf(linea -> {
            String[] datos = linea.split(";", -1);
            return datos.length == 3 && datos[0].equals(Long.toString(idEstudiante))
                    && datos[1].equals(Long.toString(idNovedad));
        });
        if (eliminada) sobrescribir(registros);
        return eliminada;
    }

    public boolean estaInscrito(long idEstudiante, long idNovedad) throws IOException {
        for (String linea : leerTodos()) {
            String[] datos = linea.split(";", -1);
            if (datos.length == 3 && datos[0].equals(Long.toString(idEstudiante))
                    && datos[1].equals(Long.toString(idNovedad))) return true;
        }
        return false;
    }

    public List<Long> listarNovedadesDe(long idEstudiante) throws IOException {
        List<Long> ids = new ArrayList<>();
        for (String linea : leerTodos()) {
            String[] datos = linea.split(";", -1);
            if (datos.length == 3 && datos[0].equals(Long.toString(idEstudiante))) {
                ids.add(Long.parseLong(datos[1]));
            }
        }
        return ids;
    }

    public int contarInscritos(long idNovedad) throws IOException {
        int cantidad = 0;
        for (String linea : leerTodos()) {
            String[] datos = linea.split(";", -1);
            if (datos.length == 3 && datos[1].equals(Long.toString(idNovedad))) cantidad++;
        }
        return cantidad;
    }

    public synchronized void eliminarPorNovedad(long idNovedad) throws IOException {
        List<String> registros = leerTodos();
        if (registros.removeIf(linea -> {
            String[] datos = linea.split(";", -1);
            return datos.length == 3 && datos[1].equals(Long.toString(idNovedad));
        })) {
            sobrescribir(registros);
        }
    }

    private List<String> leerTodos() throws IOException {
        asegurarArchivo();
        List<String> registros = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(ARCHIVO, StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (!linea.isBlank()) registros.add(linea);
            }
        }
        return registros;
    }

    private void sobrescribir(List<String> registros) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(ARCHIVO, StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            for (String linea : registros) {
                writer.write(linea);
                writer.newLine();
            }
        }
    }

    private void asegurarArchivo() throws IOException {
        Files.createDirectories(ARCHIVO.getParent());
        if (Files.notExists(ARCHIVO)) Files.createFile(ARCHIVO);
    }
}
