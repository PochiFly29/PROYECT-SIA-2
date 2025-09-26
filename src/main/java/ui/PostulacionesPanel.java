package ui;

import com.formdev.flatlaf.FlatClientProperties;
import enums.EstadoPostulacion;
import enums.Rol;
import gestores.GestorIntercambio;
import modelo.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Panel de listado de postulaciones.
 * - Estudiante: muestra sus propias postulaciones (filtro por RUT).
 * - Funcionario: por defecto muestra POR_REVISAR; puede filtrar por convenio desde PostularPanel.
 */
public class PostulacionesPanel extends JPanel {

    private final GestorIntercambio gestor;
    private Usuario usuario;

    private JLabel title;
    private JTextField search;
    private JComboBox<Object> cbEstado; // [Todos | EstadoPostulacion...]
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private JButton btnDetalle;

    private final CardLayout cards = new CardLayout();
    private JPanel centerCards;

    private static final int COL_ID = 0;
    private static final int COL_UNI = 1;
    private static final int COL_PAIS = 2;
    private static final int COL_EMITIDA = 3;
    private static final int COL_VIGENCIA = 4;
    private static final int COL_ESTADO = 5;
    private static final int COL_OBJ = 6;

    private String convenioFilterId = null;

    public PostulacionesPanel(GestorIntercambio gestor, Usuario usuario) {
        this.gestor = Objects.requireNonNull(gestor);
        this.usuario = usuario;
        initUI();
        refresh();
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setFiltroConvenio(String convenioId) {
        this.convenioFilterId = (convenioId == null || convenioId.isBlank()) ? null : convenioId.trim();
        refresh();
    }

    public void refresh() {
        List<Postulacion> fuente;

        if (usuario != null && usuario.getRol() == Rol.ESTUDIANTE) {
            fuente = gestor.getPostulaciones("rut", usuario.getRut());
            setTitulo("MIS POSTULACIONES");
        } else if (usuario != null && usuario.getRol() == Rol.FUNCIONARIO) {
            if (convenioFilterId != null) {
                fuente = gestor.getPostulaciones("convenio", convenioFilterId);
                setTitulo("POSTULACIONES • CONVENIO " + convenioFilterId);
            } else {
                fuente = gestor.getPostulaciones("estado", EstadoPostulacion.POR_REVISAR.name());
                setTitulo("POSTULACIONES PENDIENTES");
            }
        } else {
            fuente = gestor.getPostulaciones("estado", EstadoPostulacion.POR_REVISAR.name());
            setTitulo("POSTULACIONES");
        }

        model.setRowCount(0);

        if (fuente != null) {
            for (Postulacion p : fuente) {
                Convenio conv = p.getConvenioSeleccionado();

                String id = safe(p.getId());
                String uni = (conv != null) ? safe(conv.getUniversidad()) : "-";
                String pais = (conv != null) ? safe(conv.getPais()) : "-";
                String emitida = (p.getFechaPostulacion() != null) ? p.getFechaPostulacion().toString() : "-";

                Programa prog = (conv != null) ? gestor.getProgramaDeConvenio(conv) : null;
                String vigencia = vigenciaTexto(prog);

                String estado = (p.getEstado() != null) ? p.getEstado().name() : "-";

                model.addRow(new Object[]{ id, uni, pais, emitida, vigencia, estado, p });
            }
        }

        boolean hayDatos = model.getRowCount() > 0;
        cards.show(centerCards, hayDatos ? "table" : "empty");

        applyFilters();
        revalidate();
        repaint();
    }

    private void initUI() {
        setOpaque(false);
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(16, 24, 8, 24));

        title = new JLabel("POSTULACIONES", SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.putClientProperty(FlatClientProperties.STYLE, "font:bold +6");
        header.add(title);

        header.add(Box.createVerticalStrut(10));

        JPanel filterRow = new JPanel(new GridBagLayout());
        filterRow.setOpaque(false);
        var gc = new GridBagConstraints();
        gc.insets = new Insets(0, 240, 0, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        search = new JTextField();
        search.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Buscar");
        search.putClientProperty(FlatClientProperties.STYLE, "arc:999; margin:6,14,6,14");
        filterRow.add(search, gc);

        gc.insets = new Insets(0, 6, 0, 240);
        gc.gridx = 1; gc.weightx = 0;
        cbEstado = new JComboBox<>();
        cbEstado.addItem("Todos");
        for (EstadoPostulacion e : EstadoPostulacion.values()) cbEstado.addItem(e);
        cbEstado.putClientProperty(FlatClientProperties.STYLE, "arc:999");
        cbEstado.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        filterRow.add(cbEstado, gc);

        header.add(filterRow);
        add(header, BorderLayout.NORTH);

        // Tabla
        String[] cols = { "ID", "UNIVERSIDAD", "PAÍS", "EMITIDA", "VIGENCIA", "ESTADO", "_POST_" };
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == COL_OBJ ? Postulacion.class : String.class;
            }
        };
        table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFocusable(false);

        sorter = new TableRowSorter<>(model);
        sorter.setComparator(COL_ID, Comparator.comparingInt(s -> {
            try {
                String t = String.valueOf(s);
                return Integer.parseInt(t.replaceAll("\\D+", ""));
            } catch (Exception ex) { return Integer.MAX_VALUE; }
        }));
        table.setRowSorter(sorter);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    openDetalle(selectedPostulacion());
                }
            }
        });

        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });
        cbEstado.addActionListener(e -> applyFilters());

        table.getColumnModel().getColumn(COL_OBJ).setMinWidth(0);
        table.getColumnModel().getColumn(COL_OBJ).setMaxWidth(0);
        table.getColumnModel().getColumn(COL_OBJ).setPreferredWidth(0);

        centerCards = new JPanel(cards);
        JScrollPane scroll = new JScrollPane(table);
        centerCards.add(scroll, "table");

        JPanel empty = new JPanel(new GridBagLayout());
        JLabel vacio = new JLabel("No se han encontrado postulaciones.");
        vacio.putClientProperty(FlatClientProperties.STYLE, "font:+3");
        empty.add(vacio, new GridBagConstraints());
        centerCards.add(empty, "empty");

        add(centerCards, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        footer.setOpaque(false);
        btnDetalle = new JButton("Ver detalle");
        btnDetalle.addActionListener(e -> {
            Postulacion p = selectedPostulacion();
            if (p != null) openDetalle(p);
        });
        footer.add(btnDetalle);
        add(footer, BorderLayout.SOUTH);
    }

    private void setTitulo(String t) {
        if (title != null) title.setText(t);
    }

    private void applyFilters() {
        if (sorter == null) return;

        String txt = (search.getText() == null) ? "" : search.getText().trim();
        Object estadoSel = cbEstado.getSelectedItem();

        RowFilter<DefaultTableModel, Integer> rfTexto = null;
        if (!txt.isEmpty()) {
            rfTexto = RowFilter.regexFilter("(?i)" + PatternUtil.quoteIfNeeded(txt));
        }

        RowFilter<DefaultTableModel, Integer> rfEstado = null;
        if (estadoSel instanceof EstadoPostulacion) {
            String needle = ((EstadoPostulacion) estadoSel).name();
            rfEstado = RowFilter.regexFilter("^" + PatternUtil.quoteIfNeeded(needle) + "$", COL_ESTADO);
        }

        if (rfTexto == null && rfEstado == null) {
            sorter.setRowFilter(null);
        } else if (rfTexto != null && rfEstado != null) {
            sorter.setRowFilter(RowFilter.andFilter(List.of(rfTexto, rfEstado)));
        } else {
            sorter.setRowFilter((rfTexto != null) ? rfTexto : rfEstado);
        }
    }

    private Postulacion selectedPostulacion() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        return (Postulacion) model.getValueAt(modelRow, COL_OBJ);
    }

    private void openDetalle(Postulacion p) {
        if (p == null) return;

        Convenio conv = p.getConvenioSeleccionado();
        Programa prog = (conv != null) ? gestor.getProgramaDeConvenio(conv) : null;

        JPanel info = new JPanel(new GridBagLayout());
        info.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(4, 4, 4, 4);

        info.add(new JLabel("ID: " + safe(p.getId())), gc);
        gc.gridy++;
        info.add(new JLabel("Universidad: " + (conv != null ? safe(conv.getUniversidad()) : "-")), gc);
        gc.gridy++;
        info.add(new JLabel("País: " + (conv != null ? safe(conv.getPais()) : "-")), gc);
        gc.gridy++;
        info.add(new JLabel("Plazo: " + vigenciaTexto(prog)), gc);
        gc.gridy++;
        info.add(new JLabel("Estado: " + (p.getEstado() != null ? p.getEstado().name() : "-")), gc);
        if (conv != null) {
            gc.gridy++;
            info.add(new JLabel("Requisitos Académicos: " + safe(conv.getRequisitosAcademicos())), gc);
            gc.gridy++;
            info.add(new JLabel("Requisitos Económicos: " + safe(conv.getRequisitosEconomicos())), gc);
        }
        JButton btnAdj = new JButton("📎 Adjuntar/Comentar");
        JButton btnInter = new JButton("Interacciones");
        JButton btnCerrar = new JButton("Cerrar");

        JOptionPane pane = new JOptionPane(
                info,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.DEFAULT_OPTION,
                null,
                new Object[]{},
                null
        );

        btnAdj.addActionListener(e -> pane.setValue(btnAdj));
        btnInter.addActionListener(e -> pane.setValue(btnInter));
        btnCerrar.addActionListener(e -> pane.setValue(btnCerrar));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actions.add(btnAdj);
        btnAdj.putClientProperty(FlatClientProperties.STYLE, "background:#2E86FF; foreground:#FFFFFF");
        actions.add(btnInter);
        btnInter.putClientProperty(FlatClientProperties.STYLE, "background:#2E86FF; foreground:#FFFFFF");
        actions.add(btnCerrar);

        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.add(info, BorderLayout.CENTER);
        content.add(actions, BorderLayout.SOUTH);

        pane.setMessage(content);

        JDialog dialog = pane.createDialog(this, "Detalle de postulación");
        dialog.setModal(true);
        dialog.setVisible(true);

        Object val = pane.getValue();
        if (val == btnAdj) {
            if (usuario.getRol() == Rol.ESTUDIANTE) {
                JTextField tfTitulo = new JTextField();
                tfTitulo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ej: PASAPORTE.pdf");
                int ok = JOptionPane.showConfirmDialog(this, tfTitulo,
                        "Título del documento", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (ok == JOptionPane.OK_OPTION && !tfTitulo.getText().trim().isEmpty()) {
                    Interaccion inter = Interaccion.ofDocumento(usuario, tfTitulo.getText().trim());
                    gestor.agregarInteraccionAPostulacion(p.getId(), inter);
                    if (p.getEstado() != EstadoPostulacion.POR_REVISAR) {
                        gestor.actualizarEstadoPostulacion(p.getId(), EstadoPostulacion.POR_REVISAR);
                    }
                    info("Documento agregado.");
                    refresh();
                }
            } else if (usuario.getRol() == Rol.FUNCIONARIO) {
                JTextArea ta = new JTextArea(5, 30);
                int ok = JOptionPane.showConfirmDialog(this, new JScrollPane(ta),
                        "Comentario", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (ok == JOptionPane.OK_OPTION && !ta.getText().trim().isEmpty()) {
                    Interaccion inter = Interaccion.ofComentario(usuario, ta.getText().trim());
                    gestor.agregarInteraccionAPostulacion(p.getId(), inter);
                    gestor.actualizarEstadoPostulacion(p.getId(), EstadoPostulacion.REVISADA);
                    info("Comentario agregado.");
                    refresh();
                }
            }
        } else if (val == btnInter) {
            verHistorial(p);
        }
    }

    private void verHistorial(Postulacion p) {
        java.util.List<Interaccion> xs = p.getInteracciones();
        if (xs == null || xs.isEmpty()) {
            info("No hay interacciones registradas.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (Interaccion i : xs) {
            sb.append(i.getFechaHora().toLocalDate()).append(" | ").append(i.getAutor().getNombreCompleto()).append(" | ").append(i.getTipo().name()).append(" | ").append(i.getTitulo()).append("\n");
        }

        JTextArea ta = new JTextArea(sb.toString(), 12, 60);
        ta.setEditable(false);
        JOptionPane.showMessageDialog(
                this,
                new JScrollPane(ta),
                "Historial de Interacciones",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    private static String safe(String s) { return (s == null || s.trim().isEmpty()) ? "-" : s.trim(); }

    private static String vigenciaTexto(Programa prog) {
        if (prog != null && prog.getFechaInicio() != null && prog.getFechaFin() != null)
            return prog.getFechaInicio() + " a " + prog.getFechaFin();
        return "-";
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Información", JOptionPane.INFORMATION_MESSAGE);
    }

    private static class PatternUtil {
        static String quoteIfNeeded(String s) {
            StringBuilder out = new StringBuilder();
            for (char ch : s.toCharArray()) {
                if ("[](){}.^$|?*+\\".indexOf(ch) >= 0) out.append('\\');
                out.append(ch);
            }
            return out.toString();
        }
    }
}
