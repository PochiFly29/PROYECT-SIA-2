package modelo;

public class Convenio {
    private String id;
    private String universidad;
    private String pais;
    private String area; // ¡Atributo 'area' agregado!
    private String requisitosAcademicos;
    private String requisitosEconomicos;
    private int idPrograma;

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
    public String getId() { return id; }
    public String getUniversidad() { return universidad; }
    public String getPais() { return pais; }
    public String getArea() { return area; }
    public String getRequisitosAcademicos() { return requisitosAcademicos; }
    public String getRequisitosEconomicos() { return requisitosEconomicos; }
    public int getIdPrograma() { return idPrograma; }
}