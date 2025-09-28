package ui;

import gestores.GestorIntercambio;
import modelo.Programa;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * **Panel de Interfaz de Usuario para la Administración del Ciclo de Vida de los Programas.**
 * <p>Este panel proporciona la interfaz necesaria para que el Auditor realice la gestión
 * completa de los {@link Programa}s de intercambio: **Crear**, **Consultar**,
 * **Finalizar** y **Eliminar**. Implementa las reglas de negocio críticas relacionadas
 * con la finalización de un programa activo.</p>
 */
public class GestionProgramasPanel extends JPanel {
    private final GestorIntercambio gestor;
    private DefaultTableModel model;
    private JTable table;
    private JButton btnCrear, btnFinalizar, btnEliminar;

    /**
     * Crea e inicializa el panel de gestión de programas.
     * @param gestor El gestor central de la aplicación.
     */
    public GestionProgramasPanel(GestorIntercambio gestor) {
        this.gestor = gestor;
        init();
        refresh();
    }

    public void refresh() {
        List<Programa> programas = gestor.getServicioConsulta().getTodosLosProgramas();
        model.setRowCount(0);
        programas.forEach(p -> model.addRow(new Object[]{p.getId(), p.getNombre(), p.getEstado(), p.getFechaInicio(), p.getFechaFin(), p}));

        // Habilita/deshabilita el botón de finalizar según si hay un programa activo
        boolean hayActivo = gestor.getServicioConsulta().getProgramaActivo().isPresent();
        btnFinalizar.setEnabled(hayActivo);
    }

    private void init() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Título
        JLabel title = new JLabel("GESTIÓN DE PROGRAMAS DE INTERCAMBIO", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        add(title, BorderLayout.NORTH);

        // Tabla
        String[] cols = {"ID", "Nombre", "Estado", "Inicio", "Fin", "_OBJ"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        table.getColumn("_OBJ").setMinWidth(0);
        table.getColumn("_OBJ").setMaxWidth(0);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Panel de botones de acción
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        btnCrear = new JButton("Crear Nuevo Programa");
        btnFinalizar = new JButton("Finalizar Programa Activo");
        btnEliminar = new JButton("Eliminar Seleccionado");
        actionsPanel.add(btnCrear);
        actionsPanel.add(btnFinalizar);
        actionsPanel.add(btnEliminar);
        add(actionsPanel, BorderLayout.SOUTH);

        // Action Listeners
        btnCrear.addActionListener(e -> crearPrograma());
        btnFinalizar.addActionListener(e -> finalizarPrograma());
        btnEliminar.addActionListener(e -> eliminarPrograma());
    }

    private void crearPrograma() {
        // Formulario simple para crear un programa
        JTextField nombreField = new JTextField();
        JTextField inicioField = new JTextField(LocalDate.now().toString());
        JTextField finField = new JTextField(LocalDate.now().plusYears(1).toString());

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Nombre del Programa:"));
        panel.add(nombreField);
        panel.add(new JLabel("Fecha Inicio (YYYY-MM-DD):"));
        panel.add(inicioField);
        panel.add(new JLabel("Fecha Fin (YYYY-MM-DD):"));
        panel.add(finField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Crear Programa", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                gestor.getServicioPrograma().crearPrograma(
                        nombreField.getText(),
                        LocalDate.parse(inicioField.getText()),
                        LocalDate.parse(finField.getText())
                );
                JOptionPane.showMessageDialog(this, "Programa creado exitosamente.");
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error al Crear", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void finalizarPrograma() {
        int r = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de que desea finalizar el programa activo?\nTodas las postulaciones PENDIENTES o REVISADAS serán RECHAZADAS.",
                "Confirmar Finalización", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (r == JOptionPane.YES_OPTION) {
            try {
                Programa activo = gestor.getServicioConsulta().getProgramaActivo().orElseThrow();
                gestor.getServicioPrograma().finalizarPrograma(activo);
                JOptionPane.showMessageDialog(this, "Programa finalizado exitosamente.");
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error al Finalizar", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void eliminarPrograma() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un programa de la tabla para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Programa programa = (Programa) model.getValueAt(table.convertRowIndexToModel(selectedRow), 5);
        int r = JOptionPane.showConfirmDialog(this,
                "¡ADVERTENCIA! Está a punto de eliminar el programa '" + programa.getNombre() + "'.\n" +
                        "Esto borrará permanentemente TODAS sus postulaciones e interacciones asociadas.\n¿Está absolutamente seguro?",
                "Confirmar Eliminación Permanente", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);

        if (r == JOptionPane.YES_OPTION) {
            try {
                gestor.getServicioPrograma().eliminarPrograma(programa.getId());
                JOptionPane.showMessageDialog(this, "Programa eliminado exitosamente.");
                refresh();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error al Eliminar", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}