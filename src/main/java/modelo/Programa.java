package modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Programa {
    private int id;
    private final String nombre;
    private final LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;
    private final List<Postulacion> postulaciones;

    /**
     * Constructor para crear un NUEVO programa (antes de tener ID de la BD).
     */
    public Programa(String nombre, LocalDate fechaInicio, LocalDate fechaFin) {
        this.id = 0; // ID temporal hasta que la BD lo asigne
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = "ACTIVO"; // Los nuevos programas siempre nacen activos
        this.postulaciones = new ArrayList<>();
    }

    /**
     * Constructor para cargar un programa existente DESDE la BD.
     */
    public Programa(int id, String nombre, LocalDate fechaInicio, LocalDate fechaFin, String estado) {
        this.id = id;
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
        this.postulaciones = new ArrayList<>();
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public String getEstado() { return estado; }
    public List<Postulacion> getPostulaciones() { return postulaciones; }

    // --- Setters para campos mutables ---
    public void setId(int id) { this.id = id; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setPostulaciones(List<Postulacion> postulaciones) {
        this.postulaciones.clear();
        this.postulaciones.addAll(postulaciones);
    }
    public void agregarPostulacion(Postulacion p) {
        this.postulaciones.add(p);
    }
}