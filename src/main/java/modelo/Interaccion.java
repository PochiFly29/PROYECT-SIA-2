package modelo;

import java.time.LocalDateTime;
import enums.TipoInteraccion;

/**
 * Representa una interacción asociada a una postulación dentro del sistema.
 * <p>
 * Una interacción puede ser un comentario o un documento, con información
 * sobre el autor, tipo, título, fecha/hora y ruta de archivo (opcional).
 */
public class Interaccion {
    /** Identificador único de la interacción */
    private int id;

    /** Usuario que realizó la interacción */
    private final Usuario autor;

    /** Tipo de interacción (COMENTARIO o DOCUMENTO) */
    private final TipoInteraccion tipo;

    /** Título o contenido de la interacción */
    private final String titulo;

    /** Fecha y hora en que se realizó la interacción */
    private final LocalDateTime fechaHora;

    /**
     * Constructor principal para la creación de interacciones.
     * Genera un ID único automáticamente.
     *
     * @param autor Usuario que realiza la interacción
     * @param tipo Tipo de interacción
     * @param titulo Título o contenido de la interacción
     * @param fechaHora Fecha y hora de la interacción
     */
    public Interaccion(int id, Usuario autor, TipoInteraccion tipo, String titulo, LocalDateTime fechaHora) {
        this.id = id;
        this.autor = autor;
        this.tipo = tipo;
        this.titulo = titulo;
        this.fechaHora = fechaHora;
    }

    // Métodos estáticos para crear interacciones de forma simple y limpia
    /**
     * Crea una interacción de tipo COMENTARIO de forma simple.
     *
     * @param autor Usuario que realiza el comentario
     * @param comentario Texto del comentario
     * @return Nueva instancia de Interaccion
     */
    public static Interaccion ofComentario(Usuario autor, String comentario) {
        return new Interaccion(0, autor, TipoInteraccion.COMENTARIO, comentario, LocalDateTime.now());
    }

    /**
     * Crea una interacción de tipo DOCUMENTO de forma simple.
     *
     * @param autor Usuario que sube el documento
     * @param titulo Título del documento
     * @return Nueva instancia de Interaccion
     */
    public static Interaccion ofDocumento(Usuario autor, String titulo) {
        return new Interaccion(0, autor, TipoInteraccion.DOCUMENTO, titulo, LocalDateTime.now());
    }

    // Getters
    public int getId() { return id; }
    public Usuario getAutor() { return autor; }
    public TipoInteraccion getTipo() { return tipo; }
    public String getTitulo() { return titulo; }
    public LocalDateTime getFechaHora() { return fechaHora; }

    // Setter solo para el ID, que es asignado por la capa de persistencia
    public void setId(int id) { this.id = id; }
}