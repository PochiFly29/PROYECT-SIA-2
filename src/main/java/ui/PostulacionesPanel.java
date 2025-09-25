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
import java.util.regex.Pattern;

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

    // columnas (índices)
    private static final int COL_ID = 0;
    private static final int COL_UNI = 1;
    private static final int COL_PAIS = 2;
    private static final int COL_EMITIDA = 3;
    private static final int COL_VIGENCIA = 4;
    private static final int COL_ESTADO = 5;
    private static final int COL_OBJ = 6;

    public PostulacionesPanel(GestorIntercambio gestor, Usuario usuario) {
        this.gestor = Objects.requireNonNull(gestor);
        this.usuario = usuario;
        init();
        refresh();
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        refresh();
    }

    public void refresh() {
        List<Postulacion> postulaciones;

        if (usuario.getRol() == Rol.ESTUDIANTE) {
            postulaciones = gestor.getPostulaciones("rut", usuario.getRut());
        } else if (usuario.getRol() == Rol.FUNCIONARIO) {
            postulaciones = gestor.getPostulaciones("estado", EstadoPostulacion.POR_REVISAR.name());
        } else {
            postulaciones = Collections.emptyList();
        }

        if (postulaciones.isEmpty()) {
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
            // Llama al gestor para obtener el programa asociado al convenio
            Programa prog = (conv != null) ? gestor.getProgramaDeConvenio(conv) : null;

            String vigencia = vigenciaTexto(prog);

            model.addRow(new Object[]{
                    safe(p.getId()),
                    conv != null ? safe(conv.getUniversidad()) : "-",
                    conv != null ? safe(conv.getPais()) : "-",
                    (p.getFechaPostulacion() != null) ? p.getFechaPostulacion().toString() : "-",
                    vigencia,
                    (p.getEstado() != null) ? p.getEstado().name() : "-",
                    p
            });
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

        String panelTitle = (usuario.getRol() == Rol.ESTUDIANTE) ? "MIS POSTULACIONES" : "POSTULACIONES PENDIENTES";
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

        String[] cols = {"ID", "UNIVERSIDAD", "PAÍS", "EMITIDA", "VIGENCIA", "ESTADO", "_POST_"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }

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
        sorter.setComparator(COL_ID, (Comparator<String>) (s1, s2) -> {
            int n1 = Integer.parseInt(s1.substring(1));
            int n2 = Integer.parseInt(s2.substring(1));
            return Integer.compare(n1, n2);
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
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }
        });

        table.getColumnModel().getColumn(COL_OBJ).setMinWidth(0);
        table.getColumnModel().getColumn(COL_OBJ).setMaxWidth(0);
        table.getColumnModel().getColumn(COL_OBJ).setPreferredWidth(0);

        JScrollPane scroll = new JScrollPane(table);
        add(scroll, BorderLayout.CENTER);

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

    private void applyFilter() {
        if (sorter == null) return;
        String txt = search.getText() == null ? "" : search.getText().trim();
        if (txt.isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(txt)));
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
        // Llama al gestor para obtener el programa asociado al convenio
        Programa prog = (conv != null) ? gestor.getProgramaDeConvenio(conv) : null;
        Estudiante est = gestor.buscarEstudiantePorPostulacion(p.getId());

        JPanel info = new JPanel(new GridBagLayout());
        info.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(4, 4, 4, 4);

        if (usuario.getRol() == Rol.FUNCIONARIO) {
            info.add(new JLabel("Estudiante: " + (est != null ? est.getNombreCompleto() : "-")), gc);
            gc.gridy++;
            info.add(new JLabel("Carrera: " + (est != null ? est.getCarrera() : "-")), gc);
            gc.gridy++;
            info.add(new JLabel("Promedio: " + (est != null ? est.getPromedio() : "-")), gc);
            gc.gridy++;
        }

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

        JButton btnAdj = new JButton("Adjuntar");
        JButton btnInter = new JButton("Interacciones");
        JButton btnCambiarEstado;
        if (usuario.getRol() == Rol.FUNCIONARIO) {
            btnCambiarEstado = new JButton("Cambiar estado");
        } else {
            btnCambiarEstado = null;
        }
        JButton btnCerrar = new JButton("Cerrar");

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
        if (btnCambiarEstado != null) {
            btnCambiarEstado.addActionListener(e -> pane.setValue(btnCambiarEstado));
        }
        btnCerrar.addActionListener(e -> pane.setValue(btnCerrar));

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
        } else if (val == btnCambiarEstado && usuario.getRol() == Rol.FUNCIONARIO) {
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
        java.util.List<Interaccion> xs = p.getInteracciones();
        if (xs == null || xs.isEmpty()) {
            info("No hay interacciones registradas.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (Interaccion i : xs) {
            String contenido = (i.getTipo() == enums.TipoInteraccion.DOCUMENTO)
                    ? i.getTitulo()
                    : i.getTitulo();
            sb.append(i.getFechaHora().toLocalDate())
                    .append(" | ")
                    .append(i.getAutor().getNombreCompleto())
                    .append(" | ")
                    .append(contenido)
                    .append("\n");
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

    // ===== util =====
    private static String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s.trim();
    }

    private static String vigenciaTexto(Programa prog) {
        if (prog != null && prog.getFechaInicio() != null && prog.getFechaFin() != null)
            return prog.getFechaInicio() + " a " + prog.getFechaFin();
        return "-";
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Información", JOptionPane.INFORMATION_MESSAGE);
    }
}