package co.uts.duts.datos.texto;

import co.uts.duts.logros.Logro;
import co.uts.duts.usuarios.Estudiante;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Revisa hitos de DUTS y persiste los logros obtenidos en TXT. */
public class LogroArchivoRepository {
    private static final Path ARCHIVO = Paths.get("datos-texto", "logros_obtenidos.txt");
    private static final List<Logro> CATALOGO = List.of(
            new Logro("INICIO", "Primeros DUTS", "Alcanzaste 2.000 DUTS acumulados.", 2000, null),
            new Logro("AHORRO_5K", "Ahorro inicial", "Alcanzaste 5.000 DUTS acumulados.", 5000, null),
            new Logro("META_10K", "Constancia", "Alcanzaste 10.000 DUTS acumulados.", 10000, null),
            new Logro("META_25K", "Gran impulso", "Alcanzaste 25.000 DUTS acumulados.", 25000, null),
            new Logro("META_50K", "Casi en la meta", "Alcanzaste 50.000 DUTS acumulados.", 50000, null),
            new Logro("META_70K", "Meta cumplida", "Alcanzaste 70.000 DUTS acumulados.", 70000, null)
    );

    public synchronized List<Logro> actualizarYListar(Estudiante estudiante) throws IOException {
        asegurarArchivo();
        Map<String, LocalDateTime> obtenidos = leerLogros(estudiante.getId());
        try (BufferedWriter writer = Files.newBufferedWriter(ARCHIVO, StandardCharsets.UTF_8,
                StandardOpenOption.APPEND)) {
            for (Logro logro : CATALOGO) {
                if (estudiante.getCuenta().getDutsAcumulados() >= logro.getDutsRequeridos()
                        && !obtenidos.containsKey(logro.getCodigo())) {
                    LocalDateTime ahora = LocalDateTime.now();
                    writer.write(estudiante.getId() + ";" + logro.getCodigo() + ";" + ahora);
                    writer.newLine();
                    obtenidos.put(logro.getCodigo(), ahora);
                }
            }
        }
        List<Logro> resultado = new ArrayList<>();
        for (Logro logro : CATALOGO) {
            LocalDateTime fecha = obtenidos.get(logro.getCodigo());
            if (fecha != null) {
                resultado.add(new Logro(logro.getCodigo(), logro.getNombre(), logro.getDescripcion(),
                        logro.getDutsRequeridos(), fecha));
            }
        }
        return resultado;
    }

    private Map<String, LocalDateTime> leerLogros(long idEstudiante) throws IOException {
        Map<String, LocalDateTime> obtenidos = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(ARCHIVO, StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.isBlank()) continue;
                String[] datos = linea.split(";", -1);
                if (datos.length != 3) throw new IOException("Registro inválido en " + ARCHIVO + ".");
                try {
                    if (Long.parseLong(datos[0]) == idEstudiante) {
                        obtenidos.putIfAbsent(datos[1], LocalDateTime.parse(datos[2]));
                    }
                } catch (RuntimeException ex) {
                    throw new IOException("No se pudo leer un logro obtenido.", ex);
                }
            }
        }
        return obtenidos;
    }

    private void asegurarArchivo() throws IOException {
        Files.createDirectories(ARCHIVO.getParent());
        if (Files.notExists(ARCHIVO)) Files.createFile(ARCHIVO);
    }
}
