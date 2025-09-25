package modelo;

import java.time.LocalDateTime;
import java.util.UUID;
import enums.TipoInteraccion;

public class Interaccion {
    private String id;
    private Usuario autor;
    private TipoInteraccion tipo;
    private String titulo;
    private LocalDateTime fechaHora;
    private String rutaArchivo;

    // Constructor para la creación de interacciones
    public Interaccion(Usuario autor, TipoInteraccion tipo, String titulo, LocalDateTime fechaHora, String rutaArchivo) {
        this.id = UUID.randomUUID().toString();
        this.autor = autor;
        this.tipo = tipo;
        this.titulo = titulo;
        this.fechaHora = fechaHora;
        this.rutaArchivo = rutaArchivo;
    }

    // Constructor para la carga desde la base de datos
    public Interaccion(String id, Usuario autor, TipoInteraccion tipo, String titulo, LocalDateTime fechaHora, String rutaArchivo) {
        this.id = id;
        this.autor = autor;
        this.tipo = tipo;
        this.titulo = titulo;
        this.fechaHora = fechaHora;
        this.rutaArchivo = rutaArchivo;
    }

    // Métodos estáticos para crear interacciones de forma simple
    public static Interaccion ofComentario(Usuario autor, String comentario) {
        return new Interaccion(autor, TipoInteraccion.COMENTARIO, comentario, LocalDateTime.now(), null);
    }

    public static Interaccion ofDocumento(Usuario autor, String titulo) {
        return new Interaccion(autor, TipoInteraccion.DOCUMENTO, titulo, LocalDateTime.now(), null);
    }

    // Getters y Setters
    public String getId() { return id; }
    public Usuario getAutor() { return autor; }
    public TipoInteraccion getTipo() { return tipo; }
    public String getTitulo() { return titulo; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }
}