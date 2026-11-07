package co.uts.duts.interfaz;

import co.uts.duts.datos.texto.EstudianteArchivoRepository;
import co.uts.duts.usuarios.Usuario;
import java.awt.BorderLayout;
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

/** Consulta usuarios y permite que un administrador cree otras cuentas administradoras. */
public class UsuariosAdministracionVentana extends JDialog {
    private final Usuario administrador;
    private final EstudianteArchivoRepository repositorio = new EstudianteArchivoRepository();
    private final JTextArea listado = new JTextArea(16, 54);

    public static void abrir(JFrame padre, Usuario administrador) {
        new UsuariosAdministracionVentana(padre, administrador).setVisible(true);
    }

    private UsuariosAdministracionVentana(JFrame padre, Usuario administrador) {
        super(padre, "Usuarios", true);
        this.administrador = administrador;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(660, 450);
        setLocationRelativeTo(padre);
        setLayout(new BorderLayout(8, 8));

        JLabel titulo = new JLabel("Usuarios registrados", JLabel.CENTER);
        titulo.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 22));
        titulo.setForeground(new java.awt.Color(52, 83, 31));
        titulo.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 10, 4, 10));
        listado.setEditable(false);
        listado.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
        add(titulo, BorderLayout.NORTH);
        add(new JScrollPane(listado), BorderLayout.CENTER);

        JButton crear = crearBoton("Crear administrador");
        crear.addActionListener(event -> mostrarFormulario());
        JButton cerrar = crearBoton("Cerrar");
        cerrar.addActionListener(event -> dispose());
        JPanel acciones = new JPanel();
        acciones.add(crear);
        acciones.add(cerrar);
        add(acciones, BorderLayout.SOUTH);
        cargarUsuarios();
    }

    private JButton crearBoton(String texto) {
        JButton boton = new JButton(texto);
        boton.setBackground(new java.awt.Color(52, 83, 31));
        boton.setForeground(java.awt.Color.WHITE);
        return boton;
    }

    private void cargarUsuarios() {
        try {
            StringBuilder texto = new StringBuilder(String.format("%-5s %-12s %-26s %-18s%n",
                    "ID", "ROL", "NOMBRE", "CORREO"));
            for (Usuario usuario : repositorio.listar()) {
                texto.append(String.format("%-5d %-12s %-26s %-18s%n", usuario.getId(), usuario.getRol(),
                        usuario.getNombre(), usuario.getCorreo()));
            }
            listado.setText(texto.toString());
            listado.setCaretPosition(0);
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
}
