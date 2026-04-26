package client.presentation.login.main;

import client.presentation.core.SessionContext;
import client.presentation.customer.FormCustomerManagement;
import client.presentation.dashboard.*;
import client.presentation.employee.FormEmployeeManagement;
import client.presentation.menu.MenuAction;
import client.presentation.order.FormOrderManagement;
import client.presentation.profile.FormProfileInfo;
import client.presentation.promotion.FormPromotionManagement;
import client.presentation.room.FormRoomManagement;
import client.presentation.roomBooking.FormRoomBookingManagement;
import client.presentation.service.FormServiceManagement;
import client.presentation.statistics.FormThongKe;
import client.presentation.statistics.bookingType_revenue.FormBookingTypeRevenueStats;
import client.presentation.statistics.order_statistics.FormOrderStatistics;
import client.presentation.statistics.service_statistics.FormServiceStatistics;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.util.UIScale;
import server.core.service.EmployeeService;
import server.core.service.EmployeeTypeService;
import client.presentation.core.ServiceRegistry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URL;

public class MainForm extends JLayeredPane {

    private final SessionContext session;
    private final EmployeeService employeeService;
    private final EmployeeTypeService employeeTypeService;

    private client.presentation.menu.Menu menu;
    private JPanel panelBody;
    private JButton menuButton;

    public MainForm(SessionContext session, EmployeeService employeeService, EmployeeTypeService employeeTypeService) {
        this.session = session;
        this.employeeService = employeeService;
        this.employeeTypeService = employeeTypeService;
        init();
    }

    private void init() {
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setLayout(new MainFormLayout());

        menu = new client.presentation.menu.Menu(session.getRole());
        panelBody = new JPanel(new BorderLayout());

        initMenuArrowIcon();

        menuButton.putClientProperty(
                FlatClientProperties.STYLE,
                "background:$Menu.button.background;arc:999;focusWidth:0;borderWidth:0"
        );
        menuButton.addActionListener((ActionEvent e) -> setMenuFull(!menu.isMenuFull()));

        initMenuEvent();

        setLayer(menuButton, JLayeredPane.POPUP_LAYER);
        add(menuButton);
        add(menu);
        add(panelBody);
    }

    @Override
    public void applyComponentOrientation(ComponentOrientation o) {
        super.applyComponentOrientation(o);
        initMenuArrowIcon();
    }

    private void initMenuArrowIcon() {
        if (menuButton == null) {
            menuButton = new JButton();
        }
        String iconName = getComponentOrientation().isLeftToRight()
                ? "menu_left.svg"
                : "menu_right.svg";
        menuButton.setIcon(loadSvg(iconName, 0.8f));
    }

    private Icon loadSvg(String name, float scale) {
        String[] candidates = {
                "/presentation/menu/icon/svg/" + name,
                "/client/presentation/menu/icon/svg/" + name
        };
        for (String p : candidates) {
            URL url = getClass().getResource(p);
            if (url != null) {
                return new FlatSVGIcon(String.valueOf(url), scale);
            }
        }
        return UIManager.getIcon("Tree.expandedIcon");
    }

    private void initMenuEvent() {
        menu.addMenuEvent((int index, int subIndex, MenuAction action) -> {

            if (session.isReceptionist()) {
                switch (index) {
                    case 0 -> showForm(new FormTrangChu());
                    case 1 -> showForm(new FormAbout());
                    case 2 -> showForm(new FormDashboard(session.getEmployee()));
                    case 3 -> showForm(new FormSearch());
                    case 4 -> showForm(new FormRoomManagement(ServiceRegistry.getRoomService(), ServiceRegistry.getRoomTypeService(), true));
                    case 5 -> showForm(new FormRoomBookingManagement(Application.session, ServiceRegistry.getRoomService(), ServiceRegistry.getRoomStayService(), ServiceRegistry.getServiceService()));
                    case 6 -> showForm(new FormOrderManagement(ServiceRegistry.getOrderService(), ServiceRegistry.getPromotionService()));

                    case 7 -> {
                        switch (subIndex) {
                            case 1 -> showForm(new FormCustomerManagement());
                            case 2 -> showForm(new FormPromotionManagement(ServiceRegistry.getPromotionService(), true));
                            case 3 -> showForm(new FormThongKe());
                            case 31 -> showForm(new FormServiceStatistics(ServiceRegistry.getServiceRankingService()));
                            case 33 -> showForm(new FormBookingTypeRevenueStats(ServiceRegistry.getBookingTypeRevenueService()));
                            default -> action.cancel();
                        }
                    }

                    case 8 -> showForm(new FormProfileInfo(employeeService));

                    case 9 -> showForm(new FormHelp(
                            session.getEmployee() != null ? session.getEmployee().getFullName() : "",
                            session.getRole()
                    ));

                    case 10 -> {
                        int choice = JOptionPane.showConfirmDialog(
                                this,
                                "Bạn có chắc muốn đăng xuất không?",
                                "Warning",
                                JOptionPane.YES_NO_OPTION
                        );
                        if (choice == JOptionPane.YES_OPTION) {
                            Application.logout();
                        } else {
                            action.cancel();
                        }
                    }

                    default -> action.cancel();
                }
                return;
            }

            switch (index) {
                case 0 -> showForm(new FormTrangChu());
                case 1 -> showForm(new FormAbout());
                case 2 -> showForm(new FormDashboard(session.getEmployee()));
                case 3 -> showForm(new FormSearch());

                case 4 -> showForm(new FormRoomManagement(ServiceRegistry.getRoomService(), ServiceRegistry.getRoomTypeService(), false));
                case 5 -> showForm(new FormRoomBookingManagement(Application.session, ServiceRegistry.getRoomService(), ServiceRegistry.getRoomStayService(), ServiceRegistry.getServiceService()));
                case 6 -> showForm(new FormOrderManagement(ServiceRegistry.getOrderService(), ServiceRegistry.getPromotionService()));
                case 7 -> showForm(new FormEmployeeManagement());

                case 8 -> {
                    switch (subIndex) {
                        case 1 -> showForm(new FormCustomerManagement());
                        case 2 -> showForm(new FormPromotionManagement(ServiceRegistry.getPromotionService(), false));
                        case 3 -> showForm(new FormThongKe());
                        case 31 -> showForm(new FormServiceStatistics(ServiceRegistry.getServiceRankingService()));
                        case 32 -> showForm(new FormOrderStatistics(ServiceRegistry.getOrderStatisticsService()));
                        case 33 -> showForm(new FormBookingTypeRevenueStats(ServiceRegistry.getBookingTypeRevenueService()));
                        default -> action.cancel();
                    }
                }

                case 9 -> showForm(new FormServiceManagement());
                case 10 -> showForm(new FormProfileInfo(employeeService));

                case 11 -> showForm(new FormHelp(
                        session.getEmployee() != null ? session.getEmployee().getFullName() : "",
                        session.getRole()
                ));

                case 12 -> {
                    int choice = JOptionPane.showConfirmDialog(
                            this,
                            "Bạn có chắc muốn đăng xuất không?",
                            "Warning",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (choice == JOptionPane.YES_OPTION) {
                        Application.logout();
                    } else {
                        action.cancel();
                    }
                }

                default -> action.cancel();
            }
        });
    }

    private void setMenuFull(boolean full) {
        String iconName;
        if (getComponentOrientation().isLeftToRight()) {
            iconName = full ? "menu_left.svg" : "menu_right.svg";
        } else {
            iconName = full ? "menu_right.svg" : "menu_left.svg";
        }
        menuButton.setIcon(loadSvg(iconName, 0.8f));
        menu.setMenuFull(full);
        revalidate();
    }

    public void hideMenu() {
        menu.hideMenuItem();
    }

    public void showForm(Component component) {
        panelBody.removeAll();
        panelBody.add(component);
        panelBody.repaint();
        panelBody.revalidate();

        SwingUtilities.invokeLater(() -> {
            panelBody.requestFocusInWindow();
            component.requestFocusInWindow();
        });
    }

    public void setSelectedMenu(int index, int subIndex) {
        menu.setSelectedMenu(index, subIndex);
    }

    private final class MainFormLayout implements LayoutManager {
        @Override
        public void addLayoutComponent(String name, Component comp) {
        }

        @Override
        public void removeLayoutComponent(Component comp) {
        }

        @Override
        public Dimension preferredLayoutSize(Container parent) {
            return new Dimension(5, 5);
        }

        @Override
        public Dimension minimumLayoutSize(Container parent) {
            return new Dimension(0, 0);
        }

        @Override
        public void layoutContainer(Container parent) {
            synchronized (parent.getTreeLock()) {
                boolean ltr = parent.getComponentOrientation().isLeftToRight();
                Insets insets = UIScale.scale(parent.getInsets());

                int x = insets.left;
                int y = insets.top;
                int width = parent.getWidth() - (insets.left + insets.right);
                int height = parent.getHeight() - (insets.top + insets.bottom);

                int menuWidth = UIScale.scale(
                        menu.isMenuFull() ? menu.getMenuMaxWidth() : menu.getMenuMinWidth()
                );
                int menuX = ltr ? x : x + width - menuWidth;
                menu.setBounds(menuX, y, menuWidth, height);

                int menuButtonWidth = menuButton.getPreferredSize().width;
                int menuButtonHeight = menuButton.getPreferredSize().height;

                int menuButtonX;
                if (ltr) {
                    menuButtonX = (int) (x + menuWidth - (menuButtonWidth * (menu.isMenuFull() ? 0.5f : 0.3f)));
                } else {
                    menuButtonX = (int) (menuX - (menuButtonWidth * (menu.isMenuFull() ? 0.5f : 0.7f)));
                }

                menuButton.setBounds(
                        menuButtonX,
                        UIScale.scale(30),
                        menuButtonWidth,
                        menuButtonHeight
                );

                int gap = UIScale.scale(5);
                int bodyWidth = width - menuWidth - gap;
                int bodyHeight = height;
                int bodyX = ltr ? (x + menuWidth + gap) : x;
                int bodyY = y;

                panelBody.setBounds(bodyX, bodyY, bodyWidth, bodyHeight);
            }
        }
    }
}