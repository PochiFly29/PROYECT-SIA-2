package gestores;

import modelo.Convenio;
import modelo.Estudiante;
import modelo.Postulacion;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Proveedor de estadísticas para el panel de auditoría.
 * Reúne datos desde {@link ServicioConsulta} y expone métricas útiles.
 */
public class GestorStatsProvider {

    private final ServicioConsulta consulta;

    public GestorStatsProvider(ServicioConsulta consulta) {
        this.consulta = consulta;
    }

    /** Total de estudiantes distintos que han postulado */
    public long totalPostulantes() {
        return consulta.getTodosLosProgramas().stream()
                .flatMap(p -> p.getPostulaciones().stream())
                .map(Postulacion::getRutEstudiante)
                .distinct()
                .count();
    }

    /** Total de postulaciones realizadas */
    public long totalPostulaciones() {
        return consulta.getTodosLosProgramas().stream()
                .mapToLong(p -> p.getPostulaciones().size())
                .sum();
    }

    /** Total de convenios existentes */
    public long totalConvenios() {
        return consulta.getTodosLosConvenios().size();
    }

    /** Promedio académico general de todos los estudiantes que postularon */
    public double promedioGeneral() {
        return consulta.getTodosLosProgramas().stream()
                .flatMap(p -> p.getPostulaciones().stream())
                .map(p -> consulta.buscarEstudiantePorRut(p.getRutEstudiante()).orElse(null))
                .filter(Objects::nonNull)
                .mapToDouble(Estudiante::getPromedio)
                .average()
                .orElse(0.0);
    }

    /**
     * Estadísticas de postulaciones por convenio (clave por ID único).
     * Devuelve una lista ordenada por count desc con label legible y porcentaje.
     */
    public List<StatConvenio> statsPostulacionesPorConvenio() {
        // Conteo por ID de convenio (único/estable)
        Map<String, Long> countsById = consulta.getTodosLosProgramas().stream()
                .flatMap(p -> p.getPostulaciones().stream())
                .collect(Collectors.groupingBy(
                        p -> p.getConvenioSeleccionado().getId(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        long total = countsById.values().stream().mapToLong(Long::longValue).sum();
        final long safeTotal = (total <= 0) ? 1 : total; // efectivamente final

        // Etiquetas legibles por ID
        Map<String, String> labelById = consulta.getTodosLosConvenios().stream()
                .collect(Collectors.toMap(
                        Convenio::getId,
                        c -> {
                            String uni = c.getUniversidad();
                            return (uni != null && !uni.isBlank()) ? uni : c.getId();
                        },
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        // Construimos lista ordenada desc por count
        return countsById.entrySet().stream()
                .map(e -> {
                    String id = e.getKey();
                    long count = e.getValue();
                    double percent = (count * 1.0) / safeTotal; // usa safeTotal
                    String label = labelById.getOrDefault(id, id);
                    return new StatConvenio(id, label, count, percent);
                })
                .sorted(Comparator.comparingLong(StatConvenio::count).reversed())
                .collect(Collectors.toList());
    }

    /** DTO inmutable para análisis por convenio */
    public static final class StatConvenio {
        private final String id;
        private final String label;
        private final long count;
        private final double percent; // 0..1

        public StatConvenio(String id, String label, long count, double percent) {
            this.id = id;
            this.label = label;
            this.count = count;
            this.percent = percent;
        }
        public String id() { return id; }
        public String label() { return label; }
        public long count() { return count; }
        public double percent() { return percent; }
    }
}
