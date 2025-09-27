package gestores;

import enums.EstadoPostulacion;
import modelo.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Clase encargada de la gestión de intercambios académicos.
 * Administra usuarios, estudiantes, programas, convenios y postulaciones.
 * Integra las operaciones de consulta, registro, actualización y postulación
 * utilizando el DataStore para persistencia en base de datos.
 */
public class GestorIntercambio {

    /** Objeto DataStore que maneja la persistencia y los datos en memoria */
    private DataStore dataStore;

    /**
     * Constructor que inicializa el gestor.
     * Crea las tablas si no existen y carga los datos desde la base de datos.
     * Enlaza los datos en memoria para uso posterior.
     */
    public GestorIntercambio() {
        this.dataStore = new DataStore();
        try {
            dataStore.crearTablas();
            dataStore.cargarDatosDesdeBD();
            System.out.println("Datos cargados y enlazados en DataStore. Usuarios: " + dataStore.getUsuarios().size() + ", Convenios: " + dataStore.getConvenios().size() + ", Postulaciones: " + dataStore.getPostulaciones().size());
        } catch (SQLException e) {
            System.out.println("Error al inicializar el gestor: " + e.getMessage());
        }
    }

    /**
     * Guarda todos los datos en la base de datos a partir de la memoria.
     * Captura excepciones de SQL y muestra mensajes de error en consola.
     */
    public void guardarDatos() {
        try {
            dataStore.guardarDatos();
        } catch (SQLException e) {
            System.out.println("Error al guardar datos: " + e.getMessage());
        }
    }


    /**
     * Intenta iniciar sesión para un usuario dado su RUT y contraseña.
     * Controla intentos fallidos y bloqueos.
     *
     * @param rut RUT del usuario
     * @param pass Contraseña
     * @return ResultadoLogin con información del usuario o mensaje de error
     */
    public ResultadoLogin iniciarSesion(String rut, String pass) {
        Usuario usuario = dataStore.getUsuarioPorRut(rut);
        if (usuario == null) {
            return new ResultadoLogin("El RUT no está registrado.");
        }
        if (usuario.isBloqueado()) {
            return new ResultadoLogin("Su cuenta ha sido bloqueada. Contacte a un funcionario.");
        }
        if (usuario.getPass().equals(pass)) {
            usuario.setIntentosFallidos(0);
            return new ResultadoLogin(usuario);
        } else {
            usuario.setIntentosFallidos();
            if (usuario.getIntentosFallidos() >= 3) {
                usuario.setBloqueado(true);
                return new ResultadoLogin("Demasiados intentos fallidos. Su cuenta ha sido bloqueada.");
            }
            return new ResultadoLogin("Contraseña incorrecta. Intento " + usuario.getIntentosFallidos() + " de 3.");
        }
    }

    /**
     * Actualiza el nombre de un usuario.
     *
     * @param rut RUT del usuario
     * @param nuevoNombre Nuevo nombre a asignar
     */
    public void actualizarNombreUsuario(String rut, String nuevoNombre) {
        dataStore.actualizarNombreUsuario(rut, nuevoNombre);
    }

    /**
     * Actualiza el email de un usuario.
     *
     * @param rut RUT del usuario
     * @param nuevoEmail Nuevo correo electrónico
     */
    public void actualizarEmailUsuario(String rut, String nuevoEmail) {
        dataStore.actualizarEmailUsuario(rut, nuevoEmail);
    }

    /**
     * Actualiza la contraseña de un usuario.
     *
     * @param rut RUT del usuario
     * @param nuevaPass Nueva contraseña
     */
    public void actualizarPasswordUsuario(String rut, String nuevaPass) {
        dataStore.actualizarPasswordUsuario(rut, nuevaPass);
    }

    /**
     * Actualiza la carrera de un estudiante.
     *
     * @param rut RUT del estudiante
     * @param nuevaCarrera Nueva carrera
     */
    public void actualizarCarreraEstudiante(String rut, String nuevaCarrera) {
        dataStore.actualizarCarreraEstudiante(rut, nuevaCarrera);
    }

    /**
     * Agrega una interacción a una postulacion específica.
     *
     * @param idPostulacion ID de la postulacion
     * @param interaccion Interacción a agregar
     */
    public void agregarInteraccionAPostulacion(String idPostulacion, Interaccion interaccion) {
        dataStore.agregarInteraccionAPostulacion(idPostulacion, interaccion);
    }

    /**
     * Actualiza el estado de una postulacion.
     *
     * @param idPostulacion ID de la postulacion
     * @param nuevoEstado Nuevo estado a asignar
     */
    public void actualizarEstadoPostulacion(String idPostulacion, EstadoPostulacion nuevoEstado) {
        dataStore.actualizarEstadoPostulacion(idPostulacion, nuevoEstado);
    }

    /**
     * Busca el estudiante asociado a una postulacion.
     *
     * @param idPostulacion ID de la postulacion
     * @return Estudiante asociado o null si no existe
     */
    public Estudiante buscarEstudiantePorPostulacion(String idPostulacion) {
        Postulacion p = dataStore.getPostulacionPorId(idPostulacion);
        if (p == null) return null;
        Usuario u = dataStore.getUsuarioPorRut(p.getRutEstudiante());
        return u instanceof Estudiante ? (Estudiante) u : null;
    }

    /**
     * Descarta otras postulaciones de un estudiante, dejando solo la excepción.
     *
     * @param rutEstudiante RUT del estudiante
     * @param idPostulacionAExcluir ID de la postulacion que no se debe descartar
     */
    public void descartarOtrasPostulaciones(String rutEstudiante, String idPostulacionAExcluir) {
        dataStore.actualizarEstadosPostulaciones(rutEstudiante, idPostulacionAExcluir, EstadoPostulacion.RECHAZADA);
    }

    /**
     * Registra un nuevo estudiante en el sistema.
     *
     * @param rut RUT del estudiante
     * @param nombre Nombre completo
     * @param email Correo electrónico
     * @param pass Contraseña
     * @param carrera Carrera del estudiante
     * @param semestres Semestres cursados
     * @param promedio Promedio académico
     */
    public void registrarEstudiante(String rut, String nombre, String email, String pass, String carrera, int semestres, double promedio) {
        if (dataStore.getUsuarioPorRut(rut) != null) {
            System.out.println("El RUT ya se encuentra registrado.");
            return;
        }
        Estudiante nuevoEstudiante = new Estudiante(rut, nombre, email, pass, carrera, promedio, semestres);
        dataStore.addUsuario(nuevoEstudiante);
        System.out.println("Estudiante " + nombre + " registrado exitosamente.");
    }

    /**
     * Devuelve la lista de programas vigentes.
     *
     * @return Lista de programas
     */
    public List<Programa> getProgramasVigentes() {
        return dataStore.getProgramas();
    }

    /**
     * Busca un convenio por su ID.
     *
     * @param idConvenio ID del convenio
     * @return Optional de Convenio
     */
    public Optional<Convenio> buscarConvenio(String idConvenio) {
        return Optional.ofNullable(dataStore.getConvenioPorId(idConvenio));
    }

    /**
     * Obtiene el programa asociado a un convenio.
     *
     * @param convenio Convenio
     * @return Programa asociado
     */
    public Programa getProgramaDeConvenio(Convenio convenio) {
        return dataStore.getProgramaPorId(convenio.getIdPrograma());
    }

    /**
     * Realiza la postulación de un estudiante a un convenio.
     * Evita duplicar postulaciones.
     *
     * @param estudiante Estudiante
     * @param convenio Convenio
     * @return true si la postulación se realizó, false si ya existía
     */
    public boolean postular(Estudiante estudiante, Convenio convenio) {
        if (dataStore.getPostulaciones().stream().anyMatch(p -> p.getRutEstudiante().equals(estudiante.getRut()) && p.getIdConvenio().equals(convenio.getId()))) {
            return false;
        }
        String nuevoId = "P" + (dataStore.getPostulaciones().size() + 1);
        Postulacion nuevaPostulacion = new Postulacion(nuevoId, estudiante.getRut(), convenio.getId(), LocalDate.now(), EstadoPostulacion.POR_REVISAR);
        nuevaPostulacion.setConvenioSeleccionado(convenio);
        dataStore.addPostulacion(nuevaPostulacion);
        estudiante.agregarPostulacion(nuevaPostulacion);
        return true;
    }

    /**
     * Obtiene postulaciones filtradas por rut, estado o convenio.
     *
     * @param tipoFiltro Tipo de filtro ("rut", "estado", "convenio")
     * @param valorFiltro Valor a filtrar
     * @return Lista de postulaciones filtradas
     */
    public List<Postulacion> getPostulaciones(String tipoFiltro, String valorFiltro) {
        Stream<Postulacion> postulacionesStream = dataStore.getPostulaciones().stream();
        switch (tipoFiltro) {
            case "rut":
                return postulacionesStream
                        .filter(p -> p.getRutEstudiante().equals(valorFiltro))
                        .sorted(Comparator.comparing(Postulacion::getFechaPostulacion).reversed())
                        .collect(Collectors.toList());
            case "estado":
                EstadoPostulacion estado = EstadoPostulacion.valueOf(valorFiltro.toUpperCase());
                if (estado == EstadoPostulacion.POR_REVISAR) {
                    return postulacionesStream
                            .filter(p -> p.getEstado() == estado)
                            .sorted(Comparator.comparing(Postulacion::getFechaPostulacion))
                            .collect(Collectors.toList());
                }
                return postulacionesStream
                        .filter(p -> p.getEstado() == estado)
                        .sorted(Comparator.comparing(p -> Integer.parseInt(p.getId().substring(1))))
                        .collect(Collectors.toList());
            case "convenio":
                return postulacionesStream
                        .filter(p -> p.getIdConvenio().equals(valorFiltro))
                        .sorted(Comparator.comparing(p -> Integer.parseInt(p.getId().substring(1))))
                        .collect(Collectors.toList());
            default:
                return dataStore.getPostulaciones();
        }
    }

    /**
     * Verifica si un usuario existe en el sistema por su RUT.
     *
     * @param rut RUT del usuario
     * @return true si existe, false en caso contrario
     */
    public boolean existeUsuario(String rut) {
        return dataStore.getUsuarioPorRut(rut) != null;
    }

    /**
     * Cierra sesión del usuario activo.
     * Actualmente no realiza ninguna acción.
     */
    public void cerrarSesion() {

    }
}