package ui;

import com.formdev.flatlaf.FlatClientProperties;
import gestores.GestorIntercambio;
import modelo.ResultadoLogin;
import modelo.Usuario;
import servicios.ManagerTemas;

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

        rut = new JTextField();
        pass = new JPasswordField();
        login = new JButton("Ingresar");

        JPanel formPanel = new JPanel(new GridBagLayout());

        // Estilo del Recuadro Central (FlatLaf)
        formPanel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:lighten(@background,3%)");

        // Estilos para los componentes internos
        pass.putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true");
        login.putClientProperty(FlatClientProperties.STYLE, "background:lighten(@background,10%); borderWidth:0; focusWidth:0; innerFocusWidth:0");
        rut.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "(ej: 11111111K)");
        pass.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "(al menos 3 caracteres)");

        JLabel titulo = new JLabel("¡Bienvenido!");
        JLabel descripcion = new JLabel("Inicie sesión para ingresar a su cuenta");

        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        descripcion.setHorizontalAlignment(SwingConstants.CENTER);

        titulo.putClientProperty(FlatClientProperties.STYLE, "font:bold +10");
        descripcion.putClientProperty(FlatClientProperties.STYLE, "foreground:darken(@foreground,30%)");

        Dimension fieldPrefSize = new Dimension(300, 32);
        rut.setPreferredSize(fieldPrefSize);
        pass.setPreferredSize(fieldPrefSize);
        login.setPreferredSize(fieldPrefSize);

        // Aseguramos que los labels de título/descripción ocupen el mismo ancho para centrarse bien.
        titulo.setPreferredSize(fieldPrefSize);
        descripcion.setPreferredSize(fieldPrefSize);

        // --- GridBagLayout para la disposición interna del formulario ---
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 20, 4, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        // 1) Título + descripción
        gbc.insets = new Insets(20, 20, 5, 20);
        formPanel.add(titulo, gbc);
        gbc.insets = new Insets(0, 20, 30, 20);
        formPanel.add(descripcion, gbc);

        // 2) RUT
        gbc.insets = new Insets(0, 20, 3, 20);
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("RUT"), gbc);
        gbc.insets = new Insets(0, 20, 10, 20);
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(rut, gbc);

        // 3) Contraseña
        gbc.insets = new Insets(5, 20, 3, 20);
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Contraseña"), gbc);
        gbc.insets = new Insets(0, 20, 20, 20);
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(pass, gbc);

        // 4) Ingresar
        gbc.insets = new Insets(0, 20, 15, 20);
        formPanel.add(login, gbc);

        // 5) Registro
        gbc.insets = new Insets(0, 20, 20, 20);
        formPanel.add(crearSeccionRegistro(), gbc);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        GridBagConstraints cwc = new GridBagConstraints();
        cwc.gridx = 0; cwc.gridy = 0;
        cwc.weightx = 1.0; cwc.weighty = 1.0;
        cwc.anchor = GridBagConstraints.CENTER;
        centerWrapper.add(formPanel, cwc);

        JLayeredPane layered = ManagerTemas.wrapWithFloatingThemeButton(centerWrapper, false);
        add(layered, BorderLayout.CENTER);

        login.addActionListener(e -> doLogin());
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ENTER"), "enterLogin");
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
        String rutTxt  = rut.getText().trim().replace(".", "").replace("-", "").toUpperCase();
        String passTxt = new String(pass.getPassword());

        ResultadoLogin r = gestor.getServicioAutenticacion().iniciarSesion(rutTxt, passTxt);
        if (r.isExito()) {
            onSuccess.accept(r.getUsuario());
        } else {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, r.getMensaje(), "Inicio de sesión", JOptionPane.ERROR_MESSAGE);
            pass.setText("");
            pass.requestFocusInWindow();
        }
    }

    public void limpiarCampos() {
        rut.setText("");
        pass.setText("");
        rut.requestFocusInWindow();
    }
}
