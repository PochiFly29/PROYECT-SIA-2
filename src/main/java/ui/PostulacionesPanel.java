package ui;

import com.formdev.flatlaf.FlatClientProperties;
import gestores.GestorIntercambio;
import modelo.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * **Panel de Interfaz de Usuario para la Visualización de Postulaciones (Rol Estudiante).**
 * <p>Muestra un listado de **todas las {@link Postulacion}es** creadas por el
 * {@link Estudiante} actualmente logueado. Permite al estudiante ver el estado de
 * sus solicitudes y acceder al detalle para adjuntar documentos (CV, Notas, etc.).</p>
 */
public class PostulacionesPanel extends JPanel {

    private final GestorIntercambio gestor;
    private final Estudiante estudiante; // CAMBIO: Ahora es específicamente un Estudiante.

    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private boolean estructuraConstruida = false;

    // CAMBIO: El constructor ahora espera un Estudiante, no un Usuario genérico.
    /**
     * Crea e inicializa el panel de postulaciones del estudiante.
     * @param gestor El gestor central de la aplicación.
     * @param estudiante El estudiante cuyos datos se consultarán.
     */
    public PostulacionesPanel(GestorIntercambio gestor, Estudiante estudiante) {
        this.gestor = Objects.requireNonNull(gestor);
        this.estudiante = estudiante;
        refresh();
    }

    public void refresh() {
        // CAMBIO: La lógica se simplifica enormemente. Ya no hay que verificar roles.
        // Obtenemos las postulaciones del programa 1 que correspondan al RUT del estudiante.
        Programa programa = gestor.getServicioConsulta().getProgramaActivo().orElse(null);
        List<Postulacion> postulaciones;

        if (programa != null) {
            postulaciones = gestor.getServicioConsulta().getPostulacionesFiltradas(programa, "rut", estudiante.getRut());
        } else {
            postulaciones = Collections.emptyList();
        }

        if (!estructuraConstruida) {
            initUI(); // Construye la UI la primera vez.
        }

        if (postulaciones.isEmpty()) {
            // Muestra un mensaje si no hay postulaciones.
            removeAll();
            setLayout(new GridBagLayout());
            JLabel vacio = new JLabel("Aún no tienes postulaciones registradas.");
            vacio.putClientProperty(FlatClientProperties.STYLE, "font:+2");
            add(vacio, new GridBagConstraints());
        } else {
            // Carga los datos en la tabla.
            model.setRowCount(0);
            for (Postulacion p : postulaciones) {
                Convenio c = p.getConvenioSeleccionado();
                model.addRow(new Object[]{
                        p.getId(),
                        c.getUniversidad(),
                        c.getPais(),
                        c.getArea(),
                        p.getFechaPostulacion().toString(),
                        p.getEstado().name(),
                        p // Objeto oculto
                });
            }
        }
        revalidate();
        repaint();
    }

    private void openDetalle(Postulacion p) {
        if (p == null) return;

        // 1. Crea el diálogo que contendrá nuestro panel de detalles.
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Detalle de Postulación", Dialog.ModalityType.APPLICATION_MODAL);

        // 2. Crea el panel de detalles, pasándole el usuario correcto (estudiante o funcionario).
        DetallePostulacionPanel detallePanel = new DetallePostulacionPanel(gestor, p, estudiante, dialog); // this.usuario se refiere al atributo de la clase

        // 3. Configura y muestra el diálogo.
        dialog.setContentPane(detallePanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true); // El código se detiene aquí hasta que el diálogo se cierra.

        // 4. CAMBIO CLAVE: Esta línea se ejecuta JUSTO DESPUÉS de que el diálogo se cierra.
        // Aquí es donde forzamos la actualización de la tabla.
        refresh(); // O refreshTodasLasPostulaciones() en el caso del funcionario.
    }
    private void initUI() {
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        JLabel title = new JLabel("MIS POSTULACIONES", SwingConstants.CENTER);
        title.putClientProperty(FlatClientProperties.STYLE, "font:bold +4");

        JTextField searchField = new JTextField();
        searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Buscar...");

        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(searchField, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Tabla
        String[] cols = {"ID", "Universidad", "País", "Área", "Fecha", "Estado", "_OBJ"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.getColumn("_OBJ").setMinWidth(0);
        table.getColumn("_OBJ").setMaxWidth(0);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Footer
        JButton btnDetalle = new JButton("Ver Detalle / Adjuntar Documentos");
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.add(btnDetalle);
        add(footerPanel, BorderLayout.SOUTH);

        // Listeners
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(searchField.getText()); }
            public void removeUpdate(DocumentEvent e) { applyFilter(searchField.getText()); }
            public void changedUpdate(DocumentEvent e) { applyFilter(searchField.getText()); }
        });

        btnDetalle.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow >= 0) {
                int modelRow = table.convertRowIndexToModel(viewRow);
                Postulacion p = (Postulacion) model.getValueAt(modelRow, 6);
                openDetalle(p);
            }
        });

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
                    Postulacion p = (Postulacion) model.getValueAt(modelRow, 6);
                    openDetalle(p);
                }
            }
        });

        estructuraConstruida = true;
    }

    private void applyFilter(String text) {
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
    }
}