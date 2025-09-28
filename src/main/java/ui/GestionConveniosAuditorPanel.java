package ui;

import com.formdev.flatlaf.FlatClientProperties;
import gestores.GestorIntercambio;
import modelo.Convenio;
import modelo.Usuario;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * **Panel de Interfaz de Usuario para la Gestión Administrativa de Convenios.**
 * <p>Este panel implementa las operaciones **CRUD (Crear, Consultar, Eliminar)** * para el rol de Auditor, permitiendo la administración directa del catálogo de
 * {@link Convenio}s. La funcionalidad de "Eliminar" está acoplada a la lógica
 * de negocio que rechaza las postulaciones asociadas.</p>
 */
public class GestionConveniosAuditorPanel extends JPanel {
    private final GestorIntercambio gestor;
    private final Usuario auditor;
    private final ConveniosAuditorTableModel model;
    private final TableRowSorter<ConveniosAuditorTableModel> sorter;
    private JTextField searchField;
    private JTable table;

    /**
     * Crea e inicializa el panel de gestión de convenios.
     * @param gestor El gestor central de la aplicación.
     * @param auditor El usuario Auditor autenticado.
     */
    public GestionConveniosAuditorPanel(GestorIntercambio gestor, Usuario auditor) {
        this.gestor = gestor;
        this.auditor = auditor;
        this.model = new ConveniosAuditorTableModel();
        this.sorter = new TableRowSorter<>(model);
        init();
        refresh();
    }

    public void refresh() {
        List<Convenio> convenios = gestor.getServicioConsulta().getTodosLosConvenios();
        model.setData(convenios);
    }

    private void init() {
        setLayout(new BorderLayout());

        // ===== Header (alineado con PostularPanel) =====
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));
        header.setOpaque(false);

        JLabel title = new JLabel("GESTIÓN DE CONVENIOS", SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.putClientProperty(FlatClientProperties.STYLE, "font:bold +6");
        header.add(title);

        header.add(Box.createVerticalStrut(10));

        JPanel searchRow = new JPanel(new BorderLayout());
        searchRow.setOpaque(false);
        searchRow.setBorder(BorderFactory.createEmptyBorder(0, 240, 0, 240));

        searchField = new JTextField();
        searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Buscar por ID, Universidad, País o Área...");
        searchField.putClientProperty(FlatClientProperties.STYLE, "arc:999; margin:6,14,6,14");
        searchRow.add(searchField, BorderLayout.CENTER);

        header.add(searchRow);
        add(header, BorderLayout.NORTH);

        // ===== Tabla (mismo estilo que PostularPanel) =====
        table = new JTable(model);
        table.setFont(table.getFont().deriveFont(14f));
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 14f));
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFocusable(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setRowSorter(sorter);

        // Columna ID más angosta
        TableColumn idCol = table.getColumnModel().getColumn(0);
        idCol.setPreferredWidth(60);
        idCol.setMaxWidth(60);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== Footer (alineado a la derecha) =====
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        actionsPanel.setOpaque(false);
        JButton btnCrear = new JButton("Crear Nuevo Convenio");
        JButton btnEliminar = new JButton("Eliminar Seleccionado");
        actionsPanel.add(btnCrear);
        actionsPanel.add(btnEliminar);
        add(actionsPanel, BorderLayout.SOUTH);

        // ===== Listeners (misma lógica) =====
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        btnCrear.addActionListener(e -> crearConvenio());
        btnEliminar.addActionListener(e -> eliminarConvenio());
    }

    private void applyFilter() {
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(searchField.getText())));
    }

    private void crearConvenio() {
        JTextField idField = new JTextField();
        JTextField uniField = new JTextField();
        JTextField paisField = new JTextField();
        JTextField areaField = new JTextField();
        JTextField reqAcadField = new JTextField();
        JTextField reqEconField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.add(new JLabel("ID Convenio (Ej: C061):"));
        panel.add(idField);
        panel.add(new JLabel("Universidad:"));
        panel.add(uniField);
        panel.add(new JLabel("País:"));
        panel.add(paisField);
        panel.add(new JLabel("Área de Estudio:"));
        panel.add(areaField);
        panel.add(new JLabel("Requisitos Académicos:"));
        panel.add(reqAcadField);
        panel.add(new JLabel("Requisitos Económicos:"));
        panel.add(reqEconField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Crear Nuevo Convenio",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                Convenio nuevo = new Convenio(
                        idField.getText(), uniField.getText(), paisField.getText(),
                        areaField.getText(), reqAcadField.getText(), reqEconField.getText()
                );
                gestor.getServicioConvenio().crearConvenio(nuevo);
                JOptionPane.showMessageDialog(this, "Convenio creado exitosamente.");
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al crear el convenio: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void eliminarConvenio() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un convenio de la tabla para eliminar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);
        Convenio convenio = model.getConvenioAt(modelRow);

        int r = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de que desea eliminar el convenio con " + convenio.getUniversidad() + "?\n" +
                        "Cualquier postulación pendiente asociada será rechazada.",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (r == JOptionPane.YES_OPTION) {
            try {
                gestor.getServicioConvenio().eliminarConvenio(convenio, auditor);
                JOptionPane.showMessageDialog(this, "Convenio eliminado exitosamente.");
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar el convenio: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- Modelo de Tabla (sin cambios de lógica) ---
    private static class ConveniosAuditorTableModel extends AbstractTableModel {
        private final String[] cols = {"ID", "Universidad", "País", "Área", "Requisitos Académicos"};
        private List<Convenio> data = new ArrayList<>();

        public void setData(List<Convenio> data) {
            this.data = data != null ? data : new ArrayList<>();
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int column) { return cols[column]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Convenio c = data.get(rowIndex);
            switch (columnIndex) {
                case 0: return c.getId();
                case 1: return c.getUniversidad();
                case 2: return c.getPais();
                case 3: return c.getArea();
                case 4: return c.getRequisitosAcademicos();
                default: return null;
            }
        }

        @Override public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }

        public Convenio getConvenioAt(int rowIndex) { return data.get(rowIndex); }
    }
}
