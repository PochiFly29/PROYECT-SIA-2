package ui;

import com.formdev.flatlaf.FlatClientProperties;
import gestores.GestorIntercambio;
import modelo.Estudiante;
import modelo.Usuario;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class PerfilPanel extends JPanel {

    private final GestorIntercambio gestor;
    private Usuario usuario; // Almacenamos el Usuario base

    // Campos del Perfil (Comunes)
    private JLabel lblRol;
    private JLabel lblRut;
    private JTextField txtNombre;
    private JTextField txtEmail;
    private JPasswordField txtPass;

    // Campos del Perfil (Específicos de Estudiante)
    private JPanel panelAcademico; // Panel que contendrá la info académica
    private JTextField txtCarrera;
    private JTextField txtSemestres;
    private JTextField txtPromedio;

    // Constructor que acepta el objeto Usuario/Estudiante
    public PerfilPanel(GestorIntercambio gestor, Usuario usuario) {
        this.gestor = gestor;
        this.usuario = usuario;
        init();
        refreshData();
    }

    // Método para actualizar la instancia de Usuario si se necesita (ej: en el EstudiantePanel)
    public void setUsuario(Usuario u) {
        this.usuario = u;
        refreshData();
    }

    private void init() {
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.weightx = 1.0;

        JLabel title = new JLabel("Mi Perfil");
        title.putClientProperty(FlatClientProperties.STYLE, "font:bold +10; foreground:darken(@background, 50%)");
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(title, gbc);

        // --- Datos Básicos (Comunes a todos) ---
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        lblRol = new JLabel();
        lblRol.putClientProperty(FlatClientProperties.STYLE, "font:bold +2; foreground:lighten(@foreground,20%)");
        addLabelAndComponent(formPanel, "Rol:", lblRol, gbc, 1);

        lblRut = new JLabel();
        addLabelAndComponent(formPanel, "RUT:", lblRut, gbc, 2);

        txtNombre = new JTextField(20);
        addLabelAndComponent(formPanel, "Nombre Completo:", txtNombre, gbc, 3);

        txtEmail = new JTextField(20);
        addLabelAndComponent(formPanel, "Email:", txtEmail, gbc, 4);

        txtPass = new JPasswordField(20);
        txtPass.putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true");
        addLabelAndComponent(formPanel, "Contraseña (Nueva):", txtPass, gbc, 5);

        // --- Datos Académicos (Solo para Estudiante) ---
        panelAcademico = new JPanel(new GridBagLayout());
        panelAcademico.setOpaque(false);
        GridBagConstraints gbcAcademico = new GridBagConstraints();
        gbcAcademico.fill = GridBagConstraints.HORIZONTAL;
        gbcAcademico.insets = new Insets(8, 5, 8, 5);
        gbcAcademico.weightx = 1.0;
        gbcAcademico.gridwidth = 2;

        JLabel acaTitle = new JLabel("Información Académica");
        acaTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +1; foreground:lighten(@foreground,20%)");
        gbcAcademico.gridx = 0;
        gbcAcademico.gridy = 0;
        panelAcademico.add(acaTitle, gbcAcademico);

        gbcAcademico.gridwidth = 1;

        txtCarrera = new JTextField(20);
        addLabelAndComponent(panelAcademico, "Carrera:", txtCarrera, gbcAcademico, 1);

        txtSemestres = new JTextField(5);
        addLabelAndComponent(panelAcademico, "Semestres Cursados:", txtSemestres, gbcAcademico, 2);

        txtPromedio = new JTextField(5);
        addLabelAndComponent(panelAcademico, "Promedio Ponderado:", txtPromedio, gbcAcademico, 3);

        // Añadir el panel académico al formulario principal (inicialmente invisible si no es estudiante)
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 6;
        formPanel.add(panelAcademico, gbc);


        // --- Botón de Guardar ---
        JButton btnGuardar = new JButton("Guardar Cambios");
        btnGuardar.putClientProperty(FlatClientProperties.STYLE, "background:#2E86FF; foreground:#FFFFFF; font:bold +1");
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.insets = new Insets(30, 5, 8, 5);
        formPanel.add(btnGuardar, gbc);

        // Añadir el formulario al centro del PerfilPanel (para que no ocupe todo el espacio)
        add(new JScrollPane(formPanel), BorderLayout.CENTER);

        // --- Listeners ---
        btnGuardar.addActionListener(e -> guardarCambios());
    }

    private void addLabelAndComponent(JPanel panel, String labelText, JComponent component, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        panel.add(component, gbc);
    }

    public void refreshData() {
        if (usuario == null) return;

        lblRol.setText(usuario.getRol().name());
        lblRut.setText(usuario.getRut());
        txtNombre.setText(usuario.getNombreCompleto());
        txtEmail.setText(usuario.getEmail());
        txtPass.setText(""); // Siempre limpia la contraseña

        // --- Lógica polimórfica: Solo muestra info académica si es Estudiante ---
        boolean esEstudiante = usuario instanceof Estudiante;
        panelAcademico.setVisible(esEstudiante);

        if (esEstudiante) {
            Estudiante estudiante = (Estudiante) usuario;
            txtCarrera.setText(estudiante.getCarrera());
            txtSemestres.setText(String.valueOf(estudiante.getSemestresCursados()));
            txtPromedio.setText(String.format("%.2f", estudiante.getPromedio()));
        }
    }

    private void guardarCambios() {
        try {
            String rut = usuario.getRut();
            boolean cambiosRealizados = false;

            // 1. Validar y Guardar Cambios Comunes (Nombre y Email)
            String nuevoNombre = txtNombre.getText().trim();
            String nuevoEmail = txtEmail.getText().trim();

            if (!nuevoNombre.equals(usuario.getNombreCompleto()) || !nuevoEmail.equals(usuario.getEmail())) {
                // CAMBIO: Usamos un solo método del servicio para actualizar el perfil básico.
                gestor.getServicioAutenticacion().actualizarPerfilUsuario(rut, nuevoNombre, nuevoEmail);
                cambiosRealizados = true;
            }

            // 2. Validar y Guardar Nueva Contraseña
            String nuevaPass = new String(txtPass.getPassword());
            if (nuevaPass.length() >= 3) {
                // CAMBIO: Llamamos al servicio correspondiente.
                gestor.getServicioAutenticacion().actualizarPasswordUsuario(rut, nuevaPass);
                cambiosRealizados = true;
            }
            txtPass.setText(""); // Limpiar campo después de guardar

            // 3. Validar y Guardar Cambios de Estudiante
            if (usuario instanceof Estudiante) {
                Estudiante estudiante = (Estudiante) usuario;
                String nuevaCarrera = txtCarrera.getText().trim();
                int nuevosSemestres = Integer.parseInt(txtSemestres.getText().trim());
                double nuevoPromedio = Double.parseDouble(txtPromedio.getText().trim().replace(',', '.'));

                // Comprobamos si alguno de los datos académicos ha cambiado
                if (!nuevaCarrera.equals(estudiante.getCarrera()) || nuevosSemestres != estudiante.getSemestresCursados() || nuevoPromedio != estudiante.getPromedio()) {
                    // CAMBIO: Usamos un método específico para datos académicos.
                    gestor.getServicioAutenticacion().actualizarDatosAcademicos(rut, nuevaCarrera, nuevosSemestres, nuevoPromedio);
                    cambiosRealizados = true;
                }
            }

            if (cambiosRealizados) {
                JOptionPane.showMessageDialog(this, "Perfil actualizado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                refreshData(); // Recargar datos para reflejar cambios
            } else {
                JOptionPane.showMessageDialog(this, "No se detectaron cambios para guardar.", "Información", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error en el formato de los números (semestres/promedio).", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ocurrió un error al guardar los cambios: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}