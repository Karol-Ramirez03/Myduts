package co.uts.duts.interfaz;

import co.uts.duts.logros.Logro;
import co.uts.duts.logros.ServicioLogros;
import co.uts.duts.usuarios.Usuario;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class LogrosVentana extends JDialog {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static void abrir(JFrame padre, Usuario estudiante) {
        new LogrosVentana(padre, estudiante).setVisible(true);
    }

    private LogrosVentana(JFrame padre, Usuario estudiante) {
        super(padre, "Mis logros", true);
        initComponents();
        setLocationRelativeTo(padre);
        cargarLogros(padre, estudiante);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        etiquetaTitulo = new JLabel("Logros por DUTS acumulados", JLabel.CENTER);
        areaLogros = new JTextArea(16, 46);
        scrollLogros = new JScrollPane();
        scrollLogros.setViewportView(areaLogros);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(560, 430);
        setLayout(new BorderLayout(8, 8));
        etiquetaTitulo.setForeground(new Color(52, 83, 31));
        etiquetaTitulo.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 20));
        areaLogros.setEditable(false);
        areaLogros.setLineWrap(true);
        areaLogros.setWrapStyleWord(true);
        add(etiquetaTitulo, BorderLayout.NORTH);
        add(scrollLogros, BorderLayout.CENTER);
    }
    // </editor-fold>//GEN-END:initComponents

    private void cargarLogros(JFrame padre, Usuario estudiante) {
        try {
            List<Logro> logros = new ServicioLogros().actualizarYListar(estudiante.getId());
            StringBuilder texto = new StringBuilder("Mis logros desbloqueados\n\n");
            if (logros.isEmpty()) {
                texto.append("Aún no tienes logros. Acumula DUTS para desbloquearlos.\n");
            } else {
                for (Logro logro : logros) {
                    texto.append("🏆 ").append(logro.getNombre()).append(" — ")
                            .append(logro.getDutsRequeridos()).append(" DUTS\n")
                            .append(logro.getDescripcion()).append("\n")
                            .append("Desbloqueado: ").append(logro.getFechaObtencion().format(FECHA))
                            .append("\n\n");
                }
            }
            areaLogros.setText(texto.toString());
            areaLogros.setCaretPosition(0);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(padre, ex.getMessage(), "No se pudieron cargar los logros",
                    JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea areaLogros;
    private javax.swing.JLabel etiquetaTitulo;
    private javax.swing.JScrollPane scrollLogros;
    // End of variables declaration//GEN-END:variables
}
