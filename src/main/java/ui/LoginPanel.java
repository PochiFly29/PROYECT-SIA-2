package ui;

import com.formdev.flatlaf.FlatClientProperties;
import gestores.GestorIntercambio;
import gestores.ResultadoLogin;
import modelo.Usuario;
import net.miginfocom.swing.MigLayout;
import servicios.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class LoginPanel extends JPanel {

    private final GestorIntercambio gestor;
    private final Consumer<Usuario> onSuccess;
    private final Runnable onRegisterRequest;

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
        setLayout(new BorderLayout());

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        topBar.setOpaque(false);

        JToggleButton btnTheme = new JToggleButton("🌙", false);
        btnTheme.setFocusable(false);
        btnTheme.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnTheme.setMargin(new Insets(6, 10, 6, 10));
        btnTheme.setPreferredSize(new Dimension(44, 44));
        btnTheme.setFont(btnTheme.getFont().deriveFont(20f));
        btnTheme.putClientProperty(FlatClientProperties.STYLE, "arc:999; background:lighten(@background,6%); borderWidth:0; focusWidth:0; innerFocusWidth:0");

        btnTheme.addActionListener(e -> { boolean dark = btnTheme.isSelected(); ThemeManager.applySession(dark); });

        topBar.add(btnTheme);
        add(topBar, BorderLayout.NORTH);

        JPanel centerWrap = new JPanel(new MigLayout("fill,insets 20", "[center]", "[center]"));
        centerWrap.setOpaque(false);

        rut = new JTextField();
        pass = new JPasswordField();
        login = new JButton("Ingresar");

        JPanel card = new JPanel(new MigLayout("wrap,fillx,insets 35 45 30 45", "fill,250::280"));
        card.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:lighten(@background,3%)");
        pass.putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true");
        login.putClientProperty(FlatClientProperties.STYLE,"background:lighten(@background,10%); borderWidth:0; focusWidth:0; innerFocusWidth:0");

        rut.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "(ej: 11111111K)");
        pass.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "(al menos 3 caracteres)");

        JLabel titulo = new JLabel("¡Bienvenido!");
        JLabel descripcion =  new JLabel("Inicie sesión para ingresar a su cuenta");
        titulo.putClientProperty(FlatClientProperties.STYLE, "font:bold +10");
        descripcion.putClientProperty(FlatClientProperties.STYLE, "foreground:darken(@foreground,30%)");

        card.add(titulo);
        card.add(descripcion);
        card.add(new JLabel("RUT"), "gapy 8");
        card.add(rut);
        card.add(new JLabel("Contraseña"), "gapy 8");
        card.add(pass);
        card.add(login, "gapy 10");
        card.add(crearSeccionRegistro(), "gapy 10");

        centerWrap.add(card);
        add(centerWrap, BorderLayout.CENTER);

        login.addActionListener(e -> doLogin());

        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke("ENTER"), "enterLogin");
        getActionMap().put("enterLogin", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { doLogin(); }
        });
    }

    private Component crearSeccionRegistro(){
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
        panel.putClientProperty(FlatClientProperties.STYLE, "background:null");
        JButton registrar = new JButton("<html><a href=\"#\">Registrar</a></html>");
        registrar.putClientProperty(FlatClientProperties.STYLE, "border:3,3,3,3");
        registrar.setContentAreaFilled(false);
        registrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registrar.addActionListener(e -> onRegisterRequest.run());
        JLabel label = new JLabel("¿No tienes una cuenta? ");
        label.putClientProperty(FlatClientProperties.STYLE, "foreground:darken(@foreground,30%)");
        panel.add(label);
        panel.add(registrar);
        return panel;
    }

    private void doLogin() {
        String rutTxt  = rut.getText().trim().toUpperCase();
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
