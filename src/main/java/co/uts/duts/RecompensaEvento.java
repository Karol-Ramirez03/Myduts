package co.uts.duts;

import java.time.LocalDateTime;

/**
 * DUTS ganados por participar en una novedad (evento). Cuentan como DUTS acumulados.
 */
public class RecompensaEvento extends Movimiento {
    private final long idNovedad;

    public RecompensaEvento(long idDestino, long idNovedad, int monto, String descripcion) {
        super(0, null, idDestino, monto, descripcion, LocalDateTime.now(), ESTADO_PENDIENTE);
        this.idNovedad = idNovedad;
    }

    public RecompensaEvento(long id, long idDestino, long idNovedad, int monto, String descripcion,
                            LocalDateTime fecha, String estado) {
        super(id, null, idDestino, monto, descripcion, fecha, estado);
        this.idNovedad = idNovedad;
    }

    public long getIdNovedad() { return idNovedad; }

    @Override
    public void aplicar(Cuenta origen, Cuenta destino) {
        if (destino == null) {
            throw new IllegalArgumentException("Una recompensa necesita cuenta de destino.");
        }
        destino.acumular(monto);
        this.estado = ESTADO_CONFIRMADO;
    }

    @Override
    public String getTipo() { return "RECOMPENSA_EVENTO"; }
}
