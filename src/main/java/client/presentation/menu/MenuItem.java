package client.presentation.menu;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Path2D;
import java.util.List;

public class MenuItem extends JPanel {

    private final Menu menu;
    private final String[] menus;
    private final int menuIndex;
    private final List<MenuEvent> events;
    private boolean menuShow;
    private float animate;
    private PopupSubmenu popup;

    private final int menuItemHeight = 48;
    private final int subMenuItemHeight = 42;
    private final int subMenuLeftGap = 38;
    private final int firstGap = 10;
    private final int bottomGap = 10;

    public MenuItem(Menu menu, String[] menus, int menuIndex, List<MenuEvent> events) {
        this.menu = menu;
        this.menus = menus;
        this.menuIndex = menuIndex;
        this.events = events;
        init();
    }

    private void init() {
        setLayout(new MenuLayout());
        putClientProperty(FlatClientProperties.STYLE,
                "background:#3C4038;foreground:#6B8E23");

        for (int i = 0; i < menus.length; i++) {
            JButton menuItem = createButtonItem(menus[i]);
            menuItem.setHorizontalAlignment(
                    menuItem.getComponentOrientation().isLeftToRight()
                            ? JButton.LEADING
                            : JButton.TRAILING
            );

            if (i == 0) {
                menuItem.setIcon(getIcon());
                menuItem.addActionListener((ActionEvent e) -> {
                    if (menus.length > 1) {
                        if (menu.isMenuFull()) {
                            MenuAnimation.animate(this, !menuShow);
                        } else {
                            popup.show(this,
                                    (int) this.getWidth() + UIScale.scale(5),
                                    UIScale.scale(menuItemHeight) / 2);
                        }
                    } else {
                        menu.runEvent(menuIndex, 0);
                    }
                });
            } else {
                menuItem.setIcon(getSubIcon(menuIndex, i));

                boolean isKhachHangSection =
                        menus.length > 0 && "Khách hàng".equalsIgnoreCase(menus[0]);

                if (isKhachHangSection && "Thống kê".equalsIgnoreCase(menus[i])) {
                    JPopupMenu statsPopup = createStatsPopup(menuIndex);
                    menuItem.addActionListener((ActionEvent e) -> {
                        boolean ltr = getComponentOrientation().isLeftToRight();
                        int x = ltr
                                ? menuItem.getWidth() - UIScale.scale(8)
                                : UIScale.scale(8) - statsPopup.getPreferredSize().width;
                        int y = menuItem.getHeight() / 2;
                        statsPopup.show(menuItem, x, y);
                    });
                } else {
                    int subIndex = i;
                    menuItem.addActionListener((ActionEvent e) -> menu.runEvent(menuIndex, subIndex));
                }
            }
            add(menuItem);
        }

        popup = new PopupSubmenu(getComponentOrientation(), menu, menuIndex, menus);
    }

    private JPopupMenu createStatsPopup(int parentIndex) {
        JPopupMenu pm = new JPopupMenu();

        JMenuItem miSvc = new JMenuItem("Dịch vụ", getNestedIcon(parentIndex + "_3_1"));
        JMenuItem miBill = new JMenuItem("Hóa đơn", getNestedIcon(parentIndex + "_3_2"));
        JMenuItem miIncome = new JMenuItem("Thu nhập", getNestedIcon(parentIndex + "_3_3"));

        miSvc.addActionListener(e -> menu.runEvent(menuIndex, 31));
        miBill.addActionListener(e -> menu.runEvent(menuIndex, 32));
        miIncome.addActionListener(e -> menu.runEvent(menuIndex, 33));

        pm.add(miSvc);
        if (menu.isManagerMenu()) {
            pm.add(miBill);
        }
        pm.add(miIncome);
        return pm;
    }

    private Icon getNestedIcon(String key) {
        FlatSVGIcon.ColorFilter gold = new FlatSVGIcon.ColorFilter() {
            @Override
            public Color filter(Color c) {
                return new Color(242, 201, 76, c.getAlpha());
            }
        };

        String last = key.substring(key.lastIndexOf('_') + 1);
        String[] candidates = new String[]{
                "icon/svg/" + key + ".svg",
                "icon/svg/" + key.replace('_', '-') + ".svg",
                "icon/svg/6_3_" + last + ".svg"
        };

        ClassLoader cl = getClass().getClassLoader();
        for (String p : candidates) {
            if (cl.getResource(p) != null) {
                FlatSVGIcon ic = new FlatSVGIcon(p, 0.60f);
                ic.setColorFilter(gold);
                return ic;
            }
        }

        FlatSVGIcon ic = new FlatSVGIcon("icon/svg/8_3.svg", 0.60f);
        ic.setColorFilter(gold);
        return ic;
    }

    private Icon getIcon() {
        FlatSVGIcon icon = new FlatSVGIcon("icon/svg/" + menuIndex + ".svg", 0.75f);
        icon.setColorFilter(new FlatSVGIcon.ColorFilter() {
            @Override
            public Color filter(Color c) {
                return new Color(242, 201, 76, c.getAlpha());
            }
        });
        return icon;
    }

    private Icon getSubIcon(int parentIndex, int subIndex) {
        FlatSVGIcon.ColorFilter gold = new FlatSVGIcon.ColorFilter() {
            @Override
            public Color filter(Color c) {
                return new Color(242, 201, 76, c.getAlpha());
            }
        };

        String[] candidates = new String[]{
                "icon/svg/" + parentIndex + "_" + subIndex + ".svg",
                "icon/svg/" + parentIndex + "-" + subIndex + ".svg"
        };

        ClassLoader cl = getClass().getClassLoader();
        for (String p : candidates) {
            if (cl.getResource(p) != null) {
                FlatSVGIcon ic = new FlatSVGIcon(p, 0.60f);
                ic.setColorFilter(gold);
                return ic;
            }
        }

        FlatSVGIcon ic = new FlatSVGIcon("icon/svg/" + parentIndex + ".svg", 0.60f);
        ic.setColorFilter(gold);
        return ic;
    }

    private JButton createButtonItem(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Inter", Font.BOLD, 13));
        button.putClientProperty(FlatClientProperties.STYLE,
                "background:#102C49;foreground:#EAF2FF;" +
                        "rolloverForeground:#0B1F33;selectedBackground:#F2C94C;selectedForeground:#0B1F33;" +
                        "borderWidth:0;focusWidth:0;innerFocusWidth:0;arc:10;iconTextGap:14;margin:6,14,6,14");
        return button;
    }

    public boolean isMenuShow() {
        return menuShow;
    }

    public void setMenuShow(boolean menuShow) {
        this.menuShow = menuShow;
    }

    public float getAnimate() {
        return animate;
    }

    public void setAnimate(float animate) {
        this.animate = animate;
    }

    public String[] getMenus() {
        return menus;
    }

    public int getMenuIndex() {
        return menuIndex;
    }

    protected void setSelectedIndex(int index) {
        int size = getComponentCount();
        boolean selected = false;
        for (int i = 0; i < size; i++) {
            Component com = getComponent(i);
            if (com instanceof JButton) {
                ((JButton) com).setSelected(i == index);
                if (i == index) {
                    selected = true;
                }
            }
        }
        ((JButton) getComponent(0)).setSelected(selected);
        popup.setSelectedIndex(index);
    }

    public void hideMenuItem() {
        animate = 0;
        menuShow = false;
    }

    public void setFull(boolean full) {
        if (full) {
            int size = getComponentCount();
            for (int i = 0; i < size; i++) {
                Component com = getComponent(i);
                if (com instanceof JButton) {
                    JButton button = (JButton) com;
                    button.setText(menus[i]);
                    button.setHorizontalAlignment(
                            getComponentOrientation().isLeftToRight()
                                    ? JButton.LEFT
                                    : JButton.RIGHT
                    );
                }
            }
        } else {
            for (Component com : getComponents()) {
                if (com instanceof JButton) {
                    JButton button = (JButton) com;
                    button.setText("");
                    button.setHorizontalAlignment(JButton.CENTER);
                }
            }
            animate = 0f;
            menuShow = false;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (animate > 0) {
            int ssubMenuItemHeight = UIScale.scale(subMenuItemHeight);
            int ssubMenuLeftGap = UIScale.scale(subMenuLeftGap);
            int smenuItemHeight = UIScale.scale(menuItemHeight);
            int sfirstGap = UIScale.scale(firstGap);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Path2D.Double p = new Path2D.Double();
            int last = getComponent(getComponentCount() - 1).getY() + (ssubMenuItemHeight / 2);
            boolean ltr = getComponentOrientation().isLeftToRight();
            int round = UIScale.scale(8);
            int x = ltr ? (ssubMenuLeftGap - round) : (getWidth() - (ssubMenuLeftGap - round));

            p.moveTo(x, smenuItemHeight + sfirstGap);
            p.lineTo(x, last - round);

            for (int i = 1; i < getComponentCount(); i++) {
                int com = getComponent(i).getY() + (ssubMenuItemHeight / 2);
                p.append(createCurve(round, x, com, ltr), false);
            }

            g2.setColor(new Color(242, 201, 76));
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.setStroke(new BasicStroke(UIScale.scale(1f)));
            g2.draw(p);
            g2.dispose();
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (menus.length > 1) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.setColor(new Color(242, 201, 76));

            int smenuItemHeight = UIScale.scale(menuItemHeight);
            boolean ltr = getComponentOrientation().isLeftToRight();
            g2.setStroke(new BasicStroke(UIScale.scale(1f)));

            if (menu.isMenuFull()) {
                int arrowWidth = UIScale.scale(10);
                int arrowHeight = UIScale.scale(5);
                int ax = ltr ? (getWidth() - arrowWidth * 2) : arrowWidth;
                int ay = (smenuItemHeight - arrowHeight) / 2;

                Path2D p = new Path2D.Double();
                p.moveTo(0, animate * arrowHeight);
                p.lineTo(arrowWidth / 2, (1f - animate) * arrowHeight);
                p.lineTo(arrowWidth, animate * arrowHeight);

                g2.translate(ax, ay);
                g2.draw(p);
            } else {
                int arrowWidth = UIScale.scale(4);
                int arrowHeight = UIScale.scale(8);
                int ax = ltr ? (getWidth() - arrowWidth - UIScale.scale(3)) : UIScale.scale(3);
                int ay = (smenuItemHeight - arrowHeight) / 2;

                Path2D p = new Path2D.Double();
                if (ltr) {
                    p.moveTo(0, 0);
                    p.lineTo(arrowWidth, arrowHeight / 2);
                    p.lineTo(0, arrowHeight);
                } else {
                    p.moveTo(arrowWidth, 0);
                    p.lineTo(0, arrowHeight / 2);
                    p.lineTo(arrowWidth, arrowHeight);
                }

                g2.translate(ax, ay);
                g2.draw(p);
            }
            g2.dispose();
        }
    }

    private Shape createCurve(int round, int x, int y, boolean ltr) {
        Path2D p2 = new Path2D.Double();
        p2.moveTo(x, y - round);
        p2.curveTo(x, y - round, x, y, x + (ltr ? round : -round), y);
        return p2;
    }

    private class MenuLayout implements LayoutManager {
        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            synchronized (parent.getTreeLock()) {
                Insets inset = parent.getInsets();
                int width = parent.getWidth();
                int height = inset.top + inset.bottom;
                int size = parent.getComponentCount();
                Component item = parent.getComponent(0);

                height += UIScale.scale(menuItemHeight);
                if (item.isVisible()) {
                    int subMenuHeight = size > 1 ? UIScale.scale(firstGap) + UIScale.scale(bottomGap) : 0;
                    for (int i = 1; i < size; i++) {
                        Component com = parent.getComponent(i);
                        if (com.isVisible()) {
                            subMenuHeight += UIScale.scale(subMenuItemHeight);
                        }
                    }
                    height += (int) (subMenuHeight * animate);
                } else {
                    height = 0;
                }
                return new Dimension(width, height);
            }
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            synchronized (parent.getTreeLock()) {
                return new Dimension(0, 0);
            }
        }

        @Override
        public void layoutContainer(Container parent) {
            synchronized (parent.getTreeLock()) {
                boolean ltr = parent.getComponentOrientation().isLeftToRight();
                Insets insets = parent.getInsets();
                int x = insets.left;
                int y = insets.top;
                int width = parent.getWidth() - (insets.left + insets.right);
                int size = parent.getComponentCount();

                for (int i = 0; i < size; i++) {
                    Component com = parent.getComponent(i);
                    if (com.isVisible()) {
                        if (i == 0) {
                            int smenuItemHeight = UIScale.scale(menuItemHeight);
                            int sfirstGap = UIScale.scale(firstGap);
                            com.setBounds(x, y, width, smenuItemHeight);
                            y += smenuItemHeight + sfirstGap;
                        } else {
                            int ssubMenuLeftGap = UIScale.scale(subMenuLeftGap);
                            int subMenuX = ltr ? ssubMenuLeftGap : 0;
                            int ssubMenuItemHeight = UIScale.scale(subMenuItemHeight);
                            com.setBounds(x + subMenuX, y, width - ssubMenuLeftGap, ssubMenuItemHeight);
                            y += ssubMenuItemHeight;
                        }
                    }
                }
            }
        }
    }
}