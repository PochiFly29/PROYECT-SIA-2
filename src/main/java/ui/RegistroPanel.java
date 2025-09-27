package ui;

import com.formdev.flatlaf.FlatClientProperties;
import gestores.GestorIntercambio;
import modelo.Estudiante;
import net.miginfocom.swing.MigLayout;
import servicios.VerificarInput;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class RegistroPanel extends JPanel {
    private final GestorIntercambio gestor;
    private final Runnable onRegisterSuccess;

    private JTextField rutF, nombreF, emailF, carreraF;
    private JPasswordField passF;
    private JSpinner promedioF, semestresF;
    private JButton registrarBtn, volverBtn;

    public RegistroPanel(GestorIntercambio gestor, Runnable onRegisterSuccess) {
        this.gestor = gestor;
        this.onRegisterSuccess = onRegisterSuccess;
        init();
    }

    private void init() {
        setLayout(new MigLayout("fill,insets 20", "[center]", "[center]"));

        JPanel p = new JPanel(new MigLayout("wrap,fillx,insets 35 45 30 45", "fill,350::380"));
        p.putClientProperty(FlatClientProperties.STYLE, "arc:20; background:lighten(@background,3%)");

        JLabel titulo = new JLabel("Registro de Estudiante");
        titulo.putClientProperty(FlatClientProperties.STYLE, "font:bold +10");

        rutF = new JTextField();
        nombreF = new JTextField();
        emailF = new JTextField();
        passF = new JPasswordField();
        carreraF = new JTextField();
        promedioF = new JSpinner(new SpinnerNumberModel(4.0, 1.0, 7.0, 0.1));
        semestresF = new JSpinner(new SpinnerNumberModel(1, 1, 15, 1));

        passF.putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true");
        rutF.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "11111111K");

        JSpinner.NumberEditor promEditor = new JSpinner.NumberEditor(promedioF, "0.0");
        promedioF.setEditor(promEditor);

        p.add(titulo, "center,gapbottom 15");
        p.add(new JLabel("RUT")); p.add(rutF);
        p.add(new JLabel("Nombre Completo")); p.add(nombreF);
        p.add(new JLabel("Email")); p.add(emailF);
        p.add(new JLabel("Contraseña")); p.add(passF);
        p.add(new JLabel("Carrera")); p.add(carreraF);
        p.add(new JLabel("Promedio (1.0 - 7.0)")); p.add(promedioF);
        p.add(new JLabel("Semestres Cursados (1 - 15)")); p.add(semestresF);

        registrarBtn = new JButton("Registrar");
        volverBtn = new JButton("Volver al Login");

        JPanel btnPanel = new JPanel(new MigLayout("insets 0", "[]15[]", "[]"));
        btnPanel.putClientProperty(FlatClientProperties.STYLE, "background:null");
        btnPanel.add(registrarBtn);
        btnPanel.add(volverBtn);

        p.add(btnPanel, "center, gaptop 15");
        add(p);

        registrarBtn.addActionListener(e -> doRegistro());
        volverBtn.addActionListener(e -> onRegisterSuccess.run());
    }

    private String toPascalCase(String s) {
        if (s == null || s.trim().isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        String[] words = s.trim().split("\\s+");
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)));
                sb.append(word.substring(1).toLowerCase());
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }

    private void doRegistro() {
        try {
            String rut = rutF.getText().trim();
            String nombre = nombreF.getText().trim();
            String email = emailF.getText().trim();
            String pass = new String(passF.getPassword());
            String carrera = carreraF.getText().trim();
            double promedio = ((Number) promedioF.getValue()).doubleValue();
            int semestres = (int) semestresF.getValue();

            StringBuilder errores = new StringBuilder();

            if (!VerificarInput.rutValido(rut)) {
                errores.append("• El RUT no es válido. Formato: 11111111K (sin guion)\n");
            }
            if (gestor.existeUsuario(VerificarInput.normalizarRut(rut))) {
                errores.append("• Ya existe un usuario con este RUT.\n");
            }
            if (nombre.isEmpty()) {
                errores.append("• El nombre no puede estar vacío.\n");
            }
            if (pass.length() < 3) {
                errores.append("• La contraseña debe tener al menos 3 caracteres.\n");
            }
            if (!email.contains("@")) {
                errores.append("• El email no es válido.\n");
            }
            if (carrera.isEmpty()) {
                errores.append("• La carrera no puede estar vacía.\n");
            }
            if (promedio < 1.0 || promedio > 7.0) {
                errores.append("• El promedio debe estar entre 1.0 y 7.0.\n");
            }
            if (semestres < 1 || semestres > 15) {
                errores.append("• El número de semestres debe estar entre 1 y 15.\n");
            }

            if (errores.length() > 0) {
                JOptionPane.showMessageDialog(this, errores.toString(), "Error de Registro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String rutNormalizado = VerificarInput.normalizarRut(rut);
            String nombreFormateado = toPascalCase(nombre);

            gestor.registrarEstudiante(rutNormalizado, nombreFormateado, email, pass, carrera, semestres, promedio);
            JOptionPane.showMessageDialog(this, "Estudiante registrado exitosamente.");
            onRegisterSuccess.run();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo registrar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}