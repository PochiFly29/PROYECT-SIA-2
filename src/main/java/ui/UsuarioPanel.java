package ui;

import com.formdev.flatlaf.FlatClientProperties;
import enums.Rol;
import gestores.GestorIntercambio;
import modelo.Usuario;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class UsuarioPanel extends JPanel {

    // ====== permisos por rol ======
    private static final class Permisos {
        final boolean verCatalogo;
        final boolean postular;
        final boolean verPostPropias;
        final boolean verPostTodas;     // funcionario / auditor

        private Permisos(boolean verCatalogo, boolean postular, boolean verPostPropias, boolean verPostTodas) {
            this.verCatalogo = verCatalogo;
            this.postular = postular;
            this.verPostPropias = verPostPropias;
            this.verPostTodas = verPostTodas;
        }

        static Permisos para(Rol r) {
            if (r == Rol.ESTUDIANTE) return new Permisos(true,  true,  true,  false);
            if (r == Rol.FUNCIONARIO) return new Permisos(true,  false, false, true );
            if (r == Rol.AUDITOR) return new Permisos(false, false, false, true );
            return new Permisos(false,false,false,false);
        }
    }

    // ====== deps / estado ======
    private final GestorIntercambio gestor;
    private Usuario usuario;
    private final Runnable onLogout;

    // UI izquierda
    private JLabel lblSidebarNombre;
    private JButton btnPerfil;
    private JButton btnPostulaciones;
    private JButton btnCatalogo;

    // Centro con cards
    private final CardLayout cardsLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardsLayout);
    private static final String CARD_PERFIL        = "perfil";
    private static final String CARD_POSTULACIONES = "postulaciones";
    private static final String CARD_CATALOGO      = "catalogo";

    // Subvistas
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
        if (perfilPanel != null)        perfilPanel.setUsuario(u);
        if (postulacionesPanel != null) postulacionesPanel.setUsuario(u);
        if (postularPanel != null)      postularPanel.setUsuario(u);
        applyUsuario();
    }

    private void init() {
        setLayout(new BorderLayout());

        // ====== Sidebar ======
        JPanel panelCerrarSesion = new JPanel(new BorderLayout());
        JButton btnCerrar = new JButton("Cerrar Sesion");
        btnCerrar.setPreferredSize(new Dimension(180, 48));
        btnCerrar.putClientProperty(
                FlatClientProperties.STYLE,
                "background:#2E86FF; foreground:#FFFFFF; font:bold +2; borderWidth:0; focusWidth:0; innerFocusWidth:0"
        );
        btnCerrar.addActionListener(e -> { if (onLogout != null) onLogout.run(); });

        panelCerrarSesion.setBorder(BorderFactory.createEmptyBorder(50, 50, 30, 50));
        panelCerrarSesion.add(btnCerrar, BorderLayout.NORTH);

        btnPerfil = new JButton("Perfil");                btnPerfil.setBorder(null);
        btnPostulaciones = new JButton("Ver postulaciones"); btnPostulaciones.setBorder(null);
        btnCatalogo = new JButton("Catálogo de convenios");  btnCatalogo.setBorder(null);

        JPanel panelOpciones = new JPanel(new GridLayout(0, 1, 0, 12));
        panelOpciones.setBorder(BorderFactory.createEmptyBorder(50, 16, 12, 16));
        panelOpciones.add(btnPerfil);
        panelOpciones.add(btnPostulaciones);
        panelOpciones.add(btnCatalogo);

        JPanel panelPerfilMini = new JPanel();
        panelPerfilMini.setBorder(BorderFactory.createEmptyBorder(200, 16, 100, 16));
        lblSidebarNombre = new JLabel("Usuario");
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

        add(panelIzquierdo, BorderLayout.WEST);

        // ====== Centro (cards) ======
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

        cards.add(perfilPanel,        CARD_PERFIL);
        cards.add(postulacionesPanel, CARD_POSTULACIONES);
        cards.add(postularPanel,      CARD_CATALOGO);

        add(cards, BorderLayout.CENTER);
        cardsLayout.show(cards, currentCard);

        // ====== Navegación ======
        btnPerfil.addActionListener(e -> {
            currentCard = CARD_PERFIL;
            cardsLayout.show(cards, CARD_PERFIL);
        });

        btnPostulaciones.addActionListener(e -> {
            postulacionesPanel.setUsuario(usuario);
            postulacionesPanel.setFiltroConvenio(null); // limpiar filtro si venimos de catálogo
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
        String rolLegible = (usuario.getRol() != null)
                ? toTitulo(usuario.getRol().name())
                : "Usuario";
        lblSidebarNombre.setText(rolLegible + " " + safe(usuario.getNombreCompleto()));

        Permisos p = Permisos.para(usuario.getRol());

        // Ajustar textos según rol
        if (usuario.getRol() == Rol.ESTUDIANTE) {
            btnPerfil.setText("Ver mi Perfil");
            btnPostulaciones.setText("Ver mis Postulaciones");
            btnCatalogo.setText("Postular a un Convenio");
        } else if (usuario.getRol() == Rol.FUNCIONARIO) {
            btnPerfil.setText("Ver mi Perfil");
            btnPostulaciones.setText("Revisar Postulaciones");
            btnCatalogo.setText("Catálogo de convenios");
        } else {
            // Auditor u otro rol
            btnPerfil.setText("Perfil");
            btnPostulaciones.setText("Ver Postulaciones");
            btnCatalogo.setText("Catálogo de convenios");
        }

        // Visibilidad
        btnCatalogo.setVisible(p.verCatalogo || p.postular);
        btnPostulaciones.setVisible(p.verPostPropias || p.verPostTodas);

        // Propaga contexto
        postulacionesPanel.setUsuario(usuario);
        if (postularPanel != null) postularPanel.setUsuario(usuario);

        enforceAccessibleCard(p);

        revalidate();
        repaint();
    }

    private void enforceAccessibleCard(Permisos p) {
        boolean perfilOk = true; // siempre
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

        if (perfilOk) {
            currentCard = CARD_PERFIL;
        } else if (postulacionesOk) {
            currentCard = CARD_POSTULACIONES;
        } else if (catalogoOk) {
            currentCard = CARD_CATALOGO;
        } else {
            currentCard = CARD_PERFIL;
        }
        cardsLayout.show(cards, currentCard);
    }

    // ===== util =====
    private static String safe(String s) { return (s == null || s.trim().isEmpty()) ? "-" : s.trim(); }
    private static String toTitulo(String enumName) {
        String lower = enumName.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
