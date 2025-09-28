package ui;

import com.formdev.flatlaf.FlatClientProperties;
import gestores.GestorIntercambio;
import modelo.Usuario;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * **Panel de Interfaz de Usuario (Dashboard) para el Rol de Funcionario.**
 * <p>Centraliza las herramientas administrativas clave para el Funcionario (Perfil,
 * Gestión de Postulaciones, y Consulta/Selección de Convenios). Utiliza un diseño
 * de barra lateral y {@link CardLayout} para la navegación.</p>
 * <p>Implementa lógica de flujo de trabajo que permite seleccionar un convenio
 * desde la vista de convenios y redirigir el foco a la gestión de postulaciones,
 * aplicando automáticamente un filtro sobre las postulaciones asociadas.</p>
 */
public class FuncionarioPanel extends JPanel {

    private final GestorIntercambio gestor;
    private final Usuario funcionario;
    private final Runnable onLogout;

    private JLabel lblSidebarNombre;

    private final CardLayout centerCardsLayout = new CardLayout();
    private final JPanel centerCards = new JPanel(centerCardsLayout);

    private static final String CARD_PERFIL = "perfil";
    private static final String CARD_GESTION_POSTULACIONES = "gestionPostulaciones";
    private static final String CARD_VER_CONVENIOS = "verConvenios";

    private PerfilPanel perfilPanel;
    private PostulacionesFuncionarioPanel postulacionesPanel;
    private ConveniosPanel conveniosPanel;

    private JToggleButton btnPerfil;
    private JToggleButton btnGestionPost;
    private JToggleButton btnVerConvenios;

    /**
     * Crea e inicializa el panel principal del Funcionario.
     * @param gestor El gestor central de la aplicación.
     * @param funcionario El usuario Funcionario autenticado.
     * @param onLogout El {@code Runnable} que maneja la transición a la pantalla de login.
     */
    public FuncionarioPanel(GestorIntercambio gestor, Usuario funcionario, Runnable onLogout) {
        this.gestor = gestor;
        this.funcionario = funcionario;
        this.onLogout = onLogout;
        init();
        refreshSidebar();
    }

    private JToggleButton botonNavegacion(String text) {
        JToggleButton b = new JToggleButton(text);
        b.setHorizontalAlignment(SwingConstants.CENTER);
        b.setFocusPainted(false);
        b.setOpaque(true);

        Insets padding = new Insets(20, 28, 20, 28);
        b.setMargin(padding);
        b.setBorder(BorderFactory.createEmptyBorder(padding.top, padding.left, padding.bottom, padding.right));

        b.setMinimumSize(new Dimension(0, 88));
        b.setPreferredSize(new Dimension(0, 88));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);

        b.setFont(b.getFont().deriveFont(Font.BOLD, b.getFont().getSize2D() + 3f));
        b.setForeground(UIManager.getColor("Label.foreground"));

        wireToggleBehavior(b);
        addHoverEffect(b);
        return b;
    }

    /** Selección: fondo azul + franja derecha */
    private void wireToggleBehavior(JToggleButton b) {
        final Color selectedBg = new Color(0x2E86FF);
        final Color selectedFg = Color.WHITE;
        final Color unselectedFg = UIManager.getColor("Label.foreground");
        final Color rightStripe = new Color(0x1F5FCC);

        b.setContentAreaFilled(false);

        b.addChangeListener(e -> {
            boolean sel = b.isSelected();
            if (sel) {
                b.setContentAreaFilled(true);
                b.setBackground(selectedBg);
                b.setForeground(selectedFg);
                b.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 0, 6, rightStripe),
                        BorderFactory.createEmptyBorder(20, 28, 20, 22) // compensa franja
                ));
            } else {
                b.setContentAreaFilled(false);
                b.setForeground(unselectedFg);
                b.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));
            }
        });
    }

    /** Hover: sombrea la opción si NO está seleccionada */
    private void addHoverEffect(JToggleButton b) {
        final Color hoverBg = new Color(0x2F2F2F); // un poco más claro que #262626
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

    /** Botón rectangular (footer) */
    private static void styleRectButtonPrimary(JButton b, Color bg) {
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(b.getFont().deriveFont(Font.BOLD, b.getFont().getSize2D() + 2f));
        b.setMargin(new Insets(16, 20, 16, 20));
        b.setBorder(BorderFactory.createLineBorder(bg.darker(), 1, false));
    }

    private void init() {
        // ===== Sidebar =====
        JPanel panelIzquierdo = new JPanel(new BorderLayout());
        panelIzquierdo.setBackground(new Color(0x262626));
        panelIzquierdo.setOpaque(true);
        panelIzquierdo.setPreferredSize(new Dimension(360, 0));

        // Top: logo grande + separador
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
        separator.setBackground(new Color(0x3A3A3A));
        topPanel.add(separator, "growx, gaptop 12");

        // Centro: botones
        JPanel navButtonsPanel = new JPanel();
        navButtonsPanel.setOpaque(false);
        navButtonsPanel.setLayout(new BoxLayout(navButtonsPanel, BoxLayout.Y_AXIS));
        navButtonsPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        btnPerfil       = botonNavegacion("Mi Perfil");
        btnGestionPost  = botonNavegacion("Gestionar Postulaciones");
        btnVerConvenios = botonNavegacion("Ver Convenios");

        ButtonGroup grp = new ButtonGroup();
        grp.add(btnPerfil);
        grp.add(btnGestionPost);
        grp.add(btnVerConvenios);
        btnPerfil.setSelected(true);

        navButtonsPanel.add(Box.createVerticalGlue());
        navButtonsPanel.add(btnPerfil);
        navButtonsPanel.add(Box.createVerticalStrut(12));
        navButtonsPanel.add(btnGestionPost);
        navButtonsPanel.add(Box.createVerticalStrut(12));
        navButtonsPanel.add(btnVerConvenios);
        navButtonsPanel.add(Box.createVerticalGlue());

        panelIzquierdo.add(topPanel, BorderLayout.NORTH);
        panelIzquierdo.add(navButtonsPanel, BorderLayout.CENTER);

        // Bottom: saludo + dos botones (Cerrar sesión / Salir)
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

        // ===== Centro (cards) =====
        perfilPanel = new PerfilPanel(gestor, funcionario);
        conveniosPanel = new ConveniosPanel(gestor, this::onConvenioSeleccionado);
        postulacionesPanel = new PostulacionesFuncionarioPanel(gestor, funcionario);

        centerCards.add(perfilPanel, CARD_PERFIL);
        centerCards.add(postulacionesPanel, CARD_GESTION_POSTULACIONES);
        centerCards.add(conveniosPanel, CARD_VER_CONVENIOS);
        centerCardsLayout.show(centerCards, CARD_PERFIL);

        // ===== Layout exterior =====
        setLayout(new BorderLayout());
        add(panelIzquierdo, BorderLayout.WEST);
        add(centerCards, BorderLayout.CENTER);

        // ===== Listeners (misma lógica) =====
        btnPerfil.addActionListener(e -> {
            perfilPanel.refreshData();
            centerCardsLayout.show(centerCards, CARD_PERFIL);
            btnPerfil.setSelected(true);
        });
        btnGestionPost.addActionListener(e -> {
            postulacionesPanel.refreshTodasLasPostulaciones();
            centerCardsLayout.show(centerCards, CARD_GESTION_POSTULACIONES);
            btnGestionPost.setSelected(true);
        });
        btnVerConvenios.addActionListener(e -> {
            conveniosPanel.refresh();
            centerCardsLayout.show(centerCards, CARD_VER_CONVENIOS);
            btnVerConvenios.setSelected(true);
        });
        btnCerrar.addActionListener(e -> onLogout.run());
        btnSalir.addActionListener(e -> System.exit(0));
    }

    /** Callback desde ConveniosPanel para cambiar con filtro aplicado */
    private void onConvenioSeleccionado(String idConvenio) {
        postulacionesPanel.filtrarPorConvenio(idConvenio);
        centerCardsLayout.show(centerCards, CARD_GESTION_POSTULACIONES);
        btnGestionPost.setSelected(true);
    }

    private void refreshSidebar() {
        if (funcionario == null) return;
        String nombre = safe(funcionario.getNombreCompleto());
        lblSidebarNombre.setText("Hola, Funcionario " + (nombre.isEmpty() ? "" : nombre.split(" ")[0]) + "!");
    }

    private static String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "" : s.trim();
    }
}
