import gestores.GestorIntercambio;
import menu.*;
import ui.VentanaPrincipal;
import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws Exception {
        // La ruta de la base de datos debe ser la misma que en GestorIntercambio
        String url = "jdbc:sqlite:gestion_intercambio.db";

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

        gestor.crearProgramaPorDefecto();
        gestor.recargarDatos();
        // gestor.cargarDatosIniciales();
        // gestor.cargarConveniosDesdeArchivo("src/main/resources/convenios.txt");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Cerrando la aplicación. Guardando datos...");
            gestor.guardarDatos();
        }));

        // Iniciamos el menú principal como una instancia
        VerificarInput input = new VerificarInput();
        MenuPrincipal menuPrincipal = new MenuPrincipal(input, gestor);
        menuPrincipal.iniciar();
    }
}