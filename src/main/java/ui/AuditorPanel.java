package ui;

import com.formdev.flatlaf.FlatClientProperties;
import enums.Rol;
import gestores.GestorIntercambio;
import modelo.Usuario;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

/**
 * **Panel de Interfaz de Usuario (Dashboard) para el Rol de Auditor.**
 * <p>Actúa como la ventana principal que centraliza la navegación y las funcionalidades
 * disponibles para el rol {@link Rol#AUDITOR}. Utiliza un diseño de
 * {@link CardLayout} para cambiar las vistas del contenido principal (centro).</p>
 * <p>Incluye una barra lateral (`JPanel` en {@link BorderLayout#WEST}) con los botones
 * de navegación (Perfil, Gestión de Programas, etc.).</p>
 */
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

    /**
     * Crea e inicializa el panel principal del Auditor.
     * @param gestor El gestor central de la aplicación.
     * @param auditor El usuario Auditor autenticado.
     * @param onLogout El {@code Runnable} que maneja la transición a la pantalla de login.
     */
    public AuditorPanel(GestorIntercambio gestor, Usuario auditor, Runnable onLogout) {
        this.gestor = gestor;
        this.auditor = auditor;
        this.onLogout = onLogout;
        init();
    }

    // ---------- helpers de UI (mismo look&feel que Estudiante/Funcionario) ----------

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

    /** Selección: fondo azul + franja a la derecha */
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
                        BorderFactory.createEmptyBorder(20, 28, 20, 22)
                ));
            } else {
                b.setContentAreaFilled(false);
                b.setForeground(unselectedFg);
                b.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));
            }
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

    // ---------- init ----------

    private void init() {
        // Sidebar
        JPanel panelIzquierdo = new JPanel(new BorderLayout());
        panelIzquierdo.setBackground(new Color(0x262626));
        panelIzquierdo.setOpaque(true);
        panelIzquierdo.setPreferredSize(new Dimension(360, 0));

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

        JPanel separator = new JPanel();
        separator.setPreferredSize(new Dimension(0, 1));
        separator.setBackground(new Color(0x3A3A3A));
        topPanel.add(separator, "growx, gaptop 12");

        // Navegación (5 opciones con separadores)
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

        navButtonsPanel.add(Box.createVerticalGlue());

        // Grupo 1
        navButtonsPanel.add(btnPerfil);
        navButtonsPanel.add(Box.createVerticalStrut(12));
        navButtonsPanel.add(btnGestionUsuarios);

        // Separador visual entre grupos
        navButtonsPanel.add(Box.createVerticalStrut(18));
        JSeparator sep1 = new JSeparator();
        sep1.setForeground(new Color(0x3A3A3A));
        navButtonsPanel.add(sep1);
        navButtonsPanel.add(Box.createVerticalStrut(18));

        // Grupo 2
        navButtonsPanel.add(btnGestionProgramas);
        navButtonsPanel.add(Box.createVerticalStrut(12));
        navButtonsPanel.add(btnGestionConvenios);

        // Separador hacia Análisis
        navButtonsPanel.add(Box.createVerticalStrut(18));
        JSeparator sep2 = new JSeparator();
        sep2.setForeground(new Color(0x3A3A3A));
        navButtonsPanel.add(sep2);
        navButtonsPanel.add(Box.createVerticalStrut(18));

        // Grupo 3
        navButtonsPanel.add(btnAnalisis);

        navButtonsPanel.add(Box.createVerticalGlue());

        panelIzquierdo.add(topPanel, BorderLayout.NORTH);
        panelIzquierdo.add(navButtonsPanel, BorderLayout.CENTER);

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
        centerCardsLayout.show(centerCards, CARD_PERFIL);

        // Layout principal
        setLayout(new BorderLayout());
        add(panelIzquierdo, BorderLayout.WEST);
        add(centerCards, BorderLayout.CENTER);

        // Listeners (misma lógica, sólo añadí setSelected para el estado visual)
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

    // El método para crear usuarios se mantiene igual
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
