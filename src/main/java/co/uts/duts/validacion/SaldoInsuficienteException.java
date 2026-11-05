package co.uts.duts.validacion;

/**
 * Se lanza cuando se intenta gastar o transferir más DUTS de los que hay
 * disponibles.
 */
public class SaldoInsuficienteException extends Exception {
    private static final long serialVersionUID = 1L;
    private final int saldoActual;
    private final int montoSolicitado;

    public SaldoInsuficienteException(int saldoActual, int montoSolicitado) {
        super("Saldo insuficiente: tienes " + saldoActual + " DUTS y necesitas " + montoSolicitado + ".");
        this.saldoActual = saldoActual;
        this.montoSolicitado = montoSolicitado;
    }

    public int getSaldoActual() {
        return saldoActual;
    }

    public int getMontoSolicitado() {
        return montoSolicitado;
    }

    public int getFaltante() {
        return montoSolicitado - saldoActual;
    }
}
