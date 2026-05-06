package client.presentation.roomBooking;

import client.network.socket.HotelClient;
import client.network.socket.SocketRequestExecutor;
import client.network.socket.SocketSessionManager;
import client.presentation.login.main.Application;
import common.dto.CustomerDTO;
import common.dto.request_dto.BookRoomRequestDTO;
import common.protocol.command.CommandType;
import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;
import net.miginfocom.swing.MigLayout;
import server.core.service.CustomerService;
import server.core.service.RoomStayService;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class FormMultiBookRooms extends JDialog {

    private final Color BG = new Color(0x0B1F33);
    private final Color FG = new Color(0xE9EEF6);
    private final Color CARD_BG = new Color(0x102D4A);
    private final Color BORDER = new Color(0x274A6B);
    private final Color GOLD = new Color(0xF5C452);

    private final List<String> roomIDs;
    private final String employeeID;
    private final FormRoomBookingManagement parent;
    private final RoomStayService roomStayService;
    private final CustomerService customerService;

    private JTextField txtFullName;
    private JTextField txtPhone;
    private JTextField txtEmail;
    private JTextField txtIdCard;

    private JTextField txtBookingDate;
    private JTextField txtCheckIn;
    private JTextField txtCheckOut;
    private JSpinner spCheckInTime;
    private JSpinner spCheckOutTime;
    private JComboBox<String> cbxBookingType;
    private JSpinner spHours;
    private JLabel lblHours;
    private JLabel lblNightNote;

    private JTable table;
    private RoomMultiBookTableModel tableModel;

    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private String lastAutoPhone = null;

    public FormMultiBookRooms(Window owner,
                              List<String> roomIDs,
                              String employeeID,
                              RoomStayService roomStayService,
                              CustomerService customerService,
                              FormRoomBookingManagement parent) {
        super(owner, "Đặt nhiều phòng (" + roomIDs.size() + ")", ModalityType.APPLICATION_MODAL);
        this.roomIDs = new ArrayList<>(roomIDs);
        this.employeeID = employeeID;
        this.roomStayService = roomStayService;
        this.customerService = customerService;
        this.parent = parent;

        buildUI();
        pack();
        setSize(Math.max(900, getWidth()), Math.max(640, getHeight()));
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        JPanel root = new JPanel(new MigLayout("wrap, fill, insets 14 16 16 16, gap 12", "[grow,fill]"));
        root.setBackground(BG);

        JPanel pCus = section("Thông tin khách hàng");
        txtFullName = text();
        txtPhone = text();
        txtEmail = text();
        txtIdCard = text();

        pCus.add(label("Họ tên:"));
        pCus.add(txtFullName, "growx, wrap");
        pCus.add(label("Số điện thoại:"));
        pCus.add(txtPhone, "growx, wrap");
        pCus.add(label("Email:"));
        pCus.add(txtEmail, "growx, wrap");
        pCus.add(label("ID Card:"));
        pCus.add(txtIdCard, "growx");
        root.add(pCus, "growx");

        JPanel pTime = section("Thiết lập thời gian mặc định");
        txtBookingDate = dateField();
        txtCheckIn = dateField();
        txtCheckOut = dateField();
        spCheckInTime = timeSpinner();
        spCheckOutTime = timeSpinner();

        SpinnerNumberModel hourModel = new SpinnerNumberModel(1, 1, 240, 1);
        spHours = new JSpinner(hourModel);
        JSpinner.NumberEditor hoursEditor = new JSpinner.NumberEditor(spHours, "#");
        spHours.setEditor(hoursEditor);
        JFormattedTextField tfHours = hoursEditor.getTextField();
        tfHours.setBackground(CARD_BG);
        tfHours.setForeground(FG);
        NumberFormatter fmt = (NumberFormatter) tfHours.getFormatter();
        fmt.setAllowsInvalid(false);
        fmt.setMinimum(1);
        fmt.setMaximum(240);
        spHours.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        spHours.setForeground(FG);
        spHours.setBackground(CARD_BG);
        spHours.setBorder(new LineBorder(BORDER, 1));

        lblHours = label("Hours of use:");
        lblNightNote = new JLabel("Từ 08:00 đến 19:00 sẽ có phí giữ phòng 20.000 VNĐ/giờ.");
        lblNightNote.setForeground(new Color(0xFCA5A5));
        lblNightNote.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblNightNote.setVisible(false);

        cbxBookingType = new JComboBox<>(new String[]{"Giờ", "Ngày", "Đêm"});
        styleCombo(cbxBookingType);

        txtBookingDate.setText(LocalDate.now().format(df));
        txtCheckIn.setText(LocalDate.now().format(df));
        txtCheckOut.setText(LocalDate.now().format(df));
        setSpinnerTime(spCheckInTime, LocalTime.now().withSecond(0).withNano(0));
        setSpinnerTime(spCheckOutTime, LocalTime.now().plusHours(2).withSecond(0).withNano(0));

        JButton btnPickBD = btnCal(txtBookingDate);
        JButton btnPickCI = btnCal(txtCheckIn);
        JButton btnPickCO = btnCal(txtCheckOut);

        pTime.add(label("Ngày đặt phòng:"));
        pTime.add(rowDate(txtBookingDate, btnPickBD), "growx, wrap");
        pTime.add(label("Check-in (mặc định):"));
        pTime.add(rowDateTime(txtCheckIn, btnPickCI, spCheckInTime), "growx, wrap");
        pTime.add(lblHours, "hidemode 3");
        pTime.add(spHours, "growx, hidemode 3, wrap");
        pTime.add(lblNightNote, "span 2, gapleft 140, hidemode 3, wrap");
        pTime.add(label("Check-out (mặc định):"));
        pTime.add(rowDateTime(txtCheckOut, btnPickCO, spCheckOutTime), "growx, wrap");
        pTime.add(label("Kiểu đặt phòng:"));
        pTime.add(cbxBookingType, "growx");
        root.add(pTime, "growx");

        JPanel pList = section("Danh sách phòng & tùy chỉnh thời gian theo phòng");
        tableModel = new RoomMultiBookTableModel();
        tableModel.setRooms(roomIDs, getDefaultCheckIn(), getDefaultCheckOut(), String.valueOf(cbxBookingType.getSelectedItem()));

        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFillsViewportHeight(true);
        table.setBackground(new Color(0x0E253D));
        table.setForeground(FG);
        table.getTableHeader().setBackground(new Color(0x102A43));
        table.getTableHeader().setForeground(new Color(0x22D3EE));
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);

        TableCellRendererButton.install(table, 5, "Tùy chỉnh", rowView -> {
            int rowModel = table.convertRowIndexToModel(rowView);
            RoomMultiBookTableModel.Row row = tableModel.getRowAt(rowModel);
            if (row != null) editRoom(row.getRoomID());
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(BORDER, 1));
        scrollPane.getViewport().setBackground(BG);
        pList.add(scrollPane, "span 2, grow, push, h 220:280:");
        root.add(pList, "grow, push");

        JPanel pnActions = new JPanel(new MigLayout("insets 8 16 0 24, fillx, gapx 12", "[grow,fill]push[]10[pref!]", "[]"));
        pnActions.setOpaque(false);

        JLabel lblHint = new JLabel("Mẹo: Nhấn F4 để đóng nhanh cửa sổ này");
        lblHint.setForeground(new Color(0xB8C4D4));
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        JButton btnCancel = secondary("HỦY");
        JButton btnSubmit = primary("ĐẶT " + roomIDs.size() + " PHÒNG");

        pnActions.add(lblHint, "left");
        pnActions.add(btnCancel);
        pnActions.add(btnSubmit);
        root.add(pnActions, "growx");

        btnCancel.addActionListener(e -> dispose());
        btnSubmit.addActionListener(e -> bookMultiRoom());

        setContentPane(root);

        attachAutoFillCustomerByPhone();
        attachDateChangeListener(txtCheckIn, this::applyDefaultsToTable);
        attachDateChangeListener(txtCheckOut, this::applyDefaultsToTable);

        spCheckInTime.addChangeListener(e -> {
            String bt = String.valueOf(cbxBookingType.getSelectedItem());
            if ("Giờ".equalsIgnoreCase(bt)) updateCheckoutFieldsForHourType();
            else if ("Đêm".equalsIgnoreCase(bt)) updateCheckoutFieldsForNightType();
            applyDefaultsToTable();
        });

        spHours.addChangeListener(e -> {
            String bt = String.valueOf(cbxBookingType.getSelectedItem());
            if ("Giờ".equalsIgnoreCase(bt)) updateCheckoutFieldsForHourType();
            else if ("Đêm".equalsIgnoreCase(bt)) updateCheckoutFieldsForNightType();
            applyDefaultsToTable();
        });

        cbxBookingType.addActionListener(e -> updateTimeByBookingType());
        updateTimeByBookingType();
    }

    private void bookMultiRoom() {
        String fullName = txtFullName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String idCard = txtIdCard.getText().trim();

        if (fullName.isEmpty() || phone.isEmpty() || email.isEmpty() || idCard.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin.");
            return;
        }
        if (!phone.matches("^0\\d{9}$")) {
            JOptionPane.showMessageDialog(this, "SĐT phải dạng 0xxxxxxxxx");
            return;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            JOptionPane.showMessageDialog(this, "Email không hợp lệ");
            return;
        }
        if (!idCard.matches("\\d{12}")) {
            JOptionPane.showMessageDialog(this, "CCCD phải đúng 12 số");
            return;
        }

        LocalDate booking = parseDate(txtBookingDate.getText());
        if (booking == null) {
            JOptionPane.showMessageDialog(this, "Định dạng Booking date phải là dd-MM-yyyy.");
            return;
        }

        CustomerDTO customer = new CustomerDTO();
        customer.setFullName(fullName);
        customer.setPhone(phone);
        customer.setEmail(email);
        customer.setIdCard(idCard);

        List<String[]> okRows = new ArrayList<>();
        List<String[]> errRows = new ArrayList<>();

        for (RoomMultiBookTableModel.Row r : tableModel.getAllRows()) {
            LocalDateTime checkInDateTime = r.getCheckIn();
            LocalDateTime checkOutDateTime = r.getCheckOut();
            String bookingType = r.getBookingType();

            if ("Đêm".equalsIgnoreCase(bookingType) && !checkOutDateTime.isAfter(checkInDateTime)) {
                checkOutDateTime = checkOutDateTime.plusDays(1);
            }

            if (checkInDateTime.toLocalDate().isBefore(booking)) {
                errRows.add(new String[]{r.getRoomID(), "Check-in phải sau hoặc bằng Booking date."});
                continue;
            }
            if (!checkOutDateTime.isAfter(checkInDateTime)) {
                errRows.add(new String[]{r.getRoomID(), "Check-out phải sau Check-in."});
                continue;
            }

            try {
                BaseResponse response = sendRequest(
                        CommandType.BOOK_ROOM,
                        new BookRoomRequestDTO(
                                customer,
                                r.getRoomID(),
                                employeeID,
                                booking.atStartOfDay(),
                                checkInDateTime,
                                checkOutDateTime,
                                bookingType
                        )
                );

                boolean ok = response.isSuccess();

                if (ok) {
                    okRows.add(new String[]{r.getRoomID(), dtf.format(checkInDateTime), dtf.format(checkOutDateTime), bookingType});
                } else {
                    errRows.add(new String[]{r.getRoomID(), response.getMessage()});
                }
            } catch (Exception ex) {
                errRows.add(new String[]{r.getRoomID(), ex.getMessage()});
            }
        }

        showBookingResultDialog(okRows, errRows);

        if (!okRows.isEmpty()) {
            dispose();
            if (parent != null) parent.reload();
        }
    }

    private void editRoom(String roomID) {
        JDialog d = new JDialog(this, "Tùy chỉnh – " + roomID, true);
        d.getContentPane().setBackground(BG);
        d.setLayout(new MigLayout("wrap 2, insets 14 16 12 16, gap 10", "[right]12[grow,260!]", "[]"));

        JTextField txtCI = dateField();
        JTextField txtCO = dateField();
        JSpinner spCI = timeSpinner();
        JSpinner spCO = timeSpinner();
        JComboBox<String> cbType = new JComboBox<>(new String[]{"Giờ", "Ngày", "Đêm"});
        styleCombo(cbType);

        JSpinner spUsage = new JSpinner(new SpinnerNumberModel(1, 1, 240, 1));

        RoomMultiBookTableModel.Row currentRow = tableModel.getAllRows().stream()
                .filter(r -> r.getRoomID().equals(roomID))
                .findFirst().orElse(null);

        LocalDateTime checkIn = currentRow != null ? currentRow.getCheckIn() : getDefaultCheckIn();
        LocalDateTime checkOut = currentRow != null ? currentRow.getCheckOut() : getDefaultCheckOut();
        String bookingType = currentRow != null ? currentRow.getBookingType() : String.valueOf(cbxBookingType.getSelectedItem());

        txtCI.setText(checkIn.toLocalDate().format(df));
        setSpinnerTime(spCI, checkIn.toLocalTime());
        txtCO.setText(checkOut.toLocalDate().format(df));
        setSpinnerTime(spCO, checkOut.toLocalTime());
        cbType.setSelectedItem(bookingType);

        d.add(label("Check-in:")); d.add(rowDateTime(txtCI, btnCal(txtCI), spCI), "growx");
        d.add(label("Check-out:")); d.add(rowDateTime(txtCO, btnCal(txtCO), spCO), "growx");
        d.add(label("Kiểu đặt:")); d.add(cbType, "growx");
        d.add(label("Số giờ/đêm:")); d.add(spUsage, "growx");

        JButton btnReset = secondary("Reset mặc định");
        JButton btnSave = primary("Lưu");

        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.setOpaque(false);
        p.add(btnReset);
        p.add(btnSave);
        d.add(p, "span 2, growx");

        btnReset.addActionListener(e -> {
            tableModel.resetOverride(roomID);
            d.dispose();
        });

        btnSave.addActionListener(e -> {
            LocalDate ciDate = parseDate(txtCI.getText());
            LocalDate coDate = parseDate(txtCO.getText());
            if (ciDate == null || coDate == null) {
                JOptionPane.showMessageDialog(d, "Ngày không hợp lệ.");
                return;
            }
            LocalDateTime ci = LocalDateTime.of(ciDate, readTime(spCI));
            LocalDateTime co = LocalDateTime.of(coDate, readTime(spCO));
            tableModel.setOverride(roomID, ci, co, String.valueOf(cbType.getSelectedItem()));
            d.dispose();
        });

        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private void attachAutoFillCustomerByPhone() {
        txtPhone.getDocument().addDocumentListener(new DocumentListener() {
            private void onPhoneChanged() {
                String phone = txtPhone.getText().trim();
                if (!phone.matches("^0\\d{9}$")) {
                    lastAutoPhone = null;
                    return;
                }
                if (phone.equals(lastAutoPhone)) return;

                try {
                    BaseResponse response = sendRequest(CommandType.GET_CUSTOMER_BY_PHONE, phone);
                    lastAutoPhone = phone;

                    if (response.isSuccess()) {
                        CustomerDTO customer = (CustomerDTO) response.getData();
                        if (customer != null) {
                            if (isBlank(txtFullName.getText())) txtFullName.setText(nvl(customer.getFullName()));
                            if (isBlank(txtEmail.getText())) txtEmail.setText(nvl(customer.getEmail()));
                            if (isBlank(txtIdCard.getText())) txtIdCard.setText(nvl(customer.getIdCard()));
                        }
                    }
                } catch (Exception ignored) {
                    // ...
                }
            }

            @Override public void insertUpdate(DocumentEvent e) { onPhoneChanged(); }
            @Override public void removeUpdate(DocumentEvent e) { onPhoneChanged(); }
            @Override public void changedUpdate(DocumentEvent e) { onPhoneChanged(); }
        });
    }

    private void showBookingResultDialog(List<String[]> okRows, List<String[]> errRows) {
        JDialog dialog = new JDialog(this, "Kết quả đặt phòng", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new MigLayout("wrap, fill, insets 14 18 16 18, gapy 10", "[grow,fill]"));
        root.setBackground(BG);

        String titleText;
        if (okRows.isEmpty() && errRows.isEmpty()) titleText = "Không có phòng nào được xử lý.";
        else if (errRows.isEmpty()) titleText = "Đặt thành công " + okRows.size() + " phòng.";
        else if (okRows.isEmpty()) titleText = "Không đặt được phòng nào.";
        else titleText = "Đặt thành công " + okRows.size() + " phòng, lỗi " + errRows.size() + " phòng.";

        JLabel title = new JLabel(titleText);
        title.setForeground(FG);
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        root.add(title, "wrap");

        if (!okRows.isEmpty()) {
            root.add(buildResultTable("Phòng đặt thành công", new String[]{"RoomID", "Check-in", "Check-out", "Kiểu đặt"}, okRows), "growx, wrap");
        }
        if (!errRows.isEmpty()) {
            root.add(buildResultTable("Phòng bị lỗi", new String[]{"RoomID", "Lý do"}, errRows), "growx, wrap");
        }

        JButton btnClose = primary("Đóng");
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.setOpaque(false);
        p.add(btnClose);
        root.add(p, "growx");

        btnClose.addActionListener(e -> dialog.dispose());

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setSize(Math.max(760, dialog.getWidth()), Math.max(420, dialog.getHeight()));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JComponent buildResultTable(String title, String[] columns, List<String[]> rows) {
        JPanel wrap = new JPanel(new BorderLayout(0, 6));
        wrap.setOpaque(false);

        JLabel lb = new JLabel(title);
        lb.setForeground(GOLD);
        lb.setFont(new Font("Segoe UI", Font.BOLD, 13));
        wrap.add(lb, BorderLayout.NORTH);

        String[][] data = rows.toArray(new String[0][]);
        JTable t = new JTable(data, columns);
        t.setRowHeight(26);
        JScrollPane sp = new JScrollPane(t);
        wrap.add(sp, BorderLayout.CENTER);
        return wrap;
    }

    private void applyDefaultsToTable() {
        String bookingType = String.valueOf(cbxBookingType.getSelectedItem());
        tableModel.applyNewDefaults(getDefaultCheckIn(), getDefaultCheckOut(), bookingType);
        table.repaint();
    }

    private LocalDateTime getDefaultCheckIn() {
        LocalDate d = parseDate(txtCheckIn.getText());
        if (d == null) d = LocalDate.now();
        return LocalDateTime.of(d, readTime(spCheckInTime));
    }

    private LocalDateTime getDefaultCheckOut() {
        String bookingType = String.valueOf(cbxBookingType.getSelectedItem());
        if ("Giờ".equalsIgnoreCase(bookingType)) return calcCheckOutForHourType();
        if ("Đêm".equalsIgnoreCase(bookingType)) return calcCheckOutForNightType();

        LocalDate d = parseDate(txtCheckOut.getText());
        if (d == null) d = LocalDate.now();
        return LocalDateTime.of(d, readTime(spCheckOutTime));
    }

    private LocalDateTime calcCheckOutForHourType() {
        LocalDate checkInDate = parseDate(txtCheckIn.getText());
        if (checkInDate == null) return null;
        return LocalDateTime.of(checkInDate, readTime(spCheckInTime)).plusHours((int) spHours.getValue());
    }

    private void updateCheckoutFieldsForHourType() {
        if (!"Giờ".equalsIgnoreCase(String.valueOf(cbxBookingType.getSelectedItem()))) return;
        LocalDateTime co = calcCheckOutForHourType();
        if (co == null) return;
        txtCheckOut.setText(co.toLocalDate().format(df));
        setSpinnerTime(spCheckOutTime, co.toLocalTime());
    }

    private LocalDateTime calcCheckOutForNightType() {
        LocalDate checkInDate = parseDate(txtCheckIn.getText());
        if (checkInDate == null) return null;
        int nights = Math.max((int) spHours.getValue(), 1);
        return LocalDateTime.of(checkInDate.plusDays(nights), LocalTime.of(8, 0));
    }

    private void updateCheckoutFieldsForNightType() {
        if (!"Đêm".equalsIgnoreCase(String.valueOf(cbxBookingType.getSelectedItem()))) return;
        LocalDateTime co = calcCheckOutForNightType();
        if (co == null) return;
        txtCheckOut.setText(co.toLocalDate().format(df));
        setSpinnerTime(spCheckOutTime, co.toLocalTime());
    }

    private void updateTimeByBookingType() {
        String bookingType = String.valueOf(cbxBookingType.getSelectedItem());
        boolean enableTime = !"Ngày".equalsIgnoreCase(bookingType);
        boolean isHourly = "Giờ".equalsIgnoreCase(bookingType);
        boolean isNight = "Đêm".equalsIgnoreCase(bookingType);

        boolean showUsage = isHourly || isNight;
        lblHours.setVisible(showUsage);
        spHours.setVisible(showUsage);
        lblNightNote.setVisible(isNight);

        if (isHourly) lblHours.setText("Số giờ sử dụng:");
        else if (isNight) lblHours.setText("Số đêm ở:");

        txtCheckOut.setEnabled(!isHourly);
        spCheckOutTime.setEnabled(!isHourly && enableTime);
        spCheckInTime.setEnabled(enableTime);

        if (isHourly) updateCheckoutFieldsForHourType();
        else if (isNight) {
            setSpinnerTime(spCheckInTime, LocalTime.of(19, 0));
            updateCheckoutFieldsForNightType();
        } else {
            setSpinnerTime(spCheckInTime, LocalTime.MIDNIGHT);
            setSpinnerTime(spCheckOutTime, LocalTime.MIDNIGHT);
        }

        applyDefaultsToTable();
    }

    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return SocketSessionManager.send(BaseRequest.of(commandType, data));
    }


    private JPanel section(String title) {
        JPanel p = new JPanel(new MigLayout("wrap 2, fillx, insets 12 12 12 12", "[right]12[grow,fill]"));
        p.setBackground(CARD_BG);
        p.setBorder(new LineBorder(BORDER, 1, true));
        JLabel l = new JLabel(title);
        l.setForeground(GOLD);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        p.add(l, "span 2, wrap");
        return p;
    }

    private JLabel label(String s) {
        JLabel l = new JLabel(s);
        l.setForeground(FG);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return l;
    }

    private JTextField text() {
        JTextField t = new JTextField();
        t.setBackground(CARD_BG);
        t.setForeground(FG);
        t.setCaretColor(FG);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        t.setBorder(new LineBorder(BORDER, 1));
        return t;
    }

    private JTextField dateField() {
        JTextField t = text();
        t.setEditable(false);
        return t;
    }

    private JSpinner timeSpinner() {
        SpinnerDateModel model = new SpinnerDateModel();
        JSpinner sp = new JSpinner(model);
        sp.setEditor(new JSpinner.DateEditor(sp, "HH:mm"));
        return sp;
    }

    private JPanel rowDate(JTextField tf, JButton pick) {
        JPanel p = new JPanel(new MigLayout("insets 0, fillx", "[grow]8[]", "[]"));
        p.setOpaque(false);
        p.add(tf, "growx");
        p.add(pick, "w 44!, h 28!");
        return p;
    }

    private JPanel rowDateTime(JTextField tf, JButton pick, JSpinner time) {
        JPanel p = new JPanel(new MigLayout("insets 0, fillx", "[grow]8[]12[90!]", "[]"));
        p.setOpaque(false);
        p.add(tf, "growx");
        p.add(pick, "w 44!, h 28!");
        p.add(time, "h 28!");
        return p;
    }

    private JButton btnCal(JTextField field) {
        JButton b = new JButton("📅");
        b.setBackground(GOLD);
        b.setForeground(Color.BLACK);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));

        b.addActionListener(e -> showDatePopup(field));

        return b;
    }

    private JButton primary(String text) {
        JButton b = new JButton(text);
        b.setBackground(GOLD);
        b.setForeground(new Color(0x0B1F33));
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return b;
    }

    private JButton secondary(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(0x1B4F72));
        b.setForeground(FG);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return b;
    }

    private void styleCombo(JComboBox<?> c) {
        c.setBackground(CARD_BG);
        c.setForeground(FG);
        c.setBorder(new LineBorder(BORDER, 1));
        c.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    private void attachDateChangeListener(JTextField field, Runnable action) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            private void run() {
                SwingUtilities.invokeLater(action);
            }

            @Override public void insertUpdate(DocumentEvent e) { run(); }
            @Override public void removeUpdate(DocumentEvent e) { run(); }
            @Override public void changedUpdate(DocumentEvent e) { run(); }
        });
    }

    private void showDatePopup(JTextField field) {
        LocalDate current = parseDate(field.getText().trim());
        if (current == null) current = LocalDate.now();

        Date initDate = Date.from(current.atStartOfDay(ZoneId.systemDefault()).toInstant());
        SpinnerDateModel model = new SpinnerDateModel(initDate, null, null, Calendar.DAY_OF_MONTH);
        JSpinner spDate = new JSpinner(model);
        spDate.setEditor(new JSpinner.DateEditor(spDate, "dd-MM-yyyy"));
        spDate.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPanel panel = new JPanel(new MigLayout("wrap 1, insets 12, gap 8", "[260!,fill]"));
        panel.add(new JLabel("Chọn ngày:"));
        panel.add(spDate, "growx");

        Window owner = SwingUtilities.getWindowAncestor(field);
        int result = JOptionPane.showConfirmDialog(
                owner == null ? this : owner,
                panel,
                "Chọn ngày",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            Date selected = (Date) spDate.getValue();
            LocalDate date = selected.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            field.setText(date.format(df));
        }
    }


    private LocalDate parseDate(String s) {
        try {
            return LocalDate.parse(s, df);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalTime readTime(JSpinner spinner) {
        Date date = (Date) spinner.getValue();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return LocalTime.of(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
    }

    private void setSpinnerTime(JSpinner spinner, LocalTime time) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, time.getHour());
        cal.set(Calendar.MINUTE, time.getMinute());
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        spinner.setValue(cal.getTime());
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    public static class TableCellRendererButton extends JButton implements TableCellRenderer, TableCellEditor {
        public interface RowAction { void run(int rowView); }

        private final JTable table;
        private final JButton editorButton = new JButton();
        private final RowAction action;
        private int editingRow = -1;

        private TableCellRendererButton(JTable table, String text, RowAction action) {
            this.table = table;
            this.action = action;
            setText(text);
            editorButton.setText(text);
            editorButton.addActionListener(e -> {
                if (editingRow >= 0) action.run(editingRow);
                fireEditingStopped();
            });
        }

        public static void install(JTable table, int col, String text, RowAction action) {
            TableCellRendererButton button = new TableCellRendererButton(table, text, action);
            table.getColumnModel().getColumn(col).setCellRenderer(button);
            table.getColumnModel().getColumn(col).setCellEditor(button);
        }

        @Override public Object getCellEditorValue() { return "Tùy chỉnh"; }
        @Override public boolean isCellEditable(java.util.EventObject e) { return true; }
        @Override public boolean shouldSelectCell(java.util.EventObject e) { return true; }
        @Override public boolean stopCellEditing() { return true; }
        @Override public void cancelCellEditing() {}
        @Override public void addCellEditorListener(javax.swing.event.CellEditorListener l) {}
        @Override public void removeCellEditorListener(javax.swing.event.CellEditorListener l) {}
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) { return this; }
        @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            editingRow = row;
            return editorButton;
        }
        private void fireEditingStopped() {}
    }
}