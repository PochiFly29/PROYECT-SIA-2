package gestores;

import datastore.DataStore;
import enums.EstadoPostulacion;
import modelo.Programa;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

/**
 * **Servicio de Dominio para la Gestión del Ciclo de Vida de los Programas de Intercambio.**
 * <p>Implementa las operaciones de negocio relacionadas con la creación, activación,
 * finalización y eliminación de los objetos {@link Programa}.</p>
 * <p>Aplica la regla de negocio fundamental de que solo puede existir **un programa activo** * a la vez.</p>
 */

public class ServicioPrograma {
    private final DataStore dataStore;
    private final ServicioPostulacion servicioPostulacion;

    /**
     * Constructor que inicializa el servicio inyectando las dependencias.
     * @param dataStore La instancia única de {@link DataStore}.
     * @param servicioPostulacion El {@link ServicioPostulacion} para gestionar los efectos colaterales.
     */
    public ServicioPrograma(DataStore dataStore, ServicioPostulacion servicioPostulacion) {
        this.dataStore = dataStore;
        this.servicioPostulacion = servicioPostulacion;
    }

    public Optional<Programa> getProgramaActivo() {
        return dataStore.getProgramas().stream()
                .filter(p -> "ACTIVO".equals(p.getEstado()))
                .findFirst();
    }

    /**
     * Crea un nuevo programa de intercambio, siempre que no haya ya un programa activo.
     * <p>Regla de Negocio: Solo se permite un programa activo simultáneamente.</p>
     * @param nombre El nombre del nuevo programa.
     * @param fechaInicio La fecha de inicio de la postulación.
     * @param fechaFin La fecha de fin de la postulación.
     * @throws SQLException Si ocurre un error al persistir el programa.
     * @throws IllegalStateException Si ya existe un programa activo en el sistema.
     */
    public void crearPrograma(String nombre, LocalDate fechaInicio, LocalDate fechaFin) throws SQLException, IllegalStateException {
        if (getProgramaActivo().isPresent()) {
            throw new IllegalStateException("No se puede crear un nuevo programa mientras otro está activo. Debe finalizar el actual primero.");
        }

        // CAMBIO: Se llama al constructor correcto con 3 parámetros.
        Programa nuevoPrograma = new Programa(nombre, fechaInicio, fechaFin);

        dataStore.crearPrograma(nuevoPrograma);
    }

    /**
     * Finaliza un {@link Programa} activo, aplicando la regla de negocio de saneamiento.
     * <p>Regla de Negocio de Saneamiento:</p>
     * <ul>
     * <li>Todas las postulaciones que no están en estado **ACEPTADA** son automáticamente **RECHAZADAS**.</li>
     * <li>El estado del programa se cambia a **FINALIZADO** y se persiste.</li>
     * </ul>
     * @param programa El {@link Programa} a finalizar (objeto en caché).
     * @throws SQLException Si ocurre un error durante la actualización de las postulaciones o el programa.
     */
    public void finalizarPrograma(Programa programa) throws SQLException {
        // 1. Rechazar todas las postulaciones que no estén ACEPTADAS
        programa.getPostulaciones().stream()
                .filter(p -> p.getEstado() != EstadoPostulacion.ACEPTADA)
                .forEach(p -> {
                    try {
                        servicioPostulacion.actualizarEstadoPostulacion(p, EstadoPostulacion.RECHAZADA);
                    } catch (SQLException e) {
                        // Manejar el error, por ahora lo imprimimos
                        e.printStackTrace();
                    }
                });

        // 2. Cambiar el estado del programa a FINALIZADO
        programa.setEstado("FINALIZADO");
        dataStore.actualizarPrograma(programa);
    }

    /**
     * Elimina un programa de la base de datos y de la caché.
     * <p>Esta operación es destructiva y debería ser precedida por una confirmación
     * en la capa de interfaz. Se espera que la BD maneje la eliminación en cascada
     * de postulaciones e interacciones.</p>
     * @param idPrograma El ID del programa a eliminar.
     * @throws SQLException Si falla la eliminación en la base de datos.
     */
    public void eliminarPrograma(int idPrograma) throws SQLException {
        // La confirmación se hace en la UI, aquí solo ejecutamos.
        dataStore.eliminarPrograma(idPrograma);
    }
}