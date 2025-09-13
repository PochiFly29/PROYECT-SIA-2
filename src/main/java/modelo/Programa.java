package modelo;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Programa {
    private String id; // ej: "S1-2025"
    private String nombre; // ej: "Movilidad Semestral 2025"
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private List<Convenio> conveniosVigentes; // colección anidada (SIA1.5)

    public Programa(String id, String nombre, LocalDate fechaInicio, LocalDate fechaFin) {
        this.id = id;
        this.nombre = nombre;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.conveniosVigentes = new ArrayList<>();
    }

    public boolean agregarConvenio(Convenio conv1) {
        if (conveniosVigentes.contains(conv1)) {
            return false;
        }
        conveniosVigentes.add(conv1);
        return true;
    }

    // Getters y Setters

    public List<Convenio> getConveniosVigentes() {
        return conveniosVigentes.stream()
                .filter(Convenio::estaVigente)
                .collect(Collectors.toList());
    }

    public boolean estaVigente() {
        LocalDate hoy = LocalDate.now();
        return !hoy.isBefore(this.fechaInicio) && !hoy.isAfter(this.fechaFin);
    }

    public void setConveniosVigentes(List<Convenio> conveniosVigentes) {
        this.conveniosVigentes = conveniosVigentes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }
}
