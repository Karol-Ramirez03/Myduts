package co.uts.duts.duts;

import co.uts.duts.datos.texto.EstudianteArchivoRepository;
import co.uts.duts.datos.texto.MovimientoArchivoRepository;
import co.uts.duts.usuarios.Estudiante;
import co.uts.duts.usuarios.Usuario;
import co.uts.duts.validacion.SaldoInsuficienteException;
import co.uts.duts.validacion.ValidacionException;
import co.uts.duts.validacion.Validador;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Operaciones de DUTS guardadas en los archivos de texto de la aplicación. */
public class ServicioDuts {
    private final EstudianteArchivoRepository usuarios = new EstudianteArchivoRepository();
    private final MovimientoArchivoRepository movimientos = new MovimientoArchivoRepository();
    private static final DateTimeFormatter FORMATO_FECHA_EXTRACTO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Valida una transferencia y la devuelve sin aplicarla ni guardarla. */
    public synchronized Transferencia previsualizarTransferencia(long idOrigen, String correoDestino, int monto)
            throws IOException, ValidacionException, SaldoInsuficienteException {
        List<Usuario> lista = usuarios.listar();
        Estudiante origen = estudiantePorId(lista, idOrigen);
        Estudiante destino = estudiantePorCorreo(lista, correoDestino);
        if (origen == null || !origen.isActivo()) {
            throw new ValidacionException("origen", "La cuenta de estudiante no existe o está inactiva.");
        }
        if (destino == null || !destino.isActivo()) {
            throw new ValidacionException("destino", "No se encontró un estudiante activo con ese correo.");
        }
        return origen.enviarDuts(destino, monto);
    }

    public synchronized Transferencia transferir(long idOrigen, String correoDestino, int monto)
            throws IOException, ValidacionException, SaldoInsuficienteException {
        List<Usuario> lista = usuarios.listar();
        Estudiante origen = estudiantePorId(lista, idOrigen);
        Estudiante destino = estudiantePorCorreo(lista, correoDestino);
        if (origen == null || !origen.isActivo()) {
            throw new ValidacionException("origen", "La cuenta de estudiante no existe o está inactiva.");
        }
        if (destino == null || !destino.isActivo()) {
            throw new ValidacionException("destino", "No se encontró un estudiante activo con ese correo o código.");
        }

        Transferencia transferencia = origen.enviarDuts(destino, monto);
        int saldoOrigenAntes = origen.getCuenta().getSaldoDuts();
        int saldoDestinoAntes = destino.getCuenta().getSaldoDuts();
        transferencia.aplicar(origen.getCuenta(), destino.getCuenta());
        usuarios.guardarUsuarios(lista);
        try {
            movimientos.guardar(transferencia);
        } catch (IOException ex) {
            origen.getCuenta().establecerSaldoDuts(saldoOrigenAntes);
            destino.getCuenta().establecerSaldoDuts(saldoDestinoAntes);
            usuarios.guardarUsuarios(lista);
            throw ex;
        }
        return transferencia;
    }

    public synchronized AjusteAdmin ajustarSaldo(long idAdministrador, long idEstudiante, int monto, String motivo)
            throws IOException, ValidacionException {
        verificarAdministrador(idAdministrador);
        Validador.montoValido(monto);
        Validador.noVacio(motivo, "motivo");
        List<Usuario> lista = usuarios.listar();
        Estudiante estudiante = estudiantePorId(lista, idEstudiante);
        if (estudiante == null) {
            throw new ValidacionException("estudiante", "El estudiante no existe.");
        }

        int saldoAntes = estudiante.getCuenta().getSaldoDuts();
        int acumuladosAntes = estudiante.getCuenta().getDutsAcumulados();
        AjusteAdmin ajuste = new AjusteAdmin(idAdministrador, idEstudiante, monto, motivo.trim());
        ajuste.aplicar(null, estudiante.getCuenta());
        usuarios.guardarUsuarios(lista);
        try {
            movimientos.guardar(ajuste);
        } catch (IOException ex) {
            estudiante.getCuenta().establecerSaldoDuts(saldoAntes);
            estudiante.getCuenta().establecerDutsAcumulados(acumuladosAntes);
            usuarios.guardarUsuarios(lista);
            throw ex;
        }
        return ajuste;
    }

    /** Cambia el saldo exacto; las reducciones quedan registradas y no suman al promedio de ingresos. */
    public synchronized void establecerSaldo(long idAdministrador, long idEstudiante, int nuevoSaldo)
            throws IOException, ValidacionException, SaldoInsuficienteException {
        verificarAdministrador(idAdministrador);
        if (nuevoSaldo < 0) {
            throw new ValidacionException("saldo", "El saldo no puede ser negativo.");
        }
        List<Usuario> lista = usuarios.listar();
        Estudiante estudiante = estudiantePorId(lista, idEstudiante);
        if (estudiante == null) {
            throw new ValidacionException("estudiante", "El estudiante no existe.");
        }
        int actual = estudiante.getCuenta().getSaldoDuts();
        int diferencia = nuevoSaldo - actual;
        if (diferencia == 0) {
            return;
        }

        AjusteAdmin movimiento = new AjusteAdmin(idAdministrador, idEstudiante, Math.abs(diferencia),
                diferencia > 0 ? "Ajuste de saldo por administrador" : "[DEBITO] Ajuste de saldo por administrador");
        int acumuladosAntes = estudiante.getCuenta().getDutsAcumulados();
        if (diferencia > 0) {
            movimiento.aplicar(null, estudiante.getCuenta());
        } else {
            estudiante.getCuenta().retirar(-diferencia);
            movimiento.setEstado(Movimiento.ESTADO_CONFIRMADO);
        }
        usuarios.guardarUsuarios(lista);
        try {
            movimientos.guardar(movimiento);
        } catch (IOException ex) {
            estudiante.getCuenta().establecerSaldoDuts(actual);
            estudiante.getCuenta().establecerDutsAcumulados(acumuladosAntes);
            usuarios.guardarUsuarios(lista);
            throw ex;
        }
    }

    public Estudiante consultarEstudiante(long id) throws IOException {
        Usuario usuario = usuarios.consultar(id);
        return usuario instanceof Estudiante ? (Estudiante) usuario : null;
    }

    public List<Estudiante> listarEstudiantes() throws IOException {
        return usuarios.listarEstudiantes();
    }

    public List<Movimiento> listarMovimientos(long idEstudiante) throws IOException {
        return movimientos.listarPorEstudiante(idEstudiante);
    }

    /** Filtra el historial a los últimos meses e, opcionalmente, por correo del destinatario. */
    public List<Movimiento> filtrarMovimientos(long idEstudiante, int meses, String correoDestino)
            throws IOException, ValidacionException {
        if (meses < 1) {
            throw new ValidacionException("meses", "Indica un número de meses mayor que cero.");
        }
        LocalDateTime desde = LocalDateTime.now().minusMonths(meses);
        String correoFiltro = correoDestino == null ? "" : correoDestino.trim();
        List<Movimiento> resultado = new java.util.ArrayList<>();
        for (Movimiento movimiento : movimientos.listarPorEstudiante(idEstudiante)) {
            if (movimiento.getFecha().isBefore(desde)) continue;
            if (!correoFiltro.isEmpty()) {
                if (!"TRANSFERENCIA".equals(movimiento.getTipo())
                        || movimiento.getIdOrigen() == null || movimiento.getIdOrigen() != idEstudiante
                        || movimiento.getIdDestino() == null) continue;
                Usuario destinatario = usuarios.consultar(movimiento.getIdDestino());
                if (destinatario == null || !destinatario.getCorreo().equalsIgnoreCase(correoFiltro)) continue;
            }
            resultado.add(movimiento);
        }
        resultado.sort(Comparator.comparing(Movimiento::getFecha).reversed());
        return resultado;
    }

    public Usuario consultarUsuario(long id) throws IOException {
        return usuarios.consultar(id);
    }

    public List<Estudiante> rankingDuts(int limite) throws IOException {
        List<Estudiante> estudiantes = usuarios.listarEstudiantes();
        estudiantes.removeIf(estudiante -> !estudiante.isActivo());
        estudiantes.sort(Comparator
                .comparingInt((Estudiante estudiante) -> estudiante.getCuenta().getDutsAcumulados()).reversed()
                .thenComparing(Estudiante::getNombre, String.CASE_INSENSITIVE_ORDER));
        if (limite < estudiantes.size()) return new ArrayList<>(estudiantes.subList(0, Math.max(0, limite)));
        return estudiantes;
    }

    /** Genera y guarda el extracto de los movimientos del último mes en un archivo TXT. */
    public Path generarExtractoMensual(long idEstudiante) throws IOException, ValidacionException {
        Usuario estudiante = usuarios.consultar(idEstudiante);
        if (!(estudiante instanceof Estudiante)) {
            throw new IOException("No se encontró la cuenta de estudiante para generar el extracto.");
        }
        List<Movimiento> lista = filtrarMovimientos(idEstudiante, 1, "");
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime desde = ahora.minusMonths(1);
        StringBuilder contenido = new StringBuilder("MyDUTS - Extracto de transacciones\n")
                .append("Estudiante: ").append(estudiante.getNombre()).append("\n")
                .append("Correo: ").append(estudiante.getCorreo()).append("\n")
                .append("Periodo: ").append(desde.format(FORMATO_FECHA_EXTRACTO)).append(" a ")
                .append(ahora.format(FORMATO_FECHA_EXTRACTO)).append("\n\n");
        if (lista.isEmpty()) {
            contenido.append("No se registraron movimientos en este periodo.\n");
        } else {
            for (Movimiento movimiento : lista) {
                contenido.append(movimiento.getFecha().format(FORMATO_FECHA_EXTRACTO)).append(" | ")
                        .append(movimiento.getTipo()).append(" | ").append(movimiento.getMonto())
                        .append(" DUTS | ").append(movimiento.getEstado()).append("\n")
                        .append(movimiento.getDescripcion() == null ? "" : movimiento.getDescripcion())
                        .append("\n");
                Long relacionadoId = movimiento.getIdOrigen() != null
                        && movimiento.getIdOrigen() == idEstudiante
                        ? movimiento.getIdDestino() : movimiento.getIdOrigen();
                if (relacionadoId != null) {
                    Usuario relacionado = usuarios.consultar(relacionadoId);
                    if (relacionado != null) contenido.append("Cuenta relacionada: ")
                            .append(relacionado.getCorreo()).append("\n");
                }
                contenido.append("\n");
            }
        }
        Path carpeta = Paths.get("datos-texto", "extractos");
        Files.createDirectories(carpeta);
        String mes = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        Path archivo = carpeta.resolve("extracto_estudiante_" + idEstudiante + "_" + mes + ".txt");
        Files.writeString(archivo, contenido.toString(), StandardCharsets.UTF_8);
        return archivo;
    }

    public void guardarCuenta(Estudiante estudiante) throws IOException {
        usuarios.guardarUsuario(estudiante);
    }

    public void cargarHistorial(Estudiante estudiante) throws IOException {
        estudiante.getCuenta().cargarMovimientos(movimientos.listarPorEstudiante(estudiante.getId()));
    }

    private Estudiante estudiantePorId(List<Usuario> lista, long id) {
        for (Usuario usuario : lista) {
            if (usuario.getId() == id && usuario instanceof Estudiante) {
                return (Estudiante) usuario;
            }
        }
        return null;
    }

    private Estudiante estudiantePorCorreo(List<Usuario> lista, String correo) {
        if (correo == null) return null;
        for (Usuario usuario : lista) {
            if (usuario instanceof Estudiante && usuario.getCorreo().equalsIgnoreCase(correo.trim())) {
                return (Estudiante) usuario;
            }
        }
        return null;
    }

    private void verificarAdministrador(long idAdministrador) throws IOException, ValidacionException {
        Usuario usuario = usuarios.consultar(idAdministrador);
        if (usuario == null || !"ADMIN".equals(usuario.getRol()) || !usuario.isActivo()) {
            throw new ValidacionException("administrador", "Se requiere una cuenta de administrador activa.");
        }
    }
}
