package client.presentation.login.main;
import client.network.socket.SocketSessionManager;
import client.presentation.core.PlaceholderPresentationFactory;
import client.presentation.core.PresentationFactory;
import client.presentation.core.ServiceRegistry;
import client.presentation.core.SessionContext;
import client.presentation.login.forms.Home;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import common.dto.EmployeeDTO;
import common.dto.request_dto.LoginResultDTO;
import server.core.repository.*;
import server.core.repository.impl.*;
import server.core.service.*;
import server.core.service.impl.*;
import server.infrastructure.db.Neo4jConnManager;
import server.infrastructure.mapper.GenericDataMapper;
import server.infrastructure.mapper.impl.JacksonDataMapper;

import javax.swing.*;
import java.awt.*;

public class Application extends JFrame {

    private static Application app;

    private static AccountService accountService;
    private static EmployeeService employeeService;
    private static EmployeeTypeService employeeTypeService;
    private static PresentationFactory presentationFactory;

    public static final SessionContext session = new SessionContext();

    private static final String SOCKET_HOST = "localhost";
    private static final int SOCKET_PORT = 1111;

    private MainForm mainForm;
    private final Home home;

    public Application(AccountService accountService,
                       EmployeeService employeeService,
                       EmployeeTypeService employeeTypeService,
                       PresentationFactory presentationFactory) {
        Application.accountService = accountService;
        Application.employeeService = employeeService;
        Application.employeeTypeService = employeeTypeService;
        Application.presentationFactory = presentationFactory != null
                ? presentationFactory
                : new PlaceholderPresentationFactory();

        initComponents();
        setSize(new Dimension(1400, 800));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        home = new Home();
        home.setVisible(true);
        setContentPane(home);

        getRootPane().putClientProperty("JRootPane.useWindowDecorations", true);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                home.initOverlay(Application.this);
                home.play(0);
            }

            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                home.stop();
            }
        });
    }

    public static void bootstrap(AccountService accountService, EmployeeService employeeService, EmployeeTypeService employeeTypeService, PresentationFactory presentationFactory) {
        FlatRobotoFont.install();
        UIManager.put("defaultFont", new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        FlatMacDarkLaf.setup();

        EventQueue.invokeLater(() -> {
            JFrame owner = new JFrame();
            owner.setUndecorated(true);
            owner.setSize(0, 0);
            owner.setLocationRelativeTo(null);

            client.presentation.loadingscreen.SplashScreen splash =
                    new client.presentation.loadingscreen.SplashScreen(owner, true);

            splash.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    app = new Application(accountService, employeeService, employeeTypeService, presentationFactory);
                    app.setVisible(true);
                }
            });

            splash.setVisible(true);
        });
    }

    public static Application getInstance() {
        return app;
    }

    public static SessionContext getSession() {
        return session;
    }

    public static AccountService getAccountService() {
        return accountService;
    }

    public static EmployeeService getEmployeeService() {
        return employeeService;
    }

    public static EmployeeTypeService getEmployeeTypeService() {
        return employeeTypeService;
    }

    public static String getSocketHost() {
        return SOCKET_HOST;
    }

    public static int getSocketPort() {
        return SOCKET_PORT;
    }

    public static void showForm(Component component) {
        if (app != null && app.mainForm != null) {
            component.applyComponentOrientation(app.getComponentOrientation());
            app.mainForm.showForm(component);
        }
    }

    public static void loginSuccess(String username, String password, LoginResultDTO loginResult) {
        if (app == null) {
            throw new IllegalStateException("Application chưa được khởi tạo.");
        }

        if (loginResult == null) {
            throw new IllegalStateException("Dữ liệu đăng nhập trả về không hợp lệ.");
        }

        try {
            SocketSessionManager.init(getSocketHost(), getSocketPort());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Không thể kết nối socket server: " + e.getMessage(),
                    "Lỗi kết nối",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        EmployeeDTO employee = new EmployeeDTO();
        employee.setEmployeeId(loginResult.getEmployeeId());
        employee.setFullName(loginResult.getEmployeeName());
        employee.setEmployeeTypeId(loginResult.getEmployeeTypeId());
        employee.setEmployeeTypeName(loginResult.getEmployeeTypeName());
        employee.setImgSource(loginResult.getImgSource());
        employee.setGender(loginResult.isGender());

        session.setUsername(username);
        session.setPassword(password);
        session.setEmployee(employee);
        session.setRole(employee.getEmployeeTypeName());

        String role = employee.getEmployeeTypeName() == null
                ? ""
                : employee.getEmployeeTypeName().trim();

        JOptionPane.showMessageDialog(
                null,
                "Bạn đã đăng nhập thành công với vai trò là " + role
        );

        FlatAnimatedLafChange.showSnapshot();

        app.home.disableOverlay();

        app.mainForm = new MainForm(session, employeeService, employeeTypeService);
        app.setContentPane(app.mainForm);
        app.mainForm.applyComponentOrientation(app.getComponentOrientation());
        app.mainForm.hideMenu();
        SwingUtilities.updateComponentTreeUI(app.mainForm);

        app.revalidate();
        app.repaint();

        app.mainForm.setSelectedMenu(2, 0);
        app.mainForm.showForm(new client.presentation.dashboard.FormDashboard(
                session.getEmployee()));

        SwingUtilities.invokeLater(() -> {
            app.toFront();
            app.requestFocus();
            app.mainForm.requestFocusInWindow();
        });

        FlatAnimatedLafChange.hideSnapshotWithAnimation();
    }

    public static void logout() {
        if (app == null) {
            return;
        }

        SocketSessionManager.shutdown();
        session.clear();

        FlatAnimatedLafChange.showSnapshot();
        app.setContentPane(app.home);
        app.home.applyComponentOrientation(app.getComponentOrientation());
        SwingUtilities.updateComponentTreeUI(app.home);
        app.home.enableOverlay(app);
        app.home.play(0);
        FlatAnimatedLafChange.hideSnapshotWithAnimation();
    }

    public static void setSelectedMenu(int index, int subIndex) {
        if (app != null && app.mainForm != null) {
            app.mainForm.setSelectedMenu(index, subIndex);
        }
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 719, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 521, Short.MAX_VALUE)
        );

        pack();
    }

    public static void main(String[] args) {
        Neo4jConnManager connManager = new Neo4jConnManager();

        GenericDataMapper mapper = new JacksonDataMapper();
        IdGeneratorService idGeneratorService = new IdGeneratorServiceImpl(connManager);

        // Repository
        AccountRepository accountRepository = new AccountRepositoryImpl(connManager, mapper);
        EmployeeTypeRepository employeeTypeRepository = new EmployeeTypeRepositoryImpl(connManager, mapper);
        EmployeeRepository employeeRepository = new EmployeeRepositoryImpl(connManager, mapper);
        CustomerRepository customerRepository = new CustomerRepositoryImpl(connManager, mapper);
        ServiceRepository serviceRepository = new ServiceRepositoryImpl(connManager, mapper);

        RoomTypeRepository roomTypeRepository = new RoomTypeRepositoryImpl(connManager, mapper);
        RoomRepository roomRepository = new RoomRepositoryImpl(connManager, mapper);
        PromotionRepository promotionRepository = new PromotionRepositoryImpl(connManager, mapper);
        RoomStayRepository roomStayRepository = new RoomStayRepositoryImpl(connManager, mapper);

        OrderRepository orderRepository = new OrderRepositoryImpl(connManager, mapper);
        OrderDetailRoomRepository orderDetailRoomRepository = new OrderDetailRoomRepositoryImpl(connManager, mapper);
        OrderDetailServiceRepository orderDetailServiceRepository = new OrderDetailServiceRepositoryImpl(connManager, mapper);

        ServiceRankingRepository serviceRankingRepository = new ServiceRankingRepositoryImpl(connManager, mapper);
        BookingTypeRevenueRepository bookingTypeRepository = new BookingTypeRevenueRepositoryImpl(connManager, mapper);
        OrderStatisticsRepository orderStatisticsRepository = new OrderStatisticsRepositoryImpl(connManager, mapper);
        DashboardRepository dashboardRepository = new DashboardRepositoryImpl(connManager, mapper);

        // Service
        AccountService accountService = new AccountServiceImpl(accountRepository, mapper);
        EmployeeTypeService employeeTypeService = new EmployeeTypeServiceImpl(employeeTypeRepository, mapper);
        EmployeeService employeeService = new EmployeeServiceImpl(employeeRepository, employeeTypeRepository, accountRepository, idGeneratorService, mapper);
        CustomerService customerService = new CustomerServiceImpl(customerRepository, idGeneratorService, mapper);
        ServiceService serviceService = new ServiceServiceImpl(serviceRepository, idGeneratorService, mapper);

        RoomTypeService roomTypeService = new RoomTypeServiceImpl(roomTypeRepository, mapper);
        RoomService roomService = new RoomServiceImpl(roomRepository, roomTypeRepository, idGeneratorService, mapper);
        PromotionService promotionService = new PromotionServiceImpl(promotionRepository, idGeneratorService, mapper);
        RoomStayService roomStayService = new RoomStayServiceImpl(roomStayRepository, mapper);
        OrderService orderService = new OrderServiceImpl(orderRepository, orderDetailRoomRepository, orderDetailServiceRepository, mapper);

        ServiceRankingService serviceRankingService = new ServiceRankingServiceImpl(serviceRankingRepository);
        BookingTypeRevenueService bookingTypeRevenueService = new BookingTypeRevenueServiceImpl(bookingTypeRepository);
        OrderStatisticsService orderStatisticsService = new OrderStatisticsServiceImpl(orderStatisticsRepository);

        DashboardService dashboardService = new DashboardServiceImpl(dashboardRepository);

        ServiceRegistry.init(
                employeeService,
                employeeTypeService,
                customerService,
                serviceService,
                roomService,
                roomTypeService,
                promotionService,
                roomStayService,
                orderService,
                serviceRankingService,
                bookingTypeRevenueService,
                orderStatisticsService,
                dashboardService
        );

        bootstrap(accountService, employeeService, employeeTypeService, new PlaceholderPresentationFactory());
    }
}