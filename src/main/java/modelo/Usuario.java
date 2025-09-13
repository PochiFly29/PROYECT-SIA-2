package modelo;

import enums.*;

public class Usuario {
    protected String rut;
    protected String nombreCompleto;
    protected String email;
    protected String pass;
    protected boolean bloqueado;
    protected int intentosFallidos;
    protected Rol rol;

    public Usuario(String rut, String nombreCompleto, String email, String pass, Rol rol) {
        this.rut = rut;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.pass = pass;
        this.rol = rol;
        this.bloqueado = false;
        this.intentosFallidos = 0;
    }


    public boolean validarCredenciales(String pass) {
        return pass.equals(this.pass);
    }

    // Getters y Setters
    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPass() { return pass; }
    public void setPass(String pass) { this.pass = pass; }

    public boolean isBloqueado() { return bloqueado; }
    public void setBloqueado(boolean bloqueado) { this.bloqueado = bloqueado; }

    public int getIntentosFallidos() { return intentosFallidos; }
    public void setIntentosFallidos(int intentosFallidos) { this.intentosFallidos = intentosFallidos; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
}