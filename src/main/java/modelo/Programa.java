package modelo;

import java.time.LocalDate;
import java.util.*;

public class Programa {
    private int id;
    private String nombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private List<Convenio> convenios;

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
    public boolean agregarConvenio(Convenio conv) {
        if (convenios.contains(conv)) {
            return false;
        }
        convenios.add(conv);
        return true;
    }
    public boolean estaVigente() {
        LocalDate hoy = LocalDate.now();
        return !hoy.isBefore(this.fechaInicio) && !hoy.isAfter(this.fechaFin);
    }
}