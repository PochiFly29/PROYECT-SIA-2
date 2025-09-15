package ui;

import gestores.GestorIntercambio;
import modelo.Usuario;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class EstudiantePanel extends JPanel {
    private JPanel contentPane;
    private JPanel headerPanel;
    private JPanel navPanel;
    private JPanel contentPanel;
    private JLabel lblUsuario;
    private JButton btnPerfil;
    private JButton btnConvenios;
    private JButton btnPostulaciones;
    private JButton btnCerrarSesion;

    private final GestorIntercambio gestor;
    private Usuario usuario;
    private final Runnable onLogout;

    // Vistas internas
    private static final String VIEW_PERFIL = "perfil";
    private static final String VIEW_CONVENIOS = "convenios";
    private static final String VIEW_POSTULACIONES = "postulaciones";

    public EstudiantePanel(GestorIntercambio gestor, Usuario usuario, Runnable onLogout) {
        this.gestor = Objects.requireNonNull(gestor);
        this.usuario = Objects.requireNonNull(usuario);
        this.onLogout = onLogout;

        setLayout(new BorderLayout());
        add(contentPane, BorderLayout.CENTER);

        contentPanel.setLayout(new CardLayout());

        // Vistas placeholder
        contentPanel.add(crearPerfilView(), VIEW_PERFIL);
        contentPanel.add(crearConveniosView(), VIEW_CONVENIOS);
        contentPanel.add(crearPostulacionesView(), VIEW_POSTULACIONES);

        // Header
        actualizarHeader();

        // Navegación
        btnPerfil.addActionListener(e -> show(VIEW_PERFIL));
        btnConvenios.addActionListener(e -> show(VIEW_CONVENIOS));
        btnPostulaciones.addActionListener(e -> show(VIEW_POSTULACIONES));

        btnCerrarSesion.addActionListener(e -> {
            if (onLogout != null) onLogout.run();
        });

        // Pantalla por defecto
        show(VIEW_PERFIL);
    }

    public void setUsuario(Usuario u) {
        this.usuario = Objects.requireNonNull(u);
        actualizarHeader();
    }

    private void actualizarHeader() {
        lblUsuario.setText(usuario.getNombreCompleto() + " — " + rolDe(usuario));
    }

    private void show(String name) {
        ((CardLayout) contentPanel.getLayout()).show(contentPanel, name);
        revalidate();
        repaint();
    }

    private static String rolDe(Usuario u) {
        String cn = u.getClass().getSimpleName();  // Estudiante / Funcionario / Auditor
        return cn;
    }

    // --- Vistas simples (placeholder) ---
    private JComponent crearPerfilView() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel("Perfil del estudiante (próximamente)", SwingConstants.CENTER), BorderLayout.CENTER);
        return p;
    }

    private JComponent crearConveniosView() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel("Convenios vigentes (próximamente)", SwingConstants.CENTER), BorderLayout.CENTER);
        return p;
    }

    private JComponent crearPostulacionesView() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel("Mis postulaciones (próximamente)", SwingConstants.CENTER), BorderLayout.CENTER);
        return p;
    }
}
