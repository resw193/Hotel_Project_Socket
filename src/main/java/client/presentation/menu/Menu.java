package client.presentation.menu;

import client.presentation.menu.mode.LightDarkMode;
import client.presentation.menu.mode.ToolBarAccentColor;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Menu extends JPanel {

    private String[][] items;
    private String role;

    private final String[][] FULL_MENU = {
            {"Trang chủ"},
            {"About"},
            {"Dashboard"},
            {"Tra cứu"},
            {"Phòng"},
            {"Đặt phòng"},
            {"Hóa đơn"},
            {"Nhân viên"},
            {"Khách hàng", "Thông tin khách hàng", "Khuyến mãi", "Thống kê"},
            {"Dịch vụ"},
            {"Thông tin cá nhân"},
            {"Trợ giúp"},
            {"Đăng xuất"}
    };

    private final String[][] RECEPTION_MENU = {
            {"Trang chủ"},
            {"About"},
            {"Dashboard"},
            {"Tra cứu"},
            {"Phòng"},
            {"Đặt phòng"},
            {"Hóa đơn"},
            {"Khách hàng", "Thông tin khách hàng", "Khuyến mãi", "Thống kê"},
            {"Thông tin cá nhân"},
            {"Trợ giúp"},
            {"Đăng xuất"}
    };

    private final List<MenuEvent> events = new ArrayList<>();
    private boolean menuFull = true;
    private final String headerName = "MIMOSA Hotel";

    protected final boolean hideMenuTitleOnMinimum = true;
    protected final int menuTitleLeftInset = 10;
    protected final int menuTitleVgap = 12;
    protected final int menuMaxWidth = 270;
    protected final int menuMinWidth = 72;
    protected final int headerFullHgap = 8;

    private JLabel header;
    private JScrollPane scroll;
    private JPanel panelMenu;
    private LightDarkMode lightDarkMode;
    private ToolBarAccentColor toolBarAccentColor;

    public Menu() {
        this(null);
    }

    public Menu(String role) {
        this.role = role;

        if (role != null && role.equalsIgnoreCase("Lễ tân")) {
            items = RECEPTION_MENU;
        } else {
            items = FULL_MENU;
        }
        init();
    }

    boolean isReceptionMenu() {
        return role != null && role.equalsIgnoreCase("Lễ tân");
    }

    boolean isManagerMenu() {
        return !isReceptionMenu();
    }

    private void init() {
        setLayout(new MenuLayout());
        setBackground(Color.WHITE);
        setBorder(new javax.swing.border.AbstractBorder() {});

        putClientProperty(FlatClientProperties.STYLE, "arc:12;background:#0B1F33");

        header = new JLabel(headerName);
        header.setFont(new Font("SansSerif", Font.BOLD, 20));
        header.setIcon(new ImageIcon(getClass().getResource("/other/logo/mimosa_hotel_logo.jpg")));
        header.setForeground(new Color(240, 244, 255));
        header.putClientProperty(FlatClientProperties.STYLE, "border:8,10,8,10");

        scroll = new JScrollPane();
        panelMenu = new JPanel(new MenuItemLayout(this));
        panelMenu.setBackground(new Color(0x0F2A47));
        panelMenu.putClientProperty(FlatClientProperties.STYLE, "border:6,8,6,8;background:#0F2A47");

        scroll.setViewportView(panelMenu);
        scroll.setBackground(Color.WHITE);
        scroll.setBorder(null);

        JScrollBar vscroll = scroll.getVerticalScrollBar();
        vscroll.setUnitIncrement(14);
        vscroll.putClientProperty(
                FlatClientProperties.STYLE,
                "width:10; background:#0B1F33; track:#0B1F33; thumb:#1F4069; trackArc:999"
        );

        createMenu();

        lightDarkMode = new LightDarkMode();
        toolBarAccentColor = new ToolBarAccentColor(this);

        // giống project cũ: mặc định false
        toolBarAccentColor.setVisible(FlatUIUtils.getUIBoolean("AccentControl.show", false));

        add(header);
        add(scroll);
        add(lightDarkMode);
        add(toolBarAccentColor);
    }

    private void createMenu() {
        int index = 0;
        for (String[] def : items) {
            MenuItem menuItem = new MenuItem(this, def, index++, events);
            panelMenu.add(menuItem);
        }
    }

    public boolean isMenuFull() {
        return menuFull;
    }

    public int getMenuMaxWidth() {
        return menuMaxWidth;
    }

    public int getMenuMinWidth() {
        return menuMinWidth;
    }

    public boolean isHideMenuTitleOnMinimum() {
        return hideMenuTitleOnMinimum;
    }

    public int getMenuTitleLeftInset() {
        return menuTitleLeftInset;
    }

    public int getMenuTitleVgap() {
        return menuTitleVgap;
    }

    public void setMenuFull(boolean menuFull) {
        this.menuFull = menuFull;
        if (menuFull) {
            header.setText(headerName);
            header.setHorizontalAlignment(getComponentOrientation().isLeftToRight() ? JLabel.LEFT : JLabel.RIGHT);
        } else {
            header.setText("");
            header.setHorizontalAlignment(JLabel.CENTER);
        }

        for (Component com : panelMenu.getComponents()) {
            if (com instanceof MenuItem) {
                ((MenuItem) com).setFull(menuFull);
            }
        }

        lightDarkMode.setMenuFull(menuFull);
        toolBarAccentColor.setMenuFull(menuFull);
    }

    public void setSelectedMenu(int index, int subIndex) {
        runEvent(index, subIndex);
    }

    protected void setSelected(int index, int subIndex) {
        int size = panelMenu.getComponentCount();
        for (int i = 0; i < size; i++) {
            Component com = panelMenu.getComponent(i);
            if (com instanceof MenuItem) {
                MenuItem item = (MenuItem) com;
                if (item.getMenuIndex() == index) {
                    item.setSelectedIndex(subIndex);
                } else {
                    item.setSelectedIndex(-1);
                }
            }
        }
    }

    protected void runEvent(int index, int subIndex) {
        MenuAction menuAction = new MenuAction();
        for (MenuEvent event : events) {
            event.menuSelected(index, subIndex, menuAction);
        }
        if (!menuAction.isCancel()) {
            setSelected(index, subIndex);
        }
    }

    public void addMenuEvent(MenuEvent event) {
        events.add(event);
    }

    public void hideMenuItem() {
        for (Component com : panelMenu.getComponents()) {
            if (com instanceof MenuItem) {
                ((MenuItem) com).hideMenuItem();
            }
        }
        revalidate();
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
                return new Dimension(5, 5);
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
                Insets insets = parent.getInsets();
                int x = insets.left;
                int y = insets.top;

                int gap = UIScale.scale(5);
                int sheaderFullHgap = UIScale.scale(headerFullHgap);

                int width = parent.getWidth() - (insets.left + insets.right);
                int height = parent.getHeight() - (insets.top + insets.bottom);

                int iconWidth = width;
                int iconHeight = header.getPreferredSize().height;

                int hgap = menuFull ? sheaderFullHgap : 0;
                int accentColorHeight = toolBarAccentColor.isVisible()
                        ? toolBarAccentColor.getPreferredSize().height + gap
                        : 0;

                header.setBounds(x + hgap, y, iconWidth - (hgap * 2), iconHeight);

                int ldgap = UIScale.scale(10);
                int ldWidth = width - ldgap * 2;
                int ldHeight = lightDarkMode.getPreferredSize().height;
                int ldx = x + ldgap;
                int ldy = y + height - ldHeight - ldgap - accentColorHeight;

                int menux = x;
                int menuy = y + iconHeight + gap;
                int menuWidth = width;
                int menuHeight = height - (iconHeight + gap) - (ldHeight + ldgap * 2) - accentColorHeight;

                scroll.setBounds(menux, menuy, menuWidth, menuHeight);
                lightDarkMode.setBounds(ldx, ldy, ldWidth, ldHeight);

                if (toolBarAccentColor.isVisible()) {
                    int tbheight = toolBarAccentColor.getPreferredSize().height;
                    int tbwidth = Math.min(toolBarAccentColor.getPreferredSize().width, ldWidth);
                    int tby = y + height - tbheight - ldgap;
                    int tbx = ldx + ((ldWidth - tbwidth) / 2);
                    toolBarAccentColor.setBounds(tbx, tby, tbwidth, tbheight);
                }
            }
        }
    }
}