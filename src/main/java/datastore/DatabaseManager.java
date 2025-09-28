package datastore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase de utilidad (Singleton de facto) para **gestionar y centralizar la conexión** * con la base de datos SQLite del proyecto.
 * * <p>Su objetivo principal es ocultar la cadena de conexión (URL) y
 * simplificar la obtención de un objeto {@code Connection} para el resto de las clases
 * de la capa de datos.</p>
 */
public class DatabaseManager {
    /**
     * URL de conexión a la base de datos SQLite.
     * Apunta al archivo local {@code gestion_intercambio.db}.
     */
    private static final String URL_BD = "jdbc:sqlite:gestion_intercambio.db";

    public static Connection getConnection() throws SQLException {
        // NOTA: Asegúrate de tener la librería de SQLite (el conector JDBC) en tu proyecto.
        return DriverManager.getConnection(URL_BD);
    }
}