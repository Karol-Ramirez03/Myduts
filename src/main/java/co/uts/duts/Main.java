package co.uts.duts;


import co.uts.duts.datos.BaseDatos;
import co.uts.duts.interfaz.PantallaInicial;
public class Main {
    public static void main(String[] args) {
         BaseDatos.inicializar();
         PantallaInicial pti= new PantallaInicial();
         pti.setVisible(true);
        // try {
        //     BaseDatos.inicializar();
        //     javax.swing.SwingUtilities.invokeLater(() -> new PantallaInicial().setVisible(true));
        // } catch (RuntimeException ex) {
        //     ex.printStackTrace();
        //     javax.swing.SwingUtilities.invokeLater(() -> javax.swing.JOptionPane.showMessageDialog(
        //             null,
        //             "No se pudo iniciar MyDUTS. Verifica que la carpeta de datos sea accesible.\n\n"
        //                     + ex.getMessage(),
        //             "Error al iniciar MyDUTS",
        //             javax.swing.JOptionPane.ERROR_MESSAGE));
        // }
    }
}
