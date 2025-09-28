package modelo;

import enums.EstadoPostulacion;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa una postulación de un estudiante a un convenio dentro de un programa.
 * <p>
 * Cada postulación tiene un {@link Convenio} seleccionado, un {@code rutEstudiante} y una lista de interacciones.
 * El {@code id} es asignado por la base de datos al persistir la postulación.
 * </p>
 */
public class Postulacion {
    private int id;
    private final String rutEstudiante; // CAMBIO: Inmutable
    private final Convenio convenioSeleccionado; // CAMBIO: Inmutable y única fuente de verdad
    private final LocalDate fechaPostulacion; // CAMBIO: Inmutable
    private EstadoPostulacion estado;
    private final List<Interaccion> interacciones;

    /**
     * Constructor principal para cargar o crear postulaciones desde la base de datos.
     *
     * @param id identificador de la postulación (0 si es nueva)
     * @param rutEstudiante RUT del estudiante que realiza la postulación
     * @param convenio convenio seleccionado por el estudiante
     * @param fecha fecha de la postulación
     * @param estado estado actual de la postulación
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
     * La fecha se establece como la fecha actual y el estado inicial como {@link EstadoPostulacion#PENDIENTE}.
     *
     * @param rutEstudiante RUT del estudiante
     * @param convenio convenio seleccionado
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
     * Añade una interacción a la postulación.
     *
     * @param interaccion interacción a agregar
     */
    public void agregarInteraccion(Interaccion interaccion) { this.interacciones.add(interaccion); }
    public void setId(int id) { this.id = id; }
    public void setEstado(EstadoPostulacion estadoPostulacion) { this.estado = estadoPostulacion; }
    public void setInteracciones(List<Interaccion> interacciones) {
        this.interacciones.clear();
        this.interacciones.addAll(interacciones);
    }
}