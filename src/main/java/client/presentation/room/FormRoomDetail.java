package client.presentation.room;

import common.dto.RoomDTO;
import common.dto.RoomTypeDTO;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class FormRoomDetail extends JDialog {

    public FormRoomDetail(RoomDTO room, RoomTypeDTO roomType) {
        setTitle("Thông tin phòng");
        setLayout(new MigLayout("wrap 2, insets 20, gap 14", "[180!,right]40[fill,grow]"));
        getContentPane().setBackground(new Color(0x0B1F33));

        Color fg = new Color(0xE9EEF6);
        Color box = new Color(0x102D4A);

        JLabel lblTitle = new JLabel("CHI TIẾT PHÒNG");
        lblTitle.setForeground(fg);
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 18f));

        ImageIcon icon = loadRoomImage(room == null ? null : room.getImgRoomSource());
        JLabel lblImage = new JLabel(icon);
        lblImage.setBorder(BorderFactory.createLineBorder(new Color(0x153C5B)));

        add(lblTitle, "span 2, al center, wrap");
        add(lblImage, "span 2, al center, wrap");

        add(label("Room ID:"));     add(value(room == null ? "" : room.getRoomId(), fg, box));
        add(label("Mô tả:"));       add(value(room == null ? "" : safe(room.getDescription()), fg, box));
        add(label("Trạng thái:"));  add(value(room != null && room.isAvailable() ? "Trống" : "Đã đặt/Đang ở", fg, box));
        add(label("Loại phòng:"));  add(value(room == null ? "" : safe(room.getRoomTypeName()), fg, box));
        add(label("View:"));        add(value(room == null || room.getView() == null ? "-" : room.getView(), fg, box));

        if (roomType != null) {
            add(label("Sức chứa:"));
            add(value(
                    "Người lớn: " + roomType.getMaxAdults() + " | Trẻ em: " + roomType.getMaxChildren(),
                    fg, box
            ));

            add(label("Bố cục:"));
            add(value(roomType.getDescription() == null ? "-" : roomType.getDescription(), fg, box));
        }

        pack();
        setSize(700, getHeight());
        setLocationRelativeTo(null);
    }

    private ImageIcon loadRoomImage(String path) {
        ImageIcon icon = null;
        try {
            if (path != null && !path.trim().isEmpty()) {
                File f = new File(path);
                Image img;
                if (f.exists()) {
                    img = new ImageIcon(f.getAbsolutePath()).getImage();
                } else {
                    java.net.URL u = getClass().getResource(path.startsWith("/") ? path : "/" + path);
                    img = (u != null) ? new ImageIcon(u).getImage() : null;
                }
                if (img != null) {
                    icon = new ImageIcon(img.getScaledInstance(220, 160, Image.SCALE_SMOOTH));
                }
            }
        } catch (Exception ignored) {
        }

        if (icon == null) {
            java.net.URL nf = getClass().getResource("/images/404-not-found.jpg");
            if (nf != null) {
                Image fallback = new ImageIcon(nf).getImage().getScaledInstance(220, 160, Image.SCALE_SMOOTH);
                icon = new ImageIcon(fallback);
            } else {
                icon = new ImageIcon(new BufferedImage(220, 160, BufferedImage.TYPE_INT_RGB));
            }
        }
        return icon;
    }

    private JLabel label(String s) {
        JLabel lb = new JLabel(s);
        lb.setForeground(new Color(0xB8C4D4));
        return lb;
    }

    private JComponent value(String s, Color fg, Color bg) {
        JTextArea ta = new JTextArea(s);
        ta.setWrapStyleWord(true);
        ta.setLineWrap(true);
        ta.setEditable(false);
        ta.setOpaque(true);
        ta.setForeground(fg);
        ta.setBackground(bg);
        ta.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        return ta;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}