import com.formdev.flatlaf.FlatDarkLaf;
import gestores.GestorIntercambio;
import ui.VentanaPrincipal;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) throws Exception {
        // Configurar el tema inicial de la interfaz
        FlatDarkLaf.setup();

        // Crear una única instancia del gestor de intercambio
        GestorIntercambio gestor = new GestorIntercambio();

        // Abrir la ventana principal y pasarle el gestor
        SwingUtilities.invokeLater(() -> new VentanaPrincipal(gestor));
    }
}