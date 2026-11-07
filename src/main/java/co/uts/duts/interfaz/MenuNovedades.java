package co.uts.duts.interfaz;

import co.uts.duts.usuarios.Usuario;
import javax.swing.JFrame;

/** Submenú de novedades del estudiante. Diseñado como formulario de NetBeans. */
public class MenuNovedades extends javax.swing.JFrame {
    private final JFrame pantallaAnterior;
    private final Usuario estudiante;

    public MenuNovedades(JFrame pantallaAnterior, Usuario estudiante) {
        this.pantallaAnterior = pantallaAnterior;
        this.estudiante = estudiante;
        initComponents();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent event) {
                cerrar();
            }
        });
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        etiquetaTitulo = new javax.swing.JLabel();
        botonVerNovedades = new javax.swing.JButton();
        botonMisEventos = new javax.swing.JButton();
        botonCerrar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("MyDUTS - Novedades");

        etiquetaTitulo.setFont(new java.awt.Font("SansSerif", 1, 25));
        etiquetaTitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        etiquetaTitulo.setForeground(new java.awt.Color(52, 83, 31));
        etiquetaTitulo.setText("Novedades y eventos");

        botonVerNovedades.setBackground(java.awt.Color.WHITE);
        botonVerNovedades.setFont(new java.awt.Font("SansSerif", 1, 16));
        botonVerNovedades.setForeground(new java.awt.Color(52, 83, 31));
        botonVerNovedades.setText("Ver novedades");
        botonVerNovedades.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonVerNovedadesActionPerformed(evt);
            }
        });

        botonMisEventos.setBackground(java.awt.Color.WHITE);
        botonMisEventos.setFont(new java.awt.Font("SansSerif", 1, 16));
        botonMisEventos.setForeground(new java.awt.Color(52, 83, 31));
        botonMisEventos.setText("Mis eventos");
        botonMisEventos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonMisEventosActionPerformed(evt);
            }
        });

        botonCerrar.setText("Cerrar");
        botonCerrar.setBackground(java.awt.Color.WHITE);
        botonCerrar.setForeground(new java.awt.Color(52, 83, 31));
        botonCerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botonCerrarActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(55, 55, 55)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(etiquetaTitulo, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                    .add(botonVerNovedades, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(botonMisEventos, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(botonCerrar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .add(55, 55, 55))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(38, 38, 38)
                .add(etiquetaTitulo)
                .add(32, 32, 32)
                .add(botonVerNovedades, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(botonMisEventos, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 54, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(24, 24, 24)
                .add(botonCerrar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(38, Short.MAX_VALUE))
        );
        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void botonVerNovedadesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonVerNovedadesActionPerformed
        abrirLista(false);
    }//GEN-LAST:event_botonVerNovedadesActionPerformed

    private void botonMisEventosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonMisEventosActionPerformed
        abrirLista(true);
    }//GEN-LAST:event_botonMisEventosActionPerformed

    private void botonCerrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonCerrarActionPerformed
        cerrar();
    }//GEN-LAST:event_botonCerrarActionPerformed

    private void abrirLista(boolean misEventos) {
        setVisible(false);
        new ListaNovedades(this, estudiante, misEventos).setVisible(true);
    }

    private void cerrar() {
        dispose();
        pantallaAnterior.setVisible(true);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonCerrar;
    private javax.swing.JButton botonMisEventos;
    private javax.swing.JButton botonVerNovedades;
    private javax.swing.JLabel etiquetaTitulo;
    // End of variables declaration//GEN-END:variables
}
