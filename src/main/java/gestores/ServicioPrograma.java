package gestores;

import datastore.DataStore;
import enums.EstadoPostulacion;
import modelo.Programa;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class ServicioPrograma {
    private final DataStore dataStore;
    private final ServicioPostulacion servicioPostulacion;

    public ServicioPrograma(DataStore dataStore, ServicioPostulacion servicioPostulacion) {
        this.dataStore = dataStore;
        this.servicioPostulacion = servicioPostulacion;
    }

    public Optional<Programa> getProgramaActivo() {
        return dataStore.getProgramas().stream()
                .filter(p -> "ACTIVO".equals(p.getEstado()))
                .findFirst();
    }

    public void crearPrograma(String nombre, LocalDate fechaInicio, LocalDate fechaFin) throws SQLException, IllegalStateException {
        if (getProgramaActivo().isPresent()) {
            throw new IllegalStateException("No se puede crear un nuevo programa mientras otro está activo. Debe finalizar el actual primero.");
        }

        // CAMBIO: Se llama al constructor correcto con 3 parámetros.
        Programa nuevoPrograma = new Programa(nombre, fechaInicio, fechaFin);

        dataStore.crearPrograma(nuevoPrograma);
    }

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

    public void eliminarPrograma(int idPrograma) throws SQLException {
        // La confirmación se hace en la UI, aquí solo ejecutamos.
        dataStore.eliminarPrograma(idPrograma);
    }
}