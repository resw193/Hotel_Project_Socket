package client.presentation.statistics.service_statistics;

import common.dto.ServiceRankingDTO;
import com.formdev.flatlaf.FlatClientProperties;
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
import java.util.List;
import java.util.Locale;

public class FormReportServiceStatistics extends JDialog {

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

    public FormReportServiceStatistics(Window owner,
                                       LocalDateTime start,
                                       LocalDateTime end,
                                       List<ServiceRankingDTO> data) {
        super(owner, "Báo cáo thống kê dịch vụ sử dụng", ModalityType.APPLICATION_MODAL);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1180, 820);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(22, 22, 22, 22));
        setContentPane(root);

        root.add(createHeader(start, end, data), BorderLayout.NORTH);
        root.add(createCenter(data), BorderLayout.CENTER);
        root.add(createFooter(data), BorderLayout.SOUTH);
    }

    private JComponent createHeader(LocalDateTime start, LocalDateTime end, List<ServiceRankingDTO> data) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        JPanel header = new JPanel(new MigLayout("insets 24 30 22 30, wrap", "[grow]", "[][][][]"));
        header.setBackground(PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(4, 4, 4, 4)
        ));

        JLabel lblUniversity = new JLabel("KHÁCH SẠN MIMOSA");
        lblUniversity.setForeground(MUTED);
        lblUniversity.setFont(new Font("Segoe UI", Font.BOLD, 15));

        JLabel lblTitle = new JLabel("BÁO CÁO THỐNG KÊ DỊCH VỤ SỬ DỤNG");
        lblTitle.setForeground(ACCENT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 30));

        JSeparator line = new JSeparator();
        line.setForeground(BORDER);
        line.setBackground(BORDER);

        JLabel lblTime = new JLabel("Khoảng thời gian thống kê: " + formatDateTime(start) + "  →  " + formatDateTime(end));
        lblTime.setForeground(TEXT);
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 17));

        JLabel lblInfo = new JLabel(
                "Ngày xuất báo cáo: " + DTF.format(LocalDateTime.now()) +
                        "    |    Số dòng thống kê: " + (data == null ? 0 : data.size())
        );
        lblInfo.setForeground(MUTED);
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        header.add(lblUniversity);
        header.add(lblTitle);
        header.add(line, "growx, h 1!, gaptop 4, gapbottom 6");
        header.add(lblTime);
        header.add(lblInfo);

        wrapper.add(header, BorderLayout.CENTER);
        return wrapper;
    }

    private JComponent createCenter(List<ServiceRankingDTO> data) {
        String[] cols = {"STT", "Tên dịch vụ", "Tổng số lượng", "Tổng doanh thu"};

        Object[][] rows;
        if (data == null || data.isEmpty()) {
            rows = new Object[0][cols.length];
        } else {
            rows = new Object[data.size()][cols.length];
            for (int i = 0; i < data.size(); i++) {
                ServiceRankingDTO s = data.get(i);
                rows[i][0] = i + 1;
                rows[i][1] = s.getServiceName();
                rows[i][2] = s.getTotalQuantity();
                rows[i][3] = VND.format(s.getTotalRevenue());
            }
        }

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

        table.setRowHeight(42);
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
        header.setPreferredSize(new Dimension(header.getWidth(), 44));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer left = new DefaultTableCellRenderer();
        left.setHorizontalAlignment(SwingConstants.LEFT);

        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(0).setCellRenderer(center);

        table.getColumnModel().getColumn(1).setPreferredWidth(500);
        table.getColumnModel().getColumn(1).setCellRenderer(left);

        table.getColumnModel().getColumn(2).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setCellRenderer(center);

        table.getColumnModel().getColumn(3).setPreferredWidth(240);
        table.getColumnModel().getColumn(3).setCellRenderer(center);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(new LineBorder(BORDER, 1, true));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(scroll, BorderLayout.CENTER);

        return centerPanel;
    }

    private JComponent createFooter(List<ServiceRankingDTO> data) {
        JPanel footer = new JPanel(new MigLayout("insets 20 24 20 24", "[grow][right]", "[][]"));
        footer.setBackground(SUMMARY_BG);
        footer.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(4, 4, 4, 4)
        ));

        int totalQty = 0;
        double totalRevenue = 0;

        if (data != null) {
            for (ServiceRankingDTO s : data) {
                totalQty += s.getTotalQuantity();
                totalRevenue += s.getTotalRevenue();
            }
        }

        JLabel lblTotalQtyTitle = new JLabel("Tổng số lượng dịch vụ sử dụng:");
        lblTotalQtyTitle.setForeground(TEXT);
        lblTotalQtyTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));

        JLabel lblTotalQty = new JLabel(String.valueOf(totalQty));
        lblTotalQty.setForeground(ACCENT);
        lblTotalQty.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JLabel lblTotalRevenueTitle = new JLabel("Tổng doanh thu dịch vụ:");
        lblTotalRevenueTitle.setForeground(TEXT);
        lblTotalRevenueTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));

        JLabel lblTotalRevenue = new JLabel(VND.format(totalRevenue));
        lblTotalRevenue.setForeground(TOTAL_HIGHLIGHT);
        lblTotalRevenue.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JButton btnClose = new JButton("Đóng");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnClose.putClientProperty(FlatClientProperties.STYLE,
                "arc:10; background:#0F4C81; foreground:#FFFFFF; borderColor:#0B3E69; hoverBackground:#1D5E96;");
        btnClose.addActionListener(e -> dispose());

        JPanel leftSummary = new JPanel(new MigLayout("insets 0, gapy 6, wrap 2", "[][grow]", "[][]"));
        leftSummary.setOpaque(false);
        leftSummary.add(lblTotalQtyTitle);
        leftSummary.add(lblTotalQty);
        leftSummary.add(lblTotalRevenueTitle);
        leftSummary.add(lblTotalRevenue);

        footer.add(leftSummary, "growx");
        footer.add(btnClose, "aligny bottom, alignx right, w 145!, h 42!");

        return footer;
    }

    private String formatDateTime(LocalDateTime dt) {
        if (dt == null) return "";
        return DTF.format(dt);
    }
}