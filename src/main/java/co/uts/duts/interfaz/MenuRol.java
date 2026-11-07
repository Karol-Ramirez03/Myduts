package co.uts.duts.interfaz;

import co.uts.duts.datos.texto.EstudianteArchivoRepository;
import co.uts.duts.duts.ServicioDuts;
import co.uts.duts.duts.Transferencia;
import co.uts.duts.duts.Movimiento;
import co.uts.duts.usuarios.Estudiante;
import co.uts.duts.usuarios.Usuario;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
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
        setSize(560, Math.min(780, Math.max(430, 130 + (opciones.length + 1) * 50)));
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
        } else if ("Historial de transacciones".equals(opcion)) {
            mostrarHistorialTransacciones();
        } else if ("Extracto de transacciones".equals(opcion)) {
            generarExtractoMensual();
        } else if ("Ranking DUTS".equals(opcion)) {
            mostrarRankingDuts();
        } else if ("Calculadora de notas".equals(opcion)) {
            abrirCalculadoraNotas();
        } else if ("Preguntas frecuentes".equals(opcion)) {
            PreguntasFrecuentesVentana.abrir(this, usuario);
        } else if ("Calificación de eventos".equals(opcion)) {
            CalificacionEventosVentana.abrir(this, usuario);
        } else if ("Mis logros".equals(opcion)) {
            LogrosVentana.abrir(this, usuario);
        } else if ("Novedades".equals(opcion) || "Novedades y eventos".equals(opcion)) {
            setVisible(false);
            if ("ADMIN".equals(usuario.getRol())) {
                new AdminNovedades(this, usuario).setVisible(true);
            } else {
                new MenuNovedades(this, usuario).setVisible(true);
            }
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
        try {
            Estudiante saldoActual = servicioDuts.consultarEstudiante(usuario.getId());
            if (saldoActual != null && saldoActual.getCuenta().estaBajoMinimo()) {
                JOptionPane.showMessageDialog(this,
                        "Alerta: tu saldo actual es " + saldoActual.consultarSaldo() + " DUTS y está por debajo "
                                + "del mínimo de " + saldoActual.getCuenta().getSaldoMinimoAlerta() + " DUTS.",
                        "Alerta de saldo mínimo", JOptionPane.WARNING_MESSAGE);
            }
        } catch (IOException ex) {
            mostrarError(ex);
            return;
        }

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
            String correoDestino = destino.getText().trim();
            Transferencia vistaPrevia = servicioDuts.previsualizarTransferencia(
                    usuario.getId(), correoDestino, cantidad);
            Estudiante origen = servicioDuts.consultarEstudiante(usuario.getId());
            Usuario destinatario = servicioDuts.consultarUsuario(vistaPrevia.getIdDestino());
            int saldoActual = origen.consultarSaldo();
            int saldoPosterior = saldoActual - cantidad;
            int saldoMinimo = origen.getCuenta().getSaldoMinimoAlerta();

            StringBuilder alerta = new StringBuilder();
            if (saldoPosterior < saldoMinimo) {
                alerta.append("Después del envío tendrás ").append(saldoPosterior)
                        .append(" DUTS, por debajo del mínimo de ").append(saldoMinimo).append(" DUTS.\n");
            }
            if (alerta.length() > 0) {
                JOptionPane.showMessageDialog(this, alerta.toString(),
                        "Alerta de saldo mínimo", JOptionPane.WARNING_MESSAGE);
            }

            String confirmacion = "Confirma la transferencia:\n\n"
                    + "Destinatario: " + destinatario.getNombre() + " (" + destinatario.getCorreo() + ")\n"
                    + "Cantidad: " + cantidad + " DUTS\n"
                    + "Saldo actual: " + saldoActual + " DUTS\n"
                    + "Saldo después del envío: " + saldoPosterior + " DUTS\n\n"
                    + "¿Deseas continuar?";
            if (JOptionPane.showConfirmDialog(this, confirmacion, "Confirmar transferencia",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) {
                return;
            }

            Transferencia transferencia = servicioDuts.transferir(usuario.getId(), correoDestino, cantidad);
            String resultado = "Enviaste " + transferencia.getMonto() + " DUTS correctamente."
                    + (saldoPosterior < saldoMinimo ? "\nAlerta: tu saldo quedó por debajo del mínimo." : "");
            JOptionPane.showMessageDialog(this, resultado, "Transferencia realizada", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser un número entero.",
                    "Dato inválido", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            mostrarError(ex);
        }
    }

    private void mostrarHistorialTransacciones() {
        JTextField meses = new JTextField("3");
        JTextField correoDestino = new JTextField();
        JPanel filtros = new JPanel(new GridLayout(0, 2, 8, 8));
        filtros.add(new JLabel("Mostrar últimos meses:"));
        filtros.add(meses);
        filtros.add(new JLabel("Correo al que enviaste (opcional):"));
        filtros.add(correoDestino);
        if (JOptionPane.showConfirmDialog(this, filtros, "Filtrar transacciones",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;

        try {
            int cantidadMeses = Integer.parseInt(meses.getText().trim());
            List<Movimiento> movimientos = servicioDuts.filtrarMovimientos(
                    usuario.getId(), cantidadMeses, correoDestino.getText());
            if (movimientos.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay movimientos con esos filtros.",
                        "Historial", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            StringBuilder texto = new StringBuilder("Historial de transacciones\n\n");
            for (Movimiento movimiento : movimientos) {
                texto.append(movimiento.getFecha().format(formato)).append(" | ")
                        .append(movimiento.getTipo()).append(" | ")
                        .append(movimiento.getMonto()).append(" DUTS | ")
                        .append(movimiento.getEstado()).append("\n")
                        .append(movimiento.getDescripcion() == null ? "" : movimiento.getDescripcion())
                        .append("\n");
                Long otraCuenta = movimiento.getIdOrigen() != null
                        && movimiento.getIdOrigen() == usuario.getId()
                        ? movimiento.getIdDestino() : movimiento.getIdOrigen();
                if (otraCuenta != null) {
                    Usuario relacionado = servicioDuts.consultarUsuario(otraCuenta);
                    if (relacionado != null) texto.append("Cuenta relacionada: ").append(relacionado.getCorreo()).append("\n");
                }
                texto.append("\n");
            }
            JTextArea area = new JTextArea(texto.toString(), 20, 60);
            area.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(area), "Historial de transacciones",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingresa un número válido de meses.",
                    "Filtro inválido", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            mostrarError(ex);
        }
    }

    private void generarExtractoMensual() {
        JTextField mes = new JTextField(Integer.toString(java.time.LocalDate.now().getMonthValue()));
        JTextField anio = new JTextField(Integer.toString(java.time.LocalDate.now().getYear()));
        JPanel periodo = new JPanel(new GridLayout(0, 2, 8, 8));
        periodo.add(new JLabel("Mes (1 a 12):"));
        periodo.add(mes);
        periodo.add(new JLabel("Año:"));
        periodo.add(anio);
        if (JOptionPane.showConfirmDialog(this, periodo, "Seleccionar periodo del extracto",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            int mesSeleccionado = Integer.parseInt(mes.getText().trim());
            int anioSeleccionado = Integer.parseInt(anio.getText().trim());
            if (mesSeleccionado < 1 || mesSeleccionado > 12 || anioSeleccionado < 1) {
                throw new IllegalArgumentException("Ingresa un mes entre 1 y 12 y un año válido.");
            }
            java.time.YearMonth mesAnio = java.time.YearMonth.of(anioSeleccionado, mesSeleccionado);
            Path archivo = servicioDuts.generarExtracto(usuario.getId(), mesAnio);
            String contenido = Files.readString(archivo, StandardCharsets.UTF_8);
            JTextArea area = new JTextArea(contenido, 22, 90);
            area.setEditable(false);
            area.setCaretPosition(0);
            area.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 13));
            JPanel panel = new JPanel(new BorderLayout(8, 8));
            panel.add(new JLabel("Archivo generado: " + archivo.toString()), BorderLayout.NORTH);
            panel.add(new JScrollPane(area), BorderLayout.CENTER);
            JOptionPane.showMessageDialog(this, panel, "Extracto de " + mesAnio,
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Mes y año deben ser números enteros.",
                    "Periodo inválido", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Periodo inválido", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            mostrarError(ex);
        }
    }

    private void mostrarRankingDuts() {
        try {
            List<Estudiante> ranking = servicioDuts.rankingDuts(5);
            if (ranking.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Todavía no hay estudiantes para mostrar.",
                        "Ranking DUTS", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            StringBuilder texto = new StringBuilder("Top 5 de DUTS acumulados\nMeta de referencia: 70.000 DUTS en 2 semestres\n\n");
            int puesto = 1;
            for (Estudiante estudiante : ranking) {
                int acumulados = estudiante.getCuenta().getDutsAcumulados();
                double avance = Math.min(100.0, acumulados * 100.0 / co.uts.duts.duts.Cuenta.META_GRADUACION);
                texto.append(puesto++).append(". ").append(estudiante.getNombre())
                        .append(" — ").append(acumulados).append(" DUTS — ")
                        .append(String.format("%.1f%%", avance)).append(" de la meta — ")
                        .append(estudiante.semestresRestantes()).append(" semestres restantes\n");
            }
            JTextArea area = new JTextArea(texto.toString(), 16, 58);
            area.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(area), "Ranking DUTS",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            mostrarError(ex);
        }
    }

    private void abrirCalculadoraNotas() {
        JTextField[] notas = {new JTextField(), new JTextField(), new JTextField()};
        JTextField[] porcentajes = {new JTextField(), new JTextField(), new JTextField()};
        JPanel panel = new JPanel(new GridLayout(0, 3, 8, 8));
        panel.add(new JLabel("Actividad"));
        panel.add(new JLabel("Nota (0 a 5)"));
        panel.add(new JLabel("Porcentaje"));
        for (int i = 0; i < 3; i++) {
            panel.add(new JLabel("Nota " + (i + 1)));
            panel.add(notas[i]);
            panel.add(porcentajes[i]);
        }
        JOptionPane.showMessageDialog(this, "Ingresa tres notas y el porcentaje de cada una. "
                + "Los porcentajes deben sumar 100.", "Calculadora de notas", JOptionPane.INFORMATION_MESSAGE);
        if (JOptionPane.showConfirmDialog(this, panel, "Calculadora de notas",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            double sumaPorcentajes = 0;
            double promedioPonderado = 0;
            for (int i = 0; i < 3; i++) {
                double nota = Double.parseDouble(notas[i].getText().trim().replace(',', '.'));
                double porcentaje = Double.parseDouble(porcentajes[i].getText().trim().replace(',', '.'));
                if (nota < 0 || nota > 5) throw new IllegalArgumentException("Cada nota debe estar entre 0 y 5.");
                if (porcentaje < 0 || porcentaje > 100) throw new IllegalArgumentException("Cada porcentaje debe estar entre 0 y 100.");
                sumaPorcentajes += porcentaje;
                promedioPonderado += nota * porcentaje;
            }
            if (Math.abs(sumaPorcentajes - 100.0) > 0.001) {
                throw new IllegalArgumentException("Los tres porcentajes deben sumar exactamente 100%.");
            }
            JOptionPane.showMessageDialog(this,
                    String.format("Promedio ponderado: %.2f / 5.0", promedioPonderado / 100.0),
                    "Resultado", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Completa las seis casillas con números válidos.",
                    "Datos inválidos", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Datos inválidos", JOptionPane.WARNING_MESSAGE);
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
