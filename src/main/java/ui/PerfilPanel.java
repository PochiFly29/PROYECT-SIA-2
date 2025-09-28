package ui;

import com.formdev.flatlaf.FlatClientProperties;
import gestores.GestorIntercambio;
import modelo.Estudiante;
import modelo.Usuario;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;

/**
 * Panel que muestra y permite editar los datos del perfil de un usuario.
 * Incluye campos generales para todos los usuarios y campos académicos
 * específicos para estudiantes.
 */
public class PerfilPanel extends JPanel {

    private final GestorIntercambio gestor;
    private Usuario usuario;

    // Campos del Perfil (Comunes)
    private JLabel lblRol;
    private JLabel lblRut;
    private JTextField txtNombre;
    private JTextField txtEmail;
    private JPasswordField txtPass;

    private JPanel panelAcademico;
    private JTextField txtCarrera;
    private JTextField txtSemestres;
    private JTextField txtPromedio;

    private JLabel promLbl;
    private JLabel carreraLbl;
    private JLabel semLbl;

    private JLabel avatarImage;

    private JScrollPane sp;

    /**
     * Constructor.
     *
     * @param gestor  Gestor de servicios de la aplicación.
     * @param usuario Usuario cuyo perfil se mostrará.
     */
    public PerfilPanel(GestorIntercambio gestor, Usuario usuario) {
        this.gestor = gestor;
        this.usuario = usuario;
        init();
        refreshData();
    }

    public void setUsuario(Usuario u) {
        this.usuario = u;
        refreshData();
    }

    private void init() {
        setBorder(null);
        putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE, "border: null");
        setOpaque(true);
        setBackground(UIManager.getColor("Panel.background"));

        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(112, 24, 40, 24));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JPanel avatar = new JPanel(new GridBagLayout());
        avatar.setMaximumSize(new Dimension(420, 240));
        avatar.setPreferredSize(new Dimension(420, 240));
        avatar.putClientProperty(FlatClientProperties.STYLE, "arc:32; background:#D7D7D7");

        avatarImage = new JLabel();
        avatarImage.setHorizontalAlignment(SwingConstants.CENTER);
        avatarImage.setVerticalAlignment(SwingConstants.BOTTOM);
        avatarImage.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        GridBagConstraints ac = new GridBagConstraints();
        ac.gridx = 0;
        ac.gridy = 0;
        ac.weightx = 1.0;
        ac.weighty = 1.0;
        ac.anchor = GridBagConstraints.SOUTH;
        avatar.add(avatarImage, ac);

        loadAvatar(avatar.getPreferredSize());

        lblRol = new JLabel();
        lblRol.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblRol.setHorizontalAlignment(SwingConstants.CENTER);
        lblRol.putClientProperty(FlatClientProperties.STYLE, "font:bold +5; foreground:#FFFFFF");

        lblRut = new JLabel();
        lblRut.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblRut.setHorizontalAlignment(SwingConstants.CENTER);
        lblRut.putClientProperty(FlatClientProperties.STYLE, "font:medium +4; foreground:#FFFFFF");

        header.add(Box.createVerticalStrut(24));
        header.add(avatar);
        header.add(Box.createVerticalStrut(14));
        header.add(lblRol);
        header.add(Box.createVerticalStrut(6));
        header.add(lblRut);
        header.add(Box.createVerticalStrut(32));

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;

        Dimension fieldPref = new Dimension(420, 56);
        Insets labelInsets = new Insets(8, 48, 2, 48);
        Insets fieldInsets = new Insets(2, 48, 16, 48);

        JLabel nombreLbl = new JLabel("Nombre");
        nombreLbl.putClientProperty(FlatClientProperties.STYLE, "font:bold +4; foreground:#FFFFFF");
        txtNombre = new JTextField();
        txtNombre.setPreferredSize(fieldPref);
        txtNombre.setMaximumSize(new Dimension(fieldPref.width, Integer.MAX_VALUE));
        txtNombre.putClientProperty(FlatClientProperties.STYLE, "arc:12; borderWidth:1; focusWidth:1");
        txtNombre.putClientProperty("JComponent.sizeVariant","large");

        JLabel emailLbl = new JLabel("Email");
        emailLbl.putClientProperty(FlatClientProperties.STYLE, "font:bold +4; foreground:#FFFFFF");
        txtEmail = new JTextField();
        txtEmail.setPreferredSize(fieldPref);
        txtEmail.setMaximumSize(new Dimension(fieldPref.width, Integer.MAX_VALUE));
        txtEmail.putClientProperty(FlatClientProperties.STYLE, "arc:12; borderWidth:1; focusWidth:1");
        txtEmail.putClientProperty("JComponent.sizeVariant","large");

        JLabel passLbl = new JLabel("Contraseña (Nueva)");
        passLbl.putClientProperty(FlatClientProperties.STYLE, "font:bold +4; foreground:#FFFFFF");
        txtPass = new JPasswordField();
        txtPass.setPreferredSize(fieldPref);
        txtPass.setMaximumSize(new Dimension(fieldPref.width, Integer.MAX_VALUE));
        txtPass.putClientProperty(FlatClientProperties.STYLE, "showRevealButton:true; arc:12; borderWidth:1; focusWidth:1");
        txtPass.putClientProperty("JComponent.sizeVariant","large");

        gbc.insets = labelInsets;
        gbc.gridx = 0; gbc.gridy = 0; grid.add(nombreLbl, gbc);
        gbc.gridx = 1; gbc.gridy = 0; grid.add(emailLbl, gbc);
        gbc.gridx = 2; gbc.gridy = 0; grid.add(passLbl, gbc);

        gbc.insets = fieldInsets;
        gbc.gridx = 0; gbc.gridy = 1; grid.add(txtNombre, gbc);
        gbc.gridx = 1; gbc.gridy = 1; grid.add(txtEmail, gbc);
        gbc.gridx = 2; gbc.gridy = 1; grid.add(txtPass, gbc);

        promLbl = new JLabel("Promedio");
        promLbl.putClientProperty(FlatClientProperties.STYLE, "font:bold +4; foreground:#FFFFFF");
        carreraLbl = new JLabel("Carrera");
        carreraLbl.putClientProperty(FlatClientProperties.STYLE, "font:bold +4; foreground:#FFFFFF");
        semLbl = new JLabel("Semestres Cursados");
        semLbl.putClientProperty(FlatClientProperties.STYLE, "font:bold +4; foreground:#FFFFFF");

        gbc.insets = new Insets(28, 48, 2, 48);
        gbc.gridx = 0; gbc.gridy = 2; grid.add(promLbl, gbc);
        gbc.gridx = 1; gbc.gridy = 2; grid.add(carreraLbl, gbc);
        gbc.gridx = 2; gbc.gridy = 2; grid.add(semLbl, gbc);

        gbc.insets = fieldInsets;
        txtPromedio = new JTextField();
        txtPromedio.setPreferredSize(fieldPref);
        txtPromedio.setMaximumSize(new Dimension(fieldPref.width, Integer.MAX_VALUE));
        txtPromedio.putClientProperty(FlatClientProperties.STYLE, "arc:12; borderWidth:1; focusWidth:1");
        txtPromedio.putClientProperty("JComponent.sizeVariant","large");

        txtCarrera = new JTextField();
        txtCarrera.setPreferredSize(fieldPref);
        txtCarrera.setMaximumSize(new Dimension(fieldPref.width, Integer.MAX_VALUE));
        txtCarrera.putClientProperty(FlatClientProperties.STYLE, "arc:12; borderWidth:1; focusWidth:1");
        txtCarrera.putClientProperty("JComponent.sizeVariant","large");

        txtSemestres = new JTextField();
        txtSemestres.setPreferredSize(fieldPref);
        txtSemestres.setMaximumSize(new Dimension(fieldPref.width, Integer.MAX_VALUE));
        txtSemestres.putClientProperty(FlatClientProperties.STYLE, "arc:12; borderWidth:1; focusWidth:1");
        txtSemestres.putClientProperty("JComponent.sizeVariant","large");

        gbc.gridx = 0; gbc.gridy = 3; grid.add(txtPromedio, gbc);
        gbc.gridx = 1; gbc.gridy = 3; grid.add(txtCarrera, gbc);
        gbc.gridx = 2; gbc.gridy = 3; grid.add(txtSemestres, gbc);

        panelAcademico = new JPanel(new GridBagLayout());
        panelAcademico.setOpaque(false);

        JButton btnGuardar = new JButton("Guardar Cambios");
        btnGuardar.putClientProperty(FlatClientProperties.STYLE, "background:#2E86FF; foreground:#FFFFFF; font:normal +4; arc:14; borderWidth:0; focusWidth:0; innerFocusWidth:0");
        btnGuardar.putClientProperty("JComponent.sizeVariant","large");
        btnGuardar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel contentStack = new JPanel();
        contentStack.setOpaque(false);
        contentStack.setLayout(new BoxLayout(contentStack, BoxLayout.Y_AXIS));
        grid.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentStack.add(grid);
        contentStack.add(Box.createVerticalStrut(40));
        contentStack.add(btnGuardar);

        JPanel centerWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centerWrap.setOpaque(false);
        centerWrap.add(contentStack);

        root.add(header, BorderLayout.NORTH);
        root.add(centerWrap, BorderLayout.CENTER);

        sp = new JScrollPane(root);
        sp.setBorder(null);
        sp.setViewportBorder(null);
        add(sp, BorderLayout.CENTER);

        setBorder(null);
        putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE, "border: null");

        btnGuardar.addActionListener(e -> guardarCambios());
    }

    private void stripBorders() {
        setBorder(null);
        putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE, "border: null");
        if (sp != null) {
            sp.setBorder(BorderFactory.createEmptyBorder());
            sp.setViewportBorder(BorderFactory.createEmptyBorder());
            sp.putClientProperty(com.formdev.flatlaf.FlatClientProperties.STYLE, "border: null");
            if (sp.getViewport() != null) sp.getViewport().setOpaque(false);
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();
        SwingUtilities.invokeLater(this::stripBorders);
    }

    public void refreshData() {
        if (usuario == null) return;

        lblRol.setText(usuario.getRol().name());
        lblRut.setText("Rut: " + usuario.getRut());
        txtNombre.setText(usuario.getNombreCompleto());
        txtEmail.setText(usuario.getEmail());
        txtPass.setText("");

        boolean esEstudiante = usuario instanceof Estudiante;

        promLbl.setVisible(esEstudiante);
        carreraLbl.setVisible(esEstudiante);
        semLbl.setVisible(esEstudiante);
        if (esEstudiante) {
            Estudiante estudiante = (Estudiante) usuario;
            txtCarrera.setText(estudiante.getCarrera());
            txtSemestres.setText(String.valueOf(estudiante.getSemestresCursados()));
            txtPromedio.setText(String.format("%.2f", estudiante.getPromedio()));
            txtCarrera.setVisible(true);
            txtSemestres.setVisible(true);
            txtPromedio.setVisible(true);
        } else {
            txtCarrera.setVisible(false);
            txtSemestres.setVisible(false);
            txtPromedio.setVisible(false);
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
                gestor.getServicioAutenticacion().actualizarPerfilUsuario(rut, nuevoNombre, nuevoEmail);
                cambiosRealizados = true;
            }

            // 2. Validar y Guardar Nueva Contraseña
            String nuevaPass = new String(txtPass.getPassword());
            if (nuevaPass.length() >= 3) {
                gestor.getServicioAutenticacion().actualizarPasswordUsuario(rut, nuevaPass);
                cambiosRealizados = true;
            }
            txtPass.setText("");

            // 3. Validar y Guardar Cambios de Estudiante
            if (usuario instanceof Estudiante) {
                Estudiante estudiante = (Estudiante) usuario;
                String nuevaCarrera = txtCarrera.getText().trim();
                int nuevosSemestres = Integer.parseInt(txtSemestres.getText().trim());
                double nuevoPromedio = Double.parseDouble(txtPromedio.getText().trim().replace(',', '.'));

                // Comprobamos si alguno de los datos académicos ha cambiado
                if (!nuevaCarrera.equals(estudiante.getCarrera()) || nuevosSemestres != estudiante.getSemestresCursados() || nuevoPromedio != estudiante.getPromedio()) {
                    gestor.getServicioAutenticacion().actualizarDatosAcademicos(rut, nuevaCarrera, nuevosSemestres, nuevoPromedio);
                    cambiosRealizados = true;
                }
            }

            if (cambiosRealizados) {
                JOptionPane.showMessageDialog(this, "Perfil actualizado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
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

    private void loadAvatar(Dimension box) {
        try (InputStream is = getClass().getResourceAsStream("/AvatarOscuro.png")) {
            if (is == null) return;
            BufferedImage img = ImageIO.read(is);
            ImageIcon icon = scaleToFit(img, box.width - 2, box.height - 2);
            avatarImage.setIcon(icon);
        } catch (Exception ignore) { }
    }

    private static ImageIcon scaleToFit(BufferedImage img, int maxW, int maxH) {
        int iw = img.getWidth();
        int ih = img.getHeight();
        double sw = maxW / (double) iw;
        double sh = maxH / (double) ih;
        double s = Math.min(sw, sh);
        int nw = Math.max(1, (int) Math.round(iw * s));
        int nh = Math.max(1, (int) Math.round(ih * s));
        BufferedImage out = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(img, 0, 0, nw, nh, null);
        g2.dispose();
        return new ImageIcon(out);
    }
}