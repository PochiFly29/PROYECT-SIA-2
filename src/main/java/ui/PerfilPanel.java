package ui;

import com.formdev.flatlaf.FlatClientProperties;
import gestores.GestorIntercambio;
import modelo.Estudiante;
import modelo.Usuario;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

public class PerfilPanel extends JPanel {

    private final GestorIntercambio gestor;
    private Usuario usuario;

    private JLabel lblTituloPeq;
    private JLabel lblNombreValor, lblEmailValor, lblPassValor, lblRolValor;
    private JButton btnEditNombre, btnEditEmail, btnEditPass, btnEstudianteData;

    public PerfilPanel(GestorIntercambio gestor, Usuario usuarioInicial) {
        this.gestor = gestor;
        this.usuario = usuarioInicial;
        initUI();
        refreshFromUsuario();
    }

    public void setUsuario(Usuario u) {
        this.usuario = u;
        refreshFromUsuario();
    }

    private void initUI() {
        // ===== Banner =====
        JPanel banner = new JPanel();
        banner.setLayout(new BoxLayout(banner, BoxLayout.Y_AXIS));
        banner.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        banner.setOpaque(true);

        lblTituloPeq = new JLabel("Nombre Apellido • Rol", SwingConstants.CENTER);
        lblTituloPeq.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTituloPeq.putClientProperty(FlatClientProperties.STYLE, "font:+2; foreground:lighten(@foreground,10%)");

        JLabel avatar = new JLabel();
        avatar.setPreferredSize(new Dimension(160, 160));
        avatar.setOpaque(true);
        avatar.putClientProperty(FlatClientProperties.STYLE, "background:#E6E6E6; arc:16");
        avatar.setBorder(new javax.swing.border.LineBorder(new Color(0xE6E6E6), 0, true));

        try {
            ImageIcon userIcon;
            java.net.URL imageUrl = getClass().getResource("/imagen.jpg");
            if (imageUrl != null) {
                userIcon = new ImageIcon(imageUrl);
            } else {
                userIcon = new ImageIcon("src/main/resources/imagen.jpg");
            }
            Image scaledImage = userIcon.getImage().getScaledInstance(160, 160, Image.SCALE_SMOOTH);
            avatar.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            System.err.println("No se pudo cargar la imagen de perfil. " + e.getMessage());
        }

        JPanel avatarWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        avatarWrap.setOpaque(false);
        avatarWrap.add(avatar);

        banner.add(lblTituloPeq);
        banner.add(Box.createVerticalStrut(10));
        banner.add(avatarWrap);

        // ===== Detalle (tarjeta) =====
        JPanel detalle = new JPanel(new GridBagLayout());
        detalle.setOpaque(false);
        detalle.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 100, 8, 16);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;

        // Izquierda
        c.gridx = 0; c.gridy = 0; detalle.add(labelTitulo("Nombre", 2), c);
        lblNombreValor = new JLabel("Nombre Apellido");
        btnEditNombre = createEditButton("Cambiar nombre");
        btnEditNombre.addActionListener(e -> onEditNombre());
        c.gridy = 1; detalle.add(rowCampo(lblNombreValor, btnEditNombre, 2), c);

        c.gridy = 2; detalle.add(labelTitulo("Email", 2), c);
        lblEmailValor = new JLabel("mail@gmail.com");
        btnEditEmail = createEditButton("Cambiar email");
        btnEditEmail.addActionListener(e -> onEditEmail());
        c.gridy = 3; detalle.add(rowCampo(lblEmailValor, btnEditEmail, 2), c);

        // Derecha
        c.gridx = 1; c.gridy = 0; detalle.add(labelTitulo("Rol", 2), c);
        lblRolValor = new JLabel();
        lblRolValor.putClientProperty(FlatClientProperties.STYLE, "font:+2");
        c.gridy = 1; detalle.add(lblRolValor, c);

        c.gridy = 2; detalle.add(labelTitulo("Contraseña", 2), c);
        lblPassValor = new JLabel("**********");
        btnEditPass = createEditButton("Cambiar contraseña");
        btnEditPass.addActionListener(e -> onEditPassword());
        c.gridy = 3; detalle.add(rowCampo(lblPassValor, btnEditPass, 2), c);

        // CORRECCIÓN: Botón de estudiante ahora se añade al panel de detalle con un layout específico.
        btnEstudianteData = new JButton("Ver y actualizar mis datos de estudiante");
        btnEstudianteData.putClientProperty(FlatClientProperties.STYLE, "background:#2E86FF; foreground:#FFFFFF; arc:999; font:bold +1");
        btnEstudianteData.setPreferredSize(new Dimension(300, 40));
        btnEstudianteData.addActionListener(e -> onEditEstudiante());

        JPanel tarjeta = new JPanel(new BorderLayout());
        tarjeta.putClientProperty(FlatClientProperties.STYLE, "background:#E6E6E6; arc:16");
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(new Color(0xE6E6E6), 3, false),
                BorderFactory.createEmptyBorder(8, 24, 8, 24)
        ));

        // Nuevo panel para el botón, para centrarlo correctamente
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.add(btnEstudianteData);

        // Añadimos el botón al layout de la tarjeta
        tarjeta.add(detalle, BorderLayout.CENTER);
        tarjeta.add(buttonPanel, BorderLayout.SOUTH);

        Color textoOscuro = new Color(0x444444);
        tintLabels(detalle, textoOscuro);

        setLayout(new BorderLayout());
        add(banner, BorderLayout.NORTH);
        add(tarjeta, BorderLayout.CENTER);
    }

    private void onEditEstudiante() {
        if (usuario instanceof Estudiante) {
            new EstudianteDialog(SwingUtilities.getWindowAncestor(this), gestor, (Estudiante) usuario);
            refreshFromUsuario();
        }
    }

    private JButton createEditButton(String text) {
        JButton btn = new JButton(text);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setForeground(new Color(0x2E86FF));
        return btn;
    }

    private void refreshFromUsuario() {
        if (usuario == null) return;
        String nombre = safe(usuario.getNombreCompleto());
        String email = safe(usuario.getEmail());
        String rolLegible = (usuario.getRol() != null) ? toTitulo(usuario.getRol().name()) : "Usuario";

        // CORRECCIÓN: Se actualiza el texto del lblRolValor
        lblRolValor.setText(rolLegible);

        lblTituloPeq.setText(nombre + " • " + rolLegible);
        lblNombreValor.setText(nombre);
        lblEmailValor.setText(email);
        lblPassValor.setText(mask(usuario.getPass()));

        btnEstudianteData.setVisible(usuario instanceof Estudiante);

        revalidate();
        repaint();
    }

    private void onEditNombre() {
        inlineEdit(lblNombreValor, nuevo -> {
            if (nuevo.trim().isEmpty()) {
                beepWarn("El nombre no puede estar vacío.");
                return false;
            }
            gestor.actualizarNombreUsuario(usuario.getRut(), nuevo.trim());
            usuario.setNombreCompleto(nuevo.trim());
            refreshFromUsuario();
            info("Nombre actualizado correctamente.");
            return true;
        }, false);
    }

    private void onEditEmail() {
        inlineEdit(lblEmailValor, nuevo -> {
            String n = nuevo.trim();
            if (!n.contains("@") || n.startsWith("@") || n.endsWith("@")) {
                beepWarn("Ingrese un email válido.");
                return false;
            }
            gestor.actualizarEmailUsuario(usuario.getRut(), n);
            usuario.setEmail(n);
            refreshFromUsuario();
            info("Email actualizado correctamente.");
            return true;
        }, false);
    }

    private void onEditPassword() {
        inlineEdit(lblPassValor, nuevo -> {
            if (nuevo.length() < 6) {
                beepWarn("Debe tener al menos 6 caracteres.");
                return false;
            }
            gestor.actualizarPasswordUsuario(usuario.getRut(), nuevo);
            usuario.setPass(nuevo);
            lblPassValor.setText(mask(nuevo));
            info("Contraseña actualizada correctamente.");
            return true;
        }, true);
    }

    private void inlineEdit(final JLabel targetLabel, final Function<String, Boolean> validatorCommit, final boolean password) {
        JPanel parent = (JPanel) targetLabel.getParent();

        JComponent editor;
        if (password) {
            editor = new JPasswordField(15);
            ((JPasswordField) editor).setText("");
            ((JPasswordField) editor).setEchoChar('*');
        } else {
            editor = new JTextField(15);
            String currentText = targetLabel.getText();
            ((JTextField) editor).setText(currentText.equals("-") ? "" : currentText);
        }

        editor.putClientProperty(FlatClientProperties.STYLE, "arc:999; margin:6,14,6,14");

        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setFocusPainted(false);
        btnGuardar.putClientProperty(FlatClientProperties.STYLE, "background:#2E86FF; foreground:#FFFFFF; arc:999;");

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFocusPainted(false);
        btnCancelar.putClientProperty(FlatClientProperties.STYLE, "background:#A4A4A4; foreground:#FFFFFF; arc:999;");

        JButton originalButton = (JButton) parent.getComponent(parent.getComponentCount() - 1);
        originalButton.setVisible(false);

        targetLabel.setVisible(false);
        parent.add(editor, 1);
        parent.add(btnGuardar);
        parent.add(btnCancelar);

        btnGuardar.addActionListener(e -> {
            String nuevoValor;
            if (editor instanceof JPasswordField) {
                nuevoValor = new String(((JPasswordField) editor).getPassword());
            } else {
                nuevoValor = ((JTextField) editor).getText();
            }

            if (validatorCommit.apply(nuevoValor)) {
                parent.remove(editor);
                parent.remove(btnGuardar);
                parent.remove(btnCancelar);
                targetLabel.setVisible(true);
                originalButton.setVisible(true);
            }
        });

        btnCancelar.addActionListener(e -> {
            parent.remove(editor);
            parent.remove(btnGuardar);
            parent.remove(btnCancelar);
            targetLabel.setVisible(true);
            originalButton.setVisible(true);
        });

        if (editor instanceof JTextField) {
            ((JTextField) editor).addActionListener(e -> btnGuardar.doClick());
        }

        editor.requestFocusInWindow();
        parent.revalidate();
        parent.repaint();
    }

    private static void tintLabels(Container parent, Color color) {
        for (Component comp : parent.getComponents()) {
            if (comp instanceof JLabel) {
                ((JLabel) comp).setForeground(color);
            } else if (comp instanceof Container) {
                tintLabels((Container) comp, color);
            }
        }
    }

    private static JLabel labelTitulo(String t, int sizeOffset) {
        JLabel l = new JLabel(t);
        l.putClientProperty(FlatClientProperties.STYLE, "font:bold +" + sizeOffset);
        return l;
    }

    private JPanel rowCampo(JLabel valorLabel, JButton linkButton, int sizeOffset) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        valorLabel.putClientProperty(FlatClientProperties.STYLE, "font:+" + sizeOffset);
        valorLabel.setForeground(new Color(0x444444));
        row.add(valorLabel);
        row.add(linkButton);
        return row;
    }

    private static String mask(String real) {
        if (real == null) return "—";
        int n = real.length();
        if (n <= 0) return "—";
        return "*".repeat(n);
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    private void beepWarn(String msg) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(this, msg, "Atención", JOptionPane.WARNING_MESSAGE);
    }

    private static String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
    }
    private static String toTitulo(String enumName) {
        String lower = enumName.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private class EstudianteDialog extends JDialog {
        public EstudianteDialog(Window parent, GestorIntercambio gestor, Estudiante estudiante) {
            super(parent, "Datos de Estudiante", ModalityType.APPLICATION_MODAL);
            setSize(400, 300);
            setLocationRelativeTo(parent);
            setResizable(false);

            JPanel mainPanel = new JPanel(new MigLayout("wrap 2, insets 20", "[right]10[left, grow]"));
            mainPanel.putClientProperty(FlatClientProperties.STYLE, "background:lighten(@background,3%)");

            JLabel lblCarrera = new JLabel("Carrera:");
            JTextField txtCarrera = new JTextField(estudiante.getCarrera());
            JLabel lblSemestres = new JLabel("Semestres Cursados:");
            JTextField txtSemestres = new JTextField(String.valueOf(estudiante.getSemestresCursados()));
            JLabel lblPromedio = new JLabel("Promedio:");
            JTextField txtPromedio = new JTextField(String.format("%.1f", estudiante.getPromedio()));

            lblCarrera.putClientProperty(FlatClientProperties.STYLE, "font:+2");
            txtCarrera.putClientProperty(FlatClientProperties.STYLE, "font:+2");
            lblSemestres.putClientProperty(FlatClientProperties.STYLE, "font:+2");
            txtSemestres.putClientProperty(FlatClientProperties.STYLE, "font:+2");
            lblPromedio.putClientProperty(FlatClientProperties.STYLE, "font:+2");
            txtPromedio.putClientProperty(FlatClientProperties.STYLE, "font:+2");

            JButton btnGuardar = new JButton("Guardar Cambios");
            btnGuardar.putClientProperty(FlatClientProperties.STYLE, "background:#2E86FF; foreground:#FFFFFF; arc:999");

            mainPanel.add(lblCarrera); mainPanel.add(txtCarrera, "growx");
            mainPanel.add(lblSemestres); mainPanel.add(txtSemestres, "growx");
            mainPanel.add(lblPromedio); mainPanel.add(txtPromedio, "growx");
            mainPanel.add(btnGuardar, "span 2, center, gaptop 20");

            btnGuardar.addActionListener(e -> {
                try {
                    String nuevaCarrera = txtCarrera.getText().trim();
                    int nuevosSemestres = Integer.parseInt(txtSemestres.getText().trim());
                    double nuevoPromedio = Double.parseDouble(txtPromedio.getText().trim());

                    if (nuevaCarrera.isEmpty() || nuevosSemestres <= 0 || nuevoPromedio <= 0 || nuevoPromedio > 7.0) {
                        JOptionPane.showMessageDialog(this, "Datos inválidos. Verifique los campos.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    gestor.actualizarDatosEstudiante(estudiante.getRut(), nuevaCarrera, nuevosSemestres, nuevoPromedio);
                    estudiante.setCarrera(nuevaCarrera);
                    estudiante.setSemestresCursados(nuevosSemestres);
                    estudiante.setPromedio(nuevoPromedio);

                    JOptionPane.showMessageDialog(this, "Datos de estudiante actualizados.");
                    dispose();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Semestres y promedio deben ser números válidos.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
                }
            });

            setContentPane(mainPanel);
            setVisible(true);
        }
    }
}