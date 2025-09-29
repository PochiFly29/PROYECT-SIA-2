package ui;

import com.formdev.flatlaf.FlatClientProperties;
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

/**
 * **Panel de Interfaz de Usuario para la Visualización de Postulaciones (Rol Estudiante).**
 * <p>Muestra un listado de **todas las {@link Postulacion}es** creadas por el
 * {@link Estudiante} actualmente logueado. Permite al estudiante ver el estado de
 * sus solicitudes y acceder al detalle para adjuntar documentos (CV, Notas, etc.).</p>
 */
public class PostulacionesPanel extends JPanel {

    private final GestorIntercambio gestor;
    private final Estudiante estudiante;

    // --- Estilo unificado con PostularPanel ---
    private DefaultTableModel model;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;
    private JLabel titulo;
    private JButton btnDetalle;

    // CAMBIO: El constructor ahora espera un Estudiante, no un Usuario genérico.
    /**
     * Crea e inicializa el panel de postulaciones del estudiante.
     * @param gestor El gestor central de la aplicación.
     * @param estudiante El estudiante cuyos datos se consultarán.
     */
    private static final int COL_ID    = 0;
    private static final int COL_OBJET = 6; // columna oculta _OBJ

    public PostulacionesPanel(GestorIntercambio gestor, Estudiante estudiante) {
        this.gestor = Objects.requireNonNull(gestor);
        this.estudiante = estudiante;
        initUI();     // armamos la UI una sola vez con el mismo estilo que PostularPanel
        refresh();    // y luego cargamos datos
    }

    public void refresh() {
        // Obtiene las postulaciones del programa activo para el RUT del estudiante.
        Programa programa = gestor.getServicioConsulta().getProgramaActivo().orElse(null);
        List<Postulacion> postulaciones;

        if (programa != null) {
            postulaciones = gestor.getServicioConsulta()
                    .getPostulacionesFiltradas(programa, "rut", estudiante.getRut());
        } else {
            postulaciones = Collections.emptyList();
        }

        // Carga/limpia la tabla manteniendo la UI (mismo comportamiento visual que PostularPanel)
        model.setRowCount(0);
        if (postulaciones.isEmpty()) {
            titulo.setText("AÚN NO TIENES POSTULACIONES REGISTRADAS");
            btnDetalle.setEnabled(false);
        } else {
            for (Postulacion p : postulaciones) {
                Convenio c = p.getConvenioSeleccionado();
                model.addRow(new Object[]{
                        p.getId(),
                        c.getUniversidad(),
                        c.getPais(),
                        c.getArea(),
                        p.getFechaPostulacion().toString(),
                        p.getEstado().name(),
                        p // _OBJ
                });
            }
            titulo.setText("MIS POSTULACIONES (" + postulaciones.size() + ")");
            btnDetalle.setEnabled(true);
        }

        revalidate();
        repaint();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // -------- Header (igual que PostularPanel) --------
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));
        header.setOpaque(false);

        titulo = new JLabel("MIS POSTULACIONES", SwingConstants.CENTER);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        titulo.putClientProperty(FlatClientProperties.STYLE, "font:bold +6");
        header.add(titulo);

        header.add(Box.createVerticalStrut(10));

        JPanel searchRow = new JPanel(new BorderLayout());
        searchRow.setOpaque(false);
        // mismo padding lateral que en PostularPanel
        searchRow.setBorder(BorderFactory.createEmptyBorder(0, 240, 0, 240));

        searchField = new JTextField();
        searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Buscar");
        searchField.putClientProperty(FlatClientProperties.STYLE, "arc:999; margin:6,14,6,14");
        searchRow.add(searchField, BorderLayout.CENTER);

        header.add(searchRow);
        add(header, BorderLayout.NORTH);

        // -------- Tabla (misma tipografía/altura/selección) --------
        String[] cols = {"ID", "Universidad", "País", "Área", "Fecha", "Estado", "_OBJ"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        table.setFont(table.getFont().deriveFont(14f));
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 14f));
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFocusable(false);

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // ID angosto (como en PostularPanel)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        TableColumn idCol = table.getColumnModel().getColumn(COL_ID);
        idCol.setPreferredWidth(60);
        idCol.setMaxWidth(60);

        // Ocultamos la columna de objeto
        TableColumn objCol = table.getColumnModel().getColumn(COL_OBJET);
        objCol.setMinWidth(0);
        objCol.setMaxWidth(0);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // -------- Footer (alineado a la derecha, como PostularPanel) --------
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        footer.setOpaque(false);
        btnDetalle = new JButton("Ver Detalle / Adjuntar Documentos");
        footer.add(btnDetalle);
        add(footer, BorderLayout.SOUTH);

        // -------- Listeners (misma UX que PostularPanel) --------
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        btnDetalle.addActionListener(e -> openDetalleSeleccionado());

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    openDetalleSeleccionado();
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

    private void openDetalleSeleccionado() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, "Seleccione una postulación.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Postulacion p = (Postulacion) model.getValueAt(modelRow, COL_OBJET);
        openDetalle(p);
    }

    private void openDetalle(Postulacion p) {
        if (p == null) return;

        // Diálogo modal con el mismo flujo que ya tenías
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Detalle de Postulación", Dialog.ModalityType.APPLICATION_MODAL);
        DetallePostulacionPanel detallePanel = new DetallePostulacionPanel(gestor, p, estudiante, dialog);
        dialog.setContentPane(detallePanel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        // Al cerrar, refrescamos (sin cambiar lógica)
        refresh();
    }
}