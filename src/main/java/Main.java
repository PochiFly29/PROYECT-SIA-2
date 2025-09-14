import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import gestores.GestorIntercambio;
import menu.*;
import ui.VentanaPrincipal;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws Exception {
        // Como está dentro de resources, necesitas ruta relativa al classpath
        String url = "jdbc:sqlite:" + System.getProperty("user.dir") + "/db/miBase.db";

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                System.out.println("Conexión SQLite exitosa!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        FlatMacDarkLaf.setup();
        GestorIntercambio gestor = new GestorIntercambio();

        // Abrir la ventana
        SwingUtilities.invokeLater(() -> new VentanaPrincipal(gestor).setVisible(true));
    }
}