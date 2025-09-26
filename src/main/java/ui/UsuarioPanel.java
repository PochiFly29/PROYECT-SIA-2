package ui;

import com.formdev.flatlaf.FlatClientProperties;
import enums.Rol;
import gestores.GestorIntercambio;
import modelo.Usuario;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class UsuarioPanel extends JPanel {

    private static final class Permisos {
        final boolean verCatalogo;
        final boolean postular;
        final boolean verPostPropias;
        final boolean verPostTodas;

        private Permisos(boolean verCatalogo, boolean postular, boolean verPostPropias, boolean verPostTodas) {
            this.verCatalogo = verCatalogo;
            this.postular = postular;
            this.verPostPropias = verPostPropias;
            this.verPostTodas = verPostTodas;
        }
        static Permisos para(Rol r) {
            if (r == Rol.ESTUDIANTE) return new Permisos(true,true, true,false);
            if (r == Rol.FUNCIONARIO) return new Permisos(true,false, false,true );
            if (r == Rol.AUDITOR) return new Permisos(false,false,false,true );
            return new Permisos(false,false,false,false);
        }
    }

    private final GestorIntercambio gestor;
    private Usuario usuario;
    private final Runnable onLogout;

    // UI izquierda
    private JLabel lblSidebarNombre;
    private JButton btnPerfil;
    private JButton btnPostulaciones;
    private JButton btnCatalogo;

    private final CardLayout cardsLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardsLayout);
    private static final String CARD_PERFIL = "perfil";
    private static final String CARD_POSTULACIONES = "postulaciones";
    private static final String CARD_CATALOGO = "catalogo";

    private PerfilPanel perfilPanel;
    private PostularPanel postularPanel;
    private PostulacionesPanel postulacionesPanel;

    private String currentCard = CARD_PERFIL;

    public UsuarioPanel(GestorIntercambio gestor, Usuario usuario, Runnable onLogout) {
        this.gestor = Objects.requireNonNull(gestor);
        this.usuario = Objects.requireNonNull(usuario);
        this.onLogout = onLogout;
        init();
        applyUsuario();
    }

    public void setUsuario(Usuario u) {
        this.usuario = Objects.requireNonNull(u);
        if (perfilPanel != null)perfilPanel.setUsuario(u);
        if (postulacionesPanel != null)postulacionesPanel.setUsuario(u);
        if (postularPanel != null)postularPanel.setUsuario(u);
        applyUsuario();
    }

    private void init() {
        setLayout(new BorderLayout());

        JPanel panelIzquierdo = new JPanel(new BorderLayout());
        panelIzquierdo.putClientProperty(FlatClientProperties.STYLE, "background:lighten(@background,3%)");
        panelIzquierdo.setPreferredSize(new Dimension(280, 0));
        panelIzquierdo.setOpaque(true);

        JPanel topPanel = new JPanel(new MigLayout("wrap, fillx, insets 16 24 8 24", "fill"));
        topPanel.setOpaque(false);

        try {
            ImageIcon logoIcon = new ImageIcon(getClass().getResource("/logo.png"));
            Image scaledImage = logoIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            JLabel lblLogo = new JLabel(new ImageIcon(scaledImage));
            lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
            topPanel.add(lblLogo, "growx, center, wrap, gaptop 8");
        } catch (Exception ignored) {
        }

        JLabel lblTitulo = new JLabel("Gestiones de Intercambio");
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitulo.putClientProperty(FlatClientProperties.STYLE, "font:bold +1");
        topPanel.add(lblTitulo, "growx, center");

        JPanel separator = new JPanel();
        separator.setPreferredSize(new Dimension(0, 1));
        separator.setBackground(UIManager.getColor("Component.borderColor"));
        topPanel.add(separator, "growx, gaptop 8");

        panelIzquierdo.add(topPanel, BorderLayout.NORTH);

        JPanel navButtonsPanel = new JPanel(new GridLayout(0, 1, 0, 12));
        navButtonsPanel.setOpaque(false);
        navButtonsPanel.setBorder(BorderFactory.createEmptyBorder(32, 16, 12, 16));

        btnPerfil = new JButton("Perfil");
        btnPostulaciones  = new JButton("Ver postulaciones");
        btnCatalogo = new JButton("Catálogo de convenios");

        String buttonStyle = "background:#2E86FF; foreground:#FFFFFF; font:bold +1; borderWidth:0; focusWidth:0; innerFocusWidth:0; arc:999";
        btnPerfil.putClientProperty(FlatClientProperties.STYLE, buttonStyle);
        btnPostulaciones.putClientProperty(FlatClientProperties.STYLE, buttonStyle);
        btnCatalogo.putClientProperty(FlatClientProperties.STYLE, buttonStyle);

        btnPerfil.setPreferredSize(new Dimension(180, 40));
        btnPostulaciones.setPreferredSize(new Dimension(180, 40));
        btnCatalogo.setPreferredSize(new Dimension(180, 40));

        navButtonsPanel.add(btnPerfil);
        navButtonsPanel.add(btnPostulaciones);
        navButtonsPanel.add(btnCatalogo);
        panelIzquierdo.add(navButtonsPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new MigLayout("wrap, fillx, insets 16 24 24 24", "fill"));
        bottomPanel.setOpaque(false);

        lblSidebarNombre = new JLabel("Usuario");
        lblSidebarNombre.setHorizontalAlignment(SwingConstants.CENTER);
        lblSidebarNombre.putClientProperty(FlatClientProperties.STYLE, "font:bold; foreground:lighten(@foreground,20%)");

        JButton btnCerrar = new JButton("Cerrar Sesión");
        btnCerrar.putClientProperty(FlatClientProperties.STYLE, "background:#E42828; foreground:#FFFFFF; arc:999");
        btnCerrar.putClientProperty(FlatClientProperties.BUTTON_TYPE, "destructive");
        btnCerrar.addActionListener(e -> { if (onLogout != null) onLogout.run(); });

        bottomPanel.add(lblSidebarNombre, "growx, gaptop 12");
        bottomPanel.add(btnCerrar, "growx, height 40, gaptop 12");

        panelIzquierdo.add(bottomPanel, BorderLayout.SOUTH);

        add(panelIzquierdo, BorderLayout.WEST);

        // Centro
        perfilPanel = new PerfilPanel(usuario);
        postulacionesPanel = new PostulacionesPanel(gestor, usuario);
        postularPanel = new PostularPanel(gestor, usuario);

        try {
            postularPanel.setOnVerPostulacionesPorConvenio(convenioId -> {
                postulacionesPanel.setFiltroConvenio(convenioId);
                postulacionesPanel.setUsuario(usuario);
                postulacionesPanel.refresh();
                currentCard = CARD_POSTULACIONES;
                cardsLayout.show(cards, CARD_POSTULACIONES);
            });
        } catch (NoSuchMethodError | Exception ignored) {
        }

        cards.add(perfilPanel, CARD_PERFIL);
        cards.add(postulacionesPanel, CARD_POSTULACIONES);
        cards.add(postularPanel, CARD_CATALOGO);

        add(cards, BorderLayout.CENTER);
        cardsLayout.show(cards, currentCard);

        // Navegacion
        btnPerfil.addActionListener(e -> {
            currentCard = CARD_PERFIL;
            cardsLayout.show(cards, CARD_PERFIL);
        });

        btnPostulaciones.addActionListener(e -> {
            postulacionesPanel.setUsuario(usuario);
            postulacionesPanel.setFiltroConvenio(null);
            postulacionesPanel.refresh();
            currentCard = CARD_POSTULACIONES;
            cardsLayout.show(cards, CARD_POSTULACIONES);
        });

        btnCatalogo.addActionListener(e -> {
            if (postularPanel != null) postularPanel.setUsuario(usuario);
            currentCard = CARD_CATALOGO;
            cardsLayout.show(cards, CARD_CATALOGO);
        });
    }

    private void applyUsuario() {
        // Saludo
        String rolLegible = (usuario.getRol() != null) ? toTitulo(usuario.getRol().name()) : "Usuario";
        String nombreCorto = firstNameSafe(usuario.getNombreCompleto());
        lblSidebarNombre.setText(rolLegible + " • Hola, " + nombreCorto + "!");

        // permisos por rol
        Permisos p = Permisos.para(usuario.getRol());

        if (usuario.getRol() == Rol.ESTUDIANTE) {
            btnPerfil.setText("Ver mi Perfil");
            btnPostulaciones.setText("Ver mis Postulaciones");
            btnCatalogo.setText("Postular a un Convenio");
        } else if (usuario.getRol() == Rol.FUNCIONARIO) {
            btnPerfil.setText("Ver mi Perfil");
            btnPostulaciones.setText("Revisar Postulaciones");
            btnCatalogo.setText("Catálogo de convenios");
        } else {
            btnPerfil.setText("Perfil");
            btnPostulaciones.setText("Ver Postulaciones");
            btnCatalogo.setText("Catálogo de convenios");
        }

        // visibilidad por permisos
        btnCatalogo.setVisible(p.verCatalogo || p.postular);
        btnPostulaciones.setVisible(p.verPostPropias || p.verPostTodas);

        postulacionesPanel.setUsuario(usuario);
        if (postularPanel != null) postularPanel.setUsuario(usuario);

        enforceAccessibleCard(p);

        revalidate();
        repaint();
    }

    private void enforceAccessibleCard(Permisos p) {
        boolean perfilOk = true;
        boolean postulacionesOk = (p.verPostPropias || p.verPostTodas);
        boolean catalogoOk = (p.verCatalogo || p.postular);

        boolean needRedirect;
        if (CARD_PERFIL.equals(currentCard)) {
            needRedirect = false;
        } else if (CARD_POSTULACIONES.equals(currentCard)) {
            needRedirect = !postulacionesOk;
        } else if (CARD_CATALOGO.equals(currentCard)) {
            needRedirect = !catalogoOk;
        } else {
            needRedirect = true;
        }

        if (!needRedirect) return;

        if (perfilOk) currentCard = CARD_PERFIL;
        else if (postulacionesOk) currentCard = CARD_POSTULACIONES;
        else if (catalogoOk) currentCard = CARD_CATALOGO;
        else currentCard = CARD_PERFIL;

        cardsLayout.show(cards, currentCard);
    }

    private static String firstNameSafe(String s) {
        String t = (s == null) ? "" : s.trim();
        if (t.isEmpty()) return "-";
        int sp = t.indexOf(' ');
        return (sp > 0) ? t.substring(0, sp) : t;
    }
    private static String toTitulo(String enumName) {
        String lower = enumName.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}