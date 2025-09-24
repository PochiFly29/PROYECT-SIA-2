package ui;

import gestores.GestorIntercambio;
import modelo.Estudiante;
import modelo.Usuario;

import javax.swing.*;
import java.awt.*;

public class VentanaPrincipal extends JFrame {
    private static final String VIEW_LOGIN = "login";
    private static final String VIEW_HOME = "home";
    private static final String VIEW_ESTUDIANTE = "estu";
    private static final String VIEW_FUNCIONARIO = "func";
    private static final String VIEW_AUDITOR = "aud";

    private final JPanel cards = new JPanel(new CardLayout());
    private final GestorIntercambio gestor;

    private EstudiantePanel estudiantePanel;
    private JPanel funcionarioPanel;
    private JPanel auditorPanel;

    public VentanaPrincipal(GestorIntercambio gestor){
        this.gestor = gestor;
        init();
    }

    private void init(){
        setTitle("Gestión Intercambios Estudiantiles");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280,720);
        setLocationRelativeTo(null);

        LoginPanel login = new LoginPanel(gestor, this::onLoginOk);

        cards.add(login, VIEW_LOGIN);
        setContentPane(cards);

        show(VIEW_LOGIN);
    }

    private void onLoginOk(Usuario usuario) {
        redirigirUsuario(usuario);
    }

    private void redirigirUsuario(Usuario u) {
        if (u instanceof Estudiante) {
            if (estudiantePanel == null) {
                estudiantePanel = new EstudiantePanel(gestor, u, this::logout);
                cards.add(estudiantePanel, VIEW_ESTUDIANTE);
            } else {
                estudiantePanel.setUsuario(u);
            }
            show(VIEW_ESTUDIANTE);
        } else if (u.getRol().equals("Funcionario")) {
            if (funcionarioPanel == null) {
                funcionarioPanel = placeholder("Panel Funcionario (proximamente)");
                cards.add(funcionarioPanel, VIEW_FUNCIONARIO);
            }
            show(VIEW_FUNCIONARIO);
        } else if (u.getRol().equals("Auditor")) {
            if (auditorPanel == null) {
                auditorPanel = placeholder("Panel Auditor (proximamente)");
                cards.add(auditorPanel, VIEW_AUDITOR);
            }
            show(VIEW_AUDITOR);
        } else {
            show(VIEW_HOME);
        }
    }

    private void logout() {
        gestor.cerrarSesion();
        show(VIEW_LOGIN);
    }

    private void show(String name) {
        ((CardLayout) cards.getLayout()).show(cards, name);
        revalidate(); repaint();
    }

    private JPanel placeholder(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel(text, SwingConstants.CENTER), BorderLayout.CENTER);
        return p;
    }
}