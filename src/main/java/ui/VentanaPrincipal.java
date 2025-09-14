package ui;

import gestores.GestorIntercambio;
import modelo.Usuario;
import ui.autorizacion.LoginPanel;

import javax.swing.*;
import java.awt.*;

public class VentanaPrincipal extends JFrame {
    private static final String VIEW_LOGIN = "login";
    private static final String VIEW_HOME  = "home";

    private final JPanel cards = new JPanel(new CardLayout());
    private final GestorIntercambio gestor;

    public VentanaPrincipal(GestorIntercambio gestor){
        this.gestor = gestor;
        init();
    }
    private static String rolDe(modelo.Usuario u) {
        if (u instanceof modelo.Estudiante) return "Estudiante";
        if (u instanceof modelo.Funcionario) return "Funcionario";
        if (u instanceof modelo.Auditor)     return "Auditor";
        return "Desconocido";
    }

    private void init(){
        setTitle("Gestión Intercambios Estudiantiles");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280,720);
        setLocationRelativeTo(null);

        LoginPanel login = new LoginPanel(gestor, this::onLoginOk);

        // Vista “home” temporal
        JPanel home = new JPanel(new BorderLayout());
        home.add(new JLabel("Inicio (placeholder)", SwingConstants.CENTER), BorderLayout.CENTER);

        cards.add(login, VIEW_LOGIN);
        cards.add(home, VIEW_HOME);
        setContentPane(cards);

        show(VIEW_LOGIN);
    }

    private void onLoginOk(Usuario usuario) {
        JOptionPane.showMessageDialog(
                this,
                "Bienvenido, " + usuario.getNombreCompleto() + "\nRol: " + rolDe(usuario),
                "Sesión iniciada",
                JOptionPane.INFORMATION_MESSAGE
        );
        show(VIEW_HOME);
    }

    private void show(String name) {
        ((CardLayout) cards.getLayout()).show(cards, name);
        revalidate(); repaint();
    }
}
