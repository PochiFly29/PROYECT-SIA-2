package ui;

import com.formdev.flatlaf.FlatClientProperties;
import enums.EstadoPostulacion;
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
import java.util.stream.Collectors;

/**
 * Panel destinado a la gestión de postulaciones por parte de un funcionario.
 * Permite visualizar todas las postulaciones, filtrar por convenio o estado,
 * buscar por texto, y acceder a un detalle editable de cada postulación.
 */
public class PostulacionesFuncionarioPanel extends JPanel {

    private final GestorIntercambio gestor;
    private final Usuario funcionario;
    private List<Postulacion> todasLasPostulaciones = Collections.emptyList();

    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;
    private JCheckBox chkFiltro;
    private JLabel title;

    /**
     * Constructor.
     *
     * @param gestor     Gestor de servicios de la aplicación.
     * @param funcionario Usuario con rol de funcionario.
     */
    public PostulacionesFuncionarioPanel(GestorIntercambio gestor, Usuario funcionario) {
        this.gestor = Objects.requireNonNull(gestor);
        this.funcionario = funcionario;
        initUI();
        refreshTodasLasPostulaciones();
    }

    public void refreshTodasLasPostulaciones() {
        // CAMBIO: Se obtiene la lista de postulaciones del programa 1 desde el servicio de consulta.
        this.todasLasPostulaciones = gestor.getServicioConsulta().getProgramaPorId(1)
                .map(Programa::getPostulaciones)
                .orElse(Collections.emptyList());
        title.setText("GESTIÓN DE POSTULACIONES");
        loadDataToTable();
    }

    public void filtrarPorConvenio(String idConvenio) {
        List<Postulacion> filtradas = todasLasPostulaciones.stream()
                .filter(p -> p.getConvenioSeleccionado().getId().equals(idConvenio))
                .collect(Collectors.toList());
        loadSpecificData(filtradas);
        title.setText("POSTULACIONES PARA CONVENIO: " + idConvenio);
        chkFiltro.setSelected(false);
    }

    private void loadDataToTable() {
        List<Postulacion> dataToShow = todasLasPostulaciones.stream()
                .filter(p -> !chkFiltro.isSelected() || p.getEstado() == EstadoPostulacion.PENDIENTE)
                .collect(Collectors.toList());
        loadSpecificData(dataToShow);
    }

    private void loadSpecificData(List<Postulacion> data) {
        model.setRowCount(0);
        for (Postulacion p : data) {
            Estudiante est = gestor.getServicioConsulta()
                    .buscarEstudiantePorRut(p.getRutEstudiante())
                    .orElse(null);
            model.addRow(new Object[]{
                    p.getId(),
                    p.getConvenioSeleccionado().getUniversidad(),
                    p.getConvenioSeleccionado().getPais(), // CAMBIO: Se añade el país.
                    est != null ? est.getNombreCompleto() : "N/A",
                    p.getConvenioSeleccionado().getArea(),
                    p.getFechaPostulacion().toString(),
                    p.getEstado().name(),
                    p
            });
        }
    }

    private void openDetalle(Postulacion p) {
        if (p == null) return;

        // 1. Crea el diálogo que contendrá nuestro panel de detalles.
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Detalle de Postulación", Dialog.ModalityType.APPLICATION_MODAL);

        // 2. Crea el panel de detalles, pasándole el usuario correcto (estudiante o funcionario).
        DetallePostulacionPanel detallePanel = new DetallePostulacionPanel(gestor, p, funcionario, dialog); // this.usuario se refiere al atributo de la clase

        // 3. Configura y muestra el diálogo.
        dialog.setContentPane(detallePanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        // Al cerrar el diálogo, refrescamos la tabla por si hubo cambios.
        refreshTodasLasPostulaciones();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        title = new JLabel("GESTIÓN DE POSTULACIONES", SwingConstants.CENTER);
        title.putClientProperty(FlatClientProperties.STYLE, "font:bold +4");

        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchField = new JTextField();
        searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Buscar...");
        chkFiltro = new JCheckBox("Solo Pendientes", true);

        searchPanel.add(chkFiltro, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(searchPanel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Tabla
        String[] cols = {"ID", "Universidad", "País", "Estudiante", "Área", "Fecha", "Estado", "_OBJ"};
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
        JButton btnDetalle = new JButton("Ver / Gestionar Detalle");
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.add(btnDetalle);
        add(footerPanel, BorderLayout.SOUTH);

        // Listeners
        chkFiltro.addActionListener(e -> loadDataToTable());
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        btnDetalle.addActionListener(e -> {
            int viewRow = table.getSelectedRow();
            if (viewRow >= 0) {
                int modelRow = table.convertRowIndexToModel(viewRow);
                Postulacion p = (Postulacion) model.getValueAt(modelRow, 7);
                openDetalle(p);
            }
        });

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
                    Postulacion p = (Postulacion) model.getValueAt(modelRow, 7);
                    openDetalle(p);
                }
            }
        });
    }

    private void applyFilter() {
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchField.getText()));
    }
}