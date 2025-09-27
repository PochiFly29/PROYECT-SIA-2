package ui;

import enums.EstadoPostulacion;
import enums.Rol;
import gestores.GestorIntercambio;
import modelo.*;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel ÚNICO y REUTILIZABLE para mostrar los detalles de una postulación.
 * Es polivalente: muestra diferentes acciones si el usuario es Estudiante o Funcionario.
 */
public class DetallePostulacionPanel extends JPanel {
    private final GestorIntercambio gestor;
    private final Postulacion postulacion;
    private final Usuario usuarioActivo; // Quien está viendo el panel
    private final JDialog parentDialog;  // Para poder cerrar el diálogo desde los botones

    public DetallePostulacionPanel(GestorIntercambio gestor, Postulacion postulacion, Usuario usuarioActivo, JDialog parentDialog) {
        this.gestor = gestor;
        this.postulacion = postulacion;
        this.usuarioActivo = usuarioActivo;
        this.parentDialog = parentDialog;
        init();
    }

    private void init() {
        setLayout(new BorderLayout(10, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // 1. Panel de Información (arriba)
        add(createInfoPanel(), BorderLayout.CENTER);

        // 2. Panel de Acciones/Botones (abajo)
        add(createActionsPanel(), BorderLayout.SOUTH);
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Obtenemos los datos necesarios usando el servicio de consulta
        Estudiante estudiante = gestor.getServicioConsulta()
                .buscarEstudiantePorRut(postulacion.getRutEstudiante())
                .orElse(null);
        Convenio convenio = postulacion.getConvenioSeleccionado();

        // Título con el estado actual
        JLabel lblTitulo = new JLabel("Detalle Postulación #" + postulacion.getId() + " - Estado: " + postulacion.getEstado().name());
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 16f));
        gbc.gridwidth = 2; // Ocupa dos columnas
        panel.add(lblTitulo, gbc);
        gbc.gridy++;
        gbc.gridwidth = 1; // Restaura a una columna

        // Añadimos la información fila por fila
        if (estudiante != null) {
            addInfoRow(panel, gbc, "Estudiante:", estudiante.getNombreCompleto());
            addInfoRow(panel, gbc, "Carrera:", estudiante.getCarrera());
        }
        addInfoRow(panel, gbc, "Universidad Destino:", convenio.getUniversidad() + " (" + convenio.getPais() + ")");
        addInfoRow(panel, gbc, "Área:", convenio.getArea());
        addInfoRow(panel, gbc, "Fecha Postulación:", postulacion.getFechaPostulacion().toString());

        return panel;
    }

    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        // --- Botones Específicos por Rol ---
        if (usuarioActivo.getRol() == Rol.ESTUDIANTE) {
            JButton btnAdjuntar = new JButton("Adjuntar Documento");
            btnAdjuntar.addActionListener(e -> handleAdjuntarDocumento());
            panel.add(btnAdjuntar);
        } else if (usuarioActivo.getRol() == Rol.FUNCIONARIO) {
            JButton btnComentar = new JButton("Agregar Comentario");
            btnComentar.addActionListener(e -> handleAgregarComentario());
            panel.add(btnComentar);

            JButton btnCambiarEstado = new JButton("Cambiar Estado");
            btnCambiarEstado.addActionListener(e -> handleCambiarEstado());
            panel.add(btnCambiarEstado);
        }

        // --- Botones Comunes para Ambos Roles ---
        JButton btnHistorial = new JButton("Ver Interacciones");
        btnHistorial.addActionListener(e -> verHistorial());
        panel.add(btnHistorial);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> parentDialog.dispose());
        panel.add(btnCerrar);

        return panel;
    }

    // --- Métodos de Acción (Handlers) ---

    private void handleAdjuntarDocumento() {
        String tituloDoc = JOptionPane.showInputDialog(this, "Ingrese el nombre del documento (ej: Pasaporte.pdf):", "Adjuntar Documento", JOptionPane.PLAIN_MESSAGE);
        if (tituloDoc != null && !tituloDoc.trim().isEmpty()) {
            try {
                Interaccion inter = Interaccion.ofDocumento(usuarioActivo, tituloDoc.trim());
                gestor.getServicioPostulacion().agregarInteraccion(postulacion, inter);
                info("Documento '" + tituloDoc.trim() + "' registrado en el historial.");
            } catch (SQLException ex) {
                error("No se pudo registrar el documento: " + ex.getMessage());
            }
        }
    }

    private void handleAgregarComentario() {
        String comentario = JOptionPane.showInputDialog(this, "Ingrese su comentario:", "Agregar Comentario", JOptionPane.PLAIN_MESSAGE);
        if (comentario != null && !comentario.trim().isEmpty()) {
            try {
                Interaccion inter = Interaccion.ofComentario(usuarioActivo, comentario.trim());
                gestor.getServicioPostulacion().agregarInteraccion(postulacion, inter);
                info("Comentario agregado exitosamente.");
            } catch (SQLException ex) {
                error("No se pudo agregar el comentario: " + ex.getMessage());
            }
        }
    }

    private void handleCambiarEstado() {
        EstadoPostulacion[] estados = EstadoPostulacion.values();
        EstadoPostulacion nuevoEstado = (EstadoPostulacion) JOptionPane.showInputDialog(this, "Seleccione el nuevo estado:", "Cambiar Estado", JOptionPane.QUESTION_MESSAGE, null, estados, postulacion.getEstado());

        if (nuevoEstado != null && nuevoEstado != postulacion.getEstado()) {
            try {
                Programa programa = gestor.getServicioConsulta().getProgramaPorId(1).orElseThrow();
                if (nuevoEstado == EstadoPostulacion.ACEPTADA) {
                    gestor.getServicioPostulacion().aceptarPostulacionYRechazarResto(programa, postulacion);
                    info("Estado actualizado a ACEPTADA. Las demás postulaciones del estudiante han sido rechazadas.");
                } else {
                    gestor.getServicioPostulacion().actualizarEstadoPostulacion(postulacion, nuevoEstado);
                    info("Estado actualizado a " + nuevoEstado + ".");
                }
                parentDialog.dispose(); // Cierra el diálogo si la acción fue exitosa
            } catch (Exception ex) {
                error("No se pudo actualizar el estado: " + ex.getMessage());
            }
        }
    }

    private void verHistorial() {
        List<Interaccion> interacciones = postulacion.getInteracciones();
        if (interacciones.isEmpty()) {
            info("No hay interacciones registradas para esta postulación.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Interaccion i : interacciones) {
            sb.append(String.format("%s (%s) - %s: %s\n",
                    i.getFechaHora().toLocalDate(), i.getTipo(), i.getAutor().getNombreCompleto(), i.getTitulo()));
        }
        JTextArea textArea = new JTextArea(sb.toString(), 15, 50);
        textArea.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Historial de Interacciones", JOptionPane.PLAIN_MESSAGE);
    }

    // --- Métodos de Utilidad ---

    private void addInfoRow(JPanel panel, GridBagConstraints gbc, String label, String value) {
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(lblLabel.getFont().deriveFont(Font.BOLD));
        panel.add(lblLabel, gbc);

        gbc.gridx = 1;
        panel.add(new JLabel(value), gbc);

        gbc.gridx = 0;
        gbc.gridy++;
    }

    private void info(String msg) { JOptionPane.showMessageDialog(this, msg, "Información", JOptionPane.INFORMATION_MESSAGE); }
    private void error(String msg) { JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE); }
}