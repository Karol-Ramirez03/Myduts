package co.uts.duts.duts;

import java.time.LocalDateTime;

/**
 * Ajuste manual de DUTS hecho por un administrador (suma DUTS a un estudiante). Cuenta como acumulado.
 * En la base de datos: el administrador va en id_estudiante_origen y el motivo en descripcion.
 */
public class AjusteAdmin extends Movimiento {

    public AjusteAdmin(long idAdministrador, long idDestino, int monto, String motivo) {
        super(0, idAdministrador, idDestino, monto, motivo, LocalDateTime.now(), ESTADO_PENDIENTE);
    }

    public AjusteAdmin(long id, long idAdministrador, long idDestino, int monto, String motivo,
                       LocalDateTime fecha, String estado) {
        super(id, idAdministrador, idDestino, monto, motivo, fecha, estado);
    }

    public long getIdAdministrador() { return idOrigen; }
    public String getMotivo() { return descripcion; }

    @Override
    public void aplicar(Cuenta origen, Cuenta destino) {
        if (destino == null) {
            throw new IllegalArgumentException("Un ajuste necesita la cuenta del estudiante.");
        }
        destino.acumular(monto);
        this.estado = ESTADO_CONFIRMADO;
    }

    @Override
    public String getTipo() { return "AJUSTE"; }
}
