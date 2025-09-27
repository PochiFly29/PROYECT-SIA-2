package modelo;

import java.time.LocalDateTime;
import java.util.UUID;
import enums.TipoInteraccion;

/**
 * Representa una interacción asociada a una postulación dentro del sistema.
 * <p>
 * Una interacción puede ser un comentario o un documento, con información
 * sobre el autor, tipo, título, fecha/hora y ruta de archivo (opcional).
 */
public class Interaccion {
    /** Identificador único de la interacción */
    private String id;

    /** Usuario que realizó la interacción */
    private Usuario autor;

    /** Tipo de interacción (COMENTARIO o DOCUMENTO) */
    private TipoInteraccion tipo;

    /** Título o contenido de la interacción */
    private String titulo;

    /** Fecha y hora en que se realizó la interacción */
    private LocalDateTime fechaHora;

    /** Ruta del archivo asociado, si corresponde */
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
    /**
     * Constructor principal para la creación de interacciones.
     * Genera un ID único automáticamente.
     *
     * @param autor Usuario que realiza la interacción
     * @param tipo Tipo de interacción
     * @param titulo Título o contenido de la interacción
     * @param fechaHora Fecha y hora de la interacción
     * @param rutaArchivo Ruta del archivo asociado (puede ser null)
     */
    public Interaccion(String id, Usuario autor, TipoInteraccion tipo, String titulo, LocalDateTime fechaHora, String rutaArchivo) {
        this.id = id;
        this.autor = autor;
        this.tipo = tipo;
        this.titulo = titulo;
        this.fechaHora = fechaHora;
        this.rutaArchivo = rutaArchivo;
    }

    // Métodos estáticos para crear interacciones de forma simple
    /**
     * Crea una interacción de tipo COMENTARIO de forma simple.
     *
     * @param autor Usuario que realiza el comentario
     * @param comentario Texto del comentario
     * @return Nueva instancia de Interaccion
     */
    public static Interaccion ofComentario(Usuario autor, String comentario) {
        return new Interaccion(autor, TipoInteraccion.COMENTARIO, comentario, LocalDateTime.now(), null);
    }

    /**
     * Crea una interacción de tipo DOCUMENTO de forma simple.
     *
     * @param autor Usuario que sube el documento
     * @param titulo Título del documento
     * @return Nueva instancia de Interaccion
     */
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