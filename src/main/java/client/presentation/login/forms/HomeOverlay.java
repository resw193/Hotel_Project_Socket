package client.presentation.login.forms;

import client.presentation.login.components.EventHomeOverlay;
import client.presentation.login.components.HeaderButton;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.util.Animator;
import com.formdev.flatlaf.util.CubicBezierEasing;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.List;

public class HomeOverlay extends JWindow {

    private final PanelOverlay overlay;

    public HomeOverlay(JFrame frame, List<ModelLocation> locations) {
        super(frame);
        setBackground(new Color(35, 96, 135, 80));
        setLayout(new BorderLayout());
        overlay = new PanelOverlay(locations);
        add(overlay);
        setSize(frame.getSize());
        setLocationRelativeTo(frame);
    }

    public PanelOverlay getOverlay() {
        return overlay;
    }

    public static class PanelOverlay extends JPanel {
        private final List<ModelLocation> locations;
        private final MigLayout migLayout;
        private EventHomeOverlay eventHomeOverlay;
        private AnimationType animationType = AnimationType.NONE;
        private final Animator animator;
        private final Animator loginAnimator;
        private float animate;
        private int index;
        private boolean showLogin;
        private JPanel header;
        private JTextPane textTitle;
        private JTextPane textDescription;
        private Login panelLogin;

        public PanelOverlay(List<ModelLocation> locations) {
            this.locations = locations;
            setOpaque(false);
            migLayout = new MigLayout("fill,insets 10 180 10 180", "fill", "[grow 0][]");
            setLayout(migLayout);
            createHeader();
            createPageButton();
            createLogin();

            JPanel panel = new JPanel(new MigLayout("wrap", "", "[]30[]"));
            panel.setOpaque(false);
            textTitle = new JTextPane();
            textDescription = new JTextPane();
            textTitle.setOpaque(false);
            textDescription.setOpaque(false);
            textTitle.setEditable(false);
            textDescription.setEditable(false);
            textTitle.putClientProperty(FlatClientProperties.STYLE, "font:bold +40;border:0,0,0,0;foreground:#FFFFFF");
            textDescription.putClientProperty(FlatClientProperties.STYLE, "font:bold +2;border:0,0,0,0;foreground:#EAF2FF");
            panel.add(textTitle);
            panel.add(textDescription);
            add(panel, "width 50%!");

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    runLoginAnimation(false);
                }
            });

            animator = new Animator(500, new Animator.TimingTarget() {
                @Override
                public void timingEvent(float v) {
                    animate = v;
                    repaint();
                }

                @Override
                public void end() {
                    if (animationType == AnimationType.CLOSE_VIDEO && eventHomeOverlay != null) {
                        eventHomeOverlay.onChanged(index);
                        SwingUtilities.invokeLater(() -> runAnimation(index, AnimationType.SHOW_VIDEO));
                    } else {
                        animationType = AnimationType.NONE;
                    }
                }
            });
            animator.setInterpolator(CubicBezierEasing.EASE_IN);

            loginAnimator = new Animator(500, new Animator.TimingTarget() {
                @Override
                public void timingEvent(float v) {
                    float f = showLogin ? v : 1f - v;
                    int x = (int) ((350 + 180) * f);
                    migLayout.setComponentConstraints(panelLogin, "pos 100%-" + x + " 0.5al,w 350");
                    revalidate();
                }
            });
            loginAnimator.setInterpolator(CubicBezierEasing.EASE);

            if (!locations.isEmpty()) {
                setIndex(0);
            }
        }

        public void setEventHomeOverlay(EventHomeOverlay eventHomeOverlay) {
            this.eventHomeOverlay = eventHomeOverlay;
        }

        public void setIndex(int index) {
            this.index = index;
            ModelLocation location = locations.get(index);
            textTitle.setText(location.getTitle());
            textDescription.setText(location.getDescription());
        }

        private void createHeader() {
            header = new JPanel(new MigLayout("fill", "[]push[][]"));
            header.setOpaque(false);

            JLabel title = new JLabel("MIMOSA");
            title.putClientProperty(FlatClientProperties.STYLE, "font:bold +10;foreground:#FFFFFF");

            HeaderButton miniGame = new HeaderButton("MINI GAME");
            miniGame.putClientProperty(FlatClientProperties.STYLE,
                    "font:bold +3;borderWidth:0;focusWidth:0;innerFocusWidth:0;arc:20;foreground:#FFFFFF");
            miniGame.addActionListener(e -> JOptionPane.showMessageDialog(this,
                    "Mini game có thể được nối lại sau khi bạn migrate xong phần presentation chính.",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE));

            HeaderButton login = new HeaderButton("ĐĂNG NHẬP");
            login.putClientProperty(FlatClientProperties.STYLE,
                    "font:bold +3;borderWidth:0;focusWidth:0;innerFocusWidth:0;arc:20;foreground:#FFFFFF");
            login.addActionListener(e -> runLoginAnimation(true));

            header.add(title);
            header.add(miniGame);
            header.add(login);
            add(header, "wrap");
        }

        private void createLogin() {
            panelLogin = new Login();
            add(panelLogin, "pos 100% 0.5al,w 350");
        }

        private void createPageButton() {
            JPanel panel = new JPanel(new MigLayout("gapx 20"));
            panel.setOpaque(false);
            for (int i = 0; i < locations.size(); i++) {
                JButton cmd = new JButton();
                cmd.putClientProperty(FlatClientProperties.STYLE,
                        "margin:5,5,5,5;arc:999;borderWidth:0;focusWidth:0;innerFocusWidth:0;selectedBackground:$Component.accentColor");
                cmd.setCursor(new Cursor(Cursor.HAND_CURSOR));
                final int newIndex = i;
                cmd.addActionListener(e -> {
                    if (index != newIndex) {
                        boolean act = runAnimation(newIndex, AnimationType.CLOSE_VIDEO);
                        if (act) {
                            setSelectedButton(panel, newIndex);
                        }
                    }
                });
                panel.add(cmd);
            }
            add(panel, "pos 0.5al 80%");
            setSelectedButton(panel, index);
        }

        private void setSelectedButton(JPanel panel, int index) {
            for (int i = 0; i < panel.getComponentCount(); i++) {
                ((JButton) panel.getComponent(i)).setSelected(i == index);
            }
        }

        private boolean runAnimation(int index, AnimationType animationType) {
            if (!animator.isRunning()) {
                this.animate = 0;
                this.animationType = animationType;
                this.index = index;
                animator.start();
                return true;
            }
            return false;
        }

        private void runLoginAnimation(boolean show) {
            this.showLogin = show;
            if (loginAnimator.isRunning()) {
                loginAnimator.stop();
            }
            loginAnimator.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();
            int height = getHeight();
            float alpha = animationType == AnimationType.NONE ? 0.15f : Math.min(0.35f, 0.15f + (animate * 0.2f));
            g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
            Shape oval = new Ellipse2D.Double(width - 380, -120, 520, 520);
            Area area = new Area(oval);
            g2.setColor(Color.WHITE);
            g2.fill(area);
            g2.dispose();
        }

        private enum AnimationType {
            NONE, CLOSE_VIDEO, SHOW_VIDEO
        }
    }
}
