package client.presentation.roomBooking;

import client.network.socket.HotelClient;
import client.network.socket.SocketRequestExecutor;
import client.network.socket.SocketSessionManager;
import client.presentation.login.main.Application;
import common.dto.OdrInfoDTO;
import common.dto.RoomDTO;
import common.dto.request_dto.CalculateRoomFeeWithNewRoomRequestDTO;
import common.dto.request_dto.ChangeRoomBeforeCheckInRequestDTO;
import common.protocol.command.CommandType;
import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class FormChangeRoomDetail extends JDialog {

    private final Color BG = new Color(0x0B1F33);
    private final Color FG = new Color(0xE9EEF6);
    private final Color CARD_BG = new Color(0x102D4A);
    private final Color BORDER = new Color(0x274A6B);
    private final Color PRIMARY = new Color(0xF5C452);

    private final String oldRoomID;
    private final RoomDTO newRoom;
    private final OdrInfoDTO infoOldOdr;
    private final FormChangeRoom parent;

    private JSpinner spinCheckInDate, spinCheckInHour, spinCheckInMin;
    private JSpinner spinCheckOutDate, spinCheckOutHour, spinCheckOutMin;
    private JLabel lblNewPrice;

    private final DateTimeFormatter DT_DATE_TIME = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public FormChangeRoomDetail(FormChangeRoom parent,
                                String oldRoomID,
                                RoomDTO newRoom,
                                OdrInfoDTO infoOldOdr,
                                LocalDateTime defaultCheckIn,
                                LocalDateTime defaultCheckOut) {
        super(parent, "Xác nhận đổi phòng", true);
        this.parent = parent;
        this.oldRoomID = oldRoomID;
        this.newRoom = newRoom;
        this.infoOldOdr = infoOldOdr;

        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());
        setResizable(true);

        Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int) (scr.width * 0.70), (int) (scr.height * 0.70));
        setMinimumSize(new Dimension(900, 620));

        JLabel lblTitle = new JLabel("XÁC NHẬN ĐỔI PHÒNG", SwingConstants.CENTER);
        lblTitle.setForeground(FG);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
        add(lblTitle, BorderLayout.NORTH);

        JPanel pnBody = new JPanel(new MigLayout("insets 16, gap 14", "[180!,right]10[fill,grow]", ""));
        pnBody.setBackground(BG);

        JScrollPane scrollPaneBody = new JScrollPane(pnBody);
        scrollPaneBody.setBorder(BorderFactory.createEmptyBorder());
        scrollPaneBody.getVerticalScrollBar().setUnitIncrement(18);
        add(scrollPaneBody, BorderLayout.CENTER);

        CompoundBorder sectionBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 2),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        );

        JLabel lblInfoOld = sectionTitle("Thông tin hiện tại");
        JPanel oldCard = new JPanel(new MigLayout("wrap 2, gapx 12, insets 8 10 8 10", "[right]8[fill,grow]"));
        oldCard.setBackground(CARD_BG);
        oldCard.setBorder(sectionBorder);

        addKvp(oldCard, "Phòng cũ:", oldRoomID);
        addKvp(oldCard, "Giá phòng cũ:", valueLabel(formatMoney(infoOldOdr.getRoomFee())));
        addKvp(oldCard, "Check-in cũ:", valueLabel(formatLdt(infoOldOdr.getCheckIn())));
        addKvp(oldCard, "Check-out cũ:", valueLabel(formatLdt(infoOldOdr.getCheckOut())));
        pnBody.add(lblInfoOld, "span 2, wrap");
        pnBody.add(oldCard, "span 2, growx, wrap");

        JLabel lblInfoNew = sectionTitle("Thiết lập phòng mới");
        JPanel newCard = new JPanel(new MigLayout("wrap 2, gapx 12, insets 8 10 8 10", "[right]8[fill,grow]"));
        newCard.setBackground(CARD_BG);
        newCard.setBorder(sectionBorder);

        addKvp(newCard, "Phòng mới:", newRoom.getRoomId() + " – " + safe(newRoom.getDescription()));
        addKvp(newCard, "Booking type:", safe(infoOldOdr.getBookingType()));
        addKvp(newCard, "Loại phòng mới:", safe(newRoom.getRoomTypeName()));

        LocalDateTime checkInTime = defaultCheckIn == null ? LocalDateTime.now() : defaultCheckIn;
        LocalDateTime checkOutTime = defaultCheckOut == null ? checkInTime.plusHours(2) : defaultCheckOut;

        spinCheckInDate = new JSpinner(new SpinnerDateModel(java.sql.Date.valueOf(checkInTime.toLocalDate()), null, null, java.util.Calendar.DAY_OF_MONTH));
        spinCheckInHour = new JSpinner(new SpinnerNumberModel(checkInTime.getHour(), 0, 23, 1));
        spinCheckInMin = new JSpinner(new SpinnerNumberModel(checkInTime.getMinute(), 0, 59, 5));

        spinCheckOutDate = new JSpinner(new SpinnerDateModel(java.sql.Date.valueOf(checkOutTime.toLocalDate()), null, null, java.util.Calendar.DAY_OF_MONTH));
        spinCheckOutHour = new JSpinner(new SpinnerNumberModel(checkOutTime.getHour(), 0, 23, 1));
        spinCheckOutMin = new JSpinner(new SpinnerNumberModel(checkOutTime.getMinute(), 0, 59, 5));

        spinCheckInDate.setEditor(new JSpinner.DateEditor(spinCheckInDate, "dd/MM/yyyy"));
        spinCheckOutDate.setEditor(new JSpinner.DateEditor(spinCheckOutDate, "dd/MM/yyyy"));

        styleSpinner(spinCheckInDate);
        styleSpinner(spinCheckInHour);
        styleSpinner(spinCheckInMin);
        styleSpinner(spinCheckOutDate);
        styleSpinner(spinCheckOutHour);
        styleSpinner(spinCheckOutMin);

        addKvp(newCard, "Check-in mới:", rowTime(spinCheckInDate, spinCheckInHour, spinCheckInMin));
        addKvp(newCard, "Check-out mới:", rowTime(spinCheckOutDate, spinCheckOutHour, spinCheckOutMin));

        lblNewPrice = valueLabel("…");
        addKvp(newCard, "Giá phòng mới (ước tính):", lblNewPrice);

        pnBody.add(lblInfoNew, "span 2, wrap");
        pnBody.add(newCard, "span 2, growx, wrap");

        JPanel pnFooter = new JPanel(new MigLayout("insets 10 16 16 16", "[grow]push[]10[]", "[]"));
        pnFooter.setBackground(BG);

        JButton btnPreview = primaryButton("Tính giá");
        btnPreview.addActionListener(e -> updateNewPriceOfNewRoom());

        JButton btnConfirm = primaryButton("Xác nhận đổi phòng");
        btnConfirm.addActionListener(e -> confirmChangeRoom());

        pnFooter.add(new JLabel(), "grow");
        pnFooter.add(btnPreview);
        pnFooter.add(btnConfirm);
        add(pnFooter, BorderLayout.SOUTH);

        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);

        updateNewPriceOfNewRoom();
    }

    private void updateNewPriceOfNewRoom() {
        LocalDateTime checkIn = getCheckIn();
        LocalDateTime checkOut = getCheckOut();
        if (checkOut == null || checkIn == null || !checkOut.isAfter(checkIn)) {
            lblNewPrice.setText("Thời gian không hợp lệ");
            return;
        }

        try {
            BaseResponse response = sendRequest(
                    CommandType.CALCULATE_ROOM_FEE_WITH_NEW_ROOM,
                    new CalculateRoomFeeWithNewRoomRequestDTO(
                            newRoom.getRoomId(),
                            infoOldOdr.getBookingType(),
                            checkIn,
                            checkOut
                    )
            );

            if (!response.isSuccess()) {
                lblNewPrice.setText(response.getMessage());
                return;
            }

            Double roomFee = (Double) response.getData();
            lblNewPrice.setText(roomFee == null ? "(Không tính được)" : formatMoney(roomFee));
        } catch (Exception ex) {
            lblNewPrice.setText(ex.getMessage());
        }
    }

    private void confirmChangeRoom() {
        LocalDateTime checkIn = getCheckIn();
        LocalDateTime checkOut = getCheckOut();

        if (checkOut == null || checkIn == null || !checkOut.isAfter(checkIn)) {
            JOptionPane.showMessageDialog(this, "Check-out phải sau check-in");
            return;
        }

        try {
            BaseResponse response = sendRequest(
                    CommandType.CHANGE_ROOM_BEFORE_CHECKIN,
                    new ChangeRoomBeforeCheckInRequestDTO(
                            oldRoomID,
                            newRoom.getRoomId(),
                            checkIn,
                            checkOut
                    )
            );

            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Đổi phòng thành công!");
                dispose();
                parent.closeAfterSuccess();
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return SocketSessionManager.send(BaseRequest.of(commandType, data));
    }

    private LocalDateTime getCheckIn() {
        LocalDate d = new java.sql.Date(((java.util.Date) spinCheckInDate.getValue()).getTime()).toLocalDate();
        int h = (int) spinCheckInHour.getValue();
        int m = (int) spinCheckInMin.getValue();
        return LocalDateTime.of(d, LocalTime.of(h, m));
    }

    private LocalDateTime getCheckOut() {
        LocalDate d = new java.sql.Date(((java.util.Date) spinCheckOutDate.getValue()).getTime()).toLocalDate();
        int h = (int) spinCheckOutHour.getValue();
        int m = (int) spinCheckOutMin.getValue();
        return LocalDateTime.of(d, LocalTime.of(h, m));
    }

    private JLabel sectionTitle(String s) {
        JLabel l = new JLabel(s);
        l.setForeground(PRIMARY);
        l.setFont(new Font("Segoe UI", Font.BOLD, 16));
        return l;
    }

    private JLabel valueLabel(String s) {
        JLabel l = new JLabel(s);
        l.setForeground(FG);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return l;
    }

    private void addKvp(JPanel p, String key, Object valueComp) {
        JLabel k = new JLabel(key);
        k.setForeground(new Color(0xB8C4D4));
        p.add(k);
        if (valueComp instanceof Component c) p.add(c, "growx");
        else p.add(valueLabel(String.valueOf(valueComp)), "growx");
    }

    private JPanel rowTime(JSpinner date, JSpinner hour, JSpinner min) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        p.add(date);
        p.add(new JLabel("Giờ:"));
        p.add(hour);
        p.add(new JLabel("Phút:"));
        p.add(min);
        return p;
    }

    private void styleSpinner(JSpinner sp) {
        sp.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sp.setPreferredSize(new Dimension(100, 28));
    }

    private JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(PRIMARY);
        b.setForeground(new Color(0x0B1F33));
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return b;
    }

    private String formatMoney(double money) {
        return String.format("%,.0f VNĐ", money);
    }

    private String formatLdt(LocalDateTime ldt) {
        return ldt == null ? "-" : DT_DATE_TIME.format(ldt);
    }

    private String safe(String s) {
        return s == null ? "-" : s;
    }
}