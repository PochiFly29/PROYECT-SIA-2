package gestores;

import datastore.DataStore;
import enums.EstadoPostulacion;
import enums.Rol;
import modelo.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Servicio encargado de gestionar las postulaciones de estudiantes a convenios
 * dentro de los programas de intercambio.
 * <p>
 * Incluye operaciones para crear postulaciones, actualizar estados,
 * agregar interacciones y manejar la aceptación o rechazo de postulaciones.
 */
public class ServicioPostulacion {
    private final DataStore dataStore;

    /**
     * Constructor que inicializa el servicio con la referencia a {@link DataStore}.
     *
     * @param dataStore fuente centralizada de datos
     */
    public ServicioPostulacion(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Crea una nueva postulación de un estudiante a un convenio dentro de un programa.
     * Evita duplicados en el mismo programa.
     *
     * @param programa   programa al cual se postula
     * @param estudiante estudiante que postula
     * @param convenio   convenio al que se postula
     * @return {@code true} si la postulación fue creada exitosamente,
     *         {@code false} si ya existía una postulación del mismo estudiante al mismo convenio
     * @throws SQLException si ocurre un error en la persistencia
     */
    public boolean crearPostulacion(Programa programa, Estudiante estudiante, Convenio convenio) throws SQLException {
        // Validación para evitar duplicados en el mismo programa
        boolean yaPostulo = programa.getPostulaciones().stream()
                .anyMatch(p -> p.getRutEstudiante().equals(estudiante.getRut()) &&
                        p.getConvenioSeleccionado().equals(convenio));
        if (yaPostulo) {
            return false;
        }

        // CAMBIO: Usamos el constructor de conveniencia de Postulacion.
        Postulacion nuevaPostulacion = new Postulacion(estudiante.getRut(), convenio);

        // La capa de datos la persiste y le asigna un ID
        dataStore.addPostulacion(programa.getId(), nuevaPostulacion);

        // Se añade al programa en memoria para reflejar el estado actual
        // programa.agregarPostulacion(nuevaPostulacion);

        // CAMBIO CRÍTICO: La línea obsoleta "estudiante.agregarPostulacion(...)" se elimina.
        return true;
    }

    /**
     * Actualiza el estado de una postulación específica, tanto en la base de datos como en memoria.
     *
     * @param postulacion  la postulación a modificar
     * @param nuevoEstado  nuevo estado a asignar
     * @throws SQLException si ocurre un error en la persistencia
     */
    public void actualizarEstadoPostulacion(Postulacion postulacion, EstadoPostulacion nuevoEstado) throws SQLException {
        dataStore.actualizarEstadoPostulacion(postulacion.getId(), nuevoEstado);
        postulacion.setEstado(nuevoEstado); // Actualiza el estado en memoria
    }

    /**
     * Agrega una interacción a una postulación y aplica reglas automáticas
     * de cambio de estado según el rol del autor.
     * <ul>
     *   <li>Si el autor es un funcionario, el estado pasa a {@code REVISADA}.</li>
     *   <li>Si el autor es un estudiante, el estado pasa a {@code PENDIENTE}.</li>
     *   <li>Si la postulación ya está en estado final ({@code ACEPTADA} o {@code RECHAZADA}),
     *       no se realizan cambios.</li>
     * </ul>
     *
     * @param postulacion la postulación a la cual se asocia la interacción
     * @param interaccion interacción agregada
     * @throws SQLException si ocurre un error en la persistencia
     */
    public void agregarInteraccion(Postulacion postulacion, Interaccion interaccion) throws SQLException {
        // 1. Persiste la nueva interacción en la base de datos y la caché.
        dataStore.agregarInteraccionAPostulacion(postulacion.getId(), interaccion);

        // 2. LÓGICA DE CAMBIO DE ESTADO AUTOMÁTICO
        EstadoPostulacion estadoActual = postulacion.getEstado();

        // Si la postulación ya está en un estado final, no hacemos nada.
        if (estadoActual == EstadoPostulacion.ACEPTADA || estadoActual == EstadoPostulacion.RECHAZADA) {
            return; // No se puede cambiar un estado terminal.
        }

        // Determinamos el nuevo estado según quién agregó la interacción.
        EstadoPostulacion nuevoEstado = null;
        if (interaccion.getAutor().getRol() == Rol.FUNCIONARIO) {
            nuevoEstado = EstadoPostulacion.REVISADA;
        } else if (interaccion.getAutor().getRol() == Rol.ESTUDIANTE) {
            nuevoEstado = EstadoPostulacion.PENDIENTE;
        }

        // 3. Si se definió un nuevo estado y es diferente al actual, lo actualizamos.
        if (nuevoEstado != null && nuevoEstado != estadoActual) {
            actualizarEstadoPostulacion(postulacion, nuevoEstado);
        }
    }

    /**
     * Acepta una postulación específica y rechaza todas las demás
     * postulaciones del mismo estudiante dentro del mismo programa.
     *
     * @param programa             programa al cual pertenece la postulación
     * @param postulacionAceptada  postulación que será aceptada
     * @throws SQLException si ocurre un error en la persistencia
     */
    public void aceptarPostulacionYRechazarResto(Programa programa, Postulacion postulacionAceptada) throws SQLException {
        // 1. Aceptar la postulación principal
        actualizarEstadoPostulacion(postulacionAceptada, EstadoPostulacion.ACEPTADA);

        // 2. Buscar y rechazar las demás postulaciones del mismo estudiante EN EL MISMO PROGRAMA
        String rutEstudiante = postulacionAceptada.getRutEstudiante();
        programa.getPostulaciones().stream()
                .filter(p -> p.getRutEstudiante().equals(rutEstudiante) && p.getId() != postulacionAceptada.getId())
                .forEach(p -> {
                    try {
                        actualizarEstadoPostulacion(p, EstadoPostulacion.RECHAZADA);
                    } catch (SQLException e) {
                        // Manejar el error, quizás loggearlo o acumular fallos
                        System.err.println("Error al rechazar postulación " + p.getId() + ": " + e.getMessage());
                    }
                });
    }
}