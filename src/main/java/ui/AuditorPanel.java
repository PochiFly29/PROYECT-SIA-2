package ui;

import com.formdev.flatlaf.FlatClientProperties;
import enums.Rol;
import gestores.GestorIntercambio;
import modelo.Usuario;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class AuditorPanel extends JPanel {

    private final GestorIntercambio gestor;
    private final Usuario auditor;
    private final Runnable onLogout;

    private JLabel lblSidebarNombre;

    private final CardLayout centerCardsLayout = new CardLayout();
    private final JPanel centerCards = new JPanel(centerCardsLayout);

    // Nombres de tarjetas
    private static final String CARD_PERFIL = "perfil";
    private static final String CARD_GESTION_PROGRAMAS = "gestionProgramas";
    private static final String CARD_GESTION_CONVENIOS = "gestionConvenios";
    private static final String CARD_ANALISIS = "analisis";

    // Paneles de contenido
    private PerfilPanel perfilPanel;
    private GestionProgramasPanel gestionProgramasPanel;
    private GestionConveniosAuditorPanel gestionConveniosPanel;

    // Botones nav
    private JToggleButton btnPerfil;
    private JToggleButton btnGestionUsuarios;
    private JToggleButton btnGestionProgramas;
    private JToggleButton btnGestionConvenios;
    private JToggleButton btnAnalisis;

    public AuditorPanel(GestorIntercambio gestor, Usuario auditor, Runnable onLogout) {
        this.gestor = gestor;
        this.auditor = auditor;
        this.onLogout = onLogout;
        init();
    }

    // ==== Helpers de UI ====

    private JToggleButton botonNavegacion(String text) {
        JToggleButton b = new JToggleButton(text);
        b.setHorizontalAlignment(SwingConstants.CENTER);
        b.setFocusPainted(false);
        b.setOpaque(true);

        Insets padding = new Insets(14, 24, 14, 24);
        b.setMargin(padding);
        b.setBorder(BorderFactory.createEmptyBorder(padding.top, padding.left, padding.bottom, padding.right));

        b.setMinimumSize(new Dimension(0, 72));
        b.setPreferredSize(new Dimension(0, 72));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        b.setFont(b.getFont().deriveFont(Font.BOLD, b.getFont().getSize2D() + 2f));
        b.setForeground(UIManager.getColor("Label.foreground"));

        wireToggleBehavior(b);
        addHoverEffect(b);
        return b;
    }

    /** Fila con 2 columnas: [botón][franjaDerecha 6px] usando MigLayout para garantizar la franja */
    private JPanel makeNavItem(JToggleButton b) {
        JPanel row = new JPanel(new MigLayout("insets 0, gap 0, fill", "[grow,fill][6!]", "[fill]"));
        row.setOpaque(false);
        row.setMinimumSize(new Dimension(0, 72));
        row.setPreferredSize(new Dimension(0, 72));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        row.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel stripe = new JPanel();
        stripe.setOpaque(true);
        stripe.setBackground(new Color(0x4A95FF)); // azul un poco más claro que el fondo seleccionado
        stripe.setVisible(b.isSelected());

        // Guardamos referencia para actualizarla desde wireToggleBehavior
        b.putClientProperty("stripe", stripe);

        row.add(b, "cell 0 0, grow");
        row.add(stripe, "cell 1 0, growy");
        return row;
    }

    /** Selección: fondo azul + controlar visibilidad de la franja */
    private void wireToggleBehavior(JToggleButton b) {
        final Color selectedBg = new Color(0x2E86FF);
        final Color selectedFg = Color.WHITE;
        final Color unselectedFg = UIManager.getColor("Label.foreground");

        b.setContentAreaFilled(false);
        b.addChangeListener(e -> {
            boolean sel = b.isSelected();
            JPanel stripe = (JPanel) b.getClientProperty("stripe");
            if (sel) {
                b.setContentAreaFilled(true);
                b.setBackground(selectedBg);
                b.setForeground(selectedFg);
                if (stripe != null) stripe.setVisible(true);
            } else {
                b.setContentAreaFilled(false);
                b.setForeground(unselectedFg);
                if (stripe != null) stripe.setVisible(false);
            }
            b.revalidate(); b.repaint();
            if (stripe != null) { stripe.revalidate(); stripe.repaint(); }
        });
    }

    /** Hover: sombreado cuando no está seleccionado */
    private void addHoverEffect(JToggleButton b) {
        final Color hoverBg = new Color(0x2F2F2F);
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!b.isSelected()) {
                    b.setContentAreaFilled(true);
                    b.setBackground(hoverBg);
                }
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                if (!b.isSelected()) {
                    b.setContentAreaFilled(false);
                    b.setBackground(null);
                }
            }
        });
    }

    private static void styleRectButtonPrimary(JButton b, Color bg) {
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(b.getFont().deriveFont(Font.BOLD, b.getFont().getSize2D() + 2f));
        b.setMargin(new Insets(16, 20, 16, 20));
        b.setBorder(BorderFactory.createLineBorder(bg.darker(), 1, false));
    }

    // ==== init ====

    private void init() {
        // Sidebar fijo
        JPanel panelIzquierdo = new JPanel(new BorderLayout());
        panelIzquierdo.setBackground(new Color(0x262626));
        panelIzquierdo.setOpaque(true);
        panelIzquierdo.setPreferredSize(new Dimension(360, 0));
        panelIzquierdo.setMinimumSize(new Dimension(320, 0));

        // Header con logo
        JPanel topPanel = new JPanel(new MigLayout("wrap, fillx, insets 24 24 8 24", "[fill]"));
        topPanel.setOpaque(false);

        JLabel logo = new JLabel("", SwingConstants.CENTER);
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/Logo SGIE.png"));
            Image img = icon.getImage();

            int maxW = 330, maxH = 220;
            int iw = img.getWidth(null), ih = img.getHeight(null);
            double s = Math.min(maxW / (double) iw, maxH / (double) ih);
            int nw = Math.max(1, (int) Math.round(iw * s));
            int nh = Math.max(1, (int) Math.round(ih * s));

            Image scaled = img.getScaledInstance(nw, nh, Image.SCALE_SMOOTH);
            logo.setIcon(new ImageIcon(scaled));
            logo.setPreferredSize(new Dimension(maxW, maxH));
        } catch (Exception ignore) {
            logo.setText("Logo");
            logo.setForeground(Color.WHITE);
            logo.setFont(logo.getFont().deriveFont(Font.BOLD, logo.getFont().getSize2D() + 6f));
        }
        topPanel.add(logo, "growx, gaptop 4");

        JPanel separatorTop = new JPanel();
        separatorTop.setPreferredSize(new Dimension(0, 1));
        separatorTop.setBackground(new Color(0x333333)); // separador suave
        topPanel.add(separatorTop, "growx, gaptop 12");

        // Navegación
        JPanel navButtonsPanel = new JPanel();
        navButtonsPanel.setOpaque(false);
        navButtonsPanel.setLayout(new BoxLayout(navButtonsPanel, BoxLayout.Y_AXIS));
        navButtonsPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        btnPerfil            = botonNavegacion("Mi Perfil");
        btnGestionUsuarios   = botonNavegacion("Gestionar Usuarios");
        btnGestionProgramas  = botonNavegacion("Gestionar Programas");
        btnGestionConvenios  = botonNavegacion("Gestionar Convenios");
        btnAnalisis          = botonNavegacion("Análisis");

        ButtonGroup grp = new ButtonGroup();
        grp.add(btnPerfil);
        grp.add(btnGestionUsuarios);
        grp.add(btnGestionProgramas);
        grp.add(btnGestionConvenios);
        grp.add(btnAnalisis);
        btnPerfil.setSelected(true);

        // Separadores suaves
        JSeparator sep1 = new JSeparator();
        sep1.setForeground(new Color(0x333333));
        sep1.setAlignmentX(Component.CENTER_ALIGNMENT);
        JSeparator sep2 = new JSeparator();
        sep2.setForeground(new Color(0x333333));
        sep2.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Filas con franja
        navButtonsPanel.add(makeNavItem(btnPerfil));
        navButtonsPanel.add(Box.createVerticalStrut(8));
        navButtonsPanel.add(makeNavItem(btnGestionUsuarios));
        navButtonsPanel.add(Box.createVerticalStrut(12));
        navButtonsPanel.add(sep1);
        navButtonsPanel.add(Box.createVerticalStrut(12));
        navButtonsPanel.add(makeNavItem(btnGestionProgramas));
        navButtonsPanel.add(Box.createVerticalStrut(8));
        navButtonsPanel.add(makeNavItem(btnGestionConvenios));
        navButtonsPanel.add(Box.createVerticalStrut(12));
        navButtonsPanel.add(sep2);
        navButtonsPanel.add(Box.createVerticalStrut(12));
        navButtonsPanel.add(makeNavItem(btnAnalisis));

        // Contenido scrolleable del sidebar (para pantallas pequeñas)
        JPanel scrollContent = new JPanel(new BorderLayout());
        scrollContent.setOpaque(false);
        scrollContent.add(topPanel, BorderLayout.NORTH);

        JPanel navCenter = new JPanel();
        navCenter.setOpaque(false);
        navCenter.setLayout(new BoxLayout(navCenter, BoxLayout.Y_AXIS));
        navCenter.add(Box.createVerticalGlue());
        navButtonsPanel.setAlignmentX(0.5f);
        navCenter.add(navButtonsPanel);
        navCenter.add(Box.createVerticalGlue());

        scrollContent.add(navCenter, BorderLayout.CENTER);

        JScrollPane sideScroll = new JScrollPane(
                scrollContent,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        sideScroll.setBorder(null);
        sideScroll.setViewportBorder(null);
        sideScroll.setOpaque(false);
        if (sideScroll.getViewport() != null) sideScroll.getViewport().setOpaque(false);
        sideScroll.getVerticalScrollBar().setUnitIncrement(18);

        panelIzquierdo.add(sideScroll, BorderLayout.CENTER);

        // Footer (Hola + botones)
        JPanel bottomPanel = new JPanel(new MigLayout("wrap, fillx, insets 24 24 32 24", "[fill]"));
        bottomPanel.setOpaque(false);
        lblSidebarNombre = new JLabel("Hola, Auditor " + safe(auditor.getNombreCompleto()).split(" ")[0] + "!");
        lblSidebarNombre.setHorizontalAlignment(SwingConstants.CENTER);
        lblSidebarNombre.putClientProperty(FlatClientProperties.STYLE, "font:bold +3; foreground:lighten(@foreground,25%)");

        JPanel actions = new JPanel(new GridLayout(1, 2, 14, 0));
        actions.setOpaque(false);

        JButton btnCerrar = new JButton("Cerrar Sesión");
        styleRectButtonPrimary(btnCerrar, new Color(0x2E86FF));
        btnCerrar.setPreferredSize(new Dimension(0, 72));

        JButton btnSalir = new JButton("Salir");
        styleRectButtonPrimary(btnSalir, new Color(0xE42828));
        btnSalir.setPreferredSize(new Dimension(0, 72));

        actions.add(btnCerrar);
        actions.add(btnSalir);

        bottomPanel.add(lblSidebarNombre, "growx, gaptop 12");
        bottomPanel.add(actions, "growx, height 72, gaptop 18");

        panelIzquierdo.add(bottomPanel, BorderLayout.SOUTH);

        // Centro (cards)
        perfilPanel = new PerfilPanel(gestor, auditor);
        gestionProgramasPanel = new GestionProgramasPanel(gestor);
        gestionConveniosPanel = new GestionConveniosAuditorPanel(gestor, auditor);
        JPanel analisisPanel = new JPanel();
        analisisPanel.add(new JLabel("Módulo de Análisis y Reportes (en desarrollo)"));

        centerCards.add(perfilPanel, CARD_PERFIL);
        centerCards.add(gestionProgramasPanel, CARD_GESTION_PROGRAMAS);
        centerCards.add(gestionConveniosPanel, CARD_GESTION_CONVENIOS);
        centerCards.add(analisisPanel, CARD_ANALISIS);

        // Layout principal (sidebar fijo)
        setLayout(new BorderLayout());
        add(panelIzquierdo, BorderLayout.WEST);
        add(centerCards, BorderLayout.CENTER);

        // Listeners
        btnPerfil.addActionListener(e -> {
            centerCardsLayout.show(centerCards, CARD_PERFIL);
            btnPerfil.setSelected(true);
        });
        btnGestionUsuarios.addActionListener(e -> {
            mostrarDialogoCrearUsuario();
            btnGestionUsuarios.setSelected(true);
        });
        btnGestionProgramas.addActionListener(e -> {
            gestionProgramasPanel.refresh();
            centerCardsLayout.show(centerCards, CARD_GESTION_PROGRAMAS);
            btnGestionProgramas.setSelected(true);
        });
        btnGestionConvenios.addActionListener(e -> {
            gestionConveniosPanel.refresh();
            centerCardsLayout.show(centerCards, CARD_GESTION_CONVENIOS);
            btnGestionConvenios.setSelected(true);
        });
        btnAnalisis.addActionListener(e -> {
            centerCardsLayout.show(centerCards, CARD_ANALISIS);
            btnAnalisis.setSelected(true);
        });

        btnCerrar.addActionListener(e -> onLogout.run());
        btnSalir.addActionListener(e -> System.exit(0));
    }

    private void mostrarDialogoCrearUsuario() {
        JTextField rutField = new JTextField();
        JTextField nombreField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JComboBox<Rol> rolComboBox = new JComboBox<>(new Rol[]{Rol.FUNCIONARIO, Rol.AUDITOR});

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("RUT (sin puntos ni guion):"));
        panel.add(rutField);
        panel.add(new JLabel("Nombre Completo:"));
        panel.add(nombreField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Contraseña Temporal:"));
        panel.add(passField);
        panel.add(new JLabel("Rol:"));
        panel.add(rolComboBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Crear Nuevo Usuario", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                gestor.getServicioAutenticacion().crearUsuarioAdministrativo(
                        rutField.getText().trim(),
                        nombreField.getText().trim(),
                        emailField.getText().trim(),
                        new String(passField.getPassword()),
                        (Rol) rolComboBox.getSelectedItem()
                );
                JOptionPane.showMessageDialog(this, "Usuario creado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al crear usuario: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "" : s.trim();
    }
}
