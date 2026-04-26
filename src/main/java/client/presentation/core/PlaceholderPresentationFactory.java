package client.presentation.core;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import java.awt.*;

public class PlaceholderPresentationFactory implements PresentationFactory {
    @Override
    public Component create(String route, SessionContext session) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.putClientProperty(FlatClientProperties.STYLE,
                "arc:20;background:lighten($Panel.background,3%)");

        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.putClientProperty(FlatClientProperties.STYLE,
                "arc:24;background:$Menu.background;border:24,24,24,24");
        card.setPreferredSize(new Dimension(520, 220));

        JLabel title = new JLabel(route, SwingConstants.CENTER);
        title.setForeground(new Color(242, 201, 76));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));

        String employeeName = session != null && session.getEmployee() != null
                ? session.getEmployee().getFullName()
                : "Unknown";
        String role = session != null ? session.getRole() : "Unknown";

        JTextArea text = new JTextArea(
                "Đây là placeholder cho route: " + route + "\n\n" +
                        "Nhân viên hiện tại: " + employeeName + "\n" +
                        "Vai trò: " + role + "\n\n" +
                        "Khi bạn gửi tiếp các form chức năng cũ, chỉ cần map route này sang form presentation mới.");
        text.setEditable(false);
        text.setOpaque(false);
        text.setForeground(new Color(234, 242, 255));
        text.setFont(text.getFont().deriveFont(15f));

        card.add(title, BorderLayout.NORTH);
        card.add(text, BorderLayout.CENTER);
        panel.add(card);
        return panel;
    }
}
