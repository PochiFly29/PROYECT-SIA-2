package ui;

import com.formdev.flatlaf.FlatClientProperties;
import gestores.GestorIntercambio;
import modelo.Convenio;
import modelo.Estudiante;
import modelo.Programa;
import modelo.Usuario;

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

public class PostularPanel extends JPanel {

    private final GestorIntercambio gestor;
    private Usuario usuario;

    private final ConveniosTableModel model = new ConveniosTableModel();
    private JTable table;
    private TableRowSorter<ConveniosTableModel> sorter;
    private JTextField searchField;
    private JLabel titulo;
    private JButton btnVerDetalle;

    private Consumer<String> onVerPostulacionesPorConvenio;

    private static final int COL_ID   = 0;
    private static final int COL_AREA = 3;

    public PostularPanel(GestorIntercambio gestor, Usuario usuario) {
        this.gestor = Objects.requireNonNull(gestor);
        this.usuario = usuario;
        init();
        loadData();
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        loadData();
        revalidate();
        repaint();
    }

    public void setOnVerPostulacionesPorConvenio(Consumer<String> onVerPostulacionesPorConvenio) {
        this.onVerPostulacionesPorConvenio = onVerPostulacionesPorConvenio;
    }

    private void init() {
        setLayout(new BorderLayout());

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));
        header.setOpaque(false);

        titulo = new JLabel("LISTA DE CONVENIOS", SwingConstants.CENTER);
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
        sorter.setComparator(COL_ID, Comparator.naturalOrder());
        table.setRowSorter(sorter);

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    verDetalleSeleccionado();
                }
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== Footer =====
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        footer.setOpaque(false);
        btnVerDetalle = new JButton("Ver detalle");
        btnVerDetalle.addActionListener(e -> verDetalleSeleccionado());
        footer.add(btnVerDetalle);

        add(footer, BorderLayout.SOUTH);
    }

    private void applyFilter() {
        String txt = searchField.getText().trim();
        if (txt.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(txt)));
    }

    private void loadData() {
        List<Programa> programas = gestor.getProgramasVigentes();
        if (programas == null || programas.isEmpty()) {
            model.setData(List.of());
            titulo.setText("NO HAY PROGRAMAS VIGENTES");
            btnVerDetalle.setEnabled(false);
            return;
        }

        Programa p = programas.get(0);

        if (usuario != null && usuario.getRol() != null && "ESTUDIANTE".equals(usuario.getRol().name())) {
            titulo.setText("POSTULAR • " + p.getNombre());
        } else {
            titulo.setText("CATÁLOGO • " + p.getNombre());
        }

        List<Convenio> convenios = new ArrayList<>(p.getConvenios());
        convenios.sort(Comparator.comparing(Convenio::getId));
        model.setData(convenios);

        btnVerDetalle.setEnabled(!convenios.isEmpty());
    }

    private void verDetalleSeleccionado() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, "Seleccione un convenio.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Convenio c = model.getAt(modelRow);

        JPanel info = new JPanel(new GridBagLayout());
        info.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0; gc.anchor = GridBagConstraints.WEST; gc.insets = new Insets(4,4,4,4);

        JLabel lblID = new JLabel("ID: " + c.getId());
        lblID.setFont(lblID.getFont().deriveFont(14f));
        info.add(lblID, gc);

        gc.gridy++;
        JLabel lblUni = new JLabel("Universidad: " + c.getUniversidad());
        lblUni.setFont(lblUni.getFont().deriveFont(14f));
        info.add(lblUni, gc);

        gc.gridy++;
        JLabel lblPais = new JLabel("País: " + c.getPais());
        lblPais.setFont(lblPais.getFont().deriveFont(14f));
        info.add(lblPais, gc);

        gc.gridy++;
        JLabel lblArea = new JLabel("Área: " + (c.getArea() != null ? c.getArea() : "-"));
        lblArea.setFont(lblArea.getFont().deriveFont(14f));
        info.add(lblArea, gc);

        gc.gridy++;
        JLabel lblReqAcad = new JLabel("Requisitos Académicos: " + c.getRequisitosAcademicos());
        lblReqAcad.setFont(lblReqAcad.getFont().deriveFont(14f));
        info.add(lblReqAcad, gc);

        gc.gridy++;
        JLabel lblReqEco = new JLabel("Requisitos Económicos: " + c.getRequisitosEconomicos());
        lblReqEco.setFont(lblReqEco.getFont().deriveFont(14f));
        info.add(lblReqEco, gc);

        // Acciones segun rol
        if (usuario instanceof Estudiante) {
            Object[] options = {"Postular", "Cerrar"};
            int opt = JOptionPane.showOptionDialog(this, info, "Detalle de convenio",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, options, options[0]
            );
            if (opt == 0) {
                boolean ok = gestor.postular((Estudiante) usuario, c);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "¡Postulación exitosa!", "Postulación", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(this, "No se pudo postular.\nPuede que ya tengas una postulación activa para este convenio.",
                            "Postulación", JOptionPane.WARNING_MESSAGE);
                }
            }
        } else {
            Object[] options = {(onVerPostulacionesPorConvenio != null ? "Ver postulaciones del convenio" : "Cerrar"),"Cerrar"};
            int opt = JOptionPane.showOptionDialog(this, info, "Detalle de convenio",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, options, options[0]
            );

            if (onVerPostulacionesPorConvenio != null && opt == 0) {
                onVerPostulacionesPorConvenio.accept(c.getId());
            }
        }
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
