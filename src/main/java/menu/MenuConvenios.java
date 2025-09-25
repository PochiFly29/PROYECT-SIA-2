package menu;

import gestores.GestorIntercambio;
import modelo.*;
import servicios.VerificarInput;
import java.util.Optional;
import java.util.*;
/*
public class MenuConvenios {
    private final VerificarInput input;
    private final Usuario usuarioActual;
    private final GestorIntercambio gestor;
    private final MenuPostulaciones menuPostulaciones;

    public MenuConvenios(VerificarInput input, GestorIntercambio gestor, Usuario usuarioActual, MenuPostulaciones menuPostulaciones) {
        this.input = input;
        this.gestor = gestor;
        this.usuarioActual = usuarioActual;
        this.menuPostulaciones = menuPostulaciones;
    }

    public void ejecutarMenu() {
        while (true) {
            System.out.println("\n>>> Catálogo de Convenios Vigentes");
            List<Programa> programasVigentes = gestor.getProgramasVigentes();

            if (programasVigentes.isEmpty()) {
                System.out.println("No hay programas de intercambio activos en este momento.");
                input.pausaEnter("[Enter] para volver...");
                return;
            }

            Programa programaActual = programasVigentes.get(0);
            System.out.println("Programa: " + programaActual.getNombre());
            System.out.println("-------------------------------------------------------------------------------------------------------------------");
            System.out.printf("| %-5s | %-40s | %-20s | %-25s |%n", "ID", "UNIVERSIDAD", "PAÍS", "REQUISITOS ACADÉMICOS");
            System.out.println("-------------------------------------------------------------------------------------------------------------------");

            // Ordenar la lista de convenios
            List<Convenio> conveniosOrdenados = programaActual.getConveniosVigentes();
            conveniosOrdenados.sort(Comparator.comparing(Convenio::getId));

            for (Convenio c : conveniosOrdenados) {
                String uni = c.getUniversidad().length() > 37 ? c.getUniversidad().substring(0, 37) + "..." : c.getUniversidad();
                String pais = c.getPais().length() > 17 ? c.getPais().substring(0, 17) + "..." : c.getPais();
                String req = c.getRequisitosAcademicos().length() > 22 ? c.getRequisitosAcademicos().substring(0, 22) + "..." : c.getRequisitosAcademicos();

                System.out.printf("| %-5s | %-40s | %-20s | %-25s |%n",
                        c.getId(), uni, pais, req);
            }
            System.out.println("-------------------------------------------------------------------------------------------------------------------");

            String prompt = "\n0) Volver | ID) Ver detalle";
            if (usuarioActual instanceof Estudiante) {
                prompt += " y postular";
            } else if (usuarioActual instanceof Funcionario) {
                prompt += " y gestionar";
            }

            String op = input.leerLinea(prompt + "\nOpción: ");

            if (op.equalsIgnoreCase("0")) return;

            String idNormalizado = op.toUpperCase();
            if (!idNormalizado.startsWith("C")) {
                idNormalizado = "C" + idNormalizado;
            }

            Optional<Convenio> convenioOpt = gestor.buscarConvenio(idNormalizado);
            if (convenioOpt.isPresent()) {
                Convenio convenio = convenioOpt.get();
                System.out.println("\n--- Detalle del Convenio ---");
                System.out.println("ID: " + convenio.getId());
                System.out.println("Universidad: " + convenio.getUniversidad());
                System.out.println("País: " + convenio.getPais());
                System.out.println("Requisitos Académicos: " + convenio.getRequisitosAcademicos());
                System.out.println("Requisitos Económicos: " + convenio.getRequisitosEconomicos());

                if (usuarioActual instanceof Estudiante) {
                    // Lógica para estudiantes: postular
                    String postularOpcion = input.leerLinea("\n1) Postular | 0) Volver\nOpción: ");
                    if (postularOpcion.equals("1")) {
                        if (gestor.postular((Estudiante) usuarioActual, convenio)) {
                            System.out.println("¡Postulación exitosa!");
                        } else {
                            System.out.println("No se pudo postular. Ya tienes una postulación activa para este convenio.");
                        }
                    }
                } else if (usuarioActual instanceof Funcionario) {
                    // Lógica para funcionarios: editar, aprobar, etc.
                    System.out.println("Lógica de gestión de convenios para funcionarios en desarrollo.");
                }
            } else {
                System.out.println("ID de convenio inválido.");
            }
            input.pausaEnter("\n[Enter] para continuar...");
        }
    }

    public void ejecutarMenuFuncionario() {
        while (true) {
            System.out.println("\n>>> Catálogo de Convenios Vigentes");
            List<Programa> programasVigentes = gestor.getProgramasVigentes();

            if (programasVigentes.isEmpty()) {
                System.out.println("No hay programas de intercambio activos en este momento.");
                input.pausaEnter("[Enter] para volver...");
                return;
            }

            Programa programaActual = programasVigentes.get(0);
            System.out.println("Programa: " + programaActual.getNombre());
            System.out.println("-------------------------------------------------------------------------------------------------------------------");
            System.out.printf("| %-5s | %-40s | %-20s | %-25s |%n", "ID", "UNIVERSIDAD", "PAÍS", "REQUISITOS ACADÉMICOS");
            System.out.println("-------------------------------------------------------------------------------------------------------------------");

            List<Convenio> conveniosOrdenados = programaActual.getConveniosVigentes();
            conveniosOrdenados.sort(Comparator.comparing(Convenio::getId));

            for (Convenio c : conveniosOrdenados) {
                String uni = c.getUniversidad().length() > 37 ? c.getUniversidad().substring(0, 37) + "..." : c.getUniversidad();
                String pais = c.getPais().length() > 17 ? c.getPais().substring(0, 17) + "..." : c.getPais();
                String req = c.getRequisitosAcademicos().length() > 22 ? c.getRequisitosAcademicos().substring(0, 22) + "..." : c.getRequisitosAcademicos();

                System.out.printf("| %-5s | %-40s | %-20s | %-25s |%n",
                        c.getId(), uni, pais, req);
            }
            System.out.println("-------------------------------------------------------------------------------------------------------------------");

            System.out.println("\n0) Volver | Ingrese ID para ver postulaciones asociadas");
            String op = input.leerLinea("Opción: ");

            if (op.equalsIgnoreCase("0")) return;

            String idNormalizado = op.toUpperCase();
            if (!idNormalizado.startsWith("C")) {
                idNormalizado = "C" + idNormalizado;
            }

            Optional<Convenio> convenioOpt = gestor.buscarConvenio(idNormalizado);

            if (convenioOpt.isPresent()) {
                Convenio c = convenioOpt.get();
                menuPostulaciones.mostrarPostulacionesPorConvenio(c.getId());
            } else {
                System.out.println("ID de convenio inválido.");
                input.pausaEnter("[Enter] para continuar...");
            }
        }
    }
}

 */