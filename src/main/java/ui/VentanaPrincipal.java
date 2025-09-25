package ui;

import gestores.GestorIntercambio;
import modelo.Usuario;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/*
   TODO
 - No se refresca el panel de estudiante al entrar y salir, se queda donde antes aunque entres en otra cuenta
 - Pequeño panel con el nombre a la esquina izquierda inferior no se actualiza al cambiar el nombre
 - Quitar boton de gestionar Convenios de funcionario y hacer que se pueda gestionar en el otro boton
 - Cambiar de lugar boton de cerrar sesion con el panel de la izquierda inferior y agregar un boton de cerrar programa
 - Hacer un poco mas grande las letras en general
 */

public class VentanaPrincipal extends JFrame {
    private static final String VIEW_LOGIN = "login";
    private static final String VIEW_APP   = "app";

    private final JPanel cards = new JPanel(new CardLayout());
    private final GestorIntercambio gestor;

    private LoginPanel loginPanel;
    private UsuarioPanel appPanel;

    public VentanaPrincipal(GestorIntercambio gestor){
        this.gestor = Objects.requireNonNull(gestor);
        init();
    }

    private void init(){
        setTitle("Gestión Intercambios Estudiantiles");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280,720);
        setLocationRelativeTo(null);

        // Login
        loginPanel = new LoginPanel(gestor, this::onLoginOk);
        cards.add(loginPanel, VIEW_LOGIN);

        setContentPane(cards);
        showCard(VIEW_LOGIN);
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
