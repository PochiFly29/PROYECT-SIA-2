package modelo;

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
    private String area; // ¡Atributo 'area' agregado!

    /** Requisitos académicos del convenio */
    private String requisitosAcademicos;

    /** Requisitos económicos del convenio */
    private String requisitosEconomicos;

    /** ID del programa al que pertenece el convenio */
    private int idPrograma;

    /**
     * Constructor de un convenio.
     *
     * @param id identificador único
     * @param universidad nombre de la universidad
     * @param pais país de la universidad
     * @param area área de estudios
     * @param requisitosAcademicos requisitos académicos
     * @param requisitosEconomicos requisitos económicos
     * @param idPrograma identificador del programa asociado
     */
    public Convenio(String id, String universidad, String pais, String area, String requisitosAcademicos, String requisitosEconomicos, int idPrograma) {
        this.id = id;
        this.universidad = universidad;
        this.pais = pais;
        this.area = area;
        this.requisitosAcademicos = requisitosAcademicos;
        this.requisitosEconomicos = requisitosEconomicos;
        this.idPrograma = idPrograma;
    }
    // Getters y Setters...
    /** @return el ID del convenio */
    public String getId() { return id; }

    /** @return el nombre de la universidad */
    public String getUniversidad() { return universidad; }

    /** @return el país de la universidad */
    public String getPais() { return pais; }

    /** @return el área de estudios */
    public String getArea() { return area; }

    /** @return los requisitos académicos */
    public String getRequisitosAcademicos() { return requisitosAcademicos; }

    /** @return los requisitos económicos */
    public String getRequisitosEconomicos() { return requisitosEconomicos; }

    /** @return el ID del programa asociado */
    public int getIdPrograma() { return idPrograma; }
}