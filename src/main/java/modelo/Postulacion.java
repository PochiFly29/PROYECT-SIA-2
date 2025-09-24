package modelo;

import enums.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Postulacion {
    private String id;
    private String rutEstudiante;
    private String idConvenio;
    private Convenio convenioSeleccionado;
    private LocalDate fechaPostulacion;
    private EstadoPostulacion estado;
    private List<Interaccion> interacciones;

    // Constructor para la creación de postulaciones
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
    public void agregarInteraccion(Interaccion interaccion) { this.interacciones.add(interaccion); }
    public void setEstado(EstadoPostulacion estadoPostulacion) { this.estado = estadoPostulacion; }
}