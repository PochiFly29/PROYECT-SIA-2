package modelo;

import enums.Rol;
import java.util.Map;

/**
 * Representa un estudiante dentro del sistema de intercambio académico.
 * <p>
 * Extiende la clase {@link Usuario} y agrega información específica del estudiante:
 * carrera, promedio, semestres cursados y postulaciones asociadas.
 */
public class Estudiante extends Usuario {
    /** Carrera que cursa el estudiante */
    private String carrera;

    /** Promedio académico del estudiante */
    private double promedio;

    /** Cantidad de semestres cursados por el estudiante */
    private int semestresCursados;

    /**
     * Constructor de la clase Estudiante.
     *
     * @param rut RUT del estudiante
     * @param nombre Nombre completo
     * @param email Correo electrónico
     * @param pass Contraseña
     * @param carrera Carrera que cursa
     * @param promedio Promedio académico
     * @param semestresCursados Semestres cursados
     */
    public Estudiante(String rut, String nombre, String email, String pass, String carrera, double promedio, int semestresCursados) {
        super(rut, nombre, email, pass, Rol.ESTUDIANTE);
        this.carrera = carrera;
        this.promedio = promedio;
        this.semestresCursados = semestresCursados;
    }

    // --- Método de Lógica de Negocio (Sobrescrito) ---
    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> data = super.toMap();
        data.put("carrera", carrera);
        data.put("promedio", promedio);
        data.put("semestresCursados", semestresCursados);
        return data;
    }

    // Getters y Setters
    /** @return la carrera del estudiante */
    public String getCarrera() { return carrera; }

    /** Establece la carrera del estudiante */
    public void setCarrera(String carrera) { this.carrera = carrera; }

    /** @return el promedio académico */
    public double getPromedio() { return promedio; }

    /** Establece el promedio académico */
    public void setPromedio(double promedio) { this.promedio = promedio; }

    /** @return los semestres cursados */
    public int getSemestresCursados() { return semestresCursados; }

    /** Establece los semestres cursados */
    public void setSemestresCursados(int semestresCursados) { this.semestresCursados = semestresCursados; }

    @Override
    public String toString() {
        return String.format("%s | Carrera: %s, Promedio: %.2f", super.toString(), carrera, promedio);
    }
}