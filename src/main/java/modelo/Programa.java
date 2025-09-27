package modelo;

import java.time.LocalDate;
import java.util.*;

/**
 * Representa un programa de intercambio académico.
 * <p>
 * Cada programa tiene un identificador, nombre, fechas de inicio y fin,
 * y mantiene una lista de convenios asociados.
 */
public class Programa {
    /** Identificador único del programa */
    private int id;

    /** Nombre del programa */
    private String nombre;

    /** Fecha de inicio del programa */
    private LocalDate fechaInicio;

    /** Fecha de finalización del programa */
    private LocalDate fechaFin;

    /** Lista de convenios asociados al programa */
    private List<Convenio> convenios;

    /**
     * Constructor para crear un nuevo programa.
     *
     * @param id Identificador único
     * @param nombre Nombre del programa
     * @param fechaInicio Fecha de inicio del programa
     * @param fechaFin Fecha de finalización del programa
     */
    public Programa(int id, String nombre, LocalDate fechaInicio, LocalDate fechaFin) {
        this.id = id;
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.convenios = new ArrayList<>();
    }
    // Getters, Setters y otros métodos...
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public List<Convenio> getConvenios() { return convenios; }

    /**
     * Agrega un convenio a la lista de convenios del programa.
     * Evita duplicados.
     *
     * @param conv Convenio a agregar
     * @return true si se agregó correctamente, false si ya existía
     */
    public boolean agregarConvenio(Convenio conv) {
        if (convenios.contains(conv)) {
            return false;
        }
        convenios.add(conv);
        return true;
    }

    /**
     * Verifica si el programa está vigente según la fecha actual.
     *
     * @return true si la fecha actual está entre la fecha de inicio y fin (inclusive), false en caso contrario
     */
    public boolean estaVigente() {
        LocalDate hoy = LocalDate.now();
        return !hoy.isBefore(this.fechaInicio) && !hoy.isAfter(this.fechaFin);
    }
}