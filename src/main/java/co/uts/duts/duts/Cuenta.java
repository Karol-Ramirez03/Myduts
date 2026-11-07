package co.uts.duts.duts;


import co.uts.duts.usuarios.Estudiante;
import co.uts.duts.validacion.SaldoInsuficienteException;
import co.uts.duts.validacion.ValidacionException;
import co.uts.duts.validacion.Validador;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Cuenta de DUTS de un estudiante (composición: no existe sin su {@link Estudiante}).
 * Sus datos se guardan junto al estudiante en el archivo de texto.
 *
 * - saldoDuts: DUTS disponibles ahora (sube y baja).
 * - dutsAcumulados: DUTS ganados en total (premios y ajustes); nunca baja. Sirve para la meta de
 *   graduación, el ranking top 5 y el ritmo de los 2 semestres.
 */
public class Cuenta {
    public static final int META_GRADUACION = 70000;
    public static final int PLAZO_SEMESTRES = 2;
    public static final int SALDO_MINIMO_POR_DEFECTO = 5000;

    private long idPropietario;
    private int saldoDuts;
    private int dutsAcumulados;
    private int saldoMinimoAlerta;
    private String pin;
    private final List<Movimiento> movimientos = new ArrayList<>();

    public Cuenta() {
        this(0, 0, SALDO_MINIMO_POR_DEFECTO, null);
    }

    public Cuenta(int saldoDuts, int dutsAcumulados, int saldoMinimoAlerta, String pin) {
        this.saldoDuts = saldoDuts;
        this.dutsAcumulados = dutsAcumulados;
        this.saldoMinimoAlerta = saldoMinimoAlerta;
        this.pin = pin;
    }

    // ---- operaciones sobre el saldo ----

    /** Suma DUTS al saldo sin contarlos como acumulados (p. ej. una transferencia recibida). */
    public void depositar(int monto) {
        if (monto <= 0) throw new IllegalArgumentException("El monto debe ser mayor que cero.");
        saldoDuts += monto;
    }

    /** Suma DUTS al saldo y a los acumulados (premios de eventos y ajustes). */
    public void acumular(int monto) {
        depositar(monto);
        dutsAcumulados += monto;
    }

    public void retirar(int monto) throws SaldoInsuficienteException {
        if (monto <= 0) throw new IllegalArgumentException("El monto debe ser mayor que cero.");
        if (!tieneSaldoSuficiente(monto)) throw new SaldoInsuficienteException(saldoDuts, monto);
        saldoDuts -= monto;
    }

    public boolean tieneSaldoSuficiente(int monto) {
        return saldoDuts >= monto;
    }

    public boolean estaBajoMinimo() {
        return saldoDuts < saldoMinimoAlerta;
    }

    // ---- PIN de confirmación ----

    public boolean tienePin() { return pin != null; }

    public void establecerPin(String pin) throws ValidacionException {
        Validador.pinValido(pin);
        this.pin = pin;
    }

    public boolean validarPin(String pin) {
        return this.pin != null && pin != null && this.pin.equals(pin);
    }

    // ---- promedios de DUTS recibidos por periodo ----

    public void cargarMovimientos(List<Movimiento> lista) {
        movimientos.clear();
        movimientos.addAll(lista);
    }

    public List<Movimiento> getMovimientos() { return Collections.unmodifiableList(movimientos); }

    public double promedioSemana() { return promedio(ChronoUnit.WEEKS, 1); }
    public double promedioMes() { return promedio(ChronoUnit.MONTHS, 1); }
    public double promedioAnio() { return promedio(ChronoUnit.YEARS, 1); }
    public double promedioSemestre() { return promedio(ChronoUnit.MONTHS, 6); }

    /** Total de DUTS recibidos dividido entre los periodos transcurridos desde el primer ingreso. */
    private double promedio(ChronoUnit unidad, int tamanoPeriodo) {
        long total = 0;
        LocalDateTime primero = null;
        for (Movimiento m : movimientos) {
            if (m.esIngresoPara(idPropietario) && Movimiento.ESTADO_CONFIRMADO.equals(m.getEstado())
                    && (m.getDescripcion() == null || !m.getDescripcion().startsWith("[DEBITO]"))) {
                total += m.getMonto();
                if (primero == null || m.getFecha().isBefore(primero)) primero = m.getFecha();
            }
        }
        if (primero == null) return 0;
        long periodos = Math.max(1, unidad.between(primero, LocalDateTime.now()) / tamanoPeriodo + 1);
        return (double) total / periodos;
    }

    // ---- getters y setters ----

    public int getSaldoDuts() { return saldoDuts; }
    public int getDutsAcumulados() { return dutsAcumulados; }
    public int getSaldoMinimoAlerta() { return saldoMinimoAlerta; }
    public String getPin() { return pin; }
    public long getIdPropietario() { return idPropietario; }
    public void setIdPropietario(long idPropietario) { this.idPropietario = idPropietario; }

    public void establecerSaldoDuts(int saldoDuts) {
        if (saldoDuts < 0) throw new IllegalArgumentException("El saldo no puede ser negativo.");
        this.saldoDuts = saldoDuts;
    }

    public void establecerDutsAcumulados(int dutsAcumulados) {
        if (dutsAcumulados < 0) throw new IllegalArgumentException("Los DUTS acumulados no pueden ser negativos.");
        this.dutsAcumulados = dutsAcumulados;
    }

    public void setSaldoMinimoAlerta(int saldoMinimoAlerta) {
        if (saldoMinimoAlerta < 0) throw new IllegalArgumentException("El saldo mínimo no puede ser negativo.");
        this.saldoMinimoAlerta = saldoMinimoAlerta;
    }
}
