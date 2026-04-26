package client.presentation.order;

import com.toedter.calendar.JDateChooser;
import common.dto.request_dto.OrderIdRequestDTO;
import common.dto.request_dto.OrderStatusRequestDTO;
import common.dto.request_dto.SearchOrdersRequestDTO;
import common.dto.request_dto.UpdateOrderPromotionRequestDTO;
import common.protocol.command.CommandType;
import common.protocol.response.BaseResponse;
import other.ReceiptCache;
import common.dto.OrderDTO;
import common.dto.OrderDetailRoomDTO;
import common.dto.OrderDetailServiceDTO;
import common.dto.PromotionDTO;
import common.enums.OrderStatus;
import net.miginfocom.swing.MigLayout;
import server.core.service.OrderService;
import server.core.service.PromotionService;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class FormOrderManagement extends JPanel {

    private static final Color BG = new Color(0x0B1F33);
    private static final Color CARD_BG = new Color(0x0F2A44);
    private static final Color BORDER = new Color(0x274A6B);
    private static final Color TEXT = new Color(0xE6F1FF);
    private static final Color MUTED = new Color(0x9FB6CC);
    private static final Color ACCENT = new Color(0x22D3EE);
    private static final Color ACCENT2 = new Color(0xF59E0B);
    private static final Font BASE_FONT = new Font("Segoe UI", Font.PLAIN, 13);

    private final OrderTableModel orderTableModel = new OrderTableModel();
    private JTable tableOrder;
    private JTextField txtSearchCustomerName;
    private JToggleButton toggleHistory;
    private JPanel pnDetail;

    private final OrderService orderService;
    private final PromotionService promotionService;

    private JDateChooser dcOrderDateFilter;
    private JButton btnClearDate;

    private static final String LOGO_IMAGE_PATH = "/images/mimosa_hotel_logo.jpg";
    private static final String QR_IMAGE_PATH = "/images/qrtui.jpg";
    private static final String TICK_IMAGE_PATH = "/images/thanhcong.png";
    private static final int HISTORY_DAYS = 7;

    public FormOrderManagement(OrderService orderService, PromotionService promotionService) {
        this.orderService = orderService;
        this.promotionService = promotionService;

        setLayout(new BorderLayout());
        setBackground(BG);

        JPanel pnTop = new JPanel(new MigLayout("insets 10 12 10 12", "[grow,fill]push[][]", "[]"));
        pnTop.setOpaque(true);
        pnTop.setBackground(BG);

        txtSearchCustomerName = new JTextField();
        txtSearchCustomerName.putClientProperty(
                "JTextField.placeholderText",
                "Tìm theo mã / tên khách hàng, mã hóa đơn…"
        );
        styleTextField(txtSearchCustomerName);
        pnTop.add(txtSearchCustomerName, "w 300!");

        dcOrderDateFilter = new com.toedter.calendar.JDateChooser();
        dcOrderDateFilter.setDateFormatString("dd/MM/yyyy");
        styleDateChooser(dcOrderDateFilter);
        dcOrderDateFilter.setBorder(new LineBorder(BORDER));
        dcOrderDateFilter.addPropertyChangeListener("date", e -> reloadData());
        pnTop.add(dcOrderDateFilter, "w 150!, gapx 8");

        btnClearDate = chipButton("Reset ngày");
        btnClearDate.addActionListener(e -> {
            dcOrderDateFilter.setDate(null);
            reloadData();
        });
        pnTop.add(btnClearDate, "w 90!, gapx 6");

        toggleHistory = createHistoryToggle("Truy vấn lịch sử");
        toggleHistory.setToolTipText("Hiển thị hóa đơn 7 ngày gần đây (nếu không chọn ngày cụ thể)");
        pnTop.add(toggleHistory, "w 150!");

        add(pnTop, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.58);
        split.setBorder(null);
        split.setOpaque(false);
        add(split, BorderLayout.CENTER);

        tableOrder = createTable();
        JScrollPane scrollPane = new JScrollPane(tableOrder);
        scrollPane.getViewport().setBackground(BG);
        scrollPane.setBorder(new LineBorder(BORDER));
        split.setLeftComponent(scrollPane);

        pnDetail = new JPanel(new BorderLayout());
        pnDetail.setOpaque(false);
        JScrollPane rightScroll = new JScrollPane(pnDetail);
        rightScroll.getVerticalScrollBar().setUnitIncrement(27);
        rightScroll.setWheelScrollingEnabled(true);
        split.setRightComponent(rightScroll);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                split.setDividerLocation(0.58);
            }
        });

        txtSearchCustomerName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                reloadData();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                reloadData();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                reloadData();
            }
        });


        toggleHistory.addActionListener(e -> {
            refreshToggleStyle(toggleHistory);
            reloadData();
        });

        tableOrder.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tableOrder.getSelectedRow();
                showDetail(orderTableModel.getAt(row));
            }
        });

        reloadData();
    }

    private void reloadData() {
        String kw = norm(txtSearchCustomerName.getText());
        boolean history = toggleHistory.isSelected();

        OrderStatus targetStatus = history
                ? OrderStatus.DA_THANH_TOAN
                : OrderStatus.CHUA_THANH_TOAN;

        BaseResponse response = sendRequest(CommandType.SEARCH_ORDERS, new SearchOrdersRequestDTO(targetStatus.getDisplayName(), kw));

        if (!response.isSuccess()) {
            JOptionPane.showMessageDialog(this, response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        @SuppressWarnings("unchecked")
        List<OrderDTO> filtered = (List<OrderDTO>) response.getData();
        if (filtered == null) filtered = new ArrayList<>();

        filtered = filtered.stream()
                .filter(o -> o.getOrderStatus() == targetStatus)
                .collect(Collectors.toList());

        LocalDate pick = getPickedDate();
        if (history) {
            if (pick == null) {
                LocalDate today = LocalDate.now(ZoneId.systemDefault());
                LocalDate start = today.minusDays(HISTORY_DAYS);

                filtered = filtered.stream()
                        .filter(o -> o.getOrderDate() != null)
                        .filter(o -> {
                            LocalDate d = o.getOrderDate().toLocalDate();
                            return !d.isBefore(start) && !d.isAfter(today);
                        })
                        .collect(Collectors.toList());
            }
            else {
                filtered = filtered.stream()
                        .filter(o -> o.getOrderDate() != null && o.getOrderDate().toLocalDate().isEqual(pick))
                        .collect(Collectors.toList());
            }
        }
        else {
            if (pick != null) {
                filtered = filtered.stream()
                        .filter(o -> o.getOrderDate() != null && o.getOrderDate().toLocalDate().isEqual(pick))
                        .collect(Collectors.toList());
            }
        }

        orderTableModel.setDsHoaDon(filtered);

        if (!filtered.isEmpty()) {
            tableOrder.setRowSelectionInterval(0, 0);
            showDetail(filtered.get(0));
        } else {
            pnDetail.removeAll();
            pnDetail.revalidate();
            pnDetail.repaint();
        }
    }

    private void reloadDataAndKeepSelection(String orderIDToKeep) {
        reloadData();
        if (orderIDToKeep == null) return;

        int rowCount = orderTableModel.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            OrderDTO o = orderTableModel.getAt(i);
            if (o != null && orderIDToKeep.equals(o.getOrderId())) {
                tableOrder.setRowSelectionInterval(i, i);
                tableOrder.scrollRectToVisible(tableOrder.getCellRect(i, 0, true));
                showDetail(o);
                break;
            }
        }
    }

    private void showDetail(OrderDTO order) {
        pnDetail.removeAll();
        if (order == null) {
            pnDetail.revalidate();
            pnDetail.repaint();
            return;
        }

        boolean historyMode = toggleHistory.isSelected();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        BaseResponse roomRes = sendRequest(CommandType.GET_ORDER_ROOM_LINES, new OrderIdRequestDTO(order.getOrderId()));
        BaseResponse svcRes = sendRequest(CommandType.GET_ORDER_SERVICE_LINES, new OrderIdRequestDTO(order.getOrderId()));

        @SuppressWarnings("unchecked")
        List<OrderDetailRoomDTO> roomLines = roomRes.isSuccess() ? (List<OrderDetailRoomDTO>) roomRes.getData() : new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<OrderDetailServiceDTO> svLines = svcRes.isSuccess() ? (List<OrderDetailServiceDTO>) svcRes.getData() : new ArrayList<>();

        JPanel pnRoot = new JPanel(new MigLayout("wrap, insets 12, gap 8", "[grow,fill]", ""));
        pnRoot.setOpaque(true);
        pnRoot.setBackground(BG);

        JPanel pnHeader = titledCard("Thông tin hóa đơn");
        pnHeader.add(label("Mã hóa đơn: ", order.getOrderId(), true));
        pnHeader.add(label("Ngày lập: ", order.getOrderDate() == null ? "" : formatter.format(order.getOrderDate()), false));
        pnHeader.add(label("Nhân viên: ", nullToEmpty(order.getEmployeeName()), false));
        pnHeader.add(label("Khách hàng: ", nullToEmpty(order.getCustomerName()), false));
        pnRoot.add(pnHeader);

        JPanel pnRoomList = titledCard("Các phòng đã đặt");
        if (roomLines != null && !roomLines.isEmpty()) {
            for (OrderDetailRoomDTO r : roomLines) {
                JPanel card = miniCard();
                card.add(text("• " + nullToEmpty(r.getRoomDescription())));
                card.add(text("Loại phòng: " + nullToEmpty(r.getRoomTypeName())));
                card.add(text("Đặt: " + format(r.getBookingDate(), formatter)
                        + " | Check-in: " + format(r.getCheckInDate(), formatter)
                        + " | Check-out: " + format(r.getCheckOutDate(), formatter)));
                card.add(text("Hình thức: " + (r.getBookingType() == null ? "" : r.getBookingType().getDisplayName())));
                card.add(text("Trạng thái: " + nullToEmpty(r.getStatus())));
                pnRoomList.add(card, "growx");
            }
        } else {
            pnRoomList.add(text("Không có chi tiết phòng."));
        }
        pnRoot.add(pnRoomList);

        JPanel pSvc = titledCard("Dịch vụ đã sử dụng");
        if (svLines != null && !svLines.isEmpty()) {
            Map<String, Integer> agg = svLines.stream()
                    .collect(Collectors.groupingBy(
                            OrderDetailServiceDTO::getServiceName,
                            Collectors.summingInt(OrderDetailServiceDTO::getQuantity)
                    ));
            agg.forEach((name, qty) -> pSvc.add(text("• " + name + "  × " + qty)));
        } else {
            pSvc.add(text("Không có dịch vụ."));
        }
        pnRoot.add(pSvc);

        JPanel pnFooter = titledCard(historyMode ? "In hóa đơn" : "Thanh toán");
        pnFooter.setLayout(new MigLayout("insets 8, gap 10", "[grow][280!]", "[][][][][grow]"));

        double tongTienBanDau = order.getTotal();
        double discountPercent = order.getPromotionDiscount();
        if (discountPercent < 0) discountPercent = 0;
        if (discountPercent > 100) discountPercent = 100;

        double tienKhuyenMai = tongTienBanDau * discountPercent / 100.0;
        double tongTienSauKhiKhuyenMai = tongTienBanDau - tienKhuyenMai;
        double vat = tongTienSauKhiKhuyenMai * 0.10;
        double tongThanhToan = tongTienSauKhiKhuyenMai + vat;

        boolean paidStatus = order.getOrderStatus() == OrderStatus.DA_THANH_TOAN;
        double tienPhaiTra = paidStatus ? 0d : tongThanhToan;

        String promoTxt = (discountPercent <= 0)
                ? "Không có"
                : ((int) discountPercent) + "% (-" + formatVND(tienKhuyenMai) + ")";

        if (!historyMode) {
            pnFooter.add(text("Tổng (Chưa áp KM): " + formatVND(tongTienBanDau)), "wrap");

            boolean canChangePromotion = !paidStatus;

            if (canChangePromotion) {
                JPanel pnPromoLine = new JPanel(new MigLayout("insets 0, gapx 6", "[grow][]", "[]"));
                pnPromoLine.setOpaque(false);
                pnPromoLine.add(text("Khuyến mãi: " + promoTxt), "growx");

                JButton btnChoosePromo = chipButton("Chọn / đổi khuyến mãi");
                btnChoosePromo.setFont(BASE_FONT.deriveFont(Font.BOLD, 11f));
                pnPromoLine.add(btnChoosePromo);

                pnFooter.add(pnPromoLine, "wrap");

                btnChoosePromo.addActionListener(e -> {
                    Window owner = SwingUtilities.getWindowAncestor(this);
                    FormApplyPromotion formApplyPromotion = new FormApplyPromotion(owner, order, promotionService);
                    formApplyPromotion.setVisible(true);

                    if (!formApplyPromotion.isAccepted()) {
                        return;
                    }

                    PromotionDTO chosen = formApplyPromotion.getSelectedPromotion();

                    BaseResponse promoRes = sendRequest(CommandType.UPDATE_ORDER_PROMOTION, new UpdateOrderPromotionRequestDTO(order.getOrderId(), chosen));

                    boolean ok = promoRes.isSuccess();
                    if (!ok) {
                        JOptionPane.showMessageDialog(this,
                                "Áp dụng khuyến mãi thất bại.",
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        order.setPromotionId(chosen == null ? null : chosen.getPromotionId());
                        order.setPromotionName(chosen == null ? null : chosen.getPromotionName());
                        order.setPromotionDiscount(chosen == null ? 0d : chosen.getDiscount());
                        reloadDataAndKeepSelection(order.getOrderId());
                    }
                });
            } else {
                pnFooter.add(text("Khuyến mãi: " + promoTxt), "wrap");
            }

            pnFooter.add(text("Tổng (Đã áp KM): " + formatVND(tongTienSauKhiKhuyenMai)), "wrap");
            pnFooter.add(text("VAT (10%): " + formatVND(vat)), "wrap");
        } else {
            double tongThanhToanHistory = calculateFinalTotal(roomLines, svLines, order);
            pnFooter.add(boldText("Tổng thanh toán: " + formatVND(tongThanhToanHistory)), "wrap");
        }

        JPanel pnCash = new JPanel(new MigLayout("insets 8, gap 6", "[grow,fill]", ""));
        pnCash.setOpaque(true);
        pnCash.setBackground(new Color(0x102E4A));
        pnCash.setBorder(new LineBorder(BORDER));

        JLabel lblPaid = text("Khách đưa: " + formatVND(0));
        JLabel lblRemain = text("Còn thiếu: " + formatVND(tienPhaiTra));
        JLabel lblChange = text("Tiền thối: " + formatVND(0));
        double[] tienKhachDua = {0};

        if (!historyMode) {
            pnCash.add(lblPaid, "wrap");
            pnCash.add(lblRemain, "wrap");
            pnCash.add(lblChange, "wrap 10");

            JPanel pnDooms = new JPanel(new GridLayout(3, 3, 6, 6));
            pnDooms.setOpaque(false);

            int[] vals = {500_000, 200_000, 100_000, 50_000, 20_000, 10_000, 5_000, 2_000, 1_000};
            for (int v : vals) {
                JButton btnMoney = chipButton("+" + moneyShort(v));
                btnMoney.putClientProperty("val", v);
                btnMoney.addActionListener(e -> {
                    tienKhachDua[0] += (int) btnMoney.getClientProperty("val");
                    updateCashUI(tienKhachDua[0], tienPhaiTra, lblPaid, lblRemain, lblChange);
                });
                pnDooms.add(btnMoney);
            }
            pnCash.add(pnDooms, "growx, wrap");

            JPanel pnTool = new JPanel(new GridLayout(1, 2, 6, 6));
            pnTool.setOpaque(false);

            JButton btnExact = chipButton("Bằng đúng");
            btnExact.addActionListener(e -> {
                tienKhachDua[0] = tienPhaiTra;
                updateCashUI(tienKhachDua[0], tienPhaiTra, lblPaid, lblRemain, lblChange);
            });

            JButton btnClear = chipButton("Xoá");
            btnClear.addActionListener(e -> {
                tienKhachDua[0] = 0;
                updateCashUI(tienKhachDua[0], tienPhaiTra, lblPaid, lblRemain, lblChange);
            });

            pnTool.add(btnExact);
            pnTool.add(btnClear);
            pnCash.add(pnTool, "growx, wrap 10");

            JLabel lblFinal = boldText("Tổng thanh toán: " + formatVND(tienPhaiTra));
            pnCash.add(lblFinal);

            pnFooter.add(pnCash, "cell 0 4, grow, gaptop 6");
        }

        JPanel pnAction = new JPanel(new MigLayout("wrap, insets 0, gap 8", "[grow,fill]", "[]6[]10[]"));
        pnAction.setOpaque(false);

        String qrTitle;
        boolean allRoomsCompleted =
                (roomLines != null && !roomLines.isEmpty())
                        ? roomLines.stream().allMatch(r -> r.getStatus() != null && r.getStatus().trim().equalsIgnoreCase("Hoàn tất"))
                        : true;

        if (historyMode) qrTitle = "In/Xem hóa đơn";
        else if (paidStatus) qrTitle = "ĐÃ THANH TOÁN";
        else qrTitle = allRoomsCompleted ? "Thanh toán hóa đơn" : "Còn phòng chưa check-out";

        JLabel lblQrTitle = new JLabel(qrTitle, SwingConstants.CENTER);
        lblQrTitle.setFont(BASE_FONT.deriveFont(Font.BOLD, 16f));
        lblQrTitle.setForeground(ACCENT2);
        pnAction.add(lblQrTitle, "growx");

        JLabel lblQRImage = new JLabel("", SwingConstants.CENTER);
        lblQRImage.setOpaque(false);
        lblQRImage.setPreferredSize(new Dimension(220, 220));

        ImageIcon icon = loadIcon(paidStatus ? TICK_IMAGE_PATH : QR_IMAGE_PATH, 220, 220);
        if (icon != null) {
            lblQRImage.setIcon(icon);
        } else {
            lblQRImage.setText("<html><div style='text-align:center;padding:80px 6px;color:#9fb6cc;'>no image</div></html>");
        }
        pnAction.add(lblQRImage, "growx");

        JButton btnAction = primaryButton(historyMode ? "Xem hóa đơn (Ctrl+P)" : "Thanh toán (Ctrl+P)", true);
        pnAction.add(btnAction, "growx");

        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK), "payOrPrint");
        am.put("payOrPrint", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (btnAction.isEnabled()) btnAction.doClick();
            }
        });

        pnFooter.add(pnAction, "cell 1 0, span 1 5, grow");
        pnRoot.add(pnFooter, "growx");

        btnAction.setEnabled(historyMode || (!paidStatus && allRoomsCompleted));
        btnAction.setToolTipText(btnAction.isEnabled() || historyMode ? null : "Còn phòng chưa check-out");

        if (historyMode) {
            btnAction.addActionListener(e -> {
                Window owner = SwingUtilities.getWindowAncestor(this);
                Double cash = ReceiptCache.getCashReceived(order.getOrderId());
                new FormPayOrder(owner, order, roomLines, svLines, LOGO_IMAGE_PATH, cash).setVisible(true);
            });
        } else {
            btnAction.addActionListener(e -> {
                boolean okRooms = (roomLines != null && !roomLines.isEmpty())
                        ? roomLines.stream().allMatch(r -> r.getStatus() != null && r.getStatus().trim().equalsIgnoreCase("Hoàn tất"))
                        : true;

                if (!okRooms) {
                    JOptionPane.showMessageDialog(this,
                            "Còn phòng chưa check-out. Vui lòng hoàn tất check-out trước khi thanh toán.");
                    return;
                }

                if (tienKhachDua[0] + 0.0001 < tongThanhToan) {
                    double thieu = tongThanhToan - tienKhachDua[0];
                    JOptionPane.showMessageDialog(this,
                            "Khách đưa còn thiếu " + formatVND(thieu) + ".\nKhông thể thanh toán.");
                    return;
                }

                try {
                    ReceiptCache.setCashReceived(order.getOrderId(), tienKhachDua[0]);

                    // gui request thanh toan hoa don den server
                    BaseResponse payRes = sendRequest(CommandType.PAY_ORDER, new OrderIdRequestDTO(order.getOrderId()));
                    if (!payRes.isSuccess()) {
                        throw new RuntimeException(payRes.getMessage());
                    }

                    BaseResponse freshOrderRes = sendRequest(CommandType.GET_ORDER_BY_ID, new OrderIdRequestDTO(order.getOrderId()));
                    BaseResponse freshRoomsRes = sendRequest(CommandType.GET_ORDER_ROOM_LINES, new OrderIdRequestDTO(order.getOrderId()));
                    BaseResponse freshServicesRes = sendRequest(CommandType.GET_ORDER_SERVICE_LINES, new OrderIdRequestDTO(order.getOrderId()));

                    OrderDTO freshOrder = freshOrderRes.isSuccess() ? (OrderDTO) freshOrderRes.getData() : order;

                    @SuppressWarnings("unchecked")
                    List<OrderDetailRoomDTO> freshRooms = freshRoomsRes.isSuccess()
                            ? (List<OrderDetailRoomDTO>) freshRoomsRes.getData()
                            : new ArrayList<>();

                    @SuppressWarnings("unchecked")
                    List<OrderDetailServiceDTO> freshServices = freshServicesRes.isSuccess()
                            ? (List<OrderDetailServiceDTO>) freshServicesRes.getData()
                            : new ArrayList<>();

                    if (freshOrder == null) {
                        freshOrder = order;
                    }

                    freshOrder.setTotal(tongThanhToan);
                    freshOrder.setPromotionId(order.getPromotionId());
                    freshOrder.setPromotionName(order.getPromotionName());
                    freshOrder.setPromotionDiscount(order.getPromotionDiscount());
                    JOptionPane.showMessageDialog(this, "Thanh toán thành công!");

                    Window owner = SwingUtilities.getWindowAncestor(this);
                    new FormPayOrder(owner, freshOrder, freshRooms, freshServices, LOGO_IMAGE_PATH, tienKhachDua[0]).setVisible(true);

                    reloadData();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "Thanh toán thất bại: " + ex.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            });
        }

        pnDetail.add(pnRoot, BorderLayout.CENTER);
        pnDetail.revalidate();
        pnDetail.repaint();
    }

    private void updateCashUI(double tienKhachDua, double tienPhaiTra, JLabel lblPaid, JLabel lblRemain, JLabel lblChange) {
        lblPaid.setText("Khách đưa: " + formatVND(tienKhachDua));
        double tienConLai = tienKhachDua - tienPhaiTra;
        if (tienConLai >= 0) {
            lblRemain.setText("Còn thiếu: " + formatVND(0));
            lblChange.setText("Tiền thối: " + formatVND(tienConLai));
        } else {
            lblRemain.setText("Còn thiếu: " + formatVND(-tienConLai));
            lblChange.setText("Tiền thối: " + formatVND(0));
        }
    }

    private double calculateFinalTotal(List<OrderDetailRoomDTO> roomLines, List<OrderDetailServiceDTO> serviceLines, OrderDTO order) {
        double roomsSubtotal = 0d;
        double servicesSubtotal = 0d;

        if (roomLines != null) {
            for (OrderDetailRoomDTO r : roomLines) {
                double fee = r.getRoomFee();
                if (fee <= 0) fee = 0d;
                roomsSubtotal += fee;
            }
        }

        if (serviceLines != null) {
            for (OrderDetailServiceDTO s : serviceLines) {
                servicesSubtotal += s.getServiceFee();
            }
        }

        double tong = roomsSubtotal + servicesSubtotal;

        double discountPercent = order == null ? 0d : order.getPromotionDiscount();
        if (discountPercent < 0) discountPercent = 0;
        if (discountPercent > 100) discountPercent = 100;

        double tienKhuyenMai = tong * discountPercent / 100.0;
        double sauKhuyenMai = tong - tienKhuyenMai;
        double vat = sauKhuyenMai * 0.10;

        return sauKhuyenMai + vat;
    }

    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return client.network.socket.SocketSessionManager.send(
                common.protocol.request.BaseRequest.of(commandType, data)
        );
    }

    private JTable createTable() {
        JTable t = new JTable(orderTableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
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

        t.setRowHeight(30);
        t.setFont(BASE_FONT);
        t.setForeground(TEXT);
        t.setBackground(BG);
        t.setGridColor(new Color(0x13314A));
        t.setFillsViewportHeight(true);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JTableHeader h = t.getTableHeader();
        h.setBackground(new Color(0x102A43));
        h.setForeground(ACCENT);
        h.setFont(BASE_FONT.deriveFont(Font.BOLD, 13f));
        ((DefaultTableCellRenderer) h.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

        int[] widths = {110, 160, 170, 170, 90, 120, 120};
        TableColumnModel cm = t.getColumnModel();
        for (int i = 0; i < cm.getColumnCount() && i < widths.length; i++) {
            cm.getColumn(i).setPreferredWidth(widths[i]);
        }
        return t;
    }

    private JPanel titledCard(String title) {
        JPanel p = new JPanel(new MigLayout("wrap, insets 8, gap 8", "[grow,fill]", ""));
        p.setOpaque(true);
        p.setBackground(CARD_BG);
        var tb = BorderFactory.createTitledBorder(
                new LineBorder(BORDER),
                title, 0, 0,
                BASE_FONT.deriveFont(Font.BOLD),
                ACCENT
        );
        p.setBorder(tb);
        return p;
    }

    private JPanel miniCard() {
        JPanel p = new JPanel(new MigLayout("wrap, gap 4", "[grow,fill]", "[]"));
        p.setOpaque(true);
        p.setBackground(new Color(0x102E4A));
        p.setBorder(new LineBorder(BORDER));
        return p;
    }

    private JLabel text(String s) {
        JLabel l = new JLabel(s);
        l.setFont(BASE_FONT);
        l.setForeground(TEXT);
        return l;
    }

    private JLabel boldText(String s) {
        JLabel l = text(s);
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        l.setForeground(new Color(0xFDE68A));
        return l;
    }

    private JPanel label(String head, String value, boolean highlight) {
        JPanel p = new JPanel(new MigLayout("insets 0, gapx 6", "[][grow,fill]", "[]"));
        p.setOpaque(false);

        JLabel h = new JLabel(head);
        h.setFont(BASE_FONT.deriveFont(Font.BOLD));
        h.setForeground(highlight ? ACCENT2 : ACCENT);

        JLabel v = new JLabel(value == null ? "" : value);
        v.setFont(BASE_FONT);
        v.setForeground(TEXT);

        p.add(h);
        p.add(v, "growx");
        return p;
    }

    private static String format(LocalDateTime dt, DateTimeFormatter f) {
        return dt == null ? "" : f.format(dt);
    }

    private static String formatVND(double v) {
        return String.format("%,.0f VND", v);
    }

    private static String moneyShort(int v) {
        return String.format("%,d", v);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private void styleTextField(JTextField f) {
        f.setFont(BASE_FONT);
        f.setForeground(TEXT);
        f.setBackground(new Color(0x102A43));
        f.setCaretColor(TEXT);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
    }

    private JToggleButton createHistoryToggle(String text) {
        JToggleButton t = new JToggleButton(text);
        t.setFont(BASE_FONT.deriveFont(Font.BOLD));
        t.setFocusPainted(false);
        t.setBorder(new LineBorder(BORDER));
        t.setBackground(new Color(0x102A43));
        t.setForeground(TEXT);
        refreshToggleStyle(t);
        return t;
    }

    private void refreshToggleStyle(JToggleButton t) {
        if (t.isSelected()) {
            t.setBackground(new Color(0xF2C94C));
            t.setForeground(new Color(0x0B1F33));
        } else {
            t.setBackground(new Color(0x102A43));
            t.setForeground(TEXT);
        }
    }

    private void styleDateChooser(com.toedter.calendar.JDateChooser dc) {
        dc.setOpaque(true);
        dc.setBackground(new Color(0x102A43));
        dc.setBorder(new LineBorder(BORDER));

        com.toedter.calendar.JTextFieldDateEditor ed =
                (com.toedter.calendar.JTextFieldDateEditor) dc.getDateEditor();

        ed.setFont(BASE_FONT);
        Color YELLOW = new Color(0xFDE68A);
        ed.setForeground(YELLOW);
        ed.setDisabledTextColor(YELLOW);
        ed.setCaretColor(YELLOW);
        ed.setBackground(new Color(0x102A43));
        ed.setSelectionColor(new Color(0x1E3A5F));
    }
    private JButton primaryButton(String text, boolean solid) {
        JButton b = new JButton(text);
        b.setFont(BASE_FONT.deriveFont(Font.BOLD));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(new Color(0x1B4F72)));
        b.setBackground(solid ? new Color(0x2563EB) : new Color(0x0EA5E9));
        b.setOpaque(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton chipButton(String text) {
        JButton b = new JButton(text);
        b.setFont(BASE_FONT.deriveFont(Font.BOLD, 12f));
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(0x1F6FEB));
        b.setBorder(new LineBorder(new Color(0x0B3D91)));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static String norm(String s) {
        if (s == null) return "";
        String tmp = Normalizer.normalize(s, Normalizer.Form.NFD);
        tmp = tmp.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return tmp.toLowerCase().trim();
    }

    private LocalDate getPickedDate() {
        java.util.Date d = dcOrderDateFilter == null ? null : dcOrderDateFilter.getDate();
        return d == null ? null : d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }


    private void putDateNull(JSpinner sp) {
        sp.putClientProperty("date-null", true);
        if (sp.getEditor() instanceof JSpinner.DefaultEditor de) {
            de.getTextField().setText("");
        }
    }

    private static ImageIcon loadIcon(String path, int w, int h) {
        try {
            java.net.URL url = FormOrderManagement.class.getResource(path);
            if (url == null) return null;
            ImageIcon raw = new ImageIcon(url);
            Image scaled = raw.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception ex) {
            return null;
        }
    }

}