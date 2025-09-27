package servicios;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;

import javax.swing.*;
import java.awt.*;

public final class ThemeManager {

    private ThemeManager() {}

    public static void applySession(boolean dark) {
        applyInternal(dark);
    }

    private static void applyInternal(boolean dark) {
        try {
            if (dark) FlatMacDarkLaf.setup();
            else FlatDarkLaf.setup();

            for (Window w : Window.getWindows()) {
                Rectangle bounds = w.getBounds();
                int extState = (w instanceof Frame) ? ((Frame) w).getExtendedState() : 0;

                SwingUtilities.updateComponentTreeUI(w);
                w.invalidate();
                w.validate();
                w.repaint();

                w.setBounds(bounds);
                if (w instanceof Frame) ((Frame) w).setExtendedState(extState);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "No se pudo aplicar el tema: " + e.getMessage(),
                    "Tema", JOptionPane.ERROR_MESSAGE);
        }
    }
}
