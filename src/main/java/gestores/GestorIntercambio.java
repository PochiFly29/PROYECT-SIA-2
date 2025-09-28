package gestores;

import datastore.DataStore; // Asumiendo que DataStore está en su propio paquete
import modelo.*;

import java.sql.SQLException;
import java.util.List;

/**
 * Clase principal que centraliza la gestión del intercambio académico.
 * <p>
 * Este gestor actúa como fachada y coordina los diferentes servicios que
 * interactúan con la capa de datos ({@link DataStore}).
 * </p>
 */
public class GestorIntercambio {

    /** Almacén centralizado de datos compartido entre los servicios. */
    private final DataStore dataStore;

    /** Servicio encargado de la autenticación y gestión de usuarios. */
    private final ServicioAutenticacion servicioAutenticacion;

    /** Servicio encargado de la gestión de postulaciones. */
    private final ServicioPostulacion servicioPostulacion;

    /** Servicio encargado de consultas generales (convenios, programas, etc.). */
    private final ServicioConsulta servicioConsulta;

    /**
     * Constructor que inicializa el {@link DataStore} y los servicios asociados.
     *
     * @throws Exception si ocurre un error en la inicialización o carga de datos.
     */
    public GestorIntercambio() throws Exception {
        this.dataStore = new DataStore();
        // CAMBIO: El Gestor ahora compone los servicios, pasándoles el DataStore
        // para que puedan operar. Se elimina toda la lógica de carga de datos de aquí.
        this.servicioAutenticacion = new ServicioAutenticacion(dataStore);
        this.servicioPostulacion = new ServicioPostulacion(dataStore);
        this.servicioConsulta = new ServicioConsulta(dataStore);
        System.out.println("✅ Gestor y servicios inicializados.");
    }

    // --- Métodos delegados a los servicios correspondientes ---

    /**
     * Obtiene el servicio de autenticación.
     *
     * @return instancia de {@link ServicioAutenticacion}.
     */
    public ServicioAutenticacion getServicioAutenticacion() {
        return servicioAutenticacion;
    }

    /**
     * Obtiene el servicio de gestión de postulaciones.
     *
     * @return instancia de {@link ServicioPostulacion}.
     */
    public ServicioPostulacion getServicioPostulacion() {
        return servicioPostulacion;
    }

    /**
     * Obtiene el servicio de consultas generales.
     *
     * @return instancia de {@link ServicioConsulta}.
     */
    public ServicioConsulta getServicioConsulta() {
        return servicioConsulta;
    }

    /**
     * Persiste los cambios de los usuarios en la BD.
     * Es una responsabilidad del servicio de autenticación.
     */
    public void guardarDatos() {
        servicioAutenticacion.guardarCambiosDeUsuarios();
    }
}