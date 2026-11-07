package co.uts.duts.interfaz;

import co.uts.duts.datos.texto.PreguntaFrecuenteArchivoRepository;
import co.uts.duts.novedades.PreguntaFrecuente;
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

/** Vista de preguntas frecuentes y captura de nuevas preguntas para el administrador. */
public class PreguntasFrecuentesVentana extends JDialog {
    private final Usuario usuario;
    private final PreguntaFrecuenteArchivoRepository repositorio = new PreguntaFrecuenteArchivoRepository();
    private final JPanel lista = new JPanel();

    public static void abrir(JFrame padre, Usuario usuario) {
        new PreguntasFrecuentesVentana(padre, usuario).setVisible(true);
    }

    private PreguntasFrecuentesVentana(JFrame padre, Usuario usuario) {
        super(padre, "Preguntas frecuentes", true);
        this.usuario = usuario;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(620, 500);
        setLocationRelativeTo(padre);
        setLayout(new BorderLayout(10, 10));

        JLabel titulo = new JLabel("Preguntas frecuentes", JLabel.CENTER);
        titulo.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 22));
        titulo.setForeground(new Color(52, 83, 31));
        titulo.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));
        lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        lista.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        lista.setBackground(new Color(245, 247, 242));
        add(titulo, BorderLayout.NORTH);
        add(new JScrollPane(lista), BorderLayout.CENTER);

        JPanel acciones = new JPanel();
        if ("ADMIN".equals(usuario.getRol())) {
            JButton agregar = new JButton("Agregar pregunta");
            agregar.setBackground(new Color(52, 83, 31));
            agregar.setForeground(Color.WHITE);
            agregar.addActionListener(event -> agregarPregunta());
            acciones.add(agregar);
        }
        JButton cerrar = new JButton("Cerrar");
        cerrar.setBackground(new Color(52, 83, 31));
        cerrar.setForeground(Color.WHITE);
        cerrar.addActionListener(event -> dispose());
        acciones.add(cerrar);
        acciones.setBorder(BorderFactory.createEmptyBorder(0, 8, 10, 8));
        add(acciones, BorderLayout.SOUTH);
        cargarPreguntas();
    }

    private void cargarPreguntas() {
        lista.removeAll();
        try {
            List<PreguntaFrecuente> preguntas = repositorio.listar();
            if (preguntas.isEmpty()) {
                JLabel vacio = new JLabel("Aún no se han agregado preguntas.");
                vacio.setAlignmentX(CENTER_ALIGNMENT);
                lista.add(Box.createVerticalStrut(18));
                lista.add(vacio);
            }
            for (PreguntaFrecuente pregunta : preguntas) {
                JButton boton = new JButton("<html><div style='width:480px;padding:6px'><b>"
                        + escaparHtml(pregunta.getPregunta()) + "</b><br><span style='color:#52713e'>"
                        + "Presiona para ver la respuesta</span></div></html>");
                boton.setHorizontalAlignment(JButton.LEFT);
                boton.setBackground(Color.WHITE);
                boton.setFocusPainted(false);
                boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                boton.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(205, 216, 198), 1, true),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)));
                boton.setAlignmentX(LEFT_ALIGNMENT);
                boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
                boton.addActionListener(event -> mostrarRespuesta(pregunta));
                lista.add(boton);
                lista.add(Box.createVerticalStrut(10));
            }
        } catch (IOException ex) {
            mostrarError(ex);
        }
        lista.revalidate();
        lista.repaint();
    }

    private void mostrarRespuesta(PreguntaFrecuente pregunta) {
        JTextArea respuesta = new JTextArea(pregunta.getRespuesta(), 8, 42);
        respuesta.setEditable(false);
        respuesta.setLineWrap(true);
        respuesta.setWrapStyleWord(true);
        JOptionPane.showMessageDialog(this, new JScrollPane(respuesta), pregunta.getPregunta(),
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void agregarPregunta() {
        JTextArea pregunta = new JTextArea(3, 36);
        JTextArea respuesta = new JTextArea(7, 36);
        pregunta.setLineWrap(true);
        pregunta.setWrapStyleWord(true);
        respuesta.setLineWrap(true);
        respuesta.setWrapStyleWord(true);
        JPanel panel = new JPanel(new BorderLayout(6, 8));
        JPanel campos = new JPanel(new java.awt.GridLayout(0, 1, 4, 4));
        campos.add(new JLabel("Pregunta:"));
        campos.add(new JScrollPane(pregunta));
        campos.add(new JLabel("Respuesta:"));
        campos.add(new JScrollPane(respuesta));
        panel.add(campos, BorderLayout.CENTER);
        if (JOptionPane.showConfirmDialog(this, panel, "Agregar pregunta frecuente",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            repositorio.agregar(pregunta.getText(), respuesta.getText());
            cargarPreguntas();
            JOptionPane.showMessageDialog(this, "Pregunta frecuente guardada en TXT.");
        } catch (IOException | IllegalArgumentException ex) {
            mostrarError(ex);
        }
    }

    private String escaparHtml(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void mostrarError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
