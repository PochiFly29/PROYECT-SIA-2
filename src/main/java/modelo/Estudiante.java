package modelo;

import enums.Rol;

import java.util.ArrayList;
import java.util.List;

public class Estudiante extends Usuario {
    private String carrera;
    private double promedio;
    private int semestresCursados;
    private List<Postulacion> postulaciones;

    public Estudiante(String rut, String nombre, String email, String pass, String carrera, double promedio, int semestresCursados) {
        super(rut, nombre, email, pass, Rol.ESTUDIANTE);
        this.carrera = carrera;
        this.promedio = promedio;
        this.semestresCursados = semestresCursados;
        this.postulaciones = new ArrayList<>();
    }

    public void postular(Postulacion postulacion) {
        this.postulaciones.add(postulacion);
    }

    // Getters y Setters
    public String getCarrera() { return carrera; }
    public void setCarrera(String carrera) { this.carrera = carrera; }

    public double getPromedio() { return promedio; }
    public void setPromedio(double promedio) { this.promedio = promedio; }

    public int getSemestresCursados() { return semestresCursados; }
    public void setSemestresCursados(int semestresCursados) { this.semestresCursados = semestresCursados; }

    public List<Postulacion> getPostulaciones() {
        return postulaciones;
    }
    public void setPostulaciones(List<Postulacion> postulaciones) {
        this.postulaciones = postulaciones;
    }
}