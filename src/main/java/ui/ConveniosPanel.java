package ui;

import com.formdev.flatlaf.FlatClientProperties;
import gestores.GestorIntercambio;
import modelo.Convenio;
import modelo.Programa;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * **Panel de Interfaz de Usuario para la Visualización y Gestión del Catálogo de Convenios.**
 * <p>Muestra una tabla interactiva de todos los {@link Convenio}s vigentes.
 * Incluye funcionalidad de búsqueda, filtrado y una vista de detalle modal.</p>
 * <p>Esta clase es **reutilizable**: su comportamiento final (botón de acción y modal)
 * se define por el callback {@code onConvenioSelected}, permitiendo que sirva como
 * simple visor (para Estudiantes/Auditores) o como selector (para Funcionarios).</p>
 */
public class ConveniosPanel extends JPanel {

    private final GestorIntercambio gestor;
    private final Consumer<String> onConvenioSelected;

    private final ConveniosTableModel model = new ConveniosTableModel();
    private JTable table;
    private TableRowSorter<ConveniosTableModel> sorter;
    private JTextField searchField;
    private JLabel titulo;
    private JButton btnDetalleAccion;

    private static final int COL_ID = 0;

    /**
     * Crea e inicializa el panel de convenios.
     * @param gestor El gestor central de la aplicación.
     * @param onConvenioSelected Callback que recibe el ID del convenio seleccionado, o {@code null} si es solo un visor.
     */
    public ConveniosPanel(GestorIntercambio gestor, Consumer<String> onConvenioSelected) {
        this.gestor = Objects.requireNonNull(gestor);
        this.onConvenioSelected = onConvenioSelected;
        init();
        loadData();
    }

    public void refresh() {
        loadData();
    }

    private void init() {
        setLayout(new BorderLayout());

        // Header y Título
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));
        header.setOpaque(false);

        titulo = new JLabel("CATÁLOGO DE CONVENIOS VIGENTES", SwingConstants.CENTER);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        titulo.putClientProperty(FlatClientProperties.STYLE, "font:bold +6");
        header.add(titulo);

        header.add(Box.createVerticalStrut(10));

        JPanel searchRow = new JPanel(new BorderLayout());
        searchRow.setOpaque(false);
        searchRow.setBorder(BorderFactory.createEmptyBorder(0, 240, 0, 240));

        searchField = new JTextField();
        searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Buscar");
        searchField.putClientProperty(FlatClientProperties.STYLE, "arc:999; margin:6,14,6,14");
        searchRow.add(searchField, BorderLayout.CENTER);
        header.add(searchRow);

        add(header, BorderLayout.NORTH);

        // Configuración de la Tabla
        table = new JTable(model);
        table.setFont(table.getFont().deriveFont(14f));
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 14f));
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFocusable(false);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        TableColumn idColumn = table.getColumnModel().getColumn(COL_ID);
        idColumn.setPreferredWidth(60);
        idColumn.setMaxWidth(60);

        sorter = new TableRowSorter<>(model);
        sorter.setComparator(0, Comparator.naturalOrder());
        table.setRowSorter(sorter);

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    executeConvenioAction();
                }
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });


        add(new JScrollPane(table), BorderLayout.CENTER);

        // Footer con el botón de acción
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        footer.setOpaque(false);

        String btnText = (onConvenioSelected != null) ? "Buscar Postulaciones Asociadas" : "Ver Detalle";
        btnDetalleAccion = new JButton(btnText);
        btnDetalleAccion.addActionListener(e -> executeConvenioAction());

        footer.add(btnDetalleAccion);

        add(footer, BorderLayout.SOUTH);
    }

    // Método para mostrar el detalle del convenio
    private JPanel buildDetallePanel(Convenio c) {
        JPanel info = new JPanel(new GridBagLayout());
        info.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 🚨 CORRECCIÓN: Declaramos GridBagConstraints dentro del método
        final GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0; gc.anchor = GridBagConstraints.WEST; gc.insets = new Insets(4,4,4,4);

        // Función auxiliar para añadir etiquetas
        // 🚨 CORRECCIÓN: La lambda ya no necesita ser 'final', pero sigue modificando
        // una variable que no es local a su propio ámbito.
        // Para ser totalmente compatible y evitar errores de 'effectively final' en todas las versiones de Java,
        // la implementación más limpia es usar la propia variable gc:

        Consumer<String> addLabel = (text) -> {
            JLabel lbl = new JLabel(text);
            lbl.setFont(lbl.getFont().deriveFont(14f));
            info.add(lbl, gc);
            // El problema está aquí: gc.gridy++ es una modificación a una variable externa.
            // La solución más simple para un código limpio es eliminar la lambda y usar la variable directamente.
        };

        // 🚨 SOLUCIÓN: Reemplazamos la lambda por el código directo para evitar el error 'effectively final'.
        // Usamos la variable 'gc' declarada en este método.

        // Fila 0
        JLabel lblID = new JLabel("ID: " + c.getId());
        lblID.setFont(lblID.getFont().deriveFont(14f));
        info.add(lblID, gc);

        // Fila 1
        gc.gridy++;
        JLabel lblUni = new JLabel("Universidad: " + c.getUniversidad());
        lblUni.setFont(lblUni.getFont().deriveFont(14f));
        info.add(lblUni, gc);

        // Fila 2
        gc.gridy++;
        JLabel lblPais = new JLabel("País: " + c.getPais());
        lblPais.setFont(lblPais.getFont().deriveFont(14f));
        info.add(lblPais, gc);

        // Fila 3
        gc.gridy++;
        JLabel lblArea = new JLabel("Área: " + (c.getArea() != null ? c.getArea() : "-"));
        lblArea.setFont(lblArea.getFont().deriveFont(14f));
        info.add(lblArea, gc);

        // Fila 4
        gc.gridy++;
        JLabel lblReqAcad = new JLabel("Requisitos Académicos: " + c.getRequisitosAcademicos());
        lblReqAcad.setFont(lblReqAcad.getFont().deriveFont(14f));
        info.add(lblReqAcad, gc);

        // Fila 5
        gc.gridy++;
        JLabel lblReqEco = new JLabel("Requisitos Económicos: " + c.getRequisitosEconomicos());
        lblReqEco.setFont(lblReqEco.getFont().deriveFont(14f));
        info.add(lblReqEco, gc);

        return info;
    }

    // Método para ejecutar la acción de la selección
    private void executeConvenioAction() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un convenio.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Convenio c = model.getAt(modelRow);

        // 1. Mostrar el detalle
        Object[] options = (onConvenioSelected != null) ? new Object[]{"Seleccionar", "Cerrar"} : new Object[]{"Cerrar"};

        int opt = JOptionPane.showOptionDialog(this, buildDetallePanel(c), "Detalle de convenio",
                (onConvenioSelected != null) ? JOptionPane.YES_NO_OPTION : JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]
        );

        // 2. Ejecutar el callback (Solo si el rol es Funcionario y selecciona "Seleccionar")
        if (onConvenioSelected != null && opt == 0) {
            onConvenioSelected.accept(c.getId());
        }
    }

    private void applyFilter() {
        String txt = searchField.getText().trim();
        if (txt.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(txt)));
    }

    public void loadData() {
        List<Convenio> todosConvenios = gestor.getServicioConsulta().getTodosLosConvenios();

        todosConvenios.sort(Comparator.comparing(Convenio::getId));
        model.setData(todosConvenios);

        titulo.setText("CATÁLOGO DE CONVENIOS VIGENTES (" + todosConvenios.size() + ")");
        btnDetalleAccion.setEnabled(!todosConvenios.isEmpty());
    }

    private static class ConveniosTableModel extends AbstractTableModel {
        private final String[] cols = {"ID", "Universidad", "País", "Área", "Requisitos Académicos"};
        private List<Convenio> data = new ArrayList<>();

        public void setData(List<Convenio> d) {
            this.data = (d == null) ? new ArrayList<>() : d;
            fireTableDataChanged();
        }

        public Convenio getAt(int row) { return data.get(row); }
        public int getRowCount() { return data.size(); }
        public int getColumnCount() { return cols.length; }
        public String getColumnName(int column) { return cols[column]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Convenio c = data.get(rowIndex);
            switch (columnIndex) {
                case 0: return c.getId();
                case 1: return c.getUniversidad();
                case 2: return c.getPais();
                case 3: return c.getArea();
                case 4: return c.getRequisitosAcademicos();
                default: return "";
            }
        }
        public Class<?> getColumnClass(int columnIndex) { return String.class; }
        public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }
    }
}