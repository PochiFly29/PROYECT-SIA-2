package gestores;

import datastore.DataStore;
import enums.EstadoPostulacion;
import modelo.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServicioConsulta {
    private final DataStore dataStore;

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

    public Optional<Convenio> buscarConvenioPorId(String idConvenio) {
        return Optional.ofNullable(dataStore.getConvenioPorId(idConvenio));
    }

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

}