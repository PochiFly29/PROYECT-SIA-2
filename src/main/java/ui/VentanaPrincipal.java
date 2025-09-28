package ui;

import enums.Rol;
import gestores.GestorIntercambio;
import modelo.Estudiante;
import modelo.Usuario;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

public class VentanaPrincipal extends JFrame {

    private final JPanel cards = new JPanel(new CardLayout());
    private final GestorIntercambio gestor;
    private final Map<String, JPanel> paneles = new HashMap<>();

    public VentanaPrincipal(GestorIntercambio gestor) {
        this.gestor = gestor;
        init();
        initWindowListener();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    private void init() {
        setTitle("Gestión Intercambios Estudiantiles");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setContentPane(cards);

        // Icono de la aplicación (logo)
        java.net.URL urlIcon = getClass().getResource("/Logo SGIE.png");
        if (urlIcon != null) {
            Image appImg = new ImageIcon(urlIcon).getImage();
            setIconImage(appImg);
            try {
                if (Taskbar.isTaskbarSupported()) {
                    Taskbar.getTaskbar().setIconImage(appImg);
                }
            } catch (Throwable ignore) {}
        } else {
            System.err.println("No se encontró el recurso Logo");
        }

        // Login / Registro
        RegistroPanel registroPanel = new RegistroPanel(gestor, () -> show("login"));
        LoginPanel loginPanel = new LoginPanel(gestor, this::onLoginOk, () -> show("registro"));

        paneles.put("login", loginPanel);
        paneles.put("registro", registroPanel);

        cards.add(loginPanel, "login");
        cards.add(registroPanel, "registro");

        show("login");
    }

    private void initWindowListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                gestor.getServicioAutenticacion().guardarCambiosDeUsuarios();
            }
        });
    }

    private void onLoginOk(Usuario usuario) {
        Rol rol = usuario.getRol();

        switch (rol) {
            case ESTUDIANTE: {
                String keyEstu = "estudiante" + usuario.getRut();
                if (!paneles.containsKey(keyEstu)) {
                    Estudiante estudiante = (Estudiante) usuario;
                    EstudiantePanel estudiantePanel = new EstudiantePanel(gestor, estudiante, this::logout);
                    paneles.put(keyEstu, estudiantePanel);
                    cards.add(estudiantePanel, keyEstu);
                }
                show(keyEstu);
                break;
            }
            case FUNCIONARIO: {
                String keyFunc = "funcionario" + usuario.getRut();
                if (!paneles.containsKey(keyFunc)) {
                    FuncionarioPanel funcionarioPanel = new FuncionarioPanel(gestor, usuario, this::logout);
                    paneles.put(keyFunc, funcionarioPanel);
                    cards.add(funcionarioPanel, keyFunc);
                }
                show(keyFunc);
                break;
            }
            case AUDITOR: {
                String keyAud = "auditor" + usuario.getRut();
                if (!paneles.containsKey(keyAud)) {
                    JPanel auditorPanel = new AuditorPanel(gestor, usuario, this::logout);
                    paneles.put(keyAud, auditorPanel);
                    cards.add(auditorPanel, keyAud);
                }
                show(keyAud);
                break;
            }
            default:
                JOptionPane.showMessageDialog(this, "Rol de usuario no reconocido.", "Error de inicio de sesión", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void logout() {
        LoginPanel loginPanel = (LoginPanel) paneles.get("login");
        if (loginPanel != null) {
            loginPanel.limpiarCampos();
        }
        show("login");
    }

    public void show(String name) {
        ((CardLayout) cards.getLayout()).show(cards, name);
        revalidate();
        repaint();
    }

    private JPanel placeholder(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel(text, SwingConstants.CENTER), BorderLayout.CENTER);
        return p;
    }
}
