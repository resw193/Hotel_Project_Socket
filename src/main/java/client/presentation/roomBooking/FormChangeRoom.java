package client.presentation.roomBooking;

import client.network.socket.SocketSessionManager;
import common.dto.OdrInfoDTO;
import common.dto.RoomCalendarSlotDTO;
import common.dto.RoomDTO;
import common.dto.request_dto.RoomCalendarRequestDTO;
import common.dto.request_dto.RoomIdRequestDTO;
import common.protocol.command.CommandType;
import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;
import net.miginfocom.swing.MigLayout;
import server.core.service.RoomService;
import server.core.service.RoomStayService;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FormChangeRoom extends JDialog {

    private static final Color BG = new Color(0x0B1F33);
    private static final Color CARD_BG = new Color(0x102D4A);
    private static final Color CARD_BG_2 = new Color(0x12395C);
    private static final Color BORDER = new Color(0x274A6B);
    private static final Color FG = new Color(0xE9EEF6);
    private static final Color MUTED = new Color(0xAFC0D0);
    private static final Color PRIMARY = new Color(0xF5C452);
    private static final Color PRIMARY_HOVER = new Color(0xFFD36E);
    private static final Color DANGER = new Color(0xFF6B6B);
    private static final Color SUCCESS = new Color(0x22C55E);

    private final String oldRoomID;
    private final FormRoomBookingManagement parent;

    private JComboBox<OdrInfoDTO> cbxBookings;
    private JComboBox<RoomDTO> cbxNewRoom;

    private JLabel lblCustomer;
    private JLabel lblPhone;
    private JLabel lblOdrId;
    private JLabel lblBookingType;
    private JLabel lblCurrentTime;
    private JLabel lblNewRoomInfo;
    private JLabel lblConflictHint;

    private JTable tblTargetRoomSchedule;
    private TargetRoomScheduleTableModel scheduleTableModel;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public FormChangeRoom(Window owner, String oldRoomID, RoomService roomService, RoomStayService roomStayService, FormRoomBookingManagement parent) {
        super(owner, "Đổi phòng trước check-in", ModalityType.APPLICATION_MODAL);
        this.oldRoomID = oldRoomID;
        this.parent = parent;

        initUI();
        loadBookings();
        loadRooms();
    }

    private void initUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        setContentPane(root);

        root.add(createHeader(), BorderLayout.NORTH);
        root.add(createBody(), BorderLayout.CENTER);
        root.add(createFooter(), BorderLayout.SOUTH);

        applyResponsiveSize(
                1320, 780,
                0.84, 0.82,
                1560, 920
        );

        setLocationRelativeTo(getOwner());
    }

    private JComponent createHeader() {
        JPanel header = new JPanel(new MigLayout(
                "insets 22 28 18 28, fillx",
                "[grow]push[]",
                "[]4[]"
        ));
        header.setBackground(BG);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        JLabel title = new JLabel("ĐỔI PHÒNG TRƯỚC CHECK-IN");
        title.setForeground(PRIMARY);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel subtitle = new JLabel("Chọn đúng booking cần đổi, chọn phòng mới và kiểm tra lịch phòng mới trước khi xác nhận.");
        subtitle.setForeground(MUTED);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPanel left = new JPanel(new MigLayout("insets 0, wrap 1", "[grow]", "[]2[]"));
        left.setOpaque(false);
        left.add(title);
        left.add(subtitle);

        JLabel roomBadge = new JLabel("Phòng cũ: " + oldRoomID);
        roomBadge.setOpaque(true);
        roomBadge.setBackground(PRIMARY);
        roomBadge.setForeground(new Color(0x0B1F33));
        roomBadge.setFont(new Font("Segoe UI", Font.BOLD, 14));
        roomBadge.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        header.add(left, "grow");
        header.add(roomBadge, "aligny top");

        return header;
    }

    private JComponent createBody() {
        JPanel body = new JPanel(new MigLayout(
                "insets 22 28 22 28, fill, gap 18",
                "[56%, grow, fill][44%, grow, fill]",
                "[grow, fill]"
        ));
        body.setBackground(BG);

        body.add(createSelectionPanel(), "grow");
        body.add(createSchedulePanel(), "grow");

        return body;
    }

    private JComponent createSelectionPanel() {
        JPanel panel = cardPanel(new MigLayout(
                "wrap 2, insets 22, gapx 14, gapy 14, fillx",
                "[145!, right][grow, fill]",
                "[][][]18[][][][]"
        ));

        JLabel sectionTitle = sectionTitle("1. Chọn booking và phòng mới");
        panel.add(sectionTitle, "span 2, left, gapbottom 8");

        panel.add(fieldLabel("Phòng cũ"));
        panel.add(readOnlyBox(oldRoomID), "growx");

        cbxBookings = new JComboBox<>();
        cbxBookings.setRenderer(new BookingRenderer());
        styleCombo(cbxBookings);
        cbxBookings.addActionListener(e -> updateSelectedBookingInfo());

        panel.add(fieldLabel("Booking cần đổi"));
        panel.add(cbxBookings, "growx, h 36!");

        cbxNewRoom = new JComboBox<>();
        cbxNewRoom.setRenderer(new RoomRenderer());
        styleCombo(cbxNewRoom);
        cbxNewRoom.addActionListener(e -> {
            updateNewRoomInfo();
            loadTargetRoomSchedule();
        });

        panel.add(fieldLabel("Phòng mới"));
        panel.add(cbxNewRoom, "growx, h 36!");

        JPanel infoPanel = new JPanel(new MigLayout(
                "wrap 2, insets 16, gapx 12, gapy 12, fillx",
                "[grow, fill][grow, fill]",
                "[][][]"
        ));
        infoPanel.setBackground(CARD_BG_2);
        infoPanel.setBorder(BorderFactory.createLineBorder(BORDER));

        lblCustomer = infoItem(infoPanel, "Khách hàng", "-");
        lblPhone = infoItem(infoPanel, "Số điện thoại", "-");
        lblOdrId = infoItem(infoPanel, "Mã ODR", "-");
        lblBookingType = infoItem(infoPanel, "Kiểu đặt", "-");
        lblCurrentTime = infoItemFull(infoPanel, "Khung giờ booking đang chọn", "-");
        lblNewRoomInfo = infoItemFull(infoPanel, "Thông tin phòng mới", "-");

        panel.add(new JLabel(), "skip");
        panel.add(infoPanel, "growx, hmin 260");

        return panel;
    }

    private JComponent createSchedulePanel() {
        JPanel panel = cardPanel(new MigLayout(
                "wrap 1, insets 22, fill",
                "[grow, fill]",
                "[]10[]10[grow, fill]10[]"
        ));

        JLabel title = sectionTitle("2. Lịch đã chiếm của phòng mới");
        panel.add(title);

        JTextArea note = new JTextArea(
                "Các khung giờ dưới đây là booking hoặc check-in hiện có của phòng mới. " +
                        "Khi đổi phòng, khoảng check-in/check-out mới không được giao nhau với các khung giờ này."
        );
        note.setWrapStyleWord(true);
        note.setLineWrap(true);
        note.setEditable(false);
        note.setOpaque(false);
        note.setForeground(MUTED);
        note.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(note, "growx");

        scheduleTableModel = new TargetRoomScheduleTableModel();
        tblTargetRoomSchedule = new JTable(scheduleTableModel);
        styleTable(tblTargetRoomSchedule);

        JScrollPane scroll = new JScrollPane(tblTargetRoomSchedule);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(CARD_BG);
        panel.add(scroll, "grow, h 500!");

        lblConflictHint = new JLabel("Chọn phòng mới để xem lịch đã đặt/check-in.");
        lblConflictHint.setForeground(MUTED);
        lblConflictHint.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblConflictHint.setBorder(BorderFactory.createEmptyBorder(8, 4, 0, 4));
        panel.add(lblConflictHint, "growx");

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

        JLabel note = new JLabel("Hãy kiểm tra đúng khách hàng, booking, phòng mới và các khung giờ đã chiếm trước khi tiếp tục.");
        note.setForeground(MUTED);
        note.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JButton btnClose = button("ĐÓNG", new Color(0x153C5B), FG);
        JButton btnNext = button("TIẾP TỤC", PRIMARY, new Color(0x0B1F33));

        btnClose.addActionListener(e -> dispose());
        btnNext.addActionListener(e -> openDetail());

        footer.add(note, "grow");
        footer.add(btnClose, "w 110!, h 38!");
        footer.add(btnNext, "w 150!, h 38!");

        return footer;
    }

    private void loadBookings() {
        try {
            BaseResponse response = sendRequest(CommandType.GET_PENDING_BOOKINGS_OF_ROOM, new RoomIdRequestDTO(oldRoomID));

            if (!response.isSuccess()) {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                dispose();
                return;
            }

            DefaultComboBoxModel<OdrInfoDTO> model = new DefaultComboBoxModel<>();

            if (response.getData() instanceof List<?> rawList) {
                for (Object obj : rawList) {
                    if (obj instanceof OdrInfoDTO odr) {
                        model.addElement(odr);
                    }
                }
            }

            cbxBookings.setModel(model);

            if (model.getSize() == 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Phòng " + oldRoomID + " không có booking chờ để đổi phòng.",
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE
                );
                dispose();
                return;
            }

            cbxBookings.setSelectedIndex(0);
            updateSelectedBookingInfo();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    private void loadRooms() {
        try {
            BaseResponse response = sendRequest(CommandType.GET_ALL_ROOMS, null);

            if (!response.isSuccess()) {
                throw new RuntimeException(response.getMessage());
            }

            @SuppressWarnings("unchecked")
            List<RoomDTO> rooms = (List<RoomDTO>) response.getData();
            if (rooms == null) rooms = new ArrayList<>();

            DefaultComboBoxModel<RoomDTO> model = new DefaultComboBoxModel<>();

            for (RoomDTO room : rooms) {
                if (room == null || room.getRoomId() == null) continue;

                if (!oldRoomID.equalsIgnoreCase(room.getRoomId())) {
                    model.addElement(room);
                }
            }

            cbxNewRoom.setModel(model);

            if (model.getSize() > 0) {
                cbxNewRoom.setSelectedIndex(0);
            }

            updateNewRoomInfo();
            loadTargetRoomSchedule();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSelectedBookingInfo() {
        OdrInfoDTO odr = getSelectedBooking();

        if (odr == null) {
            lblCustomer.setText("-");
            lblPhone.setText("-");
            lblOdrId.setText("-");
            lblBookingType.setText("-");
            lblCurrentTime.setText("-");
            return;
        }

        lblCustomer.setText(safe(odr.getFullName()));
        lblPhone.setText(safe(odr.getPhone()));
        lblOdrId.setText(safe(odr.getOrderDetailRoomId()));
        lblBookingType.setText(safe(odr.getBookingType()));
        lblCurrentTime.setText(format(odr.getCheckIn()) + "  →  " + format(odr.getCheckOut()));
    }

    private void updateNewRoomInfo() {
        RoomDTO room = getSelectedRoom();

        if (room == null) {
            lblNewRoomInfo.setText("-");
            return;
        }

        lblNewRoomInfo.setText(safe(room.getRoomId()) + " - " + safe(room.getDescription()) + " - " + safe(room.getRoomTypeName()) + " - View: " + safe(room.getView()));
    }

    private void loadTargetRoomSchedule() {
        RoomDTO room = getSelectedRoom();

        if (room == null || room.getRoomId() == null) {
            scheduleTableModel.setRows(new ArrayList<>());
            lblConflictHint.setText("Chưa chọn phòng mới.");
            lblConflictHint.setForeground(MUTED);
            return;
        }

        try {
            LocalDate from = LocalDate.now().minusMonths(1);
            LocalDate to = LocalDate.now().plusYears(1);

            BaseResponse response = sendRequest(
                    CommandType.GET_ROOM_CALENDAR,
                    new RoomCalendarRequestDTO(from, to)
            );

            if (!response.isSuccess()) {
                scheduleTableModel.setRows(new ArrayList<>());
                lblConflictHint.setText(response.getMessage());
                lblConflictHint.setForeground(DANGER);
                return;
            }

            List<RoomCalendarSlotDTO> rows = new ArrayList<>();

            if (response.getData() instanceof List<?> rawList) {
                for (Object obj : rawList) {
                    if (obj instanceof RoomCalendarSlotDTO slot
                            && room.getRoomId().equalsIgnoreCase(safeRaw(slot.getRoomId()))
                            && isBusyStatus(slot.getStatus())) {
                        rows.add(slot);
                    }
                }
            }

            rows.sort((a, b) -> {
                LocalDateTime x = a.getCheckIn();
                LocalDateTime y = b.getCheckIn();
                if (x == null && y == null) return 0;
                if (x == null) return 1;
                if (y == null) return -1;
                return x.compareTo(y);
            });

            scheduleTableModel.setRows(rows);

            if (rows.isEmpty()) {
                lblConflictHint.setText("Phòng " + room.getRoomId() + " chưa có booking/check-in trong khoảng kiểm tra. Có thể chọn nếu thời gian hợp lệ.");
                lblConflictHint.setForeground(SUCCESS);
            } else {
                lblConflictHint.setText("Phòng " + room.getRoomId() + " có " + rows.size() + " khung giờ đã bị chiếm. Hãy chọn khung giờ mới không giao nhau.");
                lblConflictHint.setForeground(PRIMARY);
            }

        } catch (Exception ex) {
            scheduleTableModel.setRows(new ArrayList<>());
            lblConflictHint.setText(ex.getMessage());
            lblConflictHint.setForeground(DANGER);
        }
    }

    private void openDetail() {
        OdrInfoDTO selectedBooking = getSelectedBooking();
        RoomDTO newRoom = getSelectedRoom();

        if (selectedBooking == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách/booking cần đổi phòng.");
            return;
        }

        if (selectedBooking.getOrderDetailRoomId() == null || selectedBooking.getOrderDetailRoomId().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Booking được chọn thiếu mã ODR.");
            return;
        }

        if (newRoom == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phòng mới.");
            return;
        }

        FormChangeRoomDetail detail = new FormChangeRoomDetail(this, oldRoomID, newRoom, selectedBooking, selectedBooking.getCheckIn(),
                selectedBooking.getCheckOut(),
                scheduleTableModel.getRows()
        );

        detail.setLocationRelativeTo(this);
        detail.setVisible(true);
    }

    public void closeAfterSuccess() {
        dispose();
        if (parent != null) {
            parent.loadData();
        }
    }

    private OdrInfoDTO getSelectedBooking() {
        Object selected = cbxBookings.getSelectedItem();
        return selected instanceof OdrInfoDTO odr ? odr : null;
    }

    private RoomDTO getSelectedRoom() {
        Object selected = cbxNewRoom.getSelectedItem();
        return selected instanceof RoomDTO room ? room : null;
    }

    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return SocketSessionManager.send(BaseRequest.of(commandType, data));
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

    private JPanel cardPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        return panel;
    }

    private JLabel sectionTitle(String text) {
        JLabel lb = new JLabel(text);
        lb.setForeground(PRIMARY);
        lb.setFont(new Font("Segoe UI", Font.BOLD, 18));
        return lb;
    }

    private JLabel fieldLabel(String text) {
        JLabel lb = new JLabel(text + ":");
        lb.setForeground(FG);
        lb.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return lb;
    }

    private JLabel readOnlyBox(String text) {
        JLabel lb = new JLabel(text);
        lb.setOpaque(true);
        lb.setBackground(CARD_BG_2);
        lb.setForeground(FG);
        lb.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        return lb;
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

    private void styleCombo(JComboBox<?> combo) {
        combo.setBackground(CARD_BG_2);
        combo.setForeground(FG);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBorder(BorderFactory.createLineBorder(BORDER));
    }

    private void styleTable(JTable table) {
        table.setRowHeight(38);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(FG);
        table.setBackground(CARD_BG);
        table.setSelectionBackground(new Color(0x1D4F7A));
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(BORDER);
        table.setShowVerticalLines(false);

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setForeground(PRIMARY);
        table.getTableHeader().setBackground(new Color(0x0E2A44));
        table.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setBackground(CARD_BG);
        renderer.setForeground(FG);
        renderer.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    private JButton button(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(bg.darker()));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (bg.equals(PRIMARY)) btn.setBackground(PRIMARY_HOVER);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(bg);
            }
        });

        return btn;
    }

    private boolean isBusyStatus(String status) {
        if (status == null) return false;
        String s = status.trim();
        return s.equalsIgnoreCase("Đặt") || s.equalsIgnoreCase("Check-in");
    }

    private String safe(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    private String safeRaw(String value) {
        return value == null ? "" : value.trim();
    }

    private String format(LocalDateTime time) {
        return time == null ? "-" : time.format(dtf);
    }

    private class BookingRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof OdrInfoDTO odr) {
                setText(
                        safe(odr.getFullName())
                                + " - " + safe(odr.getPhone())
                                + " | " + format(odr.getCheckIn())
                                + " → " + format(odr.getCheckOut())
                                + " | " + safe(odr.getOrderDetailRoomId())
                );
            }

            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            return this;
        }
    }

    private class RoomRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof RoomDTO room) {
                setText(
                        safe(room.getRoomId())
                                + " - " + safe(room.getDescription())
                                + " - " + safe(room.getRoomTypeName())
                                + " - View: " + safe(room.getView())
                );
            }

            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            return this;
        }
    }

    private class TargetRoomScheduleTableModel extends AbstractTableModel {
        private final String[] columns = {"Trạng thái", "Khách hàng", "SĐT", "Check-in", "Check-out", "Kiểu"};
        private final List<RoomCalendarSlotDTO> rows = new ArrayList<>();

        public void setRows(List<RoomCalendarSlotDTO> data) {
            rows.clear();
            if (data != null) rows.addAll(data);
            fireTableDataChanged();
        }

        public List<RoomCalendarSlotDTO> getRows() {
            return new ArrayList<>(rows);
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
                case 2 -> safe(s.getPhone());
                case 3 -> format(s.getCheckIn());
                case 4 -> format(s.getCheckOut());
                case 5 -> safe(s.getBookingType());
                default -> "";
            };
        }
    }
}