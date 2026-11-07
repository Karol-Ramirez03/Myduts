package co.uts.duts.interfaz;

import co.uts.duts.logros.Logro;
import co.uts.duts.logros.ServicioLogros;
import co.uts.duts.usuarios.Usuario;
import java.awt.BorderLayout;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/** Muestra los hitos de DUTS que ya desbloqueó el estudiante. */
public class LogrosVentana extends JDialog {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static void abrir(JFrame padre, Usuario estudiante) {
        new LogrosVentana(padre, estudiante).setVisible(true);
    }

    private LogrosVentana(JFrame padre, Usuario estudiante) {
        super(padre, "Mis logros", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(560, 430);
        setLocationRelativeTo(padre);
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
            JTextArea area = new JTextArea(texto.toString(), 16, 46);
            area.setEditable(false);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            add(new JLabel("Logros por DUTS acumulados", JLabel.CENTER), BorderLayout.NORTH);
            add(new JScrollPane(area), BorderLayout.CENTER);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(padre, ex.getMessage(), "No se pudieron cargar los logros",
                    JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }
}
