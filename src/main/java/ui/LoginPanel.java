package ui;

import com.formdev.flatlaf.FlatClientProperties;
import gestores.GestorIntercambio;
import gestores.ResultadoLogin;
import modelo.Usuario;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class LoginPanel extends JPanel {

    private final GestorIntercambio gestor;
    private final Consumer<Usuario> onSuccess;
    private final Runnable onRegisterRequest; // abre RegistroPanel (card "registro")

    private JTextField rut;
    private JPasswordField pass;
    private JButton login;

    public LoginPanel(GestorIntercambio gestor, Consumer<Usuario> onSuccess, Runnable onRegisterRequest) {
        this.gestor = gestor;
        this.onSuccess = onSuccess;
        this.onRegisterRequest = onRegisterRequest;
        init();
    }

    private void init(){
        setLayout(new MigLayout("fill,insets 20", "[center]", "[center]"));

        rut = new JTextField();
        pass = new JPasswordField();
        login = new JButton("Ingresar");

        JPanel panel = new JPanel(new MigLayout("wrap,fillx,insets 35 45 30 45", "fill,250::280"));
        panel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:lighten(@background,3%)");

        pass.putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true");
        login.putClientProperty(FlatClientProperties.STYLE,
                "background:lighten(@background,10%); borderWidth:0; focusWidth:0; innerFocusWidth:0");

        rut.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ingrese su RUT");
        pass.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ingrese su contraseña");

        JLabel titulo = new JLabel("¡Bienvenido!");
        JLabel descripcion =  new JLabel("Inicie sesión para ingresar a su cuenta");
        titulo.putClientProperty(FlatClientProperties.STYLE, "font:bold +10");
        descripcion.putClientProperty(FlatClientProperties.STYLE, "foreground:darken(@foreground,30%)");

        panel.add(titulo);
        panel.add(descripcion);
        panel.add(new JLabel("RUT"), "gapy 8");
        panel.add(rut);
        panel.add(new JLabel("Contraseña"), "gapy 8");
        panel.add(pass);
        panel.add(login, "gapy 10");
        panel.add(crearSeccionRegistro(), "gapy 10");
        add(panel);

        // Acciones
        login.addActionListener(e -> doLogin());

        // ENTER para login
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke("ENTER"), "enterLogin");
        getActionMap().put("enterLogin", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { doLogin(); }
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        JRootPane rp = SwingUtilities.getRootPane(this);
        if (rp != null) rp.setDefaultButton(login);
    }

    private Component crearSeccionRegistro(){
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
        panel.putClientProperty(FlatClientProperties.STYLE, "background:null");

        JButton registrar = new JButton("<html><a href=\"#\">Registrar</a></html>");
        registrar.putClientProperty(FlatClientProperties.STYLE, "border:3,3,3,3");
        registrar.setContentAreaFilled(false);
        registrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registrar.addActionListener(e -> {
            if (onRegisterRequest != null) onRegisterRequest.run();
            else Toolkit.getDefaultToolkit().beep();
        });

        JLabel label = new JLabel("¿No tienes una cuenta? ");
        label.putClientProperty(FlatClientProperties.STYLE, "foreground:darken(@foreground,30%)");

        panel.add(label);
        panel.add(registrar);
        return panel;
    }

    private void doLogin() {
        if (gestor == null) {
            JOptionPane.showMessageDialog(this, "Gestor no inicializado.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String rutTxt  = rut.getText().trim();               // sin normalizar
        String passTxt = new String(pass.getPassword());

        ResultadoLogin r = gestor.iniciarSesion(rutTxt, passTxt);
        if (r.isExito()) {
            onSuccess.accept(r.getUsuario());
        } else {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, r.getMensaje(), "Inicio de sesión", JOptionPane.ERROR_MESSAGE);
            pass.setText("");
            pass.requestFocusInWindow();
        }
    }
}