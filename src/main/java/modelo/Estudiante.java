package modelo;

import enums.Rol;
import java.util.Map;

/**
 * Representa un estudiante, que es un tipo especializado de {@link Usuario}.
 * <p>
 * Además de los atributos heredados de {@code Usuario}, un estudiante posee información académica:
 * carrera, promedio y cantidad de semestres cursados.
 * </p>
 */
public class Estudiante extends Usuario {
    private String carrera;
    private double promedio;
    private int semestresCursados;

    /**
     * Crea un nuevo estudiante con sus datos personales y académicos.
     *
     * @param rut identificador único del estudiante
     * @param nombre nombre completo
     * @param email correo electrónico
     * @param pass contraseña
     * @param carrera carrera que cursa el estudiante
     * @param promedio promedio académico
     * @param semestresCursados cantidad de semestres que ha cursado
     */
    public Estudiante(String rut, String nombre, String email, String pass, String carrera, double promedio, int semestresCursados) {
        super(rut, nombre, email, pass, Rol.ESTUDIANTE);
        this.carrera = carrera;
        this.promedio = promedio;
        this.semestresCursados = semestresCursados;
    }

    // --- Método de Lógica de Negocio (Sobrescrito) ---
    /**
     * Convierte los datos del estudiante en un mapa clave-valor.
     * <p>
     * Sobrescribe el método {@link Usuario#toMap()} para incluir los atributos académicos.
     * </p>
     *
     * @return mapa con la información del estudiante
     */
    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> data = super.toMap();
        data.put("carrera", carrera);
        data.put("promedio", promedio);
        data.put("semestresCursados", semestresCursados);
        return data;
    }

    public String getCarrera() { return carrera; }
    public void setCarrera(String carrera) { this.carrera = carrera; }
    public double getPromedio() { return promedio; }
    public void setPromedio(double promedio) { this.promedio = promedio; }
    public int getSemestresCursados() { return semestresCursados; }
    public void setSemestresCursados(int semestresCursados) { this.semestresCursados = semestresCursados; }

    /**
     * Representación en texto del estudiante.
     * Incluye los datos heredados de {@code Usuario} más la carrera y promedio.
     *
     * @return cadena representativa del estudiante
     */
    @Override
    public String toString() {
        return String.format("%s | Carrera: %s, Promedio: %.2f", super.toString(), carrera, promedio);
    }
}