package gestores;

import modelo.Usuario;

public class ResultadoLogin {
    private final boolean exito;
    private final String mensaje;
    private final Usuario usuario;

    // Constructor para éxito
    public ResultadoLogin(Usuario usuario) {
        this.exito = true;
        this.mensaje = "Inicio de sesión exitoso.";
        this.usuario = usuario;
    }

    // Constructor para fracaso
    public ResultadoLogin(String mensaje) {
        this.exito = false;
        this.mensaje = mensaje;
        this.usuario = null;
    }

    // Getters
    public boolean isExito() { return exito; }
    public String getMensaje() { return mensaje; }
    public Usuario getUsuario() { return usuario; }
}