package ui.autorizacion;

import com.formdev.flatlaf.FlatClientProperties;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {

    public LoginPanel() {
        init();
    }

    private void init(){
        setLayout(new MigLayout("fill,insets 20", "[center]","[center]"));
        rut = new JTextField();
        contraseña = new JPasswordField();
        login = new JButton("Ingresar");

        JPanel panel = new JPanel(new MigLayout("wrap,fillx,insets 35 45 30 45", "fill,250::280"));
        panel.putClientProperty(FlatClientProperties.STYLE, "" + "arc:20;" + "background:lighten(@background,3%)");

        contraseña.putClientProperty(FlatClientProperties.STYLE, "" + "showRevealButton:true"); // Boton para revelar contraseña
        login.putClientProperty(FlatClientProperties.STYLE, "" + "background:lighten(@background,10%);" + "borderWidth:0;" + "focusWidth:0;" + "innerFocusWidth:0"); // Hoover, al pasar y apretar parpadea el boton de ingreso

        rut.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ingrese su rut"); // Texto previo a escribir usuario
        contraseña.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ingrese su contraseña"); // Texto previo a escribir contraseña

        JLabel titulo = new JLabel("Bienvenido!");
        JLabel descripcion =  new JLabel("Inicie sesion para ingresar a su cuenta");
        titulo.putClientProperty(FlatClientProperties.STYLE, "" + "font:bold +10");
        descripcion.putClientProperty(FlatClientProperties.STYLE, "" + "foreground:darken(@foreground,30%)");

        panel.add(titulo);
        panel.add(descripcion);
        panel.add(new JLabel("Rut"), "gapy 8");
        panel.add(rut);
        panel.add(new JLabel("Contraseña"), "gapy 8");
        panel.add(contraseña);
        panel.add(login, "gapy 10");
        panel.add(crearSeccionRegistro(), "gapy 10");
        add(panel);
    }

    private Component crearSeccionRegistro(){
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
        panel.putClientProperty(FlatClientProperties.STYLE, "" + "background:null");
        JButton registrar = new JButton("<html><a href=\"#\">Registrar</a></html>");
        registrar.putClientProperty(FlatClientProperties.STYLE, "" + "border:3,3,3,3");
        registrar.setContentAreaFilled(false);
        registrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registrar.addActionListener(e -> {
            System.out.println("Ir a registro");
            // proximamente
        });
        JLabel label = new JLabel("No tienes una cuenta?");
        label.putClientProperty(FlatClientProperties.STYLE, "" + "foreground:darken(@foreground,30%)");
        panel.add(label);
        panel.add(registrar);
        return panel;
    }

    private JTextField rut;
    private JPasswordField contraseña;
    private JButton login;
}
