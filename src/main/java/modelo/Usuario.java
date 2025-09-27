package modelo;

import enums.*;

/**
 * Clase base que representa a un usuario del sistema.
 * <p>
 * Cada usuario tiene un RUT, nombre completo, email, contraseña, rol, estado de bloqueo
 * y cantidad de intentos fallidos de inicio de sesión.
 */
public class Usuario {
    /** RUT único del usuario */
    protected String rut;

    /** Nombre completo del usuario */
    protected String nombreCompleto;

    /** Correo electrónico del usuario */
    protected String email;

    /** Contraseña del usuario */
    protected String pass;

    /** Indica si el usuario está bloqueado por múltiples intentos fallidos */
    protected boolean bloqueado;

    /** Cantidad de intentos fallidos de inicio de sesión */
    protected int intentosFallidos;

    /** Rol del usuario dentro del sistema */
    protected Rol rol;

    /**
     * Constructor principal de Usuario.
     *
     * @param rut RUT del usuario
     * @param nombreCompleto Nombre completo
     * @param email Correo electrónico
     * @param pass Contraseña
     * @param rol Rol del usuario
     */
    public Usuario(String rut, String nombreCompleto, String email, String pass, Rol rol) {
        this.rut = rut;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.pass = pass;
        this.rol = rol;
        this.bloqueado = false;
        this.intentosFallidos = 0;
    }


    /**
     * Valida si la contraseña ingresada coincide con la del usuario.
     *
     * @param pass Contraseña a validar
     * @return true si es correcta, false si no
     */
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
    // todo sobrecarga

    /**
     * Establece la cantidad de intentos fallidos.
     *
     * @param intentosFallidos Nueva cantidad de intentos
     */
    public void setIntentosFallidos(int intentosFallidos) { this.intentosFallidos = intentosFallidos; }

    /**
     * Incrementa en 1 la cantidad de intentos fallidos.
     */
    public void setIntentosFallidos() { this.intentosFallidos += 1; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
/*
public class Funcionario extends Usuario {
    public Funcionario(String rut, String nombreCompleto, String email, String pass) {
        super(rut, nombreCompleto, email, pass, Rol.FUNCIONARIO);
    }
}

 */
}