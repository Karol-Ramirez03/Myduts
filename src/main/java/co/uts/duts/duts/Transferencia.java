package co.uts.duts.duts;


import co.uts.duts.validacion.SaldoInsuficienteException;
import java.time.LocalDateTime;

/**
 * Envío de DUTS entre dos estudiantes. Exige que el PIN esté confirmado y que el origen tenga saldo.
 * Las transferencias NO cuentan como DUTS acumulados para quien los recibe (solo premios y ajustes).
 */
public class Transferencia extends Movimiento {
    private boolean pinConfirmado;

    /** Transferencia nueva (aún no aplicada). */
    public Transferencia(long idOrigen, long idDestino, int monto, String descripcion) {
        super(0, idOrigen, idDestino, monto, descripcion, LocalDateTime.now(), ESTADO_PENDIENTE);
    }

    /** Transferencia leída de la base de datos. */
    public Transferencia(long id, long idOrigen, long idDestino, int monto, String descripcion,
                         LocalDateTime fecha, String estado) {
        super(id, idOrigen, idDestino, monto, descripcion, fecha, estado);
        this.pinConfirmado = ESTADO_CONFIRMADO.equals(estado);
    }

    public boolean confirmarPin(String pin, Cuenta cuentaOrigen) {
        this.pinConfirmado = cuentaOrigen.validarPin(pin);
        return pinConfirmado;
    }

    public boolean isPinConfirmado() { return pinConfirmado; }

    @Override
    public void aplicar(Cuenta origen, Cuenta destino) throws SaldoInsuficienteException {
        if (!pinConfirmado) {
            throw new IllegalStateException("La transferencia requiere confirmar el PIN antes de aplicarse.");
        }
        if (origen == null || destino == null) {
            throw new IllegalArgumentException("Una transferencia necesita cuenta de origen y de destino.");
        }
        origen.retirar(monto);
        destino.depositar(monto);
        this.estado = ESTADO_CONFIRMADO;
    }

    @Override
    public String getTipo() { return "TRANSFERENCIA"; }
}
