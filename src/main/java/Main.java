import gestores.GestorIntercambio;
import menu.*;
import servicios.VerificarInput;
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

        // Creamos las instancias de las clases principales
        VerificarInput input = new VerificarInput();
        GestorIntercambio gestor = new GestorIntercambio();

        // Iniciamos el menú principal como una instancia
        MenuPrincipal menuPrincipal = new MenuPrincipal(input, gestor);
        menuPrincipal.iniciar();
    }
    /*

    public static void main(String[] args) throws Exception {
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

        // Creamos las instancias de las clases principales
        VerificarInput input = new VerificarInput();
        GestorIntercambio gestor = new GestorIntercambio();

        // Iniciamos el menú principal como una instancia
        MenuPrincipal menuPrincipal = new MenuPrincipal(input, gestor);
        menuPrincipal.iniciar();
    }

     */
}