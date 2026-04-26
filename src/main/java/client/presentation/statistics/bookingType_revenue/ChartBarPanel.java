package client.presentation.statistics.bookingType_revenue;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.function.Function;

public class ChartBarPanel extends JPanel {

    private String title = "";
    private List<String> labels = List.of();
    private List<Double> values = List.of();
    private Function<Double, String> formatter = v -> String.valueOf(v.intValue());

    private Insets padding = new Insets(28, 80, 32, 24);
    private int yLabelGap = 8;
    private int barGap = 8;
    private int barArc = 10;

    private float animProgress = 1f;
    private Timer animTimer;

    public ChartBarPanel() {
        setOpaque(true);
        setBackground(new Color(0x0B1F33));
        setForeground(new Color(0x22D3EE));
    }

    public void setData(String title, List<String> labels, List<Double> values, Function<Double, String> formatter) {
        this.title = title != null ? title : "";
        this.labels = labels != null ? labels : List.of();
        this.values = values != null ? values : List.of();
        this.formatter = formatter != null ? formatter : (v -> String.valueOf(v.intValue()));
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

    public void setPadding(Insets p) {
        if (p != null) this.padding = p;
        repaint();
    }

    public void setYLabelGap(int g) {
        this.yLabelGap = Math.max(0, g);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        Font titleFont = getFont().deriveFont(Font.PLAIN, 12f);
        FontMetrics fm = g2.getFontMetrics(titleFont);

        int longest = 0;
        for (String s : labels) {
            if (s != null) longest = Math.max(longest, fm.stringWidth(s));
        }

        int dynamicLeft = Math.max(padding.left, longest + 16);
        Insets pad = new Insets(padding.top, dynamicLeft, padding.bottom, padding.right);

        g2.setColor(new Color(0xB8C4D4));
        g2.setFont(titleFont);
        int titleY = pad.top - 10;
        if (!title.isEmpty() && titleY > 0) {
            g2.drawString(title, pad.left, titleY);
        }

        int gx = pad.left;
        int gy = pad.top;
        int gw = w - pad.left - pad.right;
        int gh = h - pad.top - pad.bottom;

        if (gw <= 0 || gh <= 0 || values.isEmpty()) {
            drawEmpty(g2, w, h);
            g2.dispose();
            return;
        }

        g2.setColor(new Color(0x0E253D));
        g2.fillRoundRect(gx, gy, gw, gh, 12, 12);

        double max = 0;
        for (Double v : values) {
            max = Math.max(max, v != null ? v : 0);
        }
        if (max == 0) {
            drawEmpty(g2, w, h);
            g2.dispose();
            return;
        }

        float eased = (float) (1 - Math.pow(1 - animProgress, 3));

        int n = values.size();
        int slotH = gh / Math.max(1, n);
        int barH = Math.max(12, slotH - barGap);

        Color barColor = new Color(0x22D3EE);
        Color valueColor = new Color(0xE9EEF6);
        Color labelColor = new Color(0xE9EEF6);

        g2.setFont(getFont().deriveFont(Font.PLAIN, 12f));
        FontMetrics labelFm = g2.getFontMetrics();

        for (int i = 0; i < n; i++) {
            String label = labels.get(i) == null ? "-" : labels.get(i);
            double val = values.get(i) == null ? 0 : values.get(i);
            int y = gy + i * slotH + (slotH - barH) / 2;

            g2.setColor(labelColor);
            g2.drawString(
                    label,
                    gx - yLabelGap - labelFm.stringWidth(label),
                    y + barH / 2 + labelFm.getAscent() / 2 - 2
            );

            int bw = (int) Math.round(val / max * gw * eased);
            bw = Math.max(0, Math.min(bw, gw));

            g2.setColor(barColor);
            g2.fill(new RoundRectangle2D.Float(gx, y, bw, barH, barArc, barArc));

            String txt = formatter.apply(val);
            int tw = labelFm.stringWidth(txt);
            int tx;
            int ty = y + barH / 2 + labelFm.getAscent() / 2 - 2;

            int outerX = gx + bw + 6;
            int innerX = gx + bw - tw - 6;

            if (bw >= tw + 14) {
                tx = innerX;
                g2.setColor(new Color(0x0B1F33));
            } else {
                tx = Math.min(outerX, gx + gw - tw);
                g2.setColor(valueColor);
            }

            g2.drawString(txt, tx, ty);
        }

        g2.dispose();
    }

    private void drawEmpty(Graphics2D g2, int w, int h) {
        String msg = "Không có dữ liệu";
        g2.setFont(getFont().deriveFont(Font.PLAIN, 13f));
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(new Color(0x7DD3FC));
        g2.drawString(msg, (w - fm.stringWidth(msg)) / 2, h / 2);
    }
}