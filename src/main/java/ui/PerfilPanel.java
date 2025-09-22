package ui;

import com.formdev.flatlaf.FlatClientProperties;
import modelo.Estudiante;
import modelo.Usuario;

import javax.swing.*;
import java.awt.*;

public class PerfilPanel extends JPanel {

    private Usuario usuario;

    private JLabel lblTituloPeq;
    private JLabel lblNombreValor;
    private JLabel lblEmailValor;
    private JLabel lblCarreraValor;
    private JLabel lblPassValor;
    private JLabel lblRol;
    private JLabel lblSemestres;
    private JLabel lblPromedio;

    public PerfilPanel(Usuario usuarioInicial) {
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
        c.gridy = 1; detalle.add(rowCampo(lblNombreValor, "[cambiar nombre]", new Runnable() {
            public void run() { onEditNombre(); }
        }), c);

        c.gridy = 2; detalle.add(labelTitulo("Email"), c);
        lblEmailValor = new JLabel("mail@gmail.com");
        c.gridy = 3; detalle.add(rowCampo(lblEmailValor, "[cambiar email]", new Runnable() {
            public void run() { onEditEmail(); }
        }), c);

        // Derecha
        c.gridx = 1; c.gridy = 0; detalle.add(labelTitulo("Carrera"), c);
        lblCarreraValor = new JLabel("Ingeniería");
        c.gridy = 1; detalle.add(rowCampo(lblCarreraValor, "[cambiar carrera]", new Runnable() {
            public void run() { onEditCarrera(); }
        }), c);

        c.gridy = 2; detalle.add(labelTitulo("Contraseña"), c);
        lblPassValor = new JLabel("**********");
        c.gridy = 3; detalle.add(rowCampo(lblPassValor, "[cambiar contraseña]", new Runnable() {
            public void run() { onEditPassword(); }
        }), c);

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

        // Texto oscuro dentro de la tarjeta
        Color textoOscuro = new Color(0x222222);
        tintLabels(detalle, textoOscuro);
        lblRol.setForeground(textoOscuro);
        lblSemestres.setForeground(new Color(0x555555));
        lblPromedio.setForeground(new Color(0x777777));

        setLayout(new BorderLayout());
        add(banner, BorderLayout.NORTH);
        add(tarjeta, BorderLayout.CENTER);
    }

    // ===== Datos → UI =====
    private void refreshFromUsuario() {
        if (usuario == null) return;

        String nombre = safe(usuario.getNombreCompleto());
        String email  = safe(usuario.getEmail());
        String rolLegible = (usuario.getRol() != null) ? toTitulo(usuario.getRol().name()) : "Usuario";

        String carrera = "-";
        Integer semestres = null;
        Double promedio = null;
        if (usuario instanceof Estudiante) {
            Estudiante e = (Estudiante) usuario; // Java 11 OK
            carrera = safe(e.getCarrera());
            semestres = e.getSemestresCursados();
            promedio  = e.getPromedio();
        }

        lblTituloPeq.setText(nombre + " • " + (isEmpty(carrera) ? "-" : carrera));
        lblNombreValor.setText(nombre);
        lblEmailValor.setText(email);
        lblCarreraValor.setText(isEmpty(carrera) ? "-" : carrera);
        lblPassValor.setText(mask(usuario.getPass()));

        lblRol.setText(rolLegible);
        lblSemestres.setText(
                (semestres == null) ? "—" : (semestres + (semestres == 1 ? " semestre cursado" : " semestres cursados"))
        );
        lblPromedio.setText((promedio == null) ? "—" : String.format("%.1f Promedio", promedio));

        revalidate();
        repaint();
    }

    // ===== Edición inline (misma lógica que tenías) =====
    private void onEditNombre() {
        inlineEdit(lblNombreValor, new java.util.function.Function<String, Boolean>() {
            public Boolean apply(String nuevo) {
                if (nuevo.trim().isEmpty()) { beepWarn("El nombre no puede estar vacío."); return false; }
                usuario.setNombreCompleto(nuevo.trim());
                refreshFromUsuario();
                info("Nombre actualizado correctamente.");
                return true;
            }
        }, false);
    }

    private void onEditEmail() {
        inlineEdit(lblEmailValor, new java.util.function.Function<String, Boolean>() {
            public Boolean apply(String nuevo) {
                String n = nuevo.trim();
                if (!n.contains("@") || n.startsWith("@") || n.endsWith("@")) {
                    beepWarn("Ingrese un email válido.");
                    return false;
                }
                usuario.setEmail(n);
                refreshFromUsuario();
                info("Email actualizado correctamente.");
                return true;
            }
        }, false);
    }

    private void onEditCarrera() {
        if (!(usuario instanceof Estudiante)) { beepWarn("Solo los estudiantes tienen carrera."); return; }
        final Estudiante est = (Estudiante) usuario;
        inlineEdit(lblCarreraValor, new java.util.function.Function<String, Boolean>() {
            public Boolean apply(String nuevo) {
                String n = nuevo.trim();
                if (n.isEmpty()) { beepWarn("La carrera no puede estar vacía."); return false; }
                est.setCarrera(n);
                refreshFromUsuario();
                info("Carrera actualizada correctamente.");
                return true;
            }
        }, false);
    }

    private void onEditPassword() {
        inlineEdit(lblPassValor, new java.util.function.Function<String, Boolean>() {
            public Boolean apply(String n) {
                if (n.length() < 6) { beepWarn("Debe tener al menos 6 caracteres."); return false; }
                usuario.setPass(n);
                lblPassValor.setText(mask(n));
                info("Contraseña actualizada correctamente.");
                return true;
            }
        }, true);
    }

    private void inlineEdit(final JLabel targetLabel,
                            final java.util.function.Function<String, Boolean> validatorCommit,
                            final boolean password) {

        final Container row = targetLabel.getParent();
        if (row == null) return;

        int idx = -1;
        for (int i = 0; i < row.getComponentCount(); i++) {
            if (row.getComponent(i) == targetLabel) { idx = i; break; }
        }
        if (idx < 0) return;

        final int index = idx;

        final String current = (password && usuario.getPass() != null) ? usuario.getPass() : targetLabel.getText();
        final JComponent editor;
        if (password) {
            editor = new JPasswordField(current);
        } else {
            JTextField tf = new JTextField(current);
            tf.selectAll();
            editor = tf;
        }

        Dimension prefLbl = targetLabel.getPreferredSize();
        editor.setPreferredSize(new Dimension(Math.max(prefLbl.width, 180), prefLbl.height + 6));
        editor.setMaximumSize(new Dimension(400, Integer.MAX_VALUE));

        row.remove(index);
        row.add(editor, index);
        row.revalidate();
        row.repaint();
        editor.requestFocusInWindow();

        final Runnable restoreLabel = new Runnable() {
            public void run() {
                row.remove(editor);
                row.add(targetLabel, index);
                row.revalidate();
                row.repaint();
            }
        };

        final Runnable tryCommit = new Runnable() {
            public void run() {
                String value;
                if (password) value = new String(((JPasswordField) editor).getPassword());
                else value = ((JTextField) editor).getText();

                boolean ok = validatorCommit.apply(value);
                if (ok) restoreLabel.run();
                else editor.requestFocusInWindow();
            }
        };

        editor.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "commit");
        editor.getActionMap().put("commit", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { tryCommit.run(); }
        });

        editor.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ESCAPE"), "cancel");
        editor.getActionMap().put("cancel", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { restoreLabel.run(); }
        });
    }

    // ===== Helpers =====
    private static JLabel labelTitulo(String t) {
        JLabel l = new JLabel(t);
        l.putClientProperty(FlatClientProperties.STYLE, "font:bold");
        return l;
    }

    private JPanel rowCampo(JLabel valorLabel, String linkTexto, Runnable onClick) {
        JButton link = new JButton(linkTexto);
        link.setBorderPainted(false);
        link.setContentAreaFilled(false);
        link.setFocusPainted(false);
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.setForeground(new Color(0x2E86FF));
        link.addActionListener(e -> onClick.run());

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        row.add(valorLabel);
        row.add(link);
        return row;
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
        return "*".repeat(n);
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    private void beepWarn(String msg) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(this, msg, "Atención", JOptionPane.WARNING_MESSAGE);
    }

    private static String safe(String s) { return (s == null || s.trim().isEmpty()) ? "-" : s.trim(); }
    private static boolean isEmpty(String s) { return s == null || s.trim().isEmpty(); }
    private static String toTitulo(String enumName) {
        String lower = enumName.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
