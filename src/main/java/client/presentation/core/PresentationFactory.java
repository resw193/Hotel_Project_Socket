package client.presentation.core;

import javax.swing.*;
import java.awt.*;

public interface PresentationFactory {
    Component create(String route, SessionContext session);

    default Component createFallback(String route) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Chưa map giao diện: " + route, SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 22f));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }
}
