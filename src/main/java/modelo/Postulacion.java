package modelo;

import enums.EstadoPostulacion;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Postulacion {
    private int id;
    private final String rutEstudiante; // CAMBIO: Inmutable
    private final Convenio convenioSeleccionado; // CAMBIO: Inmutable y única fuente de verdad
    private final LocalDate fechaPostulacion; // CAMBIO: Inmutable
    private EstadoPostulacion estado;
    private final List<Interaccion> interacciones;

    /**
     * Constructor único para cargar o crear postulaciones.
     * Si el ID es 0, es una nueva postulación. Si es > 0, viene de la BD.
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
    public void agregarInteraccion(Interaccion interaccion) { this.interacciones.add(interaccion); }
    public void setId(int id) { this.id = id; }
    public void setEstado(EstadoPostulacion estadoPostulacion) { this.estado = estadoPostulacion; }
    public void setInteracciones(List<Interaccion> interacciones) {
        this.interacciones.clear();
        this.interacciones.addAll(interacciones);
    }
}