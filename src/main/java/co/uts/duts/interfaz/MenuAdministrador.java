package co.uts.duts.interfaz;

import co.uts.duts.usuarios.Usuario;

/** Menú principal disponible para administradores. */
public class MenuAdministrador extends MenuRol {
    public MenuAdministrador(Usuario administrador) {
        super(administrador, "Administrador", new String[] {
            "Usuarios", "Novedades y eventos", "Gestión DUTS", "Perfil"
        });
    }
}
