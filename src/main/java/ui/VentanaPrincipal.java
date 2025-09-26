package ui;

import gestores.GestorIntercambio;
import modelo.Usuario;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;

/*
   Unificacion de las dos versiones
   - LoginPanel moderno (con o sin callback de registro)
   - RegistroPanel (opcional): al finalizar vuelve a login
   - Un único UsuarioPanel que cambia por rol (estudiante/funcionario/auditor)
   - Persistencia: guardar datos al cerrar la ventana (DataStore)
   - Cambio de sesión: reutiliza UsuarioPanel y propaga setUsuario()
*/

public class VentanaPrincipal extends JFrame {
    private static final String VIEW_LOGIN = "login";
    private static final String VIEW_REGISTRO = "registro";
    private static final String VIEW_APP = "app";

    private final JPanel cards = new JPanel(new CardLayout());
    private final GestorIntercambio gestor;

    private LoginPanel loginPanel;
    private RegistroPanel registroPanel;
    private UsuarioPanel appPanel;

    public VentanaPrincipal(GestorIntercambio gestor){
        this.gestor = Objects.requireNonNull(gestor);
        init();
        initWindowListener();
        setVisible(true);
    }

    private void init(){
        setTitle("Gestión Intercambios Estudiantiles");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280,720);
        setLocationRelativeTo(null);
        setContentPane(cards);

        // Login
        loginPanel = new LoginPanel(gestor, this::onLoginOk, () -> showCard(VIEW_REGISTRO));
        cards.add(loginPanel, VIEW_LOGIN);

        // Registro
        registroPanel = new RegistroPanel(gestor, () -> showCard(VIEW_LOGIN));
        cards.add(registroPanel, VIEW_REGISTRO);

        showCard(VIEW_LOGIN);
    }

    private void initWindowListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    gestor.guardarDatos();
                } catch (Exception ex) {
                    // Evita crash al cerrar en caso de error de base de datos
                    System.err.println("Error al guardar datos al cerrar: " + ex.getMessage());
                }
            }
        });
    }

    private void onLoginOk(Usuario usuario) {
        if (appPanel == null) {
            appPanel = new UsuarioPanel(gestor, usuario, this::logout);
            cards.add(appPanel, VIEW_APP);
        } else {
            appPanel.setUsuario(usuario);
        }
        showCard(VIEW_APP);
    }

    private void logout() {
        gestor.cerrarSesion();
        showCard(VIEW_LOGIN);
    }

    private void showCard(String name) {
        ((CardLayout) cards.getLayout()).show(cards, name);
        revalidate();
        repaint();
    }
}
