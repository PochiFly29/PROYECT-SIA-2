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

public class ServicioConvenio {
    private final DataStore dataStore;
    private final ServicioPostulacion servicioPostulacion;

    public ServicioConvenio(DataStore dataStore, ServicioPostulacion servicioPostulacion) {
        this.dataStore = dataStore;
        this.servicioPostulacion = servicioPostulacion;
    }

    public void crearConvenio(Convenio convenio) throws SQLException {
        dataStore.crearConvenio(convenio);
    }

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