package ui;

import com.formdev.flatlaf.FlatClientProperties;
import gestores.GestorIntercambio;
import modelo.Estudiante;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class EstudiantePanel extends JPanel {

    private final GestorIntercambio gestor;
    private Estudiante estudiante;
    private final Runnable onLogout;

    private JLabel lblSidebarNombre;

    private final CardLayout centerCardsLayout = new CardLayout();
    private final JPanel centerCards = new JPanel(centerCardsLayout);
    private static final String CARD_PERFIL = "perfil";
    private static final String CARD_POSTULACIONES = "postulaciones";
    private static final String CARD_POSTULAR = "postular";

    private PerfilPanel perfilPanel;
    private PostularPanel postularPanel;
    private PostulacionesPanel postulacionesPanel;

    private JToggleButton btnPerfil;
    private JToggleButton btnVerPost;
    private JToggleButton btnPostular;

    public EstudiantePanel(GestorIntercambio gestor, Estudiante estudiante, Runnable onLogout) {
        this.gestor = gestor;
        this.estudiante = estudiante;
        this.onLogout = onLogout;
        init();
        refreshSidebar();
    }

    public void setEstudiante(Estudiante e) {
        this.estudiante = e;
        refreshSidebar();

        if (perfilPanel != null) {
            perfilPanel.setUsuario(e);
        }

        // Re-construir paneles dependientes del estudiante
        PostulacionesPanel nuevoPostulaciones = new PostulacionesPanel(gestor, e);
        replaceCard(CARD_POSTULACIONES, postulacionesPanel, nuevoPostulaciones);
        postulacionesPanel = nuevoPostulaciones;

        PostularPanel nuevoPostular = new PostularPanel(gestor, e);
        replaceCard(CARD_POSTULAR, postularPanel, nuevoPostular);
        postularPanel = nuevoPostular;
    }

    public void refreshData() {
        if (perfilPanel != null) perfilPanel.refreshData();
        if (postulacionesPanel != null) postulacionesPanel.refresh();
    }

    // ---------- UI helpers ----------

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
        addHoverEffect(b);   // hover visual
        return b;
    }

    /** Fila: [botón][franja derecha 6px] para garantizar la franja de selección */
    private JPanel makeNavItem(JToggleButton b) {
        JPanel row = new JPanel(new MigLayout("insets 0, gap 0, fill", "[grow,fill][6!]", "[fill]"));
        row.setOpaque(false);
        row.setMinimumSize(new Dimension(0, 72));
        row.setPreferredSize(new Dimension(0, 72));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        row.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel stripe = new JPanel();
        stripe.setOpaque(true);
        stripe.setBackground(new Color(0x4A95FF)); // azul claro
        stripe.setVisible(b.isSelected());

        b.putClientProperty("stripe", stripe);

        row.add(b, "cell 0 0, grow");
        row.add(stripe, "cell 1 0, growy");
        return row;
    }

    /** Selección: fondo azul + controla visibilidad de la franja */
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

    /** Hover: sombrea la opción si NO está seleccionada */
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

    private void replaceCard(String name, JComponent oldComp, JComponent newComp) {
        if (oldComp != null) centerCards.remove(oldComp);
        centerCards.add(newComp, name);
        centerCards.revalidate();
        centerCards.repaint();
    }

    // ---------- Init ----------

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

            int maxW = 330, maxH = 280;
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

        JPanel separator = new JPanel();
        separator.setPreferredSize(new Dimension(0, 1));
        separator.setBackground(new Color(0x333333));
        topPanel.add(separator, "growx, gaptop 12");

        // Navegación
        JPanel navButtonsPanel = new JPanel();
        navButtonsPanel.setOpaque(false);
        navButtonsPanel.setLayout(new BoxLayout(navButtonsPanel, BoxLayout.Y_AXIS));
        navButtonsPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        btnPerfil   = botonNavegacion("Perfil");
        btnPostular = botonNavegacion("Postular a un convenio");
        btnVerPost  = botonNavegacion("Ver postulaciones");

        ButtonGroup grp = new ButtonGroup();
        grp.add(btnPerfil);
        grp.add(btnPostular);
        grp.add(btnVerPost);
        btnPerfil.setSelected(true);

        // Separador suave entre grupos (opcional)
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x333333));
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);

        navButtonsPanel.add(makeNavItem(btnPerfil));
        navButtonsPanel.add(Box.createVerticalStrut(12));
        navButtonsPanel.add(makeNavItem(btnPostular));
        navButtonsPanel.add(Box.createVerticalStrut(12));
        navButtonsPanel.add(sep);
        navButtonsPanel.add(Box.createVerticalStrut(12));
        navButtonsPanel.add(makeNavItem(btnVerPost));

        // Contenido scrolleable del sidebar
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

        // Footer
        JPanel bottomPanel = new JPanel(new MigLayout("wrap, fillx, insets 24 24 32 24", "[fill]"));
        bottomPanel.setOpaque(false);
        lblSidebarNombre = new JLabel();
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
        perfilPanel = new PerfilPanel(gestor, estudiante);
        postulacionesPanel = new PostulacionesPanel(gestor, estudiante);
        postularPanel = new PostularPanel(gestor, estudiante);

        centerCards.add(perfilPanel, CARD_PERFIL);
        centerCards.add(postulacionesPanel, CARD_POSTULACIONES);
        centerCards.add(postularPanel, CARD_POSTULAR);
        centerCardsLayout.show(centerCards, CARD_PERFIL);

        // Layout principal (sidebar fijo)
        setLayout(new BorderLayout());
        add(panelIzquierdo, BorderLayout.WEST);
        add(centerCards, BorderLayout.CENTER);

        // Navegación (misma lógica)
        btnPerfil.addActionListener(e -> {
            centerCardsLayout.show(centerCards, CARD_PERFIL);
            btnPerfil.setSelected(true);
        });
        btnVerPost.addActionListener(e -> {
            postulacionesPanel.refresh();
            centerCardsLayout.show(centerCards, CARD_POSTULACIONES);
            btnVerPost.setSelected(true);
        });
        btnPostular.addActionListener(e -> {
            postularPanel.refresh();
            centerCardsLayout.show(centerCards, CARD_POSTULAR);
            btnPostular.setSelected(true);
        });

        btnCerrar.addActionListener(e -> onLogout.run());
        btnSalir.addActionListener(e -> System.exit(0));
    }

    private void refreshSidebar() {
        if (estudiante == null) return;
        String nombre = safe(estudiante.getNombreCompleto());
        lblSidebarNombre.setText("Hola, " + (nombre.isEmpty() ? "" : nombre.split(" ")[0]) + "!");
    }

    private static String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "" : s.trim();
    }
}
