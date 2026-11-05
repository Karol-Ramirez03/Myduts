package co.uts.duts;

public class Main {
    public static void main(String[] args) {
        BaseDatos.inicializar();
        javax.swing.SwingUtilities.invokeLater(() -> new PantallaInicial().setVisible(true));
    }
}
