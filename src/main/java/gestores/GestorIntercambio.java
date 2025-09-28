package gestores;

import datastore.DataStore; // Asumiendo que DataStore está en su propio paquete
import modelo.*;

import java.sql.SQLException;
import java.util.List;

public class GestorIntercambio {

    private final DataStore dataStore;
    private final ServicioAutenticacion servicioAutenticacion;
    private final ServicioPostulacion servicioPostulacion;
    private final ServicioConsulta servicioConsulta;
    private final ServicioPrograma servicioPrograma;
    private final ServicioConvenio servicioConvenio;

    public GestorIntercambio() throws Exception {
        this.dataStore = new DataStore();
        // CAMBIO: El Gestor ahora compone los servicios, pasándoles el DataStore
        // para que puedan operar. Se elimina toda la lógica de carga de datos de aquí.
        this.servicioAutenticacion = new ServicioAutenticacion(dataStore);
        this.servicioConsulta = new ServicioConsulta(dataStore);
        this.servicioPostulacion = new ServicioPostulacion(dataStore,servicioConsulta);
        this.servicioPrograma = new ServicioPrograma(dataStore, servicioPostulacion);
        this.servicioConvenio = new ServicioConvenio(dataStore,servicioPostulacion);
        System.out.println("✅ Gestor y servicios inicializados.");
    }

    // --- Métodos delegados a los servicios correspondientes ---

    public ServicioAutenticacion getServicioAutenticacion() {
        return servicioAutenticacion;
    }

    public ServicioPostulacion getServicioPostulacion() {
        return servicioPostulacion;
    }

    public ServicioConsulta getServicioConsulta() {
        return servicioConsulta;
    }
    public ServicioPrograma getServicioPrograma() { return servicioPrograma; }
    public ServicioConvenio getServicioConvenio() { return servicioConvenio; }

    /**
     * Persiste los cambios de los usuarios en la BD.
     * Es una responsabilidad del servicio de autenticación.
     */
    public void guardarDatos() {
        servicioAutenticacion.guardarCambiosDeUsuarios();
    }
}