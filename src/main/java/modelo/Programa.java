package modelo;

import java.time.LocalDate;
import java.util.*;

public class Programa {
    private final int id; // CAMBIO: Inmutable
    private final String nombre; // CAMBIO: Inmutable
    private final LocalDate fechaInicio; // CAMBIO: Inmutable
    private final LocalDate fechaFin; // CAMBIO: Inmutable
    private final List<Postulacion> postulaciones;

    public Programa(int id, String nombre, LocalDate fechaInicio, LocalDate fechaFin) {
        this.id = id;
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.postulaciones = new ArrayList<>();
    }

    // Getters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public List<Postulacion> getPostulaciones() {
        return Collections.unmodifiableList(postulaciones); // CAMBIO: Se retorna una vista de solo lectura
    }

    // Métodos de gestión
    public void agregarPostulacion(Postulacion post) {
        this.postulaciones.add(post);
    }

    public void setPostulaciones(List<Postulacion> postulaciones) {
        this.postulaciones.clear();
        this.postulaciones.addAll(postulaciones);
    }

    public boolean estaVigente() {
        LocalDate hoy = LocalDate.now();
        return !hoy.isBefore(this.fechaInicio) && !hoy.isAfter(this.fechaFin);
    }
}