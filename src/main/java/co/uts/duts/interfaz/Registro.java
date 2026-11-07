/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package co.uts.duts.interfaz;


import co.uts.duts.datos.texto.EstudianteArchivoRepository;
import java.awt.Color;

public class Registro extends javax.swing.JFrame {

    public Registro() {
        initComponents();
        setLocationRelativeTo(null);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        etiquetaTitulo = new javax.swing.JLabel();
        etiquetaNombre = new javax.swing.JLabel();
        campoNombre = new javax.swing.JTextField();
        etiquetaCodigo = new javax.swing.JLabel();
        campoCodigo = new javax.swing.JTextField();
        etiquetaCorreo = new javax.swing.JLabel();
        campoCorreo = new javax.swing.JTextField();
        etiquetaContrasena = new javax.swing.JLabel();
        campoContrasena = new javax.swing.JPasswordField();
        botonRegistrar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("DUTS - Registro de estudiante");

        etiquetaTitulo.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        etiquetaTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        etiquetaTitulo.setText("Crear cuenta");

        etiquetaNombre.setText("Nombre completo");

        etiquetaCodigo.setText("Código estudiantil");

        etiquetaCorreo.setText("Correo electrónico");

        etiquetaContrasena.setText("Contraseña");

        botonRegistrar.setBackground(new java.awt.Color(52, 83, 31));
        botonRegistrar.setForeground(new java.awt.Color(255, 255, 255));
        botonRegistrar.setText("Registrarme");
        botonRegistrar.addActionListener(evt -> registrar());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(45, 45, 45)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(etiquetaTitulo, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(etiquetaNombre)
                    .add(campoNombre)
                    .add(etiquetaCodigo)
                    .add(campoCodigo)
                    .add(etiquetaCorreo)
                    .add(campoCorreo)
                    .add(etiquetaContrasena)
                    .add(campoContrasena)
                    .add(botonRegistrar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .add(45, 45, 45))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(24, 24, 24)
                .add(etiquetaTitulo)
                .add(18, 18, 18)
                .add(etiquetaNombre)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(campoNombre, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 32, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(12, 12, 12)
                .add(etiquetaCodigo)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(campoCodigo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 32, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(12, 12, 12)
                .add(etiquetaCorreo)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(campoCorreo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 32, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(12, 12, 12)
                .add(etiquetaContrasena)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(campoContrasena, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 32, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(botonRegistrar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(24, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void registrar() {
        String nombre = campoNombre.getText().trim();
        String codigo = campoCodigo.getText().trim();
        String correo = campoCorreo.getText().trim();
        String contrasena = new String(campoContrasena.getPassword());
        if (nombre.isEmpty() || codigo.isEmpty() || correo.isEmpty() || contrasena.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(this, "Completa todos los campos.", "Validación", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!correo.contains("@")) {
            javax.swing.JOptionPane.showMessageDialog(this, "Ingresa un correo válido.", "Validación", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            EstudianteArchivoRepository repositorio = new EstudianteArchivoRepository();
            if (repositorio.existeCorreoOCodigo(correo, codigo)) {
                javax.swing.JOptionPane.showMessageDialog(this, "El correo o código ya están registrados.", "Registro duplicado", javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }
            repositorio.registrar(nombre, codigo, correo, contrasena);
            javax.swing.JOptionPane.showMessageDialog(this, "Cuenta creada. Ya puedes iniciar sesión.");
            dispose();
        } catch (java.io.IOException | IllegalArgumentException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "No se pudo guardar en el archivo: " + ex.getMessage(), "Error de archivo", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonRegistrar;
    private javax.swing.JTextField campoCodigo;
    private javax.swing.JPasswordField campoContrasena;
    private javax.swing.JTextField campoCorreo;
    private javax.swing.JTextField campoNombre;
    private javax.swing.JLabel etiquetaCodigo;
    private javax.swing.JLabel etiquetaContrasena;
    private javax.swing.JLabel etiquetaCorreo;
    private javax.swing.JLabel etiquetaNombre;
    private javax.swing.JLabel etiquetaTitulo;
    // End of variables declaration//GEN-END:variables
}
