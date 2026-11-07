package co.uts.duts.duts;

import co.uts.duts.datos.texto.EstudianteArchivoRepository;
import co.uts.duts.datos.texto.MovimientoArchivoRepository;
import co.uts.duts.usuarios.Estudiante;
import co.uts.duts.usuarios.Usuario;
import co.uts.duts.validacion.SaldoInsuficienteException;
import co.uts.duts.validacion.ValidacionException;
import co.uts.duts.validacion.Validador;
import java.io.IOException;
import java.util.List;

/** Operaciones de DUTS guardadas en los archivos de texto de la aplicación. */
public class ServicioDuts {
    private final EstudianteArchivoRepository usuarios = new EstudianteArchivoRepository();
    private final MovimientoArchivoRepository movimientos = new MovimientoArchivoRepository();

    public synchronized Transferencia transferir(long idOrigen, String correoDestino, int monto)
            throws IOException, ValidacionException, SaldoInsuficienteException {
        List<Usuario> lista = usuarios.listar();
        Estudiante origen = estudiantePorId(lista, idOrigen);
        Estudiante destino = null;
        for (Usuario usuario : lista) {
            if (usuario instanceof Estudiante
                    && usuario.getCorreo().equalsIgnoreCase(correoDestino.trim())) {
                destino = (Estudiante) usuario;
                break;
            }
        }
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

    private void verificarAdministrador(long idAdministrador) throws IOException, ValidacionException {
        Usuario usuario = usuarios.consultar(idAdministrador);
        if (usuario == null || !"ADMIN".equals(usuario.getRol()) || !usuario.isActivo()) {
            throw new ValidacionException("administrador", "Se requiere una cuenta de administrador activa.");
        }
    }
}
