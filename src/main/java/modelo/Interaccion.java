package modelo;

import java.time.LocalDateTime;
import enums.TipoInteraccion;

public class Interaccion {
    private int id;
    private final Usuario autor; // CAMBIO: Inmutable
    private final TipoInteraccion tipo; // CAMBIO: Inmutable
    private final String titulo; // CAMBIO: Inmutable
    private final LocalDateTime fechaHora; // CAMBIO: Inmutable

    /**
     * Constructor principal. Usado para cargar desde BD o crear nuevas.
     */
    public Interaccion(int id, Usuario autor, TipoInteraccion tipo, String titulo, LocalDateTime fechaHora) {
        this.id = id;
        this.autor = autor;
        this.tipo = tipo;
        this.titulo = titulo;
        this.fechaHora = fechaHora;
    }

    // Métodos estáticos para crear interacciones de forma simple y limpia
    public static Interaccion ofComentario(Usuario autor, String comentario) {
        return new Interaccion(0, autor, TipoInteraccion.COMENTARIO, comentario, LocalDateTime.now());
    }

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