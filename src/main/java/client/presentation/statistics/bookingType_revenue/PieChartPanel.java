package client.presentation.statistics.bookingType_revenue;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.util.LinkedHashMap;
import java.util.Map;

public class PieChartPanel extends JPanel {

    private String title = "";
    private LinkedHashMap<String, Double> data = new LinkedHashMap<>();
    private Insets padding = new Insets(16, 16, 16, 16);

    private static final Color LIGHT_TEXT = new Color(0xFDE68A);
    private static final Color DARK_TEXT = new Color(0x111827);

    private float animProgress = 1f;
    private Timer animTimer;

    public PieChartPanel() {
        setOpaque(true);
        setBackground(new Color(0x0B1F33));
        setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }

    public void setData(String title, LinkedHashMap<String, Double> map) {
        this.title = title != null ? title : "";
        this.data = map != null ? map : new LinkedHashMap<>();
        startAnimation();
    }

    public void setPadding(Insets p) {
        if (p != null) this.padding = p;
        repaint();
    }

    private void startAnimation() {
        if (animTimer != null && animTimer.isRunning()) {
            animTimer.stop();
        }
        animProgress = 0f;

        animTimer = new Timer(16, e -> {
            animProgress += 0.015f;
            if (animProgress >= 1f) {
                animProgress = 1f;
                ((Timer) e.getSource()).stop();
            }
            repaint();
        });
        animTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int legendW = 130;
        int cxTitle = padding.left + legendW;

        g2.setFont(getFont().deriveFont(Font.PLAIN, 12f));
        FontMetrics fm = g2.getFontMetrics();
        int titleH = (title == null || title.isEmpty()) ? 0 : fm.getHeight();
        int titleBaseY = padding.top + fm.getAscent();

        int contentTop = padding.top + titleH + 6;
        int availH = h - contentTop - padding.bottom;
        int availW = w - padding.left - padding.right - legendW;
        int size = Math.max(60, Math.min(availW, availH));

        if (titleH > 0) {
            g2.setColor(new Color(0xB8C4D4));
            g2.drawString(title, cxTitle, titleBaseY);
        }

        double sum = data.values().stream().mapToDouble(v -> v == null ? 0 : v).sum();
        if (sum <= 0) {
            g2.setColor(new Color(0x7DD3FC));
            String msg = "Không có dữ liệu";
            g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
            g2.dispose();
            return;
        }

        int x = cxTitle + 10;
        int y = contentTop;
        g2.translate(x, y);

        int diameter = size;
        int inner = (int) (diameter * 0.58);

        float eased = (float) (1 - Math.pow(1 - animProgress, 3));
        float start = 90f + 360f * (1f - eased);

        Color[] palette = {
                new Color(0xFACC15),
                new Color(0xEF4444),
                new Color(0x3B82F6),
                new Color(0x10B981),
                new Color(0xA855F7),
                new Color(0xF59E0B)
        };

        float curStart = start;
        int i = 0;
        for (Map.Entry<String, Double> e : data.entrySet()) {
            double v = e.getValue() == null ? 0 : e.getValue();
            if (v <= 0) continue;

            float extent = (float) (v / sum * 360.0);
            g2.setColor(palette[i % palette.length]);
            g2.fill(new Arc2D.Float(0, 0, diameter, diameter, curStart, -extent, Arc2D.PIE));
            curStart -= extent;
            i++;
        }

        g2.setColor(getBackground());
        g2.fillOval((diameter - inner) / 2, (diameter - inner) / 2, inner, inner);

        float start2 = start;
        int cxPie = diameter / 2;
        int cyPie = diameter / 2;
        double outerR = diameter / 2.0;
        double innerR = inner / 2.0;
        double labelR = (outerR + innerR) / 2.0;

        float percentFontSize = Math.max(13f, diameter * 0.045f);
        g2.setFont(getFont().deriveFont(Font.BOLD, percentFontSize));
        FontMetrics fmPercent = g2.getFontMetrics();

        i = 0;
        for (Map.Entry<String, Double> e : data.entrySet()) {
            double v = e.getValue() == null ? 0 : e.getValue();
            if (v <= 0) continue;

            double angle = v / sum * 360.0;
            float midAngle = (float) (start2 - angle / 2.0);
            double rad = Math.toRadians(midAngle);

            double lx = cxPie + Math.cos(rad) * labelR;
            double ly = cyPie - Math.sin(rad) * labelR;

            double percent = v / sum * 100.0;
            String lbl = (percent >= 10 || Math.abs(percent - Math.round(percent)) < 0.001)
                    ? String.format("%.0f%%", percent)
                    : String.format("%.1f%%", percent);

            int sw = fmPercent.stringWidth(lbl);
            int sh = fmPercent.getAscent();

            Color sliceColor = palette[i % palette.length];
            Color labelColor = getContrastColor(sliceColor);

            g2.setColor(labelColor);
            g2.drawString(
                    lbl,
                    (int) Math.round(lx - sw / 2.0),
                    (int) Math.round(ly + sh / 2.0 - 2)
            );

            start2 -= angle;
            i++;
        }

        g2.translate(-x, -y);

        int legendY = contentTop + 6;
        i = 0;
        g2.setFont(getFont().deriveFont(Font.PLAIN, 12f));
        fm = g2.getFontMetrics();

        for (Map.Entry<String, Double> e : data.entrySet()) {
            Color c = palette[i % palette.length];

            g2.setColor(c);
            g2.fillRect(padding.left, legendY - fm.getAscent() + 2, 10, 10);

            g2.setColor(new Color(0xE9EEF6));
            g2.drawString(e.getKey(), padding.left + 16, legendY);

            legendY += fm.getHeight() + 4;
            i++;
        }

        g2.dispose();
    }

    private static Color getContrastColor(Color c) {
        double yiq = (c.getRed() * 299 + c.getGreen() * 587 + c.getBlue() * 114) / 1000.0;
        return yiq >= 180 ? DARK_TEXT : LIGHT_TEXT;
    }
}