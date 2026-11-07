package co.uts.duts.duts;


import co.uts.duts.validacion.SaldoInsuficienteException;
import java.time.LocalDateTime;

/**
 * Movimiento de DUTS (clase abstracta). Cada tipo concreto (transferencia, recompensa, ajuste)
 * define cómo afecta las cuentas: eso es el polimorfismo de {@link #aplicar(Cuenta, Cuenta)}.
 */
public abstract class Movimiento {
    public static final String ESTADO_PENDIENTE = "PENDIENTE";
    public static final String ESTADO_CONFIRMADO = "CONFIRMADO";
    public static final String ESTADO_RECHAZADO = "RECHAZADO";

    private long id;
    protected int monto;
    protected String descripcion;
    protected LocalDateTime fecha;
    protected String estado;
    protected Long idOrigen;
    protected Long idDestino;

    protected Movimiento(long id, Long idOrigen, Long idDestino, int monto, String descripcion,
                         LocalDateTime fecha, String estado) {
        this.id = id;
        this.idOrigen = idOrigen;
        this.idDestino = idDestino;
        this.monto = monto;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.estado = estado;
    }

    /** Modifica las cuentas según el tipo de movimiento. {@code origen} o {@code destino} pueden ser null. */
    public abstract void aplicar(Cuenta origen, Cuenta destino) throws SaldoInsuficienteException;

    /** Tipo de movimiento guardado en el historial de texto. */
    public abstract String getTipo();

    public boolean validar() {
        return monto > 0 && fecha != null && estado != null;
    }

    public boolean estaEnPeriodo(LocalDateTime desde, LocalDateTime hasta) {
        return (desde == null || !fecha.isBefore(desde)) && (hasta == null || !fecha.isAfter(hasta));
    }

    public boolean esIngresoPara(long idEstudiante) {
        return idDestino != null && idDestino == idEstudiante;
    }

    public boolean esEgresoDe(long idEstudiante) {
        return idOrigen != null && idOrigen == idEstudiante && "TRANSFERENCIA".equals(getTipo());
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public int getMonto() { return monto; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDateTime getFecha() { return fecha; }
    public String getEstado() { return estado; }

    public void setEstado(String estado) {
        if (!ESTADO_PENDIENTE.equals(estado) && !ESTADO_CONFIRMADO.equals(estado) && !ESTADO_RECHAZADO.equals(estado)) {
            throw new IllegalArgumentException("Estado de movimiento no válido: " + estado);
        }
        this.estado = estado;
    }

    public Long getIdOrigen() { return idOrigen; }
    public Long getIdDestino() { return idDestino; }

    @Override
    public String toString() {
        return getTipo() + " de " + monto + " DUTS (" + estado + ") - " + descripcion;
    }
}
