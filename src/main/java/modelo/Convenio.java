package modelo;

import java.util.Objects;

/**
 * Representa un convenio de intercambio entre universidades.
 * <p>
 * Esta clase es inmutable: todos sus atributos son finales y no existen setters,
 * lo que garantiza que un convenio no pueda ser modificado una vez creado.
 * </p>
 */
public class Convenio {
    private final String id; // CAMBIO: Inmutable
    private final String universidad; // CAMBIO: Inmutable
    private final String pais; // CAMBIO: Inmutable
    private final String area; // CAMBIO: Inmutable
    private final String requisitosAcademicos; // CAMBIO: Inmutable
    private final String requisitosEconomicos; // CAMBIO: Inmutable

    /**
     * Crea un nuevo convenio de intercambio.
     *
     * @param id identificador único del convenio
     * @param universidad universidad con la cual se establece el convenio
     * @param pais país de la universidad asociada
     * @param area área de estudios que cubre el convenio
     * @param requisitosAcademicos requisitos académicos necesarios para postular
     * @param requisitosEconomicos requisitos económicos o financieros para postular
     */
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
    /**
     * Dos convenios son iguales si tienen el mismo {@code id}.
     *
     * @param o objeto a comparar
     * @return {@code true} si ambos convenios tienen el mismo identificador
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Convenio convenio = (Convenio) o;
        return Objects.equals(id, convenio.id);
    }

    /**
     * Calcula el código hash del convenio en base a su {@code id}.
     *
     * @return valor hash correspondiente
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Devuelve una representación en texto legible del convenio,
     * que incluye el identificador, universidad y país.
     *
     * @return representación en cadena del convenio
     */
    @Override
    public String toString() {
        return String.format("Convenio [%s] %s, %s", id, universidad, pais);
    }
}