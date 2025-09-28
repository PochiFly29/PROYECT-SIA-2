package servicios;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;

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
            UIManager.setLookAndFeel(dark ? new FlatDarkLaf() : new FlatMacDarkLaf());
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

    /** Envuelve un componente centrado, con un botón flotante (inferior-izquierdo) para cambiar tema. */
    public static JLayeredPane wrapWithFloatingThemeButton(Component center, boolean darkInitial) {
        JLayeredPane layered = new JLayeredPane();

        // Holder que MANTIENE tu diseño centrado (usa GridBag para centrar el "center")
        JPanel holder = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        holder.add(center, gbc);

        layered.add(holder, JLayeredPane.DEFAULT_LAYER);

        // Botón flotante
        int buttonSize = 44;
        JToggleButton themeToggle = new JToggleButton(darkInitial ? "☀" : "🌙");
        themeToggle.setSelected(darkInitial);
        themeToggle.setFont(themeToggle.getFont().deriveFont(18f));
        themeToggle.setSize(buttonSize, buttonSize);
        themeToggle.setPreferredSize(new Dimension(buttonSize, buttonSize));
        themeToggle.setFocusable(false);
        themeToggle.setBorder(BorderFactory.createLineBorder(
                UIManager.getColor("Component.borderColor"), 1, true));
        themeToggle.setBackground(UIManager.getColor("Panel.background"));

        themeToggle.addActionListener(ev -> {
            boolean dark = themeToggle.isSelected();
            themeToggle.setText(dark ? "☀" : "🌙");
            applySession(dark);
        });

        layered.add(themeToggle, JLayeredPane.PALETTE_LAYER);

        // Layout del layered
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
