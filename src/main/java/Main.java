import com.formdev.flatlaf.FlatDarkLaf;
import gestores.GestorIntercambio;
import ui.VentanaPrincipal;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) throws Exception {
        // Configurar el tema de la interfaz
        FlatDarkLaf.setup();

        // Crear una única instancia del gestor de intercambio
        // El constructor de GestorIntercambio ya se encarga de todo.
        GestorIntercambio gestor = new GestorIntercambio();

        // Abrir la ventana principal y pasarle el gestor
        SwingUtilities.invokeLater(() -> new VentanaPrincipal(gestor));
    }
}