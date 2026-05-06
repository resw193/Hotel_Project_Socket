package client.presentation.roomBooking;

import client.network.socket.SocketSessionManager;
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
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.time.format.DateTimeFormatter;

public class FormChangeRoomWhileCheckInDetail extends JDialog {

    private final Color BG = new Color(0x0B1F33);
    private final Color FG = new Color(0xE9EEF6);
    private final Color CARD_BG = new Color(0x102D4A);
    private final Color PANEL_BG = new Color(0x123657);
    private final Color PANEL_BG_2 = new Color(0x143F66);
    private final Color BORDER = new Color(0x274A6B);
    private final Color PRIMARY = new Color(0xF5C452);
    private final Color MUTED = new Color(0xB8C4D4);
    private final Color SUCCESS = new Color(0x2FC35B);
    private final Color DANGER = new Color(0xF26B6B);
    private final Color BTN_SECONDARY = new Color(0x1B4D78);

    private final String oldRoomID;
    private final RoomDTO newRoom;
    private final OdrInfoDTO odrInfoCheckIn;
    private final FormChangeRoomWhileCheckIn parent;

    private JTextArea txtOldCheckOut;
    private JTextArea txtOldPriceSeg;

    private JTextArea txtChangeTime;
    private JTextArea txtCheckOutRoomNew;
    private JTextArea txtNewPriceSeg;

    private JTextArea txtPreviewOldRoom;
    private JTextArea txtPreviewNewRoom;
    private JTextArea txtPreviewGuest;
    private JTextArea txtPreviewChangeTime;
    private JTextArea txtPreviewOldFee;
    private JTextArea txtPreviewNewFee;

    private JLabel lblStatusText;
    private JTextArea txtNote;

    private LocalDateTime checkInOld;
    private LocalDateTime checkOutOld;
    private LocalDateTime changeTimeSelected;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public FormChangeRoomWhileCheckInDetail(FormChangeRoomWhileCheckIn parent, String oldRoomID, RoomDTO newRoom, OdrInfoDTO odrInfoCheckIn) {
        super(parent, "Xác nhận đổi phòng (đang ở)", true);

        this.parent = parent;
        this.oldRoomID = oldRoomID;
        this.newRoom = newRoom;
        this.odrInfoCheckIn = odrInfoCheckIn;

        this.checkInOld = odrInfoCheckIn.getCheckIn() == null
                ? LocalDateTime.now().minusMinutes(5)
                : odrInfoCheckIn.getCheckIn();

        this.checkOutOld = odrInfoCheckIn.getCheckOut() == null
                ? checkInOld.plusHours(2)
                : odrInfoCheckIn.getCheckOut();

        this.changeTimeSelected =
                (LocalDateTime.now().isAfter(checkInOld) && LocalDateTime.now().isBefore(checkOutOld))
                        ? LocalDateTime.now().withSecond(0).withNano(0)
                        : checkInOld.plusMinutes(1).withSecond(0).withNano(0);

        initUI();
        recalculateRoomFee();
    }

    private void initUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);

        applyLargeDialogSize(0.96, 0.90, 1600, 850);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        setContentPane(root);

        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);

        setLocationRelativeTo(getOwner());
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new MigLayout(
                "insets 20 24 16 24, fillx",
                "[grow,fill]push[][]",
                "[]"
        ));
        header.setBackground(BG);

        JPanel left = new JPanel(new MigLayout("wrap 1, insets 0, gap 0 6", "[grow,fill]"));
        left.setOpaque(false);

        JLabel title = new JLabel("XÁC NHẬN ĐỔI PHÒNG (ĐÃ CHECK-IN)");
        title.setForeground(FG);
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));

        JLabel sub = new JLabel("Kiểm tra phòng cũ, phòng mới, thời điểm chuyển và chi phí trước khi xác nhận.");
        sub.setForeground(MUTED);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        left.add(title);
        left.add(sub);

        header.add(left, "growx");
        header.add(headerBadge("Phòng cũ: " + safe(oldRoomID)), "h 48!");
        header.add(headerBadge("Phòng mới: " + safe(newRoom.getRoomId())), "h 48!");

        return header;
    }

    private JComponent buildCenter() {
        JPanel content = new JPanel(new MigLayout(
                "insets 0 24 18 24, gap 18, fill",
                "[grow,fill][540!,fill]",
                "[grow,fill]"
        ));
        content.setBackground(BG);

        content.add(buildLeftColumn(), "grow");
        content.add(buildRightColumn(), "grow");

        JScrollPane sp = new JScrollPane(content);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.getVerticalScrollBar().setUnitIncrement(18);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(BG);

        return sp;
    }

    private JComponent buildLeftColumn() {
        JPanel col = new JPanel(new MigLayout(
                "wrap 1, insets 0, gap 16, fillx",
                "[grow,fill]",
                "[][][]"
        ));
        col.setOpaque(false);

        col.add(buildOldRoomCard(), "growx");
        col.add(buildNewRoomCard(), "growx");
        col.add(buildTransferCard(), "growx");

        return col;
    }

    private JComponent buildRightColumn() {
        JPanel col = new JPanel(new MigLayout(
                "wrap 1, insets 0, gap 16, fillx, filly",
                "[grow,fill]",
                "[][grow,fill]"
        ));
        col.setOpaque(false);

        col.add(buildPreviewCard(), "growx");
        col.add(buildStatusCard(), "grow");

        return col;
    }

    private JComponent buildOldRoomCard() {
        JPanel card = cardPanel(new MigLayout(
                "wrap 1, insets 18, gap 0 10, fillx",
                "[grow,fill]",
                "[]8[]12[]"
        ));

        card.add(sectionTitle("1. Thông tin hiện tại (phòng cũ)"));
        card.add(mutedLabel("Phòng cũ vẫn ở trạng thái check-in cho đến đúng thời điểm chuyển."));

        JPanel grid = new JPanel(new MigLayout(
                "insets 0, gap 10 10, fillx",
                "[grow,fill][grow,fill][grow,fill]",
                "[][][]"
        ));
        grid.setOpaque(false);

        txtOldCheckOut = infoValue(changeTimeSelected.format(formatter));
        txtOldPriceSeg = infoValue("Đang tính...");

        grid.add(infoBox("Phòng cũ", infoValue(oldRoomID)), "growx");
        grid.add(infoBox("Khách hàng", infoValue(safe(odrInfoCheckIn.getFullName()))), "growx");
        grid.add(infoBox("Số điện thoại", infoValue(safe(odrInfoCheckIn.getPhone()))), "growx");

        grid.add(infoBox("Kiểu booking", infoValue(safe(odrInfoCheckIn.getBookingType()))), "growx");
        grid.add(infoBox("Check-in cũ", infoValue(checkInOld.format(formatter))), "growx");
        grid.add(infoBox("Check-out cũ đến thời điểm chuyển", txtOldCheckOut), "growx");

        grid.add(infoBox("Giá phòng cũ từ check-in đến thời điểm chuyển", txtOldPriceSeg), "span 3, growx");

        card.add(grid, "growx");
        return card;
    }

    private JComponent buildNewRoomCard() {
        JPanel card = cardPanel(new MigLayout(
                "wrap 1, insets 18, gap 0 10, fillx",
                "[grow,fill]",
                "[]8[]12[]"
        ));

        card.add(sectionTitle("2. Thiết lập phòng mới"));
        card.add(mutedLabel("Phòng mới sẽ bắt đầu từ thời điểm chuyển đến check-out cuối cùng."));

        JPanel grid = new JPanel(new MigLayout(
                "insets 0, gap 10 10, fillx",
                "[grow,fill][grow,fill][grow,fill]",
                "[][][]"
        ));
        grid.setOpaque(false);

        txtChangeTime = infoValue(changeTimeSelected.format(formatter));
        txtCheckOutRoomNew = infoValue(checkOutOld.format(formatter));
        txtNewPriceSeg = infoValue("Đang tính...");

        grid.add(infoBox("Phòng mới", infoValue(safe(newRoom.getRoomId()) + " - " + safe(newRoom.getDescription()))), "span 3, growx");

        grid.add(infoBox("Loại phòng", infoValue(safe(newRoom.getRoomTypeName()))), "growx");
        grid.add(infoBox("View", infoValue(safe(newRoom.getView()))), "growx");
        grid.add(infoBox("Booking type", infoValue(safe(odrInfoCheckIn.getBookingType()))), "growx");

        grid.add(infoBox("Bắt đầu ở phòng mới", txtChangeTime), "growx");
        grid.add(infoBox("Check-out cuối cùng", txtCheckOutRoomNew), "growx");
        grid.add(infoBox("Giá phòng mới", txtNewPriceSeg), "growx");

        card.add(grid, "growx");
        return card;
    }

    private JComponent buildTransferCard() {
        JPanel card = cardPanel(new MigLayout(
                "wrap 1, insets 18, gap 0 10, fillx",
                "[grow,fill]",
                "[]8[]12[]"
        ));

        card.add(sectionTitle("3. Chọn thời điểm chuyển"));
        card.add(mutedLabel("Thời điểm chuyển phải nằm trong khoảng sau check-in cũ và trước check-out cũ."));

        JPanel chooserRow = new JPanel(new MigLayout(
                "insets 0, gap 12, fillx",
                "[][grow,fill][240!]",
                "[]"
        ));
        chooserRow.setOpaque(false);

        chooserRow.add(label("Thời điểm chuyển hiện tại:"));
        chooserRow.add(valueField(changeTimeSelected.format(formatter)), "growx");

        JButton btnPick = primaryButton("Chọn thời điểm chuyển");
        btnPick.addActionListener(e -> {
            LocalDateTime min = checkInOld.plusMinutes(1);
            LocalDateTime max = checkOutOld.minusMinutes(1);

            LocalDateTime picked = pickChangeTime(min, max, changeTimeSelected);
            if (picked != null) {
                changeTimeSelected = picked.withSecond(0).withNano(0);
                updatePreview();
                recalculateRoomFee();
            }
        });

        chooserRow.add(btnPick, "h 42!");

        JPanel notePanel = new JPanel(new MigLayout(
                "wrap 1, insets 12, gap 0 5",
                "[grow,fill]"
        ));
        notePanel.setBackground(PANEL_BG);
        notePanel.setBorder(BorderFactory.createLineBorder(BORDER));

        notePanel.add(mutedLabel("Lưu ý nghiệp vụ"));
        notePanel.add(mutedLabel("- Phòng cũ vẫn check-in đến đúng thời điểm chuyển."));
        notePanel.add(mutedLabel("- Phòng mới bắt đầu tính tiền từ thời điểm chuyển đến check-out cuối cùng."));

        card.add(chooserRow, "growx");
        card.add(notePanel, "growx");

        return card;
    }

    private JComponent buildPreviewCard() {
        JPanel card = cardPanel(new MigLayout(
                "wrap 1, insets 18, gap 0 10, fillx",
                "[grow,fill]",
                "[]8[]12[]"
        ));

        card.add(sectionTitle("4. Xem trước cập nhật"));
        card.add(mutedLabel("Tóm tắt các thông tin quan trọng trước khi xác nhận đổi phòng."));

        JPanel summary = new JPanel(new MigLayout(
                "wrap 2, insets 0, gap 10 10, fillx",
                "[grow,fill][grow,fill]",
                "[][][]"
        ));
        summary.setOpaque(false);

        txtPreviewOldRoom = infoValue(safe(oldRoomID));
        txtPreviewNewRoom = infoValue(safe(newRoom.getRoomId()));
        txtPreviewGuest = infoValue(safe(odrInfoCheckIn.getFullName()));
        txtPreviewChangeTime = infoValue(changeTimeSelected.format(formatter));
        txtPreviewOldFee = infoValue("Đang tính...");
        txtPreviewNewFee = infoValue("Đang tính...");

        summary.add(infoBox("Phòng cũ", txtPreviewOldRoom), "growx");
        summary.add(infoBox("Phòng mới", txtPreviewNewRoom), "growx");

        summary.add(infoBox("Khách hàng", txtPreviewGuest), "growx");
        summary.add(infoBox("Thời điểm chuyển", txtPreviewChangeTime), "growx");

        summary.add(infoBox("Chi phí phòng cũ", txtPreviewOldFee), "growx");
        summary.add(infoBox("Chi phí phòng mới", txtPreviewNewFee), "growx");

        JTextArea explain = plainTextArea(
                "Sau khi xác nhận:\n" +
                        "- Phòng cũ vẫn giữ trạng thái check-in đến đúng thời điểm chuyển.\n" +
                        "- Phòng mới bắt đầu được sử dụng từ thời điểm chuyển đến check-out cuối.\n" +
                        "- Hệ thống sẽ tính lại chi phí theo 2 đoạn thời gian."
        );
        explain.setBackground(PANEL_BG_2);
        explain.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        card.add(summary, "growx");
        card.add(explain, "growx, h 120!");

        return card;
    }

    private JComponent buildStatusCard() {
        JPanel card = cardPanel(new MigLayout(
                "wrap 1, insets 18, gap 0 10, fillx, filly",
                "[grow,fill]",
                "[]8[]12[]8[grow,fill]"
        ));

        card.add(sectionTitle("5. Trạng thái kiểm tra"));
        card.add(mutedLabel("Kiểm tra nhanh tình trạng trước khi bấm xác nhận."));

        lblStatusText = new JLabel("Đang kiểm tra...");
        lblStatusText.setOpaque(true);
        lblStatusText.setHorizontalAlignment(SwingConstants.CENTER);
        lblStatusText.setBackground(new Color(0xD2A93A));
        lblStatusText.setForeground(new Color(0x0B1F33));
        lblStatusText.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblStatusText.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        txtNote = plainTextArea("");
        txtNote.setBackground(PANEL_BG_2);
        txtNote.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        card.add(lblStatusText, "growx, h 52!");
        card.add(mutedLabel("Ghi chú"));
        card.add(txtNote, "grow");

        return card;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new MigLayout(
                "insets 12 24 20 24, fillx",
                "[grow,fill]push[140!][210!]",
                "[]"
        ));
        footer.setBackground(BG);

        JLabel note = new JLabel("Hệ thống sẽ gửi yêu cầu đổi phòng theo nghiệp vụ hiện tại. Vui lòng kiểm tra kỹ trước khi xác nhận.");
        note.setForeground(MUTED);
        note.setFont(new Font("Segoe UI", Font.ITALIC, 13));

        JButton btnClose = secondaryButton("Đóng");
        JButton btnConfirm = primaryButton("Xác nhận đổi phòng");

        btnClose.addActionListener(e -> dispose());
        btnConfirm.addActionListener(e -> confirmChangeRoomWhileCheckIn());

        footer.add(note, "growx");
        footer.add(btnClose, "h 44!");
        footer.add(btnConfirm, "h 44!");

        return footer;
    }

    private void confirmChangeRoomWhileCheckIn() {
        if (!(changeTimeSelected.isAfter(checkInOld) && changeTimeSelected.isBefore(checkOutOld))) {
            JOptionPane.showMessageDialog(this,
                    "Thời điểm chuyển phải sau check-in và trước check-out hiện tại.");
            return;
        }

        try {
            BaseResponse response = sendRequest(
                    CommandType.CHANGE_ROOM_WHILE_CHECKIN,
                    new ChangeRoomWhileCheckInRequestDTO(oldRoomID, newRoom.getRoomId(), changeTimeSelected)
            );

            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this,
                        "Lên lịch đổi phòng thành công!\n\n" +
                                "• Phòng cũ vẫn Check-in đến: " + changeTimeSelected.format(formatter) + "\n" +
                                "• Phòng mới được đặt từ: " + changeTimeSelected.format(formatter) + "\n" +
                                "• Phòng mới: " + newRoom.getRoomId() + "\n" +
                                "• Check-out cuối: " + checkOutOld.format(formatter) + "\n\n" +
                                "Đến thời điểm chuyển, hãy check-out phòng cũ và check-in phòng mới.");

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
        updatePreview();

        if (!(changeTimeSelected.isAfter(checkInOld) && changeTimeSelected.isBefore(checkOutOld))) {
            txtOldPriceSeg.setText("Thời gian không hợp lệ");
            txtNewPriceSeg.setText("Thời gian không hợp lệ");
            txtPreviewOldFee.setText("Thời gian không hợp lệ");
            txtPreviewNewFee.setText("Thời gian không hợp lệ");

            setStatus(false,
                    "Chưa hợp lệ",
                    "Thời điểm chuyển phải sau check-in cũ và trước check-out cũ.");
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
                String oldFee = oldSeg == null ? "(không có dữ liệu)" : formatMoney(oldSeg);

                txtOldPriceSeg.setText(oldFee);
                txtPreviewOldFee.setText(oldFee);
            } else {
                txtOldPriceSeg.setText(oldRes.getMessage());
                txtPreviewOldFee.setText(oldRes.getMessage());
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
                String newFee = newSeg == null ? "(không có dữ liệu)" : formatMoney(newSeg);

                txtNewPriceSeg.setText(newFee);
                txtPreviewNewFee.setText(newFee);

                setStatus(true,
                        "Hợp lệ",
                        "Có thể tiến hành xác nhận đổi phòng.\n\n" +
                                "- Phòng cũ: " + safe(oldRoomID) + "\n" +
                                "- Phòng mới: " + safe(newRoom.getRoomId()) + "\n" +
                                "- Khách hàng: " + safe(odrInfoCheckIn.getFullName()) + "\n" +
                                "- Thời điểm chuyển: " + changeTimeSelected.format(formatter) + "\n" +
                                "- Đoạn phí phòng cũ: " + txtPreviewOldFee.getText() + "\n" +
                                "- Đoạn phí phòng mới: " + txtPreviewNewFee.getText());
            } else {
                txtNewPriceSeg.setText(newRes.getMessage());
                txtPreviewNewFee.setText(newRes.getMessage());

                setStatus(false,
                        "Cần kiểm tra",
                        "Không thể tính được chi phí phòng mới.\nChi tiết: " + newRes.getMessage());
            }
        } catch (Exception ex) {
            txtOldPriceSeg.setText("Lỗi");
            txtNewPriceSeg.setText("Lỗi");
            txtPreviewOldFee.setText("Lỗi");
            txtPreviewNewFee.setText("Lỗi");

            setStatus(false, "Lỗi", ex.getMessage());
        }
    }

    private void updatePreview() {
        if (txtOldCheckOut != null) {
            txtOldCheckOut.setText(changeTimeSelected.format(formatter));
        }

        if (txtChangeTime != null) {
            txtChangeTime.setText(changeTimeSelected.format(formatter));
        }

        if (txtCheckOutRoomNew != null) {
            txtCheckOutRoomNew.setText(checkOutOld.format(formatter));
        }

        if (txtPreviewChangeTime != null) {
            txtPreviewChangeTime.setText(changeTimeSelected.format(formatter));
        }
    }

    private void setStatus(boolean ok, String title, String message) {
        lblStatusText.setText(title);
        lblStatusText.setBackground(ok ? SUCCESS : DANGER);
        lblStatusText.setForeground(Color.WHITE);

        txtNote.setText(message == null ? "" : message);
        txtNote.setCaretPosition(0);
    }

    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return SocketSessionManager.send(BaseRequest.of(commandType, data));
    }

    // Chọn thoi diem chuyen
    private LocalDateTime pickChangeTime(LocalDateTime minTime, LocalDateTime maxTime, LocalDateTime current) {
        JDialog d = new JDialog(this, "Chọn thời điểm chuyển", true);
        d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        d.setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);

        JPanel header = new JPanel(new MigLayout("wrap 1, insets 24 28 14 28, gap 0 6", "[grow]"));
        header.setBackground(BG);

        JLabel lbTitle = new JLabel("CHỌN THỜI ĐIỂM CHUYỂN");
        lbTitle.setForeground(FG);
        lbTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));

        JLabel lbSub = new JLabel("Chọn ngày và giờ chuyển phòng. Thời điểm chuyển phải nằm trong khoảng hợp lệ.");
        lbSub.setForeground(MUTED);
        lbSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        header.add(lbTitle);
        header.add(lbSub);

        root.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new MigLayout(
                "insets 10 28 18 28, gap 14 14, fillx",
                "[130!][grow,fill][130!][grow,fill]",
                "[][]20[]"
        ));
        content.setBackground(BG);

        content.add(label("Sớm nhất:"));
        content.add(valueField(minTime.format(formatter)), "growx");

        content.add(label("Muộn nhất:"));
        content.add(valueField(maxTime.format(formatter)), "growx, wrap");

        JTextField txtDate = new JTextField(current.toLocalDate().format(dateFormatter));
        styleTextField(txtDate);

        JSpinner spHour = new JSpinner(new SpinnerNumberModel(current.getHour(), 0, 23, 1));
        JSpinner spMin = new JSpinner(new SpinnerNumberModel(current.getMinute(), 0, 59, 1));

        styleSpinner(spHour, 90);
        styleSpinner(spMin, 90);

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        timePanel.setOpaque(false);

        JLabel lbHour = normalText("Giờ");
        JLabel lbMin = normalText("Phút");

        timePanel.add(spHour);
        timePanel.add(lbHour);
        timePanel.add(spMin);
        timePanel.add(lbMin);

        JButton btnPickDate = smallDateButton(txtDate);
        JPanel dateWrap = new JPanel(new MigLayout("insets 0, gap 8", "[grow,fill][44!]", "[]"));
        dateWrap.setOpaque(false);
        dateWrap.add(txtDate, "growx");
        dateWrap.add(btnPickDate, "growy");

        content.add(label("Ngày chuyển:"));
        content.add(dateWrap, "growx");

        content.add(label("Giờ chuyển:"));
        content.add(timePanel, "growx, wrap");

        JTextArea note = plainTextArea(
                "Lưu ý nghiệp vụ:\n" +
                        "- Thời điểm chuyển phải sau check-in cũ.\n" +
                        "- Thời điểm chuyển phải trước check-out cũ.\n" +
                        "- Sau khi xác nhận, hệ thống sẽ tính lại chi phí phòng cũ và phòng mới."
        );
        note.setBackground(PANEL_BG);
        note.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        content.add(label("Ghi chú:"));
        content.add(note, "span 3, growx, h 115!");

        root.add(content, BorderLayout.CENTER);

        JPanel footer = new JPanel(new MigLayout(
                "insets 12 28 24 28, fillx",
                "[grow]push[130!][150!]",
                "[]"
        ));
        footer.setBackground(BG);

        final LocalDateTime[] result = new LocalDateTime[1];

        JButton btnCancel = secondaryButton("Hủy");
        JButton btnOk = primaryButton("Xác nhận");

        btnCancel.addActionListener(e -> d.dispose());

        btnOk.addActionListener(e -> {
            try {
                LocalDate selectedDate = LocalDate.parse(txtDate.getText().trim(), dateFormatter);

                LocalDateTime selected = LocalDateTime.of(
                        selectedDate,
                        LocalTime.of((int) spHour.getValue(), (int) spMin.getValue())
                ).withSecond(0).withNano(0);

                if (selected.isBefore(minTime) || selected.isAfter(maxTime)) {
                    JOptionPane.showMessageDialog(d,
                            "Thời điểm chuyển phải nằm trong khoảng:\n"
                                    + minTime.format(formatter)
                                    + "  đến  "
                                    + maxTime.format(formatter));
                    return;
                }

                result[0] = selected;
                d.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d,
                        "Ngày chuyển không hợp lệ. Vui lòng chọn hoặc nhập theo định dạng dd/MM/yyyy.",
                        "Lỗi ngày chuyển",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        footer.add(new JLabel(), "growx");
        footer.add(btnCancel, "h 42!");
        footer.add(btnOk, "h 42!");

        root.add(footer, BorderLayout.SOUTH);

        d.setContentPane(root);
        d.setSize(900, 540);
        d.setMinimumSize(new Dimension(900, 540));
        d.setLocationRelativeTo(this);
        d.setVisible(true);

        return result[0];
    }


    private JButton smallDateButton(JTextField field) {
        JButton b = new JButton("📅");
        b.setFocusable(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBackground(PRIMARY);
        b.setForeground(new Color(0x0B1F33));
        b.setBorder(BorderFactory.createLineBorder(BORDER));
        b.addActionListener(e -> showDatePopup(field));
        return b;
    }

    private void showDatePopup(JTextField field) {
        LocalDate current = parseDateSafe(field.getText().trim());
        if (current == null) current = LocalDate.now();

        Date initDate = Date.from(current.atStartOfDay(ZoneId.systemDefault()).toInstant());
        SpinnerDateModel model = new SpinnerDateModel(initDate, null, null, Calendar.DAY_OF_MONTH);
        JSpinner spDate = new JSpinner(model);
        spDate.setEditor(new JSpinner.DateEditor(spDate, "dd/MM/yyyy"));
        spDate.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPanel panel = new JPanel(new MigLayout("wrap 1, insets 12, gap 8", "[260!,fill]"));
        panel.add(new JLabel("Chọn ngày:"));
        panel.add(spDate, "growx");

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Chọn ngày chuyển",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            Date selected = (Date) spDate.getValue();
            LocalDate date = selected.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            field.setText(date.format(dateFormatter));
        }
    }

    private LocalDate parseDateSafe(String text) {
        try {
            if (text == null || text.trim().isEmpty()) return null;
            return LocalDate.parse(text.trim(), dateFormatter);
        } catch (Exception e) {
            return null;
        }
    }

    private void applyLargeDialogSize(double widthRatio, double heightRatio, int minWidth, int minHeight) {
        Rectangle screen = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();

        int width = (int) Math.min(screen.width - 30, Math.max(minWidth, screen.width * widthRatio));
        int height = (int) Math.min(screen.height - 30, Math.max(minHeight, screen.height * heightRatio));

        setMinimumSize(new Dimension(Math.min(minWidth, screen.width - 30), Math.min(minHeight, screen.height - 30)));
        setPreferredSize(new Dimension(width, height));
        setSize(width, height);

        setLocation(
                screen.x + (screen.width - width) / 2,
                screen.y + (screen.height - height) / 2
        );
    }

    private JPanel cardPanel(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(CARD_BG);
        p.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));
        return p;
    }

    private JLabel headerBadge(String text) {
        JLabel lb = new JLabel(text);
        lb.setOpaque(true);
        lb.setBackground(PRIMARY);
        lb.setForeground(new Color(0x0B1F33));
        lb.setHorizontalAlignment(SwingConstants.CENTER);
        lb.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lb.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        return lb;
    }

    private JLabel sectionTitle(String s) {
        JLabel lb = new JLabel(s);
        lb.setForeground(PRIMARY);
        lb.setFont(new Font("Segoe UI", Font.BOLD, 20));
        return lb;
    }

    private JLabel mutedLabel(String s) {
        JLabel lb = new JLabel(s);
        lb.setForeground(MUTED);
        lb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return lb;
    }

    private JLabel normalText(String s) {
        JLabel lb = new JLabel(s);
        lb.setForeground(FG);
        lb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return lb;
    }

    private JLabel label(String s) {
        JLabel lb = new JLabel(s);
        lb.setForeground(FG);
        lb.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return lb;
    }

    private JTextArea infoValue(String s) {
        JTextArea txt = new JTextArea(safe(s));
        txt.setEditable(false);
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        txt.setOpaque(false);
        txt.setForeground(FG);
        txt.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txt.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        txt.setFocusable(false);
        return txt;
    }

    private JTextArea plainTextArea(String text) {
        JTextArea txt = new JTextArea(text == null ? "" : text);
        txt.setEditable(false);
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        txt.setForeground(MUTED);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txt.setFocusable(false);
        return txt;
    }

    private JComponent valueField(String s) {
        JTextArea txt = infoValue(s);
        txt.setBackground(PANEL_BG);
        txt.setOpaque(true);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        return txt;
    }

    private JPanel infoBox(String title, JComponent value) {
        JPanel p = new JPanel(new MigLayout(
                "wrap 1, insets 10 12 10 12, fillx",
                "[grow,fill]",
                "[]4[grow]"
        ));
        p.setBackground(PANEL_BG);
        p.setBorder(BorderFactory.createLineBorder(BORDER));

        JLabel t = new JLabel(title);
        t.setForeground(MUTED);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        p.add(t, "growx");
        p.add(value, "growx");

        p.setMinimumSize(new Dimension(0, 66));

        return p;
    }

    private void styleTextField(JTextField txt) {
        txt.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txt.setForeground(FG);
        txt.setBackground(PANEL_BG);
        txt.setCaretColor(Color.WHITE);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
    }

    private void styleSpinner(JSpinner sp, int width) {
        sp.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sp.setPreferredSize(new Dimension(width, 38));
        sp.setBorder(BorderFactory.createLineBorder(BORDER));
    }

    private JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(PRIMARY);
        b.setForeground(new Color(0x0B1F33));
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        return b;
    }

    private JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(BTN_SECONDARY);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        return b;
    }

    private String formatMoney(double money) {
        return String.format("%,.0f VNĐ", money);
    }

    private String safe(String s) {
        return s == null || s.trim().isEmpty() ? "-" : s.trim();
    }
}