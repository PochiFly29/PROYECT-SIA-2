package ui;

import com.formdev.flatlaf.FlatClientProperties;
import gestores.GestorIntercambio;
import modelo.Estudiante;
import modelo.Usuario; // Necesario para el contexto de roles
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.MatteBorder;

public class EstudiantePanel extends JPanel {

    private final GestorIntercambio gestor;
    private Estudiante estudiante; // ¡CORREGIDO: Ahora es el objeto Estudiante!
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

    // ¡CORREGIDO: Recibe Estudiante!
    public EstudiantePanel(GestorIntercambio gestor, Estudiante estudiante, Runnable onLogout) {
        this.gestor = gestor;
        this.estudiante = estudiante;
        this.onLogout = onLogout;
        init();
        refreshSidebar();
    }

    // Método de soporte si la instancia cambia (aunque el objeto es el mismo, es útil)
    public void setEstudiante(Estudiante e) {
        this.estudiante = e;
        refreshSidebar();
        if (perfilPanel != null) perfilPanel.setUsuario(e); // Asumiendo que PerfilPanel acepta Usuario/Estudiante
    }

    // Método que permite refrescar el panel desde fuera si hay un cambio de datos
    public void refreshData() {
        if (perfilPanel != null) perfilPanel.refreshData();
        if (postulacionesPanel != null) postulacionesPanel.refresh();
    }


    private void init() {
        // ===== Barra izquierda =====
        JPanel panelIzquierdo = new JPanel(new BorderLayout());
        panelIzquierdo.putClientProperty(FlatClientProperties.STYLE, "background:lighten(@background,3%)");
        panelIzquierdo.setPreferredSize(new Dimension(280, 0));

        // ... (Configuración del panelIzquierdo igual) ...

        // Panel superior para el logo y título
        JPanel topPanel = new JPanel(new MigLayout("wrap, fillx, insets 16 24 16 24", "fill"));
        topPanel.setOpaque(false);
        //
        try {
            ImageIcon logoIcon = new ImageIcon(getClass().getResource("/logo.png"));
            Image scaledImage = logoIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            JLabel lblLogo = new JLabel(new ImageIcon(scaledImage));
            lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
            topPanel.add(lblLogo, "growx, center, wrap, gaptop 8");
        } catch (Exception e) {
            System.err.println("No se pudo cargar el logo. Asegúrate de que el archivo 'logo.png' esté en la carpeta 'src/main/resources'.");
        }

        // Título
        JLabel lblTitulo = new JLabel("Gestiones de Intercambio");
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitulo.putClientProperty(FlatClientProperties.STYLE, "font:bold +1");
        topPanel.add(lblTitulo, "growx, center");

        // Separador
        JPanel separator = new JPanel();
        separator.setPreferredSize(new Dimension(0, 1));
        separator.setBackground(UIManager.getColor("Component.borderColor"));
        topPanel.add(separator, "growx, gaptop 8");

        // Panel de botones de navegación (CENTER)
        JPanel navButtonsPanel = new JPanel(new GridLayout(0, 1, 0, 12));
        navButtonsPanel.setOpaque(false);
        navButtonsPanel.setBorder(BorderFactory.createEmptyBorder(50, 16, 12, 16));

        JButton btnPerfil = new JButton("Perfil");
        JButton btnVerPost = new JButton("Ver Postulaciones");
        JButton btnPostular = new JButton("Postular a un convenio");

        String buttonStyle = "background:#2E86FF; foreground:#FFFFFF; font:bold +1; borderWidth:0; focusWidth:0; innerFocusWidth:0";
        btnPerfil.putClientProperty(FlatClientProperties.STYLE, buttonStyle);
        btnVerPost.putClientProperty(FlatClientProperties.STYLE, buttonStyle);
        btnPostular.putClientProperty(FlatClientProperties.STYLE, buttonStyle);

        btnPerfil.setPreferredSize(new Dimension(180, 40));
        btnVerPost.setPreferredSize(new Dimension(180, 40));
        btnPostular.setPreferredSize(new Dimension(180, 40));

        navButtonsPanel.add(btnPerfil);
        navButtonsPanel.add(btnVerPost);
        navButtonsPanel.add(btnPostular);

        // Panel inferior para la info del usuario y el botón de cerrar sesión (SOUTH)
        JPanel bottomPanel = new JPanel(new MigLayout("wrap, fillx, insets 16 24 16 24", "fill"));
        bottomPanel.setOpaque(false);
        lblSidebarNombre = new JLabel();
        lblSidebarNombre.setHorizontalAlignment(SwingConstants.CENTER);
        lblSidebarNombre.putClientProperty(FlatClientProperties.STYLE, "font:bold; foreground:lighten(@foreground,20%)");

        JButton btnCerrar = new JButton("Cerrar Sesion");
        btnCerrar.putClientProperty(FlatClientProperties.STYLE, "background:#E42828; foreground:#FFFFFF; arc:999");
        btnCerrar.putClientProperty(FlatClientProperties.BUTTON_TYPE, "destructive");

        bottomPanel.add(lblSidebarNombre, "growx, gaptop 16");
        bottomPanel.add(btnCerrar, "growx, height 40, gaptop 16");

        // Añade los paneles al panel principal izquierdo
        panelIzquierdo.add(topPanel, BorderLayout.NORTH);
        panelIzquierdo.add(navButtonsPanel, BorderLayout.CENTER);
        panelIzquierdo.add(bottomPanel, BorderLayout.SOUTH);

        // ===== Centro (cards) =====
        // ¡CORREGIDO: Se pasan los objetos Estudiante correspondientes!
        perfilPanel = new PerfilPanel(gestor, estudiante);
        postulacionesPanel = new PostulacionesPanel(gestor, estudiante);
        postularPanel = new PostularPanel(gestor, estudiante);

        centerCards.add(perfilPanel, CARD_PERFIL);
        centerCards.add(postulacionesPanel, CARD_POSTULACIONES);
        centerCards.add(postularPanel, CARD_POSTULAR);
        centerCardsLayout.show(centerCards, CARD_PERFIL);

        // ===== Layout exterior =====
        setLayout(new BorderLayout());
        add(panelIzquierdo, BorderLayout.WEST);
        add(centerCards, BorderLayout.CENTER);

        // Nav
        btnPerfil.addActionListener(e -> {
            postulacionesPanel.refresh();
            centerCardsLayout.show(centerCards, CARD_PERFIL);
        });
        btnVerPost.addActionListener(e -> {
            postulacionesPanel.refresh();
            centerCardsLayout.show(centerCards, CARD_POSTULACIONES);
        });
        btnPostular.addActionListener(e -> {
            postularPanel.refresh();
            centerCardsLayout.show(centerCards, CARD_POSTULAR);
        });
        btnCerrar.addActionListener(e -> onLogout.run());
    }

    private void refreshSidebar() {
        if (estudiante == null) return;
        String nombre = safe(estudiante.getNombreCompleto());
        lblSidebarNombre.setText("Hola, " + nombre.split(" ")[0] + "!");
    }

    private static String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "" : s.trim();
    }
}