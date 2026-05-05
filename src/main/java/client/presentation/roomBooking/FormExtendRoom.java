package client.presentation.roomBooking;

import client.network.socket.SocketSessionManager;
import com.raven.datechooser.DateChooser;
import common.dto.OdrInfoDTO;
import common.dto.request_dto.ExtendRoomRequestDTO;
import common.dto.request_dto.RoomIdRequestDTO;
import common.protocol.command.CommandType;
import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class FormExtendRoom extends JDialog {

    private static final Color BG = new Color(0x0B1F33);
    private static final Color FG = new Color(0xE9EEF6);
    private static final Color CARD_BG = new Color(0x102D4A);
    private static final Color BORDER = new Color(0x274A6B);
    private static final Color PRIMARY = new Color(0xF5C452);

    private final String roomID;
    private final FormRoomBookingManagement parent;

    private JTextField txtRoomID;
    private JComboBox<OdrInfoDTO> cboBookings;
    private JTextField txtCurrentCheckIn;
    private JTextField txtCurrentCheckOut;
    private JTextField txtNewCheckOutDate;
    private JSpinner spNewCheckOutTime;

    private List<OdrInfoDTO> bookings = Collections.emptyList();

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public FormExtendRoom(String roomID, server.core.service.RoomStayService roomStayService, FormRoomBookingManagement parent) {
        this.roomID = roomID;
        this.parent = parent;
        initUI();
        loadBookings();
    }

    private void initUI() {
        setTitle("Gia hạn phòng");
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new MigLayout("wrap 2, insets 16, gap 10", "[right]15[420!,grow]"));
        root.setBackground(BG);

        txtRoomID = text();
        txtRoomID.setEditable(false);
        txtRoomID.setText(roomID);

        cboBookings = new JComboBox<>();
        cboBookings.setBackground(CARD_BG);
        cboBookings.setForeground(FG);
        cboBookings.setRenderer(new BookingRenderer());
        cboBookings.addActionListener(e -> fillSelectedBookingInfo());

        txtCurrentCheckIn = text();
        txtCurrentCheckIn.setEditable(false);

        txtCurrentCheckOut = text();
        txtCurrentCheckOut.setEditable(false);

        txtNewCheckOutDate = text();
        txtNewCheckOutDate.setEditable(false);
        txtNewCheckOutDate.setText(LocalDate.now().format(dateFormatter));

        DateChooser chooser = new DateChooser();
        chooser.setTextRefernce(txtNewCheckOutDate);

        JButton btnCalendar = new JButton("📅");
        btnCalendar.addActionListener(e -> chooser.showPopup());

        spNewCheckOutTime = timeSpinner();

        JButton btnSave = primaryButton("Gia hạn");
        JButton btnClose = secondaryButton("Đóng");

        root.add(label("Room ID:"));
        root.add(txtRoomID, "growx");

        root.add(label("Chọn khách/booking:"));
        root.add(cboBookings, "growx");

        root.add(label("Check-in hiện tại:"));
        root.add(txtCurrentCheckIn, "growx");

        root.add(label("Check-out hiện tại:"));
        root.add(txtCurrentCheckOut, "growx");

        root.add(label("Ngày check-out mới:"));
        root.add(wrapDateTime(txtNewCheckOutDate, btnCalendar, spNewCheckOutTime), "growx");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(btnClose);
        actions.add(btnSave);

        root.add(actions, "span 2, growx");

        btnClose.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> extendRoom());

        setContentPane(root);
        pack();
        setSize(680, getPreferredSize().height + 30);
        setLocationRelativeTo(parent);
    }

    private void loadBookings() {
        try {
            DefaultComboBoxModel<OdrInfoDTO> model = new DefaultComboBoxModel<>();

            BaseResponse pendingRes = sendRequest(
                    CommandType.GET_PENDING_BOOKINGS_OF_ROOM,
                    new RoomIdRequestDTO(roomID)
            );

            if (pendingRes.isSuccess() && pendingRes.getData() instanceof List<?> rawList) {
                for (Object obj : rawList) {
                    if (obj instanceof OdrInfoDTO odr) {
                        model.addElement(odr);
                    }
                }
            }

            BaseResponse activeRes = sendRequest(
                    CommandType.GET_ACTIVE_CHECKIN_INFO,
                    new RoomIdRequestDTO(roomID)
            );

            if (activeRes.isSuccess() && activeRes.getData() instanceof OdrInfoDTO active) {
                model.insertElementAt(active, 0);
            }

            cboBookings.setModel(model);

            if (model.getSize() == 0) {
                JOptionPane.showMessageDialog(this, "Phòng này không có booking Đặt/Check-in để gia hạn.");
                dispose();
                return;
            }

            cboBookings.setSelectedIndex(0);
            fillSelectedBookingInfo();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    private void fillSelectedBookingInfo() {
        OdrInfoDTO odr = getSelectedOdr();
        if (odr == null) {
            txtCurrentCheckIn.setText("");
            txtCurrentCheckOut.setText("");
            return;
        }

        txtCurrentCheckIn.setText(odr.getCheckIn() == null ? "-" : displayFormatter.format(odr.getCheckIn()));
        txtCurrentCheckOut.setText(odr.getCheckOut() == null ? "-" : displayFormatter.format(odr.getCheckOut()));

        if (odr.getCheckOut() != null) {
            txtNewCheckOutDate.setText(odr.getCheckOut().toLocalDate().format(dateFormatter));
            spNewCheckOutTime.setValue(
                    java.util.Date.from(
                            odr.getCheckOut()
                                    .toLocalTime()
                                    .atDate(LocalDate.of(1970, 1, 1))
                                    .atZone(ZoneId.systemDefault())
                                    .toInstant()
                    )
            );
        }
    }

    private void extendRoom() {
        try {
            OdrInfoDTO selected = getSelectedOdr();
            if (selected == null || selected.getOrderDetailRoomId() == null || selected.getOrderDetailRoomId().isBlank()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn booking cần gia hạn.");
                return;
            }

            LocalDate d = LocalDate.parse(txtNewCheckOutDate.getText().trim(), dateFormatter);
            LocalTime t = readTime(spNewCheckOutTime);
            LocalDateTime newCheckOut = LocalDateTime.of(d, t);

            if (selected.getCheckIn() != null && !newCheckOut.isAfter(selected.getCheckIn())) {
                JOptionPane.showMessageDialog(this, "Check-out mới phải sau check-in.");
                return;
            }

            if (selected.getCheckOut() != null && !newCheckOut.isAfter(selected.getCheckOut())) {
                JOptionPane.showMessageDialog(this, "Check-out mới phải sau check-out hiện tại.");
                return;
            }

            BaseResponse response = sendRequest(
                    CommandType.EXTEND_ROOM,
                    new ExtendRoomRequestDTO(roomID, selected.getOrderDetailRoomId(), newCheckOut)
            );

            JOptionPane.showMessageDialog(
                    this,
                    response.isSuccess() ? "Gia hạn phòng thành công." : response.getMessage()
            );

            if (response.isSuccess()) {
                dispose();
                if (parent != null) parent.loadData();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private OdrInfoDTO getSelectedOdr() {
        Object selected = cboBookings.getSelectedItem();
        return selected instanceof OdrInfoDTO odr ? odr : null;
    }

    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return SocketSessionManager.send(BaseRequest.of(commandType, data));
    }

    private JLabel label(String s) {
        JLabel lb = new JLabel(s);
        lb.setForeground(FG);
        lb.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return lb;
    }

    private JTextField text() {
        JTextField t = new JTextField();
        t.setBackground(CARD_BG);
        t.setForeground(FG);
        t.setCaretColor(FG);
        t.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        return t;
    }

    private JSpinner timeSpinner() {
        SpinnerDateModel model = new SpinnerDateModel();
        JSpinner sp = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(sp, "HH:mm");
        sp.setEditor(editor);
        sp.setValue(java.sql.Time.valueOf(LocalTime.of(12, 0)));
        return sp;
    }

    private LocalTime readTime(JSpinner sp) {
        java.util.Date date = (java.util.Date) sp.getValue();
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalTime()
                .withSecond(0)
                .withNano(0);
    }

    private JPanel wrapDateTime(JTextField txtDate, JButton btnDate, JSpinner time) {
        JPanel p = new JPanel(new MigLayout("insets 0, fillx", "[grow][]8[90!]", "[]"));
        p.setOpaque(false);
        p.add(txtDate, "growx");
        p.add(btnDate, "w 42!");
        p.add(time, "growx");
        return p;
    }

    private JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(PRIMARY);
        b.setForeground(new Color(0x0B1F33));
        b.setFocusPainted(false);
        return b;
    }

    private JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(0x153C5B));
        b.setForeground(FG);
        b.setFocusPainted(false);
        return b;
    }

    private class BookingRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof OdrInfoDTO odr) {
                String customer = odr.getFullName() == null ? "Không rõ khách" : odr.getFullName();
                String phone = odr.getPhone() == null ? "" : " - " + odr.getPhone();
                String in = odr.getCheckIn() == null ? "-" : displayFormatter.format(odr.getCheckIn());
                String out = odr.getCheckOut() == null ? "-" : displayFormatter.format(odr.getCheckOut());

                setText(customer + phone + " | " + in + " → " + out + " | " + odr.getOrderDetailRoomId());
            }

            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            return this;
        }
    }
}