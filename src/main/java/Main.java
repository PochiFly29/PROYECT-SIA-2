import com.formdev.flatlaf.FlatDarkLaf;
import gestores.GestorIntercambio;
import ui.VentanaPrincipal;
import javax.swing.SwingUtilities;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException {
        // Configurar el tema de la interfaz
        FlatDarkLaf.setup();

        // Crear una única instancia del gestor de intercambio
        GestorIntercambio gestor = new GestorIntercambio();

        // Cargar los datos iniciales de forma condicional
        // Esto solo se ejecutará si la base de datos está vacía
        try {
            if (gestor.getTodosLosUsuarios().isEmpty()) {
                gestor.cargarDatosIniciales();
                gestor.cargarConveniosDesdeArchivo("src/main/resources/convenios.txt");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Abrir la ventana principal y pasarle el gestor
        SwingUtilities.invokeLater(() -> new VentanaPrincipal(gestor));
    }
}