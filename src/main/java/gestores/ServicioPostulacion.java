package gestores;

import datastore.DataStore;
import enums.EstadoPostulacion;
import enums.Rol;
import modelo.*;
import java.sql.SQLException;
import java.util.List;

public class ServicioPostulacion {
    private final DataStore dataStore;
    private final ServicioConsulta servicioConsulta;

    public ServicioPostulacion(DataStore dataStore,ServicioConsulta servicioConsulta) {
        this.dataStore = dataStore;
        this.servicioConsulta = servicioConsulta;
    }

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

    public void actualizarEstadoPostulacion(Postulacion postulacion, EstadoPostulacion nuevoEstado) throws SQLException {
        dataStore.actualizarEstadoPostulacion(postulacion.getId(), nuevoEstado);
        postulacion.setEstado(nuevoEstado); // Actualiza el estado en memoria
    }

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