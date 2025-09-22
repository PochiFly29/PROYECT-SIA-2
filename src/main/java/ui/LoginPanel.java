package ui;

import com.formdev.flatlaf.FlatClientProperties;
import gestores.GestorIntercambio;
import gestores.ResultadoLogin;
import modelo.Usuario;
import net.miginfocom.swing.MigLayout;
import servicios.VerificarInput;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class LoginPanel extends JPanel {

    private final GestorIntercambio gestor;
    private final Consumer<Usuario> onSuccess;

    private JTextField rut;
    private JPasswordField pass;
    private JButton login;

    public LoginPanel(GestorIntercambio gestor, Consumer<Usuario> onSuccess) {
        this.gestor = gestor;
        this.onSuccess = onSuccess;
        init();
    }

    private void init(){
        setLayout(new MigLayout("fill,insets 20", "[center]","[center]"));

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

        login.addActionListener(e -> doLogin());
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke("ENTER"), "enterLogin");
        getActionMap().put("enterLogin", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { doLogin(); }
        });
    }

    private static void marcarError(JComponent c, boolean error) {
        c.putClientProperty(FlatClientProperties.OUTLINE, error ? "error" : null);
    }

    public void addNotify() {
        super.addNotify();
        var rp = SwingUtilities.getRootPane(this);
        if (rp != null) rp.setDefaultButton(login);
    }

    private Component crearSeccionRegistro(){
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
        panel.putClientProperty(FlatClientProperties.STYLE, "background:null");
        JButton registrar = new JButton("<html><a href=\"#\">Registrar</a></html>");
        registrar.putClientProperty(FlatClientProperties.STYLE, "border:3,3,3,3");
        registrar.setContentAreaFilled(false);
        registrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registrar.addActionListener(e -> abrirRegistro());
        JLabel label = new JLabel("¿No tienes una cuenta? ");
        label.putClientProperty(FlatClientProperties.STYLE, "foreground:darken(@foreground,30%)");
        panel.add(label);
        panel.add(registrar);
        return panel;
    }

    // === Lógica conectada a GestorIntercambio / ResultadoLogin ===
    private void doLogin() {
        if (gestor == null) {
            JOptionPane.showMessageDialog(this, "Gestor no inicializado.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String rutTxt  = rut.getText().trim().toUpperCase();
        String passTxt = new String(pass.getPassword());

        ResultadoLogin r = gestor.iniciarSesion(rutTxt, passTxt);
        if (r.isExito()) onSuccess.accept(r.getUsuario());
        else {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, r.getMensaje(), "Inicio de sesión", JOptionPane.ERROR_MESSAGE);
            pass.setText(""); pass.requestFocusInWindow();
        }
    }

    private void abrirRegistro() {
        if (gestor == null) {
            JOptionPane.showMessageDialog(this, "Gestor no inicializado.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JTextField rutF = new JTextField();
        JTextField nombreF = new JTextField();
        JTextField emailF = new JTextField();
        JPasswordField passF = new JPasswordField();
        JTextField carreraF = new JTextField();
        JSpinner promedioF = new JSpinner(new SpinnerNumberModel(5.0, 1.0, 7.0, 0.1));
        JSpinner semestresF = new JSpinner(new SpinnerNumberModel(1, 0, 30, 1));

        rutF.putClientProperty(com.formdev.flatlaf.FlatClientProperties.PLACEHOLDER_TEXT, "11111111K (sin puntos ni guion)");
        passF.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE, "showRevealButton:true");

        JPanel p = new JPanel(new net.miginfocom.swing.MigLayout("wrap,fillx,insets 15 20 10 20", "fill,280::320"));
        p.add(new JLabel("RUT")); p.add(rutF);
        p.add(new JLabel("Nombre completo")); p.add(nombreF);
        p.add(new JLabel("Email")); p.add(emailF);
        p.add(new JLabel("Contraseña")); p.add(passF);
        p.add(new JLabel("Carrera")); p.add(carreraF);
        p.add(new JLabel("Promedio (1.0 - 7.0)")) ; p.add(promedioF);
        p.add(new JLabel("Semestres cursados")); p.add(semestresF);

        int opt = JOptionPane.showConfirmDialog(this, p, "Registrar Estudiante",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opt != JOptionPane.OK_OPTION) return;

        marcarError(rutF,false); marcarError(passF,false); marcarError(emailF,false);

        String rutN = VerificarInput.normalizarRut(rutF.getText().trim());
        String nombre = nombreF.getText().trim();
        String email = emailF.getText().trim();
        String passTxt  = new String(passF.getPassword());
        String carrera  = carreraF.getText().trim();
        double promedio = ((Number) promedioF.getValue()).doubleValue();
        int semestres   = (int) semestresF.getValue();

        StringBuilder errores = new StringBuilder();

        if (!VerificarInput.rutValido(rutN)) {
            marcarError(rutF, true);
            errores.append("• RUT inválido (8 dígitos + DV correcto)\n");
        } else if (gestor.existeUsuario(rutN)) {
            marcarError(rutF, true);
            errores.append("• Ya existe un usuario con este RUT\n");
        }

        if (passTxt.isEmpty()) {
            marcarError(passF, true);
            errores.append("• La contraseña no puede estar vacía\n");
        }

        if (errores.length()==0 && gestor.existeUsuario(rutN)) {
            marcarError(rutF, true);
            errores.append("• Ya existe un usuario con este RUT\n");
        }

        if (!email.contains("@")) {
            marcarError(emailF, true);
            errores.append("• Email inválido\n");
        }

        if (errores.length() > 0) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, errores.toString(), "Registro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            gestor.registrarEstudiante(rutN, nombre, email, passTxt, carrera, semestres, promedio);
            JOptionPane.showMessageDialog(this, "Estudiante registrado exitosamente.");
        } catch (Exception ex) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, "No se pudo registrar: " + ex.getMessage(),"Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
