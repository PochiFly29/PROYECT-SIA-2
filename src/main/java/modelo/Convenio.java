package modelo;

import java.util.Objects;

public class Convenio {
    private final String id; // CAMBIO: Inmutable
    private final String universidad; // CAMBIO: Inmutable
    private final String pais; // CAMBIO: Inmutable
    private final String area; // CAMBIO: Inmutable
    private final String requisitosAcademicos; // CAMBIO: Inmutable
    private final String requisitosEconomicos; // CAMBIO: Inmutable

    public Convenio(String id, String universidad, String pais, String area, String requisitosAcademicos, String requisitosEconomicos) {
        this.id = id;
        this.universidad = universidad;
        this.pais = pais;
        this.area = area;
        this.requisitosAcademicos = requisitosAcademicos;
        this.requisitosEconomicos = requisitosEconomicos;
    }

    // Getters (sin cambios)
    public String getId() { return id; }
    public String getUniversidad() { return universidad; }
    public String getPais() { return pais; }
    public String getArea() { return area; }
    public String getRequisitosAcademicos() { return requisitosAcademicos; }
    public String getRequisitosEconomicos() { return requisitosEconomicos; }

    // CAMBIO: Se eliminaron todos los setters para garantizar la inmutabilidad

    // --- Métodos de Infraestructura (AÑADIDOS) ---
    // Útiles para que el Convenio funcione correctamente en colecciones como HashMaps y ArrayLists.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Convenio convenio = (Convenio) o;
        return Objects.equals(id, convenio.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Convenio [%s] %s, %s", id, universidad, pais);
    }
}