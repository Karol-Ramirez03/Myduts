package co.uts.duts.interfaz;

import co.uts.duts.novedades.Novedad;
import co.uts.duts.novedades.ServicioCalificaciones;
import co.uts.duts.usuarios.Usuario;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.io.IOException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JComboBox;

/** Lista eventos finalizados y permite que el estudiante los califique una vez. */
public class CalificacionEventosVentana extends JDialog {
    private final Usuario estudiante;
    private final ServicioCalificaciones servicio = new ServicioCalificaciones();

    public static void abrir(JFrame padre, Usuario estudiante) {
        new CalificacionEventosVentana(padre, estudiante).setVisible(true);
    }

    private CalificacionEventosVentana(JFrame padre, Usuario estudiante) {
        super(padre, "Calificación de eventos", true);
        this.estudiante = estudiante;
        initComponents();
        panelEventos.setLayout(new BoxLayout(panelEventos, BoxLayout.Y_AXIS));
        setLocationRelativeTo(padre);
        cargarEventos();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        etiquetaTitulo = new JLabel("Eventos que puedes calificar", JLabel.CENTER);
        panelEventos = new JPanel();
        scrollEventos = new JScrollPane();
        scrollEventos.setViewportView(panelEventos);
        botonCerrar = new JButton("Cerrar");
        panelAcciones = new JPanel();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(640, 480);
        setLayout(new BorderLayout(8, 8));
        etiquetaTitulo.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 21));
        etiquetaTitulo.setForeground(new Color(52, 83, 31));
        etiquetaTitulo.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));
        panelEventos.setBackground(new Color(245, 247, 242));
        panelEventos.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        botonCerrar.setBackground(new Color(52, 83, 31));
        botonCerrar.setForeground(Color.WHITE);
        botonCerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonCerrarActionPerformed(evt);
            }
        });
        panelAcciones.add(botonCerrar);
        add(etiquetaTitulo, BorderLayout.NORTH);
        add(scrollEventos, BorderLayout.CENTER);
        add(panelAcciones, BorderLayout.SOUTH);
    }
    // </editor-fold>//GEN-END:initComponents

    private void botonCerrarActionPerformed(java.awt.event.ActionEvent evt) {
        dispose();
    }

    private void cargarEventos() {
        panelEventos.removeAll();
        try {
            List<Novedad> eventos = servicio.eventosCalificables(estudiante.getId());
            if (eventos.isEmpty()) {
                JLabel mensaje = new JLabel("No tienes eventos finalizados pendientes de calificación.");
                mensaje.setAlignmentX(CENTER_ALIGNMENT);
                panelEventos.add(Box.createVerticalStrut(20));
                panelEventos.add(mensaje);
            }
            for (Novedad evento : eventos) {
                JButton tarjeta = new JButton("<html><div style='width:500px;padding:8px'><b>"
                        + escaparHtml(evento.getTitulo()) + "</b><br>Califica tu experiencia del evento</div></html>");
                tarjeta.setHorizontalAlignment(JButton.LEFT);
                tarjeta.setBackground(Color.WHITE);
                tarjeta.setFocusPainted(false);
                tarjeta.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                tarjeta.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(205, 216, 198), 1, true),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)));
                tarjeta.setAlignmentX(LEFT_ALIGNMENT);
                tarjeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));
                tarjeta.addActionListener(event -> abrirFormulario(evento));
                panelEventos.add(tarjeta);
                panelEventos.add(Box.createVerticalStrut(10));
            }
        } catch (IOException | IllegalArgumentException ex) {
            mostrarError(ex);
        }
        panelEventos.revalidate();
        panelEventos.repaint();
    }

    private void abrirFormulario(Novedad evento) {
        JComboBox<String> estrellas = new JComboBox<>(new String[] {
            "1 estrella", "2 estrellas", "3 estrellas", "4 estrellas", "5 estrellas"
        });
        JTextArea comentario = new JTextArea(5, 34);
        comentario.setLineWrap(true);
        comentario.setWrapStyleWord(true);
        JPanel panel = new JPanel(new BorderLayout(6, 8));
        JPanel cabecera = new JPanel(new java.awt.GridLayout(0, 1, 4, 4));
        cabecera.add(new JLabel("Evento: " + evento.getTitulo()));
        cabecera.add(new JLabel("Calificación:"));
        cabecera.add(estrellas);
        cabecera.add(new JLabel("Comentario (opcional):"));
        panel.add(cabecera, BorderLayout.NORTH);
        panel.add(new JScrollPane(comentario), BorderLayout.CENTER);
        if (JOptionPane.showConfirmDialog(this, panel, "Calificar evento",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            servicio.calificar(estudiante.getId(), evento.getId(), estrellas.getSelectedIndex() + 1,
                    comentario.getText());
            JOptionPane.showMessageDialog(this, "Gracias. Tu calificación quedó guardada.");
            cargarEventos();
        } catch (IOException | IllegalArgumentException ex) {
            mostrarError(ex);
        }
    }

    private String escaparHtml(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void mostrarError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo completar la acción",
                JOptionPane.ERROR_MESSAGE);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonCerrar;
    private javax.swing.JLabel etiquetaTitulo;
    private javax.swing.JPanel panelEventos;
    private javax.swing.JScrollPane scrollEventos;
    private javax.swing.JPanel panelAcciones;
    // End of variables declaration//GEN-END:variables
}
