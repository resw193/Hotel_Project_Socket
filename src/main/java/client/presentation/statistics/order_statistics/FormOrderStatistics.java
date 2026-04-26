package client.presentation.statistics.order_statistics;

import client.presentation.statistics.bookingType_revenue.PieChartPanel;
import com.formdev.flatlaf.FlatClientProperties;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;
import common.dto.DailyDetailDTO;
import net.miginfocom.swing.MigLayout;
import server.core.service.OrderStatisticsService;
import client.network.socket.SocketSessionManager;
import common.dto.request_dto.OrderStatisticsRequestDTO;
import common.protocol.command.CommandType;
import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.time.*;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;

public class FormOrderStatistics extends JPanel {

    private static final Color BG = new Color(0x0B1F33);
    private static final Color PANEL_TOP = new Color(0x0E2942);
    private static final Color BORDER = new Color(0x274A6B);
    private static final Color TEXT = new Color(0xE6F1FF);
    private static final Color ACCENT = new Color(0x22D3EE);
    private static final Color YELLOW = new Color(0xFDE68A);

    private final OrderStatisticsService orderStatisticsService;

    private JDateChooser dcDate;
    private JButton btnStatsDay;
    private JButton btnStatsMonth;

    private JLabel kpiOrders;
    private JLabel kpiBookings;
    private JLabel kpiServices;
    private JLabel kpiRoomRev;
    private JLabel kpiSvcRev;
    private JLabel kpiTotal;

    private final PieChartPanel pieRevenueShare = new PieChartPanel();
    private final PieChartPanel pieCountShare = new PieChartPanel();

    private final BarChartPanel barRevenueCompare = new BarChartPanel();
    private final BarChartPanel barVolumeCompare = new BarChartPanel();

    private final NumberFormat VND = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private DailyDetailDTO currentDetail;
    private String currentReportLabel = "";

    public FormOrderStatistics(OrderStatisticsService orderStatisticsService) {
        this.orderStatisticsService = orderStatisticsService;

        setLayout(new MigLayout("fill, wrap, insets 0", "[grow]", "[]10[]10[grow]"));
        setBackground(BG);

        initTopPanel();
        initKpiPanel();
        initChartPanel();
        initDefaultDate();
        initEvents();

        loadDataDay();
    }

    private void initTopPanel() {
        JPanel pnTop = new JPanel(new MigLayout("insets 10 16 10 16", "push[][180!]16[]12[]", "[]"));
        pnTop.setBackground(PANEL_TOP);

        JLabel lblDate = new JLabel("Ngày thống kê");
        lblDate.setForeground(TEXT);

        dcDate = dateChooser();

        btnStatsDay = new JButton("Thống kê");
        btnStatsDay.putClientProperty(
                FlatClientProperties.STYLE,
                "arc:10; background:#0EA5E9; foreground:#FFFFFF; borderColor:#0B80B0; hoverBackground:#22D3EE;"
        );

        btnStatsMonth = new JButton("Theo tháng");
        btnStatsMonth.putClientProperty(
                FlatClientProperties.STYLE,
                "arc:10; background:#F5C452; foreground:#0B1F33; borderColor:#F1B93A; hoverBackground:#FFD36E;"
        );

        JButton btnExport = new JButton("Xuất thống kê");
        btnExport.putClientProperty(
                FlatClientProperties.STYLE,
                "arc:10; background:#0F4C81; foreground:#FFFFFF; borderColor:#0B3E69; hoverBackground:#1D5E96;"
        );

        pnTop.add(lblDate);
        pnTop.add(dcDate);
        pnTop.add(btnStatsDay);
        pnTop.add(btnStatsMonth);
        pnTop.add(btnExport);

        btnExport.addActionListener(e -> {
            if (currentDetail == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Không có dữ liệu để xuất báo cáo.",
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }

            Window owner = SwingUtilities.getWindowAncestor(this);
            FormReportOrderStatistics report =
                    new FormReportOrderStatistics(owner, currentReportLabel, currentDetail);
            report.setVisible(true);
        });

        add(pnTop, "growx");
    }

    private void initKpiPanel() {
        JPanel pnKPI = new JPanel(new GridLayout(1, 6, 12, 0));
        pnKPI.setBackground(BG);
        pnKPI.setBorder(new LineBorder(BORDER));

        kpiOrders = metric("Số lượng hóa đơn thanh toán", "0");
        kpiBookings = metric("Tổng số lượt đặt phòng", "0");
        kpiServices = metric("Tổng số dịch vụ đã sử dụng", "0");
        kpiRoomRev = metric("Thu nhập phòng", "₫0");
        kpiSvcRev = metric("Thu nhập dịch vụ", "₫0");
        kpiTotal = metric("Tổng thu nhập", "₫0");

        pnKPI.add(kpiOrders.getParent());
        pnKPI.add(kpiBookings.getParent());
        pnKPI.add(kpiServices.getParent());
        pnKPI.add(kpiRoomRev.getParent());
        pnKPI.add(kpiSvcRev.getParent());
        pnKPI.add(kpiTotal.getParent());

        add(pnKPI, "growx");
    }

    private void initChartPanel() {
        JPanel pnCharts = new JPanel(new MigLayout(
                "insets 10 16 16 16, gapy 12",
                "[grow]",
                "[grow][grow]"
        ));
        pnCharts.setBackground(BG);

        JPanel pnPieChart = new JPanel(new GridLayout(1, 2, 12, 12));
        pnPieChart.setOpaque(false);

        pieRevenueShare.setPadding(new Insets(10, 10, 10, 10));
        pieCountShare.setPadding(new Insets(10, 10, 10, 10));

        pnPieChart.add(pieRevenueShare);
        pnPieChart.add(pieCountShare);
        pnCharts.add(pnPieChart, "grow, wrap");

        JPanel pnBars = new JPanel(new GridLayout(1, 2, 12, 12));
        pnBars.setOpaque(false);

        barRevenueCompare.setPadding(new Insets(10, 10, 10, 10));
        barVolumeCompare.setPadding(new Insets(10, 10, 10, 10));

        pnBars.add(barRevenueCompare);
        pnBars.add(barVolumeCompare);
        pnCharts.add(pnBars, "grow");

        add(pnCharts, "grow");
    }

    private void initDefaultDate() {
        LocalDate today = LocalDate.now();
        setChooserDate(dcDate, Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    private void initEvents() {
        PropertyChangeListener pcl = evt -> {
            if ("date".equals(evt.getPropertyName())) {
                loadDataDay();
            }
        };

        dcDate.getDateEditor().addPropertyChangeListener(pcl);
        btnStatsDay.addActionListener(e -> loadDataDay());
        btnStatsMonth.addActionListener(e -> loadDataMonth());
    }

    private void loadDataDay() {
        LocalDate d = toLocalDate(dcDate.getDate());
        if (d == null) return;

        try {
            BaseResponse response = sendRequest(CommandType.GET_ORDER_STATISTICS, new OrderStatisticsRequestDTO(d, false));

            if (!response.isSuccess()) {
                throw new RuntimeException(response.getMessage());
            }

            DailyDetailDTO detail = (DailyDetailDTO) response.getData();
            currentDetail = detail;
            currentReportLabel = "Theo ngày: " + d.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            bind(detail);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể tải thống kê theo ngày.\n" + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void loadDataMonth() {
        LocalDate d = toLocalDate(dcDate.getDate());
        if (d == null) d = LocalDate.now();

        try {
            BaseResponse response = sendRequest(CommandType.GET_ORDER_STATISTICS, new OrderStatisticsRequestDTO(d, true));

            if (!response.isSuccess()) {
                throw new RuntimeException(response.getMessage());
            }

            DailyDetailDTO detail = (DailyDetailDTO) response.getData();
            YearMonth ym = YearMonth.of(d.getYear(), d.getMonth());

            currentDetail = detail;
            currentReportLabel = "Theo tháng: " + String.format("%02d/%d", ym.getMonthValue(), ym.getYear());
            bind(detail);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể tải thống kê theo tháng.\n" + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void bind(DailyDetailDTO detail) {
        if (detail == null) {
            detail = new DailyDetailDTO(0, 0, 0, 0, 0, 0);
        }
        currentDetail = detail;

        kpiOrders.setText(String.valueOf(detail.getSoLuongHoaDon()));
        kpiBookings.setText(String.valueOf(detail.getTotalBookings()));
        kpiServices.setText(String.valueOf(detail.getTotalServiceQty()));
        kpiRoomRev.setText(VND.format(detail.getRoomRevenue()));
        kpiSvcRev.setText(VND.format(detail.getServiceRevenue()));
        kpiTotal.setText(VND.format(detail.getTotalRevenue()));

        LinkedHashMap<String, Double> revenue = new LinkedHashMap<>();
        revenue.put("Phòng", detail.getRoomRevenue());
        revenue.put("Dịch vụ", detail.getServiceRevenue());
        pieRevenueShare.setData("Tỷ trọng doanh thu", revenue);

        LinkedHashMap<String, Double> counts = new LinkedHashMap<>();
        counts.put("Hóa đơn", (double) detail.getSoLuongHoaDon());
        counts.put("Lượt đặt phòng", (double) detail.getTotalBookings());
        counts.put("Dịch vụ sử dụng", (double) detail.getTotalServiceQty());
        pieCountShare.setData("Tỷ trọng số lượng", counts);

        LinkedHashMap<String, Double> revenueCompare = new LinkedHashMap<>();
        revenueCompare.put("Phòng", detail.getRoomRevenue());
        revenueCompare.put("Dịch vụ", detail.getServiceRevenue());
        revenueCompare.put("Tổng", detail.getTotalRevenue());
        barRevenueCompare.setData("So sánh thu nhập (Phòng / Dịch vụ / Tổng)", revenueCompare);

        LinkedHashMap<String, Double> volumeCompare = new LinkedHashMap<>();
        volumeCompare.put("Hóa đơn", (double) detail.getSoLuongHoaDon());
        volumeCompare.put("Lượt đặt phòng", (double) detail.getTotalBookings());
        volumeCompare.put("Dịch vụ", (double) detail.getTotalServiceQty());
        barVolumeCompare.setData("So sánh số lượng (Hóa đơn / Đặt phòng / Dịch vụ)", volumeCompare);

        revalidate();
        repaint();
    }

    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return SocketSessionManager.send(BaseRequest.of(commandType, data));
    }

    private static JDateChooser dateChooser() {
        JDateChooser dc = new JDateChooser();
        dc.setDateFormatString("dd/MM/yyyy");

        dc.putClientProperty(
                FlatClientProperties.STYLE,
                "arc:10; background:#102A43; foreground:#E6F1FF; padding:6,10,6,10;"
        );

        if (dc.getDateEditor() != null && dc.getDateEditor().getUiComponent() instanceof JTextFieldDateEditor ed) {
            ed.putClientProperty(
                    FlatClientProperties.STYLE,
                    "arc:10; background:#102A43; foreground:#E6F1FF; borderColor:#FBBF24; padding:4,8,4,8;"
            );

            Runnable paintYellow = () -> {
                ed.setForeground(YELLOW);
                ed.setDisabledTextColor(YELLOW);
                ed.setCaretColor(YELLOW);
                ed.setSelectionColor(new Color(0x1E3A5F));
                ed.repaint();
            };

            paintYellow.run();

            dc.addPropertyChangeListener("date", e -> SwingUtilities.invokeLater(paintYellow));
            ed.addPropertyChangeListener("value", e -> SwingUtilities.invokeLater(paintYellow));

            ed.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    paintYellow.run();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    paintYellow.run();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    paintYellow.run();
                }
            });
        }

        return dc;
    }

    private static void setChooserDate(JDateChooser c, Date d) {
        c.setDate(d);
    }

    private static LocalDate toLocalDate(Date d) {
        if (d == null) return null;
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private JLabel metric(String title, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(0x102D4A));
        p.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JLabel t = new JLabel(title);
        t.setForeground(new Color(0xB8C4D4));
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JLabel v = new JLabel(value);
        v.setForeground(ACCENT);
        v.setFont(new Font("Segoe UI", Font.BOLD, 16));

        p.add(t, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        return v;
    }
}