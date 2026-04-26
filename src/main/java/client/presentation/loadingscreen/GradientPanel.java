package client.presentation.loadingscreen;

import javax.swing.*;
import java.awt.*;

public class GradientPanel extends JPanel {

    private final Color top;
    private final Color bottom;

    public GradientPanel(Color top, Color bottom) {
        this.top = top;
        this.bottom = bottom;
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int h = getHeight();
        Graphics2D g2 = (Graphics2D) g.create();

        GradientPaint g1 = new GradientPaint(0,0, top, 0,h, bottom);
        g2.setPaint(g1);
        g2.fillRect(0,0,getWidth(),h);

        // lớp mờ trung gian
        g2.setPaint(new GradientPaint(0, 0, new Color(255,255,255,18),
                0, h/2f, new Color(0,0,0,0)));
        g2.fillRect(0, 0, getWidth(), h/2);

        // lớp mờ phía dưới tạo chiều sâu
        g2.setPaint(new GradientPaint(0, h*0.6f, new Color(0,0,0,0),
                0, h, new Color(0,0,0,90)));
        g2.fillRect(0, (int)(h*0.6), getWidth(), (int)(h*0.4));

        g2.dispose();
    }
}
