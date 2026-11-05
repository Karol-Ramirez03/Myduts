package co.uts.duts.duts;


import co.uts.duts.datos.ConexionSQLite;
import co.uts.duts.datos.EstudianteDAO;
import co.uts.duts.datos.MovimientoDAO;
import co.uts.duts.usuarios.Estudiante;
import co.uts.duts.validacion.SaldoInsuficienteException;
import co.uts.duts.validacion.ValidacionException;
import co.uts.duts.validacion.Validador;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Operaciones de DUTS que tocan varias tablas a la vez. Cada una corre en UNA transacción: o se guardan
 * todos los cambios (saldos y movimiento) o no se guarda ninguno (rollback si algo falla).
 */
public class ServicioDuts {
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final MovimientoDAO movimientoDAO = new MovimientoDAO();

    @FunctionalInterface
    private interface Operacion<T> {
        T ejecutar(Connection conexion) throws SQLException, ValidacionException, SaldoInsuficienteException;
    }

    private <T> T enTransaccion(Operacion<T> operacion)
            throws SQLException, ValidacionException, SaldoInsuficienteException {
        try (Connection conexion = ConexionSQLite.abrir()) {
            conexion.setAutoCommit(false);
            try {
                T resultado = operacion.ejecutar(conexion);
                conexion.commit();
                return resultado;
            } catch (SQLException | ValidacionException | SaldoInsuficienteException | RuntimeException ex) {
                conexion.rollback();
                throw ex;
            }
        }
    }

    /**
     * Envía DUTS de un estudiante a otro. Valida monto, PIN y saldo suficiente.
     * Los datos se leen de nuevo de la base de datos, así que siempre se usa el saldo real.
     */
    public Transferencia transferir(long idOrigen, long idDestino, int monto, String pin)
            throws SQLException, ValidacionException, SaldoInsuficienteException {
        return enTransaccion(conexion -> {
            Estudiante origen = estudianteDAO.consultar(conexion, idOrigen);
            Estudiante destino = estudianteDAO.consultar(conexion, idDestino);
            if (origen == null) throw new ValidacionException("origen", "El estudiante de origen no existe.");
            if (destino == null) throw new ValidacionException("destino", "El estudiante destino no existe.");

            Transferencia transferencia = origen.enviarDuts(destino, monto, pin);
            transferencia.aplicar(origen.getCuenta(), destino.getCuenta());

            estudianteDAO.actualizarCuenta(conexion, origen);
            estudianteDAO.actualizarCuenta(conexion, destino);
            movimientoDAO.crear(conexion, transferencia);
            return transferencia;
        });
    }

    /** Entrega los DUTS de una novedad a un estudiante (cuenta como acumulado). */
    public RecompensaEvento otorgarRecompensa(long idEstudiante, long idNovedad, int duts, String descripcion)
            throws SQLException, ValidacionException, SaldoInsuficienteException {
        return enTransaccion(conexion -> {
            Validador.montoValido(duts);
            Estudiante estudiante = estudianteDAO.consultar(conexion, idEstudiante);
            if (estudiante == null) throw new ValidacionException("estudiante", "El estudiante no existe.");

            RecompensaEvento recompensa = new RecompensaEvento(idEstudiante, idNovedad, duts, descripcion);
            recompensa.aplicar(null, estudiante.getCuenta());

            estudianteDAO.actualizarCuenta(conexion, estudiante);
            movimientoDAO.crear(conexion, recompensa);
            return recompensa;
        });
    }

    /** Un administrador suma DUTS a un estudiante con un motivo (cuenta como acumulado). */
    public AjusteAdmin ajustarSaldo(long idAdministrador, long idEstudiante, int monto, String motivo)
            throws SQLException, ValidacionException, SaldoInsuficienteException {
        return enTransaccion(conexion -> {
            Estudiante estudiante = estudianteDAO.consultar(conexion, idEstudiante);
            if (estudiante == null) throw new ValidacionException("estudiante", "El estudiante no existe.");
            Validador.montoValido(monto);
            Validador.noVacio(motivo, "motivo");

            AjusteAdmin ajuste = new AjusteAdmin(idAdministrador, idEstudiante, monto, motivo.trim());
            ajuste.aplicar(null, estudiante.getCuenta());

            estudianteDAO.actualizarCuenta(conexion, estudiante);
            movimientoDAO.crear(conexion, ajuste);
            return ajuste;
        });
    }

    /** Carga el historial de movimientos en la cuenta para poder calcular los promedios. */
    public void cargarHistorial(Estudiante estudiante) throws SQLException {
        estudiante.getCuenta().cargarMovimientos(movimientoDAO.listarPorEstudiante(estudiante.getId()));
    }
}
