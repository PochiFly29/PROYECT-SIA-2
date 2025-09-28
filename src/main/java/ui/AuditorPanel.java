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
    private GestionConveniosAuditorPanel gestionConveniosPanel;

    private final CardLayout centerCardsLayout = new CardLayout();
    private final JPanel centerCards = new JPanel(centerCardsLayout);

    // Nombres de las tarjetas
    private static final String CARD_PERFIL = "perfil";
    private static final String CARD_GESTION_PROGRAMAS = "gestionProgramas";
    private static final String CARD_GESTION_CONVENIOS = "gestionConvenios";
    private static final String CARD_ANALISIS = "analisis";

    // Paneles de contenido
    private PerfilPanel perfilPanel;
    private GestionProgramasPanel gestionProgramasPanel;
    // ... aquí irían los otros paneles cuando los implementes

    public AuditorPanel(GestorIntercambio gestor, Usuario auditor, Runnable onLogout) {
        this.gestor = gestor;
        this.auditor = auditor;
        this.onLogout = onLogout;
        init();
    }

    private void init() {
        // --- Sidebar (barra lateral izquierda) ---
        JPanel panelIzquierdo = new JPanel(new BorderLayout());
        panelIzquierdo.putClientProperty(FlatClientProperties.STYLE, "background:lighten(@background,3%)");
        panelIzquierdo.setPreferredSize(new Dimension(280, 0));

        // Panel superior para el logo y título
        JPanel topPanel = new JPanel(new MigLayout("wrap, fillx, insets 16 24 16 24", "fill"));
        topPanel.setOpaque(false);
        try {
            ImageIcon logoIcon = new ImageIcon(getClass().getResource("/logo.png"));
            Image scaledImage = logoIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            JLabel lblLogo = new JLabel(new ImageIcon(scaledImage));
            lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
            topPanel.add(lblLogo, "growx, center, wrap, gaptop 8");
        } catch (Exception e) {
            System.err.println("No se pudo cargar el logo.");
        }
        JLabel lblTitulo = new JLabel("Panel de Auditoría");
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitulo.putClientProperty(FlatClientProperties.STYLE, "font:bold +1");
        topPanel.add(lblTitulo, "growx, center");
        JPanel separator = new JPanel();
        separator.setPreferredSize(new Dimension(0, 1));
        separator.setBackground(UIManager.getColor("Component.borderColor"));
        topPanel.add(separator, "growx, gaptop 8");

        // --- Botones de Navegación del Auditor ---
        JPanel navButtonsPanel = new JPanel(new GridLayout(0, 1, 0, 12));
        navButtonsPanel.setOpaque(false);
        navButtonsPanel.setBorder(BorderFactory.createEmptyBorder(50, 16, 12, 16));

        JButton btnPerfil = new JButton("Mi Perfil");
        JButton btnGestionUsuarios = new JButton("Gestionar Usuarios");
        JButton btnGestionProgramas = new JButton("Gestionar Programas");
        JButton btnGestionConvenios = new JButton("Gestionar Convenios");
        JButton btnAnalisis = new JButton("Análisis");

        String buttonStyle = "background:#2E86FF; foreground:#FFFFFF; font:bold +1; borderWidth:0; focusWidth:0; innerFocusWidth:0";
        btnPerfil.putClientProperty(FlatClientProperties.STYLE, buttonStyle);
        btnGestionUsuarios.putClientProperty(FlatClientProperties.STYLE, buttonStyle);
        btnGestionProgramas.putClientProperty(FlatClientProperties.STYLE, buttonStyle);
        btnGestionConvenios.putClientProperty(FlatClientProperties.STYLE, buttonStyle);
        btnAnalisis.putClientProperty(FlatClientProperties.STYLE, buttonStyle);

        Dimension buttonSize = new Dimension(180, 40);
        btnPerfil.setPreferredSize(buttonSize);
        btnGestionUsuarios.setPreferredSize(buttonSize);
        btnGestionProgramas.setPreferredSize(buttonSize);
        btnGestionConvenios.setPreferredSize(buttonSize);
        btnAnalisis.setPreferredSize(buttonSize);

        navButtonsPanel.add(btnPerfil);
        navButtonsPanel.add(btnGestionUsuarios);
        navButtonsPanel.add(new JSeparator());
        navButtonsPanel.add(btnGestionProgramas);
        navButtonsPanel.add(btnGestionConvenios);
        navButtonsPanel.add(new JSeparator());
        navButtonsPanel.add(btnAnalisis);

        // --- Panel inferior (info de usuario y logout) ---
        JPanel bottomPanel = new JPanel(new MigLayout("wrap, fillx, insets 16 24 16 24", "fill"));
        bottomPanel.setOpaque(false);
        JLabel lblSidebarNombre = new JLabel("Hola, Auditor " + auditor.getNombreCompleto().split(" ")[0] + "!");
        lblSidebarNombre.setHorizontalAlignment(SwingConstants.CENTER);
        lblSidebarNombre.putClientProperty(FlatClientProperties.STYLE, "font:bold; foreground:lighten(@foreground,20%)");
        JButton btnCerrar = new JButton("Cerrar Sesión");
        btnCerrar.putClientProperty(FlatClientProperties.STYLE, "background:#E42828; foreground:#FFFFFF; arc:999");
        bottomPanel.add(lblSidebarNombre, "growx, gaptop 16");
        bottomPanel.add(btnCerrar, "growx, height 40, gaptop 16");

        panelIzquierdo.add(topPanel, BorderLayout.NORTH);
        panelIzquierdo.add(navButtonsPanel, BorderLayout.CENTER);
        panelIzquierdo.add(bottomPanel, BorderLayout.SOUTH);

        // --- Centro (Paneles de contenido) ---
        perfilPanel = new PerfilPanel(gestor, auditor);
        gestionProgramasPanel = new GestionProgramasPanel(gestor);
        // CAMBIO: Se reemplaza el panel placeholder por la nueva clase real.
        gestionConveniosPanel = new GestionConveniosAuditorPanel(gestor, auditor);
        JPanel analisisPanel = new JPanel();
        analisisPanel.add(new JLabel("Módulo de Análisis y Reportes (en desarrollo)"));

        centerCards.add(perfilPanel, CARD_PERFIL);
        centerCards.add(gestionProgramasPanel, CARD_GESTION_PROGRAMAS);
        // CAMBIO: Se añade la nueva instancia al CardLayout.
        centerCards.add(gestionConveniosPanel, CARD_GESTION_CONVENIOS);
        centerCards.add(analisisPanel, CARD_ANALISIS);

        // --- Layout Principal ---
        setLayout(new BorderLayout());
        add(panelIzquierdo, BorderLayout.WEST);
        add(centerCards, BorderLayout.CENTER);

        // --- Action Listeners CONECTADOS ---
        btnPerfil.addActionListener(e -> centerCardsLayout.show(centerCards, CARD_PERFIL));
        btnGestionUsuarios.addActionListener(e -> mostrarDialogoCrearUsuario());

        btnGestionProgramas.addActionListener(e -> {
            gestionProgramasPanel.refresh(); // Actualiza los datos antes de mostrar
            centerCardsLayout.show(centerCards, CARD_GESTION_PROGRAMAS);
        });

        // CAMBIO: Se conecta el botón para mostrar el panel de convenios.
        btnGestionConvenios.addActionListener(e -> {
            gestionConveniosPanel.refresh(); // Siempre actualiza los datos al mostrar
            centerCardsLayout.show(centerCards, CARD_GESTION_CONVENIOS);
        });

        btnAnalisis.addActionListener(e -> centerCardsLayout.show(centerCards, CARD_ANALISIS));
        btnCerrar.addActionListener(e -> onLogout.run());
    }

    // El método para crear usuarios que ya teníamos
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
}