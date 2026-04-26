package client.presentation.dashboard;

import com.formdev.flatlaf.FlatClientProperties;
import common.dto.*;
import net.miginfocom.swing.MigLayout;
import client.network.socket.SocketSessionManager;
import common.dto.request_dto.DashboardOverviewRequestDTO;
import common.dto.request_dto.DashboardSaveNoteRequestDTO;
import common.dto.request_dto.OrderStatisticsRequestDTO;
import common.protocol.command.CommandType;
import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Path2D;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FormDashboard extends JPanel {

    private final EmployeeDTO currentUser;

    private final boolean isManager;
    private String managerPlanText = "";
    private String managerAlertText = "";

    private JTable tableUpcoming;

    private JLabel lblRevenueBig, lblRevenueDelta;
    private RevenueChartPanel revenueChart;

    private GoalsSummaryPanel goalsSummaryPanel;
    private JLabel lblTargetRevenue;
    private JLabel lblRevenueGap;
    private JLabel lblTargetOcc;
    private JLabel lblOccStatus;
    private JPanel actionsPanel;
    private JPanel alertsPanel;

    private OccDonutPanel occDonut;
    private SplitDonutPanel bookedFreeDonut;
    private JLabel lbBookedLegend, lbFreeLegend;

    private JLabel lblTodayBookings;
    private JLabel lblTodayVisitors;
    private WeeklyCustomerComparePanel weeklyCustomerPanel;

    private Last7DaysBarChartPanel last7Chart;

    private ServiceTopPanel serviceTopPanel;
    private RoomTypeDistributionPanel roomTypePanel;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public FormDashboard(EmployeeDTO currentUser) {
        this.currentUser = currentUser;

        this.isManager = currentUser != null
                && currentUser.getEmployeeTypeName() != null
                && currentUser.getEmployeeTypeName().trim().equalsIgnoreCase("Quản lý");

        setLayout(new MigLayout("wrap,fill,insets 18 22 22 22", "[grow]"));
        setBackground(Color.decode("#0E2237"));

        add(createTopRow(), "growx, gapbottom 12");
        add(createRevenueSection(), "growx, gapbottom 10");
        add(createBottomRow(), "grow, push");

        reloadAll();
    }

    private JComponent createTopRow() {
        JPanel row = new JPanel(new MigLayout(
                "insets 0,gap 12",
                "[grow][grow][grow]",
                "[grow]"
        ));
        row.setOpaque(false);

        occDonut = new OccDonutPanel();
        row.add(cardDonut("Tỉ lệ lấp đầy phòng hiện tại", occDonut, null), "grow");

        bookedFreeDonut = new SplitDonutPanel();
        JPanel legend = new JPanel(new MigLayout("insets 0,gap 12", "[]12[]"));
        legend.setOpaque(false);
        lbBookedLegend = legendLabel("Đã đặt (0)", new Color(0x3498DB));
        lbFreeLegend = legendLabel("Trống (0)", new Color(0x7F8C8D));
        legend.add(lbBookedLegend);
        legend.add(lbFreeLegend);
        row.add(cardDonut("Phòng đã đặt / Trống hiện tại", bookedFreeDonut, legend), "grow");

        lblTodayBookings = new JLabel("--");
        lblTodayBookings.putClientProperty(FlatClientProperties.STYLE, "font:bold +10;foreground:#EAF2FF");

        lblTodayVisitors = new JLabel("--");
        lblTodayVisitors.putClientProperty(FlatClientProperties.STYLE, "font:bold +4;foreground:#EAF2FF");

        weeklyCustomerPanel = new WeeklyCustomerComparePanel();

        row.add(kpiCard(
                "Đặt phòng - Lượng khách ghé thăm",
                lblTodayBookings,
                lblTodayVisitors,
                weeklyCustomerPanel), "grow");

        return row;
    }

    private JPanel cardDonut(String title, JComponent chart, JComponent footer) {
        JPanel card = new JPanel(new MigLayout("insets 14 16 12 16", "[grow]", "[]8[grow]8[]"));
        card.putClientProperty(FlatClientProperties.STYLE, "arc:16;background:#0F2A47");
        JLabel l1 = new JLabel(title);
        l1.putClientProperty(FlatClientProperties.STYLE, "foreground:#BFD7FF;font:bold +1");
        card.add(l1, "wrap");
        card.add(chart, "w 100%, h 150!");
        if (footer != null) card.add(footer, "growx");
        return card;
    }

    private JLabel legendLabel(String text, Color c) {
        JLabel lb = new JLabel(text);
        lb.setIcon(new LegendIcon(c));
        lb.putClientProperty(FlatClientProperties.STYLE, "foreground:#EAF2FF");
        return lb;
    }

    private JPanel kpiCard(String title, JLabel bookingsLabel, JLabel visitorsLabel, JComponent weeklyPanel) {
        JPanel card = new JPanel(new MigLayout(
                "insets 14 16 14 16",
                "[grow]",
                "[]8[]4[]8[grow]"
        ));
        card.putClientProperty(FlatClientProperties.STYLE, "arc:16;background:#0F2A47");
        card.setPreferredSize(new Dimension(0, 230));

        JLabel l1 = new JLabel(title);
        l1.putClientProperty(FlatClientProperties.STYLE, "foreground:#BFD7FF;font:bold +1");

        JLabel lbBookingsCaption = new JLabel("Lượt đặt phòng hôm nay");
        lbBookingsCaption.putClientProperty(FlatClientProperties.STYLE, "foreground:#BFD7FF;font:-1");

        JLabel lbVisitorsCaption = new JLabel("Khách ghé thăm hôm nay");
        lbVisitorsCaption.putClientProperty(FlatClientProperties.STYLE, "foreground:#BFD7FF;font:-1");

        JPanel rowBookings = new JPanel(new MigLayout("insets 0,gapx 8", "[][grow]", "[]"));
        rowBookings.setOpaque(false);
        rowBookings.add(bookingsLabel, "gapbottom 0");
        rowBookings.add(lbBookingsCaption, "aligny bottom");

        JPanel rowVisitors = new JPanel(new MigLayout("insets 0,gapx 8", "[][grow]", "[]"));
        rowVisitors.setOpaque(false);
        rowVisitors.add(visitorsLabel, "gapbottom 0");
        rowVisitors.add(lbVisitorsCaption, "aligny bottom");

        card.add(l1, "wrap");
        card.add(rowBookings, "growx, wrap");
        card.add(rowVisitors, "growx, wrap");
        card.add(weeklyPanel, "growx, growy");

        return card;
    }

    private JComponent createRevenueSection() {
        JPanel revenueCard = new JPanel(new MigLayout(
                "insets 14 16 10 16",
                "[grow]",
                "[]6[]0[grow]"
        ));
        revenueCard.putClientProperty(FlatClientProperties.STYLE, "arc:16;background:#0F2A47");

        JLabel title = new JLabel("Doanh thu tháng này");
        title.putClientProperty(FlatClientProperties.STYLE, "foreground:#BFD7FF;font:bold +2");

        lblRevenueBig = new JLabel("--");
        lblRevenueBig.putClientProperty(FlatClientProperties.STYLE, "foreground:#EAF2FF;font:bold +8");

        lblRevenueDelta = new JLabel("");
        lblRevenueDelta.putClientProperty(FlatClientProperties.STYLE, "foreground:#9AE6B4;font:+0");

        revenueChart = new RevenueChartPanel();
        revenueChart.setPreferredSize(new Dimension(100, 210));

        revenueCard.add(title, "split 2");
        revenueCard.add(lblRevenueDelta, "al right, wrap");
        revenueCard.add(lblRevenueBig, "wrap");
        revenueCard.add(revenueChart, "span, growx, h 210!");

        JPanel right = new JPanel(new MigLayout(
                "insets 0,gap 12 0",
                "[grow 21][grow 2]",
                "[grow]"
        ));
        right.setOpaque(false);

        JPanel goals = new JPanel(new MigLayout(
                "insets 14 16 16 16",
                "[grow]",
                "[]6[80!]6[]4[grow]"
        ));
        goals.putClientProperty(FlatClientProperties.STYLE, "arc:16;background:#0F2A47");

        JLabel gTitle = new JLabel("Mục tiêu & kế hoạch tháng tới");
        gTitle.putClientProperty(FlatClientProperties.STYLE, "foreground:#EAF2FF;font:bold +2");
        goals.add(gTitle, "wrap");

        goalsSummaryPanel = new GoalsSummaryPanel();
        goals.add(goalsSummaryPanel, "growx, h 80!, wrap");

        JPanel goalsInfo = new JPanel(new MigLayout("insets 4 0 0 0, gapx 6, gapy 2", "[grow]", "[][]"));
        goalsInfo.setOpaque(false);

        lblTargetRevenue = new JLabel("Mục tiêu doanh thu tháng tới: --");
        lblTargetRevenue.putClientProperty(FlatClientProperties.STYLE, "foreground:#EAF2FF;font:+0");

        lblRevenueGap = new JLabel("Còn thiếu -- để đạt mục tiêu");
        lblRevenueGap.putClientProperty(FlatClientProperties.STYLE, "foreground:#F6E05E;font:-1");

        goalsInfo.add(lblTargetRevenue, "split 2");
        goalsInfo.add(lblRevenueGap, "gapleft 8, wrap");

        lblTargetOcc = new JLabel("Mục tiêu tỉ lệ lấp đầy phòng: --");
        lblTargetOcc.putClientProperty(FlatClientProperties.STYLE, "foreground:#EAF2FF;font:+0");

        lblOccStatus = new JLabel("");
        lblOccStatus.putClientProperty(FlatClientProperties.STYLE, "foreground:#9AE6B4;font:-1");

        goalsInfo.add(lblTargetOcc, "split 2");
        goalsInfo.add(lblOccStatus, "gapleft 8, wrap");

        JLabel actionsTitle = new JLabel("Hành động gợi ý");
        actionsTitle.putClientProperty(FlatClientProperties.STYLE, "foreground:#BFD7FF;font:bold");

        goals.add(goalsInfo, "growx, wrap");
        goals.add(actionsTitle, "wrap");

        actionsPanel = new JPanel(new MigLayout("insets 4 0 4 0, gapy 6, fillx, wrap", "[grow]", ""));
        actionsPanel.setOpaque(false);

        JScrollPane actionsScroll = new JScrollPane(actionsPanel);
        actionsScroll.setBorder(null);
        actionsScroll.setOpaque(false);
        actionsScroll.getViewport().setOpaque(false);
        actionsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        actionsScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        goals.add(actionsScroll, "grow, push");
        right.add(goals, "grow, pushy");

        JPanel alerts = new JPanel(new MigLayout("insets 14 16 16 16", "[grow]", "[]6[grow,fill]"));
        alerts.putClientProperty(FlatClientProperties.STYLE, "arc:16;background:#0F2A47");

        JLabel aTitle = new JLabel("Cảnh báo & Khuyến nghị");
        aTitle.putClientProperty(FlatClientProperties.STYLE, "foreground:#EAF2FF;font:bold +2");
        alerts.add(aTitle, "wrap");

        alertsPanel = new JPanel(new MigLayout("insets 6 0 0 0, gapy 6", "[grow]", ""));
        alertsPanel.setOpaque(false);

        JScrollPane alertsScroll = new JScrollPane(alertsPanel);
        alertsScroll.setBorder(null);
        alertsScroll.setOpaque(false);
        alertsScroll.getViewport().setOpaque(false);
        alertsScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        alertsScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        alerts.add(alertsScroll, "growx, growy, push");
        right.add(alerts, "grow, pushy");

        JPanel row = new JPanel(new MigLayout("insets 0,gap 12", "[grow 4][grow 2]"));
        row.setOpaque(false);
        row.add(revenueCard, "grow");
        row.add(right, "grow");

        return row;
    }

    private JComponent createBottomRow() {
        JPanel row = new JPanel(new MigLayout("insets 0,gap 12", "[grow][grow]"));
        row.setOpaque(false);

        JPanel left = new JPanel(new MigLayout("insets 0,gap 12 0", "[grow]", "[grow][grow]"));
        left.setOpaque(false);

        JPanel bestServiceCard = new JPanel(new MigLayout("insets 14 16 16 16", "[grow]", "[]8[grow]"));
        bestServiceCard.putClientProperty(FlatClientProperties.STYLE, "arc:16;background:#0F2A47");
        JLabel bestTitle = new JLabel("Dịch vụ bán chạy tháng này");
        bestTitle.putClientProperty(FlatClientProperties.STYLE, "foreground:#EAF2FF;font:bold +2");
        bestServiceCard.add(bestTitle, "wrap");
        serviceTopPanel = new ServiceTopPanel();
        bestServiceCard.add(serviceTopPanel, "grow");
        left.add(bestServiceCard, "grow");

        JPanel roomTypeCard = new JPanel(new MigLayout("insets 14 16 16 16", "[grow]", "[]8[grow]"));
        roomTypeCard.putClientProperty(FlatClientProperties.STYLE, "arc:16;background:#0F2A47");
        JLabel roomTitle = new JLabel("Phân bố loại phòng");
        roomTitle.putClientProperty(FlatClientProperties.STYLE, "foreground:#EAF2FF;font:bold +2");
        roomTypeCard.add(roomTitle, "wrap");
        roomTypePanel = new RoomTypeDistributionPanel();
        roomTypeCard.add(roomTypePanel, "grow");
        left.add(roomTypeCard, "grow");

        row.add(left, "grow");

        JPanel recent = new JPanel(new MigLayout(
                "insets 14 16 16 16",
                "[grow]",
                "[]8[grow]8[120!]"
        ));
        recent.putClientProperty(FlatClientProperties.STYLE, "arc:16;background:#0F2A47");
        JLabel lb = new JLabel("Hoạt động gần đây của khách hàng");
        lb.putClientProperty(FlatClientProperties.STYLE, "foreground:#EAF2FF;font:bold +2");
        recent.add(lb, "wrap");

        tableUpcoming = new JTable(new DefaultTableModel(
                new Object[][]{},
                new String[]{"Mã đơn", "Khách", "Phòng", "Check-in", "Check-out", "Trạng thái"}
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });
        tableUpcoming.setFillsViewportHeight(true);
        tableUpcoming.setRowHeight(26);
        tableUpcoming.setShowGrid(false);
        tableUpcoming.setForeground(new Color(0xEAF2FF));
        tableUpcoming.setBackground(new Color(0x0F2A47));
        tableUpcoming.setSelectionBackground(new Color(0x12355A));
        tableUpcoming.setSelectionForeground(Color.WHITE);
        tableUpcoming.getTableHeader().putClientProperty(
                FlatClientProperties.STYLE,
                "foreground:#BFD7FF;background:#102A43;font:bold"
        );

        JScrollPane scroll = new JScrollPane(tableUpcoming);
        scroll.getViewport().setBackground(new Color(0x0F2A47));
        recent.add(scroll, "grow, wrap");

        last7Chart = new Last7DaysBarChartPanel();
        recent.add(last7Chart, "growx, h 120!");

        row.add(recent, "grow");
        return row;
    }

    private void reloadAll() {
        LocalDate today = LocalDate.now();
        DashboardOverviewDTO overview = requestDashboardOverview(today, 7, 5);
        DashboardSummaryDTO summary = overview.getSummary();

        managerPlanText = overview.getManagerPlanText() == null ? "" : overview.getManagerPlanText();
        managerAlertText = overview.getManagerAlertText() == null ? "" : overview.getManagerAlertText();

        int total = summary != null ? summary.getTotalRooms() : 0;
        int busy = summary != null ? summary.getOccupiedNow() : 0;
        int free = Math.max(total - busy, 0);
        int todayBookings = summary != null ? summary.getBookingsToday() : 0;
        int todayVisitors = summary != null ? summary.getCustomersToday() : 0;

        int visitsThisWeek = todayVisitors;
        int visitsLastWeek = 0;

        occDonut.setData(busy, total);
        bookedFreeDonut.setValues(busy, free);
        lbBookedLegend.setText("Đã đặt (" + busy + ")");
        lbFreeLegend.setText("Trống (" + free + ")");

        lblTodayBookings.setText(String.valueOf(todayBookings));
        lblTodayVisitors.setText(String.valueOf(todayVisitors));
        weeklyCustomerPanel.setData(visitsThisWeek, visitsLastWeek);

        DefaultTableModel m = (DefaultTableModel) tableUpcoming.getModel();
        m.setRowCount(0);
        for (DashboardUpcomingBookingDTO u : overview.getUpcomingBookings()) {
            m.addRow(new Object[]{
                    u.getOrderCode(),
                    u.getCustomer(),
                    u.getRoom(),
                    formatDate(u.getCheckIn()),
                    formatDate(u.getCheckOut()),
                    u.getStatus()
            });
        }

        if (serviceTopPanel != null) {
            serviceTopPanel.setData(overview.getTopServices());
        }
        if (roomTypePanel != null) {
            roomTypePanel.setData(overview.getRoomTypeDistribution());
        }

        loadLast7DaysInvoiceChart();
        refreshRevenueAndInsights(total, busy);
        rebuildManagerPanels();
    }

    private void loadLast7DaysInvoiceChart() {
        List<LocalDate> days = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            OrderStatisticsDTO os = requestDailyOrderStatistics(d);
            int c = (os != null) ? os.getSoLuongHoaDon() : 0;
            days.add(d);
            counts.add(c);
        }

        if (last7Chart != null) {
            last7Chart.setData(days, counts);
        }
    }

    private void rebuildManagerPanels() {
        if (actionsPanel != null) {
            actionsPanel.removeAll();
            if (managerPlanText != null && !managerPlanText.isBlank()) {
                JTextArea viewPlan = area();
                viewPlan.setText(managerPlanText);
                actionsPanel.add(viewPlan, "growx, wrap");

                if (isManager) {
                    JButton btnChange = new JButton("Thay đổi");
                    btnChange.addActionListener(ev -> showEditablePlan(actionsPanel, viewPlan));
                    actionsPanel.add(btnChange, "right, wrap");
                }
            } else if (isManager) {
                JTextArea txtPlan = createEditableArea();
                JButton btnConfirmPlan = new JButton("Xác nhận");
                btnConfirmPlan.addActionListener(e -> {
                    boolean ok = requestSaveDashboardPlan(txtPlan.getText().trim(), currentUser != null ? currentUser.getEmployeeId() : null);
                    if (!ok) {
                        JOptionPane.showMessageDialog(
                                FormDashboard.this,
                                "Lưu hành động gợi ý thất bại. Vui lòng thử lại!",
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }
                    managerPlanText = txtPlan.getText().trim();
                    rebuildManagerPanels();
                });
                actionsPanel.add(new JScrollPane(txtPlan), "growx, wrap");
                actionsPanel.add(btnConfirmPlan, "right, wrap");
            }
            actionsPanel.revalidate();
            actionsPanel.repaint();
        }

        if (alertsPanel != null) {
            alertsPanel.removeAll();
            if (managerAlertText != null && !managerAlertText.isBlank()) {
                addAlert(managerAlertText, AlertLevel.HIGH);
                if (isManager) {
                    JButton btnChange = new JButton("Thay đổi");
                    btnChange.addActionListener(ev -> showEditableAlert(alertsPanel));
                    alertsPanel.add(btnChange, "right, wrap");
                }
            } else if (isManager) {
                JTextArea txtAlert = createEditableArea();
                JButton btnConfirm = new JButton("Xác nhận");
                btnConfirm.addActionListener(e -> {
                    boolean ok = requestSaveDashboardAlert(txtAlert.getText().trim(), currentUser != null ? currentUser.getEmployeeId() : null);
                    if (!ok) {
                        JOptionPane.showMessageDialog(
                                FormDashboard.this,
                                "Lưu cảnh báo & khuyến nghị thất bại.",
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }
                    managerAlertText = txtAlert.getText().trim();
                    rebuildManagerPanels();
                });
                alertsPanel.add(new JScrollPane(txtAlert), "growx, wrap");
                alertsPanel.add(btnConfirm, "right, wrap");
            }
            alertsPanel.revalidate();
            alertsPanel.repaint();
        }
    }

    private void showEditablePlan(JPanel parent, JTextArea viewPlan) {
        parent.removeAll();

        JTextArea edit = createEditableArea();
        edit.setText(managerPlanText);

        JButton btnSave = new JButton("Xác nhận");
        JButton btnCancel = new JButton("Hủy");

        btnSave.addActionListener(e -> {
            boolean ok = requestSaveDashboardPlan(edit.getText().trim(), currentUser != null ? currentUser.getEmployeeId() : null);
            if (!ok) {
                JOptionPane.showMessageDialog(
                        FormDashboard.this,
                        "Cập nhật hành động gợi ý thất bại.",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            managerPlanText = edit.getText().trim();
            viewPlan.setText(managerPlanText);

            parent.removeAll();
            parent.add(viewPlan, "growx, wrap");
            JButton btnChange = new JButton("Thay đổi");
            btnChange.addActionListener(ev -> showEditablePlan(parent, viewPlan));
            parent.add(btnChange, "right, wrap");
            parent.revalidate();
            parent.repaint();
        });

        btnCancel.addActionListener(e -> rebuildManagerPanels());

        parent.add(new JScrollPane(edit), "growx, wrap");
        parent.add(btnCancel, "split 2, right");
        parent.add(btnSave);
        parent.revalidate();
        parent.repaint();
    }

    private void showEditableAlert(JPanel parent) {
        parent.removeAll();

        JTextArea edit = createEditableArea();
        edit.setText(managerAlertText);

        JButton btnSave = new JButton("Xác nhận");
        JButton btnCancel = new JButton("Hủy");

        btnSave.addActionListener(e -> {
            boolean ok = requestSaveDashboardAlert(edit.getText().trim(), currentUser != null ? currentUser.getEmployeeId() : null);
            if (!ok) {
                JOptionPane.showMessageDialog(
                        FormDashboard.this,
                        "Cập nhật cảnh báo & khuyến nghị thất bại.",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            managerAlertText = edit.getText().trim();
            rebuildManagerPanels();
        });

        btnCancel.addActionListener(e -> rebuildManagerPanels());

        parent.add(new JScrollPane(edit), "growx, wrap");
        parent.add(btnCancel, "split 2, right");
        parent.add(btnSave);
        parent.revalidate();
        parent.repaint();
    }

    private JTextArea area() {
        JTextArea a = new JTextArea();
        a.setEditable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.putClientProperty(FlatClientProperties.STYLE,
                "background:#12355A;foreground:#EAF2FF;borderWidth:0;arc:12;font:+0");
        return a;
    }

    private JTextArea createEditableArea() {
        JTextArea a = new JTextArea(4, 20);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.putClientProperty(FlatClientProperties.STYLE,
                "background:#12355A;foreground:#EAF2FF;borderWidth:0;arc:12;font:+0;padding:6,8,6,8;");
        return a;
    }

    private void refreshRevenueAndInsights(int totalRooms, int busyRooms) {
        YearMonth ym = YearMonth.now();
        LocalDate first = ym.atDay(1);
        LocalDate last = LocalDate.now();

        List<LocalDate> daysOfMonth = new ArrayList<>();
        List<Double> daily = new ArrayList<>();
        double sumThisMonth = 0;

        for (LocalDate d = first; !d.isAfter(last); d = d.plusDays(1)) {
            double val = requestRevenueByDay(d);
            daysOfMonth.add(d);
            daily.add(val);
            sumThisMonth += val;
        }

        YearMonth prev = ym.minusMonths(1);
        double sumPrevMonth = requestRevenueByMonth(prev);

        Double deltaPercent = null;
        if (sumPrevMonth > 0) {
            deltaPercent = ((sumThisMonth - sumPrevMonth) / sumPrevMonth) * 100.0;
        }

        lblRevenueBig.setText(formatVN(sumThisMonth));

        String deltaText;
        String deltaStyle;
        if (deltaPercent == null) {
            if (sumPrevMonth == 0 && sumThisMonth == 0) {
                deltaText = "Chưa có dữ liệu để so sánh với tháng trước";
                deltaStyle = "foreground:#EAF2FF";
            } else {
                deltaText = "Tăng từ 0 lên " + formatVN(sumThisMonth) + " so với tháng trước";
                deltaStyle = "foreground:#9AE6B4";
            }
        } else {
            deltaText = String.format(Locale.US, "%s %.2f%% so với tháng trước",
                    (deltaPercent >= 0 ? "Tăng" : "Giảm"),
                    Math.abs(deltaPercent));
            deltaStyle = (deltaPercent >= 0 ? "foreground:#9AE6B4" : "foreground:#FF8787");
        }

        lblRevenueDelta.setText(deltaText);
        lblRevenueDelta.putClientProperty(FlatClientProperties.STYLE, deltaStyle);

        boolean upTrend = (deltaPercent == null && sumThisMonth > 0 && sumPrevMonth == 0)
                || (deltaPercent != null && deltaPercent >= 0);
        revenueChart.setBaseColor(upTrend ? new Color(0x1ABC9C) : new Color(0xE74C3C));
        revenueChart.setData(daysOfMonth, daily);

        double targetRevenue = (sumThisMonth > 0 ? sumThisMonth * 1.10 : sumPrevMonth * 1.15);
        int targetOcc = 75;
        double occNow = (totalRooms > 0) ? (busyRooms * 100.0 / totalRooms) : 0;

        goalsSummaryPanel.setData(sumThisMonth, targetRevenue, occNow, targetOcc);

        double gapRevenue = Math.max(0, targetRevenue - sumThisMonth);

        lblTargetRevenue.setText("Mục tiêu doanh thu tháng tới: " + formatVN(targetRevenue));

        if (gapRevenue <= 0) {
            lblRevenueGap.setText("Đã đạt / vượt mục tiêu tháng này 🎉");
            lblRevenueGap.putClientProperty(FlatClientProperties.STYLE, "foreground:#9AE6B4;font:-1");
        } else {
            lblRevenueGap.setText("Còn thiếu " + formatVN(gapRevenue) + " để đạt mục tiêu");
            lblRevenueGap.putClientProperty(FlatClientProperties.STYLE, "foreground:#F6E05E;font:-1");
        }

        lblTargetOcc.setText(String.format(Locale.US, "Mục tiêu tỉ lệ lấp đầy phòng: %d%%", targetOcc));

        if (occNow >= targetOcc) {
            lblOccStatus.setText(String.format(Locale.US, "Hiện tại: %.0f%% (Đạt mục tiêu)", occNow));
            lblOccStatus.putClientProperty(FlatClientProperties.STYLE, "foreground:#9AE6B4;font:-1");
        } else {
            double diff = targetOcc - occNow;
            lblOccStatus.setText(String.format(Locale.US, "Hiện tại: %.0f%% (Thiếu %.0f%%)", occNow, diff));
            lblOccStatus.putClientProperty(FlatClientProperties.STYLE, "foreground:#F6E05E;font:-1");
        }
    }

    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return SocketSessionManager.send(BaseRequest.of(commandType, data));
    }

    private DashboardOverviewDTO requestDashboardOverview(LocalDate today, int daysAhead, int topN) {
        BaseResponse response = sendRequest(
                CommandType.GET_DASHBOARD_OVERVIEW,
                new DashboardOverviewRequestDTO(today, daysAhead, topN)
        );

        if (!response.isSuccess()) {
            throw new RuntimeException(response.getMessage());
        }

        return (DashboardOverviewDTO) response.getData();
    }

    private OrderStatisticsDTO requestDailyOrderStatistics(LocalDate date) {
        BaseResponse response = sendRequest(
                CommandType.GET_ORDER_STATISTICS,
                new OrderStatisticsRequestDTO(date, false)
        );

        if (!response.isSuccess()) {
            throw new RuntimeException(response.getMessage());
        }

        DailyDetailDTO detail = (DailyDetailDTO) response.getData();
        if (detail == null) {
            return new OrderStatisticsDTO(0, 0);
        }

        return new OrderStatisticsDTO(detail.getSoLuongHoaDon(), detail.getTotalRevenue());
    }

    private double requestRevenueByDay(LocalDate date) {
        BaseResponse response = sendRequest(
                CommandType.GET_ORDER_STATISTICS,
                new OrderStatisticsRequestDTO(date, false)
        );

        if (!response.isSuccess()) {
            throw new RuntimeException(response.getMessage());
        }

        DailyDetailDTO detail = (DailyDetailDTO) response.getData();
        return detail == null ? 0 : detail.getTotalRevenue();
    }

    private double requestRevenueByMonth(YearMonth ym) {
        BaseResponse response = sendRequest(
                CommandType.GET_ORDER_STATISTICS,
                new OrderStatisticsRequestDTO(ym.atDay(1), true)
        );

        if (!response.isSuccess()) {
            throw new RuntimeException(response.getMessage());
        }

        DailyDetailDTO detail = (DailyDetailDTO) response.getData();
        return detail == null ? 0 : detail.getTotalRevenue();
    }

    private boolean requestSaveDashboardPlan(String content, String employeeId) {
        BaseResponse response = sendRequest(CommandType.SAVE_DASHBOARD_PLAN_NOTE, new DashboardSaveNoteRequestDTO(content, employeeId));

        return response.isSuccess();
    }

    private boolean requestSaveDashboardAlert(String content, String employeeId) {
        BaseResponse response = sendRequest(CommandType.SAVE_DASHBOARD_ALERT_NOTE, new DashboardSaveNoteRequestDTO(content, employeeId));

        return response.isSuccess();
    }


    private void addAlert(String text, AlertLevel level) {
        JPanel chip = new JPanel(new BorderLayout());
        chip.setOpaque(true);
        chip.setBackground(switch (level) {
            case HIGH -> new Color(0x6B1F1F);
            case MEDIUM -> new Color(0x5A4314);
            case LOW -> new Color(0x1E4B2E);
        });
        chip.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JLabel lb = new JLabel("<html>" + escapeHtml(text) + "</html>");
        lb.setForeground(Color.WHITE);
        chip.add(lb, BorderLayout.CENTER);

        alertsPanel.add(chip, "growx, wrap");
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
    }

    private String formatVN(double v) {
        java.text.NumberFormat vn = java.text.NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return vn.format(Math.max(0, v));
    }

    private String formatDate(LocalDate d) {
        return d != null ? d.format(DATE_FMT) : "";
    }

    private enum AlertLevel {
        HIGH, MEDIUM, LOW
    }

    // ========= INNER PANELS =========

    private static class RevenueChartPanel extends JPanel {
        private List<Double> values = new ArrayList<>();
        private List<LocalDate> days = new ArrayList<>();
        private Color baseColor = new Color(0x1ABC9C);
        private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("dd/MM");

        public RevenueChartPanel() {
            setOpaque(false);
            setToolTipText(null);
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    updateTooltip(e.getX());
                }
            });
        }

        public void setData(List<LocalDate> days, List<Double> v) {
            this.days = days == null ? new ArrayList<>() : new ArrayList<>(days);
            this.values = v == null ? new ArrayList<>() : new ArrayList<>(v);
            repaint();
        }

        public void setBaseColor(Color c) {
            if (c != null) {
                this.baseColor = c;
                repaint();
            }
        }

        private static Color withAlpha(Color c, int a) {
            return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            g2.setColor(new Color(0x0F, 0x2A, 0x47));
            g2.fillRoundRect(0, 0, w, h, 16, 16);

            if (values == null || values.size() < 2) {
                g2.dispose();
                return;
            }

            int left = 40, right = 10, top = 12, bottom = 22;
            int cw = w - left - right, ch = h - top - bottom;
            if (cw <= 0 || ch <= 0) {
                g2.dispose();
                return;
            }

            double max = values.stream().mapToDouble(Double::doubleValue).max().orElse(1);
            double min = 0;
            if (max <= 0) max = 1;

            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 10f));
            FontMetrics fmAxis = g2.getFontMetrics();
            g2.setColor(new Color(0x12355A));
            int gridLines = 4;
            for (int i = 0; i <= gridLines; i++) {
                double ratio = i / (double) gridLines;
                int y = (int) (top + ch - ratio * ch);
                g2.drawLine(left, y, left + cw, y);

                double valueAtLine = min + (max - min) * ratio;
                String label = formatShortMoney(valueAtLine);
                g2.setColor(new Color(0xBFD7FF));
                g2.drawString(label, left - 6 - fmAxis.stringWidth(label), y + fmAxis.getAscent() / 2 - 2);
                g2.setColor(new Color(0x12355A));
            }

            Path2D path = new Path2D.Double();
            int n = values.size();
            double stepX = cw * 1.0 / Math.max(1, n - 1);

            for (int i = 0; i < n; i++) {
                double x = left + i * stepX;
                double norm = (values.get(i) - min) / (max - min);
                double y = top + ch - norm * ch;
                if (i == 0) path.moveTo(x, y);
                else path.lineTo(x, y);
            }

            Path2D area = (Path2D) path.clone();
            area.lineTo(left + cw, top + ch);
            area.lineTo(left, top + ch);
            area.closePath();

            GradientPaint gp = new GradientPaint(
                    0, top, withAlpha(baseColor, 130),
                    0, top + ch, withAlpha(baseColor, 20)
            );
            g2.setPaint(gp);
            g2.fill(area);

            g2.setColor(baseColor);
            g2.setStroke(new BasicStroke(2.2f));
            g2.draw(path);

            for (int i = 0; i < n; i++) {
                double x = left + i * stepX;
                double norm = (values.get(i) - min) / (max - min);
                double y = top + ch - norm * ch;
                g2.fillOval((int) x - 2, (int) y - 2, 4, 4);
            }

            if (days != null && !days.isEmpty()) {
                int maxLabels = 8;
                int step = Math.max(1, (int) Math.ceil(days.size() / (double) maxLabels));
                g2.setColor(new Color(0xBFD7FF));
                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 10f));
                FontMetrics fmX = g2.getFontMetrics();
                int baseY = top + ch + fmX.getAscent() + 2;

                for (int i = 0; i < days.size(); i += step) {
                    double x = left + i * stepX;
                    String label = days.get(i).format(DAY_FMT);
                    int tw = fmX.stringWidth(label);
                    g2.drawString(label, (int) x - tw / 2, baseY);
                }
            }

            g2.dispose();
        }

        private void updateTooltip(int mx) {
            if (values == null || values.isEmpty()) {
                setToolTipText(null);
                return;
            }
            int w = getWidth();
            int left = 40, right = 10;
            int cw = w - left - right;
            if (cw <= 0) {
                setToolTipText(null);
                return;
            }
            int n = values.size();
            if (n <= 1) {
                setToolTipText(null);
                return;
            }
            double stepX = cw * 1.0 / (n - 1);
            int idx = (int) Math.round((mx - left) / stepX);
            if (idx < 0 || idx >= n) {
                setToolTipText(null);
                return;
            }

            LocalDate day = (days != null && days.size() > idx) ? days.get(idx) : null;
            double value = values.get(idx);

            java.text.NumberFormat vn =
                    java.text.NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String valueStr = vn.format(value);
            String dateStr = (day != null) ? day.format(DAY_FMT) : ("Ngày " + (idx + 1));

            setToolTipText(dateStr + ": " + valueStr);
        }

        private static String formatShortMoney(double v) {
            if (v >= 1_000_000_000) return String.format(Locale.US, "%.1f tỷ", v / 1_000_000_000d);
            if (v >= 1_000_000) return String.format(Locale.US, "%.1f tr", v / 1_000_000d);
            if (v >= 1_000) return String.format(Locale.US, "%.0fk", v / 1_000d);
            return String.format(Locale.US, "%.0f", v);
        }
    }

    private static class OccDonutPanel extends JPanel {
        private double percent = 0;
        private int busyRooms = 0;
        private int totalRooms = 0;

        public OccDonutPanel() {
            setOpaque(false);
        }

        public void setData(int busy, int total) {
            this.busyRooms = Math.max(0, busy);
            this.totalRooms = Math.max(0, total);
            this.percent = totalRooms > 0 ? (busyRooms * 100.0 / totalRooms) : 0;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int size = Math.min(w, h) - 20;
            int cx = (w - size) / 2, cy = (h - size) / 2;

            g2.setColor(new Color(0x12355A));
            g2.setStroke(new BasicStroke(18f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawOval(cx, cy, size, size);

            g2.setColor(new Color(0x1ABC9C));
            g2.drawArc(cx, cy, size, size, 90, (int) (-360 * percent / 100.0));

            int inner = size - 36;
            g2.setColor(new Color(0x0F2A47));
            g2.fillOval(cx + 18, cy + 18, inner, inner);

            g2.setColor(new Color(0xEAF2FF));
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 18f));
            String t = String.format(Locale.US, "%.0f%%", percent);
            FontMetrics fm = g2.getFontMetrics();
            int th = fm.getAscent();
            int baseY = (h + th / 2) / 2 - 4;
            g2.drawString(t, (w - fm.stringWidth(t)) / 2, baseY);

            if (totalRooms > 0) {
                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 11f));
                String sub = busyRooms + "/" + totalRooms + " phòng đang sử dụng";
                FontMetrics fm2 = g2.getFontMetrics();
                g2.drawString(sub, (w - fm2.stringWidth(sub)) / 2, baseY + fm2.getHeight());
            }

            g2.dispose();
        }
    }

    private static class SplitDonutPanel extends JPanel {
        private int booked = 0, free = 0;

        public SplitDonutPanel() {
            setOpaque(false);
        }

        public void setValues(int booked, int free) {
            this.booked = booked;
            this.free = free;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int total = Math.max(1, booked + free);
            double a1 = 360.0 * booked / total;
            double a2 = 360.0 * free / total;

            int w = getWidth(), h = getHeight();
            int size = Math.min(w, h) - 20;
            int cx = (w - size) / 2;
            int cy = (h - size) / 2 - 6;

            g2.setColor(new Color(0x3498DB));
            g2.fillArc(cx, cy, size, size, 90, (int) -a1);

            g2.setColor(new Color(0x7F8C8D));
            g2.fillArc(cx, cy, size, size, 90 - (int) a1, (int) -a2);

            g2.setColor(new Color(0x0F2A47));
            g2.fillOval(cx + 18, cy + 18, size - 36, size - 36);

            g2.setColor(new Color(0xEAF2FF));
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 16f));
            String t = booked + "/" + (booked + free);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(t, (w - fm.stringWidth(t)) / 2, (h + fm.getAscent() / 2) / 2 - 6);

            g2.dispose();
        }
    }

    private static class LegendIcon implements Icon {
        private final Color color;
        LegendIcon(Color c) { this.color = c; }
        public int getIconWidth() { return 10; }
        public int getIconHeight() { return 10; }
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(color);
            g2.fillRoundRect(x, y, 10, 10, 4, 4);
            g2.dispose();
        }
    }

    private static class Last7DaysBarChartPanel extends JPanel {
        private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("dd/MM");
        private List<LocalDate> days = new ArrayList<>();
        private List<Integer> counts = new ArrayList<>();

        public void setData(List<LocalDate> days, List<Integer> counts) {
            if (days == null || counts == null || days.size() != counts.size()) {
                this.days = new ArrayList<>();
                this.counts = new ArrayList<>();
            } else {
                this.days = new ArrayList<>(days);
                this.counts = new ArrayList<>(counts);
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(new Color(0x0F2A47));
            g2.fillRoundRect(0, 0, w, h, 12, 12);

            if (days.isEmpty() || counts.isEmpty()) {
                g2.setColor(new Color(0xBFD7FF));
                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 11f));
                String text = "Chưa có dữ liệu 7 ngày gần nhất";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, (w - fm.stringWidth(text)) / 2, (h + fm.getAscent()) / 2);
                g2.dispose();
                return;
            }

            g2.setColor(new Color(0xBFD7FF));
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 11f));
            String title = "Số hóa đơn/ngày (7 ngày gần nhất)";
            g2.drawString(title, 24, 16);

            int left = 24, right = 8, top = 26, bottom = 20;
            int cw = w - left - right;
            int ch = h - top - bottom;

            int max = counts.stream().mapToInt(Integer::intValue).max().orElse(1);
            if (max <= 0) max = 1;

            int n = counts.size();
            double barSpace = (double) cw / n;
            double barWidth = Math.max(6, barSpace * 0.5);

            g2.setColor(new Color(0x12355A));
            g2.drawLine(left, top + ch, left + cw, top + ch);

            Color barColor = new Color(0xF2C94C);
            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 10f));
            FontMetrics fm = g2.getFontMetrics();

            for (int i = 0; i < n; i++) {
                int v = counts.get(i);
                double ratio = (double) v / max;
                int bh = (int) (ratio * ch);

                int centerX = (int) (left + i * barSpace + barSpace / 2);
                int x = (int) (centerX - barWidth / 2);
                int y = top + ch - bh;

                g2.setColor(barColor);
                g2.fillRoundRect(x, y, (int) barWidth, bh, 6, 6);

                if (v > 0) {
                    String valueLabel = String.valueOf(v);
                    int vw = fm.stringWidth(valueLabel);
                    g2.setColor(Color.WHITE);
                    g2.drawString(valueLabel, centerX - vw / 2, y - 4);
                }

                g2.setColor(new Color(0xBFD7FF));
                String dayLabel = days.get(i).format(DAY_FMT);
                int tw = fm.stringWidth(dayLabel);
                g2.drawString(dayLabel, centerX - tw / 2, top + ch + fm.getAscent() + 2);
            }

            g2.dispose();
        }
    }

    private static class WeeklyCustomerComparePanel extends JPanel {
        private int thisWeek = 0;
        private int lastWeek = 0;

        public WeeklyCustomerComparePanel() {
            setOpaque(false);
        }

        public void setData(int thisWeek, int lastWeek) {
            this.thisWeek = Math.max(0, thisWeek);
            this.lastWeek = Math.max(0, lastWeek);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            int left = 4, right = 4, bottom = 10, topStart = 4;

            Font baseFont = g2.getFont();
            Font titleFont = baseFont.deriveFont(Font.BOLD, 11f);
            Font axisFont = baseFont.deriveFont(Font.PLAIN, 10f);
            Font deltaFont = baseFont.deriveFont(Font.BOLD, 12.5f);

            String title = "Tỉ lệ khách ghé thăm (tuần)";
            g2.setFont(titleFont);
            g2.setColor(new Color(0xEAF2FF));
            FontMetrics fmTitle = g2.getFontMetrics();
            int titleY = topStart + fmTitle.getAscent();
            g2.drawString(title, left, titleY);

            String deltaText = "";
            Color deltaColor = new Color(0x9AE6B4);

            if (lastWeek <= 0 && thisWeek > 0) {
                deltaText = "Tăng từ 0 khách so với tuần trước";
            } else if (lastWeek > 0) {
                double deltaPercent = (thisWeek - lastWeek) * 100.0 / lastWeek;
                String prefix = (deltaPercent >= 0) ? "Tăng " : "Giảm ";
                deltaText = prefix + String.format(Locale.US, "%.1f%% so với tuần trước", Math.abs(deltaPercent));
                deltaColor = (deltaPercent >= 0) ? new Color(0x9AE6B4) : new Color(0xF6AD55);
            }

            if (!deltaText.isEmpty()) {
                g2.setFont(deltaFont);
                FontMetrics fmDelta = g2.getFontMetrics();
                int tw = fmDelta.stringWidth(deltaText);
                g2.setColor(deltaColor);
                g2.drawString(deltaText, w - tw - right, titleY);
            }

            int chartTop = titleY + 16;
            int cw = w - left - right;
            int ch = h - chartTop - bottom;
            if (cw <= 0 || ch <= 0) {
                g2.dispose();
                return;
            }

            g2.setFont(axisFont);
            FontMetrics fmAxis = g2.getFontMetrics();

            int max = Math.max(thisWeek, lastWeek);
            if (max <= 0) {
                String text = "Chưa có dữ liệu khách ghé thăm";
                int tw = fmAxis.stringWidth(text);
                g2.setColor(new Color(0xBFD7FF));
                g2.drawString(text, (w - tw) / 2, chartTop + ch / 2 + fmAxis.getAscent() / 2);
                g2.dispose();
                return;
            }

            int baseY = chartTop + ch;
            double barSpace = cw / 2.0;
            double barWidth = Math.max(14, barSpace * 0.45);

            g2.setColor(new Color(0x12355A));
            g2.drawLine(left, baseY, left + cw, baseY);

            paintBar(g2, left + (int) (barSpace * 0.5), baseY, ch, barWidth, thisWeek, max, "Tuần này", new Color(0x1ABC9C), fmAxis);
            paintBar(g2, left + (int) (barSpace * 1.5), baseY, ch, barWidth, lastWeek, max, "Tuần trước", new Color(0xF2C94C), fmAxis);

            g2.dispose();
        }

        private void paintBar(Graphics2D g2, int centerX, int baseY, int chartHeight, double barWidth,
                              int value, int max, String label, Color color, FontMetrics fm) {
            int bh = (int) ((value / (double) max) * (chartHeight - 8));
            int x = (int) (centerX - barWidth / 2);
            int y = baseY - bh;

            g2.setColor(color);
            g2.fillRoundRect(x, y, (int) barWidth, bh, 8, 8);

            String txt = String.valueOf(value);
            g2.setColor(Color.WHITE);
            g2.drawString(txt, centerX - fm.stringWidth(txt) / 2, y - 4);

            g2.setColor(new Color(0xBFD7FF));
            g2.drawString(label, centerX - fm.stringWidth(label) / 2, baseY + fm.getAscent() + 2);
        }
    }

    private static class ServiceTopPanel extends JPanel {
        private List<DashboardTopServiceDTO> data = new ArrayList<>();

        public ServiceTopPanel() {
            setOpaque(false);
        }

        public void setData(List<DashboardTopServiceDTO> data) {
            this.data = data == null ? new ArrayList<>() : new ArrayList<>(data);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int y = 14;

            if (data.isEmpty()) {
                g2.setColor(new Color(0xBFD7FF));
                g2.drawString("Chưa có dữ liệu", 10, 20);
                g2.dispose();
                return;
            }

            int max = data.stream().mapToInt(DashboardTopServiceDTO::getTotalQuantity).max().orElse(1);

            for (int i = 0; i < data.size(); i++) {
                DashboardTopServiceDTO s = data.get(i);

                g2.setColor(new Color(0xEAF2FF));
                g2.drawString((i + 1) + ". " + s.getServiceName(), 10, y);

                int barX = 10;
                int barY = y + 6;
                int barW = Math.max(20, (int) ((w - 40) * (s.getTotalQuantity() / (double) max)));
                int barH = 10;

                g2.setColor(new Color(0x12355A));
                g2.fillRoundRect(barX, barY, w - 40, barH, 8, 8);

                g2.setColor(new Color(0xF2C94C));
                g2.fillRoundRect(barX, barY, barW, barH, 8, 8);

                g2.setColor(new Color(0xBFD7FF));
                String qty = String.valueOf(s.getTotalQuantity());
                g2.drawString(qty, w - 20 - g2.getFontMetrics().stringWidth(qty), y);

                y += 34;
            }

            g2.dispose();
        }
    }

    private static class RoomTypeDistributionPanel extends JPanel {
        private List<DashboardRoomTypeStatDTO> data = new ArrayList<>();

        public RoomTypeDistributionPanel() {
            setOpaque(false);
        }

        public void setData(List<DashboardRoomTypeStatDTO> data) {
            this.data = data == null ? new ArrayList<>() : new ArrayList<>(data);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (data.isEmpty()) {
                g2.setColor(new Color(0xBFD7FF));
                g2.drawString("Chưa có dữ liệu", 10, 20);
                g2.dispose();
                return;
            }

            int total = data.stream().mapToInt(DashboardRoomTypeStatDTO::getRoomCount).sum();
            int y = 18;
            Color[] colors = {
                    new Color(0x3498DB),
                    new Color(0x1ABC9C),
                    new Color(0xF2C94C),
                    new Color(0x9B59B6),
                    new Color(0xE67E22)
            };

            for (int i = 0; i < data.size(); i++) {
                DashboardRoomTypeStatDTO r = data.get(i);
                Color c = colors[i % colors.length];
                double percent = total > 0 ? (r.getRoomCount() * 100.0 / total) : 0;

                g2.setColor(c);
                g2.fillRoundRect(10, y - 10, 12, 12, 4, 4);

                g2.setColor(new Color(0xEAF2FF));
                String text = r.getTypeName() + " - " + r.getRoomCount() + " (" + String.format(Locale.US, "%.0f%%", percent) + ")";
                g2.drawString(text, 30, y);

                y += 28;
            }

            g2.dispose();
        }
    }

    private static class GoalsSummaryPanel extends JPanel {
        private double currentRevenue;
        private double targetRevenue;
        private double currentOcc;
        private double targetOcc;

        public GoalsSummaryPanel() {
            setOpaque(false);
        }

        public void setData(double currentRevenue, double targetRevenue, double currentOcc, double targetOcc) {
            this.currentRevenue = currentRevenue;
            this.targetRevenue = targetRevenue;
            this.currentOcc = currentOcc;
            this.targetOcc = targetOcc;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();

            int y1 = 18;
            paintProgress(g2, "Doanh thu", currentRevenue, targetRevenue, 10, y1, w - 20, new Color(0x1ABC9C));

            int y2 = 48;
            paintProgress(g2, "Lấp đầy", currentOcc, targetOcc, 10, y2, w - 20, new Color(0x3498DB));

            g2.dispose();
        }

        private void paintProgress(Graphics2D g2, String label, double current, double target, int x, int y, int width, Color color) {
            double ratio = target > 0 ? Math.min(current / target, 1.0) : 0;

            g2.setColor(new Color(0xBFD7FF));
            g2.drawString(label, x, y - 4);

            g2.setColor(new Color(0x12355A));
            g2.fillRoundRect(x, y, width, 10, 8, 8);

            g2.setColor(color);
            g2.fillRoundRect(x, y, (int) (width * ratio), 10, 8, 8);
        }
    }
}