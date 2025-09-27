package modelo;

import enums.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa una postulación de un estudiante a un convenio dentro del sistema.
 * <p>
 * Cada postulación mantiene información sobre el estudiante, el convenio seleccionado,
 * la fecha de postulación, su estado y las interacciones asociadas.
 */
public class Postulacion {
    /** Identificador único de la postulación */
    private String id;

    /** RUT del estudiante que realiza la postulación */
    private String rutEstudiante;

    /** Identificador del convenio al que se postula */
    private String idConvenio;

    /** Convenio seleccionado por el estudiante */
    private Convenio convenioSeleccionado;

    /** Fecha en que se realiza la postulación */
    private LocalDate fechaPostulacion;

    /** Estado actual de la postulación (PENDIENTE, ACEPTADA, RECHAZADA) */
    private EstadoPostulacion estado;

    /** Lista de interacciones asociadas a esta postulación */
    private List<Interaccion> interacciones;

    // Constructor para la creación de postulaciones
    /**
     * Constructor para la creación de una nueva postulación.
     *
     * @param id Identificador único de la postulación
     * @param rutEstudiante RUT del estudiante
     * @param idConvenio Identificador del convenio
     * @param fechaPostulacion Fecha de la postulación
     * @param estado Estado inicial de la postulación
     */
    public Postulacion(String id, String rutEstudiante, String idConvenio, LocalDate fechaPostulacion, EstadoPostulacion estado) {
        this.id = id;
        this.rutEstudiante = rutEstudiante;
        this.idConvenio = idConvenio;
        this.fechaPostulacion = fechaPostulacion;
        this.estado = estado;
        this.interacciones = new ArrayList<>();
    }

    // Getters, Setters y métodos para agregar interacciones
    public String getId() { return id; }
    public String getRutEstudiante() { return rutEstudiante; }
    public String getIdConvenio() { return idConvenio; }
    public Convenio getConvenioSeleccionado() { return convenioSeleccionado; }
    public void setConvenioSeleccionado(Convenio convenioSeleccionado) { this.convenioSeleccionado = convenioSeleccionado; }
    public LocalDate getFechaPostulacion() { return fechaPostulacion; }
    public EstadoPostulacion getEstado() { return estado; }
    public List<Interaccion> getInteracciones() { return interacciones; }
    public void setEstado(EstadoPostulacion estadoPostulacion) { this.estado = estadoPostulacion; }
    /**
     * Agrega una interacción a la lista de interacciones asociadas a la postulación.
     *
     * @param interaccion La interacción a agregar
     */
    public void agregarInteraccion(Interaccion interaccion) { this.interacciones.add(interaccion); }
}