package client.presentation.statistics.service_statistics;

import com.formdev.flatlaf.FlatClientProperties;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;
import common.dto.ServiceRankingDTO;
import net.miginfocom.swing.MigLayout;
import server.core.service.ServiceRankingService;
import client.network.socket.SocketSessionManager;
import common.dto.request_dto.ServiceRankingRangeRequestDTO;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FormServiceStatistics extends JPanel {

    private static final Color BG = new Color(0x0B1F33);
    private static final Color PANEL_TOP = new Color(0x0E2942);
    private static final Color BORDER = new Color(0x274A6B);
    private static final Color TEXT = new Color(0xE6F1FF);
    private static final Color ACCENT = new Color(0x22D3EE);
    private static final Color YELLOW = new Color(0xFDE68A);

    private final ServiceRankingService serviceRankingService;

    private final ServiceRankingTableModel tableModel = new ServiceRankingTableModel();
    private JTable table;

    private JDateChooser dcStart, dcEnd;
    private final NumberFormat VND = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    private final TopServiceBarChart chartPanel = new TopServiceBarChart();
    private final JLabel lblChartTitle = new JLabel("Top dịch vụ được sử dụng nhiều nhất");
    private List<ServiceRankingDTO> currentData = new ArrayList<>();
    private LocalDateTime currentStart;
    private LocalDateTime currentEnd;

    public FormServiceStatistics(ServiceRankingService serviceRankingService) {
        this.serviceRankingService = serviceRankingService;

        initUI();
        initDefaultDateRange();
        initEvents();
        loadData();
    }

    private void initUI() {
        setLayout(new MigLayout("fill, wrap, insets 0", "[grow]", "[grow 0][grow]"));
        setBackground(BG);

        JPanel pnTop = new JPanel(new MigLayout("insets 10 16 10 16", "[][180!]16[][180!]push[]", "[]"));
        pnTop.setBackground(PANEL_TOP);
        chartPanel.setBarSpacing(0.46f, 16);

        JLabel lblStart = new JLabel("Bắt đầu");
        lblStart.setForeground(TEXT);

        JLabel lblEnd = new JLabel("Kết thúc");
        lblEnd.setForeground(TEXT);

        dcStart = dateChooser();
        dcEnd = dateChooser();

        JButton btnReload = new JButton("Thống kê");
        btnReload.putClientProperty(FlatClientProperties.STYLE,
                "arc:10; background:#0EA5E9; foreground:#FFFFFF; borderColor:#0B80B0; hoverBackground:#22D3EE;");

        JButton btnExport = new JButton("Xuất thống kê");
        btnExport.putClientProperty(FlatClientProperties.STYLE,
                "arc:10; background:#F5C452; foreground:#0B1F33; borderColor:#F1B93A; hoverBackground:#FFD36E;");

        pnTop.add(lblStart);
        pnTop.add(dcStart);
        pnTop.add(lblEnd);
        pnTop.add(dcEnd);
        pnTop.add(btnReload, "split 2");
        pnTop.add(btnExport);

        add(pnTop, "growx");

        JPanel pnBody = new JPanel(new MigLayout(
                "insets 0 12 12 12, fill",
                "[grow 3][grow 2]",
                "[grow]"
        ));
        pnBody.setBackground(BG);
        add(pnBody, "grow");

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

        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setBackground(new Color(0x102A43));
        tableHeader.setForeground(ACCENT);
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        ((DefaultTableCellRenderer) tableHeader.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);

        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(0).setCellRenderer(center);

        table.getColumnModel().getColumn(1).setCellRenderer(center);

        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setCellRenderer(center);

        table.getColumnModel().getColumn(3).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setCellRenderer(new CurrencyRenderer(VND, center));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(BG);
        scrollPane.setBorder(new LineBorder(BORDER));

        JPanel pnBoxChart = new JPanel(new MigLayout("insets 10 10 10 10, fill", "[grow]", "[][grow]"));
        pnBoxChart.setBackground(new Color(0x0E253D));
        pnBoxChart.setBorder(new LineBorder(BORDER));

        lblChartTitle.setForeground(new Color(0xA7F3D0));
        lblChartTitle.setFont(lblChartTitle.getFont().deriveFont(Font.BOLD, 14f));

        pnBoxChart.add(lblChartTitle, "split 2, left, gapbottom 6");

        JLabel lblHint = new JLabel("");
        lblHint.setForeground(new Color(0x7DD3FC));
        lblHint.setFont(lblHint.getFont().deriveFont(Font.PLAIN, 12f));
        pnBoxChart.add(lblHint, "wrap");

        pnBoxChart.add(chartPanel, "grow");

        pnBody.add(scrollPane, "grow");
        pnBody.add(pnBoxChart, "grow");

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
            FormReportServiceStatistics reportForm = new FormReportServiceStatistics(owner, currentStart, currentEnd, currentData);
            reportForm.setVisible(true);
        });
    }

    private void initDefaultDateRange() {
        LocalDate today = LocalDate.now();

        setChooserDate(
                dcStart,
                Date.from(today.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant())
        );

        setChooserDate(
                dcEnd,
                Date.from(today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant())
        );
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
            JOptionPane.showMessageDialog(this,
                    "Khoảng thời gian không hợp lệ (Kết thúc < Bắt đầu).");
            return;
        }

        try {
            currentStart = start;
            currentEnd = end;

            BaseResponse allRes = sendRequest(CommandType.GET_SERVICE_RANKING, new ServiceRankingRangeRequestDTO(start, end, null));

            if (!allRes.isSuccess()) {
                throw new RuntimeException(allRes.getMessage());
            }

            @SuppressWarnings("unchecked")
            List<ServiceRankingDTO> allData = (List<ServiceRankingDTO>) allRes.getData();
            currentData = (allData != null) ? allData : new ArrayList<>();
            tableModel.setServiceRankings(currentData);

            BaseResponse topRes = sendRequest(CommandType.GET_SERVICE_RANKING, new ServiceRankingRangeRequestDTO(start, end, 8));

            if (!topRes.isSuccess()) {
                throw new RuntimeException(topRes.getMessage());
            }

            @SuppressWarnings("unchecked")
            List<ServiceRankingDTO> topData = (List<ServiceRankingDTO>) topRes.getData();
            chartPanel.setData(topData);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Không thể tải dữ liệu thống kê dịch vụ.\n" + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
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

    private static void setChooserDate(JDateChooser chooser, Date date) {
        chooser.setDate(date);
    }

    private static LocalDateTime startOfDay(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay();
    }

    private static LocalDateTime endOfDay(Date date) {
        if (date == null) return null;
        LocalDate ld = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
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
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = base.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (value instanceof Number) {
                ((JLabel) c).setText(fmt.format(((Number) value).doubleValue()));
            }

            return c;
        }
    }
}