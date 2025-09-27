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
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PostulacionesPanel extends JPanel {

    private final GestorIntercambio gestor;
    private Usuario usuario;

    private JLabel title;
    private JTextField search;
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private JButton btnDetalle;
    private boolean estructuraConstruida = false;

    // columnas (indices)
    private static final int COL_ID      = 0;
    private static final int COL_UNI     = 1;
    private static final int COL_PAIS    = 2;
    private static final int COL_AREA    = 3;
    private static final int COL_EMITIDA = 4;
    private static final int COL_VIGENCIA= 5;
    private static final int COL_ESTADO  = 6;
    private static final int COL_OBJ     = 7;

    private String convenioFilterId = null;

    public PostulacionesPanel(GestorIntercambio gestor, Usuario usuario) {
        this.gestor = java.util.Objects.requireNonNull(gestor);
        this.usuario = usuario;
        init();
        refresh();
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        refresh();
    }

    public void setFiltroConvenio(String convenioId) {
        this.convenioFilterId = (convenioId == null || convenioId.isBlank()) ? null : convenioId.trim();
        refresh();
    }

    public void refresh() {
        List<Postulacion> postulaciones;

        if (usuario != null && usuario.getRol() == Rol.ESTUDIANTE) {
            // solo mis postulaciones
            postulaciones = gestor.getPostulaciones("rut", usuario.getRut());
            setTitulo("MIS POSTULACIONES");
        } else if (usuario != null && usuario.getRol() == Rol.FUNCIONARIO) {
            if (convenioFilterId != null) {
                postulaciones = gestor.getPostulaciones("convenio", convenioFilterId);
                setTitulo("POSTULACIONES • CONVENIO " + convenioFilterId);
            } else {
                postulaciones = gestor.getPostulaciones("estado", EstadoPostulacion.POR_REVISAR.name());
                setTitulo("POSTULACIONES PENDIENTES");
            }
        } else {
            postulaciones = Collections.emptyList();
            setTitulo("POSTULACIONES");
        }

        if (postulaciones == null || postulaciones.isEmpty()) {
            removeAll();
            setLayout(new GridBagLayout());
            JLabel vacio = new JLabel("No se han encontrado postulaciones.");
            vacio.putClientProperty(FlatClientProperties.STYLE, "font:+3");
            add(vacio, new GridBagConstraints());
            estructuraConstruida = false;
            revalidate();
            repaint();
            return;
        }

        if (!estructuraConstruida) {
            removeAll();
            init();
        }

        model.setRowCount(0);
        for (Postulacion p : postulaciones) {
            Convenio conv = p.getConvenioSeleccionado();
            Programa prog = (conv != null) ? gestor.getProgramaDeConvenio(conv) : null;

            String id      = safe(p.getId());
            String uni     = (conv != null) ? safe(conv.getUniversidad()) : "-";
            String pais    = (conv != null) ? safe(conv.getPais()) : "-";
            String area    = (conv != null) ? safe(conv.getArea()) : "-";
            String emitida = (p.getFechaPostulacion() != null) ? p.getFechaPostulacion().toString() : "-";
            String vig     = vigenciaTexto(prog);
            String estado  = (p.getEstado() != null) ? p.getEstado().name() : "-";

            model.addRow(new Object[]{ id, uni, pais, area, emitida, vig, estado, p });
        }

        applyFilter();
        revalidate();
        repaint();
    }

    private void init() {
        setOpaque(false);
        setLayout(new BorderLayout());

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(16, 24, 8, 24));

        String panelTitle = (usuario != null && usuario.getRol() == Rol.ESTUDIANTE)
                ? "MIS POSTULACIONES" : "POSTULACIONES";
        title = new JLabel(panelTitle, SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.putClientProperty(FlatClientProperties.STYLE, "font:bold +6");
        header.add(title);
        header.add(Box.createVerticalStrut(10));

        JPanel searchRow = new JPanel(new BorderLayout());
        searchRow.setOpaque(false);
        searchRow.setBorder(BorderFactory.createEmptyBorder(0, 240, 0, 240));

        search = new JTextField();
        search.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Buscar");
        search.putClientProperty(FlatClientProperties.STYLE, "arc:999; margin:6,14,6,14");
        searchRow.add(search, BorderLayout.CENTER);
        header.add(searchRow);
        add(header, BorderLayout.NORTH);

        String[] cols = { "ID", "UNIVERSIDAD", "PAÍS", "ÁREA", "EMITIDA", "VIGENCIA", "ESTADO", "_POST_" };
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == COL_OBJ ? Postulacion.class : String.class;
            }
        };

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
        sorter.setComparator(COL_ID, (Comparator<String>) (s1, s2) -> {
            // "P123" → 123
            try {
                int n1 = Integer.parseInt(s1.substring(1));
                int n2 = Integer.parseInt(s2.substring(1));
                return Integer.compare(n1, n2);
            } catch (Exception e) {
                return s1.compareTo(s2);
            }
        });
        table.setRowSorter(sorter);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    openDetalle(selectedPostulacion());
                }
            }
        });

        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        table.getColumnModel().getColumn(COL_OBJ).setMinWidth(0);
        table.getColumnModel().getColumn(COL_OBJ).setMaxWidth(0);
        table.getColumnModel().getColumn(COL_OBJ).setPreferredWidth(0);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        footer.setOpaque(false);
        btnDetalle = new JButton("Ver detalle");
        btnDetalle.addActionListener(e -> {
            Postulacion p = selectedPostulacion();
            if (p != null) openDetalle(p);
        });
        footer.add(btnDetalle);
        add(footer, BorderLayout.SOUTH);

        estructuraConstruida = true;
    }

    private void setTitulo(String t) {
        if (title != null) title.setText(t);
    }

    private void applyFilter() {
        if (sorter == null) return;
        String txt = (search.getText() == null) ? "" : search.getText().trim();
        if (txt.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(txt)));
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
        Estudiante est = gestor.buscarEstudiantePorPostulacion(p.getId());

        JPanel info = new JPanel(new GridBagLayout());
        info.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0; gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(4, 4, 4, 4);

        if (usuario.getRol() == Rol.FUNCIONARIO) {
            JLabel lblEstudiante = new JLabel("Estudiante: " + (est != null ? est.getNombreCompleto() : "-"));
            lblEstudiante.setFont(lblEstudiante.getFont().deriveFont(14f));
            info.add(lblEstudiante, gc); gc.gridy++;

            JLabel lblCarrera = new JLabel("Carrera: " + (est != null ? est.getCarrera() : "-"));
            lblCarrera.setFont(lblCarrera.getFont().deriveFont(14f));
            info.add(lblCarrera, gc); gc.gridy++;

            JLabel lblPromedio = new JLabel("Promedio: " + (est != null ? est.getPromedio() : "-"));
            lblPromedio.setFont(lblPromedio.getFont().deriveFont(14f));
            info.add(lblPromedio, gc); gc.gridy++;
        }

        JLabel lblID = new JLabel("ID: " + safe(p.getId()));
        lblID.setFont(lblID.getFont().deriveFont(14f));
        info.add(lblID, gc); gc.gridy++;

        JLabel lblUni = new JLabel("Universidad: " + (conv != null ? safe(conv.getUniversidad()) : "-"));
        lblUni.setFont(lblUni.getFont().deriveFont(14f));
        info.add(lblUni, gc); gc.gridy++;

        JLabel lblPais = new JLabel("País: " + (conv != null ? safe(conv.getPais()) : "-"));
        lblPais.setFont(lblPais.getFont().deriveFont(14f));
        info.add(lblPais, gc); gc.gridy++;

        JLabel lblArea = new JLabel("Área: " + (conv != null ? safe(conv.getArea()) : "-"));
        lblArea.setFont(lblArea.getFont().deriveFont(14f));
        info.add(lblArea, gc); gc.gridy++;

        JLabel lblPlazo = new JLabel("Plazo: " + vigenciaTexto(prog));
        lblPlazo.setFont(lblPlazo.getFont().deriveFont(14f));
        info.add(lblPlazo, gc); gc.gridy++;

        JLabel lblEstado = new JLabel("Estado: " + (p.getEstado() != null ? p.getEstado().name() : "-"));
        lblEstado.setFont(lblEstado.getFont().deriveFont(14f));
        info.add(lblEstado, gc);

        if (conv != null) {
            gc.gridy++;
            JLabel lblReqAcad = new JLabel("Requisitos Académicos: " + safe(conv.getRequisitosAcademicos()));
            lblReqAcad.setFont(lblReqAcad.getFont().deriveFont(14f));
            info.add(lblReqAcad, gc);

            gc.gridy++;
            JLabel lblReqEco = new JLabel("Requisitos Económicos: " + safe(conv.getRequisitosEconomicos()));
            lblReqEco.setFont(lblReqEco.getFont().deriveFont(14f));
            info.add(lblReqEco, gc);
        }

        JButton btnAdj = new JButton("Adjuntar");
        JButton btnInter = new JButton("Interacciones");
        JButton btnCambiarEstado = (usuario.getRol() == Rol.FUNCIONARIO) ? new JButton("Cambiar estado") : null;
        JButton btnCerrar = new JButton("Cerrar");

        JOptionPane pane = new JOptionPane(info,JOptionPane.PLAIN_MESSAGE,JOptionPane.DEFAULT_OPTION,null,new Object[]{},null);

        btnAdj.addActionListener(e -> pane.setValue(btnAdj));
        btnInter.addActionListener(e -> pane.setValue(btnInter));
        if (btnCambiarEstado != null) btnCambiarEstado.addActionListener(e -> pane.setValue(btnCambiarEstado));
        btnCerrar.addActionListener(e -> pane.setValue(btnCerrar));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        actions.add(btnAdj);
        btnAdj.putClientProperty(FlatClientProperties.STYLE, "background:#2E86FF; foreground:#FFFFFF");
        actions.add(btnInter);
        btnInter.putClientProperty(FlatClientProperties.STYLE, "background:#2E86FF; foreground:#FFFFFF");
        if (btnCambiarEstado != null) {
            actions.add(btnCambiarEstado);
            btnCambiarEstado.putClientProperty(FlatClientProperties.STYLE, "background:#2E86FF; foreground:#FFFFFF");
        }
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
            handleAdjuntarDocumento(p);
        } else if (val == btnInter) {
            verHistorial(p);
        } else if (btnCambiarEstado != null && val == btnCambiarEstado) {
            handleCambiarEstado(p);
        }
        refresh();
    }

    private void handleAdjuntarDocumento(Postulacion p) {
        if (usuario.getRol() == Rol.ESTUDIANTE) {
            JTextField tfTitulo = new JTextField();
            tfTitulo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ej: PASAPORTE.pdf");
            int ok = JOptionPane.showConfirmDialog(this, tfTitulo,
                    "Título del documento", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (ok == JOptionPane.OK_OPTION && !tfTitulo.getText().trim().isEmpty()) {
                Interaccion inter = Interaccion.ofDocumento(usuario, tfTitulo.getText().trim());
                gestor.agregarInteraccionAPostulacion(p.getId(), inter);
                info("Documento agregado.");
            }
        } else if (usuario.getRol() == Rol.FUNCIONARIO) {
            JTextArea ta = new JTextArea(5, 30);
            int ok = JOptionPane.showConfirmDialog(this, new JScrollPane(ta),
                    "Comentario", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (ok == JOptionPane.OK_OPTION && !ta.getText().trim().isEmpty()) {
                Interaccion inter = Interaccion.ofComentario(usuario, ta.getText().trim());
                gestor.agregarInteraccionAPostulacion(p.getId(), inter);
                info("Comentario agregado.");
            }
        }
    }

    private void handleCambiarEstado(Postulacion p) {
        EstadoPostulacion[] estados = EstadoPostulacion.values();
        EstadoPostulacion nuevoEstado = (EstadoPostulacion) JOptionPane.showInputDialog(
                this,
                "Seleccione el nuevo estado:",
                "Cambiar Estado",
                JOptionPane.QUESTION_MESSAGE,
                null,
                estados,
                p.getEstado()
        );

        if (nuevoEstado != null && nuevoEstado != p.getEstado()) {
            if (nuevoEstado == EstadoPostulacion.ACEPTADA) {
                Estudiante estudiante = gestor.buscarEstudiantePorPostulacion(p.getId());
                if (estudiante != null) {
                    gestor.descartarOtrasPostulaciones(estudiante.getRut(), p.getId());
                }
                gestor.actualizarEstadoPostulacion(p.getId(), EstadoPostulacion.ACEPTADA);
                info("Estado de postulación " + p.getId() + " actualizado a 'ACEPTADA'. Las demás postulaciones del estudiante han sido rechazadas.");
            } else {
                gestor.actualizarEstadoPostulacion(p.getId(), nuevoEstado);
                info("Estado de postulación " + p.getId() + " actualizado a '" + nuevoEstado + "'.");
            }
        }
    }

    private void verHistorial(Postulacion p) {
        List<Interaccion> xs = p.getInteracciones();
        if (xs == null || xs.isEmpty()) {
            info("No hay interacciones registradas.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (Interaccion i : xs) {
            String txt = i.getTitulo();
            sb.append(i.getFechaHora().toLocalDate()).append(" | ").append(i.getAutor().getNombreCompleto()).append(" | ").append(txt).append("\n");
        }

        JTextArea ta = new JTextArea(sb.toString(), 12, 60);
        ta.setEditable(false);
        JOptionPane.showMessageDialog( this, new JScrollPane(ta),"Historial de Interacciones",JOptionPane.PLAIN_MESSAGE);
    }

    // ===== util =====
    private static String safe(String s) { return (s == null || s.trim().isEmpty()) ? "-" : s.trim(); }

    private static String vigenciaTexto(Programa prog) {
        if (prog != null && prog.getFechaInicio() != null && prog.getFechaFin() != null)
            return prog.getFechaInicio() + " a " + prog.getFechaFin();
        return "-";
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Información", JOptionPane.INFORMATION_MESSAGE);
    }
}
