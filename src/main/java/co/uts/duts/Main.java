package co.uts.duts;


import co.uts.duts.datos.BaseDatos;
import co.uts.duts.interfaz.PantallaInicial;
public class Main {
    public static void main(String[] args) {
         BaseDatos.inicializar();
         PantallaInicial pti= new PantallaInicial();
         pti.setVisible(true);

    }
}
