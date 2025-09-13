package modelo;

import java.time.LocalDateTime;
import java.util.UUID;
import enums.TipoInteraccion;

public class Interaccion {
    private String id;
    private Usuario autor;
    private TipoInteraccion tipo;
    private String titulo;
    private String contenido;
    private LocalDateTime fechaHora;

    // Constructor privado que genera el ID único internamente
    public Interaccion(Usuario autor, TipoInteraccion tipo, String titulo, String contenido, LocalDateTime fechaHora) {
        this.id = UUID.randomUUID().toString(); // Genera un ID único automáticamente
        this.autor = autor;
        this.tipo = tipo;
        this.titulo = titulo;
        this.contenido = contenido;
        this.fechaHora = fechaHora;
    }

    /**
     * Crea una nueva interacción de tipo COMENTARIO.
     * @param autor Usuario que realiza el comentario.
     * @param contenido El texto del comentario.
     * @return Una nueva instancia de Interaccion.
     */
    public static Interaccion ofComentario(Usuario autor, String contenido) {
        return new Interaccion(autor, TipoInteraccion.COMENTARIO, "Comentario", contenido, LocalDateTime.now());
    }

    /**
     * Crea una nueva interacción de tipo DOCUMENTO.
     * Se usa para simular la subida de un documento.
     * @param autor Usuario que sube el documento.
     * @param titulo El título del documento (ej: "Copia Cédula").
     * @param contenido La descripción del documento (ej: "Se adjunta copia de mi cédula.").
     * @return Una nueva instancia de Interaccion.
     */
    public static Interaccion ofDocumento(Usuario autor, String titulo, String contenido) {
        return new Interaccion(autor, TipoInteraccion.DOCUMENTO, titulo, contenido, LocalDateTime.now());
    }

    // Getters
    public String getId() { return id; }
    public Usuario getAutor() { return autor; }
    public TipoInteraccion getTipo() { return tipo; }
    public String getTitulo() { return titulo; }
    public String getContenido() { return contenido; }
    public LocalDateTime getFechaHora() { return fechaHora; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setAutor(Usuario autor) { this.autor = autor; }
    public void setTipo(TipoInteraccion tipo) { this.tipo = tipo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
}