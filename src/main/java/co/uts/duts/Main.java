package co.uts.duts;


import co.uts.duts.datos.texto.EstudianteArchivoRepository;
import co.uts.duts.interfaz.PantallaInicial;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            new EstudianteArchivoRepository().asegurarAdministradorInicial();
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo preparar el administrador inicial.", ex);
        }
        PantallaInicial pantalla = new PantallaInicial();
        pantalla.setVisible(true);
    }
}
