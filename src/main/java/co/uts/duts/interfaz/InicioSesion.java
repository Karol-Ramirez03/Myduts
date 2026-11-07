/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package co.uts.duts.interfaz;


import co.uts.duts.datos.texto.EstudianteArchivoRepository;
import co.uts.duts.usuarios.Estudiante;
import co.uts.duts.usuarios.Usuario;

public class InicioSesion extends javax.swing.JFrame {

    public InicioSesion() {
        initComponents();
        setLocationRelativeTo(null);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        etiquetaTitulo = new javax.swing.JLabel();
        etiquetaUsuario = new javax.swing.JLabel();
        campoUsuario = new javax.swing.JTextField();
        etiquetaContrasena = new javax.swing.JLabel();
        campoContrasena = new javax.swing.JPasswordField();
        botonIngresar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("DUTS - Inicio de sesión");

        etiquetaTitulo.setFont(new java.awt.Font("SansSerif", 1, 24)); // NOI18N
        etiquetaTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        etiquetaTitulo.setText("Iniciar sesión");

        etiquetaUsuario.setText("Usuario");

        campoUsuario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoUsuarioActionPerformed(evt);
            }
        });

        etiquetaContrasena.setText("Contraseña");

        campoContrasena.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                campoContrasenaActionPerformed(evt);
            }
        });

        botonIngresar.setBackground(new java.awt.Color(52, 83, 31));
        botonIngresar.setForeground(new java.awt.Color(255, 255, 255));
        botonIngresar.setText("Ingresar");
        botonIngresar.addActionListener(evt -> ingresar());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(45, 45, 45)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(etiquetaTitulo, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(etiquetaUsuario)
                    .add(campoUsuario)
                    .add(etiquetaContrasena)
                    .add(campoContrasena)
                    .add(botonIngresar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .add(45, 45, 45))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(35, 35, 35)
                .add(etiquetaTitulo)
                .add(25, 25, 25)
                .add(etiquetaUsuario)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(campoUsuario, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 34, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(etiquetaContrasena)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(campoContrasena, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 34, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(24, 24, 24)
                .add(botonIngresar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(35, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void campoUsuarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoUsuarioActionPerformed
        ingresar();
    }//GEN-LAST:event_campoUsuarioActionPerformed

    private void campoContrasenaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_campoContrasenaActionPerformed
        ingresar();
    }//GEN-LAST:event_campoContrasenaActionPerformed

    private void ingresar() {
        try {
            Usuario usuario = new EstudianteArchivoRepository().autenticar(campoUsuario.getText().trim(), new String(campoContrasena.getPassword()));
            if (usuario == null) {
                javax.swing.JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos.", "No se pudo ingresar", javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (usuario instanceof Estudiante) {
                Estudiante estudiante = (Estudiante) usuario;
                if (estudiante.getCuenta().estaBajoMinimo()) {
                    javax.swing.JOptionPane.showMessageDialog(this,
                            "Alerta: tu saldo actual es " + estudiante.consultarSaldo() + " DUTS y está por debajo "
                                    + "del mínimo de " + estudiante.getCuenta().getSaldoMinimoAlerta() + " DUTS.",
                            "Alerta de saldo mínimo", javax.swing.JOptionPane.WARNING_MESSAGE);
                }
                try {
                    new co.uts.duts.logros.ServicioLogros().actualizarYListar(estudiante.getId());
                } catch (java.io.IOException ex) {
                    javax.swing.JOptionPane.showMessageDialog(this,
                            "No se pudieron actualizar tus logros: " + ex.getMessage(),
                            "Logros", javax.swing.JOptionPane.WARNING_MESSAGE);
                }
            }
            javax.swing.JFrame menu = "ADMIN".equals(usuario.getRol())
                    ? new MenuAdministrador(usuario)
                    : new MenuEstudiante(usuario);
            menu.setVisible(true);
            dispose();
        } catch (java.io.IOException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "No se pudo consultar el archivo de estudiantes: " + ex.getMessage(), "Error de archivo", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonIngresar;
    private javax.swing.JPasswordField campoContrasena;
    private javax.swing.JTextField campoUsuario;
    private javax.swing.JLabel etiquetaContrasena;
    private javax.swing.JLabel etiquetaTitulo;
    private javax.swing.JLabel etiquetaUsuario;
    // End of variables declaration//GEN-END:variables
}
