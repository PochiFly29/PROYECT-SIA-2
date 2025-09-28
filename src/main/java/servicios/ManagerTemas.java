package servicios;

import com.formdev.flatlaf.FlatDarkLaf;           // “Claro” (tu definición)
import com.formdev.flatlaf.themes.FlatMacDarkLaf; // “Oscuro”

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public final class ManagerTemas {

    private ManagerTemas() {}

    /** dark = true -> FlatMacDarkLaf (Oscuro)
     *  dark = false -> FlatDarkLaf (Claro)  */
    public static void applySession(boolean dark) {
        try {
            if (dark) FlatMacDarkLaf.setup();
            else      FlatDarkLaf.setup();

            for (Window w : Window.getWindows()) {
                Rectangle b = w.getBounds();
                int ext = (w instanceof Frame) ? ((Frame) w).getExtendedState() : 0;

                SwingUtilities.updateComponentTreeUI(w);
                w.invalidate();
                w.validate();
                w.repaint();

                w.setBounds(b);
                if (w instanceof Frame) ((Frame) w).setExtendedState(ext);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    null,
                    "No se pudo aplicar el tema: " + e.getMessage(),
                    "Tema",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Envuelve el componente central en un JLayeredPane y le agrega un botón flotante
     * (esquina inferior izquierda) para alternar Claro/Oscuro. No desplaza el contenido.
     *
     * @param center        Componente principal (por ejemplo, tu wrapper con el formulario centrado).
     * @param darkInitial   Estado inicial del toggle: false=Claro (FlatDarkLaf), true=Oscuro (FlatMacDarkLaf).
     */
    public static JLayeredPane wrapWithFloatingThemeButton(Component center, boolean darkInitial) {
        // Contenedor en capas
        JLayeredPane layered = new JLayeredPane();

        // Panel para el center, con bounds gestionados por el resized listener
        JPanel holder = new JPanel(new BorderLayout());
        holder.add(center, BorderLayout.CENTER);

        layered.add(holder, JLayeredPane.DEFAULT_LAYER);

        // ---- Botón flotante (solo icono) ----
        int buttonSize = 40;
        JToggleButton themeToggle = new JToggleButton(darkInitial ? "🌙" : "☀");
        themeToggle.setSelected(darkInitial);
        themeToggle.setFont(themeToggle.getFont().deriveFont(20f));
        themeToggle.setSize(buttonSize, buttonSize);
        themeToggle.setPreferredSize(new Dimension(buttonSize, buttonSize));
        themeToggle.setFocusable(false);
        themeToggle.setBorderPainted(false);
        themeToggle.setContentAreaFilled(true);
        themeToggle.setOpaque(true);
        themeToggle.putClientProperty("JComponent.sizeVariant", "regular");
        themeToggle.putClientProperty(
                "FlatLaf.style",
                "borderWidth:1; arc:16; focusWidth:0; innerFocusWidth:0; background:lighten(@background,5%)"
        );

        themeToggle.addActionListener(ev -> {
            boolean dark = themeToggle.isSelected();
            themeToggle.setText(dark ? "🌙" : "☀");
            applySession(dark);
        });

        layered.add(themeToggle, JLayeredPane.PALETTE_LAYER);

        // Listener para ajustar bounds y mantener el botón en la esquina inferior izquierda
        layered.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                holder.setBounds(0, 0, layered.getWidth(), layered.getHeight());
                int margin = 12;
                int x = margin;
                int y = Math.max(margin, layered.getHeight() - themeToggle.getHeight() - margin);
                themeToggle.setLocation(x, y);
            }
        });

        return layered;
    }
}
