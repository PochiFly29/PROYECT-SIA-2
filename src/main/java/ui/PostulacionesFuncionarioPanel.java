package ui;

import com.formdev.flatlaf.FlatClientProperties;
import enums.EstadoPostulacion;
import gestores.GestorIntercambio;
import modelo.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PostulacionesFuncionarioPanel extends JPanel {

    private final GestorIntercambio gestor;
    private final Usuario funcionario;
    private List<Postulacion> todasLasPostulaciones = Collections.emptyList();

    // --- UI unificada con PostularPanel ---
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;
    private JCheckBox chkFiltro;
    private JLabel title;

    private static final int COL_ID   = 0;
    private static final int COL_OBJ  = 7; // columna oculta con el objeto Postulacion

    public PostulacionesFuncionarioPanel(GestorIntercambio gestor, Usuario funcionario) {
        this.gestor = Objects.requireNonNull(gestor);
        this.funcionario = funcionario;
        initUI();
        refreshTodasLasPostulaciones();
    }

    public void refreshTodasLasPostulaciones() {
        this.todasLasPostulaciones = gestor.getServicioConsulta().getProgramaActivo()
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
                    p.getConvenioSeleccionado().getPais(),
                    est != null ? est.getNombreCompleto() : "N/A",
                    p.getConvenioSeleccionado().getArea(),
                    p.getFechaPostulacion().toString(),
                    p.getEstado().name(),
                    p // _OBJ
            });
        }
    }

    private void openDetalle(Postulacion p) {
        if (p == null) return;

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Detalle de Postulación", Dialog.ModalityType.APPLICATION_MODAL);
        DetallePostulacionPanel detallePanel = new DetallePostulacionPanel(gestor, p, funcionario, dialog);
        dialog.setContentPane(detallePanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        // Al cerrar el diálogo, refrescamos
        refreshTodasLasPostulaciones();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // ===== Header (igual que PostularPanel) =====
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));
        header.setOpaque(false);

        title = new JLabel("GESTIÓN DE POSTULACIONES", SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.putClientProperty(FlatClientProperties.STYLE, "font:bold +6");
        header.add(title);

        header.add(Box.createVerticalStrut(10));

        JPanel searchRow = new JPanel(new BorderLayout(10, 0));
        searchRow.setOpaque(false);
        // mismo margen lateral del buscador que en PostularPanel
        searchRow.setBorder(BorderFactory.createEmptyBorder(0, 240, 0, 240));

        chkFiltro = new JCheckBox("Solo Pendientes", true);
        searchRow.add(chkFiltro, BorderLayout.WEST);

        searchField = new JTextField();
        searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Buscar");
        searchField.putClientProperty(FlatClientProperties.STYLE, "arc:999; margin:6,14,6,14");
        searchRow.add(searchField, BorderLayout.CENTER);

        header.add(searchRow);
        add(header, BorderLayout.NORTH);

        // ===== Tabla (tipografías/alturas/selección como PostularPanel) =====
        String[] cols = {"ID", "Universidad", "País", "Estudiante", "Área", "Fecha", "Estado", "_OBJ"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(model);
        table.setFont(table.getFont().deriveFont(14f));
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 14f));
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFocusable(false);

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        TableColumn idCol = table.getColumnModel().getColumn(COL_ID);
        idCol.setPreferredWidth(60);
        idCol.setMaxWidth(60);

        // Ocultar columna de objeto
        TableColumn objCol = table.getColumnModel().getColumn(COL_OBJ);
        objCol.setMinWidth(0);
        objCol.setMaxWidth(0);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== Footer (alineado a la derecha, como PostularPanel) =====
        JButton btnDetalle = new JButton("Ver / Gestionar Detalle");
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        footerPanel.setOpaque(false);
        footerPanel.add(btnDetalle);
        add(footerPanel, BorderLayout.SOUTH);

        // ===== Listeners (mantiene la lógica) =====
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
                Postulacion p = (Postulacion) model.getValueAt(modelRow, COL_OBJ);
                openDetalle(p);
            }
        });

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
                    Postulacion p = (Postulacion) model.getValueAt(modelRow, COL_OBJ);
                    openDetalle(p);
                }
            }
        });
    }

    private void applyFilter() {
        String txt = searchField.getText().trim();
        if (txt.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(txt)));
        }
    }
}
