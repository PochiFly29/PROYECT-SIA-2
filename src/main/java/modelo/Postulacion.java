package modelo;

import enums.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Postulacion {
    private String id;
    private Convenio convenioSeleccionado;
    private LocalDate fechaPostulacion;
    private EstadoPostulacion estado;
    private List<Interaccion> interacciones;

    public Postulacion(String id, Convenio convenioSeleccionado, LocalDate fechaPostulacion, EstadoPostulacion estado) {
        this.id = id;
        this.convenioSeleccionado = convenioSeleccionado;
        this.fechaPostulacion = fechaPostulacion;
        this.estado = estado;
        this.interacciones = new ArrayList<>();
    }

    public void agregarInteraccion(Interaccion i) {
        if (i != null) this.interacciones.add(i);
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Convenio getConvenioSeleccionado() { return convenioSeleccionado; }
    public void setConvenioSeleccionado(Convenio convenioSeleccionado) { this.convenioSeleccionado = convenioSeleccionado; }

    public LocalDate getFechaPostulacion() { return fechaPostulacion; }
    public void setFechaPostulacion(LocalDate fechaPostulacion) { this.fechaPostulacion = fechaPostulacion; }

    public EstadoPostulacion getEstado() { return estado; }
    public void setEstado(EstadoPostulacion estado) { this.estado = estado; }

    public List<Interaccion> getInteracciones() { return interacciones; }
    public void setInteracciones(List<Interaccion> interacciones) { this.interacciones = interacciones; }
}