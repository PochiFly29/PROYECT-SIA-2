package modelo;

import java.time.LocalDate;
import enums.EstadoConvenio;

public class Convenio {
    private String id; // único
    private String universidad; // e.g., "Oxford University"
    private String pais; // e.g., "Reino Unido"
    private String area; // opcional: área/facultad
    private String requisitosAcademicos;
    private String requisitosEconomicos;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoConvenio estado; // VIGENTE / POR_VENCER / VENCIDO

    public Convenio(String id, String universidad, String pais, String area,
                    String requisitosAcademicos, String requisitosEconomicos,
                    LocalDate fechaInicio, LocalDate fechaFin) {
        this.id = id;
        this.universidad = universidad;
        this.pais = pais;
        this.area = area;
        this.requisitosAcademicos = requisitosAcademicos;
        this.requisitosEconomicos = requisitosEconomicos;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = EstadoConvenio.VIGENTE;
    }


    // Sobrecarga de métodos comparar con hoy
    public boolean estaVigente() {
        LocalDate hoy = LocalDate.now();
        return !hoy.isBefore(fechaInicio) && !hoy.isAfter(fechaFin);
    }

    // Comparar con fecha especifica (ideal la de termino de programa)
    public boolean estaVigente(LocalDate fecha) {
        return !fecha.isBefore(fechaInicio) && !fecha.isAfter(fechaFin);
    }


    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUniversidad() { return universidad; }
    public void setUniversidad(String universidad) { this.universidad = universidad; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getRequisitosAcademicos() { return requisitosAcademicos; }
    public void setRequisitosAcademicos(String requisitosAcademicos) { this.requisitosAcademicos = requisitosAcademicos; }

    public String getRequisitosEconomicos() { return requisitosEconomicos; }
    public void setRequisitosEconomicos(String requisitosEconomicos) { this.requisitosEconomicos = requisitosEconomicos; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public EstadoConvenio getEstado() { return estado; }
    public void setEstado(EstadoConvenio estado) { this.estado = estado; }

}