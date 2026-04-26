package client.presentation.statistics.order_statistics;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class BarChartPanel extends JPanel {
    private String title = "";
    private final LinkedHashMap<String, Double> data = new LinkedHashMap<>();
    private Insets padding = new Insets(16, 16, 24, 16);

    private static final Color BG = new Color(0x0B1F33);
    private static final Color CARD_BG = new Color(0x102D4A);
    private static final Color BORDER = new Color(0x274A6B);
    private static final Color TEXT = new Color(0xE6F1FF);
    private static final Color MUTED = new Color(0x9FB6CC);
    private static final Color GRID = new Color(0x1F3B57);

    private static final Color[] BARS = {
            new Color(0x22D3EE),
            new Color(0xF59E0B),
            new Color(0x10B981),
            new Color(0xA855F7)
    };

    private float animProgress = 1f;
    private Timer animTimer;

    public BarChartPanel() {
        setOpaque(true);
        setBackground(BG);
        setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }

    public void setPadding(Insets padding) {
        if (padding != null) {
            this.padding = padding;
            repaint();
        }
    }

    public void setData(String title, LinkedHashMap<String, Double> data) {
        this.title = title != null ? title : "";
        this.data.clear();
        if (data != null) {
            this.data.putAll(data);
        }
        startAnimation();
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

        g2.setColor(CARD_BG);
        g2.fillRoundRect(0, 0, w, h, 16, 16);
        g2.setColor(BORDER);
        g2.drawRoundRect(0, 0, w - 1, h - 1, 16, 16);

        g2.setFont(getFont().deriveFont(Font.BOLD, 14f));
        FontMetrics fmTitle = g2.getFontMetrics();
        int titleY = padding.top + fmTitle.getAscent();

        g2.setColor(TEXT);
        g2.drawString(title, padding.left, titleY);

        if (data.isEmpty()) {
            String msg = "Không có dữ liệu";
            int msgW = fmTitle.stringWidth(msg);
            g2.drawString(msg, (w - msgW) / 2, h / 2);
            g2.dispose();
            return;
        }

        int chartTop = titleY + 8;
        int chartBottom = h - padding.bottom - fmTitle.getHeight() - 4;
        int chartHeight = Math.max(10, chartBottom - chartTop);
        int chartLeft = padding.left + 8;
        int chartRight = w - padding.right - 8;
        int chartWidth = Math.max(10, chartRight - chartLeft);

        double max = 0;
        for (double v : data.values()) {
            if (v > max) max = v;
        }
        if (max <= 0) max = 1;

        float eased = (float) (1 - Math.pow(1 - animProgress, 3));

        g2.setColor(GRID);
        g2.drawLine(chartLeft, chartBottom, chartRight, chartBottom);

        int n = data.size();
        double slot = chartWidth * 1.0 / n;

        g2.setFont(getFont().deriveFont(12f));
        FontMetrics fm = g2.getFontMetrics();

        int i = 0;
        for (Map.Entry<String, Double> e : data.entrySet()) {
            double v = e.getValue() == null ? 0 : e.getValue();
            double ratio = v / max;
            int barHeight = (int) Math.round(ratio * (chartHeight - 8) * eased);
            int barWidth = (int) Math.round(slot * 0.5);

            int centerX = (int) Math.round(chartLeft + slot * (i + 0.5));
            int x = centerX - barWidth / 2;
            int y = chartBottom - barHeight;

            g2.setColor(BARS[i % BARS.length]);
            g2.fillRoundRect(x, y, barWidth, barHeight, 10, 10);

            String valueStr = formatNumber(v);
            int valueW = fm.stringWidth(valueStr);
            int valueX = centerX - valueW / 2;
            int valueY = y - 4;
            g2.setColor(TEXT);
            g2.drawString(valueStr, valueX, valueY);

            String label = e.getKey();
            int labelW = fm.stringWidth(label);
            int labelX = centerX - labelW / 2;
            int labelY = chartBottom + fm.getAscent() + 2;
            g2.setColor(MUTED);
            g2.drawString(label, labelX, labelY);

            i++;
        }

        g2.dispose();
    }

    private static String formatNumber(double v) {
        if (v >= 1_000_000_000) return String.format("%.1fB", v / 1_000_000_000d);
        if (v >= 1_000_000) return String.format("%.1fM", v / 1_000_000d);
        if (v >= 1_000) return String.format("%.1fk", v / 1_000d);
        return String.format("%.0f", v);
    }
}