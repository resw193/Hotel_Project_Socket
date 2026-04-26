package client.presentation.dashboard;

import client.network.socket.SocketSessionManager;
import com.formdev.flatlaf.FlatClientProperties;
import common.dto.CustomerDTO;
import common.dto.EmployeeDTO;
import common.dto.OrderDTO;
import common.dto.PromotionDTO;
import common.dto.RoomDTO;
import common.dto.request_dto.EmployeeTypeFilterRequestDTO;
import common.dto.request_dto.SearchCustomersRequestDTO;
import common.dto.request_dto.SearchOrdersRequestDTO;
import common.protocol.command.CommandType;
import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.JViewport;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FormSearch extends JPanel {

    private static final Color BG = new Color(0x0B1F33);
    private static final Color PANEL_TOP = new Color(0x0E2942);
    private static final Color TEXT_PRIMARY = new Color(0xE9EEF6);
    private static final Color BORDER = new Color(0x274A6B);
    private static final Color ACCENT = new Color(0x22D3EE);
    private static final Font BASE_FONT = new Font("Segoe UI", Font.PLAIN, 13);

    private JTextField txtKeyword;
    private JComboBox<String> cbxTarget;

    private JPanel pnFilterBar;

    private JComboBox<String> cbxEmpType;
    private JComboBox<String> cbxCustomerLoyalty;
    private JComboBox<String> cbxOrderStatus;
    private JComboBox<String> cbxPromotionStatus;

    private CardLayout cardLayout;
    private JPanel pnCards;

    private final EmployeeTableModel employeeTableModel = new EmployeeTableModel();
    private JTable tblEmployee;

    private final CustomerTableModel customerTableModel = new CustomerTableModel();
    private JTable tblCustomer;

    private final RoomTableModel roomTableModel = new RoomTableModel();
    private JTable tblRoom;

    private final OrderTableModel orderTableModel = new OrderTableModel();
    private JTable tblOrder;

    private final PromotionTableModel promotionTableModel = new PromotionTableModel();
    private JTable tblPromotion;

    private static final String CARD_EMPLOYEE = "EMPLOYEE";
    private static final String CARD_CUSTOMER = "CUSTOMER";
    private static final String CARD_ROOM = "ROOM";
    private static final String CARD_ORDER = "ORDER";
    private static final String CARD_PROMOTION = "PROMOTION";

    private static final int[] EMP_COL_WEIGHTS = {10, 20, 14, 20, 10, 14};
    private static final int[] CUS_COL_WEIGHTS = {12, 18, 14, 18, 14, 10, 8};
    private static final int[] ROOM_COL_WEIGHTS = {10, 26, 12, 14, 10};
    private static final int[] ORDER_COL_WEIGHTS = {12, 18, 18, 18, 10, 12, 12};
    private static final int[] PROMO_COL_WEIGHTS = {10, 26, 10, 22, 22, 10};

    public FormSearch() {
        setLayout(new BorderLayout());
        setBackground(BG);

        initTopBar();
        initCenter();

        cbxTarget.setSelectedItem("Nhân viên");
        updatePlaceholder();
        rebuildFilterBar();
        reloadCurrent();
    }

    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return SocketSessionManager.send(BaseRequest.of(commandType, data));
    }

    private void initTopBar() {
        JPanel top = new JPanel(new MigLayout(
                "insets 12 16 8 60",
                "[]16[420!]16[grow,fill]16[]",
                "[]8[]"
        ));
        top.setBackground(PANEL_TOP);

        JLabel lblTitle = new JLabel("Tra cứu thông tin | Search");
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 18f));
        top.add(lblTitle, "cell 0 0");

        txtKeyword = new JTextField();
        txtKeyword.putClientProperty(FlatClientProperties.STYLE,
                "arc:10; background:#102D4A; foreground:#E9EEF6; borderColor:#274A6B; padding:6,10,6,10;");
        txtKeyword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Nhập từ khóa tra cứu…");
        top.add(txtKeyword, "cell 1 0, w 420!");

        top.add(Box.createHorizontalGlue(), "cell 2 0, growx");

        cbxTarget = new JComboBox<>(new String[]{
                "Nhân viên",
                "Khách hàng",
                "Phòng",
                "Hóa đơn",
                "Khuyến mãi"
        });
        cbxTarget.putClientProperty(FlatClientProperties.STYLE,
                "arc:10; background:#102D4A; foreground:#E9EEF6; borderColor:#274A6B; padding:6,12,6,12;");
        top.add(cbxTarget, "cell 3 0, w 160!");

        pnFilterBar = new JPanel(new MigLayout("insets 4 0 0 0", "[]8[]push", "[]"));
        pnFilterBar.setOpaque(false);
        top.add(pnFilterBar, "cell 0 1, span 4 1, growx");

        add(top, BorderLayout.NORTH);

        cbxTarget.addActionListener(e -> {
            updatePlaceholder();
            rebuildFilterBar();
            reloadCurrent();
        });

        txtKeyword.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                reloadCurrent();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                reloadCurrent();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                reloadCurrent();
            }
        });
    }

    private void initCenter() {
        cardLayout = new CardLayout();
        pnCards = new JPanel(cardLayout);
        pnCards.setBackground(BG);

        tblEmployee = createBaseTable(employeeTableModel);
        centerColumnsExcept(tblEmployee, 3);
        pnCards.add(wrapInScroll(tblEmployee, EMP_COL_WEIGHTS), CARD_EMPLOYEE);

        tblCustomer = createBaseTable(customerTableModel);
        centerColumnsExcept(tblCustomer, 3);
        pnCards.add(wrapInScroll(tblCustomer, CUS_COL_WEIGHTS), CARD_CUSTOMER);

        tblRoom = createBaseTable(roomTableModel);
        centerColumnsExcept(tblRoom);
        pnCards.add(wrapInScroll(tblRoom, ROOM_COL_WEIGHTS), CARD_ROOM);

        tblOrder = createBaseTable(orderTableModel);
        centerColumnsExcept(tblOrder);
        pnCards.add(wrapInScroll(tblOrder, ORDER_COL_WEIGHTS), CARD_ORDER);

        tblPromotion = createBaseTable(promotionTableModel);
        centerColumnsExcept(tblPromotion);
        pnCards.add(wrapInScroll(tblPromotion, PROMO_COL_WEIGHTS), CARD_PROMOTION);

        add(pnCards, BorderLayout.CENTER);
    }

    private JTable createBaseTable(AbstractTableModel model) {
        JTable t = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? new Color(0x0E253D) : new Color(0x0C2136));
                    c.setForeground(TEXT_PRIMARY);
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

        t.setRowHeight(30);
        t.setFont(BASE_FONT);
        t.setForeground(TEXT_PRIMARY);
        t.setBackground(BG);
        t.setGridColor(new Color(0x13314A));
        t.setFillsViewportHeight(true);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JTableHeader h = t.getTableHeader();
        h.setBackground(new Color(0x102A43));
        h.setForeground(ACCENT);
        h.setFont(BASE_FONT.deriveFont(Font.BOLD, 13f));
        ((DefaultTableCellRenderer) h.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        return t;
    }

    private JScrollPane wrapInScroll(JTable t, int[] weights) {
        JScrollPane sp = new JScrollPane(t);
        sp.getViewport().setBackground(BG);
        sp.setBorder(new LineBorder(BORDER));

        fitColumnsToViewport(t, weights);
        sp.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                fitColumnsToViewport(t, weights);
            }
        });
        return sp;
    }

    private void fitColumnsToViewport(JTable table, int[] weights) {
        Component p = table.getParent();
        if (!(p instanceof JViewport vp)) return;

        int vw = vp.getWidth();
        TableColumnModel cm = table.getColumnModel();
        if (cm.getColumnCount() == 0 || weights == null || weights.length == 0) return;

        int total = 0;
        for (int w : weights) total += w;

        for (int i = 0; i < cm.getColumnCount() && i < weights.length; i++) {
            int w = (int) Math.round(vw * (weights[i] / (double) total));
            cm.getColumn(i).setPreferredWidth(Math.max(w, 90));
        }
    }

    private void updatePlaceholder() {
        String target = (String) cbxTarget.getSelectedItem();
        String text;
        if ("Nhân viên".equals(target)) {
            text = "Nhập mã nhân viên cần tìm ...";
        } else if ("Khách hàng".equals(target)) {
            text = "Nhập tên hoặc mã khách hàng cần tìm ...";
        } else if ("Phòng".equals(target)) {
            text = "Nhập mã phòng cần tìm ...";
        } else if ("Hóa đơn".equals(target)) {
            text = "Nhập mã / tên khách hàng hoặc mã hóa đơn ...";
        } else if ("Khuyến mãi".equals(target)) {
            text = "Nhập tên khuyến mãi cần tìm ...";
        } else {
            text = "Nhập từ khóa tra cứu…";
        }
        txtKeyword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, text);
    }

    private void rebuildFilterBar() {
        pnFilterBar.removeAll();
        String target = (String) cbxTarget.getSelectedItem();

        if ("Nhân viên".equals(target)) {
            if (cbxEmpType == null) {
                cbxEmpType = new JComboBox<>(new String[]{"Tất cả", "Lễ tân", "Quản lý"});
                styleFilterCombo(cbxEmpType);
                cbxEmpType.addActionListener(e -> reloadCurrent());
            }
            pnFilterBar.add(createFilterLabel("Loại nhân viên:"));
            pnFilterBar.add(cbxEmpType, "w 150!");
        } else if ("Khách hàng".equals(target)) {
            if (cbxCustomerLoyalty == null) {
                cbxCustomerLoyalty = new JComboBox<>(new String[]{"Tất cả", ">= 20", "> 40"});
                styleFilterCombo(cbxCustomerLoyalty);
                cbxCustomerLoyalty.addActionListener(e -> reloadCurrent());
            }
            pnFilterBar.add(createFilterLabel("Điểm thân thiết:"));
            pnFilterBar.add(cbxCustomerLoyalty, "w 180!");
        } else if ("Hóa đơn".equals(target)) {
            if (cbxOrderStatus == null) {
                cbxOrderStatus = new JComboBox<>(new String[]{"Tất cả", "Chưa thanh toán", "Thanh toán"});
                styleFilterCombo(cbxOrderStatus);
                cbxOrderStatus.addActionListener(e -> reloadCurrent());
            }
            pnFilterBar.add(createFilterLabel("Trạng thái hóa đơn:"));
            pnFilterBar.add(cbxOrderStatus, "w 180!");
        } else if ("Khuyến mãi".equals(target)) {
            if (cbxPromotionStatus == null) {
                cbxPromotionStatus = new JComboBox<>(new String[]{"Tất cả", "Còn hiệu lực", "Hết hạn"});
                styleFilterCombo(cbxPromotionStatus);
                cbxPromotionStatus.addActionListener(e -> reloadCurrent());
            }
            pnFilterBar.add(createFilterLabel("Trạng thái KM:"));
            pnFilterBar.add(cbxPromotionStatus, "w 160!");
        }

        pnFilterBar.revalidate();
        pnFilterBar.repaint();
    }

    private JLabel createFilterLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(TEXT_PRIMARY);
        lbl.setFont(BASE_FONT.deriveFont(Font.BOLD, 13f));
        return lbl;
    }

    private void styleFilterCombo(JComboBox<?> cb) {
        cb.putClientProperty(FlatClientProperties.STYLE,
                "arc:10; background:#102D4A; foreground:#E9EEF6; borderColor:#274A6B; padding:6,12,6,12;");
    }

    private void reloadCurrent() {
        String target = (String) cbxTarget.getSelectedItem();
        String kw = txtKeyword.getText().trim();

        if ("Nhân viên".equals(target)) {
            reloadEmployees(kw);
            cardLayout.show(pnCards, CARD_EMPLOYEE);
            fitColumnsToViewport(tblEmployee, EMP_COL_WEIGHTS);
        } else if ("Khách hàng".equals(target)) {
            reloadCustomers(kw);
            cardLayout.show(pnCards, CARD_CUSTOMER);
            fitColumnsToViewport(tblCustomer, CUS_COL_WEIGHTS);
        } else if ("Phòng".equals(target)) {
            reloadRooms(kw);
            cardLayout.show(pnCards, CARD_ROOM);
            fitColumnsToViewport(tblRoom, ROOM_COL_WEIGHTS);
        } else if ("Hóa đơn".equals(target)) {
            reloadOrders(kw);
            cardLayout.show(pnCards, CARD_ORDER);
            fitColumnsToViewport(tblOrder, ORDER_COL_WEIGHTS);
        } else if ("Khuyến mãi".equals(target)) {
            reloadPromotions(kw);
            cardLayout.show(pnCards, CARD_PROMOTION);
            fitColumnsToViewport(tblPromotion, PROMO_COL_WEIGHTS);
        }
    }

    private void reloadEmployees(String kw) {
        try {
            String filterType = cbxEmpType != null ? (String) cbxEmpType.getSelectedItem() : "Tất cả";

            BaseResponse response;
            if (filterType == null || "Tất cả".equalsIgnoreCase(filterType)) {
                response = sendRequest(CommandType.GET_ALL_EMPLOYEES, null);
            } else {
                response = sendRequest(CommandType.GET_EMPLOYEES_BY_TYPE, new EmployeeTypeFilterRequestDTO(filterType));
            }

            if (!response.isSuccess()) {
                throw new RuntimeException(response.getMessage());
            }

            @SuppressWarnings("unchecked")
            List<EmployeeDTO> list = (List<EmployeeDTO>) response.getData();
            if (list == null) list = new ArrayList<>();

            String keyword = norm(kw);
            if (!keyword.isEmpty()) {
                list = list.stream()
                        .filter(e -> norm(nullToEmpty(e.getEmployeeId())).contains(keyword))
                        .collect(Collectors.toList());
            }

            employeeTableModel.setData(list);
        } catch (Exception ex) {
            employeeTableModel.setData(new ArrayList<>());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reloadCustomers(String kw) {
        try {
            String opt = cbxCustomerLoyalty != null ? (String) cbxCustomerLoyalty.getSelectedItem() : "Tất cả";
            Integer minLoyalty = null;
            if (">= 20".equals(opt)) {
                minLoyalty = 20;
            } else if ("> 40".equals(opt)) {
                minLoyalty = 41;
            }

            BaseResponse response = sendRequest(
                    CommandType.SEARCH_CUSTOMERS,
                    new SearchCustomersRequestDTO(kw, minLoyalty)
            );

            if (!response.isSuccess()) {
                throw new RuntimeException(response.getMessage());
            }

            @SuppressWarnings("unchecked")
            List<CustomerDTO> list = (List<CustomerDTO>) response.getData();
            if (list == null) list = new ArrayList<>();

            customerTableModel.setData(list);
        } catch (Exception ex) {
            customerTableModel.setData(new ArrayList<>());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reloadRooms(String kw) {
        try {
            BaseResponse response = sendRequest(CommandType.GET_ALL_ROOMS, null);
            if (!response.isSuccess()) {
                throw new RuntimeException(response.getMessage());
            }

            @SuppressWarnings("unchecked")
            List<RoomDTO> list = (List<RoomDTO>) response.getData();
            if (list == null) list = new ArrayList<>();

            String keyword = norm(kw);
            if (!keyword.isEmpty()) {
                list = list.stream()
                        .filter(r -> {
                            String hay = nullToEmpty(r.getRoomId()) + " "
                                    + nullToEmpty(r.getDescription()) + " "
                                    + nullToEmpty(r.getRoomTypeName()) + " "
                                    + nullToEmpty(r.getView()) + " "
                                    + (r.isAvailable() ? "trong" : "dang su dung");
                            return norm(hay).contains(keyword);
                        })
                        .collect(Collectors.toList());
            }

            roomTableModel.setData(list);
        } catch (Exception ex) {
            roomTableModel.setData(new ArrayList<>());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reloadOrders(String kw) {
        try {
            BaseResponse response = sendRequest(CommandType.GET_ALL_ORDERS, null);
            if (!response.isSuccess()) {
                throw new RuntimeException(response.getMessage());
            }

            @SuppressWarnings("unchecked")
            List<OrderDTO> list = (List<OrderDTO>) response.getData();
            if (list == null) list = new ArrayList<>();

            String statusFilter = cbxOrderStatus != null ? (String) cbxOrderStatus.getSelectedItem() : "Tất cả";

            if (statusFilter != null && !"Tất cả".equalsIgnoreCase(statusFilter)) {
                if ("Chưa thanh toán".equalsIgnoreCase(statusFilter)) {
                    list = list.stream()
                            .filter(o -> o.getOrderStatus() != null && "Chưa thanh toán".equalsIgnoreCase(o.getOrderStatus().getDisplayName()))
                            .collect(Collectors.toList());
                }
                else if ("Thanh toán".equalsIgnoreCase(statusFilter)) {
                    list = list.stream()
                            .filter(o -> o.getOrderStatus() != null && "Thanh toán".equalsIgnoreCase(o.getOrderStatus().getDisplayName()))
                            .collect(Collectors.toList());
                }
            }

            String normKw = norm(kw);
            if (!normKw.isEmpty()) {
                list = list.stream()
                        .filter(o -> {
                            String hay = nullToEmpty(o.getOrderId()) + " "
                                    + nullToEmpty(o.getCustomerId()) + " "
                                    + nullToEmpty(o.getCustomerName());
                            return norm(hay).contains(normKw);
                        })
                        .collect(Collectors.toList());
            }

            orderTableModel.setData(list);
        } catch (Exception ex) {
            orderTableModel.setData(new ArrayList<>());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reloadPromotions(String kw) {
        try {
            BaseResponse response = sendRequest(CommandType.GET_ALL_PROMOTIONS, null);
            if (!response.isSuccess()) {
                throw new RuntimeException(response.getMessage());
            }

            @SuppressWarnings("unchecked")
            List<PromotionDTO> list = (List<PromotionDTO>) response.getData();
            if (list == null) list = new ArrayList<>();

            String normKw = norm(kw);
            String statusFilter = cbxPromotionStatus != null ? (String) cbxPromotionStatus.getSelectedItem() : "Tất cả";
            LocalDate today = LocalDate.now(ZoneId.systemDefault());

            List<PromotionDTO> result = new ArrayList<>();
            for (PromotionDTO p : list) {
                if (!normKw.isEmpty() && !norm(nullToEmpty(p.getPromotionName())).contains(normKw)) {
                    continue;
                }

                boolean active = isPromotionActiveToday(p, today);

                if ("Còn hiệu lực".equals(statusFilter) && !active) continue;
                if ("Hết hạn".equals(statusFilter) && active) continue;

                result.add(p);
            }

            promotionTableModel.setData(result);
        } catch (Exception ex) {
            promotionTableModel.setData(new ArrayList<>());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isPromotionActiveToday(PromotionDTO p, LocalDate today) {
        if (p == null || p.getStartTime() == null || p.getEndTime() == null) return false;
        LocalDate start = p.getStartTime().toLocalDate();
        LocalDate end = p.getEndTime().toLocalDate();
        if (today.isBefore(start)) return false;
        if (today.isAfter(end)) return false;
        return true;
    }

    private void centerColumnsExcept(JTable table, int... excludeCols) {
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);

        int colCount = table.getColumnCount();
        for (int col = 0; col < colCount; col++) {
            boolean skip = false;
            for (int ex : excludeCols) {
                if (ex == col) {
                    skip = true;
                    break;
                }
            }
            if (!skip) {
                table.getColumnModel().getColumn(col).setCellRenderer(center);
            }
        }
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static String norm(String s) {
        if (s == null) return "";
        String tmp = Normalizer.normalize(s, Normalizer.Form.NFD);
        tmp = tmp.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return tmp.toLowerCase().trim();
    }

    // ========================= TABLE MODELS =========================

    private static class EmployeeTableModel extends AbstractTableModel {
        private final String[] cols = {"Mã NV", "Tên nhân viên", "SĐT", "Email", "Giới tính", "Loại nhân viên"};
        private final List<EmployeeDTO> data = new ArrayList<>();

        public void setData(List<EmployeeDTO> list) {
            data.clear();
            if (list != null) data.addAll(list);
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return cols.length;
        }

        @Override
        public String getColumnName(int column) {
            return cols[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            EmployeeDTO e = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> e.getEmployeeId();
                case 1 -> e.getFullName();
                case 2 -> e.getPhone();
                case 3 -> e.getEmail();
                case 4 -> e.isGender() ? "Nam" : "Nữ";
                case 5 -> e.getEmployeeTypeName();
                default -> "";
            };
        }
    }

    private static class CustomerTableModel extends AbstractTableModel {
        private final String[] cols = {"Mã KH", "Tên khách hàng", "SĐT", "Email", "Ngày đăng ký", "CCCD", "Điểm"};
        private final List<CustomerDTO> data = new ArrayList<>();
        private final DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        public void setData(List<CustomerDTO> list) {
            data.clear();
            if (list != null) data.addAll(list);
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return cols.length;
        }

        @Override
        public String getColumnName(int column) {
            return cols[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            CustomerDTO c = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> c.getCustomerId();
                case 1 -> c.getFullName();
                case 2 -> c.getPhone();
                case 3 -> c.getEmail();
                case 4 -> c.getRegisDate() == null ? "" : c.getRegisDate().format(f);
                case 5 -> c.getIdCard();
                case 6 -> c.getLoyaltyPoint();
                default -> "";
            };
        }
    }

    private static class RoomTableModel extends AbstractTableModel {
        private final String[] cols = {"Mã phòng", "Thông tin phòng", "Trạng thái", "Loại phòng", "View"};
        private final List<RoomDTO> data = new ArrayList<>();

        public void setData(List<RoomDTO> list) {
            data.clear();
            if (list != null) data.addAll(list);
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return cols.length;
        }

        @Override
        public String getColumnName(int column) {
            return cols[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            RoomDTO r = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> r.getRoomId();
                case 1 -> r.getDescription();
                case 2 -> r.isAvailable() ? "Trống" : "Đã đặt/Đang ở";
                case 3 -> r.getRoomTypeName();
                case 4 -> r.getView();
                default -> "";
            };
        }
    }

    private static class OrderTableModel extends AbstractTableModel {
        private final String[] cols = {"Mã HĐ", "Ngày lập", "Nhân viên", "Khách hàng", "Mã KM", "Trạng thái", "Tổng tiền"};
        private final List<OrderDTO> data = new ArrayList<>();
        private final DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        public void setData(List<OrderDTO> list) {
            data.clear();
            if (list != null) data.addAll(list);
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return cols.length;
        }

        @Override
        public String getColumnName(int column) {
            return cols[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            OrderDTO o = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> o.getOrderId();
                case 1 -> o.getOrderDate() == null ? "" : o.getOrderDate().format(f);
                case 2 -> o.getEmployeeName();
                case 3 -> o.getCustomerName();
                case 4 -> o.getPromotionId();
                case 5 -> o.getOrderStatus() == null ? "" : o.getOrderStatus().getDisplayName();
                case 6 -> String.format("%,.0f VND", o.getTotal());
                default -> "";
            };
        }
    }

    private static class PromotionTableModel extends AbstractTableModel {
        private final String[] cols = {"Mã KM", "Tên khuyến mãi", "Giảm giá", "Bắt đầu", "Kết thúc", "Số lượng"};
        private final List<PromotionDTO> data = new ArrayList<>();
        private final DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        public void setData(List<PromotionDTO> list) {
            data.clear();
            if (list != null) data.addAll(list);
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return cols.length;
        }

        @Override
        public String getColumnName(int column) {
            return cols[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            PromotionDTO p = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> p.getPromotionId();
                case 1 -> p.getPromotionName();
                case 2 -> (int) Math.round(p.getDiscount()) + " %";
                case 3 -> p.getStartTime() == null ? "" : p.getStartTime().format(f);
                case 4 -> p.getEndTime() == null ? "" : p.getEndTime().format(f);
                case 5 -> p.getQuantity();
                default -> "";
            };
        }
    }
}