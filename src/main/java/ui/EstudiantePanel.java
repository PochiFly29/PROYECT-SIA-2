package ui;

import com.formdev.flatlaf.FlatClientProperties;
import gestores.GestorIntercambio;
import modelo.Auditor;
import modelo.Estudiante;
import modelo.Funcionario;
import modelo.Usuario;

import javax.swing.*;
import java.awt.*;

public class EstudiantePanel extends JPanel {

    private final GestorIntercambio gestor;
    private Usuario usuario;
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

    public EstudiantePanel(GestorIntercambio gestor, Usuario usuario, Runnable onLogout) {
        this.gestor = gestor;
        this.usuario = usuario;
        this.onLogout = onLogout;
        init();
        refreshSidebar();
    }

    public void setUsuario(Usuario u) {
        this.usuario = u;
        refreshSidebar();
        if (perfilPanel != null) perfilPanel.setUsuario(u);
    }

    private void init() {
        // ===== Barra izquierda =====
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

        JButton btnPerfil = new JButton("Perfil");
        btnPerfil.setBorder(null);
        JButton btnVerPost = new JButton("Ver Postulaciones");
        btnVerPost.setBorder(null);
        JButton btnPostular = new JButton("Postular a un convenio");
        btnPostular.setBorder(null);

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

        // ===== Centro (cards) =====
        perfilPanel = new PerfilPanel(usuario);
        postulacionesPanel = new PostulacionesPanel(gestor, usuario);
        postularPanel = new PostularPanel(gestor, usuario);

        centerCards.add(perfilPanel, CARD_PERFIL);
        centerCards.add(postulacionesPanel, CARD_POSTULACIONES);
        centerCards.add(postularPanel, CARD_POSTULAR);
        centerCardsLayout.show(centerCards, CARD_PERFIL);

        // ===== Layout exterior =====
        setLayout(new BorderLayout());
        add(panelIzquierdo, BorderLayout.WEST);
        add(centerCards, BorderLayout.CENTER);

        // Nav
        btnPerfil.addActionListener(e -> centerCardsLayout.show(centerCards, CARD_PERFIL));
        btnVerPost.addActionListener(e -> {
            postulacionesPanel.setUsuario(usuario);
            postulacionesPanel.refresh();
            centerCardsLayout.show(centerCards, CARD_POSTULACIONES);
        });
        btnPostular.addActionListener(e -> centerCardsLayout.show(centerCards, CARD_POSTULAR));
    }

    private void refreshSidebar() {
        if (usuario == null) return;
        String rolLegible = (usuario.getRol() != null)
                ? toTitulo(usuario.getRol().name())
                : "Usuario";
        String nombre = safe(usuario.getNombreCompleto());
        lblSidebarNombre.setText(rolLegible + " " + nombre);
    }

    private static String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
    }

    private static String toTitulo(String enumName) {
        String lower = enumName.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
