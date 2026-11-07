package co.uts.duts.interfaz;

import co.uts.duts.datos.texto.EstudianteArchivoRepository;
import co.uts.duts.usuarios.Usuario;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JPasswordField;

public class UsuariosAdministracionVentana extends JDialog {
    private final Usuario administrador;
    private final EstudianteArchivoRepository repositorio = new EstudianteArchivoRepository();

    public static void abrir(JFrame padre, Usuario administrador) {
        new UsuariosAdministracionVentana(padre, administrador).setVisible(true);
    }

    private UsuariosAdministracionVentana(JFrame padre, Usuario administrador) {
        super(padre, "Usuarios", true);
        this.administrador = administrador;
        initComponents();
        setLocationRelativeTo(padre);
        cargarUsuarios();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        etiquetaTitulo = new JLabel("Usuarios registrados", JLabel.CENTER);
        areaUsuarios = new JTextArea(16, 54);
        scrollUsuarios = new JScrollPane();
        scrollUsuarios.setViewportView(areaUsuarios);
        botonCrearAdministrador = new JButton("Crear administrador");
        botonCerrar = new JButton("Cerrar");
        panelAcciones = new JPanel();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(660, 450);
        setLayout(new java.awt.BorderLayout(8, 8));

        etiquetaTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        etiquetaTitulo.setForeground(new Color(52, 83, 31));
        etiquetaTitulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 10, 4, 10));
        areaUsuarios.setEditable(false);
        areaUsuarios.setFont(new Font("Monospaced", Font.PLAIN, 12));

        botonCrearAdministrador.setBackground(new Color(52, 83, 31));
        botonCrearAdministrador.setForeground(Color.WHITE);
        botonCrearAdministrador.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonCrearAdministradorActionPerformed(evt);
            }
        });
        botonCerrar.setBackground(new Color(52, 83, 31));
        botonCerrar.setForeground(Color.WHITE);
        botonCerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonCerrarActionPerformed(evt);
            }
        });

        panelAcciones.add(botonCrearAdministrador);
        panelAcciones.add(botonCerrar);
        add(etiquetaTitulo, java.awt.BorderLayout.NORTH);
        add(scrollUsuarios, java.awt.BorderLayout.CENTER);
        add(panelAcciones, java.awt.BorderLayout.SOUTH);
    }
    // </editor-fold>//GEN-END:initComponents

    private void botonCrearAdministradorActionPerformed(java.awt.event.ActionEvent evt) {
        mostrarFormulario();
    }

    private void botonCerrarActionPerformed(java.awt.event.ActionEvent evt) {
        dispose();
    }

    private void cargarUsuarios() {
        try {
            StringBuilder texto = new StringBuilder(String.format("%-5s %-12s %-26s %-18s%n",
                    "ID", "ROL", "NOMBRE", "CORREO"));
            for (Usuario usuario : repositorio.listar()) {
                texto.append(String.format("%-5d %-12s %-26s %-18s%n", usuario.getId(), usuario.getRol(),
                        usuario.getNombre(), usuario.getCorreo()));
            }
            areaUsuarios.setText(texto.toString());
            areaUsuarios.setCaretPosition(0);
        } catch (IOException ex) {
            mostrarError(ex);
        }
    }

    private void mostrarFormulario() {
        JTextField nombre = new JTextField();
        JTextField codigo = new JTextField();
        JTextField correo = new JTextField();
        JPasswordField contrasena = new JPasswordField();
        JPanel formulario = new JPanel(new GridLayout(0, 2, 8, 8));
        formulario.add(new JLabel("Nombre completo:")); formulario.add(nombre);
        formulario.add(new JLabel("Código de administrador:")); formulario.add(codigo);
        formulario.add(new JLabel("Correo:")); formulario.add(correo);
        formulario.add(new JLabel("Contraseña (mínimo 6 caracteres):")); formulario.add(contrasena);

        if (JOptionPane.showConfirmDialog(this, formulario, "Crear administrador",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            repositorio.registrarAdministrador(administrador.getId(), nombre.getText(), codigo.getText(),
                    correo.getText(), new String(contrasena.getPassword()));
            JOptionPane.showMessageDialog(this, "La cuenta administradora quedó creada.");
            cargarUsuarios();
        } catch (IOException | IllegalArgumentException ex) {
            mostrarError(ex);
        }
    }

    private void mostrarError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo completar la operación",
                JOptionPane.ERROR_MESSAGE);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea areaUsuarios;
    private javax.swing.JButton botonCerrar;
    private javax.swing.JButton botonCrearAdministrador;
    private javax.swing.JLabel etiquetaTitulo;
    private javax.swing.JScrollPane scrollUsuarios;
    private javax.swing.JPanel panelAcciones;
    // End of variables declaration//GEN-END:variables
}
