package ui;

import com.formdev.flatlaf.FlatClientProperties;
import gestores.GestorIntercambio;
import modelo.Estudiante;
import modelo.Usuario;

import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

public class PerfilPanel extends JPanel {

    private final GestorIntercambio gestor;
    private Usuario usuario;

    private JLabel lblTituloPeq;
    private JLabel lblNombreValor, lblEmailValor, lblCarreraValor, lblPassValor, lblRol, lblSemestres, lblPromedio;
    private JButton btnEditNombre, btnEditEmail, btnEditCarrera, btnEditPass;

    public PerfilPanel(Usuario usuarioInicial) {
        this.gestor = null;
        this.usuario = usuarioInicial;
        initUI();
        refreshFromUsuario();
    }

    public void setUsuario(Usuario u) {
        this.usuario = u;
        refreshFromUsuario();
    }

    private void initUI() {
        JPanel banner = new JPanel();
        banner.setLayout(new BoxLayout(banner, BoxLayout.Y_AXIS));
        banner.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        banner.setOpaque(true);

        lblTituloPeq = new JLabel("Nombre Apellido • Ingeniería", SwingConstants.CENTER);
        lblTituloPeq.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTituloPeq.putClientProperty(FlatClientProperties.STYLE, "font:+0; foreground:lighten(@foreground,10%)");

        JLabel avatar = new JLabel("FOTO", SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(160, 160));
        avatar.setOpaque(true);
        avatar.putClientProperty(FlatClientProperties.STYLE, "background:#E6E6E6; arc:16");
        avatar.setBorder(new javax.swing.border.LineBorder(new Color(0xE6E6E6), 0, true));

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
        c.gridx = 0; c.gridy = 0; detalle.add(labelTitulo("Nombre"), c);
        lblNombreValor = new JLabel("Nombre Apellido");
        btnEditNombre = createEditButton("Cambiar nombre");
        btnEditNombre.addActionListener(e -> onEditNombre());
        c.gridy = 1; detalle.add(rowCampo(lblNombreValor, btnEditNombre), c);

        c.gridy = 2; detalle.add(labelTitulo("Email"), c);
        lblEmailValor = new JLabel("mail@gmail.com");
        btnEditEmail = createEditButton("Cambiar email");
        btnEditEmail.addActionListener(e -> onEditEmail());
        c.gridy = 3; detalle.add(rowCampo(lblEmailValor, btnEditEmail), c);

        // Derecha
        c.gridx = 1; c.gridy = 0; detalle.add(labelTitulo("Carrera"), c);
        lblCarreraValor = new JLabel("Ingeniería");
        btnEditCarrera = createEditButton("Cambiar carrera");
        btnEditCarrera.addActionListener(e -> onEditCarrera());
        c.gridy = 1; detalle.add(rowCampo(lblCarreraValor, btnEditCarrera), c);

        c.gridy = 2; detalle.add(labelTitulo("Contraseña"), c);
        lblPassValor = new JLabel("**********");
        btnEditPass = createEditButton("Cambiar contraseña");
        btnEditPass.addActionListener(e -> onEditPassword());
        c.gridy = 3; detalle.add(rowCampo(lblPassValor, btnEditPass), c);

        JPanel tarjeta = new JPanel(new BorderLayout());
        tarjeta.putClientProperty(FlatClientProperties.STYLE, "background:#E6E6E6; arc:16");
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                new javax.swing.border.LineBorder(new Color(0xE6E6E6), 3, false),
                BorderFactory.createEmptyBorder(8, 24, 8, 24)
        ));
        tarjeta.add(detalle, BorderLayout.CENTER);

        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        lblRol = new JLabel("Estudiante", SwingConstants.CENTER);
        lblRol.putClientProperty(FlatClientProperties.STYLE, "font:bold +1");
        lblSemestres = new JLabel("X semestre(s) cursado(s)", SwingConstants.CENTER);
        lblPromedio  = new JLabel("5.0 Promedio", SwingConstants.CENTER);
        lblRol.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSemestres.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblPromedio.setAlignmentX(Component.CENTER_ALIGNMENT);
        tarjeta.add(footer, BorderLayout.SOUTH);
        footer.add(lblRol);
        footer.add(Box.createVerticalStrut(3));
        footer.add(lblSemestres);
        footer.add(Box.createVerticalStrut(4));
        footer.add(lblPromedio);
        footer.add(Box.createVerticalStrut(50));

        Color textoOscuro = new Color(0x222222);
        tintLabels(detalle, textoOscuro);
        lblRol.setForeground(textoOscuro);
        lblSemestres.setForeground(new Color(0x555555));
        lblPromedio.setForeground(new Color(0x777777));

        setLayout(new BorderLayout());
        add(banner, BorderLayout.NORTH);
        add(tarjeta, BorderLayout.CENTER);
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

    private static JLabel labelTitulo(String t) {
        JLabel l = new JLabel(t);
        l.putClientProperty(FlatClientProperties.STYLE, "font:bold");
        return l;
    }

    private JPanel rowCampo(JLabel valorLabel, JButton linkButton) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        row.add(valorLabel);
        row.add(linkButton);
        return row;
    }

    // ====== Lógica ======
    private void refreshFromUsuario() {
        if (usuario == null) return;

        String nombre = safe(usuario.getNombreCompleto());
        String email  = safe(usuario.getEmail());
        String rolLegible = (usuario.getRol() != null) ? toTitulo(usuario.getRol().name()) : "Usuario";

        String carrera = "-";
        Integer semestres = null;
        Double promedio = null;
        boolean esEstudiante = usuario instanceof Estudiante;
        if (esEstudiante) {
            Estudiante e = (Estudiante) usuario;
            carrera = safe(e.getCarrera());
            semestres = e.getSemestresCursados();
            promedio  = e.getPromedio();
        }

        // Banner
        String sub = esEstudiante ? (isEmpty(carrera) ? "-" : carrera) : rolLegible;
        lblTituloPeq.setText(nombre + " • " + sub);

        lblNombreValor.setText(nombre);
        lblEmailValor.setText(email);
        lblCarreraValor.setText(isEmpty(carrera) ? "-" : carrera);
        lblPassValor.setText(mask(usuario.getPass()));

        lblRol.setText(rolLegible);
        lblSemestres.setText((semestres == null) ? "—" : (semestres + (semestres == 1 ? " semestre cursado" : " semestres cursados")));
        lblPromedio.setText((promedio == null) ? "—" : String.format("%.1f Promedio", promedio));

        btnEditCarrera.setVisible(esEstudiante);

        revalidate();
        repaint();
    }

    private void onEditNombre() {
        inlineEdit(lblNombreValor, nuevo -> {
            String n = nuevo.trim();
            if (n.isEmpty()) { beepWarn("El nombre no puede estar vacío."); return false; }
            if (gestor != null) gestor.actualizarNombreUsuario(usuario.getRut(), n);
            usuario.setNombreCompleto(n);
            refreshFromUsuario();
            info("Nombre actualizado correctamente.");
            return true;
        }, false, btnEditNombre);
    }

    private void onEditEmail() {
        inlineEdit(lblEmailValor, nuevo -> {
            String n = nuevo.trim();
            if (!n.contains("@") || n.startsWith("@") || n.endsWith("@")) {
                beepWarn("Ingrese un email válido.");
                return false;
            }
            if (gestor != null) gestor.actualizarEmailUsuario(usuario.getRut(), n);
            usuario.setEmail(n);
            refreshFromUsuario();
            info("Email actualizado correctamente.");
            return true;
        }, false, btnEditEmail);
    }

    private void onEditCarrera() {
        if (!(usuario instanceof Estudiante)) { beepWarn("Solo los estudiantes tienen carrera."); return; }
        final Estudiante est = (Estudiante) usuario;
        inlineEdit(lblCarreraValor, nuevo -> {
            String n = nuevo.trim();
            if (n.isEmpty()) { beepWarn("La carrera no puede estar vacía."); return false; }
            if (gestor != null) gestor.actualizarCarreraEstudiante(est.getRut(), n);
            est.setCarrera(n);
            refreshFromUsuario();
            info("Carrera actualizada correctamente.");
            return true;
        }, false, btnEditCarrera);
    }

    private void onEditPassword() {
        inlineEdit(lblPassValor, nuevo -> {
            if (nuevo.length() < 6) { beepWarn("Debe tener al menos 6 caracteres."); return false; }
            if (gestor != null) gestor.actualizarPasswordUsuario(usuario.getRut(), nuevo);
            usuario.setPass(nuevo);
            lblPassValor.setText(mask(nuevo));
            info("Contraseña actualizada correctamente.");
            return true;
        }, true, btnEditPass);
    }

    private void inlineEdit(final JLabel targetLabel, final Function<String, Boolean> validatorCommit, final boolean password, final JButton editBtn) {
        final JPanel row = (JPanel) targetLabel.getParent();

        final JComponent editor;
        if (password) {
            JPasswordField pf = new JPasswordField(15);
            pf.setText("");
            pf.setEchoChar('*');
            editor = pf;
        } else {
            JTextField tf = new JTextField(15);
            String currentText = targetLabel.getText();
            tf.setText("-".equals(currentText) ? "" : currentText);
            tf.selectAll();
            editor = tf;
        }

        editor.putClientProperty(FlatClientProperties.STYLE, "arc:999; margin:6,14,6,14");

        final JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setFocusPainted(false);
        btnGuardar.putClientProperty(FlatClientProperties.STYLE, "background:#2E86FF; foreground:#FFFFFF; arc:999;");
        final JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFocusPainted(false);
        btnCancelar.putClientProperty(FlatClientProperties.STYLE, "background:#A4A4A4; foreground:#FFFFFF; arc:999;");

        if (editBtn != null) editBtn.setVisible(false);

        targetLabel.setVisible(false);
        row.add(editor, 1);
        row.add(btnGuardar);
        row.add(btnCancelar);
        row.revalidate();
        row.repaint();
        editor.requestFocusInWindow();

        btnGuardar.addActionListener(e -> {
            String nuevoValor = (editor instanceof JPasswordField)
                    ? new String(((JPasswordField) editor).getPassword())
                    : ((JTextField) editor).getText();

            boolean ok = validatorCommit.apply(nuevoValor);
            if (ok) {
                restoreRow(row, editor, btnGuardar, btnCancelar, targetLabel, editBtn);
            } else {
                editor.requestFocusInWindow();
            }
        });

        btnCancelar.addActionListener(e ->
                restoreRow(row, editor, btnGuardar, btnCancelar, targetLabel, editBtn)
        );

        if (editor instanceof JTextField) {
            ((JTextField) editor).addActionListener(e ->
                    btnGuardar.doClick()
            );
        }
    }

    private void restoreRow(JPanel row, JComponent editor, JButton btnGuardar, JButton btnCancelar, JLabel targetLabel, JButton editBtn) {
        row.remove(editor);
        row.remove(btnGuardar);
        row.remove(btnCancelar);
        targetLabel.setVisible(true);
        if (editBtn != null) editBtn.setVisible(true);
        row.revalidate();
        row.repaint();
    }

    private static void tintLabels(Container parent, Color color) {
        for (Component comp : parent.getComponents()) {
            if (comp instanceof JLabel) ((JLabel) comp).setForeground(color);
            if (comp instanceof Container) tintLabels((Container) comp, color);
        }
    }

    private static String mask(String real) {
        if (real == null) return "—";
        int n = real.length();
        if (n <= 0) return "—";
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append('*');
        return sb.toString();
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
    private static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
    private static String toTitulo(String enumName) {
        String lower = enumName.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
