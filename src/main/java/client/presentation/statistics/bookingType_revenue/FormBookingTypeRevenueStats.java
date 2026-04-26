package client.presentation.statistics.bookingType_revenue;

import com.formdev.flatlaf.FlatClientProperties;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;
import common.dto.BookingTypeRevenueDTO;
import net.miginfocom.swing.MigLayout;
import server.core.service.BookingTypeRevenueService;
import client.network.socket.SocketSessionManager;
import common.dto.request_dto.BookingTypeRevenueRangeRequestDTO;
import common.protocol.command.CommandType;
import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.time.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public class FormBookingTypeRevenueStats extends JPanel {

    private static final Color BG = new Color(0x0B1F33);
    private static final Color PANEL_TOP = new Color(0x0E2942);
    private static final Color BORDER = new Color(0x274A6B);
    private static final Color TEXT = new Color(0xE9EEF6);
    private static final Color ACCENT = new Color(0x22D3EE);
    private static final Color YELLOW = new Color(0xFDE68A);

    private JLabel kpiBookings;
    private JLabel kpiRoomRev;
    private JLabel kpiAvgRev;
    private JLabel kpiTopType;

    private final ChartBarPanel chartCount = new ChartBarPanel();
    private final ChartBarPanel chartRevenue = new ChartBarPanel();
    private final PieChartPanel pieRevenueShare = new PieChartPanel();
    private final PieChartPanel pieBookingShare = new PieChartPanel();

    private JDateChooser dcStart;
    private JDateChooser dcEnd;

    private final BookingTypeRevenueTableModel tableModel = new BookingTypeRevenueTableModel();
    private JTable table;

    private final BookingTypeRevenueService bookingTypeRevenueService;
    private List<BookingTypeRevenueDTO> currentData = new ArrayList<>();
    private LocalDateTime currentStart;
    private LocalDateTime currentEnd;

    public FormBookingTypeRevenueStats(BookingTypeRevenueService bookingTypeRevenueService) {
        this.bookingTypeRevenueService = bookingTypeRevenueService;

        setLayout(new MigLayout("fill, wrap, insets 0", "[grow]", "[]10[]10[190!]8[grow]"));
        setBackground(BG);

        initTopFilter();
        initKpiArea();
        initTable();
        initCharts();
        initDefaultDateRange();
        initEvents();
        loadData();
    }

    private void initTopFilter() {
        JPanel pnTop = new JPanel(new MigLayout("insets 12 16 12 16", "[][180!]16[][180!]push[]", "[]"));
        pnTop.setBackground(PANEL_TOP);

        JLabel lblStart = label("Bắt đầu");
        JLabel lblEnd = label("Kết thúc");

        dcStart = dateChooser();
        dcEnd = dateChooser();

        JButton btnReload = new JButton("Thống kê");
        stylePrimary(btnReload);

        JButton btnExport = new JButton("Xuất thống kê");
        btnExport.putClientProperty(
                FlatClientProperties.STYLE,
                "arc:12; background:#0F4C81; foreground:#FFFFFF; borderColor:#0B3E69; hoverBackground:#1D5E96;"
        );

        pnTop.add(lblStart);
        pnTop.add(dcStart);
        pnTop.add(lblEnd);
        pnTop.add(dcEnd);
        pnTop.add(btnReload, "split 2");
        pnTop.add(btnExport);

        btnReload.addActionListener(e -> loadData());

        btnExport.addActionListener(e -> {
            if (currentData == null || currentData.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Không có dữ liệu để xuất báo cáo.",
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }

            Window owner = SwingUtilities.getWindowAncestor(this);
            FormReportBookingTypeRevenue report =
                    new FormReportBookingTypeRevenue(owner, currentStart, currentEnd, currentData);
            report.setVisible(true);
        });

        add(pnTop, "growx");
    }

    private void initKpiArea() {
        JPanel kpis = new JPanel(new GridLayout(1, 4, 12, 0));
        kpis.setBackground(BG);
        kpis.setBorder(new LineBorder(BORDER));

        kpiBookings = metric("Tổng lượt", "0");
        kpiRoomRev = metric("Doanh thu phòng", "₫0");
        kpiAvgRev = metric("Doanh thu/đặt", "₫0");
        kpiTopType = metric("Kiểu đặt phổ biến nhất", "-");

        kpis.add(kpiBookings.getParent());
        kpis.add(kpiRoomRev.getParent());
        kpis.add(kpiAvgRev.getParent());
        kpis.add(kpiTopType.getParent());

        add(kpis, "growx");
    }

    private void initTable() {
        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? new Color(0x0E253D) : new Color(0x0C2136));
                    c.setForeground(TEXT);
                } else {
                    c.setBackground(new Color(0x10344F));
                    c.setForeground(Color.WHITE);
                }
                if (c instanceof JComponent jc) {
                    jc.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                }
                return c;
            }
        };

        table.setRowHeight(30);
        table.setGridColor(new Color(0x13314A));
        table.setBorder(new LineBorder(BORDER));
        table.setFillsViewportHeight(true);

        JTableHeader h = table.getTableHeader();
        h.setBackground(new Color(0x102A43));
        h.setForeground(ACCENT);
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));
        ((DefaultTableCellRenderer) h.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);

        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(0).setCellRenderer(center);

        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setCellRenderer(center);

        NumberFormat vnd = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        table.getColumnModel().getColumn(3).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setCellRenderer(new CurrencyRenderer(vnd, right));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(BG);
        scrollPane.setBorder(new LineBorder(BORDER));

        add(scrollPane, "h 190!, growx");
    }

    private void initCharts() {
        JPanel charts = new JPanel(new GridLayout(2, 2, 12, 12));
        charts.setBackground(BG);

        chartCount.setPadding(new Insets(28, 80, 32, 24));
        chartRevenue.setPadding(new Insets(28, 80, 32, 24));
        pieRevenueShare.setPadding(new Insets(10, 10, 10, 10));
        pieBookingShare.setPadding(new Insets(10, 10, 10, 10));
        chartCount.setYLabelGap(10);
        chartRevenue.setYLabelGap(10);

        charts.add(chartCount);
        charts.add(chartRevenue);
        charts.add(pieBookingShare);
        charts.add(pieRevenueShare);

        add(charts, "grow, push, gaptop 6");
    }

    private void initDefaultDateRange() {
        LocalDate today = LocalDate.now();
        setChooserDate(dcStart, Date.from(today.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        setChooserDate(dcEnd, Date.from(today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()));
    }

    private void initEvents() {
        PropertyChangeListener pcl = evt -> {
            if ("date".equals(evt.getPropertyName())) {
                loadData();
            }
        };

        dcStart.getDateEditor().addPropertyChangeListener(pcl);
        dcEnd.getDateEditor().addPropertyChangeListener(pcl);
    }

    private void loadData() {
        LocalDateTime start = startOfDay(dcStart.getDate());
        LocalDateTime end = endOfDay(dcEnd.getDate());

        if (start == null || end == null) return;

        if (end.isBefore(start)) {
            JOptionPane.showMessageDialog(this, "Khoảng thời gian không hợp lệ (Kết thúc < Bắt đầu).");
            return;
        }

        try {
            currentStart = start;
            currentEnd = end;

            BaseResponse response = sendRequest(CommandType.GET_BOOKING_TYPE_REVENUE, new BookingTypeRevenueRangeRequestDTO(start, end));

            if (!response.isSuccess()) {
                throw new RuntimeException(response.getMessage());
            }

            @SuppressWarnings("unchecked")
            List<BookingTypeRevenueDTO> bookingTypeRevenues = (List<BookingTypeRevenueDTO>) response.getData();

            currentData = bookingTypeRevenues == null ? new ArrayList<>() : bookingTypeRevenues;
            tableModel.setData(currentData);

            int totalCount = currentData.stream()
                    .mapToInt(BookingTypeRevenueDTO::getSoLuot)
                    .sum();

            double totalRoom = currentData.stream()
                    .mapToDouble(BookingTypeRevenueDTO::getRoomRevenue)
                    .sum();

            String fRoom = NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(totalRoom);
            String fAvg = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"))
                    .format(totalCount == 0 ? 0 : totalRoom / totalCount);

            kpiBookings.setText(String.valueOf(totalCount));
            kpiRoomRev.setText(fRoom);
            kpiAvgRev.setText(fAvg);
            kpiTopType.setText(
                    currentData.stream()
                            .max(Comparator.comparingDouble(BookingTypeRevenueDTO::getRoomRevenue))
                            .map(BookingTypeRevenueDTO::getBookingType)
                            .orElse("-")
            );

            List<String> labels = new ArrayList<>();
            List<Double> counts = new ArrayList<>();
            List<Double> revs = new ArrayList<>();
            LinkedHashMap<String, Double> pieRev = new LinkedHashMap<>();
            LinkedHashMap<String, Double> pieCnt = new LinkedHashMap<>();

            for (BookingTypeRevenueDTO r : currentData) {
                String bookingType = r.getBookingType() == null || r.getBookingType().isBlank()
                        ? "Không xác định"
                        : r.getBookingType();

                labels.add(bookingType);
                counts.add((double) r.getSoLuot());
                revs.add(r.getRoomRevenue());
                pieRev.put(bookingType, r.getRoomRevenue());
                pieCnt.put(bookingType, (double) r.getSoLuot());
            }

            chartCount.setData(
                    "Số lượt theo kiểu đặt",
                    labels,
                    counts,
                    v -> String.valueOf(v.intValue())
            );

            chartRevenue.setData(
                    "Doanh thu phòng theo kiểu",
                    labels,
                    revs,
                    v -> NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(v)
            );

            pieRevenueShare.setData("Tỷ trọng doanh thu phòng", pieRev);
            pieBookingShare.setData("Tỷ trọng số lượt", pieCnt);

            revalidate();
            repaint();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể tải dữ liệu thống kê kiểu đặt phòng.\n" + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return SocketSessionManager.send(BaseRequest.of(commandType, data));
    }


    // ...
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

    private JLabel label(String s) {
        JLabel lb = new JLabel(s);
        lb.setForeground(TEXT);
        return lb;
    }

    private void stylePrimary(AbstractButton b) {
        b.putClientProperty(
                FlatClientProperties.STYLE,
                "arc:12; background:#F5C452; foreground:#0B1F33; borderColor:#F1B93A; hoverBackground:#FFD36E;"
        );
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
                    "arc:10; background:#102A43; foreground:#E6F1FF; padding:4,8,4,8;"
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

    private static LocalDateTime startOfDay(Date d) {
        if (d == null) return null;
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay();
    }

    private static LocalDateTime endOfDay(Date d) {
        if (d == null) return null;
        LocalDate ld = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return ld.atTime(LocalTime.of(23, 59, 59));
    }

    private static class CurrencyRenderer extends DefaultTableCellRenderer {
        private final NumberFormat fmt;
        private final DefaultTableCellRenderer base;

        CurrencyRenderer(NumberFormat fmt, DefaultTableCellRenderer base) {
            this.fmt = fmt;
            this.base = base;
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
            Component c = base.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof Number) {
                ((JLabel) c).setText(fmt.format(((Number) value).doubleValue()));
            }
            return c;
        }
    }
}