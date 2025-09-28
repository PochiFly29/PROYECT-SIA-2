package ui;

import com.formdev.flatlaf.FlatClientProperties;
import gestores.GestorIntercambio;
import modelo.Usuario;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Panel principal para la vista de un funcionario.
 * Permite:
 *  - Navegar entre Perfil, Gestión de Postulaciones y Ver Convenios.
 *  - Mostrar nombre en la barra lateral.
 *  - Cerrar sesión.
 *
 * Internamente usa un CardLayout para cambiar entre subpaneles:
 *  - PerfilPanel
 *  - PostulacionesFuncionarioPanel
 *  - ConveniosPanel
 *
 * Recibe un GestorIntercambio para manejar datos, un Usuario (funcionario) y un callback de logout.
 */
public class FuncionarioPanel extends JPanel {

    private final GestorIntercambio gestor;
    private final Usuario funcionario;
    private final Runnable onLogout;

    private JLabel lblSidebarNombre;

    private final CardLayout centerCardsLayout = new CardLayout();
    private final JPanel centerCards = new JPanel(centerCardsLayout);

    // Nombres de las tarjetas
    private static final String CARD_PERFIL = "perfil";
    private static final String CARD_GESTION_POSTULACIONES = "gestionPostulaciones";
    private static final String CARD_VER_CONVENIOS = "verConvenios";

    // Paneles de contenido
    private PerfilPanel perfilPanel;
    private PostulacionesFuncionarioPanel postulacionesPanel;
    private ConveniosPanel conveniosPanel;

    /**
     * Crea la interfaz principal del funcionario con barra lateral y panel central.
     *
     * @param gestor GestorIntercambio para acceder a servicios y datos.
     * @param funcionario Usuario activo con rol de funcionario.
     * @param onLogout Runnable que se ejecuta al cerrar sesión.
     */
    public FuncionarioPanel(GestorIntercambio gestor, Usuario funcionario, Runnable onLogout) {
        this.gestor = gestor;
        this.funcionario = funcionario;
        this.onLogout = onLogout;
        init();
        refreshSidebar();
    }

    private void init() {
        // ===== Barra izquierda (Sidebar) =====
        JPanel panelIzquierdo = new JPanel(new BorderLayout());
        panelIzquierdo.putClientProperty(FlatClientProperties.STYLE, "background:lighten(@background,3%)");
        panelIzquierdo.setPreferredSize(new Dimension(280, 0));

        // CÓDIGO AÑADIDO: Panel superior para el logo y título (copiado de EstudiantePanel)
        JPanel topPanel = new JPanel(new MigLayout("wrap, fillx, insets 16 24 16 24", "fill"));
        topPanel.setOpaque(false);
        try {
            ImageIcon logoIcon = new ImageIcon(getClass().getResource("/logo.png"));
            Image scaledImage = logoIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            JLabel lblLogo = new JLabel(new ImageIcon(scaledImage));
            lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
            topPanel.add(lblLogo, "growx, center, wrap, gaptop 8");
        } catch (Exception e) {
            System.err.println("No se pudo cargar el logo. Asegúrate de que 'logo.png' esté en 'src/main/resources'.");
        }
        JLabel lblTitulo = new JLabel("Gestiones de Intercambio");
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitulo.putClientProperty(FlatClientProperties.STYLE, "font:bold +1");
        topPanel.add(lblTitulo, "growx, center");
        JPanel separator = new JPanel();
        separator.setPreferredSize(new Dimension(0, 1));
        separator.setBackground(UIManager.getColor("Component.borderColor"));
        topPanel.add(separator, "growx, gaptop 8");

        // Panel de botones de navegación (CENTER)
        JPanel navButtonsPanel = new JPanel(new GridLayout(0, 1, 0, 12));
        navButtonsPanel.setOpaque(false);
        navButtonsPanel.setBorder(BorderFactory.createEmptyBorder(50, 16, 12, 16));

        JButton btnPerfil = new JButton("Mi Perfil");
        JButton btnGestionPost = new JButton("Gestionar Postulaciones");
        JButton btnVerConvenios = new JButton("Ver Convenios");

        String buttonStyle = "background:#2E86FF; foreground:#FFFFFF; font:bold +1; borderWidth:0; focusWidth:0; innerFocusWidth:0";
        btnPerfil.putClientProperty(FlatClientProperties.STYLE, buttonStyle);
        btnGestionPost.putClientProperty(FlatClientProperties.STYLE, buttonStyle);
        btnVerConvenios.putClientProperty(FlatClientProperties.STYLE, buttonStyle);

        // CÓDIGO AÑADIDO: Se establece el mismo tamaño de botón que en EstudiantePanel
        Dimension buttonSize = new Dimension(180, 40);
        btnPerfil.setPreferredSize(buttonSize);
        btnGestionPost.setPreferredSize(buttonSize);
        btnVerConvenios.setPreferredSize(buttonSize);

        navButtonsPanel.add(btnPerfil);
        navButtonsPanel.add(btnGestionPost);
        navButtonsPanel.add(btnVerConvenios);

        // Panel inferior para info de usuario y botón de logout (SOUTH)
        JPanel bottomPanel = new JPanel(new MigLayout("wrap, fillx, insets 16 24 16 24", "fill"));
        bottomPanel.setOpaque(false);
        lblSidebarNombre = new JLabel();
        lblSidebarNombre.setHorizontalAlignment(SwingConstants.CENTER);
        lblSidebarNombre.putClientProperty(FlatClientProperties.STYLE, "font:bold; foreground:lighten(@foreground,20%)");
        JButton btnCerrar = new JButton("Cerrar Sesion");
        btnCerrar.putClientProperty(FlatClientProperties.STYLE, "background:#E42828; foreground:#FFFFFF; arc:999");
        bottomPanel.add(lblSidebarNombre, "growx, gaptop 16");
        bottomPanel.add(btnCerrar, "growx, height 40, gaptop 16");

        // Añade los paneles al sidebar
        panelIzquierdo.add(topPanel, BorderLayout.NORTH);
        panelIzquierdo.add(navButtonsPanel, BorderLayout.CENTER);
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

        // ===== Listeners de Navegación =====
        btnPerfil.addActionListener(e -> {
            perfilPanel.refreshData();
            centerCardsLayout.show(centerCards, CARD_PERFIL);
        });
        btnGestionPost.addActionListener(e -> {
            postulacionesPanel.refreshTodasLasPostulaciones();
            centerCardsLayout.show(centerCards, CARD_GESTION_POSTULACIONES);
        });
        btnVerConvenios.addActionListener(e -> {
            conveniosPanel.refresh();
            centerCardsLayout.show(centerCards, CARD_VER_CONVENIOS);
        });
        btnCerrar.addActionListener(e -> onLogout.run());
    }
    /**
     * Callback llamado por ConveniosPanel al seleccionar un convenio.
     * Cambia a la vista de postulaciones y aplica el filtro.
     */
    private void onConvenioSeleccionado(String idConvenio) {
        postulacionesPanel.filtrarPorConvenio(idConvenio);
        centerCardsLayout.show(centerCards, CARD_GESTION_POSTULACIONES);
    }

    private void refreshSidebar() {
        if (funcionario == null) return;
        String nombre = safe(funcionario.getNombreCompleto());
        lblSidebarNombre.setText("Hola, Funcionario " + nombre.split(" ")[0] + "!");
    }

    private static String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "" : s.trim();
    }
}