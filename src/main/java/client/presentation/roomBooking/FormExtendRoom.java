package client.presentation.roomBooking;

import client.network.socket.SocketSessionManager;
import com.formdev.flatlaf.FlatClientProperties;
import common.dto.OdrInfoDTO;
import common.dto.request_dto.ExtendRoomRequestDTO;
import common.dto.request_dto.RoomIdRequestDTO;
import common.protocol.command.CommandType;
import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class FormExtendRoom extends JDialog {

    private static final Color BG = new Color(0x081B2C);
    private static final Color PANEL = new Color(0x0D2740);
    private static final Color PANEL_2 = new Color(0x123453);
    private static final Color BORDER = new Color(0x274B6D);

    private static final Color TEXT = new Color(0xEAF2FB);
    private static final Color MUTED = new Color(0xA9BED3);
    private static final Color GOLD = new Color(0xF5C452);
    private static final Color GOLD_DARK = new Color(0x2B2411);

    private static final Color BLUE_ACCENT = new Color(0x3DA9FC);
    private static final Color GREEN = new Color(0x4CD17C);
    private static final Color RED = new Color(0xFF6B6B);
    private static final Color ORANGE = new Color(0xFFB454);

    private final String roomID;
    private final FormRoomBookingManagement parent;

    private final DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private List<OdrInfoDTO> bookings = new ArrayList<>();

    private JButton btnExtend;
    private JTextField txtRoomID;
    private JComboBox<OdrInfoDTO> cboBookings;
    private JTextField txtNewCheckOutDate;
    private JSpinner spNewCheckOutTime;

    private JLabel lblCustomer;
    private JLabel lblPhone;
    private JLabel lblBookingType;
    private JLabel lblOdrId;
    private JLabel lblCheckIn;
    private JLabel lblCurrentCheckOut;

    private JLabel lblPreviewRoom;
    private JLabel lblPreviewCustomer;
    private JLabel lblPreviewOldOut;
    private JLabel lblPreviewNewOut;
    private JLabel lblPreviewExtendBy;
    private JLabel lblPreviewCheckIn;
    private JLabel lblPreviewStatus;
    private JTextArea txtPreviewNote;

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
        setResizable(true);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);

        JPanel centerContent = buildCenter();

        JScrollPane scrollPane = new JScrollPane(centerContent);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BG);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(scrollPane, BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        setContentPane(root);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        int width = Math.min(1450, screen.width - 120);
        int height = Math.min(880, screen.height - 100);

        setPreferredSize(new Dimension(width, height));
        setMinimumSize(new Dimension(1240, 760));

        pack();
        setSize(width, height);
        setLocationRelativeTo(parent);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0x0A2035));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                BorderFactory.createEmptyBorder(18, 22, 18, 22)
        ));

        JPanel left = new JPanel(new MigLayout("insets 0, wrap 1, gap 4", "[grow,fill]", "[]"));
        left.setOpaque(false);

        JLabel title = new JLabel("GIA HẠN PHÒNG");
        title.setForeground(TEXT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel sub = new JLabel("Lựa chọn đúng booking cần gia hạn và thiết lập thời gian check-out mới.");
        sub.setForeground(MUTED);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        left.add(title);
        left.add(sub);

        JLabel badge = createBadge("Room: " + roomID, GOLD, GOLD_DARK, new Font("Segoe UI", Font.BOLD, 13));

        header.add(left, BorderLayout.CENTER);
        header.add(badge, BorderLayout.EAST);

        return header;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel(new MigLayout(
                "insets 18, gap 18",
                "[grow,fill][370!,fill]",
                "[grow,fill]"
        ));
        center.setBackground(BG);

        JPanel left = new JPanel(new MigLayout(
                "wrap 1, insets 0, gap 16",
                "[grow,fill]",
                "[]"
        ));
        left.setOpaque(false);

        left.add(buildRoomInfoCard(), "growx, h 120!");
        left.add(buildBookingSelectionCard(), "growx, h 285!");
        left.add(buildExtendSettingsCard(), "growx, h 190!");

        center.add(left, "grow");
        center.add(buildPreviewCard(), "growy");

        return center;
    }

    private JPanel buildRoomInfoCard() {
        JPanel card = createCard();

        JLabel sectionTitle = createSectionTitle("Thông tin phòng");
        JLabel sectionSub = createSectionSub("Tóm tắt phòng đang được thao tác gia hạn.");

        txtRoomID = createTextField();
        txtRoomID.setEditable(false);
        txtRoomID.setText(roomID);

        JPanel infoWrap = new JPanel(new MigLayout("insets 0, gap 12", "[110!][grow,fill]", "[]"));
        infoWrap.setOpaque(false);
        infoWrap.add(createFieldLabel("Room ID:"));
        infoWrap.add(txtRoomID, "growx");

        card.add(sectionTitle, "wrap");
        card.add(sectionSub, "wrap");
        card.add(Box.createVerticalStrut(8), "wrap");
        card.add(infoWrap, "growx");

        return card;
    }

    private JPanel buildBookingSelectionCard() {
        JPanel card = createCard();

        JLabel sectionTitle = createSectionTitle("Chọn khách / booking");
        JLabel sectionSub = createSectionSub("Danh sách booking của phòng này. Hãy chọn đúng booking cần gia hạn.");

        cboBookings = new JComboBox<>();
        cboBookings.setRenderer(new BookingComboRenderer());
        styleComboBox(cboBookings);
        cboBookings.addActionListener(e -> onBookingChanged());

        JPanel top = new JPanel(new MigLayout("insets 0, gap 12", "[150!][grow,fill]", "[]"));
        top.setOpaque(false);
        top.add(createFieldLabel("Booking:"));
        top.add(cboBookings, "growx");

        lblCustomer = createValueLabel("-");
        lblPhone = createValueLabel("-");
        lblBookingType = createValueLabel("-");
        lblOdrId = createValueLabel("-");
        lblCheckIn = createValueLabel("-");
        lblCurrentCheckOut = createValueLabel("-");

        JPanel grid = new JPanel(new MigLayout(
                "insets 0, gap 12, wrap 2",
                "[grow,fill][grow,fill]",
                "[]"
        ));
        grid.setOpaque(false);

        grid.add(createInfoBox("Khách hàng", lblCustomer), "growx");
        grid.add(createInfoBox("Số điện thoại", lblPhone), "growx");
        grid.add(createInfoBox("Kiểu booking", lblBookingType), "growx");
        grid.add(createInfoBox("Mã ODR", lblOdrId), "growx");
        grid.add(createInfoBox("Check-in hiện tại", lblCheckIn), "growx");
        grid.add(createInfoBox("Check-out hiện tại", lblCurrentCheckOut), "growx");

        card.add(sectionTitle, "wrap");
        card.add(sectionSub, "wrap");
        card.add(Box.createVerticalStrut(8), "wrap");
        card.add(top, "growx, wrap");
        card.add(Box.createVerticalStrut(10), "wrap");
        card.add(grid, "growx");

        return card;
    }

    private JPanel buildExtendSettingsCard() {
        JPanel card = createCard();

        JLabel sectionTitle = createSectionTitle("Thiết lập gia hạn");
        JLabel sectionSub = createSectionSub("Chọn ngày và giờ check-out mới. Thời gian mới phải lớn hơn check-out hiện tại.");

        txtNewCheckOutDate = createTextField();
        txtNewCheckOutDate.setEditable(false);
        txtNewCheckOutDate.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "dd-MM-yyyy");

        JButton btnDate = new JButton("📅");
        btnDate.setFocusable(false);
        btnDate.setForeground(TEXT);
        btnDate.setBackground(PANEL_2);
        btnDate.setBorder(BorderFactory.createLineBorder(BORDER));
        btnDate.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnDate.addActionListener(e -> showDatePopup(txtNewCheckOutDate));

        spNewCheckOutTime = createTimeSpinner();
        spNewCheckOutTime.addChangeListener(e -> updatePreview());

        txtNewCheckOutDate.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updatePreview(); }

            @Override
            public void removeUpdate(DocumentEvent e) { updatePreview(); }

            @Override
            public void changedUpdate(DocumentEvent e) { updatePreview(); }
        });

        JPanel form = new JPanel(new MigLayout(
                "insets 0, gap 12",
                "[170!][grow,fill]",
                "[]"
        ));
        form.setOpaque(false);

        form.add(createFieldLabel("Ngày check-out mới:"));

        JPanel dateTimeWrap = new JPanel(new MigLayout("insets 0, gap 8", "[grow,fill][44!][100!]", "[]"));
        dateTimeWrap.setOpaque(false);
        dateTimeWrap.add(txtNewCheckOutDate, "growx");
        dateTimeWrap.add(btnDate, "growy");
        dateTimeWrap.add(spNewCheckOutTime, "growx");

        form.add(dateTimeWrap, "growx");

        JTextArea note = new JTextArea(
                "Lưu ý:\n" +
                        "- Chỉ booking đang chọn mới được gia hạn.\n" +
                        "- Không ảnh hưởng các booking khác của cùng phòng.\n" +
                        "- Hệ thống sẽ kiểm tra hợp lệ trước khi cập nhật."
        );
        note.setEditable(false);
        note.setOpaque(true);
        note.setBackground(new Color(0x10273D));
        note.setForeground(MUTED);
        note.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        note.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        card.add(sectionTitle, "wrap");
        card.add(sectionSub, "wrap");
        card.add(Box.createVerticalStrut(8), "wrap");
        card.add(form, "growx, wrap");
        card.add(Box.createVerticalStrut(12), "wrap");
        card.add(note, "growx");

        return card;
    }

    private JPanel buildPreviewCard() {
        JPanel card = createCard();
        card.removeAll();
        card.setLayout(new MigLayout("wrap 1, insets 18, gap 12", "[grow,fill]", "[]"));

        JLabel sectionTitle = createSectionTitle("Xem trước gia hạn");
        JLabel sectionSub = createSectionSub("Tóm tắt thông tin trước khi xác nhận.");

        lblPreviewRoom = createPreviewValue("-");
        lblPreviewCustomer = createPreviewValue("-");
        lblPreviewCheckIn = createPreviewValue("-");
        lblPreviewOldOut = createPreviewValue("-");
        lblPreviewNewOut = createPreviewValue("-");
        lblPreviewExtendBy = createPreviewValue("-");
        lblPreviewStatus = createBadge("Chưa hợp lệ", RED, Color.WHITE, new Font("Segoe UI", Font.BOLD, 12));

        JPanel info = new JPanel(new MigLayout("wrap 1, insets 0, gap 10", "[grow,fill]", "[]"));
        info.setOpaque(false);
        info.add(createPreviewRow("Phòng", lblPreviewRoom), "growx");
        info.add(createPreviewRow("Khách hàng", lblPreviewCustomer), "growx");
        info.add(createPreviewRow("Check-in", lblPreviewCheckIn), "growx");
        info.add(createPreviewRow("Check-out hiện tại", lblPreviewOldOut), "growx");
        info.add(createPreviewRow("Check-out mới", lblPreviewNewOut), "growx");
        info.add(createPreviewRow("Gia hạn thêm", lblPreviewExtendBy), "growx");

        JPanel statusBox = new JPanel(new MigLayout("insets 12, gap 8, wrap 1", "[grow,fill]", "[]"));
        statusBox.setBackground(new Color(0x10273D));
        statusBox.setBorder(BorderFactory.createLineBorder(BORDER));

        JLabel lbStatusTitle = new JLabel("Trạng thái");
        lbStatusTitle.setForeground(TEXT);
        lbStatusTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JLabel lbStatusDesc = new JLabel("Kiểm tra nhanh tính hợp lệ trước khi lưu");
        lbStatusDesc.setForeground(MUTED);
        lbStatusDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        statusBox.add(lbStatusTitle);
        statusBox.add(lbStatusDesc);
        statusBox.add(lblPreviewStatus);

        txtPreviewNote = new JTextArea();
        txtPreviewNote.setEditable(false);
        txtPreviewNote.setLineWrap(true);
        txtPreviewNote.setWrapStyleWord(true);
        txtPreviewNote.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtPreviewNote.setForeground(TEXT);
        txtPreviewNote.setBackground(new Color(0x10273D));
        txtPreviewNote.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        txtPreviewNote.setText("Chọn booking và thời gian check-out mới để xem trước chi tiết.");

        card.add(sectionTitle, "wrap");
        card.add(sectionSub, "wrap");
        card.add(Box.createVerticalStrut(6), "wrap");
        card.add(info, "growx, wrap");
        card.add(statusBox, "growx, wrap");
        card.add(new JLabel("Ghi chú"), "gapy 4");
        card.add(txtPreviewNote, "growx, h 120!");

        return card;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(0x0A2035));
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));

        JLabel leftNote = new JLabel("Hãy kiểm tra đúng khách hàng, booking và thời gian trước khi xác nhận.");
        leftNote.setForeground(MUTED);
        leftNote.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JButton btnClose = createSecondaryButton("ĐÓNG");
        btnExtend = createPrimaryButton("XÁC NHẬN GIA HẠN");

        btnClose.addActionListener(e -> dispose());
        btnExtend.addActionListener(e -> extendRoom());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(btnClose);
        actions.add(btnExtend);

        footer.add(leftNote, BorderLayout.WEST);
        footer.add(actions, BorderLayout.EAST);

        return footer;
    }



    private void showDatePopup(JTextField field) {
        LocalDate current = parseDateSafe(field.getText().trim());
        if (current == null) current = LocalDate.now();

        Date initDate = Date.from(current.atStartOfDay(ZoneId.systemDefault()).toInstant());
        SpinnerDateModel model = new SpinnerDateModel(initDate, null, null, Calendar.DAY_OF_MONTH);
        JSpinner spDate = new JSpinner(model);
        spDate.setEditor(new JSpinner.DateEditor(spDate, "dd-MM-yyyy"));
        spDate.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPanel panel = new JPanel(new MigLayout("wrap 1, insets 12, gap 8", "[260!,fill]"));
        panel.add(new JLabel("Chọn ngày:"));
        panel.add(spDate, "growx");

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Chọn ngày",
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

    // Load bookings
    private void loadBookings() {
        try {
            LinkedHashMap<String, OdrInfoDTO> map = new LinkedHashMap<>();

            BaseResponse activeRes = sendRequest(
                    CommandType.GET_ACTIVE_CHECKIN_INFO,
                    new RoomIdRequestDTO(roomID)
            );
            if (activeRes.isSuccess() && activeRes.getData() instanceof OdrInfoDTO active) {
                if (active.getOrderDetailRoomId() != null) {
                    map.put(active.getOrderDetailRoomId(), active);
                }
            }

            BaseResponse pendingRes = sendRequest(
                    CommandType.GET_PENDING_BOOKINGS_OF_ROOM,
                    new RoomIdRequestDTO(roomID)
            );
            if (pendingRes.isSuccess() && pendingRes.getData() instanceof List<?> rawList) {
                for (Object obj : rawList) {
                    if (obj instanceof OdrInfoDTO odr && odr.getOrderDetailRoomId() != null) {
                        map.putIfAbsent(odr.getOrderDetailRoomId(), odr);
                    }
                }
            }

            bookings = new ArrayList<>(map.values());

            DefaultComboBoxModel<OdrInfoDTO> model = new DefaultComboBoxModel<>();
            for (OdrInfoDTO odr : bookings) {
                model.addElement(odr);
            }
            cboBookings.setModel(model);

            if (bookings.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Phòng này hiện không có booking nào để gia hạn.",
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
                return;
            }

            cboBookings.setSelectedIndex(0);
            onBookingChanged();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    private void onBookingChanged() {
        OdrInfoDTO odr = getSelectedBooking();

        if (odr == null) {
            lblCustomer.setText("-");
            lblPhone.setText("-");
            lblBookingType.setText("-");
            lblOdrId.setText("-");
            lblCheckIn.setText("-");
            lblCurrentCheckOut.setText("-");
            updatePreview();
            return;
        }

        lblCustomer.setText(valueOrDash(odr.getFullName()));
        lblPhone.setText(valueOrDash(odr.getPhone()));
        lblBookingType.setText(valueOrDash(odr.getBookingType()));
        lblOdrId.setText(valueOrDash(odr.getOrderDetailRoomId()));
        lblCheckIn.setText(formatDateTime(odr.getCheckIn()));
        lblCurrentCheckOut.setText(formatDateTime(odr.getCheckOut()));

        if (odr.getCheckOut() != null) {
            txtNewCheckOutDate.setText(odr.getCheckOut().toLocalDate().format(dateFormatter));
            spNewCheckOutTime.setValue(Date.from(
                    odr.getCheckOut()
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
            ));
        }

        updatePreview();
    }

    private void updatePreview() {
        OdrInfoDTO odr = getSelectedBooking();

        lblPreviewRoom.setText(roomID);

        if (odr == null) {
            lblPreviewCustomer.setText("-");
            lblPreviewCheckIn.setText("-");
            lblPreviewOldOut.setText("-");
            lblPreviewNewOut.setText("-");
            lblPreviewExtendBy.setText("-");
            setStatusBadge("Chưa hợp lệ", RED, Color.WHITE);
            txtPreviewNote.setText("Chưa chọn booking.");
            setExtendButtonEnabled(false);
            return;
        }

        lblPreviewCustomer.setText(valueOrDash(odr.getFullName()));
        lblPreviewCheckIn.setText(formatDateTime(odr.getCheckIn()));
        lblPreviewOldOut.setText(formatDateTime(odr.getCheckOut()));

        LocalDateTime newOut = getNewCheckOutSafe();
        if (newOut == null) {
            lblPreviewNewOut.setText("-");
            lblPreviewExtendBy.setText("-");
            setStatusBadge("Thiếu thời gian mới", ORANGE, Color.BLACK);
            txtPreviewNote.setText("Hãy chọn ngày và giờ check-out mới để xem trước.");
            setExtendButtonEnabled(false);
            return;
        }

        lblPreviewNewOut.setText(formatDateTime(newOut));

        if (odr.getCheckOut() != null) {
            lblPreviewExtendBy.setText(formatDuration(odr.getCheckOut(), newOut));
        } else {
            lblPreviewExtendBy.setText("-");
        }

        boolean valid = true;
        StringBuilder note = new StringBuilder();

        if (odr.getCheckIn() == null) {
            valid = false;
            note.append("- Booking đang chọn thiếu thời gian check-in.\n");
        } else if (!newOut.isAfter(odr.getCheckIn())) {
            valid = false;
            note.append("- Check-out mới phải sau check-in.\n");
        }

        if (odr.getCheckOut() != null && !newOut.isAfter(odr.getCheckOut())) {
            valid = false;
            note.append("- Check-out mới phải sau check-out hiện tại.\n");
        }

        OdrInfoDTO conflict = findExtendConflict(odr, newOut);
        if (conflict != null) {
            valid = false;
            note.append("- Không thể gia hạn vì thời gian mới bị trùng với booking khác của phòng ")
                    .append(roomID)
                    .append(".\n");
            note.append("- Booking bị trùng: ")
                    .append(formatConflictBooking(conflict))
                    .append("\n");
        }

        if (valid) {
            setStatusBadge("Sẵn sàng gia hạn", GREEN, new Color(0x062214));
            note.append("Booking đã chọn có thể được gia hạn.\n");
            note.append("Khách hàng: ").append(valueOrDash(odr.getFullName())).append("\n");
            note.append("Gia hạn thêm: ").append(lblPreviewExtendBy.getText()).append("\n");
            note.append("Hệ thống sẽ chỉ cập nhật booking đang chọn.");
            setExtendButtonEnabled(true);
        } else {
            setStatusBadge("Không thể gia hạn", RED, Color.WHITE);
            note.append("Vui lòng chỉnh lại thời gian trước khi xác nhận.");
            setExtendButtonEnabled(false);
        }

        txtPreviewNote.setText(note.toString());
    }

    private void extendRoom() {
        try {
            OdrInfoDTO selected = getSelectedBooking();
            if (selected == null) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng chọn booking cần gia hạn.",
                        "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDateTime newCheckOut = getNewCheckOut();
            if (selected.getCheckIn() != null && !newCheckOut.isAfter(selected.getCheckIn())) {
                JOptionPane.showMessageDialog(this,
                        "Check-out mới phải sau check-in.",
                        "Dữ liệu chưa hợp lệ",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (selected.getCheckOut() != null && !newCheckOut.isAfter(selected.getCheckOut())) {
                JOptionPane.showMessageDialog(this,
                        "Check-out mới phải sau check-out hiện tại.",
                        "Dữ liệu chưa hợp lệ",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            OdrInfoDTO conflict = findExtendConflict(selected, newCheckOut);
            if (conflict != null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Không thể gia hạn vì thời gian mới bị trùng với booking khác của phòng "
                                + roomID
                                + ":\n"
                                + formatConflictBooking(conflict),
                        "Trùng lịch gia hạn",
                        JOptionPane.WARNING_MESSAGE
                );
                updatePreview();
                return;
            }

            BaseResponse response = sendRequest(
                    CommandType.EXTEND_ROOM,
                    new ExtendRoomRequestDTO(roomID, selected.getOrderDetailRoomId(), newCheckOut)
            );
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this,
                        "Gia hạn phòng thành công.",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
                if (parent != null) {
                    parent.loadData();
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        response.getMessage(),
                        "Không thể gia hạn",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // send request
    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return SocketSessionManager.send(BaseRequest.of(commandType, data));
    }

    private OdrInfoDTO getSelectedBooking() {
        Object selected = cboBookings.getSelectedItem();
        return (selected instanceof OdrInfoDTO odr) ? odr : null;
    }

    private LocalDateTime getNewCheckOut() {
        String text = txtNewCheckOutDate.getText().trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ngày check-out mới.");
        }

        LocalDate date = LocalDate.parse(text, dateFormatter);
        LocalTime time = getSpinnerTime(spNewCheckOutTime);

        return LocalDateTime.of(date, time);
    }

    private LocalDateTime getNewCheckOutSafe() {
        try {
            return getNewCheckOut();
        } catch (Exception e) {
            return null;
        }
    }

    private LocalTime getSpinnerTime(JSpinner spinner) {
        Date value = (Date) spinner.getValue();
        return value.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalTime()
                .withSecond(0)
                .withNano(0);
    }

    private String formatDateTime(LocalDateTime ldt) {
        return ldt == null ? "-" : ldt.format(displayFormatter);
    }

    private String valueOrDash(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    private String formatDuration(LocalDateTime oldOut, LocalDateTime newOut) {
        if (oldOut == null || newOut == null || !newOut.isAfter(oldOut)) {
            return "-";
        }

        Duration duration = Duration.between(oldOut, newOut);
        long totalHours = duration.toHours();
        long days = totalHours / 24;
        long hours = totalHours % 24;
        long minutes = duration.toMinutes() % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append(" ngày ");
        if (hours > 0) sb.append(hours).append(" giờ ");
        if (minutes > 0) sb.append(minutes).append(" phút");

        String rs = sb.toString().trim();
        return rs.isEmpty() ? "0 phút" : rs;
    }

    private OdrInfoDTO findExtendConflict(OdrInfoDTO selected, LocalDateTime newOut) {
        if (selected == null || newOut == null || selected.getCheckIn() == null) {
            return null;
        }

        String selectedOdrId = selected.getOrderDetailRoomId() == null
                ? ""
                : selected.getOrderDetailRoomId().trim();

        LocalDateTime newIn = selected.getCheckIn();

        for (OdrInfoDTO other : bookings) {
            if (other == null) continue;

            String otherOdrId = other.getOrderDetailRoomId() == null
                    ? ""
                    : other.getOrderDetailRoomId().trim();

            // Bỏ qua chính booking đang gia hạn
            if (!selectedOdrId.isEmpty() && selectedOdrId.equalsIgnoreCase(otherOdrId)) {
                continue;
            }

            if (other.getCheckIn() == null || other.getCheckOut() == null) {
                continue;
            }

            // Công thức giao lịch: A.start < B.end && A.end > B.start
            boolean overlap = newIn.isBefore(other.getCheckOut())
                    && newOut.isAfter(other.getCheckIn());

            if (overlap) {
                return other;
            }
        }

        return null;
    }

    private String formatConflictBooking(OdrInfoDTO conflict) {
        if (conflict == null) return "";

        return valueOrDash(conflict.getFullName())
                + " | "
                + valueOrDash(conflict.getPhone())
                + " | "
                + formatDateTime(conflict.getCheckIn())
                + " → "
                + formatDateTime(conflict.getCheckOut())
                + " | "
                + valueOrDash(conflict.getOrderDetailRoomId());
    }

    private void setExtendButtonEnabled(boolean enabled) {
        if (btnExtend != null) {
            btnExtend.setEnabled(enabled);
            btnExtend.setCursor(new Cursor(enabled ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        }
    }

    private JPanel createCard() {
        JPanel card = new JPanel(new MigLayout(
                "wrap 1, insets 18, gap 10",
                "[grow,fill]",
                "[]"
        ));
        card.setBackground(PANEL);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));
        return card;
    }

    private JLabel createSectionTitle(String text) {
        JLabel lb = new JLabel(text);
        lb.setForeground(GOLD);
        lb.setFont(new Font("Segoe UI", Font.BOLD, 18));
        return lb;
    }

    private JLabel createSectionSub(String text) {
        JLabel lb = new JLabel(text);
        lb.setForeground(MUTED);
        lb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return lb;
    }

    private JLabel createFieldLabel(String text) {
        JLabel lb = new JLabel(text);
        lb.setForeground(TEXT);
        lb.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return lb;
    }

    private JTextField createTextField() {
        JTextField txt = new JTextField();
        txt.setBackground(PANEL_2);
        txt.setForeground(TEXT);
        txt.setCaretColor(TEXT);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return txt;
    }

    private JSpinner createTimeSpinner() {
        SpinnerDateModel model = new SpinnerDateModel();
        JSpinner sp = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(sp, "HH:mm");
        sp.setEditor(editor);
        sp.setValue(Date.from(
                LocalDateTime.now().withHour(12).withMinute(0).withSecond(0).withNano(0)
                        .atZone(ZoneId.systemDefault()).toInstant()
        ));
        sp.setBackground(PANEL_2);
        sp.setForeground(TEXT);
        sp.setBorder(BorderFactory.createLineBorder(BORDER));
        sp.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return sp;
    }

    private void styleComboBox(JComboBox<?> combo) {
        combo.setBackground(PANEL_2);
        combo.setForeground(TEXT);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        combo.setBorder(BorderFactory.createLineBorder(BORDER));
    }

    private JLabel createValueLabel(String text) {
        JLabel lb = new JLabel(text);
        lb.setForeground(TEXT);
        lb.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return lb;
    }

    private JPanel createInfoBox(String title, JLabel valueLabel) {
        JPanel box = new JPanel(new BorderLayout(0, 6));
        box.setBackground(new Color(0x10273D));
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel lbTitle = new JLabel(title);
        lbTitle.setForeground(MUTED);
        lbTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        box.add(lbTitle, BorderLayout.NORTH);
        box.add(valueLabel, BorderLayout.CENTER);
        return box;
    }

    private JLabel createPreviewValue(String text) {
        JLabel lb = new JLabel(text);
        lb.setForeground(TEXT);
        lb.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return lb;
    }

    private JPanel createPreviewRow(String title, JLabel value) {
        JPanel row = new JPanel(new BorderLayout(0, 4));
        row.setBackground(new Color(0x10273D));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel t = new JLabel(title);
        t.setForeground(MUTED);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        row.add(t, BorderLayout.NORTH);
        row.add(value, BorderLayout.CENTER);
        return row;
    }

    private JLabel createBadge(String text, Color bg, Color fg, Font font) {
        JLabel lb = new JLabel("  " + text + "  ");
        lb.setOpaque(true);
        lb.setBackground(bg);
        lb.setForeground(fg);
        lb.setFont(font);
        lb.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        return lb;
    }

    private void setStatusBadge(String text, Color bg, Color fg) {
        lblPreviewStatus.setText("  " + text + "  ");
        lblPreviewStatus.setOpaque(true);
        lblPreviewStatus.setBackground(bg);
        lblPreviewStatus.setForeground(fg);
        lblPreviewStatus.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
    }

    private JButton createPrimaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(GOLD_DARK);
        b.setBackground(GOLD);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(11, 20, 11, 20));
        return b;
    }

    private JButton createSecondaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(TEXT);
        b.setBackground(PANEL_2);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)
        ));
        return b;
    }


    private class BookingComboRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            JLabel lb = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof OdrInfoDTO odr) {
                String name = valueOrDash(odr.getFullName());
                String phone = valueOrDash(odr.getPhone());
                String in = formatDateTime(odr.getCheckIn());
                String out = formatDateTime(odr.getCheckOut());
                String odrId = valueOrDash(odr.getOrderDetailRoomId());

                lb.setText("<html><div style='padding:4px 0;'>"
                        + "<b>" + name + "</b> - " + phone
                        + "<br/>"
                        + "<span style='font-size:10px;'>"
                        + in + " → " + out + " | " + odrId
                        + "</span></div></html>");
            }

            lb.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            lb.setBackground(isSelected ? new Color(0x1B4770) : PANEL_2);
            lb.setForeground(TEXT);
            return lb;
        }
    }
}