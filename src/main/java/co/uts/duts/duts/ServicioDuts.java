package co.uts.duts.duts;

import co.uts.duts.datos.texto.EstudianteArchivoRepository;
import co.uts.duts.datos.texto.MovimientoArchivoRepository;
import co.uts.duts.logros.ServicioLogros;
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
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Operaciones de DUTS guardadas en los archivos de texto de la aplicación. */
public class ServicioDuts {
    private final EstudianteArchivoRepository usuarios = new EstudianteArchivoRepository();
    private final MovimientoArchivoRepository movimientos = new MovimientoArchivoRepository();
    private static final DateTimeFormatter FORMATO_FECHA_EXTRACTO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
        revisarLogros(idEstudiante);
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
        if (diferencia > 0) revisarLogros(idEstudiante);
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

    /** Genera el extracto TXT del mes seleccionado y calcula el saldo al cierre de ese mes. */
    public Path generarExtracto(long idEstudiante, YearMonth periodo) throws IOException, ValidacionException {
        Usuario estudiante = usuarios.consultar(idEstudiante);
        if (!(estudiante instanceof Estudiante)) {
            throw new IOException("No se encontró la cuenta de estudiante para generar el extracto.");
        }
        if (periodo == null || periodo.isAfter(YearMonth.now())) {
            throw new ValidacionException("periodo", "Selecciona un mes y año válidos, no futuros.");
        }

        LocalDateTime inicio = periodo.atDay(1).atStartOfDay();
        LocalDateTime fin = periodo.plusMonths(1).atDay(1).atStartOfDay();
        List<Movimiento> todos = movimientos.listarPorEstudiante(idEstudiante);
        List<Movimiento> delPeriodo = new ArrayList<>();
        long variacionPosterior = 0;
        for (Movimiento movimiento : todos) {
            if (!Movimiento.ESTADO_CONFIRMADO.equals(movimiento.getEstado())) continue;
            if (!movimiento.getFecha().isBefore(inicio) && movimiento.getFecha().isBefore(fin)) {
                delPeriodo.add(movimiento);
            } else if (!movimiento.getFecha().isBefore(fin)) {
                variacionPosterior += variacionSaldo(movimiento, idEstudiante);
            }
        }
        delPeriodo.sort(Comparator.comparing(Movimiento::getFecha));

        long saldoFinal = ((Estudiante) estudiante).getCuenta().getSaldoDuts() - variacionPosterior;
        StringBuilder contenido = new StringBuilder("REPORTE DE EXTRACTOS\n\n")
                .append("Usuario: ").append(estudiante.getNombre()).append("\n\n")
                .append("Cuenta: ").append(estudiante.getCodigo()).append("\n\n")
                .append("Periodo: ").append(String.format("%02d/%04d", periodo.getMonthValue(), periodo.getYear()))
                .append("\n\nFecha: ").append(LocalDate.now().format(FORMATO_FECHA_EXTRACTO))
                .append("\n\n\n")
                .append(String.format("%-14s %-54s %16s%n", "Fecha", "Descripcion", "Valor"));
        if (delPeriodo.isEmpty()) {
            contenido.append("No se registraron transacciones en este periodo.\n");
        } else {
            for (Movimiento movimiento : delPeriodo) {
                long variacion = variacionSaldo(movimiento, idEstudiante);
                contenido.append(String.format("%-14s %-54s %16s%n",
                        movimiento.getFecha().format(FORMATO_FECHA_EXTRACTO),
                        descripcionExtracto(movimiento, idEstudiante).replace('\n', ' ').replace('\r', ' '),
                        formatoValor(variacion)));
            }
        }
        contenido.append("\nSALDO FINAL: ").append(formatoSaldo(saldoFinal)).append(" DUTS\n");

        Path carpeta = Paths.get("datos-texto", "extractos");
        Files.createDirectories(carpeta);
        String mes = periodo.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        Path archivo = carpeta.resolve("extracto_estudiante_" + idEstudiante + "_" + mes + ".txt");
        Files.writeString(archivo, contenido.toString(), StandardCharsets.UTF_8);
        return archivo;
    }

    private long variacionSaldo(Movimiento movimiento, long idEstudiante) {
        if ("TRANSFERENCIA".equals(movimiento.getTipo())) {
            if (movimiento.getIdOrigen() != null && movimiento.getIdOrigen() == idEstudiante) {
                return -movementAmount(movimiento);
            }
            if (movimiento.getIdDestino() != null && movimiento.getIdDestino() == idEstudiante) {
                return movementAmount(movimiento);
            }
        } else if (movimiento.getIdDestino() != null && movimiento.getIdDestino() == idEstudiante) {
            if ("AJUSTE".equals(movimiento.getTipo()) && movimiento.getDescripcion() != null
                    && movimiento.getDescripcion().startsWith("[DEBITO]")) {
                return -movementAmount(movimiento);
            }
            return movementAmount(movimiento);
        }
        return 0;
    }

    private long movementAmount(Movimiento movimiento) {
        return movimiento.getMonto();
    }

    private String descripcionExtracto(Movimiento movimiento, long idEstudiante) throws IOException {
        if ("TRANSFERENCIA".equals(movimiento.getTipo())) {
            Long idRelacionado = movimiento.getIdOrigen() != null && movimiento.getIdOrigen() == idEstudiante
                    ? movimiento.getIdDestino() : movimiento.getIdOrigen();
            Usuario relacionado = idRelacionado == null ? null : usuarios.consultar(idRelacionado);
            if (movimiento.getIdOrigen() != null && movimiento.getIdOrigen() == idEstudiante) {
                return "Transferencia a " + (relacionado == null ? "cuenta " + idRelacionado : relacionado.getCorreo());
            }
            return "Transferencia recibida de " + (relacionado == null ? "cuenta " + idRelacionado : relacionado.getCorreo());
        }
        if ("AJUSTE".equals(movimiento.getTipo()) && movimiento.getDescripcion() != null
                && movimiento.getDescripcion().startsWith("[DEBITO]")) {
            return "Débito administrativo";
        }
        return movimiento.getDescripcion() == null ? movimiento.getTipo() : movimiento.getDescripcion();
    }

    private String formatoValor(long valor) {
        String cifra = String.format(Locale.US, "%,d", Math.abs(valor)).replace(',', '.');
        return (valor < 0 ? "-" : "+") + cifra;
    }

    private String formatoSaldo(long saldo) {
        String cifra = String.format(Locale.US, "%,d", Math.abs(saldo)).replace(',', '.');
        return (saldo < 0 ? "-" : "") + cifra;
    }

    private void revisarLogros(long idEstudiante) {
        try {
            new ServicioLogros().actualizarYListar(idEstudiante);
        } catch (IOException ignored) {
            // El inicio de sesión y la pantalla Mis logros vuelven a revisar los hitos.
        }
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
