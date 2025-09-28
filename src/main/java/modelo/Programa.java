package modelo;

import java.time.LocalDate;
import java.util.*;

/**
 * Representa un programa académico o de intercambio al que los estudiantes pueden postular.
 * <p>
 * Un programa tiene un identificador único, nombre, fecha de inicio y fin, y mantiene
 * una lista de postulaciones asociadas.
 * </p>
 */
public class Programa {
    private final int id; // CAMBIO: Inmutable
    private final String nombre; // CAMBIO: Inmutable
    private final LocalDate fechaInicio; // CAMBIO: Inmutable
    private final LocalDate fechaFin; // CAMBIO: Inmutable
    private final List<Postulacion> postulaciones;

    /**
     * Constructor principal del programa.
     *
     * @param id identificador único del programa
     * @param nombre nombre del programa
     * @param fechaInicio fecha de inicio del programa
     * @param fechaFin fecha de finalización del programa
     */
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
    /**
     * Agrega una nueva postulación al programa.
     *
     * @param post postulación a agregar
     */
    public void agregarPostulacion(Postulacion post) {
        this.postulaciones.add(post);
    }

    public void setPostulaciones(List<Postulacion> postulaciones) {
        this.postulaciones.clear();
        this.postulaciones.addAll(postulaciones);
    }

    /**
     * Indica si el programa se encuentra vigente según la fecha actual.
     *
     * @return {@code true} si la fecha actual está dentro del rango de inicio y fin, {@code false} en caso contrario
     */
    public boolean estaVigente() {
        LocalDate hoy = LocalDate.now();
        return !hoy.isBefore(this.fechaInicio) && !hoy.isAfter(this.fechaFin);
    }
}