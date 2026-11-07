package co.uts.duts.interfaz;

import co.uts.duts.datos.texto.EstudianteArchivoRepository;
import co.uts.duts.duts.ServicioDuts;
import co.uts.duts.duts.Transferencia;
import co.uts.duts.usuarios.Estudiante;
import co.uts.duts.usuarios.Usuario;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/** Estructura común para los menús de cada rol y sus funciones compartidas. */
abstract class MenuRol extends JFrame {
    private final Usuario usuario;
    private final ServicioDuts servicioDuts = new ServicioDuts();

    MenuRol(Usuario usuario, String titulo, String[] opciones) {
        this.usuario = usuario;
        setTitle("MyDUTS - " + titulo);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 430);
        setLocationRelativeTo(null);

        JLabel encabezado = new JLabel("Hola, " + usuario.getNombre(), JLabel.CENTER);
        encabezado.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 22));

        JPanel botones = new JPanel(new GridLayout(opciones.length + 1, 1, 10, 10));
        botones.setBorder(BorderFactory.createEmptyBorder(22, 70, 22, 70));
        for (String opcion : opciones) {
            JButton boton = new JButton(opcion);
            boton.addActionListener(event -> abrirOpcion(opcion));
            botones.add(boton);
        }
        JButton cerrarSesion = new JButton("Cerrar sesión");
        cerrarSesion.addActionListener(event -> cerrarSesion());
        botones.add(cerrarSesion);

        add(encabezado, BorderLayout.NORTH);
        add(botones, BorderLayout.CENTER);
    }

    private void abrirOpcion(String opcion) {
        if ("Mi perfil".equals(opcion) || "Perfil".equals(opcion)) {
            mostrarPerfil();
        } else if ("Gestión DUTS".equals(opcion) && "ADMIN".equals(usuario.getRol())) {
            abrirGestionDuts();
        } else if ("Gestión DUTS".equals(opcion)) {
            enviarDuts();
        } else if ("Saldo".equals(opcion)) {
            mostrarSaldo();
        } else if ("Promedios".equals(opcion)) {
            mostrarPromedios();
        } else {
            JOptionPane.showMessageDialog(this, "La sección «" + opcion + "» se implementará después.",
                    "MyDUTS", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void mostrarPerfil() {
        JTextField nombre = new JTextField(usuario.getNombre(), 24);
        JTextField correo = new JTextField(usuario.getCorreo(), 24);
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("ID:"));
        panel.add(new JLabel(String.valueOf(usuario.getId())));
        panel.add(new JLabel("Rol:"));
        panel.add(new JLabel(usuario.getRol()));
        panel.add(new JLabel("Código:"));
        panel.add(new JLabel(usuario.getCodigo()));
        panel.add(new JLabel("Nombre:"));
        panel.add(nombre);
        panel.add(new JLabel("Correo:"));
        panel.add(correo);

        int opcion = JOptionPane.showConfirmDialog(this, panel, "Mi perfil",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcion == JOptionPane.OK_OPTION) {
            try {
                EstudianteArchivoRepository repo = new EstudianteArchivoRepository();
                if (!repo.actualizarPerfil(usuario.getId(), nombre.getText(), correo.getText())) {
                    throw new IllegalArgumentException("Revisa el nombre y el correo ingresados.");
                }
                usuario.actualizarPerfil(nombre.getText(), correo.getText());
                JOptionPane.showMessageDialog(this, "Perfil actualizado.");
            } catch (IOException | IllegalArgumentException ex) {
                mostrarError(ex);
            }
        }
    }

    private void mostrarSaldo() {
        try {
            Estudiante estudiante = servicioDuts.consultarEstudiante(usuario.getId());
            if (estudiante == null) {
                throw new IOException("No se encontró la cuenta del estudiante.");
            }
            JOptionPane.showMessageDialog(this, "Saldo disponible: "
                    + estudiante.consultarSaldo() + " DUTS", "Mi saldo", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            mostrarError(ex);
        }
    }

    private void mostrarPromedios() {
        try {
            Estudiante estudiante = servicioDuts.consultarEstudiante(usuario.getId());
            if (estudiante == null) {
                throw new IOException("No se encontró la cuenta del estudiante.");
            }
            servicioDuts.cargarHistorial(estudiante);
            String texto = String.format("Promedio de DUTS recibidos:%n%nSemana: %.2f DUTS%nMes: %.2f DUTS"
                            + "%nSemestre: %.2f DUTS%nAño: %.2f DUTS",
                    estudiante.getCuenta().promedioSemana(), estudiante.getCuenta().promedioMes(),
                    estudiante.getCuenta().promedioSemestre(), estudiante.getCuenta().promedioAnio());
            JOptionPane.showMessageDialog(this, texto, "Promedios de DUTS", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            mostrarError(ex);
        }
    }

    private void enviarDuts() {
        JTextField destino = new JTextField();
        JTextField monto = new JTextField();
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("Correo destino:"));
        panel.add(destino);
        panel.add(new JLabel("Cantidad de DUTS:"));
        panel.add(monto);

        if (JOptionPane.showConfirmDialog(this, panel, "Enviar DUTS",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            int cantidad = Integer.parseInt(monto.getText().trim());
            Transferencia transferencia = servicioDuts.transferir(usuario.getId(), destino.getText(), cantidad);
            JOptionPane.showMessageDialog(this, "Enviaste " + transferencia.getMonto()
                    + " DUTS correctamente.", "Transferencia realizada", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser un número entero.",
                    "Dato inválido", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            mostrarError(ex);
        }
    }

    private void abrirGestionDuts() {
        try {
            List<Estudiante> estudiantes = servicioDuts.listarEstudiantes();
            if (estudiantes.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Todavía no hay estudiantes registrados.");
                return;
            }
            JComboBox<Estudiante> selector = new JComboBox<>(estudiantes.toArray(new Estudiante[0]));
            selector.setRenderer((list, estudiante, index, seleccionado, foco) -> {
                JLabel etiqueta = new JLabel(estudiante == null ? "" : estudiante.getNombre()
                        + " — " + estudiante.getCodigo());
                if (seleccionado) {
                    etiqueta.setOpaque(true);
                    etiqueta.setBackground(list.getSelectionBackground());
                    etiqueta.setForeground(list.getSelectionForeground());
                }
                return etiqueta;
            });
            JTextField cantidad = new JTextField();
            JTextField motivo = new JTextField("Ajuste administrativo");
            JPanel campos = new JPanel(new GridLayout(0, 2, 8, 8));
            campos.add(new JLabel("Estudiante:"));
            campos.add(selector);
            campos.add(new JLabel("Cantidad / saldo nuevo:"));
            campos.add(cantidad);
            campos.add(new JLabel("Motivo:"));
            campos.add(motivo);

            JDialog dialogo = new JDialog(this, "Gestión de DUTS", true);
            JPanel contenido = new JPanel(new BorderLayout(8, 8));
            contenido.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            contenido.add(campos, BorderLayout.CENTER);
            JPanel acciones = new JPanel(new GridLayout(0, 2, 8, 8));
            agregarBoton(acciones, "Consultar saldo", () -> consultarSaldoSeleccionado(selector));
            agregarBoton(acciones, "Listar saldos", () -> listarSaldos());
            agregarBoton(acciones, "Asignar DUTS", () -> asignarDuts(selector, cantidad, motivo));
            agregarBoton(acciones, "Actualizar saldo", () -> actualizarSaldo(selector, cantidad));
            agregarBoton(acciones, "Eliminar saldo", () -> eliminarSaldo(selector));
            agregarBoton(acciones, "Cerrar", dialogo::dispose);
            contenido.add(acciones, BorderLayout.SOUTH);
            dialogo.setContentPane(contenido);
            dialogo.pack();
            dialogo.setLocationRelativeTo(this);
            dialogo.setVisible(true);
        } catch (IOException ex) {
            mostrarError(ex);
        }
    }

    private void agregarBoton(JPanel panel, String texto, Runnable accion) {
        JButton boton = new JButton(texto);
        boton.addActionListener(event -> accion.run());
        panel.add(boton);
    }

    private void consultarSaldoSeleccionado(JComboBox<Estudiante> selector) {
        Estudiante elegido = (Estudiante) selector.getSelectedItem();
        if (elegido != null) {
            JOptionPane.showMessageDialog(this, elegido.getNombre() + " tiene "
                    + elegido.consultarSaldo() + " DUTS.", "Saldo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void listarSaldos() {
        try {
            StringBuilder texto = new StringBuilder("Saldos de estudiantes\n\n");
            for (Estudiante estudiante : servicioDuts.listarEstudiantes()) {
                texto.append(estudiante.getCodigo()).append(" — ").append(estudiante.getNombre())
                        .append(": ").append(estudiante.consultarSaldo()).append(" DUTS\n");
            }
            JTextArea area = new JTextArea(texto.toString(), 16, 42);
            area.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(area), "Saldos", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            mostrarError(ex);
        }
    }

    private void asignarDuts(JComboBox<Estudiante> selector, JTextField cantidad, JTextField motivo) {
        Estudiante elegido = (Estudiante) selector.getSelectedItem();
        if (elegido == null) return;
        try {
            int monto = Integer.parseInt(cantidad.getText().trim());
            servicioDuts.ajustarSaldo(usuario.getId(), elegido.getId(), monto, motivo.getText());
            JOptionPane.showMessageDialog(this, "Se asignaron " + monto + " DUTS a " + elegido.getNombre() + ".");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingresa una cantidad entera.", "Dato inválido", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            mostrarError(ex);
        }
    }

    private void actualizarSaldo(JComboBox<Estudiante> selector, JTextField cantidad) {
        Estudiante elegido = (Estudiante) selector.getSelectedItem();
        if (elegido == null) return;
        try {
            int saldo = Integer.parseInt(cantidad.getText().trim());
            servicioDuts.establecerSaldo(usuario.getId(), elegido.getId(), saldo);
            JOptionPane.showMessageDialog(this, "El saldo se actualizó correctamente.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingresa un saldo entero.", "Dato inválido", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            mostrarError(ex);
        }
    }

    private void eliminarSaldo(JComboBox<Estudiante> selector) {
        Estudiante elegido = (Estudiante) selector.getSelectedItem();
        if (elegido == null) return;
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Eliminar el saldo actual de " + elegido.getNombre() + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (confirmacion != JOptionPane.YES_OPTION) return;
        try {
            servicioDuts.establecerSaldo(usuario.getId(), elegido.getId(), 0);
            JOptionPane.showMessageDialog(this, "El saldo actual quedó en cero.");
        } catch (Exception ex) {
            mostrarError(ex);
        }
    }

    private void mostrarError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "No se pudo completar la operación",
                JOptionPane.ERROR_MESSAGE);
    }

    private void cerrarSesion() {
        dispose();
        new PantallaInicial().setVisible(true);
    }
}
