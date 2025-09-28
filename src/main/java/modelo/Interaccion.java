package modelo;

import java.time.LocalDateTime;
import enums.TipoInteraccion;

/**
 * Representa una interacción asociada a una postulación.
 * <p>
 * Una interacción puede ser un comentario o un documento, y está asociada a un {@link Usuario} que la crea.
 * </p>
 * <p>
 * Todos los atributos excepto {@code id} son inmutables.
 * El {@code id} es asignado por la base de datos al persistir la interacción.
 * </p>
 */
public class Interaccion {
    private int id;
    private final Usuario autor; // CAMBIO: Inmutable
    private final TipoInteraccion tipo; // CAMBIO: Inmutable
    private final String titulo; // CAMBIO: Inmutable
    private final LocalDateTime fechaHora; // CAMBIO: Inmutable

    /**
     * Constructor principal.
     * Usado para crear interacciones a partir de la base de datos o nuevas instancias.
     *
     * @param id identificador único de la interacción
     * @param autor usuario que genera la interacción
     * @param tipo tipo de interacción ({@link TipoInteraccion})
     * @param titulo título o contenido de la interacción
     * @param fechaHora fecha y hora de creación
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
     * Crea una interacción de tipo comentario con la fecha/hora actual.
     *
     * @param autor usuario que realiza el comentario
     * @param comentario texto del comentario
     * @return nueva instancia de {@code Interaccion} tipo COMENTARIO
     */
    public static Interaccion ofComentario(Usuario autor, String comentario) {
        return new Interaccion(0, autor, TipoInteraccion.COMENTARIO, comentario, LocalDateTime.now());
    }

    /**
     * Crea una interacción de tipo documento con la fecha/hora actual.
     *
     * @param autor usuario que sube el documento
     * @param titulo título del documento
     * @return nueva instancia de {@code Interaccion} tipo DOCUMENTO
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