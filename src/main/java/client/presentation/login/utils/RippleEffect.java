package client.presentation.login.utils;

import com.formdev.flatlaf.util.Animator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class RippleEffect {

    private final Component component;
    private Color rippleColor = new Color(255, 255, 255);
    private final List<Effect> effects = new ArrayList<>();

    public RippleEffect(Component component) {
        this.component = component;
        init();
    }

    private void init() {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    addEffect(e.getPoint());
                }
            }
        });
    }

    public void addEffect(Point location) {
        effects.add(new Effect(component, location));
    }

    public void render(Graphics g, Shape contain) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (Effect effect : new ArrayList<>(effects)) {
            effect.render(g2, contain);
        }
        g2.dispose();
    }

    public void setRippleColor(Color rippleColor) {
        this.rippleColor = rippleColor;
    }

    public Color getRippleColor() {
        return rippleColor;
    }

    private final class Effect {
        private final Component component;
        private final Point location;
        private final Animator animator;
        private float animate;

        private Effect(Component component, Point location) {
            this.component = component;
            this.location = location;
            this.animator = new Animator(500, new Animator.TimingTarget() {
                @Override
                public void timingEvent(float fraction) {
                    animate = fraction;
                    component.repaint();
                }

                @Override
                public void end() {
                    effects.remove(Effect.this);
                }
            });
            animator.start();
        }

        private void render(Graphics2D g2, Shape contain) {
            Area area = new Area(contain);
            area.intersect(new Area(getShape(getSize(contain.getBounds2D()))));
            g2.setColor(rippleColor);
            float alpha = 0.3f;
            if (animate >= 0.7f) {
                double t = animate - 0.7f;
                alpha = (float) (alpha - (alpha * (t / 0.3f)));
            }
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.7f));
            g2.fill(area);
        }

        private Shape getShape(double size) {
            double s = size * animate;
            return new Ellipse2D.Double(location.getX() - s, location.getY() - s, s * 2, s * 2);
        }

        private double getSize(Rectangle2D rec) {
            double size;
            if (rec.getWidth() > rec.getHeight()) {
                size = location.getX() < rec.getWidth() / 2 ? rec.getWidth() - location.getX() : location.getX();
            } else {
                size = location.getY() < rec.getHeight() / 2 ? rec.getHeight() - location.getY() : location.getY();
            }
            return size + (size * 0.1f);
        }
    }
}
