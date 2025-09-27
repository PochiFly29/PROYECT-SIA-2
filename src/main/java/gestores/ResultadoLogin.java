package gestores;

import modelo.Usuario;

/**
 * Representa el resultado de un intento de inicio de sesión en el sistema.
 * <p>
 * Contiene información sobre si el login fue exitoso, un mensaje descriptivo
 * y, en caso de éxito, el usuario autenticado.
 */
public class ResultadoLogin {

    /** Indica si el inicio de sesión fue exitoso */
    private final boolean exito;

    /** Mensaje descriptivo del resultado */
    private final String mensaje;

    /** Usuario autenticado (solo disponible si el login fue exitoso) */
    private final Usuario usuario;

    // Constructor para éxito
    /**
     * Constructor para un inicio de sesión exitoso.
     *
     * @param usuario el usuario que inició sesión correctamente
     */
    public ResultadoLogin(Usuario usuario) {
        this.exito = true;
        this.mensaje = "Inicio de sesión exitoso.";
        this.usuario = usuario;
    }

    // Constructor para fracaso
    /**
     * Constructor para un inicio de sesión fallido.
     *
     * @param mensaje el mensaje de error que describe la razón del fallo
     */
    public ResultadoLogin(String mensaje) {
        this.exito = false;
        this.mensaje = mensaje;
        this.usuario = null;
    }

    // Getters
    /**
     * Devuelve si el inicio de sesión fue exitoso.
     *
     * @return {@code true} si el login fue exitoso, {@code false} en caso contrario
     */
    public boolean isExito() { return exito; }

    /**
     * Devuelve el mensaje descriptivo del resultado del login.
     *
     * @return mensaje del resultado
     */
    public String getMensaje() { return mensaje; }

    /**
     * Devuelve el usuario autenticado.
     *
     * @return el usuario si el login fue exitoso, {@code null} si falló
     */
    public Usuario getUsuario() { return usuario; }
}