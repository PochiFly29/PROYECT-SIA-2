package datastore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase de utilidad para centralizar la conexión a la base de datos SQLite.
 * <p>
 * Proporciona un único punto de acceso para obtener conexiones hacia
 * la base de datos {@code gestion_intercambio.db}.
 * </p>
 */
public class DatabaseManager {
    /** URL de conexión a la base de datos SQLite. */
    private static final String URL_BD = "jdbc:sqlite:gestion_intercambio.db";

    /**
     * Obtiene una conexión activa a la base de datos.
     *
     * @return una instancia de {@link Connection} conectada a la base de datos.
     * @throws SQLException si ocurre un error al intentar establecer la conexión.
     *
     * <p><b>Nota:</b> Asegúrate de que la librería JDBC de SQLite esté
     * incluida en el proyecto para evitar errores de carga.</p>
     */
    public static Connection getConnection() throws SQLException {
        // NOTA: Asegúrate de tener la librería de SQLite (el conector JDBC) en tu proyecto.
        return DriverManager.getConnection(URL_BD);
    }
}