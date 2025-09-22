package ui;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PostularPanel extends JPanel {

    public PostularPanel() {
        setLayout(new BorderLayout());

        JPanel centro = new JPanel(new GridBagLayout());
        centro.setOpaque(false);
        centro.setBorder(new EmptyBorder(24, 24, 24, 24)); // padding opcional

        JLabel ph = new JLabel("Postular a un convenio próximamente", SwingConstants.CENTER);
        ph.putClientProperty(FlatClientProperties.STYLE, "font:+3");

        centro.add(ph, new GridBagConstraints());
        add(centro, BorderLayout.CENTER);
    }
}
