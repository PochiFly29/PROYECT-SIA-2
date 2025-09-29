// En ServicioConvenio.java
package gestores;

import datastore.DataStore;
import enums.EstadoPostulacion;
import modelo.Convenio;
import modelo.Interaccion;
import modelo.Postulacion;
import modelo.Usuario;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * **Servicio de Dominio para la Administración de Convenios de Intercambio.**
 * <p>Implementa las operaciones de negocio relacionadas con el ciclo de vida
 * de los {@link Convenio}s (Crear y Eliminar), asegurando la coherencia de datos
 * al interactuar con las {@link Postulacion}es dependientes.</p>
 */
public class ServicioConvenio {
    private final DataStore dataStore;
    private final ServicioPostulacion servicioPostulacion;

    /**
     * Constructor que inicializa el servicio inyectando las dependencias necesarias.
     * @param dataStore La instancia única de {@link DataStore}.
     * @param servicioPostulacion El {@link ServicioPostulacion} para gestionar los efectos colaterales.
     */
    public ServicioConvenio(DataStore dataStore, ServicioPostulacion servicioPostulacion) {
        this.dataStore = dataStore;
        this.servicioPostulacion = servicioPostulacion;
    }

    /**
     * Crea un nuevo convenio en el sistema, persistiendo el objeto en la base de datos.
     * @param convenio El objeto {@link Convenio} a crear.
     * @throws SQLException Si ocurre un error de persistencia en la base de datos.
     */
    public void crearConvenio(Convenio convenio) throws SQLException {
        dataStore.crearConvenio(convenio);
    }

    /**
     * Elimina un convenio del sistema, aplicando una regla de negocio crucial:
     * **Todas las postulaciones activas a dicho convenio son automáticamente rechazadas.**
     * <p>Reglas de negocio aplicadas:</p>
     * <ul>
     * <li>Solo se afectan postulaciones en estados NO finales (PENDIENTE, EN_REVISION, etc.).</li>
     * <li>A cada postulación afectada se le añade una {@link Interaccion} de log con la razón del rechazo.</li>
     * <li>El estado final de las postulaciones afectadas es {@code EstadoPostulacion.RECHAZADA}.</li>
     * </ul>
     * @param convenio El objeto {@link Convenio} a eliminar.
     * @param auditor El {@link Usuario} (típicamente Funcionario o Auditor) que realiza la acción de eliminación, usado como autor del log de rechazo.
     * @throws SQLException Si ocurre un error durante la actualización de postulaciones o la eliminación del convenio.
     */
    public void eliminarConvenio(Convenio convenio, Usuario auditor) throws SQLException {
        // 1. Encontrar todas las postulaciones activas para este convenio
        List<Postulacion> postulacionesAfectadas = dataStore.getProgramas().stream()
                .flatMap(p -> p.getPostulaciones().stream())
                .filter(p -> p.getConvenioSeleccionado().equals(convenio))
                .filter(p -> p.getEstado() != EstadoPostulacion.ACEPTADA && p.getEstado() != EstadoPostulacion.RECHAZADA)
                .collect(Collectors.toList());

        // 2. Rechazarlas y añadir un comentario del auditor
        for (Postulacion p : postulacionesAfectadas) {
            String comentario = "Postulación rechazada automáticamente debido a la eliminación del convenio: " + convenio.getId();
            Interaccion inter = Interaccion.ofComentario(auditor, comentario);
            servicioPostulacion.agregarInteraccion(p, inter);
            servicioPostulacion.actualizarEstadoPostulacion(p, EstadoPostulacion.RECHAZADA);
        }

        // 3. Eliminar el convenio de la base de datos
        dataStore.eliminarConvenio(convenio.getId());
    }
}