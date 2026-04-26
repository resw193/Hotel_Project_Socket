package client.presentation.statistics.order_statistics;

import com.formdev.flatlaf.FlatClientProperties;
import common.dto.DailyDetailDTO;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class FormReportOrderStatistics extends JDialog {

    private static final Color BG = Color.WHITE;
    private static final Color PANEL = Color.WHITE;
    private static final Color BORDER = new Color(0xD9E2EC);
    private static final Color TEXT = new Color(0x1F2937);
    private static final Color MUTED = new Color(0x6B7280);
    private static final Color ACCENT = new Color(0x0F4C81);
    private static final Color ACCENT_LIGHT = new Color(0xEAF4FF);
    private static final Color SUMMARY_BG = new Color(0xF8FAFC);
    private static final Color TOTAL_HIGHLIGHT = new Color(0xB45309);

    private final NumberFormat VND = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public FormReportOrderStatistics(Window owner,
                                     String reportLabel,
                                     DailyDetailDTO detail) {
        super(owner, "Báo cáo thống kê hóa đơn", ModalityType.APPLICATION_MODAL);

        if (detail == null) {
            detail = new DailyDetailDTO(0, 0, 0, 0, 0, 0);
        }

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1180, 820);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(22, 22, 22, 22));
        setContentPane(root);

        root.add(createHeader(reportLabel), BorderLayout.NORTH);
        root.add(createCenter(detail), BorderLayout.CENTER);
        root.add(createFooter(detail), BorderLayout.SOUTH);
    }

    private JComponent createHeader(String reportLabel) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        JPanel header = new JPanel(new MigLayout("insets 24 30 22 30, wrap", "[grow]", "[][][][]"));
        header.setBackground(PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(4, 4, 4, 4)
        ));

        JLabel lblHotel = new JLabel("KHÁCH SẠN MIMOSA");
        lblHotel.setForeground(MUTED);
        lblHotel.setFont(new Font("Segoe UI", Font.BOLD, 15));

        JLabel lblTitle = new JLabel("BÁO CÁO THỐNG KÊ HÓA ĐƠN");
        lblTitle.setForeground(ACCENT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));

        JSeparator line = new JSeparator();
        line.setForeground(BORDER);
        line.setBackground(BORDER);

        JLabel lblTime = new JLabel("Kỳ thống kê: " + reportLabel);
        lblTime.setForeground(TEXT);
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 17));

        JLabel lblInfo = new JLabel("Ngày xuất báo cáo: " + DTF.format(LocalDateTime.now()));
        lblInfo.setForeground(MUTED);
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        header.add(lblHotel);
        header.add(lblTitle);
        header.add(line, "growx, h 1!, gaptop 4, gapbottom 6");
        header.add(lblTime);
        header.add(lblInfo);

        wrapper.add(header, BorderLayout.CENTER);
        return wrapper;
    }

    private JComponent createCenter(DailyDetailDTO detail) {
        String[] cols = {"STT", "Nội dung", "Giá trị"};

        Object[][] rows = {
                {1, "Số lượng hóa đơn thanh toán", String.valueOf(detail.getSoLuongHoaDon())},
                {2, "Tổng số lượt đặt phòng", String.valueOf(detail.getTotalBookings())},
                {3, "Tổng số dịch vụ đã sử dụng", String.valueOf(detail.getTotalServiceQty())},
                {4, "Thu nhập phòng", VND.format(detail.getRoomRevenue())},
                {5, "Thu nhập dịch vụ", VND.format(detail.getServiceRevenue())},
                {6, "Tổng thu nhập", VND.format(detail.getTotalRevenue())}
        };

        JTable table = new JTable(rows, cols) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);

                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xFAFBFC));
                    c.setForeground(TEXT);
                } else {
                    c.setBackground(new Color(0xDCEBFF));
                    c.setForeground(TEXT);
                }

                if (c instanceof JComponent jc) {
                    jc.setBorder(new EmptyBorder(8, 14, 8, 14));
                }

                return c;
            }
        };

        table.setRowHeight(44);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.setForeground(TEXT);
        table.setBackground(Color.WHITE);
        table.setGridColor(new Color(0xE5E7EB));
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(ACCENT_LIGHT);
        header.setForeground(ACCENT);
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setPreferredSize(new Dimension(header.getWidth(), 46));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer left = new DefaultTableCellRenderer();
        left.setHorizontalAlignment(SwingConstants.LEFT);

        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(0).setCellRenderer(center);

        table.getColumnModel().getColumn(1).setPreferredWidth(560);
        table.getColumnModel().getColumn(1).setCellRenderer(left);

        table.getColumnModel().getColumn(2).setPreferredWidth(260);
        table.getColumnModel().getColumn(2).setCellRenderer(center);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(new LineBorder(BORDER, 1, true));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);
        centerPanel.add(scroll, BorderLayout.CENTER);

        JPanel notePanel = new JPanel(new MigLayout("insets 16 18 16 18, wrap", "[grow]", "[][]"));
        notePanel.setBackground(new Color(0xFFFDF7));
        notePanel.setBorder(new LineBorder(new Color(0xF3E8C8), 1, true));

        JLabel lblNoteTitle = new JLabel("Nhận xét tổng quan");
        lblNoteTitle.setForeground(new Color(0x7C5E10));
        lblNoteTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));

        String overview = buildOverview(detail);
        JLabel lblOverview = new JLabel("<html>" + overview + "</html>");
        lblOverview.setForeground(TEXT);
        lblOverview.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        notePanel.add(lblNoteTitle);
        notePanel.add(lblOverview);

        centerPanel.add(notePanel, BorderLayout.SOUTH);

        return centerPanel;
    }

    private JComponent createFooter(DailyDetailDTO detail) {
        JPanel footer = new JPanel(new MigLayout("insets 20 24 20 24", "[grow][right]", "[][]"));
        footer.setBackground(SUMMARY_BG);
        footer.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(4, 4, 4, 4)
        ));

        JLabel lblTotalTitle = new JLabel("Tổng thu nhập thống kê:");
        lblTotalTitle.setForeground(TEXT);
        lblTotalTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JLabel lblTotal = new JLabel(VND.format(detail.getTotalRevenue()));
        lblTotal.setForeground(TOTAL_HIGHLIGHT);
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JLabel lblSub = new JLabel(
                "Phòng: " + VND.format(detail.getRoomRevenue()) +
                        "    |    Dịch vụ: " + VND.format(detail.getServiceRevenue())
        );
        lblSub.setForeground(MUTED);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnClose.putClientProperty(FlatClientProperties.STYLE,
                "arc:10; background:#0F4C81; foreground:#FFFFFF; borderColor:#0B3E69; hoverBackground:#1D5E96;");
        btnClose.addActionListener(e -> dispose());

        JPanel leftSummary = new JPanel(new MigLayout("insets 0, gapy 6, wrap", "[grow]", "[][]"));
        leftSummary.setOpaque(false);
        leftSummary.add(lblTotalTitle);
        leftSummary.add(lblTotal);
        leftSummary.add(lblSub);

        footer.add(leftSummary, "growx");
        footer.add(btnClose, "aligny bottom, alignx right, w 145!, h 42!");

        return footer;
    }

    private String buildOverview(DailyDetailDTO detail) {
        return "Trong kỳ thống kê này có <b>" + detail.getSoLuongHoaDon() + "</b> hóa đơn thanh toán, "
                + "<b>" + detail.getTotalBookings() + "</b> lượt đặt phòng và "
                + "<b>" + detail.getTotalServiceQty() + "</b> dịch vụ đã được sử dụng. "
                + "Doanh thu từ phòng đạt <b>" + VND.format(detail.getRoomRevenue()) + "</b>, "
                + "doanh thu từ dịch vụ đạt <b>" + VND.format(detail.getServiceRevenue()) + "</b>, "
                + "nâng tổng doanh thu lên <b>" + VND.format(detail.getTotalRevenue()) + "</b>.";
    }
}