package co.uts.duts.interfaz;

import co.uts.duts.novedades.Novedad;
import co.uts.duts.novedades.ServicioNovedades;
import co.uts.duts.usuarios.Usuario;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JPanel;

/** Formulario de NetBeans para las listas de novedades y eventos inscritos. */
public class ListaNovedades extends javax.swing.JFrame {
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final MenuNovedades menuAnterior;
    private final Usuario estudiante;
    private final boolean mostrarInscripciones;
    private final ServicioNovedades servicio = new ServicioNovedades();
    private final JPanel panelTarjetas = new JPanel();

    public ListaNovedades(MenuNovedades menuAnterior, Usuario estudiante, boolean mostrarInscripciones) {
        this.menuAnterior = menuAnterior;
        this.estudiante = estudiante;
        this.mostrarInscripciones = mostrarInscripciones;
        initComponents();
        setTitle(mostrarInscripciones ? "MyDUTS - Mis eventos" : "MyDUTS - Ver novedades");
        etiquetaTitulo.setText(mostrarInscripciones ? "Mis eventos" : "Novedades disponibles");
        panelTarjetas.setLayout(new BoxLayout(panelTarjetas, BoxLayout.Y_AXIS));
        panelTarjetas.setBackground(new Color(245, 247, 242));
        panelTarjetas.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        scrollNovedades.setViewportView(panelTarjetas);
        setLocationRelativeTo(menuAnterior);
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                volverAlMenu();
            }
        });
        cargarTarjetas();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        etiquetaTitulo = new javax.swing.JLabel();
        scrollNovedades = new javax.swing.JScrollPane();
        botonVolver = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("MyDUTS - Novedades");

        etiquetaTitulo.setFont(new java.awt.Font("SansSerif", 1, 24));
        etiquetaTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        etiquetaTitulo.setText("Novedades disponibles");

        botonVolver.setText("Volver");
        botonVolver.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonVolverActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(22, 22, 22)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(etiquetaTitulo, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 636, Short.MAX_VALUE)
                    .add(scrollNovedades)
                    .add(layout.createSequentialGroup()
                        .add(botonVolver, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 120, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(0, 0, Short.MAX_VALUE)))
                .add(22, 22, 22))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(18, 18, 18)
                .add(etiquetaTitulo)
                .add(14, 14, 14)
                .add(scrollNovedades, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 390, Short.MAX_VALUE)
                .add(12, 12, 12)
                .add(botonVolver, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(16, 16, 16))
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void botonVolverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonVolverActionPerformed
        volverAlMenu();
    }//GEN-LAST:event_botonVolverActionPerformed

    private void cargarTarjetas() {
        panelTarjetas.removeAll();
        try {
            List<Novedad> novedades = mostrarInscripciones
                    ? servicio.misInscripciones(estudiante.getId()) : servicio.consultarNovedades();
            if (novedades.isEmpty()) {
                String mensaje = mostrarInscripciones
                        ? "Todavía no tienes eventos inscritos." : "Todavía no hay novedades publicadas.";
                JLabel vacio = new JLabel(mensaje, javax.swing.SwingConstants.CENTER);
                vacio.setFont(new java.awt.Font("SansSerif", 0, 16));
                vacio.setAlignmentX(CENTER_ALIGNMENT);
                panelTarjetas.add(Box.createVerticalStrut(26));
                panelTarjetas.add(vacio);
            } else {
                for (Novedad novedad : novedades) {
                    panelTarjetas.add(crearTarjeta(novedad));
                    panelTarjetas.add(Box.createVerticalStrut(12));
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo cargar la lista",
                    JOptionPane.ERROR_MESSAGE);
        }
        panelTarjetas.revalidate();
        panelTarjetas.repaint();
    }

    private JButton crearTarjeta(Novedad novedad) {
        String fechas = novedad.getFechaInicio().format(FORMATO_FECHA)
                + " – " + novedad.getFechaFin().format(FORMATO_FECHA);
        JButton tarjeta = new JButton("<html><div style='width:560px;padding:8px'>"
                + "<span style='font-size:16px'><b>" + escaparHtml(novedad.getTitulo()) + "</b></span>"
                + "<br><span style='color:#34531F'><b>Recompensa: " + novedad.getDutsRecompensa()
                + " DUTS</b></span><br><span>" + escaparHtml(novedad.getDescripcion())
                + "</span><br><span style='color:#666666'>" + fechas + "</span></div></html>");
        tarjeta.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setForeground(new Color(45, 55, 42));
        tarjeta.setFont(new java.awt.Font("SansSerif", 0, 14));
        tarjeta.setFocusPainted(false);
        tarjeta.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(205, 216, 198), 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        tarjeta.setAlignmentX(LEFT_ALIGNMENT);
        tarjeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 118));
        tarjeta.addActionListener(event -> seleccionarNovedad(novedad));
        return tarjeta;
    }

    private void seleccionarNovedad(Novedad novedad) {
        try {
            int inscritos = servicio.contarInscritos(novedad.getId());
            String cupos = novedad.getCupoMaximo() == null ? "Sin límite"
                    : inscritos + " de " + novedad.getCupoMaximo() + " inscritos";
            String detalle = novedad.getDescripcion() + "\n\nRecompensa: "
                    + novedad.getDutsRecompensa() + " DUTS\nFechas: "
                    + novedad.getFechaInicio().format(FORMATO_FECHA) + " – "
                    + novedad.getFechaFin().format(FORMATO_FECHA) + "\nCupo: " + cupos
                    + "\n\nCondiciones para obtener los DUTS:\n" + novedad.getCondiciones();
            if (mostrarInscripciones) {
                int opcion = confirmarAccion(detalle + "\n\n¿Deseas cancelar tu inscripción?",
                        "Detalle de mi evento");
                if (opcion == JOptionPane.YES_OPTION) {
                    if (servicio.cancelarInscripcion(estudiante.getId(), novedad.getId())) {
                        JOptionPane.showMessageDialog(this, "Se canceló tu inscripción.");
                        cargarTarjetas();
                    }
                }
            } else {
                if (servicio.estaInscrito(estudiante.getId(), novedad.getId())) {
                    mostrarDetalle(detalle + "\n\nYa estás inscrito.", "Detalle de la novedad");
                    return;
                }
                int opcion = confirmarAccion(detalle
                        + "\n\n¿Deseas inscribirte y confirmas que aceptas las condiciones?",
                        "Detalle de la novedad");
                if (opcion == JOptionPane.YES_OPTION) {
                    servicio.inscribirse(estudiante.getId(), novedad.getId());
                    JOptionPane.showMessageDialog(this, "Tu inscripción quedó registrada.");
                }
            }
        } catch (IOException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo completar la acción",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private int confirmarAccion(String texto, String titulo) {
        JTextArea area = crearAreaDetalle(texto);
        return JOptionPane.showConfirmDialog(this, new JScrollPane(area), titulo,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    }

    private void mostrarDetalle(String texto, String titulo) {
        JOptionPane.showMessageDialog(this, new JScrollPane(crearAreaDetalle(texto)), titulo,
                JOptionPane.INFORMATION_MESSAGE);
    }

    private JTextArea crearAreaDetalle(String texto) {
        JTextArea area = new JTextArea(texto, 16, 44);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    private String escaparHtml(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void volverAlMenu() {
        dispose();
        menuAnterior.setVisible(true);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonVolver;
    private javax.swing.JLabel etiquetaTitulo;
    private javax.swing.JScrollPane scrollNovedades;
    // End of variables declaration//GEN-END:variables
}
