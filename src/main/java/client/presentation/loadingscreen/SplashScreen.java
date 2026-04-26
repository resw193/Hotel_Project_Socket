package client.presentation.loadingscreen;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;


public class SplashScreen extends JDialog {

    private CurvesPanel background;
    private ProgressBarCustom progress;
    private JLabel lbStatus;

    public SplashScreen(Frame parent, boolean modal) {
        super(parent, modal);
        setUndecorated(true);
        setBackground(new Color(0,0,0,0));
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        background = new CurvesPanel();
        background.setLayout(new GridBagLayout());      // center
        setContentPane(background);

        JPanel card = new GlassCard();
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(26, 28, 22, 28));

        // Logo
        JLabel lbLogo = new JLabel();
        lbLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbLogo.setIcon(loadRoundedIcon("/other/logo/mimosa_hotel_logo.jpg", 320));
        card.add(lbLogo);
        card.add(Box.createVerticalStrut(18));

        // thanh loading
        progress = new ProgressBarCustom();
        progress.setAlignmentX(Component.CENTER_ALIGNMENT);
        progress.setPreferredSize(new Dimension(Math.max(260, lbLogo.getPreferredSize().width - 40), 10));
        progress.setStringPainted(true);
        card.add(progress);
        card.add(Box.createVerticalStrut(8));

        // Status text
        lbStatus = new JLabel("Đang khởi động…", SwingConstants.CENTER);
        lbStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbStatus.setForeground(new Color(230, 240, 255));
        lbStatus.setFont(lbStatus.getFont().deriveFont(Font.PLAIN, 13f));
        card.add(lbStatus);

        // Đặt card vào đúng giữa màn hình
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        background.add(card, gbc);

        // Kích thước splash
        setSize(980, 560);
        setLocationRelativeTo(null);
    }

    private static ImageIcon loadRoundedIcon(String path, int targetWidth) {
        try {
            URL url = SplashScreen.class.getResource(path);
            if (url == null) return null;

            BufferedImage src = ImageIO.read(url);
            int w = targetWidth;
            int h = (int) ((double) src.getHeight() / src.getWidth() * w);

            Image scaled = src.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = out.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = Math.max(18, w / 12);
            Shape clip = new RoundRectangle2D.Float(0, 0, w, h, arc, arc);
            g2.setClip(clip);
            g2.drawImage(scaled, 0, 0, null);
            g2.dispose();
            return new ImageIcon(out);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();

        // Bắt đầu animation nền
        background.start();
    }

    @Override
    public void removeNotify() {
        background.stop();
        super.removeNotify();
    }

    private void doTask(String text, int value) throws Exception {
        lbStatus.setText(text);
        progress.setValue(value);
        Thread.sleep(350);
    }

    // Demo chạy đơn lẻ
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SplashScreen d = new SplashScreen(new JFrame(), true);
            d.setVisible(true);
        });
    }

    // Khi cửa sổ mở: chạy chuỗi task rồi đóng
    @Override
    protected void dialogInit() {
        super.dialogInit();
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowOpened(java.awt.event.WindowEvent e) {
                new Thread(() -> {
                    try {
                        doTask("Kết nối tới ứng dụng", 15);
                        doTask("Loading", 20);
                        doTask("Loading", 20);
                        doTask("Loading", 20);
                        doTask("Nạp tài nguyên…", 46);
                        doTask("Khởi tạo mô-đun…", 63);
                        doTask("Đồng bộ dữ liệu…", 81);
                        doTask("Hoàn tất.", 100);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        // đóng splash
                        SwingUtilities.invokeLater(() -> {
                            dispose();
                            background.stop();
                        });
                    }
                }).start();
            }
        });
    }

    private static final class GlassCard extends JPanel {
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int arc = 24;

            // bóng đổ
            g2.setColor(new Color(0,0,0,120));
            g2.fillRoundRect(10, 12, w-20, h-20, arc+6, arc+6);

            // lớp kính
            GradientPaint gp = new GradientPaint(0, 0, new Color(255,255,255,60),
                    0, h, new Color(255,255,255,18));
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            // viền phát sáng
            g2.setStroke(new BasicStroke(1.6f));
            g2.setColor(new Color(80, 220, 255, 180));
            g2.drawRoundRect(1, 1, w-2, h-2, arc, arc);

            g2.dispose();
        }
    }
}
