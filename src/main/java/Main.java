import gestores.GestorIntercambio;
import menu.*;
import servicios.VerificarInput;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws Exception {
        // La ruta de la base de datos debe ser la misma que en GestorIntercambio
        String url = "jdbc:sqlite:miBase.db";

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                System.out.println("Conexión SQLite exitosa!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String[] banner = {
                "  _____  _____ _____  ______ ",
                " / ____|/ ____|_   _ |  ____|",
                "| (___ | |  __  | |  | |__   ",
                " \\___ \\| | |_ | | |  |  __|  ",
                " ____) | |__| |_| |_ | |____ ",
                "|_____/ \\_____|_____||______|",
                "",
                "Sistema de Gestión de Intercambios Estudiantiles",
                "[SGIE]"
        };
        for (String line : banner) System.out.println(line);

        System.out.print("Cargando");
        for (int i = 0; i < 3; i++) {
            Thread.sleep(500);
            System.out.print(".");
        }
        System.out.print("\n");


        GestorIntercambio gestor = new GestorIntercambio();

        gestor.crearProgramaPorDefecto();
        gestor.recargarDatos();
        gestor.cargarConveniosDesdeArchivo("src/main/resources/convenios.txt");

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