package client.presentation.roomBooking;

import client.network.socket.HotelClient;
import client.network.socket.SocketRequestExecutor;
import client.network.socket.SocketSessionManager;
import client.presentation.login.main.Application;
import common.dto.OdrInfoDTO;
import common.dto.RoomDTO;
import common.dto.request_dto.CalculateRoomFeeRequestDTO;
import common.dto.request_dto.ChangeRoomWhileCheckInRequestDTO;
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

public class FormChangeRoomWhileCheckInDetail extends JDialog {

    private final Color BG = new Color(0x0B1F33);
    private final Color FG = new Color(0xE9EEF6);
    private final Color CARD_BG = new Color(0x102D4A);
    private final Color BORDER = new Color(0x274A6B);
    private final Color PRIMARY = new Color(0xF5C452);
    private final Color DANGER = new Color(0xF26B6B);

    private final String oldRoomID;
    private final RoomDTO newRoom;
    private final OdrInfoDTO odrInfoCheckIn;
    private final FormChangeRoomWhileCheckIn parent;

    private JLabel lblOldPriceSeg, lblNewPriceSeg, lblOldCheckIn, lblOldCheckOut, lblCheckOutRoomNew, lblChangeTime;
    private JLabel lblHint;

    private LocalDateTime checkInOld, checkOutOld;
    private LocalDateTime changeTimeSelected;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public FormChangeRoomWhileCheckInDetail(FormChangeRoomWhileCheckIn parent,
                                            String oldRoomID,
                                            RoomDTO newRoom,
                                            OdrInfoDTO odrInfoCheckIn) {
        super(parent, "Xác nhận đổi phòng (đang ở)", true);
        this.parent = parent;
        this.oldRoomID = oldRoomID;
        this.newRoom = newRoom;
        this.odrInfoCheckIn = odrInfoCheckIn;
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());
        setResizable(true);

        Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int) (scr.width * 0.70), (int) (scr.height * 0.72));
        setMinimumSize(new Dimension(900, 620));

        checkInOld = odrInfoCheckIn.getCheckIn() == null ? LocalDateTime.now().minusMinutes(5) : odrInfoCheckIn.getCheckIn();
        checkOutOld = odrInfoCheckIn.getCheckOut() == null ? checkInOld.plusHours(2) : odrInfoCheckIn.getCheckOut();

        changeTimeSelected =
                (LocalDateTime.now().isAfter(checkInOld) && LocalDateTime.now().isBefore(checkOutOld))
                        ? LocalDateTime.now()
                        : checkInOld.plusMinutes(1);

        JLabel lblTitle = new JLabel("XÁC NHẬN ĐỔI PHÒNG (ĐÃ CHECK-IN)", SwingConstants.CENTER);
        lblTitle.setForeground(FG);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
        add(lblTitle, BorderLayout.NORTH);

        JPanel pnBody = new JPanel(new MigLayout("insets 16 18 16 18, gap 14", "[180!,left]10[fill,grow]", ""));
        pnBody.setBackground(BG);

        JScrollPane scrollPane = new JScrollPane(pnBody);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        add(scrollPane, BorderLayout.CENTER);

        CompoundBorder sectionBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 2),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        );

        JLabel lblInfoOldRoom = sectionTitle("Thông tin hiện tại (phòng cũ)");
        JPanel pnCardOldRoom = new JPanel(new MigLayout("wrap 2, gapx 12, insets 8 10 8 10", "[right]8[fill,grow]"));
        pnCardOldRoom.setBackground(CARD_BG);
        pnCardOldRoom.setBorder(sectionBorder);

        addKvp(pnCardOldRoom, "Phòng cũ:", oldRoomID);
        addKvp(pnCardOldRoom, "Booking type:", safe(odrInfoCheckIn.getBookingType()));

        lblOldCheckIn = valueLabel(checkInOld.format(formatter));
        lblOldCheckOut = valueLabel(checkOutOld.format(formatter));

        addKvp(pnCardOldRoom, "Check-in cũ:", lblOldCheckIn);
        addKvp(pnCardOldRoom, "Check-out cũ (tự cập nhật = thời điểm chuyển):", lblOldCheckOut);

        lblOldPriceSeg = valueLabel("…");
        addKvp(pnCardOldRoom, "Giá phòng cũ (Check-in đến thời điểm chuyển):", lblOldPriceSeg);

        pnBody.add(lblInfoOldRoom, "span 2, wrap");
        pnBody.add(pnCardOldRoom, "span 2, growx, wrap");

        JLabel lblInfoNewRoom = sectionTitle("Thiết lập phòng mới");
        JPanel pnCardNewRoom = new JPanel(new MigLayout("wrap 2, gapx 12, insets 8 10 8 10", "[right]8[fill,grow]"));
        pnCardNewRoom.setBackground(CARD_BG);
        pnCardNewRoom.setBorder(sectionBorder);

        addKvp(pnCardNewRoom, "Phòng mới:", newRoom.getRoomId() + " – " + safe(newRoom.getDescription()));
        addKvp(pnCardNewRoom, "Loại phòng mới:", safe(newRoom.getRoomTypeName()));
        addKvp(pnCardNewRoom, "Booking type:", safe(odrInfoCheckIn.getBookingType()));

        lblChangeTime = valueLabel(changeTimeSelected.format(formatter));
        JButton btnPick = primaryButton("Chọn thời điểm chuyển");
        btnPick.addActionListener(e -> {
            LocalDateTime min = checkInOld.plusMinutes(1);
            LocalDateTime max = checkOutOld.minusMinutes(1);
            LocalDateTime picked = pickChangeTime(min, max, changeTimeSelected);
            if (picked != null) {
                changeTimeSelected = picked;
                lblChangeTime.setText(changeTimeSelected.format(formatter));
                lblOldCheckOut.setText(changeTimeSelected.format(formatter));
                recalculateRoomFee();
            }
        });
        addKvp(pnCardNewRoom, "Thời điểm chuyển:", wrapInline(lblChangeTime, btnPick));

        lblCheckOutRoomNew = valueLabel(checkOutOld.format(formatter));
        addKvp(pnCardNewRoom, "Check-out (không đổi):", lblCheckOutRoomNew);

        lblNewPriceSeg = valueLabel("…");
        addKvp(pnCardNewRoom, "Giá phòng mới (từ thời điểm chuyển đến Check-out):", lblNewPriceSeg);

        pnBody.add(lblInfoNewRoom, "span 2, wrap");
        pnBody.add(pnCardNewRoom, "span 2, growx, wrap");

        lblHint = new JLabel(" ");
        lblHint.setForeground(DANGER);
        pnBody.add(lblHint, "span 2, growx");

        JPanel pnFooter = new JPanel(new MigLayout("insets 10 16 16 28", "[grow]push[]", "[]"));
        pnFooter.setBackground(BG);

        JButton btnConfirm = primaryButton("Xác nhận đổi phòng");
        btnConfirm.addActionListener(e -> confirmChangeRoomWhileCheckIn());

        pnFooter.add(new JLabel(), "grow");
        pnFooter.add(btnConfirm);
        add(pnFooter, BorderLayout.SOUTH);

        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);

        recalculateRoomFee();
    }

    private void confirmChangeRoomWhileCheckIn() {
        if (!(changeTimeSelected.isAfter(checkInOld) && changeTimeSelected.isBefore(checkOutOld))) {
            JOptionPane.showMessageDialog(this, "Thời điểm chuyển phải sau check-in và trước check-out hiện tại.");
            return;
        }

        try {
            BaseResponse response = sendRequest(
                    CommandType.CHANGE_ROOM_WHILE_CHECKIN,
                    new ChangeRoomWhileCheckInRequestDTO(
                            oldRoomID,
                            newRoom.getRoomId(),
                            changeTimeSelected
                    )
            );

            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this,
                        "Lên lịch đổi phòng thành công!\n"
                                + "• Phòng cũ vẫn Check-in đến: " + changeTimeSelected.format(formatter) + "\n"
                                + "• Phòng mới được đặt từ: " + changeTimeSelected.format(formatter) + "\n"
                                + "• Phòng mới: " + newRoom.getRoomId() + "\n"
                                + "• Check-out cuối: " + checkOutOld.format(formatter) + "\n\n"
                                + "Đến thời điểm chuyển, hãy check-out phòng cũ và check-in phòng mới.");
                dispose();
                parent.closeAfterSuccess();
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void recalculateRoomFee() {
        lblHint.setText(" ");
        if (!(changeTimeSelected.isAfter(checkInOld) && changeTimeSelected.isBefore(checkOutOld))) {
            lblHint.setText("Thời điểm chuyển phải sau check-in và trước check-out hiện tại.");
            lblOldPriceSeg.setText("Thời gian không hợp lệ");
            lblNewPriceSeg.setText("Thời gian không hợp lệ");
            return;
        }

        try {
            BaseResponse oldRes = sendRequest(
                    CommandType.CALCULATE_ROOM_FEE,
                    new CalculateRoomFeeRequestDTO(
                            oldRoomID,
                            odrInfoCheckIn.getBookingType(),
                            checkInOld,
                            changeTimeSelected
                    )
            );

            if (oldRes.isSuccess()) {
                Double oldSeg = (Double) oldRes.getData();
                lblOldPriceSeg.setText(oldSeg == null ? "(…)" : formatMoney(oldSeg));
            } else {
                lblOldPriceSeg.setText(oldRes.getMessage());
            }

            BaseResponse newRes = sendRequest(
                    CommandType.CALCULATE_ROOM_FEE,
                    new CalculateRoomFeeRequestDTO(
                            newRoom.getRoomId(),
                            odrInfoCheckIn.getBookingType(),
                            changeTimeSelected,
                            checkOutOld
                    )
            );

            if (newRes.isSuccess()) {
                Double newSeg = (Double) newRes.getData();
                lblNewPriceSeg.setText(newSeg == null ? "(…)" : formatMoney(newSeg));
            } else {
                lblNewPriceSeg.setText(newRes.getMessage());
            }
        } catch (Exception ex) {
            lblHint.setText(ex.getMessage());
        }
    }

    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return SocketSessionManager.send(BaseRequest.of(commandType, data));
    }

    private LocalDateTime pickChangeTime(LocalDateTime minTime, LocalDateTime maxTime, LocalDateTime current) {
        boolean wasVisible = isVisible();
        if (wasVisible) setVisible(false);

        JDialog d = new JDialog(getOwner(), "Chọn thời điểm chuyển", Dialog.ModalityType.APPLICATION_MODAL);
        d.getContentPane().setBackground(BG);
        d.setLayout(new MigLayout("insets 14 18 14 18", "[right]10[fill,grow,300!]", "[][][]push[]"));
        d.setMinimumSize(new Dimension(700, 230));
        d.setResizable(false);

        JLabel lbMin = valueLabel(minTime.format(formatter));
        JLabel lbMax = valueLabel(maxTime.format(formatter));

        JSpinner spDate = new JSpinner(new SpinnerDateModel(java.sql.Date.valueOf(current.toLocalDate()), null, null, java.util.Calendar.DAY_OF_MONTH));
        spDate.setEditor(new JSpinner.DateEditor(spDate, "dd/MM/yyyy"));
        JSpinner spHour = new JSpinner(new SpinnerNumberModel(current.getHour(), 0, 23, 1));
        JSpinner spMin = new JSpinner(new SpinnerNumberModel(current.getMinute(), 0, 59, 1));

        styleSpinner(spDate);
        styleSpinner(spHour);
        styleSpinner(spMin);

        d.add(textMuted("Sớm nhất:")); d.add(lbMin, "wrap");
        d.add(textMuted("Muộn nhất:")); d.add(lbMax, "wrap");
        d.add(textMuted("Thời điểm chuyển:")); d.add(rowTime(spDate, spHour, spMin), "wrap");

        LocalDateTime[] result = new LocalDateTime[1];

        JButton btnOk = primaryButton("OK");
        btnOk.addActionListener(e -> {
            LocalDate date = new java.sql.Date(((java.util.Date) spDate.getValue()).getTime()).toLocalDate();
            LocalDateTime selected = LocalDateTime.of(date, LocalTime.of((int) spHour.getValue(), (int) spMin.getValue()));
            if (!selected.isAfter(minTime.minusNanos(1)) || !selected.isBefore(maxTime.plusNanos(1))) {
                JOptionPane.showMessageDialog(d, "Thời điểm chuyển phải nằm trong khoảng hợp lệ.");
                return;
            }
            result[0] = selected;
            d.dispose();
        });

        JButton btnCancel = new JButton("Hủy");
        btnCancel.addActionListener(e -> d.dispose());

        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.setOpaque(false);
        p.add(btnCancel);
        p.add(btnOk);
        d.add(p, "span 2, growx");

        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);

        if (isDisplayable()) SwingUtilities.invokeLater(() -> setVisible(true));
        return result[0];
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

    private JLabel textMuted(String s) {
        JLabel l = new JLabel(s);
        l.setForeground(new Color(0xB8C4D4));
        return l;
    }

    private void addKvp(JPanel p, String key, Object valueComp) {
        JLabel k = new JLabel(key);
        k.setForeground(new Color(0xB8C4D4));
        p.add(k);
        if (valueComp instanceof Component c) p.add(c, "growx");
        else p.add(valueLabel(String.valueOf(valueComp)), "growx");
    }

    private JPanel wrapInline(JComponent left, JComponent right) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        p.add(left);
        p.add(right);
        return p;
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

    private String safe(String s) {
        return s == null ? "-" : s;
    }
}