package co.uts.duts;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Estudiante de las UTS. Tiene una {@link Cuenta} de DUTS (composición).
 */
public class Estudiante extends Usuario {
    private LocalDateTime fechaRegistro;
    private String rutaFoto;
    private final Cuenta cuenta;

    /** Estudiante leído de la base de datos. */
    public Estudiante(long id, String nombre, String codigo, String correo, String contrasenaHash,
                      boolean activo, LocalDateTime fechaRegistro, String rutaFoto, Cuenta cuenta) {
        super(id, nombre, codigo, correo, contrasenaHash, activo);
        this.fechaRegistro = fechaRegistro;
        this.rutaFoto = rutaFoto;
        this.cuenta = cuenta != null ? cuenta : new Cuenta();
        this.cuenta.setIdPropietario(id);
    }

    /** Estudiante nuevo: la contraseña se guarda cifrada. */
    public Estudiante(String nombre, String codigo, String correo, String contrasena) {
        this(0, nombre, codigo, correo, Seguridad.hash(contrasena), true, LocalDateTime.now(), null, new Cuenta());
    }

    @Override
    void setId(long id) {
        super.setId(id);
        cuenta.setIdPropietario(id);
    }

    // ---- DUTS y meta de graduación ----

    public int consultarSaldo() { return cuenta.getSaldoDuts(); }

    /** Avance hacia los 70.000 DUTS acumulados, entre 0.0 y 1.0. */
    public double progresoGraduacion() {
        return Math.min(1.0, (double) cuenta.getDutsAcumulados() / Cuenta.META_GRADUACION);
    }

    public int dutsFaltantes() {
        return Math.max(0, Cuenta.META_GRADUACION - cuenta.getDutsAcumulados());
    }

    /** Semestres que quedan del plazo de 2 semestres, contados desde el registro. */
    public int semestresRestantes() {
        int transcurridos = (int) (ChronoUnit.MONTHS.between(fechaRegistro, LocalDateTime.now()) / 6);
        return Math.max(0, Cuenta.PLAZO_SEMESTRES - transcurridos);
    }

    /** True si los DUTS acumulados van al menos al ritmo necesario para llegar a la meta dentro del plazo. */
    public boolean estaEnRitmo() {
        double plazoDias = Cuenta.PLAZO_SEMESTRES * 182.5;
        double dias = ChronoUnit.DAYS.between(fechaRegistro, LocalDateTime.now());
        double esperado = Cuenta.META_GRADUACION * Math.min(1.0, dias / plazoDias);
        return cuenta.getDutsAcumulados() >= esperado;
    }

    /**
     * Prepara una transferencia hacia otro estudiante validando datos, PIN y saldo.
     * No modifica saldos ni guarda nada: eso lo hace {@link ServicioDuts#transferir}.
     */
    public Transferencia enviarDuts(Estudiante destino, int monto, String pin)
            throws ValidacionException, SaldoInsuficienteException {
        Validador.montoValido(monto);
        Validador.pinValido(pin);
        if (destino == null) {
            throw new ValidacionException("destino", "El estudiante destino no existe.");
        }
        if (destino.getId() == getId()) {
            throw new ValidacionException("destino", "No puedes enviarte DUTS a ti mismo.");
        }
        Transferencia t = new Transferencia(getId(), destino.getId(), monto, "Transferencia a " + destino.getNombre());
        if (!t.confirmarPin(pin, cuenta)) {
            throw new ValidacionException("pin", "PIN incorrecto.");
        }
        if (!cuenta.tieneSaldoSuficiente(monto)) {
            throw new SaldoInsuficienteException(cuenta.getSaldoDuts(), monto);
        }
        return t;
    }

    // ---- polimorfismo ----

    @Override
    public String getRol() { return "ESTUDIANTE"; }

    @Override
    public List<String> getPermisos() {
        return Collections.unmodifiableList(Arrays.asList(
                "VER_SALDO", "ENVIAR_DUTS", "VER_MOVIMIENTOS", "VER_EVENTOS",
                "INSCRIBIRSE", "CALIFICAR_EVENTOS", "VER_PERFIL"));
    }

    public Cuenta getCuenta() { return cuenta; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public String getRutaFoto() { return rutaFoto; }
    public void setRutaFoto(String rutaFoto) { this.rutaFoto = rutaFoto; }
}
