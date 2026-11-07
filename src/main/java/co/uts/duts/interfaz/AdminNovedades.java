package co.uts.duts.interfaz;

import co.uts.duts.datos.texto.EstudianteArchivoRepository;
import co.uts.duts.novedades.CalificacionEvento;
import co.uts.duts.novedades.Novedad;
import co.uts.duts.novedades.ServicioCalificaciones;
import co.uts.duts.novedades.ServicioNovedades;
import co.uts.duts.usuarios.Usuario;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/** Administración de novedades en TXT, preparada como formulario editable de NetBeans. */
public class AdminNovedades extends javax.swing.JFrame {
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final JFrame menuAnterior;
    private final Usuario administrador;
    private final ServicioNovedades servicio = new ServicioNovedades();
    private final ServicioCalificaciones servicioCalificaciones = new ServicioCalificaciones();
    private final EstudianteArchivoRepository usuarios = new EstudianteArchivoRepository();
    private final JPanel panelTarjetas = new JPanel();

    public AdminNovedades(JFrame menuAnterior, Usuario administrador) {
        this.menuAnterior = menuAnterior;
        this.administrador = administrador;
        initComponents();
        setTitle("MyDUTS - Novedades y eventos");
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
        cargarNovedades();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        etiquetaTitulo = new javax.swing.JLabel();
        scrollNovedades = new javax.swing.JScrollPane();
        botonCrear = new javax.swing.JButton();
        botonVolver = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("MyDUTS - Novedades y eventos");

        etiquetaTitulo.setFont(new java.awt.Font("SansSerif", 1, 24));
        etiquetaTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        etiquetaTitulo.setForeground(new java.awt.Color(52, 83, 31));
        etiquetaTitulo.setText("Novedades y eventos");

        botonCrear.setFont(new java.awt.Font("SansSerif", 1, 14));
        botonCrear.setBackground(java.awt.Color.WHITE);
        botonCrear.setForeground(new java.awt.Color(52, 83, 31));
        botonCrear.setText("+ Crear novedad");
        botonCrear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonCrearActionPerformed(evt);
            }
        });

        botonVolver.setText("Volver");
        botonVolver.setBackground(java.awt.Color.WHITE);
        botonVolver.setForeground(new java.awt.Color(52, 83, 31));
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
                        .add(botonCrear, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 180, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(12, 12, 12)
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
                .add(scrollNovedades, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                .add(12, 12, 12)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(botonCrear, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(botonVolver, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(16, 16, 16))
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void botonCrearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonCrearActionPerformed
        mostrarFormulario(null);
    }//GEN-LAST:event_botonCrearActionPerformed

    private void botonVolverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonVolverActionPerformed
        volverAlMenu();
    }//GEN-LAST:event_botonVolverActionPerformed

    private void cargarNovedades() {
        panelTarjetas.removeAll();
        try {
            List<Novedad> novedades = servicio.consultarNovedades();
            if (novedades.isEmpty()) {
                JLabel vacio = new JLabel("Todavía no hay novedades registradas.");
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
            mostrarError(ex);
        }
        panelTarjetas.revalidate();
        panelTarjetas.repaint();
    }

    private JButton crearTarjeta(Novedad novedad) {
        String texto = "<html><div style='width:560px;padding:8px'><span style='font-size:16px'><b>"
                + escaparHtml(novedad.getTitulo()) + "</b></span><br><span style='color:#34531F'><b>"
                + novedad.getDutsRecompensa() + " DUTS</b></span><br>" + escaparHtml(novedad.getDescripcion())
                + "<br><span style='color:#666666'>" + novedad.getFechaInicio().format(FORMATO_FECHA)
                + " – " + novedad.getFechaFin().format(FORMATO_FECHA) + "</span></div></html>";
        JButton tarjeta = new JButton(texto);
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
        tarjeta.addActionListener(event -> mostrarAcciones(novedad));
        return tarjeta;
    }

    private void mostrarAcciones(Novedad novedad) {
        Object[] acciones = {"Detalles", "Editar", "Eliminar", "Crear novedad", "Cerrar"};
        int opcion = JOptionPane.showOptionDialog(this,
                "Seleccionaste «" + novedad.getTitulo() + "». ¿Qué deseas hacer?",
                "Administrar novedad", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, acciones, acciones[0]);
        switch (opcion) {
            case 0: mostrarDetalles(novedad); break;
            case 1: mostrarFormulario(novedad); break;
            case 2: eliminarNovedad(novedad); break;
            case 3: mostrarFormulario(null); break;
            default: break;
        }
    }

    private void mostrarDetalles(Novedad novedad) {
        try {
            int inscritos = servicio.contarInscritos(novedad.getId());
            String cupo = novedad.getCupoMaximo() == null ? "Sin límite"
                    : inscritos + " de " + novedad.getCupoMaximo();
            String detalle = novedad.getTitulo() + "\n\n" + novedad.getDescripcion()
                    + "\n\nRecompensa: " + novedad.getDutsRecompensa() + " DUTS"
                    + "\nInicio: " + novedad.getFechaInicio().format(FORMATO_FECHA)
                    + "\nFin: " + novedad.getFechaFin().format(FORMATO_FECHA)
                    + "\nInscritos: " + cupo + "\n\nCondiciones para obtener DUTS:\n"
                    + novedad.getCondiciones();
            List<CalificacionEvento> calificaciones = servicioCalificaciones.listarCalificaciones(
                    administrador.getId(), novedad.getId());
            detalle += "\n\nCalificaciones del evento:\n";
            if (calificaciones.isEmpty()) {
                detalle += "Aún no hay calificaciones.";
            } else {
                double suma = 0;
                for (CalificacionEvento calificacion : calificaciones) suma += calificacion.getEstrellas();
                detalle += String.format("Promedio: %.1f/5 (%d calificaciones)\n\n",
                        suma / calificaciones.size(), calificaciones.size());
                for (CalificacionEvento calificacion : calificaciones) {
                    Usuario autor = usuarios.consultar(calificacion.getIdEstudiante());
                    detalle += (autor == null ? "Estudiante " + calificacion.getIdEstudiante() : autor.getNombre())
                            + " — " + calificacion.getEstrellas() + "/5 estrellas\n"
                            + (calificacion.getComentario().isBlank() ? "Sin comentario" : calificacion.getComentario())
                            + "\n\n";
                }
            }
            JTextArea area = new JTextArea(detalle, 17, 48);
            area.setEditable(false);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            JOptionPane.showMessageDialog(this, new JScrollPane(area), "Detalle de la novedad",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException | IllegalArgumentException ex) {
            mostrarError(ex);
        }
    }

    private void mostrarFormulario(Novedad original) {
        JTextField titulo = new JTextField(original == null ? "" : original.getTitulo());
        JTextArea descripcion = new JTextArea(original == null ? "" : original.getDescripcion(), 3, 24);
        JTextArea condiciones = new JTextArea(original == null ? "" : original.getCondiciones(), 3, 24);
        JTextField inicio = new JTextField(original == null
                ? LocalDateTime.now().plusDays(1).withSecond(0).withNano(0).format(FORMATO_FECHA)
                : original.getFechaInicio().format(FORMATO_FECHA));
        JTextField fin = new JTextField(original == null
                ? LocalDateTime.now().plusDays(2).withSecond(0).withNano(0).format(FORMATO_FECHA)
                : original.getFechaFin().format(FORMATO_FECHA));
        JTextField recompensa = new JTextField(original == null ? "0" : Integer.toString(original.getDutsRecompensa()));
        JTextField cupo = new JTextField(original == null || original.getCupoMaximo() == null
                ? "" : original.getCupoMaximo().toString());
        JPanel campos = new JPanel(new java.awt.GridLayout(0, 2, 8, 8));
        campos.add(new JLabel("Título:")); campos.add(titulo);
        campos.add(new JLabel("Descripción:")); campos.add(new JScrollPane(descripcion));
        campos.add(new JLabel("Condiciones para obtener DUTS:")); campos.add(new JScrollPane(condiciones));
        campos.add(new JLabel("Inicio (aaaa-MM-dd HH:mm):")); campos.add(inicio);
        campos.add(new JLabel("Fin (aaaa-MM-dd HH:mm):")); campos.add(fin);
        campos.add(new JLabel("Recompensa DUTS:")); campos.add(recompensa);
        campos.add(new JLabel("Cupo (vacío = sin límite):")); campos.add(cupo);

        String encabezado = original == null ? "Crear novedad" : "Editar novedad";
        if (JOptionPane.showConfirmDialog(this, campos, encabezado,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            LocalDateTime fechaInicio = LocalDateTime.parse(inicio.getText().trim(), FORMATO_FECHA);
            LocalDateTime fechaFin = LocalDateTime.parse(fin.getText().trim(), FORMATO_FECHA);
            int duts = Integer.parseInt(recompensa.getText().trim());
            Integer maximo = cupo.getText().isBlank() ? null : Integer.valueOf(cupo.getText().trim());
            if (original == null) {
                servicio.crear(administrador.getId(), titulo.getText(), descripcion.getText(),
                        condiciones.getText(), fechaInicio, fechaFin, duts, maximo);
                JOptionPane.showMessageDialog(this, "Novedad creada.");
            } else {
                servicio.actualizar(administrador.getId(), original.getId(), titulo.getText(), descripcion.getText(),
                        condiciones.getText(), fechaInicio, fechaFin, duts, maximo);
                JOptionPane.showMessageDialog(this, "Novedad actualizada.");
            }
            cargarNovedades();
        } catch (IOException | RuntimeException ex) {
            mostrarError(ex);
        }
    }

    private void eliminarNovedad(Novedad novedad) {
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Eliminar «" + novedad.getTitulo() + "» y sus inscripciones?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirmacion != JOptionPane.YES_OPTION) return;
        try {
            servicio.eliminar(administrador.getId(), novedad.getId());
            cargarNovedades();
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

    private void volverAlMenu() {
        dispose();
        menuAnterior.setVisible(true);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonCrear;
    private javax.swing.JButton botonVolver;
    private javax.swing.JLabel etiquetaTitulo;
    private javax.swing.JScrollPane scrollNovedades;
    // End of variables declaration//GEN-END:variables
}
