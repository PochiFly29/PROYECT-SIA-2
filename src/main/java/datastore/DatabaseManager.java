package datastore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase de utilidad para centralizar la conexión a la base de datos.
 */
public class DatabaseManager {
    private static final String URL_BD = "jdbc:sqlite:gestion_intercambio.db";

    public static Connection getConnection() throws SQLException {
        // NOTA: Asegúrate de tener la librería de SQLite (el conector JDBC) en tu proyecto.
        return DriverManager.getConnection(URL_BD);
    }
}