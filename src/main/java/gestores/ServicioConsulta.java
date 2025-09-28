package gestores;

import datastore.DataStore;
import enums.EstadoPostulacion;
import modelo.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * **Servicio de Dominio para Consultas y Filtrado (Lectura de Datos).**
 * <p>Implementa las operaciones de negocio relacionadas con la búsqueda, recuperación
 * y filtrado de entidades (Programas, Convenios, Postulaciones, etc.).</p>
 * <p>Este servicio solo realiza operaciones de lectura y no modifica el estado de los datos.</p>
 */
public class ServicioConsulta {
    private final DataStore dataStore;

    /**
     * Constructor que inicializa el servicio inyectando la dependencia del {@code DataStore}.
     * @param dataStore La instancia única de {@link DataStore} que contiene la caché de datos.
     */
    public ServicioConsulta(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public Optional<Programa> getProgramaPorId(int id) {
        return Optional.ofNullable(dataStore.getProgramaPorId(id));
    }

    public List<Programa> getTodosLosProgramas() {
        return dataStore.getProgramas();
    }

    public List<Convenio> getTodosLosConvenios() {
        return dataStore.getConvenios();
    }

    /**
     * Busca un convenio específico por su ID.
     * @param idConvenio El ID alfanumérico del convenio.
     * @return Un {@link Optional} que contiene el {@link Convenio} si se encuentra, o vacío si no existe.
     */
    public Optional<Convenio> buscarConvenioPorId(String idConvenio) {
        return Optional.ofNullable(dataStore.getConvenioPorId(idConvenio));
    }

    /**
     * Busca un usuario por RUT y verifica si es una instancia de {@link Estudiante}.
     * @param rut El RUT del usuario.
     * @return Un {@link Optional} que contiene el objeto {@link Estudiante} si existe y su rol lo permite, o vacío.
     */
    public Optional<Estudiante> buscarEstudiantePorRut(String rut) {
        Usuario u = dataStore.getUsuarioPorRut(rut);
        if (u instanceof Estudiante) {
            return Optional.of((Estudiante) u);
        }
        return Optional.empty();
    }

    public List<Postulacion> getPostulacionesFiltradas(Programa programa, String tipoFiltro, String valorFiltro) {
        Stream<Postulacion> stream = programa.getPostulaciones().stream();

        if (tipoFiltro == null || tipoFiltro.isEmpty() || valorFiltro == null || valorFiltro.isEmpty()) {
            return programa.getPostulaciones();
        }

        switch (tipoFiltro.toLowerCase()) {
            case "rut":
                return stream.filter(p -> p.getRutEstudiante().equals(valorFiltro)).collect(Collectors.toList());
            case "estado":
                try {
                    EstadoPostulacion estado = EstadoPostulacion.valueOf(valorFiltro.toUpperCase());
                    return stream.filter(p -> p.getEstado() == estado).collect(Collectors.toList());
                } catch (IllegalArgumentException e) {
                    return List.of(); // Devuelve lista vacía si el estado no es válido
                }
            case "convenio":
                // CAMBIO CRÍTICO: Se busca por el ID dentro del objeto Convenio
                return stream.filter(p -> p.getConvenioSeleccionado().getId().equals(valorFiltro)).collect(Collectors.toList());
            default:
                return programa.getPostulaciones();
        }
    }

    public Optional<Programa> getProgramaActivo() {
        // Busca en la caché de DataStore el único programa con estado "ACTIVO"
        return dataStore.getProgramas().stream()
                .filter(p -> "ACTIVO".equals(p.getEstado()))
                .findFirst();
    }

}