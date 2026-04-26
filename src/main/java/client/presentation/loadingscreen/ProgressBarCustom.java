package client.presentation.loadingscreen;

import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;

public class ProgressBarCustom extends JProgressBar {

    private int phase = 0;
    private Timer anim = new Timer(30, e -> { phase = (phase + 3) % 120; repaint(); });

    public ProgressBarCustom() {
        setBorder(null);
        setOpaque(false);
        setForeground(new Color(255,255,255));
        setBackground(new Color(30, 45, 60));
        setUI(new BasicProgressBarUI() {
            @Override
            protected void paintDeterminate(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = c.getWidth(), h = c.getHeight();
                int arc = Math.min(h, 14);

                // track
                g2.setColor(new Color(255,255,255,30));
                g2.fillRoundRect(0, 0, w, h, arc, arc);

                // fill width
                int amt = (int) Math.round(progressBar.getPercentComplete() * w);

                // gradient 7 sắc
                float shift = phase / 120f;
                MultipleGradientPaint gp = new LinearGradientPaint(
                        0, 0, w, 0,
                        new float[]{0f, .16f, .33f, .50f, .66f, .83f, 1f},
                        new Color[]{
                                colorWheel(shift + 0f),
                                colorWheel(shift + .16f),
                                colorWheel(shift + .33f),
                                colorWheel(shift + .50f),
                                colorWheel(shift + .66f),
                                colorWheel(shift + .83f),
                                colorWheel(shift + 1f)
                        });
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, Math.max(arc, amt), h, arc, arc);

                // highlight
                g2.setPaint(new GradientPaint(0, 0, new Color(255,255,255,110),
                        0, h/2f, new Color(255,255,255,0)));
                g2.fillRoundRect(1, 1, Math.max(arc, amt-2), Math.max(2, h/2), arc-2, arc-2);

                // % text
                if (progressBar.isStringPainted()) {
                    String s = (int) (progressBar.getPercentComplete() * 100) + "%";
                    g2.setFont(g2.getFont().deriveFont(Font.BOLD, 11f));
                    FontMetrics fm = g2.getFontMetrics();
                    int sw = fm.stringWidth(s);
                    int sh = fm.getAscent();
                    g2.setColor(new Color(20,30,40,160));
                    g2.drawString(s, (w-sw)/2 + 1, (h+sh)/2);
                    g2.setColor(new Color(245, 250, 255));
                    g2.drawString(s, (w-sw)/2, (h+sh)/2 - 1);
                }
                g2.dispose();
            }
        });
        anim.start();
    }

    private static Color colorWheel(float t) {
        float v = (t % 1f + 1f) % 1f;
        return Color.getHSBColor(v, 0.85f, 1f);
    }
}
