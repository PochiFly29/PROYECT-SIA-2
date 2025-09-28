package gestores;

import datastore.DataStore;
import enums.EstadoPostulacion;
import enums.Rol;
import modelo.*;
import java.sql.SQLException;
import java.util.List;

/**
 * **Servicio de Dominio para la Gestión del Ciclo de Vida de las Postulaciones.**
 * <p>Implementa la lógica de negocio relacionada con la creación, la actualización
 * de estado, la gestión de interacciones/logs, y las reglas complejas de
 * aceptación/rechazo de {@link Postulacion}es.</p>
 */
public class ServicioPostulacion {
    private final DataStore dataStore;
    private final ServicioConsulta servicioConsulta;

    /**
     * Constructor que inicializa el servicio inyectando las dependencias.
     * @param dataStore La instancia única de {@link DataStore}.
     * @param servicioConsulta El {@link ServicioConsulta} para operaciones de lectura.
     */
    public ServicioPostulacion(DataStore dataStore,ServicioConsulta servicioConsulta) {
        this.dataStore = dataStore;
        this.servicioConsulta = servicioConsulta;
    }

    /**
     * Intenta crear una nueva postulación para un estudiante y convenio específicos
     * en el programa dado.
     * <p>Regla de Negocio: Evita que el mismo estudiante postule dos veces al mismo
     * convenio dentro del mismo programa.</p>
     * @param programa El {@link Programa} activo al cual se postula.
     * @param estudiante El {@link Estudiante} que realiza la postulación.
     * @param convenio El {@link Convenio} al que se postula.
     * @return {@code true} si la postulación fue creada y persistida exitosamente,
     * {@code false} si ya existía una postulación idéntica.
     * @throws SQLException Si ocurre un error al persistir la nueva postulación.
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
     * Actualiza el estado de una postulación en la base de datos y en la caché.
     * <p>Este método es la vía principal para cambiar el estado de la postulación
     * por parte de un Funcionario o un proceso automático.</p>
     * @param postulacion La {@link Postulacion} cuyo estado debe cambiar (objeto en caché).
     * @param nuevoEstado El nuevo estado a asignar (debe ser un estado válido).
     * @throws SQLException Si ocurre un error al actualizar el registro en la base de datos.
     */
    public void actualizarEstadoPostulacion(Postulacion postulacion, EstadoPostulacion nuevoEstado) throws SQLException {
        dataStore.actualizarEstadoPostulacion(postulacion.getId(), nuevoEstado);
        postulacion.setEstado(nuevoEstado); // Actualiza el estado en memoria
    }

    /**
     * Agrega una nueva interacción (comentario, log) a una postulación.
     * <p>Regla de Negocio: Dispara un cambio de estado automático basado en el rol del autor.</p>
     * <ul>
     * <li>Si el autor es un **Funcionario**, el estado cambia a {@code REVISADA}.</li>
     * <li>Si el autor es un **Estudiante**, el estado cambia a {@code PENDIENTE}.</li>
     * <li>Si el estado actual es terminal (ACEPTADA o RECHAZADA), el estado no cambia.</li>
     * </ul>
     * @param postulacion La {@link Postulacion} a la cual agregar la interacción.
     * @param interaccion El objeto {@link Interaccion} a registrar.
     * @throws SQLException Si ocurre un error al persistir la interacción o el cambio de estado.
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
     * Implementa la regla de negocio crítica: Aceptar una postulación y
     * **rechazar automáticamente todas las demás postulaciones del mismo estudiante** * en el programa activo.
     * @param postulacionAceptada La {@link Postulacion} que ha sido seleccionada.
     * @throws SQLException Si ocurre un error de persistencia.
     * @throws IllegalStateException Si no se encuentra un programa activo para buscar las postulaciones.
     */
    public void aceptarPostulacionYRechazarResto(Postulacion postulacionAceptada) throws SQLException {
        // CAMBIO: Llama a su dependencia directa, no al gestor.
        Programa programaActivo = servicioConsulta.getProgramaActivo()
                .orElseThrow(() -> new IllegalStateException("No se encontró un programa activo."));

        actualizarEstadoPostulacion(postulacionAceptada, EstadoPostulacion.ACEPTADA);

        String rutEstudiante = postulacionAceptada.getRutEstudiante();
        programaActivo.getPostulaciones().stream()
                .filter(p -> p.getRutEstudiante().equals(rutEstudiante) && p.getId() != postulacionAceptada.getId())
                .forEach(p -> {
                    try {
                        actualizarEstadoPostulacion(p, EstadoPostulacion.RECHAZADA);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
    }
}