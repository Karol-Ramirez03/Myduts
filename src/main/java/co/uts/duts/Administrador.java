package co.uts.duts;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Administrador / organizador. Gestiona novedades y puede ajustar saldos de estudiantes.
 * (Los métodos de CRUD de novedades se agregan cuando exista la clase Novedad.)
 */
public class Administrador extends Usuario {
    private LocalDateTime ultimoAcceso;

    public Administrador(long id, String nombre, String codigo, String correo, String contrasenaHash,
                         boolean activo, LocalDateTime ultimoAcceso) {
        super(id, nombre, codigo, correo, contrasenaHash, activo);
        this.ultimoAcceso = ultimoAcceso;
    }

    /** Prepara un ajuste que suma DUTS a un estudiante. Lo aplica y guarda {@link ServicioDuts#ajustarSaldo}. */
    public AjusteAdmin ajustarSaldo(Estudiante estudiante, int monto, String motivo) throws ValidacionException {
        Validador.montoValido(monto);
        Validador.noVacio(motivo, "motivo");
        if (estudiante == null) {
            throw new ValidacionException("estudiante", "El estudiante no existe.");
        }
        return new AjusteAdmin(getId(), estudiante.getId(), monto, motivo.trim());
    }

    @Override
    public String getRol() { return "ADMIN"; }

    @Override
    public List<String> getPermisos() {
        return Collections.unmodifiableList(Arrays.asList(
                "GESTIONAR_NOVEDADES", "AJUSTAR_SALDOS", "VER_ESTUDIANTES", "VER_PERFIL"));
    }

    public LocalDateTime getUltimoAcceso() { return ultimoAcceso; }
    public void setUltimoAcceso(LocalDateTime ultimoAcceso) { this.ultimoAcceso = ultimoAcceso; }
}
