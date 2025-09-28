package gestores;

import datastore.DataStore;
import modelo.*;

/**
 * Punto de orquestación de la aplicación.
 * {@code GestorIntercambio} compone y expone los servicios de dominio
 * (autenticación, consulta, postulación, programa y convenio) que operan
 * sobre un {@link DataStore} compartido. Su función principal es
 * centralizar el acceso a dichos servicios desde la capa de interfaz.
 * Esta clase no implementa lógica de negocio;
 * delega todas las operaciones en los servicios especializados.
 */
public class GestorIntercambio {

    /** Repositorio/capa de persistencia compartida por todos los servicios. */
    private final DataStore dataStore;

    /** Servicio responsable de autenticación y gestión básica de usuarios. */
    private final ServicioAutenticacion servicioAutenticacion;

    /** Servicio responsable del ciclo de vida de las postulaciones. */
    private final ServicioPostulacion servicioPostulacion;

    /** Servicio de consultas de lectura (programas, convenios, postulaciones, etc.). */
    private final ServicioConsulta servicioConsulta;

    /** Servicio para administrar programas de intercambio. */
    private final ServicioPrograma servicioPrograma;

    /** Servicio para administrar convenios. */
    private final ServicioConvenio servicioConvenio;

    /**
     * Crea un {@code GestorIntercambio} inicializando la capa de datos y
     * componiendo los servicios de dominio.
     */
    public GestorIntercambio() throws Exception {
        this.dataStore = new DataStore();
        // El Gestor compone los servicios inyectando el DataStore compartido.
        this.servicioAutenticacion = new ServicioAutenticacion(dataStore);
        this.servicioConsulta = new ServicioConsulta(dataStore);
        this.servicioPostulacion = new ServicioPostulacion(dataStore, servicioConsulta);
        this.servicioPrograma = new ServicioPrograma(dataStore, servicioPostulacion);
        this.servicioConvenio = new ServicioConvenio(dataStore, servicioPostulacion);
        System.out.println("✅ Gestor y servicios inicializados.");
    }

    // --- Acceso a servicios ---

    /**
     * @return el servicio de autenticación y gestión básica de usuarios.
     */
    public ServicioAutenticacion getServicioAutenticacion() {
        return servicioAutenticacion;
    }

    /**
     * @return el servicio que gestiona el ciclo de vida de las postulaciones.
     */
    public ServicioPostulacion getServicioPostulacion() {
        return servicioPostulacion;
    }

    /**
     * @return el servicio de consultas (lecturas y filtros de datos).
     */
    public ServicioConsulta getServicioConsulta() {
        return servicioConsulta;
    }

    /**
     * @return el servicio de administración de programas de intercambio.
     */
    public ServicioPrograma getServicioPrograma() { return servicioPrograma; }

    /**
     * @return el servicio de administración de convenios.
     */
    public ServicioConvenio getServicioConvenio() { return servicioConvenio; }

    /**
     * Solicita la persistencia de los cambios relacionados con usuarios.
     */
    public void guardarDatos() {
        servicioAutenticacion.guardarCambiosDeUsuarios();
    }
}
