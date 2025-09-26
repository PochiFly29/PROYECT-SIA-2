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
        // **Agrega esta línea aquí para que la ventana sea visible**

        setVisible(true);
    }

    private void init() {
        setTitle("Gestión Intercambios Estudiantiles");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setContentPane(cards);

        // Agregamos el panel de Login y Registro
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
                gestor.guardarDatos();
            }
        });
    }

    private void onLoginOk(Usuario usuario) {
        switch (usuario.getRol()) {
            case ESTUDIANTE:
                String keyEstu = "estudiante" + usuario.getRut();
                if (!paneles.containsKey(keyEstu)) {
                    EstudiantePanel estudiantePanel = new EstudiantePanel(gestor, usuario, this::logout);
                    paneles.put(keyEstu, estudiantePanel);
                    cards.add(estudiantePanel, keyEstu);
                }
                show(keyEstu);
                break;
            case FUNCIONARIO:
                String keyFunc = "funcionario";
                if (!paneles.containsKey(keyFunc)) {
                    JPanel funcionarioPanel = placeholder("Panel Funcionario (próximamente)");
                    paneles.put(keyFunc, funcionarioPanel);
                    cards.add(funcionarioPanel, keyFunc);
                }
                show(keyFunc);
                break;
            case AUDITOR:
                String keyAud = "auditor";
                if (!paneles.containsKey(keyAud)) {
                    JPanel auditorPanel = placeholder("Panel Auditor (próximamente)");
                    paneles.put(keyAud, auditorPanel);
                    cards.add(auditorPanel, keyAud);
                }
                show(keyAud);
                break;
            default:
                JOptionPane.showMessageDialog(this, "Rol de usuario no reconocido.", "Error de inicio de sesión", JOptionPane.ERROR_MESSAGE);
                break;
        }
    }

    private void logout() {
        gestor.cerrarSesion();
        show("login");
    }

    private void show(String name) {
        ((CardLayout) cards.getLayout()).show(cards, name);
        revalidate();
        repaint();
    }

    // Panel de prueba para roles no implementados
    private JPanel placeholder(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel(text, SwingConstants.CENTER), BorderLayout.CENTER);
        return p;
    }
}