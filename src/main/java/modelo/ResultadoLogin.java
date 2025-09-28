package modelo;

/**
 * **Objeto de Valor (Value Object) que encapsula el resultado de una operación de inicio de sesión.**
 * <p>Permite al {@link gestores.ServicioAutenticacion} retornar de manera segura y unificada el
 * éxito o fracaso de la autenticación, transportando el mensaje y el objeto
 * {@link Usuario} autenticado.</p>
 */
public class ResultadoLogin {
    private final boolean exito;
    private final String mensaje;
    private final Usuario usuario; // ¡CORREGIDO: Tipo Usuario!

    // Constructor para éxito
    /**
     * Constructor para un resultado de login **exitoso**.
     * @param usuario El {@link Usuario} que fue validado correctamente.
     */
    public ResultadoLogin(Usuario usuario) { // ¡CORREGIDO: Recibe Usuario!
        this.exito = true;
        this.mensaje = "Inicio de sesión exitoso.";
        this.usuario = usuario;
    }

    // Constructor para fracaso (no cambia)
    /**
     * Constructor para un resultado de login **fallido**.
     * @param mensaje El mensaje de error que explica la razón del fracaso (ej. "Contraseña incorrecta").
     */
    public ResultadoLogin(String mensaje) {
        this.exito = false;
        this.mensaje = mensaje;
        this.usuario = null;
    }

    // Getters
    public boolean isExito() { return exito; }
    public String getMensaje() { return mensaje; }
    public Usuario getUsuario() { return usuario; } // ¡CORREGIDO: Retorna Usuario!
}