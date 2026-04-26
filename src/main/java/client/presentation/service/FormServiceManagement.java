package client.presentation.service;

import client.presentation.core.ServiceRegistry;
import com.formdev.flatlaf.FlatClientProperties;
import common.dto.ServiceDTO;
import net.miginfocom.swing.MigLayout;
import server.core.service.ServiceService;
import client.network.socket.SocketSessionManager;
import common.dto.request_dto.ServiceIdRequestDTO;
import common.dto.request_dto.ServiceTypeRequestDTO;
import common.protocol.command.CommandType;
import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class FormServiceManagement extends JPanel {

    private static final Color BG = new Color(0x0B1F33);
    private static final Color PANEL_TOP = new Color(0x0E2942);
    private static final Color CARD_BG = new Color(0x102D4A);
    private static final Color TEXT_PRIMARY = new Color(0xE9EEF6);
    private static final Color TEXT_MUTED = new Color(0xB8C4D4);
    private static final Color GOLD_PRIMARY = new Color(0xF5C452);

    private JComboBox<String> cbxFilter;
    private JButton btnAddNew;
    private JPanel pnGrid;
    private JScrollPane scrollPane;
    private JLabel lblLoading;

    private SwingWorker<List<ServiceDTO>, Void> loadWorker;

    public FormServiceManagement() {
        setLayout(new MigLayout("fill, wrap, insets 0", "[grow]", "[grow 0][grow]"));
        setBackground(BG);

        JPanel pnTop = new JPanel(new MigLayout("insets 12 16 12 16", "[grow]push[][]", "[]"));
        pnTop.setBackground(PANEL_TOP);
        pnTop.setBorder(new EmptyBorder(0, 0, 0, 0));
        add(pnTop, "growx");

        JLabel lblTitle = new JLabel("Dịch vụ | Services");
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 18f));
        pnTop.add(lblTitle, "left");

        cbxFilter = new JComboBox<>(new String[]{"All", "Food", "Drink", "Laundry", "Health"});
        cbxFilter.putClientProperty(FlatClientProperties.STYLE,
                "arc:12; background:#102D4A; foreground:#E9EEF6; borderColor:#274A6B; padding:6,12,6,12");

        JLabel lblServiceType = new JLabel("Loại dịch vụ:");
        lblServiceType.setForeground(TEXT_PRIMARY);
        lblServiceType.setFont(lblServiceType.getFont().deriveFont(Font.PLAIN, 13f));

        JPanel pnFilter = new JPanel(new MigLayout("insets 0, gap 6", "[]0[]", "[]"));
        pnFilter.setOpaque(false);
        pnFilter.add(lblServiceType);
        pnFilter.add(cbxFilter, "w 150!");
        pnTop.add(pnFilter);

        btnAddNew = new JButton("Thêm mới");
        stylePrimary(btnAddNew);
        pnTop.add(btnAddNew, "w 138!, h 34!");

        pnGrid = new GridPanel(new MigLayout(
                "insets 16 12 16 12, gapx 12, gapy 16, wrap 5",
                "[grow,fill] [grow,fill] [grow,fill] [grow,fill] [grow,fill]",
                ""
        ));
        pnGrid.setBackground(BG);

        lblLoading = new JLabel("Đang tải dữ liệu...", SwingConstants.CENTER);
        lblLoading.setForeground(TEXT_PRIMARY);
        lblLoading.setFont(lblLoading.getFont().deriveFont(Font.BOLD, 16f));

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(BG);
        container.add(pnGrid, BorderLayout.CENTER);

        scrollPane = new JScrollPane(container);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BG);
        scrollPane.getVerticalScrollBar().putClientProperty(FlatClientProperties.STYLE,
                "width:10; background:#0B1F33; track:#0B1F33; thumb:#274A6B; trackArc:999");
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);

        add(scrollPane, "grow");

        cbxFilter.addActionListener(e -> loadDataAsync());

        btnAddNew.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            FormAddService dialog = new FormAddService(this);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        }));

        loadDataAsync();
    }

    private static class GridPanel extends JPanel implements Scrollable {
        public GridPanel(LayoutManager layout) {
            super(layout);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 20;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return visibleRect.height - 20;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    public void loadData() {
        loadDataAsync();
    }

    public void loadDataAsync() {
        if (loadWorker != null && !loadWorker.isDone()) {
            loadWorker.cancel(true);
        }

        setLoading(true);

        loadWorker = new SwingWorker<>() {
            @Override
            protected List<ServiceDTO> doInBackground() throws Exception {
                String selected = String.valueOf(cbxFilter.getSelectedItem());

                BaseResponse response = switch (selected) {
                    case "Food", "Drink", "Laundry", "Health" -> sendRequest(CommandType.GET_SERVICES_BY_TYPE, new ServiceTypeRequestDTO(selected));
                    default -> sendRequest(CommandType.GET_ALL_SERVICES, null);
                };

                if (!response.isSuccess()) {
                    throw new RuntimeException(response.getMessage());
                }

                @SuppressWarnings("unchecked")
                List<ServiceDTO> data = (List<ServiceDTO>) response.getData();
                return data == null ? List.of() : data;
            }

            @Override
            protected void done() {
                try {
                    if (!isCancelled()) {
                        List<ServiceDTO> data = get();
                        setLoading(false);
                        loadDataToDisplay(data);
                    } else {
                        setLoading(false);
                    }
                } catch (Exception ex) {
                    setLoading(false);
                    JOptionPane.showMessageDialog(FormServiceManagement.this,
                            ex.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        loadWorker.execute();
    }

    private void setLoading(boolean loading) {
        if (loading) {
            pnGrid.removeAll();
            pnGrid.setLayout(new BorderLayout());
            pnGrid.add(lblLoading, BorderLayout.CENTER);
        } else {
            pnGrid.removeAll();
            pnGrid.setLayout(new MigLayout(
                    "insets 16 12 16 12, gapx 12, gapy 16, wrap 5",
                    "[grow,fill] [grow,fill] [grow,fill] [grow,fill] [grow,fill]",
                    ""
            ));
        }

        pnGrid.revalidate();
        pnGrid.repaint();

        cbxFilter.setEnabled(!loading);
        btnAddNew.setEnabled(!loading);
    }

    private void loadDataToDisplay(List<ServiceDTO> services) {
        pnGrid.removeAll();
        pnGrid.setLayout(new MigLayout(
                "insets 16 12 16 12, gapx 12, gapy 16, wrap 5",
                "[grow,fill] [grow,fill] [grow,fill] [grow,fill] [grow,fill]",
                ""
        ));

        for (ServiceDTO service : services) {
            JPanel card = new JPanel(new MigLayout(
                    "wrap 6, insets 14, gapx 6, gapy 6",
                    "[grow]",
                    "[]"
            ));
            card.setMinimumSize(new Dimension(0, 0));
            card.setBackground(CARD_BG);
            card.putClientProperty(FlatClientProperties.STYLE, "arc:16; borderColor:#153C5B");

            JLabel lblImageService = new JLabel("", SwingConstants.CENTER);
            lblImageService.setPreferredSize(new Dimension(140, 180));

            ImageIcon icon = loadImageFromResourcesOrFile(service.getImgSource(), 120, 120);
            if (icon != null) {
                lblImageService.setIcon(icon);
            } else {
                lblImageService.setText("No image");
                lblImageService.setForeground(TEXT_MUTED);
            }

            JLabel lblName = new JLabel(service.getServiceName(), SwingConstants.CENTER);
            lblName.setForeground(TEXT_PRIMARY);
            lblName.setFont(lblName.getFont().deriveFont(Font.BOLD, 14f));

            String unitName = resolveUnitName(service);

            JLabel lblQuantity = new JLabel("Số lượng:");
            lblQuantity.setForeground(TEXT_MUTED);

            JLabel lblValue = new JLabel(service.getQuantity() + " " + unitName);
            lblValue.setForeground(GOLD_PRIMARY);
            lblValue.setFont(lblValue.getFont().deriveFont(Font.BOLD, 13f));

            JPanel pnQuantity = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            pnQuantity.setOpaque(false);
            pnQuantity.add(lblQuantity);
            pnQuantity.add(lblValue);

            JLabel lblPrice = new JLabel("Giá:");
            lblPrice.setForeground(TEXT_MUTED);

            JLabel lblPriceValue = new JLabel(vnd(service.getPrice()) + " / " + unitName);
            lblPriceValue.setForeground(GOLD_PRIMARY);
            lblPriceValue.setFont(lblPriceValue.getFont().deriveFont(Font.BOLD, 13f));

            JPanel pnPrice = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            pnPrice.setOpaque(false);
            pnPrice.add(lblPrice);
            pnPrice.add(lblPriceValue);

            JButton btnDelete = new JButton("Xóa");
            JButton btnUpdate = new JButton("Cập nhật");
            JButton btnAddQty = new JButton("Thêm");

            styleDanger(btnDelete);
            styleSuccess(btnUpdate);
            stylePrimary(btnAddQty);

            card.add(lblImageService, "growx, span 6");
            card.add(lblName, "growx, span 6");
            card.add(pnQuantity, "growx, span 3");
            card.add(pnPrice, "growx, span 3");
            card.add(btnDelete, "growx, span 2");
            card.add(btnUpdate, "growx, span 2");
            card.add(btnAddQty, "growx, span 2");

            btnDelete.addActionListener(e -> deleteService(service));

            btnUpdate.addActionListener(e -> SwingUtilities.invokeLater(() -> {
                FormUpdateService dialog = new FormUpdateService(this, service);
                dialog.setLocationRelativeTo(this);
                dialog.setVisible(true);
            }));

            btnAddQty.addActionListener(e -> SwingUtilities.invokeLater(() -> {
                FormUpdateQuantityService dialog = new FormUpdateQuantityService(this, service);
                dialog.setLocationRelativeTo(this);
                dialog.setVisible(true);
            }));

            pnGrid.add(card, "grow");
        }

        pnGrid.revalidate();
        pnGrid.repaint();
    }

    private void deleteService(ServiceDTO service) {
        int opt = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa dịch vụ \"" + service.getServiceName() + "\" không?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION);

        if (opt != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            BaseResponse response = sendRequest(CommandType.DELETE_SERVICE, new ServiceIdRequestDTO(service.getServiceId()));

            boolean ok = response.isSuccess();
            if (ok) {
                JOptionPane.showMessageDialog(this, "Đã xóa dịch vụ.");
                loadDataAsync();
            }
            else {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Không thể xóa", JOptionPane.WARNING_MESSAGE);
            }
            return;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String resolveUnitName(ServiceDTO service) {
        if (service.getUnitName() != null && !service.getUnitName().isBlank()) {
            return service.getUnitName();
        }

        String type = service.getServiceType() == null ? "" : service.getServiceType().trim().toLowerCase();
        String name = service.getServiceName() == null ? "" : service.getServiceName().trim().toLowerCase();

        if ("food".equals(type)) {
            return "phần";
        }

        if ("laundry".equals(type)) {
            return "lượt";
        }

        if ("drink".equals(type)) {
            if (name.contains("rượu") || name.contains("ruou")
                    || name.contains("vang") || name.contains("soju") || name.contains("chai")) {
                return "chai";
            }
            if (name.contains("bia")) {
                return "thùng";
            }
            return "lon";
        }

        if ("health".equals(type)) {
            return "lượt";
        }

        return "";
    }

    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return SocketSessionManager.send(BaseRequest.of(commandType, data));
    }


    private void stylePrimary(AbstractButton b) {
        b.putClientProperty(FlatClientProperties.STYLE,
                "arc:12; background:#F5C452; foreground:#0B1F33; borderColor:#F1B93A; " +
                        "hoverBackground:#FFD36E; focusWidth:1; innerFocusWidth:0; margin:4,6,4,6");
    }

    private void styleSuccess(AbstractButton b) {
        b.putClientProperty(FlatClientProperties.STYLE,
                "arc:12; background:#1E9E8F; foreground:#FFFFFF; borderColor:#178C7F; " +
                        "hoverBackground:#1A8A7E; focusWidth:1; innerFocusWidth:0; margin:4,6,4,6");
    }

    private void styleDanger(AbstractButton b) {
        b.putClientProperty(FlatClientProperties.STYLE,
                "arc:12; background:#D64545; foreground:#FFFFFF; borderColor:#BF3E3E; " +
                        "hoverBackground:#B73A3A; focusWidth:1; innerFocusWidth:0; margin:4,6,4,6");
    }

    private ImageIcon loadImageFromResourcesOrFile(String path, int w, int h) {
        try {
            Image img = null;
            if (path != null && !path.isBlank()) {
                File f = new File(path);
                if (f.exists()) {
                    img = new ImageIcon(f.getAbsolutePath()).getImage();
                } else {
                    java.net.URL u = getClass().getResource(path.startsWith("/") ? path : "/" + path);
                    if (u != null) img = new ImageIcon(u).getImage();
                }
            }
            if (img == null) return null;
            return new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            return null;
        }
    }

    private String vnd(double value) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        return nf.format(value) + " đ";
    }
}