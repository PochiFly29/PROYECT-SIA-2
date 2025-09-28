package modelo;

import java.util.Objects;

/**
 * Representa un convenio de intercambio académico.
 * <p>
 * Contiene información sobre la universidad asociada, país, área de estudios,
 * requisitos académicos y económicos, y el programa al que pertenece.
 */
public class Convenio {

    /** Identificador único del convenio */
    private String id;

    /** Nombre de la universidad asociada al convenio */
    private String universidad;

    /** País de la universidad */
    private String pais;

    /** Área de estudios del convenio */
    private String area; // Atributo 'area' agregado!

    /** Requisitos académicos del convenio */
    private String requisitosAcademicos;

    /** Requisitos económicos del convenio */
    private String requisitosEconomicos;

    /**
     * Constructor de un convenio.
     *
     * @param id identificador único
     * @param universidad nombre de la universidad
     * @param pais país de la universidad
     * @param area área de estudios
     * @param requisitosAcademicos requisitos académicos
     * @param requisitosEconomicos requisitos económicos
     */
    public Convenio(String id, String universidad, String pais, String area, String requisitosAcademicos, String requisitosEconomicos) {
        this.id = id;
        this.universidad = universidad;
        this.pais = pais;
        this.area = area;
        this.requisitosAcademicos = requisitosAcademicos;
        this.requisitosEconomicos = requisitosEconomicos;
    }

    // Sin setters para garantizar la inmutabilidad
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