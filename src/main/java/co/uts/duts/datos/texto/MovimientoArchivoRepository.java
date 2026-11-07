package co.uts.duts.datos.texto;

import co.uts.duts.duts.AjusteAdmin;
import co.uts.duts.duts.Movimiento;
import co.uts.duts.duts.RecompensaEvento;
import co.uts.duts.duts.Transferencia;
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

/** Guarda el historial de movimientos de DUTS en un archivo de texto. */
public class MovimientoArchivoRepository {
    private static final Path ARCHIVO = Paths.get("datos-texto", "movimientos.txt");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public synchronized void guardar(Movimiento movimiento) throws IOException {
        asegurarArchivo();
        movimiento.setId(siguienteId());
        try (BufferedWriter writer = Files.newBufferedWriter(ARCHIVO, StandardCharsets.UTF_8,
                StandardOpenOption.APPEND)) {
            writer.write(aLinea(movimiento));
            writer.newLine();
        }
    }

    public List<Movimiento> listar() throws IOException {
        asegurarArchivo();
        List<Movimiento> movimientos = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(ARCHIVO, StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (!linea.isBlank()) {
                    movimientos.add(desdeLinea(linea));
                }
            }
        }
        return movimientos;
    }

    public List<Movimiento> listarPorEstudiante(long idEstudiante) throws IOException {
        List<Movimiento> resultado = new ArrayList<>();
        for (Movimiento movimiento : listar()) {
            if ((movimiento.getIdDestino() != null && movimiento.getIdDestino() == idEstudiante)
                    || (movimiento.getIdOrigen() != null && movimiento.getIdOrigen() == idEstudiante)) {
                resultado.add(movimiento);
            }
        }
        return resultado;
    }

    private long siguienteId() throws IOException {
        long mayor = 0;
        for (Movimiento movimiento : listar()) {
            mayor = Math.max(mayor, movimiento.getId());
        }
        return mayor + 1;
    }

    private void asegurarArchivo() throws IOException {
        Files.createDirectories(ARCHIVO.getParent());
        if (Files.notExists(ARCHIVO)) {
            Files.createFile(ARCHIVO);
        }
    }

    private String aLinea(Movimiento movimiento) {
        long origen = movimiento.getIdOrigen() == null ? 0 : movimiento.getIdOrigen();
        long destino = movimiento.getIdDestino() == null ? 0 : movimiento.getIdDestino();
        long idNovedad = movimiento instanceof RecompensaEvento
                ? ((RecompensaEvento) movimiento).getIdNovedad() : 0;
        return movimiento.getId() + ";" + movimiento.getTipo() + ";" + origen + ";" + destino + ";"
                + movimiento.getMonto() + ";" + movimiento.getFecha().format(FORMATO_FECHA) + ";"
                + movimiento.getEstado() + ";" + idNovedad + ";" + codificar(movimiento.getDescripcion());
    }

    private Movimiento desdeLinea(String linea) throws IOException {
        String[] datos = linea.split(";", -1);
        if (datos.length != 9) {
            throw new IOException("Registro inválido en " + ARCHIVO + ".");
        }
        try {
            long id = Long.parseLong(datos[0]);
            long origen = Long.parseLong(datos[2]);
            long destino = Long.parseLong(datos[3]);
            int monto = Integer.parseInt(datos[4]);
            LocalDateTime fecha = LocalDateTime.parse(datos[5], FORMATO_FECHA);
            String estado = datos[6];
            long idNovedad = Long.parseLong(datos[7]);
            String descripcion = decodificar(datos[8]);
            switch (datos[1]) {
                case "TRANSFERENCIA":
                    return new Transferencia(id, origen, destino, monto, descripcion, fecha, estado);
                case "AJUSTE":
                    return new AjusteAdmin(id, origen, destino, monto, descripcion, fecha, estado);
                case "RECOMPENSA_EVENTO":
                    return new RecompensaEvento(id, destino, idNovedad, monto, descripcion, fecha, estado);
                default:
                    throw new IOException("Tipo de movimiento no reconocido: " + datos[1]);
            }
        } catch (RuntimeException ex) {
            throw new IOException("No se pudo leer un movimiento de " + ARCHIVO + ".", ex);
        }
    }

    private String codificar(String texto) {
        return Base64.getEncoder().encodeToString((texto == null ? "" : texto).getBytes(StandardCharsets.UTF_8));
    }

    private String decodificar(String texto) {
        return new String(Base64.getDecoder().decode(texto), StandardCharsets.UTF_8);
    }
}
