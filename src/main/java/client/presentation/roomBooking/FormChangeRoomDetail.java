package client.presentation.roomBooking;

import client.network.socket.SocketSessionManager;
import com.toedter.calendar.JDateChooser;
import common.dto.OdrInfoDTO;
import common.dto.RoomCalendarSlotDTO;
import common.dto.RoomDTO;
import common.dto.request_dto.CalculateRoomFeeWithNewRoomRequestDTO;
import common.dto.request_dto.ChangeRoomBeforeCheckInRequestDTO;
import common.protocol.command.CommandType;
import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FormChangeRoomDetail extends JDialog {

    private static final Color BG = new Color(0x0B1F33);
    private static final Color CARD_BG = new Color(0x102D4A);
    private static final Color CARD_BG_2 = new Color(0x12395C);
    private static final Color BORDER = new Color(0x274A6B);
    private static final Color FG = new Color(0xE9EEF6);
    private static final Color MUTED = new Color(0xAFC0D0);
    private static final Color PRIMARY = new Color(0xF5C452);
    private static final Color SUCCESS = new Color(0x22C55E);
    private static final Color DANGER = new Color(0xFF6B6B);
    private static final Color WARNING = new Color(0xF59E0B);

    private final String oldRoomID;
    private final RoomDTO newRoom;
    private final OdrInfoDTO infoOldOdr;
    private final FormChangeRoom parent;
    private final List<RoomCalendarSlotDTO> targetRoomBusySlots;

    private JLabel lblOldRoom;
    private JLabel lblOldCustomer;
    private JLabel lblOldPhone;
    private JLabel lblOldOdr;
    private JLabel lblOldTime;
    private JLabel lblNewRoom;
    private JLabel lblNewType;
    private JLabel lblNewPrice;
    private JLabel lblStatus;
    private JTextArea txtNote;

    private JDateChooser dcCheckIn;
    private JDateChooser dcCheckOut;
    private JSpinner spCheckInHour;
    private JSpinner spCheckInMin;
    private JSpinner spCheckOutHour;
    private JSpinner spCheckOutMin;

    private JTable tblBusySlots;
    private BusySlotTableModel busySlotTableModel;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public FormChangeRoomDetail(FormChangeRoom parent,
                                String oldRoomID,
                                RoomDTO newRoom,
                                OdrInfoDTO infoOldOdr,
                                LocalDateTime defaultCheckIn,
                                LocalDateTime defaultCheckOut,
                                List<RoomCalendarSlotDTO> targetRoomBusySlots) {
        super(parent, "Xác nhận đổi phòng", true);
        this.parent = parent;
        this.oldRoomID = oldRoomID;
        this.newRoom = newRoom;
        this.infoOldOdr = infoOldOdr;
        this.targetRoomBusySlots = targetRoomBusySlots == null ? new ArrayList<>() : new ArrayList<>(targetRoomBusySlots);

        initUI(defaultCheckIn, defaultCheckOut);
        bindData();
        recalculateAndValidate();
    }

    public FormChangeRoomDetail(FormChangeRoom parent,
                                String oldRoomID,
                                RoomDTO newRoom,
                                OdrInfoDTO infoOldOdr,
                                LocalDateTime defaultCheckIn,
                                LocalDateTime defaultCheckOut) {
        this(parent, oldRoomID, newRoom, infoOldOdr, defaultCheckIn, defaultCheckOut, new ArrayList<>());
    }

    private void initUI(LocalDateTime defaultCheckIn, LocalDateTime defaultCheckOut) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        setContentPane(root);

        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createBody(defaultCheckIn, defaultCheckOut), BorderLayout.CENTER);
        root.add(createFooter(), BorderLayout.SOUTH);

        applyResponsiveSize(
                1480, 900,
                0.90, 0.90,
                1720, 1040
        );

        setLocationRelativeTo(getOwner());

        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private JComponent createHeader() {
        JPanel header = new JPanel(new MigLayout(
                "insets 22 28 18 28, fillx",
                "[grow]push[]",
                "[]4[]"
        ));
        header.setBackground(BG);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        JLabel title = new JLabel("XÁC NHẬN ĐỔI PHÒNG");
        title.setForeground(FG);
        title.setFont(new Font("Segoe UI", Font.BOLD, 25));

        JLabel subtitle = new JLabel("Kiểm tra phòng cũ, phòng mới, thời gian mới và lịch đã chiếm trước khi cập nhật.");
        subtitle.setForeground(MUTED);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPanel left = new JPanel(new MigLayout("wrap 1, insets 0", "[grow]", "[]2[]"));
        left.setOpaque(false);
        left.add(title);
        left.add(subtitle);

        JLabel badge = new JLabel("Room mới: " + safe(newRoom == null ? null : newRoom.getRoomId()));
        badge.setOpaque(true);
        badge.setBackground(PRIMARY);
        badge.setForeground(new Color(0x0B1F33));
        badge.setFont(new Font("Segoe UI", Font.BOLD, 14));
        badge.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        header.add(left, "grow");
        header.add(badge, "aligny top");

        return header;
    }

    private JComponent createBody(LocalDateTime defaultCheckIn, LocalDateTime defaultCheckOut) {
        JPanel body = new JPanel(new MigLayout(
                "insets 18 22 18 22, fill, gap 16",
                "[67%, grow, fill][33%, grow, fill]",
                "[grow, fill][260!, grow 0, fill]"
        ));
        body.setBackground(BG);

        JPanel leftTop = new JPanel(new MigLayout(
                "wrap 1, insets 0, fill, gapy 14",
                "[grow, fill]",
                "[][grow, fill]"
        ));
        leftTop.setOpaque(false);

        leftTop.add(createComparePanel(), "growx, hmin 250");
        leftTop.add(createTimePanel(defaultCheckIn, defaultCheckOut), "growx, hmin 215");

        JComponent previewPanel = createPreviewPanel();

        JComponent busyPanel = createBusySlotPanel();

        body.add(leftTop, "grow");
        body.add(previewPanel, "grow");
        body.add(busyPanel, "span 2, growx, h 260!");

        return body;
    }

    private JComponent createComparePanel() {
        JPanel panel = cardPanel(new MigLayout(
                "insets 18, fill, gap 14",
                "[50%, grow, fill][50%, grow, fill]",
                "[]10[grow, fill]"
        ));

        JLabel title = sectionTitle("1. Thông tin đổi phòng");
        panel.add(title, "span 2, wrap");

        JPanel oldBox = infoCard("Phòng / booking hiện tại");
        lblOldRoom = infoItem(oldBox, "Phòng cũ", "-");
        lblOldCustomer = infoItem(oldBox, "Khách hàng", "-");
        lblOldPhone = infoItem(oldBox, "Số điện thoại", "-");
        lblOldOdr = infoItem(oldBox, "Mã ODR", "-");
        lblOldTime = infoItemFull(oldBox, "Khung giờ cũ", "-");

        JPanel newBox = infoCard("Phòng mới");
        lblNewRoom = infoItemFull(newBox, "Phòng mới", "-");
        lblNewType = infoItem(newBox, "Loại phòng", "-");
        JLabel lblView = infoItem(newBox, "View", safe(newRoom == null ? null : newRoom.getView()));
        lblView.setText(safe(newRoom == null ? null : newRoom.getView()));

        panel.add(oldBox, "grow, hmin 190");
        panel.add(newBox, "grow, hmin 190");

        return panel;
    }

    private JComponent createTimePanel(LocalDateTime defaultCheckIn, LocalDateTime defaultCheckOut) {
        JPanel panel = cardPanel(new MigLayout(
                "wrap 2, insets 20, gapx 16, gapy 14, fillx",
                "[170!, right][grow, fill]",
                "[]10[][][]"
        ));

        panel.add(sectionTitle("2. Thiết lập thời gian mới"), "span 2, left, wrap");

        LocalDateTime checkIn = defaultCheckIn == null ? LocalDateTime.now() : defaultCheckIn;
        LocalDateTime checkOut = defaultCheckOut == null ? checkIn.plusDays(1) : defaultCheckOut;

        dcCheckIn = new JDateChooser();
        dcCheckIn.setDateFormatString("dd/MM/yyyy");
        dcCheckIn.setDate(toDate(checkIn.toLocalDate()));
        styleDateChooser(dcCheckIn);

        dcCheckOut = new JDateChooser();
        dcCheckOut.setDateFormatString("dd/MM/yyyy");
        dcCheckOut.setDate(toDate(checkOut.toLocalDate()));
        styleDateChooser(dcCheckOut);

        spCheckInHour = timeSpinner(checkIn.getHour(), 0, 23, 1);
        spCheckInMin = timeSpinner(checkIn.getMinute(), 0, 59, 5);
        spCheckOutHour = timeSpinner(checkOut.getHour(), 0, 23, 1);
        spCheckOutMin = timeSpinner(checkOut.getMinute(), 0, 59, 5);

        panel.add(fieldLabel("Check-in mới"));
        panel.add(timePickerRow(dcCheckIn, spCheckInHour, spCheckInMin), "growx");

        panel.add(fieldLabel("Check-out mới"));
        panel.add(timePickerRow(dcCheckOut, spCheckOutHour, spCheckOutMin), "growx");

        lblNewPrice = valueBox("...");
        panel.add(fieldLabel("Giá phòng mới"));
        panel.add(lblNewPrice, "growx");

        JButton btnCalculate = button("TÍNH GIÁ / KIỂM TRA", PRIMARY, new Color(0x0B1F33));
        btnCalculate.addActionListener(e -> recalculateAndValidate());

        JPanel quickPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        quickPanel.setOpaque(false);

        JButton btnKeepOld = smallButton("Giữ giờ cũ");
        JButton btnPlus1Day = smallButton("+1 ngày");
        JButton btnPlus3Day = smallButton("+3 ngày");

        btnKeepOld.addActionListener(e -> {
            setDateTime(dcCheckIn, spCheckInHour, spCheckInMin, infoOldOdr.getCheckIn());
            setDateTime(dcCheckOut, spCheckOutHour, spCheckOutMin, infoOldOdr.getCheckOut());
            recalculateAndValidate();
        });

        btnPlus1Day.addActionListener(e -> {
            LocalDateTime in = getCheckIn();
            if (in != null) {
                setDateTime(dcCheckOut, spCheckOutHour, spCheckOutMin, in.plusDays(1));
                recalculateAndValidate();
            }
        });

        btnPlus3Day.addActionListener(e -> {
            LocalDateTime in = getCheckIn();
            if (in != null) {
                setDateTime(dcCheckOut, spCheckOutHour, spCheckOutMin, in.plusDays(3));
                recalculateAndValidate();
            }
        });

        quickPanel.add(btnCalculate);
        quickPanel.add(btnKeepOld);
        quickPanel.add(btnPlus1Day);
        quickPanel.add(btnPlus3Day);

        panel.add(new JLabel());
        panel.add(quickPanel, "growx");

        addChangeListeners();

        return panel;
    }

    private JComponent createBusySlotPanel() {
        JPanel panel = cardPanel(new MigLayout(
                "wrap 1, insets 16, fill",
                "[grow, fill]",
                "[]10[grow, fill]"
        ));

        JLabel title = sectionTitle("3. Khung giờ đã chiếm của phòng mới");
        panel.add(title, "growx");

        busySlotTableModel = new BusySlotTableModel(targetRoomBusySlots);
        tblBusySlots = new JTable(busySlotTableModel);
        styleTable(tblBusySlots);

        tblBusySlots.setFillsViewportHeight(true);

        tblBusySlots.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        int[] widths = {90, 180, 150, 150};
        for (int i = 0; i < widths.length && i < tblBusySlots.getColumnCount(); i++) {
            tblBusySlots.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            tblBusySlots.getColumnModel().getColumn(i).setMinWidth(55);
        }

        JScrollPane scroll = new JScrollPane(
                tblBusySlots,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(CARD_BG);

        panel.add(scroll, "grow");

        return panel;
    }

    private JComponent createPreviewPanel() {
        JPanel panel = cardPanel(new MigLayout(
                "wrap 1, insets 20, fill",
                "[grow, fill]",
                "[]12[][][][][][][]push[]"
        ));

        panel.add(sectionTitle("Xem trước cập nhật"));

        panel.add(previewItem("Phòng cũ", oldRoomID), "growx");
        panel.add(previewItem("Phòng mới", safe(newRoom == null ? null : newRoom.getRoomId())), "growx");
        panel.add(previewItem("Khách hàng", safe(infoOldOdr == null ? null : infoOldOdr.getFullName())), "growx");
        panel.add(previewItem("Số điện thoại", safe(infoOldOdr == null ? null : infoOldOdr.getPhone())), "growx");
        panel.add(previewItem("Mã ODR", safe(infoOldOdr == null ? null : infoOldOdr.getOrderDetailRoomId())), "growx");
        panel.add(previewItem("Kiểu đặt", safe(infoOldOdr == null ? null : infoOldOdr.getBookingType())), "growx");

        lblStatus = new JLabel("Chưa kiểm tra");
        lblStatus.setOpaque(true);
        lblStatus.setForeground(Color.WHITE);
        lblStatus.setBackground(WARNING);
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblStatus.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        panel.add(new JLabel("Trạng thái kiểm tra") {{
            setForeground(MUTED);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
        }});
        panel.add(lblStatus, "growx");

        txtNote = new JTextArea();
        txtNote.setEditable(false);
        txtNote.setLineWrap(true);
        txtNote.setWrapStyleWord(true);
        txtNote.setOpaque(true);
        txtNote.setBackground(CARD_BG_2);
        txtNote.setForeground(FG);
        txtNote.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtNote.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        panel.add(new JLabel("Ghi chú") {{
            setForeground(MUTED);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
        }});
        panel.add(txtNote, "grow, h 180!");

        return panel;
    }

    private JComponent createFooter() {
        JPanel footer = new JPanel(new MigLayout(
                "insets 14 28 18 28, fillx",
                "[grow]push[]12[]",
                "[]"
        ));
        footer.setBackground(BG);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));

        JLabel note = new JLabel("Hệ thống sẽ kiểm tra trùng lịch ở server khi xác nhận. Nếu trùng khung giờ, thao tác sẽ bị từ chối.");
        note.setForeground(MUTED);
        note.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JButton btnClose = button("ĐÓNG", new Color(0x153C5B), FG);
        JButton btnConfirm = button("XÁC NHẬN ĐỔI PHÒNG", PRIMARY, new Color(0x0B1F33));

        btnClose.addActionListener(e -> dispose());
        btnConfirm.addActionListener(e -> confirmChangeRoom());

        footer.add(note, "grow");
        footer.add(btnClose, "w 130!, h 42!");
        footer.add(btnConfirm, "w 210!, h 42!");

        return footer;
    }

    private void bindData() {
        lblOldRoom.setText(oldRoomID);
        lblOldCustomer.setText(safe(infoOldOdr == null ? null : infoOldOdr.getFullName()));
        lblOldPhone.setText(safe(infoOldOdr == null ? null : infoOldOdr.getPhone()));
        lblOldOdr.setText(safe(infoOldOdr == null ? null : infoOldOdr.getOrderDetailRoomId()));
        lblOldTime.setText(format(infoOldOdr == null ? null : infoOldOdr.getCheckIn()) + "  →  " + format(infoOldOdr == null ? null : infoOldOdr.getCheckOut()));

        lblNewRoom.setText(
                safe(newRoom == null ? null : newRoom.getRoomId())
                        + " - " + safe(newRoom == null ? null : newRoom.getDescription())
        );
        lblNewType.setText(
                safe(newRoom == null ? null : newRoom.getRoomTypeName())
                        + " | View: " + safe(newRoom == null ? null : newRoom.getView())
        );
    }

    private void addChangeListeners() {
        dcCheckIn.addPropertyChangeListener("date", e -> recalculateAndValidate());
        dcCheckOut.addPropertyChangeListener("date", e -> recalculateAndValidate());

        spCheckInHour.addChangeListener(e -> recalculateAndValidate());
        spCheckInMin.addChangeListener(e -> recalculateAndValidate());
        spCheckOutHour.addChangeListener(e -> recalculateAndValidate());
        spCheckOutMin.addChangeListener(e -> recalculateAndValidate());
    }

    private void recalculateAndValidate() {
        LocalDateTime checkIn = getCheckIn();
        LocalDateTime checkOut = getCheckOut();

        if (checkIn == null || checkOut == null) {
            setInvalid("Chưa chọn đủ ngày check-in/check-out.", "Vui lòng chọn đầy đủ ngày và giờ.");
            lblNewPrice.setText("-");
            return;
        }

        if (!checkOut.isAfter(checkIn)) {
            setInvalid("Thời gian không hợp lệ.", "Check-out mới phải sau check-in mới.");
            lblNewPrice.setText("-");
            return;
        }

        RoomCalendarSlotDTO conflict = findConflict(checkIn, checkOut);
        if (conflict != null) {
            setInvalid(
                    "Bị trùng lịch phòng mới.",
                    "Khung giờ mới đang giao với booking/check-in của phòng "
                            + safe(conflict.getRoomId()) + ":\n"
                            + "- Khách: " + safe(conflict.getCustomer()) + "\n"
                            + "- SĐT: " + safe(conflict.getPhone()) + "\n"
                            + "- Thời gian: " + format(conflict.getCheckIn()) + " → " + format(conflict.getCheckOut()) + "\n\n"
                            + "Vui lòng chọn khung giờ khác."
            );
            lblNewPrice.setText("-");
            return;
        }

        try {
            BaseResponse response = sendRequest(
                    CommandType.CALCULATE_ROOM_FEE_WITH_NEW_ROOM,
                    new CalculateRoomFeeWithNewRoomRequestDTO(newRoom.getRoomId(), infoOldOdr.getBookingType(), checkIn, checkOut)
            );

            if (!response.isSuccess()) {
                setInvalid("Không tính được giá.", response.getMessage());
                lblNewPrice.setText("-");
                return;
            }

            Double roomFee = response.getData() instanceof Double d ? d : null;
            lblNewPrice.setText(roomFee == null ? "(Không tính được)" : formatMoney(roomFee));

            setValid(
                    "Hợp lệ",
                    "Khung giờ mới không trùng với lịch đã chiếm của phòng mới.\n"
                            + "Check-in mới: " + format(checkIn) + "\n"
                            + "Check-out mới: " + format(checkOut) + "\n"
                            + "Giá phòng mới: " + (roomFee == null ? "-" : formatMoney(roomFee))
            );

        } catch (Exception ex) {
            setInvalid("Lỗi kiểm tra.", ex.getMessage());
            lblNewPrice.setText("-");
        }
    }

    private void confirmChangeRoom() {
        LocalDateTime checkIn = getCheckIn();
        LocalDateTime checkOut = getCheckOut();

        if (infoOldOdr == null || infoOldOdr.getOrderDetailRoomId() == null || infoOldOdr.getOrderDetailRoomId().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không xác định được booking cần đổi phòng.");
            return;
        }

        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            JOptionPane.showMessageDialog(this, "Check-out mới phải sau check-in mới.");
            return;
        }

        RoomCalendarSlotDTO conflict = findConflict(checkIn, checkOut);
        if (conflict != null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể đổi phòng vì phòng mới bị trùng lịch:\n"
                            + safe(conflict.getCustomer()) + " | "
                            + format(conflict.getCheckIn()) + " → " + format(conflict.getCheckOut()),
                    "Trùng lịch",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Xác nhận đổi booking " + infoOldOdr.getOrderDetailRoomId()
                        + "\nTừ phòng: " + oldRoomID
                        + "\nSang phòng: " + newRoom.getRoomId()
                        + "\nThời gian mới: " + format(checkIn) + " → " + format(checkOut),
                "Xác nhận đổi phòng",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            BaseResponse response = sendRequest(
                    CommandType.CHANGE_ROOM_BEFORE_CHECKIN,
                    new ChangeRoomBeforeCheckInRequestDTO(infoOldOdr.getOrderDetailRoomId(), oldRoomID, newRoom.getRoomId(), checkIn, checkOut)
            );

            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Đổi phòng thành công!");
                dispose();
                parent.closeAfterSuccess();
            }
            else {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Không thể đổi phòng", JOptionPane.WARNING_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private RoomCalendarSlotDTO findConflict(LocalDateTime checkIn, LocalDateTime checkOut) {
        if (checkIn == null || checkOut == null) return null;

        String currentOdrId = infoOldOdr == null ? "" : safeRaw(infoOldOdr.getOrderDetailRoomId());

        for (RoomCalendarSlotDTO slot : targetRoomBusySlots) {
            if (slot == null) continue;

            String slotStatus = safeRaw(slot.getStatus());
            if (!slotStatus.equalsIgnoreCase("Đặt") && !slotStatus.equalsIgnoreCase("Check-in")) {
                continue;
            }

            if (currentOdrId.length() > 0 && currentOdrId.equalsIgnoreCase(safeRaw(slot.getRoomId()))) {
                continue;
            }

            LocalDateTime busyStart = slot.getCheckIn();
            LocalDateTime busyEnd = slot.getCheckOut();

            if (busyStart == null || busyEnd == null) continue;

            boolean overlap = checkIn.isBefore(busyEnd) && checkOut.isAfter(busyStart);
            if (overlap) {
                return slot;
            }
        }

        return null;
    }

    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return SocketSessionManager.send(BaseRequest.of(commandType, data));
    }

    private LocalDateTime getCheckIn() {
        return getDateTime(dcCheckIn, spCheckInHour, spCheckInMin);
    }

    private LocalDateTime getCheckOut() {
        return getDateTime(dcCheckOut, spCheckOutHour, spCheckOutMin);
    }

    private LocalDateTime getDateTime(JDateChooser chooser, JSpinner hour, JSpinner minute) {
        Date date = chooser.getDate();
        if (date == null) return null;

        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int h = (int) hour.getValue();
        int m = (int) minute.getValue();

        return LocalDateTime.of(localDate, LocalTime.of(h, m));
    }

    private void setDateTime(JDateChooser chooser, JSpinner hour, JSpinner minute, LocalDateTime time) {
        if (time == null) return;

        chooser.setDate(toDate(time.toLocalDate()));
        hour.setValue(time.getHour());
        minute.setValue(time.getMinute());
    }

    private Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private void setValid(String status, String note) {
        lblStatus.setText(status);
        lblStatus.setBackground(SUCCESS);
        lblStatus.setForeground(Color.WHITE);
        txtNote.setText(note);
    }

    private void setInvalid(String status, String note) {
        lblStatus.setText(status);
        lblStatus.setBackground(DANGER);
        lblStatus.setForeground(Color.WHITE);
        txtNote.setText(note);
    }

    private JPanel cardPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        return panel;
    }

    private JPanel infoCard(String title) {
        JPanel box = new JPanel(new MigLayout(
                "wrap 2, insets 14, gapx 12, gapy 10, fillx",
                "[grow, fill][grow, fill]",
                "[]"
        ));
        box.setBackground(CARD_BG_2);
        box.setBorder(BorderFactory.createLineBorder(BORDER));

        JLabel lbTitle = new JLabel(title);
        lbTitle.setForeground(PRIMARY);
        lbTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        box.add(lbTitle, "span 2, wrap, gapbottom 4");

        return box;
    }

    private JLabel infoItem(JPanel panel, String title, String value) {
        JPanel box = miniInfoBox(title, value);
        JLabel valueLabel = (JLabel) box.getClientProperty("valueLabel");
        panel.add(box, "growx");
        return valueLabel;
    }

    private JLabel infoItemFull(JPanel panel, String title, String value) {
        JPanel box = miniInfoBox(title, value);
        JLabel valueLabel = (JLabel) box.getClientProperty("valueLabel");
        panel.add(box, "span 2, growx");
        return valueLabel;
    }

    private JPanel miniInfoBox(String title, String value) {
        JPanel box = new JPanel(new MigLayout("wrap 1, insets 8 10 8 10", "[grow]", "[]2[]"));
        box.setBackground(CARD_BG);
        box.setBorder(BorderFactory.createLineBorder(BORDER));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(MUTED);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setForeground(FG);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        box.add(titleLabel);
        box.add(valueLabel, "growx");

        box.putClientProperty("valueLabel", valueLabel);
        return box;
    }

    private JPanel previewItem(String title, String value) {
        JPanel box = new JPanel(new MigLayout("wrap 1, insets 10 12 10 12", "[grow]", "[]2[]"));
        box.setBackground(CARD_BG_2);
        box.setBorder(BorderFactory.createLineBorder(BORDER));

        JLabel lbTitle = new JLabel(title);
        lbTitle.setForeground(MUTED);
        lbTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JLabel lbValue = new JLabel(value);
        lbValue.setForeground(FG);
        lbValue.setFont(new Font("Segoe UI", Font.BOLD, 13));

        box.add(lbTitle);
        box.add(lbValue);

        return box;
    }

    private JLabel sectionTitle(String text) {
        JLabel lb = new JLabel(text);
        lb.setForeground(PRIMARY);
        lb.setFont(new Font("Segoe UI", Font.BOLD, 17));
        return lb;
    }

    private JLabel fieldLabel(String text) {
        JLabel lb = new JLabel(text + ":");
        lb.setForeground(FG);
        lb.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return lb;
    }

    private JLabel valueBox(String value) {
        JLabel lb = new JLabel(value);
        lb.setOpaque(true);
        lb.setBackground(CARD_BG_2);
        lb.setForeground(FG);
        lb.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(9, 12, 9, 12)
        ));
        return lb;
    }

    private JSpinner timeSpinner(int value, int min, int max, int step) {
        JSpinner sp = new JSpinner(new SpinnerNumberModel(value, min, max, step));
        sp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sp.setPreferredSize(new Dimension(72, 34));
        return sp;
    }

    private JPanel timePickerRow(JDateChooser dateChooser, JSpinner hour, JSpinner minute) {
        JPanel row = new JPanel(new MigLayout(
                "insets 0, gap 10",
                "[][45!][][45!]",
                "[]"
        ));
        row.setOpaque(false);

        JLabel lbHour = new JLabel("Giờ");
        lbHour.setForeground(MUTED);
        lbHour.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JLabel lbMin = new JLabel("Phút");
        lbMin.setForeground(MUTED);
        lbMin.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        row.add(dateChooser, "w 190!, h 36!");
        row.add(lbHour);
        row.add(hour, "w 78!, h 36!");
        row.add(lbMin);
        row.add(minute, "w 78!, h 36!");

        return row;
    }

    private void styleDateChooser(JDateChooser chooser) {
        chooser.setPreferredSize(new Dimension(190, 36));
        chooser.setBackground(CARD_BG_2);
        chooser.setForeground(FG);
        chooser.setBorder(BorderFactory.createLineBorder(BORDER));
        chooser.getDateEditor().getUiComponent().setBackground(CARD_BG_2);
        chooser.getDateEditor().getUiComponent().setForeground(FG);
        chooser.getDateEditor().getUiComponent().setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }

    private JButton button(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(bg.darker()));
        return btn;
    }

    private JButton smallButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(0x153C5B));
        btn.setForeground(FG);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(BORDER));
        return btn;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(34);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setForeground(FG);
        table.setBackground(CARD_BG);
        table.setSelectionBackground(new Color(0x1D4F7A));
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(BORDER);
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setForeground(PRIMARY);
        table.getTableHeader().setBackground(new Color(0x0E2A44));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(true);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setBackground(CARD_BG);
        renderer.setForeground(FG);
        renderer.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
        renderer.setHorizontalAlignment(SwingConstants.LEFT);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    private String formatMoney(double money) {
        return String.format("%,.0f VNĐ", money);
    }

    private String format(LocalDateTime time) {
        return time == null ? "-" : time.format(dtf);
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    private String safeRaw(String value) {
        return value == null ? "" : value.trim();
    }

    private void applyResponsiveSize(int minW, int minH, double widthRatio, double heightRatio, int maxW, int maxH) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        int w = (int) (screen.width * widthRatio);
        int h = (int) (screen.height * heightRatio);

        w = Math.max(minW, Math.min(w, maxW));
        h = Math.max(minH, Math.min(h, maxH));

        setMinimumSize(new Dimension(minW, minH));
        setPreferredSize(new Dimension(w, h));
        setSize(w, h);
    }

    private class BusySlotTableModel extends AbstractTableModel {
        private final String[] columns = {"Trạng thái", "Khách hàng", "Check-in", "Check-out"};
        private final List<RoomCalendarSlotDTO> rows = new ArrayList<>();

        public BusySlotTableModel(List<RoomCalendarSlotDTO> data) {
            if (data != null) rows.addAll(data);
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            RoomCalendarSlotDTO s = rows.get(rowIndex);

            return switch (columnIndex) {
                case 0 -> safe(s.getStatus());
                case 1 -> safe(s.getCustomer());
                case 2 -> format(s.getCheckIn());
                case 3 -> format(s.getCheckOut());
                default -> "";
            };
        }
    }
}