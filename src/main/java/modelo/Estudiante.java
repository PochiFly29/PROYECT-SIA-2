package modelo;

import enums.Rol;
import java.util.Map;

public class Estudiante extends Usuario {
    private String carrera;
    private double promedio;
    private int semestresCursados;

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

    public String getCarrera() { return carrera; }
    public void setCarrera(String carrera) { this.carrera = carrera; }
    public double getPromedio() { return promedio; }
    public void setPromedio(double promedio) { this.promedio = promedio; }
    public int getSemestresCursados() { return semestresCursados; }
    public void setSemestresCursados(int semestresCursados) { this.semestresCursados = semestresCursados; }

    @Override
    public String toString() {
        return String.format("%s | Carrera: %s, Promedio: %.2f", super.toString(), carrera, promedio);
    }
}