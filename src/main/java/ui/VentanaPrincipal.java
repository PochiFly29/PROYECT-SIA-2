package ui;

import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import ui.autorizacion.LoginPanel;

import javax.swing.*;
import java.awt.*;

public class VentanaPrincipal extends JFrame {

    public VentanaPrincipal(){
        init();
    }

    private void init(){
        setTitle("Gestion Intercambios Estudiantiles");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280,720);
        setLocationRelativeTo(null);
        setContentPane(new LoginPanel());
    }

    public static void main(String[] args) {
        FlatRobotoFont.install();
        UIManager.put("DefaultFont", new Font(FlatRobotoFont.FAMILY,Font.PLAIN,13));
        FlatMacDarkLaf.setup();
        EventQueue.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}
