package client.presentation.roomBooking;

import client.network.socket.HotelClient;
import client.network.socket.SocketRequestExecutor;
import client.network.socket.SocketSessionManager;
import client.presentation.login.main.Application;
import com.raven.datechooser.DateChooser;
import common.dto.request_dto.ExtendRoomRequestDTO;
import common.protocol.command.CommandType;
import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class FormExtendRoom extends JDialog {

    private static final Color BG = new Color(0x0B1F33);
    private static final Color FG = new Color(0xE9EEF6);
    private static final Color CARD_BG = new Color(0x102D4A);
    private static final Color BORDER = new Color(0x274A6B);
    private static final Color PRIMARY = new Color(0xF5C452);

    private final String roomID;
    private final FormRoomBookingManagement parent;

    private JTextField txtRoomID;
    private JTextField txtNewCheckOutDate;
    private JSpinner spNewCheckOutTime;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public FormExtendRoom(String roomID, server.core.service.RoomStayService roomStayService, FormRoomBookingManagement parent) {
        this.roomID = roomID;
        this.parent = parent;
        initUI();
    }

    private void initUI() {
        setTitle("Gia hạn phòng");
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new MigLayout("wrap 2, insets 16, gap 10", "[right]15[320!,grow]"));
        root.setBackground(BG);

        txtRoomID = text();
        txtRoomID.setEditable(false);
        txtRoomID.setText(roomID);

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
        setSize(560, getPreferredSize().height + 20);
        setLocationRelativeTo(parent);
    }

    private void extendRoom() {
        try {
            LocalDate d = LocalDate.parse(txtNewCheckOutDate.getText().trim(), dateFormatter);
            LocalTime t = readTime(spNewCheckOutTime);
            LocalDateTime newCheckOut = LocalDateTime.of(d, t);

            BaseResponse response = sendRequest(
                    CommandType.EXTEND_ROOM,
                    new ExtendRoomRequestDTO(roomID, newCheckOut)
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
        return date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
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
}