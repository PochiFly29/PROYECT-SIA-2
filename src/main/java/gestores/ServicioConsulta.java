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
 * Servicio que centraliza las consultas sobre programas,
 * convenios, estudiantes y postulaciones dentro del sistema.
 */
public class ServicioConsulta {
    private final DataStore dataStore;

    /**
     * Constructor que inicializa el servicio con una referencia
     * al {@link DataStore} para acceder a los datos.
     *
     * @param dataStore fuente centralizada de datos
     */
    public ServicioConsulta(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    /**
     * Obtiene un programa a partir de su identificador único.
     *
     * @param id identificador del programa
     * @return un {@link Optional} con el programa si existe
     */
    public Optional<Programa> getProgramaPorId(int id) {
        return Optional.ofNullable(dataStore.getProgramaPorId(id));
    }

    /**
     * Recupera la lista completa de programas disponibles.
     *
     * @return lista de programas
     */
    public List<Programa> getTodosLosProgramas() {
        return dataStore.getProgramas();
    }

    /**
     * Recupera la lista completa de convenios disponibles.
     *
     * @return lista de convenios
     */
    public List<Convenio> getTodosLosConvenios() {
        return dataStore.getConvenios();
    }

    /**
     * Busca un convenio por su identificador.
     *
     * @param idConvenio identificador del convenio
     * @return un {@link Optional} con el convenio si existe
     */
    public Optional<Convenio> buscarConvenioPorId(String idConvenio) {
        return Optional.ofNullable(dataStore.getConvenioPorId(idConvenio));
    }

    /**
     * Busca un estudiante en función de su RUT.
     *
     * @param rut RUT del estudiante
     * @return un {@link Optional} con el estudiante si existe
     */
    public Optional<Estudiante> buscarEstudiantePorRut(String rut) {
        Usuario u = dataStore.getUsuarioPorRut(rut);
        if (u instanceof Estudiante) {
            return Optional.of((Estudiante) u);
        }
        return Optional.empty();
    }

    /**
     * Obtiene la lista de postulaciones de un programa filtradas
     * por un criterio específico.
     *
     * @param programa programa sobre el cual se consultan las postulaciones
     * @param tipoFiltro tipo de filtro (rut, estado, convenio)
     * @param valorFiltro valor asociado al filtro
     * @return lista de postulaciones filtradas, o todas si no se aplica filtro
     */
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

}