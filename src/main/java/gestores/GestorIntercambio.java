package gestores;

import enums.EstadoConvenio;
import enums.EstadoPostulacion;
import enums.Rol;
import modelo.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class GestorIntercambio {
    private final Map<String, Usuario> usuarios;
    private final Map<String, Programa> programas;
    private final Map<String, Convenio> convenios;
    private Usuario usuarioActual;
    private int nextPostulacionId;

    public GestorIntercambio() {
        this.usuarios = new HashMap<>();
        this.programas = new HashMap<>();
        this.convenios = new HashMap<>();
        this.nextPostulacionId = 1;
        cargarDatosDesdeArchivos();
    }

    public void cargarDatosDesdeArchivos() {
        System.out.println("Cargando datos...");
        cargarUsuariosDePrueba();
        cargarConveniosDesdeArchivo("src/main/resources/convenios.txt");
        crearProgramaDePrueba();
        System.out.println("Datos cargados. Usuarios: " + usuarios.size() + ", Convenios: " + convenios.size() + ", Programas: " + programas.size());
    }

    private void cargarUsuariosDePrueba() {
        Estudiante est1 = new Estudiante("123", "Ivan Ferreira", "juan.perez@inst.cl", "123", "Ingenieria Civil", 5.8, 6);
        Usuario func1 = new Usuario("456", "María López", "m.lopez@inst.cl", "456", Rol.FUNCIONARIO);
        Usuario aud1  = new Usuario("112233445", "Ana Torres", "a.torres@inst.cl", "audit123", Rol.AUDITOR);
        usuarios.put(est1.getRut(), est1);
        usuarios.put(func1.getRut(), func1);
        usuarios.put(aud1.getRut(), aud1);
    }

    private void cargarConveniosDesdeArchivo(String archivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] datos = linea.split(";");
                if (datos.length == 8) {
                    String id = datos[0].trim();
                    String universidad = datos[1].trim();
                    String pais = datos[2].trim();
                    String area = datos[3].trim();
                    String requisitosAcademicos = datos[4].trim();
                    String requisitosEconomicos = datos[5].trim();
                    LocalDate fechaInicio = LocalDate.parse(datos[6].trim());
                    LocalDate fechaFin = LocalDate.parse(datos[7].trim());
                    Convenio convenio = new Convenio(id, universidad, pais, area,
                            requisitosAcademicos, requisitosEconomicos,
                            fechaInicio, fechaFin);
                    if (convenio.estaVigente()) {
                        convenio.setEstado(EstadoConvenio.VIGENTE);
                    } else {
                        convenio.setEstado(EstadoConvenio.VENCIDO);
                    }
                    convenios.put(id, convenio);
                }
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo de convenios. Asegúrate de que '" + archivo + "' exista en el directorio de trabajo. " + e.getMessage());
        }
    }

    private void crearProgramaDePrueba() {
        if (convenios.isEmpty()) {
            System.err.println("La lista de convenios está vacía. No se puede crear el programa de prueba.");
            return;
        }

        Programa prog2025 = new Programa("S1-2025", "Movilidad Semestral 2025", LocalDate.of(2025, 3, 1), LocalDate.of(2025, 12, 31));        for (Convenio c : convenios.values()) {
            prog2025.agregarConvenio(c);
        }
        programas.put(prog2025.getId(), prog2025);
    }

    public ResultadoLogin iniciarSesion(String rut, String pass) {
        Usuario usuario = usuarios.get(rut);

        if (usuario == null) {
            return new ResultadoLogin("Usuario no encontrado.");
        }

        if (usuario.isBloqueado()) {
            return new ResultadoLogin("Tu cuenta está bloqueada debido a múltiples intentos fallidos.");
        }

        if (usuario.validarCredenciales(pass)) {
            usuario.setIntentosFallidos(0);
            this.usuarioActual = usuario;
            return new ResultadoLogin(usuario);
        } else {
            usuario.setIntentosFallidos(usuario.getIntentosFallidos() + 1);
            String mensaje = "Contraseña incorrecta. Intentos restantes: " + (3 - usuario.getIntentosFallidos());
            if (usuario.getIntentosFallidos() >= 3) {
                usuario.setBloqueado(true);
                mensaje += "\nTu cuenta ha sido bloqueada. Contacta a un administrador.";
            }
            return new ResultadoLogin(mensaje);
        }
    }

    public void cerrarSesion() {
        this.usuarioActual = null;
    }

    public void registrarEstudiante(String rut, String nombre, String email, String pass, String carrera, int semestres, double promedio) {
        Estudiante nuevoEstudiante = new Estudiante(rut, nombre, email, pass, carrera, promedio, semestres);
        usuarios.put(rut, nuevoEstudiante);
    }

    public boolean existeUsuario(String rut) {
        return usuarios.containsKey(rut);
    }

    public List<Programa> getProgramasVigentes() {
        return programas.values().stream()
                .filter(Programa::estaVigente)
                .collect(Collectors.toList());
    }

    public Optional<Convenio> buscarConvenio(String id) {
        return Optional.ofNullable(convenios.get(id));
    }

    public boolean postular(Estudiante estudiante, Convenio convenio) {
        boolean yaExiste = estudiante.getPostulaciones().stream()
                .anyMatch(p -> p.getConvenioSeleccionado().getId().equals(convenio.getId()));
        if (!yaExiste) {
            String idPostulacion = "P" + nextPostulacionId++;
            Postulacion nuevaPostulacion = new Postulacion(idPostulacion, convenio, LocalDate.now(), EstadoPostulacion.POR_REVISAR);
            estudiante.postular(nuevaPostulacion);
            return true;
        }
        return false;
    }

    public void agregarInteraccionAPostulacion(String idPostulacion, Interaccion interaccion) {
        for (Usuario user : usuarios.values()) {
            if (user instanceof Estudiante) {
                Estudiante est = (Estudiante) user;
                Optional<Postulacion> pOpt = est.getPostulaciones().stream()
                        .filter(p -> p.getId().equals(idPostulacion))
                        .findFirst();
                if (pOpt.isPresent()) {
                    pOpt.get().agregarInteraccion(interaccion);
                    return;
                }
            }
        }
    }

    /**
     * Obtiene una lista de todas las postulaciones de todos los estudiantes.
     * @return Una lista que contiene todas las postulaciones.
     */
    public List<Postulacion> getTodasLasPostulaciones() {
        List<Postulacion> todasLasPostulaciones = new ArrayList<>();
        // Itera sobre cada usuario en el mapa de usuarios
        for (Usuario user : usuarios.values()) {
            // Si el usuario es un Estudiante, obtiene sus postulaciones
            if (user instanceof Estudiante) {
                Estudiante estudiante = (Estudiante) user;
                todasLasPostulaciones.addAll(estudiante.getPostulaciones());
            }
        }
        return todasLasPostulaciones;
    }

    /**
     * Obtiene una lista de postulaciones filtradas por estado.
     * @param estado El estado por el que se desea filtrar.
     * @return Una lista de postulaciones que coinciden con el estado.
     */
    public List<Postulacion> getPostulacionesPorEstado(EstadoPostulacion estado) {
        return getTodasLasPostulaciones().stream()
                .filter(p -> p.getEstado().equals(estado))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una lista de postulaciones asociadas a un convenio específico.
     * @param convenioId El ID del convenio.
     * @return Una lista de postulaciones asociadas a ese convenio.
     */
    public List<Postulacion> getPostulacionesPorConvenio(String convenioId) {
        return getTodasLasPostulaciones().stream()
                .filter(p -> p.getConvenioSeleccionado().getId().equalsIgnoreCase(convenioId))
                .collect(Collectors.toList());
    }

    public Programa getProgramaDeConvenio(Convenio convenio) {
        for (Programa programa : programas.values()) {
            if (programa.getConveniosVigentes().contains(convenio)) {
                return programa;
            }
        }
        return null;
    }

    public Estudiante buscarEstudiantePorPostulacion(String idPostulacion) {
        for (Usuario user : usuarios.values()) {
            if (user instanceof Estudiante) {
                Estudiante estudiante = (Estudiante) user;
                boolean postulacionEncontrada = estudiante.getPostulaciones().stream()
                        .anyMatch(p -> p.getId().equals(idPostulacion));
                if (postulacionEncontrada) {
                    return estudiante;
                }
            }
        }
        return null;
    }

    public void descartarOtrasPostulaciones(Estudiante estudiante, String idPostulacionAceptada) {
        estudiante.getPostulaciones().stream()
                .filter(p -> !p.getId().equals(idPostulacionAceptada))
                .forEach(p -> p.setEstado(EstadoPostulacion.ABANDONADA));
    }

    public Usuario getUsuarioActual() { return usuarioActual; }
    public Map<String, Programa> getProgramas() { return programas; }
}