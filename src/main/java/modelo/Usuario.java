package modelo;

import enums.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Representa un usuario del sistema.
 * <p>
 * Contiene información básica de autenticación, rol, estado de bloqueo
 * y número de intentos fallidos.
 * </p>
 */
public class Usuario {
    protected String rut;
    protected String nombreCompleto;
    protected String email;
    protected String pass;
    protected boolean bloqueado;
    protected int intentosFallidos;
    protected Rol rol;

    /**
     * Constructor principal de Usuario.
     *
     * @param rut           RUT del usuario (identificador único)
     * @param nombreCompleto Nombre completo del usuario
     * @param email         Correo electrónico
     * @param pass          Contraseña
     * @param rol           Rol del usuario (ESTUDIANTE, FUNCIONARIO, etc.)
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

    // --- Métodos de Infraestructura (CRUCIALES) ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(rut, usuario.rut);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rut);
    }

    // --- Método de Lógica de Negocio (Polimórfico) ---
    /**
     * Convierte los atributos del usuario en un mapa para uso genérico.
     *
     * @return mapa con clave-valor de los atributos principales
     */
    public Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("rut", rut);
        data.put("nombre", nombreCompleto);
        data.put("email", email);
        data.put("rol", rol.name());
        data.put("bloqueado", bloqueado);
        data.put("intentosFallidos", intentosFallidos);
        return data;
    }

    /**
     * Valida si la contraseña ingresada coincide con la del usuario.
     *
     * @param pass contraseña a validar
     * @return true si coincide, false en caso contrario
     */
    public boolean validarCredenciales(String pass) { return pass.equals(this.pass); }

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
    public void incrementarIntentosFallidos() { this.intentosFallidos++; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    @Override
    public String toString() {
        return String.format("Usuario [Rut: %s, Nombre: %s, Rol: %s]", rut, nombreCompleto, rol.name());
    }
}