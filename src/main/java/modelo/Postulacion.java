package modelo;

import enums.EstadoPostulacion;
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
    private int id;

    /** RUT del estudiante que realiza la postulación */
    private final String rutEstudiante;

    /** Convenio seleccionado por el estudiante */
    private final Convenio convenioSeleccionado;

    /** Fecha en que se realiza la postulación */
    private final LocalDate fechaPostulacion;

    /** Estado actual de la postulación (PENDIENTE, ACEPTADA, RECHAZADA) */
    private EstadoPostulacion estado;

    /** Lista de interacciones asociadas a esta postulación */
    private final List<Interaccion> interacciones;

    /**
     * Constructor para la creación de una nueva postulación.
     * Si el ID es 0, es una nueva postulación. Si es > 0, viene de la BD.
     * @param id Identificador único de la postulación
     * @param rutEstudiante RUT del estudiante
     * @param fecha Fecha de la postulación
     * @param estado Estado inicial de la postulación
     */
    public Postulacion(int id, String rutEstudiante, Convenio convenio, LocalDate fecha, EstadoPostulacion estado) {
        this.id = id;
        this.rutEstudiante = rutEstudiante;
        this.convenioSeleccionado = convenio; // CAMBIO: Se recibe el objeto completo
        this.fechaPostulacion = fecha;
        this.estado = estado;
        this.interacciones = new ArrayList<>();
    }

    /**
     * Constructor de conveniencia para crear una NUEVA postulación.
     */
    public Postulacion(String rutEstudiante, Convenio convenio) {
        this(0, rutEstudiante, convenio, LocalDate.now(), EstadoPostulacion.PENDIENTE);
    }

    // Getters
    public int getId() { return id; }
    public String getRutEstudiante() { return rutEstudiante; }
    public Convenio getConvenioSeleccionado() { return convenioSeleccionado; }
    public LocalDate getFechaPostulacion() { return fechaPostulacion; }
    public EstadoPostulacion getEstado() { return estado; }
    public List<Interaccion> getInteracciones() { return interacciones; }

    // Setters y métodos
    /**
     * Agrega una interacción a la lista de interacciones asociadas a la postulación.
     * @param interaccion La interacción a agregar
     */
    public void agregarInteraccion(Interaccion interaccion) {
        this.interacciones.add(interaccion);
    }

    public void setId(int id) { this.id = id; }
    public void setEstado(EstadoPostulacion estadoPostulacion) { this.estado = estadoPostulacion; }
    public void setInteracciones(List<Interaccion> interacciones) {
        this.interacciones.clear();
        this.interacciones.addAll(interacciones);
    }
}