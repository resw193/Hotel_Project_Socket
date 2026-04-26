package client.presentation.dashboard;

import com.formdev.flatlaf.FlatClientProperties;
import net.miginfocom.swing.MigLayout;
import other.QRCodeUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URI;

public class FormTrangChu extends JPanel {

    private final JLayeredPane layered = new JLayeredPane();
    private final CoverImage cover;
    private final JPanel pnOverlay = new JPanel();
    private final JLabel lblTitle = new JLabel("MIMOSA HOTEL - Booking & Management");
    private final JPanel pnBox = new JPanel(new MigLayout("wrap, insets 8 8 8 8", "[grow]", "[]8[]8[]"));
    private final JLabel lblQR = new JLabel();

    public FormTrangChu() {
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(new Color(0x0E2237));

        // Ảnh trang chủ
        cover = new CoverImage("/other/hotel.jpg");

        // Lớp phủ
        pnOverlay.setOpaque(false);
        pnOverlay.setLayout(null);

        // Title của hotel
        lblTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +10;foreground:#F2C94C;");
        pnOverlay.add(lblTitle);

        // QR code
        lblQR.setHorizontalAlignment(SwingConstants.CENTER);
        lblQR.setVerticalAlignment(SwingConstants.CENTER);
        lblQR.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        lblQR.setOpaque(false);
        try {
            BufferedImage img = QRCodeUtil.generate("https://www.booking.com/apps.html", 260);
            lblQR.setIcon(new ImageIcon(img));
        } catch (Exception ignored) {}

        // appstore, googleplay
        JButton btnAppStore = storeButton("App Store", "/other/appstore.jpg",
                "https://apps.apple.com/app/booking-com-hotel-reservations/id367003839");
        JButton btnPlay     = storeButton("Google Play", "/other/googleplay.jpg",
                "https://play.google.com/store/apps/details?id=com.booking");

        // Box chứa QR + button
        pnBox.setOpaque(false);
        pnBox.add(lblQR, "growx");
        pnBox.add(btnAppStore, "growx");
        pnBox.add(btnPlay, "growx");
        pnOverlay.add(pnBox);

        // Lớp xếp chồng để che
        layered.add(cover,   JLayeredPane.DEFAULT_LAYER);
        layered.add(pnOverlay, JLayeredPane.PALETTE_LAYER);

        add(layered, BorderLayout.CENTER);

        // Sắp xếp khi thay đổi kích thước
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) { layoutAll(); }
            @Override public void componentShown  (java.awt.event.ComponentEvent e) { layoutAll(); }
        });
    }

    private JButton storeButton(String text, String iconPath, String url) {
        JButton b = new JButton(text, loadIcon(iconPath));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.putClientProperty(FlatClientProperties.STYLE,
                "background:#F2C94C;foreground:#0B1F33;arc:14;borderWidth:0;focusWidth:0;innerFocusWidth:0;margin:6,12,6,12;iconTextGap:8");
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> openURL(url));
        // làm nút “nổi” nhẹ khi hover
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.putClientProperty(FlatClientProperties.STYLE, "background:#FFD35B;foreground:#0B1F33;arc:14;borderWidth:0;focusWidth:0;innerFocusWidth:0;margin:6,12,6,12;iconTextGap:8"); }
            @Override public void mouseExited (MouseEvent e) { b.putClientProperty(FlatClientProperties.STYLE, "background:#F2C94C;foreground:#0B1F33;arc:14;borderWidth:0;focusWidth:0;innerFocusWidth:0;margin:6,12,6,12;iconTextGap:8"); }
        });
        return b;
    }

    private ImageIcon loadIcon(String path) {
        java.net.URL u = getClass().getResource(path);
        return (u != null) ? new ImageIcon(u) : null;
    }

    private void openURL(String url) {
        try { Desktop.getDesktop().browse(new URI(url)); } catch (Exception ignored) {}
    }

    private void layoutAll() {
        int w = getWidth(), h = getHeight();
        layered.setBounds(0, 0, w, h);
        cover.setBounds(0, 0, w, h);
        pnOverlay.setBounds(0, 0, w, h);

        // Title góc trái trên
        Dimension t = lblTitle.getPreferredSize();
        int margin = 18;
        lblTitle.setBounds(margin, margin, t.width, t.height);

        // Box QR + nút ở góc phải trên
        Dimension b = pnBox.getPreferredSize();
        int bx = w - b.width - 32;
        int by = margin + t.height + 20; // dưới title ít px
        pnBox.setBounds(bx, by, b.width, b.height);

        // yêu cầu vẽ lại
        pnOverlay.revalidate();
        pnOverlay.repaint();
    }

    // ảnh cover tooàn bộ bên phải
    private static class CoverImage extends JComponent {
        private final Image img;
        public CoverImage(String resourcePath) {
            ImageIcon icon = new ImageIcon(getResource(resourcePath));
            img = icon.getImage();
            setOpaque(true);
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img == null) return;
            int w = getWidth(), h = getHeight();
            int iw = img.getWidth(null), ih = img.getHeight(null);
            if (iw <= 0 || ih <= 0) return;

            double scale = Math.max(w / (double) iw, h / (double) ih);
            int nw = (int) Math.round(iw * scale);
            int nh = (int) Math.round(ih * scale);
            int x = (w - nw) / 2;
            int y = (h - nh) / 2;
            g.drawImage(img, x, y, nw, nh, null);
        }
        private static java.net.URL getResource(String path) {
            return FormTrangChu.class.getResource(path);
        }
    }
}
