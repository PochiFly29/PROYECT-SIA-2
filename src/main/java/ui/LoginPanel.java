package ui;

import com.formdev.flatlaf.FlatClientProperties;
import gestores.GestorIntercambio;
import modelo.ResultadoLogin;
import modelo.Usuario;
import servicios.ManagerTemas;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * **Panel de Interfaz de Usuario para el Inicio de Sesión.**
 * <p>Recopila las credenciales (RUT y Contraseña) del usuario, valida el acceso
 * a través del {@link gestores.ServicioAutenticacion} y, en caso de éxito,
 * notifica al contenedor principal ({@link VentanaPrincipal}) para iniciar el dashboard
 * correspondiente al {@link Usuario} autenticado.</p>
 */
public class LoginPanel extends JPanel {

    private final GestorIntercambio gestor;
    private final Consumer<Usuario> onSuccess;
    private final Runnable onRegisterRequest;

    private JTextField rut;
    private JPasswordField pass;
    private JButton login;

    /**
     * Crea e inicializa el panel de inicio de sesión.
     * @param gestor El gestor central de la aplicación.
     * @param onSuccess La acción a realizar con el usuario autenticado.
     * @param onRegisterRequest La acción a realizar para cambiar a la vista de registro.
     */
    public LoginPanel(GestorIntercambio gestor, Consumer<Usuario> onSuccess, Runnable onRegisterRequest) {
        this.gestor = gestor;
        this.onSuccess = onSuccess;
        this.onRegisterRequest = onRegisterRequest;
        init();
    }

    private void init(){
        // Contenedor principal para centrar el formulario en la ventana
        setLayout(new GridBagLayout());

        rut = new JTextField();
        pass = new JPasswordField();
        login = new JButton("Ingresar");

        // --- Panel del Formulario (Contiene todos los campos y el recuadro de estilo) ---
        JPanel formPanel = new JPanel(new GridBagLayout());

        // Estilo del Recuadro Central (FlatLaf)
        formPanel.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:lighten(@background,3%)");

        // Estilos para los componentes internos
        pass.putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true");
        // Evita claves FlatLaf conflictivas en algunas versiones
        login.putClientProperty(FlatClientProperties.STYLE, "background:lighten(@background,10%)");
        login.setFocusPainted(false);
        login.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        rut.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "(ej: 11111111K)");
        pass.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "(al menos 3 caracteres)");

        JLabel titulo = new JLabel("¡Bienvenido!");
        JLabel descripcion = new JLabel("Inicie sesión para ingresar a su cuenta");

        // CORRECCIÓN CLAVE: Aplicar el centrado de texto de Swing directamente
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        descripcion.setHorizontalAlignment(SwingConstants.CENTER);

        // Aplicar estilos FlatLaf restantes (SIN la propiedad horizontalAlignment:center)
        titulo.putClientProperty(FlatClientProperties.STYLE, "font:bold +10");
        descripcion.putClientProperty(FlatClientProperties.STYLE, "foreground:darken(@foreground,30%)");

        // Definición del tamaño de los campos: 300px de ancho y 32px de alto
        Dimension fieldPrefSize = new Dimension(300, 32);
        rut.setPreferredSize(fieldPrefSize);
        pass.setPreferredSize(fieldPrefSize);
        login.setPreferredSize(fieldPrefSize);

        // Aseguramos que los labels de título/descripción ocupen el mismo ancho para centrarse bien.
        titulo.setPreferredSize(fieldPrefSize);
        descripcion.setPreferredSize(fieldPrefSize);

        // --- GridBagLayout para la disposición interna del formulario ---
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 20, 4, 20); // Padding interno del recuadro
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER; // Cada componente ocupa toda la fila

        // 1. Título y Descripción
        gbc.insets = new Insets(20, 20, 5, 20);
        formPanel.add(titulo, gbc);
        gbc.insets = new Insets(0, 20, 30, 20);
        formPanel.add(descripcion, gbc);

        // 2. Campo RUT
        gbc.insets = new Insets(0, 20, 3, 20);
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("RUT"), gbc);
        gbc.insets = new Insets(0, 20, 10, 20);
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(rut, gbc);

        // 3. Campo Contraseña
        gbc.insets = new Insets(5, 20, 3, 20);
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Contraseña"), gbc);
        gbc.insets = new Insets(0, 20, 20, 20);
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(pass, gbc);

        // 4. Botón Ingresar
        gbc.insets = new Insets(0, 20, 15, 20);
        formPanel.add(login, gbc);

        // 5. Sección de Registro
        gbc.insets = new Insets(0, 20, 20, 20);
        formPanel.add(crearSeccionRegistro(), gbc);

        boolean darkInitial = UIManager.getLookAndFeel().getName().toLowerCase().contains("dark");
        JLayeredPane themed = ManagerTemas.wrapWithFloatingThemeButton(formPanel, darkInitial);
        setLayout(new BorderLayout());
        add(themed, BorderLayout.CENTER);

        // Agregamos el ActionListener y KeyStroke para el login
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
        // Evita estilos FlatLaf no soportados; dejamos el link-like minimalista
        registrar.setContentAreaFilled(false);
        registrar.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
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