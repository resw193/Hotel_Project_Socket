package client.presentation.loadingscreen;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.GeneralPath;

public class CurvesPanel extends GradientPanel {

    private RenderingHints hints;
    private int tick = 0;
    private Timer timer = new Timer(16, e -> { tick++; repaint(); });

    public CurvesPanel() {
        super(new Color(0x0B1F33), new Color(0x071826));
        hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        start();
    }

    public void start(){
        timer.start();
    }
    public void stop(){
        timer.stop();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2=(Graphics2D) g.create();
        g2.setRenderingHints(hints);

        int w=getWidth(), h=getHeight();
        g2.translate(0, -10);
        drawWave(g2, h*0.18f, 20, 30, new Color(0, 217, 255, 90), new Color(0, 255, 170, 35));
        g2.translate(0, 10);
        g2.translate(0, h - 110);
        drawWave(g2, h*0.22f, 28, -26, new Color(0, 223, 194, 110), new Color(0, 188, 255, 40));
        g2.translate(0, -h + 110);
        drawWave(g2, h*0.32f, 20, 22, new Color(255, 255, 255, 70), new Color(255,255,255,0));

        g2.dispose();
    }

    private void drawWave(Graphics2D g2, float baseY, float thickness, float speed,
                          Color head, Color tail) {
        int w=getWidth(), h=getHeight();

        double off = Math.sin(tick/(Math.PI*speed));
        float y1 = baseY + (float)(off * 12);
        float y2 = baseY + (float)(off * -10);

        float cx1 = w/2f - 40, cy1 = 8 + (float)(off * -22);
        float cx2 = w/2f + 60, cy2 = (float)(h*0.06 + off*8);

        CubicCurve2D c = new CubicCurve2D.Float(0,y1, cx1,cy1, cx2,cy2, w,y2);

        GeneralPath p = new GeneralPath(c);
        p.lineTo(w, h);
        p.lineTo(0, h);
        p.closePath();

        Area a = new Area(p);
        AffineTransform t = AffineTransform.getTranslateInstance(0, thickness);
        p.transform(t);
        a.subtract(new Area(p));

        GradientPaint gp = new GradientPaint(0, c.getBounds().y, head, 0, a.getBounds().y + a.getBounds().height, tail);
        Paint old = g2.getPaint();
        g2.setPaint(gp);
        g2.fill(a);
        g2.setPaint(old);
    }
}
