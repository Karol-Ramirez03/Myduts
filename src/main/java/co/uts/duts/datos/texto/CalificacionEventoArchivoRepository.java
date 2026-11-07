package co.uts.duts.datos.texto;

import co.uts.duts.novedades.CalificacionEvento;
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

/** Guarda calificaciones de eventos en datos-texto/calificaciones_eventos.txt. */
public class CalificacionEventoArchivoRepository {
    private static final Path ARCHIVO = Paths.get("datos-texto", "calificaciones_eventos.txt");

    public synchronized void guardar(CalificacionEvento calificacion) throws IOException {
        if (yaCalifico(calificacion.getIdEstudiante(), calificacion.getIdNovedad())) {
            throw new IllegalArgumentException("Ya calificaste este evento.");
        }
        asegurarArchivo();
        try (BufferedWriter writer = Files.newBufferedWriter(ARCHIVO, StandardCharsets.UTF_8,
                StandardOpenOption.APPEND)) {
            writer.write(calificacion.getIdEstudiante() + ";" + calificacion.getIdNovedad() + ";"
                    + calificacion.getEstrellas() + ";" + calificacion.getFecha() + ";"
                    + codificar(calificacion.getComentario() == null ? "" : calificacion.getComentario()));
            writer.newLine();
        }
    }

    public boolean yaCalifico(long idEstudiante, long idNovedad) throws IOException {
        for (CalificacionEvento calificacion : listar()) {
            if (calificacion.getIdEstudiante() == idEstudiante && calificacion.getIdNovedad() == idNovedad) {
                return true;
            }
        }
        return false;
    }

    public List<CalificacionEvento> listar() throws IOException {
        asegurarArchivo();
        List<CalificacionEvento> calificaciones = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(ARCHIVO, StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.isBlank()) continue;
                String[] datos = linea.split(";", -1);
                if (datos.length != 5) throw new IOException("Registro inválido en " + ARCHIVO + ".");
                try {
                    calificaciones.add(new CalificacionEvento(Long.parseLong(datos[0]),
                            Long.parseLong(datos[1]), Integer.parseInt(datos[2]),
                            decodificar(datos[4]), LocalDateTime.parse(datos[3])));
                } catch (RuntimeException ex) {
                    throw new IOException("No se pudo leer una calificación de evento.", ex);
                }
            }
        }
        return calificaciones;
    }

    public List<CalificacionEvento> listarPorEvento(long idNovedad) throws IOException {
        List<CalificacionEvento> resultado = new ArrayList<>();
        for (CalificacionEvento calificacion : listar()) {
            if (calificacion.getIdNovedad() == idNovedad) resultado.add(calificacion);
        }
        return resultado;
    }

    private void asegurarArchivo() throws IOException {
        Files.createDirectories(ARCHIVO.getParent());
        if (Files.notExists(ARCHIVO)) Files.createFile(ARCHIVO);
    }

    private String codificar(String texto) {
        return Base64.getEncoder().encodeToString(texto.getBytes(StandardCharsets.UTF_8));
    }

    private String decodificar(String texto) {
        return new String(Base64.getDecoder().decode(texto), StandardCharsets.UTF_8);
    }
}
