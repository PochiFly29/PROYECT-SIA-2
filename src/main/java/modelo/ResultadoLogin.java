package modelo;

/**
 * Representa el resultado de un intento de inicio de sesión.
 * <p>
 * Contiene información sobre si la autenticación fue exitosa, un mensaje descriptivo
 * y, en caso de éxito, el usuario autenticado.
 * </p>
 */
public class ResultadoLogin {
    private final boolean exito;
    private final String mensaje;
    private final Usuario usuario; // ¡CORREGIDO: Tipo Usuario!

    // Constructor para éxito
    /**
     * Constructor para un inicio de sesión exitoso.
     *
     * @param usuario el usuario que ha iniciado sesión correctamente
     */
    public ResultadoLogin(Usuario usuario) { // ¡CORREGIDO: Recibe Usuario!
        this.exito = true;
        this.mensaje = "Inicio de sesión exitoso.";
        this.usuario = usuario;
    }

    // Constructor para fracaso (no cambia)
    /**
     * Constructor para un inicio de sesión fallido.
     *
     * @param mensaje mensaje que describe la razón del fallo
     */
    public ResultadoLogin(String mensaje) {
        this.exito = false;
        this.mensaje = mensaje;
        this.usuario = null;
    }

    // Getters
    /**
     * Indica si el inicio de sesión fue exitoso.
     *
     * @return {@code true} si la autenticación fue correcta, {@code false} en caso contrario
     */
    public boolean isExito() { return exito; }
    public String getMensaje() { return mensaje; }
    public Usuario getUsuario() { return usuario; } // ¡CORREGIDO: Retorna Usuario!
}