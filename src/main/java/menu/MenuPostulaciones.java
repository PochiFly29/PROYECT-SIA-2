package menu;

import enums.*;
import gestores.GestorIntercambio;
import modelo.*;
import servicios.VerificarInput;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class MenuPostulaciones {
    private final VerificarInput input;
    private final Usuario usuarioActual;
    private final GestorIntercambio gestor;

    public MenuPostulaciones(VerificarInput input, GestorIntercambio gestor, Usuario usuarioActual) {
        this.input = input;
        this.gestor = gestor;
        this.usuarioActual = usuarioActual;
    }

    public void ejecutarMenuEstudiante() {
        while (true) {
            System.out.println("\n--- Mis Postulaciones ---");
            Estudiante estudiante = (Estudiante) usuarioActual;
            List<Postulacion> postulaciones = gestor.getPostulaciones("rut", estudiante.getRut());
            if (mostrarListaYPermitirSeleccion(postulaciones)) {
                return;
            }
        }
    }

    public void ejecutarMenuFuncionario() {
        while (true) {
            System.out.println("\n--- Menú de Postulaciones (Funcionario) ---");
            System.out.println("1) Ver todas las postulaciones");
            System.out.println("2) Ver postulaciones 'POR REVISAR'");
            System.out.println("0) Volver");

            int sel = input.leerEntero("Opción: ", -1);
            List<Postulacion> postulacionesAMostrar;

            if (sel == 1) {
                postulacionesAMostrar = gestor.getPostulaciones("todos", "");
                if (mostrarListaYPermitirSeleccion(postulacionesAMostrar)) {
                    return;
                }
            } else if (sel == 2) {
                postulacionesAMostrar = gestor.getPostulaciones("estado", "POR_REVISAR");
                if (mostrarListaYPermitirSeleccion(postulacionesAMostrar)) {
                    return;
                }
            } else if (sel == 0) {
                return;
            } else {
                System.out.println("Opción inválida.");
            }
        }
    }

    public void mostrarPostulacionesPorConvenio(String convenioId) {
        System.out.println("\n>>> Postulaciones para el Convenio " + convenioId);
        List<Postulacion> postulaciones = gestor.getPostulaciones("convenio", convenioId);
        mostrarListaYPermitirSeleccion(postulaciones);
    }

    private boolean mostrarListaYPermitirSeleccion(List<Postulacion> postulaciones) {
        if (postulaciones.isEmpty()) {
            System.out.println("No hay postulaciones registradas en esta lista.");
            input.pausaEnter("[Enter] para volver...");
            return true;
        }

        System.out.println("----------------------------------------------------------------------------------------------------------------------------");
        System.out.printf("| %-4s | %-34s | %-15s | %-12s | %-23s | %-15s |%n",
                "ID", "UNIVERSIDAD", "PAÍS", "EMITIDA", "VIGENCIA", "ESTADO");
        System.out.println("----------------------------------------------------------------------------------------------------------------------------");

        for (Postulacion p : postulaciones) {
            Convenio conv = p.getConvenioSeleccionado();
            Programa prog = gestor.getProgramaDeConvenio(conv);

            String uni = (conv != null && conv.getUniversidad() != null) ? conv.getUniversidad() : "-";
            if (uni.length() > 34) uni = uni.substring(0, 31) + "...";

            String pais = (conv != null && conv.getPais() != null) ? conv.getPais() : "-";
            if (pais.length() > 15) pais = pais.substring(0, 12) + "...";

            String emitida = String.valueOf(p.getFechaPostulacion());

            String vigencia = (prog != null) ? (prog.getFechaInicio() + " a " + prog.getFechaFin()) : "-";
            if (vigencia.length() > 23) vigencia = vigencia.substring(0, 20) + "...";

            System.out.printf("| %-4s | %-34s | %-15s | %-12s | %-23s | %-15s |%n",
                    p.getId(), uni, pais, emitida, vigencia, p.getEstado());
        }
        System.out.println("----------------------------------------------------------------------------------------------------------------------------");

        System.out.println("\n0) Volver | Ingrese ID para ver detalle");
        String op = input.leerLinea("Opción: ").trim();

        if ("0".equalsIgnoreCase(op)) {
            return true;
        }

        if (op.isEmpty()) {
            System.out.println("Opción inválida. Escriba 0 o un ID válido (ej: P1).");
            input.pausaEnter("[Enter] para continuar...");
            return false;
        }

        manejarSeleccionPostulacion(op, postulaciones);
        return false;
    }

    private void manejarSeleccionPostulacion(String op, List<Postulacion> postulaciones) {
        String idNormalizado = op.toUpperCase().startsWith("P") ? op.toUpperCase() : "P" + op.toUpperCase();

        Optional<Postulacion> postulacionOpt = postulaciones.stream()
                .filter(p -> p.getId().equalsIgnoreCase(idNormalizado))
                .findFirst();

        if (postulacionOpt.isPresent()) {
            gestionarPostulacion(postulacionOpt.get());
        } else {
            System.out.println("ID de postulación inválido.");
            input.pausaEnter("[Enter] para continuar...");
        }
    }

    private void gestionarPostulacion(Postulacion p) {
        while (true) {
            mostrarDetallesPostulacion(p);

            System.out.println("1) Agregar interacción");
            System.out.println("2) Ver historial de interacciones");

            if (usuarioActual.getRol() == Rol.FUNCIONARIO) {
                System.out.println("3) Cambiar estado de la postulación");
            }

            System.out.println("0) Volver");
            int sel = input.leerEntero("Opción: ", -1);

            switch (sel) {
                case 1:
                    agregarInteraccion(p);
                    break;
                case 2:
                    verHistorialInteracciones(p);
                    break;
                case 3:
                    if (usuarioActual.getRol() == Rol.FUNCIONARIO) {
                        gestionarEstado(p);
                    } else {
                        System.out.println("Opción inválida.");
                    }
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Opción inválida.");
            }
        }
    }

    private void gestionarEstado(Postulacion p) {
        System.out.println("\n--- Cambiar Estado de Postulación ---");
        System.out.println("Estado actual: " + p.getEstado());
        System.out.println("1) PRESELECCIONADA");
        System.out.println("2) ACEPTADA");
        System.out.println("3) RECHAZADA");
        System.out.println("0) Volver");

        int sel = input.leerEntero("Seleccione el nuevo estado: ", -1);
        EstadoPostulacion nuevoEstado = p.getEstado();

        switch (sel) {
            case 1:
                nuevoEstado = EstadoPostulacion.PRESELECCIONADA;
                System.out.println("Estado actualizado a PRESELECCIONADA.");
                break;
            case 2:
                nuevoEstado = EstadoPostulacion.ACEPTADA;
                Estudiante postulante = gestor.buscarEstudiantePorPostulacion(p.getId());
                if (postulante != null) {
                    gestor.descartarOtrasPostulaciones(postulante, p.getId());
                    System.out.println("El estudiante ha sido aceptado. Las demás postulaciones han sido marcadas como RECHAZADA.");
                }
                break;
            case 3:
                nuevoEstado = EstadoPostulacion.RECHAZADA;
                System.out.println("Postulación RECHAZADA.");
                break;
            case 0:
                return;
            default:
                System.out.println("Opción inválida.");
                return;
        }

        if (nuevoEstado != p.getEstado()) {
            p.setEstado(nuevoEstado);
        }
    }

    private void mostrarDetallesPostulacion(Postulacion p) {
        System.out.println("\n--- Postulación al Convenio de " + p.getConvenioSeleccionado().getUniversidad() + " ---");
        System.out.println("País: " + p.getConvenioSeleccionado().getPais());
        Programa programaAsociado = gestor.getProgramaDeConvenio(p.getConvenioSeleccionado());
        if (programaAsociado != null) {
            System.out.println("Programa: " + programaAsociado.getNombre());
            System.out.println("Plazo: " + programaAsociado.getFechaInicio() + " a " + programaAsociado.getFechaFin());
        }
        if (usuarioActual.getRol() == Rol.FUNCIONARIO) {
            Estudiante postulante = gestor.buscarEstudiantePorPostulacion(p.getId());
            if (postulante != null) {
                System.out.println("Postulante: " + postulante.getNombreCompleto());
            }
        }
        System.out.println("Estado actual: " + p.getEstado());
        System.out.println("Requisitos Académicos: " + p.getConvenioSeleccionado().getRequisitosAcademicos());
        System.out.println("Requisitos Económicos: " + p.getConvenioSeleccionado().getRequisitosEconomicos());
        System.out.println("----------------------------------------------------------");
    }

    private void agregarInteraccion(Postulacion p) {
        if (usuarioActual.getRol() == Rol.ESTUDIANTE) {
            System.out.println("\n--- Subir Documento ---");
            String titulo = input.leerLinea("Título del documento (ej: 'PASAPORTE.pdf'): ");
            Interaccion interaccion = new Interaccion(usuarioActual, TipoInteraccion.DOCUMENTO, titulo, LocalDateTime.now(), null);
            p.agregarInteraccion(interaccion);
            if (p.getEstado() != EstadoPostulacion.POR_REVISAR) {
                p.setEstado(EstadoPostulacion.POR_REVISAR);
                System.out.println("Documento agregado exitosamente. El estado de la postulación ha sido cambiado a 'POR REVISAR'.");
            } else {
                System.out.println("Documento agregado exitosamente.");
            }
        } else if (usuarioActual.getRol() == Rol.FUNCIONARIO) {
            System.out.println("\n--- Agregar Comentario ---");
            String titulo = input.leerLinea("Ingrese su comentario: ");
            Interaccion interaccion = new Interaccion(usuarioActual, TipoInteraccion.COMENTARIO, titulo, LocalDateTime.now(), null);
            p.agregarInteraccion(interaccion);
            p.setEstado(EstadoPostulacion.REVISADA);
            System.out.println("Comentario agregado y estado de postulación cambiado a 'REVISADA'.");
        }
    }

    private void verHistorialInteracciones(Postulacion p) {
        System.out.println("\n--- Historial de Interacciones ---");
        if (p.getInteracciones().isEmpty()) {
            System.out.println("No hay interacciones registradas.");
        } else {
            System.out.println("---------------------------------------------------------------------------------------------------");
            System.out.printf("| %-12s | %-25s | %-50s |%n", "FECHA", "AGREGADO POR", "ARCHIVO / COMENTARIO");
            System.out.println("---------------------------------------------------------------------------------------------------");
            for (Interaccion i : p.getInteracciones()) {
                String tituloFormato = i.getTitulo().length() > 47 ? i.getTitulo().substring(0, 44) + "..." : i.getTitulo();
                System.out.printf("| %-12s | %-25s | %-50s |%n",
                        i.getFechaHora().toLocalDate(),
                        i.getAutor().getNombreCompleto(),
                        tituloFormato);
            }
            System.out.println("---------------------------------------------------------------------------------------------------");
        }
        input.pausaEnter("\n[Enter] para continuar...");
    }
}