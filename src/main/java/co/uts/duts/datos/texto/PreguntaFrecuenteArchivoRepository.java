package co.uts.duts.datos.texto;

import co.uts.duts.novedades.PreguntaFrecuente;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/** Persistencia TXT de preguntas frecuentes. */
public class PreguntaFrecuenteArchivoRepository {
    private static final Path ARCHIVO = Paths.get("datos-texto", "preguntas_frecuentes.txt");

    public synchronized PreguntaFrecuente agregar(String pregunta, String respuesta) throws IOException {
        if (pregunta == null || pregunta.isBlank() || respuesta == null || respuesta.isBlank()) {
            throw new IllegalArgumentException("Escribe la pregunta y su respuesta.");
        }
        PreguntaFrecuente nueva = new PreguntaFrecuente(siguienteId(), pregunta.trim(), respuesta.trim());
        asegurarArchivo();
        try (BufferedWriter writer = Files.newBufferedWriter(ARCHIVO, StandardCharsets.UTF_8,
                StandardOpenOption.APPEND)) {
            writer.write(nueva.getId() + ";" + codificar(nueva.getPregunta()) + ";" + codificar(nueva.getRespuesta()));
            writer.newLine();
        }
        return nueva;
    }

    public List<PreguntaFrecuente> listar() throws IOException {
        asegurarArchivo();
        List<PreguntaFrecuente> preguntas = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(ARCHIVO, StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.isBlank()) continue;
                String[] datos = linea.split(";", -1);
                if (datos.length != 3) throw new IOException("Registro inválido en " + ARCHIVO + ".");
                try {
                    preguntas.add(new PreguntaFrecuente(Long.parseLong(datos[0]),
                            decodificar(datos[1]), decodificar(datos[2])));
                } catch (RuntimeException ex) {
                    throw new IOException("No se pudo leer una pregunta frecuente.", ex);
                }
            }
        }
        return preguntas;
    }

    private long siguienteId() throws IOException {
        long mayor = 0;
        for (PreguntaFrecuente pregunta : listar()) mayor = Math.max(mayor, pregunta.getId());
        return mayor + 1;
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
