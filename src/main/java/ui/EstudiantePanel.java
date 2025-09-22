package ui;

import com.formdev.flatlaf.FlatClientProperties;
import gestores.GestorIntercambio;
import modelo.Estudiante;
import modelo.Usuario;

import javax.swing.*;
import java.awt.*;

public class EstudiantePanel extends JPanel {

    private final GestorIntercambio gestor;
    private Usuario usuario;
    private final Runnable onLogout;

    private JLabel lblSidebarNombre;

    private JLabel lblTituloPeq;
    private JLabel lblNombreValor;
    private JLabel lblEmailValor;
    private JLabel lblCarreraValor;
    private JLabel lblPassValor;
    private JLabel lblRol;
    private JLabel lblSemestres;
    private JLabel lblPromedio;

    // Cards centro
    private CardLayout centerCardsLayout;
    private JPanel centerCards;
    private static final String CARD_PERFIL = "perfil";
    private static final String CARD_POSTULACIONES = "postulaciones";
    private static final String CARD_POSTULAR = "postular";

    public EstudiantePanel(GestorIntercambio gestor, Usuario usuario, Runnable onLogout) {
        this.gestor = gestor;
        this.usuario = usuario;
        this.onLogout = onLogout;
        initUI();
        refreshFromUsuario(); // para poblar datos
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        refreshFromUsuario();
    }

    private void initUI() {
        // ====== SIDEBAR IZQUIERDA (FIJO) ======
        JPanel panelCerrarSesion = new JPanel(new BorderLayout());
        JButton btnCerrar = new JButton("Cerrar Sesion");
        btnCerrar.setPreferredSize(new Dimension(180, 48));
        btnCerrar.putClientProperty(
                FlatClientProperties.STYLE,
                "background:#2E86FF; foreground:#FFFFFF; font:+3; borderWidth:0; focusWidth:0; innerFocusWidth:0"
        );
        btnCerrar.addActionListener(e -> { if (onLogout != null) onLogout.run(); });

        panelCerrarSesion.setBorder(BorderFactory.createEmptyBorder(50, 50, 30, 50));
        panelCerrarSesion.add(btnCerrar, BorderLayout.NORTH);

        // Botones navegación
        JButton btnPerfil = new JButton("Perfil");
        JButton btnVerPost = new JButton("Ver Postulaciones");
        JButton btnPostular = new JButton("Postular a un convenio");

        JPanel panelOpciones = new JPanel(new GridLayout(0, 1, 0, 12));
        panelOpciones.setBorder(BorderFactory.createEmptyBorder(50, 16, 12, 16));
        panelOpciones.add(btnPerfil);
        panelOpciones.add(btnVerPost);
        panelOpciones.add(btnPostular);

        JPanel panelPerfilMini = new JPanel();
        panelPerfilMini.setBorder(BorderFactory.createEmptyBorder(200, 16, 100, 16));
        lblSidebarNombre = new JLabel("Estudiante Nombre Apellido");
        panelPerfilMini.add(lblSidebarNombre);

        JPanel panelIzquierdo = new JPanel(new BorderLayout());
        panelIzquierdo.add(panelCerrarSesion, BorderLayout.NORTH);
        panelIzquierdo.add(panelOpciones, BorderLayout.CENTER);
        panelIzquierdo.add(panelPerfilMini, BorderLayout.SOUTH);
        panelIzquierdo.setPreferredSize(new Dimension(280, 0));
        panelIzquierdo.putClientProperty(FlatClientProperties.STYLE, "background:lighten(@background,3%)");
        panelIzquierdo.setOpaque(true);
        panelCerrarSesion.setOpaque(false);
        panelOpciones.setOpaque(false);
        panelPerfilMini.setOpaque(false);

        // ====== CENTRO CON CARDLAYOUT ======
        centerCardsLayout = new CardLayout();
        centerCards = new JPanel(centerCardsLayout);

        // Tarjeta PERFIL (incluye banner + tarjeta detalle)
        JPanel cardPerfil = buildCardPerfil();

        // Tarjeta POSTULACIONES (placeholder)
        JPanel cardPostulaciones = new JPanel(new BorderLayout());
        JLabel ph1 = new JLabel("Ver postulaciones próximamente", SwingConstants.CENTER);
        ph1.putClientProperty(FlatClientProperties.STYLE, "font:+3");
        cardPostulaciones.add(ph1, BorderLayout.CENTER);

        // Tarjeta POSTULAR (placeholder)
        JPanel cardPostular = new JPanel(new BorderLayout());
        JLabel ph2 = new JLabel("Postular a un convenio próximamente", SwingConstants.CENTER);
        ph2.putClientProperty(FlatClientProperties.STYLE, "font:+3");
        cardPostular.add(ph2, BorderLayout.CENTER);

        // Añadir cards
        centerCards.add(cardPerfil, CARD_PERFIL);
        centerCards.add(cardPostulaciones, CARD_POSTULACIONES);
        centerCards.add(cardPostular, CARD_POSTULAR);
        centerCardsLayout.show(centerCards, CARD_PERFIL);

        // ====== LAYOUT EXTERIOR ======
        setLayout(new BorderLayout());
        add(panelIzquierdo, BorderLayout.WEST);
        add(centerCards, BorderLayout.CENTER);

        // Acciones de navegación
        btnPerfil.addActionListener(e -> {
            centerCardsLayout.show(centerCards, CARD_PERFIL);
            refreshFromUsuario();
        });
        btnVerPost.addActionListener(e -> centerCardsLayout.show(centerCards, CARD_POSTULACIONES));
        btnPostular.addActionListener(e -> centerCardsLayout.show(centerCards, CARD_POSTULAR));
    }

    private JPanel buildCardPerfil() {
        // ====== BANNER ======
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

        // ====== TARJETA DETALLE PERFIL ======
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

        Color textoOscuro = new Color(0x222222);
        tintLabels(detalle, textoOscuro);
        lblRol.setForeground(textoOscuro);
        lblSemestres.setForeground(new Color(0x555555));
        lblPromedio.setForeground(new Color(0x777777));

        // PERFIL = banner arriba + tarjeta debajo
        JPanel perfil = new JPanel(new BorderLayout());
        perfil.add(banner, BorderLayout.NORTH);
        perfil.add(tarjeta, BorderLayout.CENTER);
        return perfil;
    }

    // ====== REFRESCO DATOS PERFIL ======
    private void refreshFromUsuario() {
        if (usuario == null) return;

        String nombre = safe(usuario.getNombreCompleto());
        String email  = safe(usuario.getEmail());

        String rolLegible = (usuario.getRol() != null) ? toTitulo(usuario.getRol().name()) : "Usuario";
        lblSidebarNombre.setText(rolLegible + " " + nombre);

        String carrera = "-";
        Integer semestres = null;
        Double promedio = null;
        if (usuario instanceof Estudiante) {
            Estudiante e = (Estudiante) usuario;
            carrera = safe(e.getCarrera());
            semestres = e.getSemestresCursados();
            promedio  = e.getPromedio();
        }

        if (lblTituloPeq != null) lblTituloPeq.setText(nombre + " • " + (isEmpty(carrera) ? "-" : carrera));
        if (lblNombreValor != null) lblNombreValor.setText(nombre);
        if (lblEmailValor != null) lblEmailValor.setText(email);
        if (lblCarreraValor != null) lblCarreraValor.setText(isEmpty(carrera) ? "-" : carrera);
        if (lblPassValor != null) lblPassValor.setText(mask(usuario.getPass()));
        if (lblRol != null) lblRol.setText(rolLegible);
        if (lblSemestres != null) {
            lblSemestres.setText(
                    (semestres == null) ? "—" : (semestres + (semestres == 1 ? " semestre cursado" : " semestres cursados"))
            );
        }
        if (lblPromedio != null) {
            lblPromedio.setText((promedio == null) ? "—" : String.format("%.1f Promedio", promedio));
        }

        revalidate();
        repaint();
    }

    // ====== Edición inline ======
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

    private void inlineEdit(final JLabel targetLabel, final java.util.function.Function<String, Boolean> validatorCommit,final boolean password) {

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

    private static String mask(String real) {
        if (real == null) return "—";
        int n = real.length();
        if (n <= 0) return "—";
        return "*".repeat(n); // Java 11
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    private void beepWarn(String msg) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(this, msg, "Atención", JOptionPane.WARNING_MESSAGE);
    }

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
