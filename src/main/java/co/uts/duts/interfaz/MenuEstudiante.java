package co.uts.duts.interfaz;

import co.uts.duts.usuarios.Usuario;

/** Menú principal disponible para estudiantes. */
public class MenuEstudiante extends MenuRol {
    public MenuEstudiante(Usuario estudiante) {
        super(estudiante, "Estudiante", new String[] {
            "Gestión DUTS", "Mi perfil", "Novedades", "Saldo", "Promedios"
        });
    }
}
