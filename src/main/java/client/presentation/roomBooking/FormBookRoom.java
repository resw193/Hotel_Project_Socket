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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class FormBookRoom extends JDialog {

    private final Color BG = new Color(0x0B1F33);
    private final Color FG = new Color(0xE9EEF6);
    private final Color CARD_BG = new Color(0x102D4A);
    private final Color BORDER = new Color(0x274A6B);
    private final Color GOLD = new Color(0xF5C452);

    private final String roomID;
    private final String employeeID;
    private final FormRoomBookingManagement parent;
    private final RoomStayService roomStayService;
    private final CustomerService customerService;

    private JTextField txtRoomID, txtEmployeeID, txtFullName, txtPhone, txtEmail, txtIdCard;
    private JTextField txtBookingDate, txtCheckIn, txtCheckOut;
    private JComboBox<String> cbxBookingType;
    private JSpinner spCheckInTime, spCheckOutTime, spHours;
    private JLabel lblHours, lblNightNote;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private String lastAutoPhone = null;

    public FormBookRoom(String roomID,
                        String employeeID,
                        RoomStayService roomStayService,
                        CustomerService customerService,
                        FormRoomBookingManagement parent) {
        super((Frame) null, "Đặt phòng", true);
        this.roomID = roomID;
        this.employeeID = employeeID;
        this.roomStayService = roomStayService;
        this.customerService = customerService;
        this.parent = parent;
        initUI();
    }

    private void initUI() {
        JPanel pnRoot = new JPanel(new MigLayout(
                "wrap 2, fillx, insets 16 20 16 20, gap 10",
                "[right]15[grow, 360!]"
        ));
        pnRoot.setBackground(BG);

        txtRoomID = roText(roomID);
        txtEmployeeID = roText(employeeID);
        txtFullName = text();
        txtPhone = text();
        txtEmail = text();
        txtIdCard = text();
        txtBookingDate = dateField();
        txtCheckIn = dateField();
        txtCheckOut = dateField();

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
        spHours.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        lblHours = label("Hours of use:");

        lblNightNote = new JLabel("Từ 08:00 đến 19:00 sẽ có phí giữ phòng 20.000 VNĐ/giờ.");
        lblNightNote.setForeground(new Color(0xFCA5A5));
        lblNightNote.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblNightNote.setVisible(false);

        spCheckInTime = timeSpinner();
        spCheckOutTime = timeSpinner();

        cbxBookingType = new JComboBox<>(new String[]{"Giờ", "Ngày", "Đêm"});
        cbxBookingType.setBackground(CARD_BG);
        cbxBookingType.setForeground(FG);
        cbxBookingType.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        cbxBookingType.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        attachDateChangeListener(txtCheckIn, () -> {
            String bt = String.valueOf(cbxBookingType.getSelectedItem());
            if ("Giờ".equalsIgnoreCase(bt)) updateCheckoutFieldsForHourType();
            else if ("Đêm".equalsIgnoreCase(bt)) updateCheckoutFieldsForNightTypeSingle();
        });

        pnRoot.add(label("Room ID:"));             pnRoot.add(txtRoomID, "growx");
        pnRoot.add(label("Employee ID:"));         pnRoot.add(txtEmployeeID, "growx");
        pnRoot.add(label("Full name:"));           pnRoot.add(txtFullName, "growx");
        pnRoot.add(label("Phone:"));               pnRoot.add(txtPhone, "growx");
        pnRoot.add(label("Email:"));               pnRoot.add(txtEmail, "growx");
        pnRoot.add(label("ID Card:"));             pnRoot.add(txtIdCard, "growx");
        pnRoot.add(label("Ngày đặt phòng:"));      pnRoot.add(rowWithPicker(txtBookingDate, btnCalendar(txtBookingDate)), "growx");
        pnRoot.add(label("Check-in date/time:"));  pnRoot.add(rowWithPickerDateTime(txtCheckIn, btnCalendar(txtCheckIn), spCheckInTime), "growx");
        pnRoot.add(lblHours, "hidemode 3");
        pnRoot.add(spHours, "growx, hidemode 3");
        pnRoot.add(lblNightNote, "span 2, gapleft 150, hidemode 3");
        pnRoot.add(label("Check-out date/time:")); pnRoot.add(rowWithPickerDateTime(txtCheckOut, btnCalendar(txtCheckOut), spCheckOutTime), "growx");
        pnRoot.add(label("Kiểu đặt phòng:"));      pnRoot.add(cbxBookingType, "growx");

        JButton btnBookRoom = primaryButton("ĐẶT PHÒNG");
        JButton btnCancel = secondaryButton("HỦY");
        JPanel pnAction = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnAction.setBackground(BG);
        pnAction.add(btnCancel);
        pnAction.add(btnBookRoom);
        pnRoot.add(pnAction, "span 2, growx");

        setContentPane(pnRoot);
        pack();
        setSize(700, getPreferredSize().height + 20);
        setLocationRelativeTo(null);

        btnCancel.addActionListener(e -> dispose());
        btnBookRoom.addActionListener(e -> bookRoom());

        cbxBookingType.addActionListener(e -> goiYBookingType());
        spCheckInTime.addChangeListener(e -> {
            String bt = String.valueOf(cbxBookingType.getSelectedItem());
            if ("Giờ".equalsIgnoreCase(bt)) updateCheckoutFieldsForHourType();
            else if ("Đêm".equalsIgnoreCase(bt)) updateCheckoutFieldsForNightTypeSingle();
        });
        spHours.addChangeListener(e -> {
            String bt = String.valueOf(cbxBookingType.getSelectedItem());
            if ("Giờ".equalsIgnoreCase(bt)) updateCheckoutFieldsForHourType();
            else if ("Đêm".equalsIgnoreCase(bt)) updateCheckoutFieldsForNightTypeSingle();
        });

        attachAutoFillCustomerByPhone();
        goiYBookingType();
    }

    private void bookRoom() {
        String fullName = txtFullName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String idCard = txtIdCard.getText().trim();
        String bookingType = String.valueOf(cbxBookingType.getSelectedItem());

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

        LocalDate booking = parseDate(txtBookingDate.getText().trim());
        LocalDate checkInDate = parseDate(txtCheckIn.getText().trim());
        LocalDate checkOutDate = parseDate(txtCheckOut.getText().trim());

        if (booking == null || checkInDate == null || (!"Giờ".equalsIgnoreCase(bookingType) && checkOutDate == null)) {
            JOptionPane.showMessageDialog(this, "Ngày không hợp lệ, định dạng phải là dd-MM-yyyy.");
            return;
        }

        LocalDateTime bookingDateTime = booking.atStartOfDay();
        LocalDateTime checkInDateTime = LocalDateTime.of(checkInDate, readTime(spCheckInTime));

        LocalDateTime checkOutDateTime;
        if ("Giờ".equalsIgnoreCase(bookingType)) {
            checkOutDateTime = calcCheckOutForHourType();
        } else if ("Đêm".equalsIgnoreCase(bookingType)) {
            checkOutDateTime = calcCheckOutForNightTypeSingle();
        } else {
            checkOutDateTime = LocalDateTime.of(checkOutDate, readTime(spCheckOutTime));
        }

        if (checkOutDateTime == null || !checkOutDateTime.isAfter(checkInDateTime)) {
            JOptionPane.showMessageDialog(this, "Check-out phải sau Check-in.");
            return;
        }
        if (checkInDateTime.toLocalDate().isBefore(booking)) {
            JOptionPane.showMessageDialog(this, "Check-in phải sau hoặc bằng ngày đặt.");
            return;
        }

        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setFullName(txtFullName.getText().trim());
        customerDTO.setPhone(txtPhone.getText().trim());
        customerDTO.setEmail(txtEmail.getText().trim());
        customerDTO.setIdCard(txtIdCard.getText().trim());

        BookRoomRequestDTO requestDTO = new BookRoomRequestDTO(
                customerDTO,
                roomID,
                employeeID,
                bookingDateTime,
                checkInDateTime,
                checkOutDateTime,
                bookingType
        );

        try {
            BaseResponse response = sendRequest(CommandType.BOOK_ROOM, requestDTO);

            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Đặt phòng thành công.");
                dispose();
                if (parent != null) parent.loadData();
            }
            else {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
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

                CustomerDTO customer = customerService.getByPhone(phone);
                lastAutoPhone = phone;

                if (customer != null) {
                    if (isBlank(txtFullName.getText())) txtFullName.setText(nvl(customer.getFullName()));
                    if (isBlank(txtEmail.getText())) txtEmail.setText(nvl(customer.getEmail()));
                    if (isBlank(txtIdCard.getText())) txtIdCard.setText(nvl(customer.getIdCard()));
                }
            }

            @Override public void insertUpdate(DocumentEvent e) { onPhoneChanged(); }
            @Override public void removeUpdate(DocumentEvent e) { onPhoneChanged(); }
            @Override public void changedUpdate(DocumentEvent e) { onPhoneChanged(); }
        });
    }

    private void goiYBookingType() {
        String bookingType = String.valueOf(cbxBookingType.getSelectedItem());
        boolean enableTime = !"Ngày".equalsIgnoreCase(bookingType);

        boolean isHourly = "Giờ".equalsIgnoreCase(bookingType);
        boolean isNight = "Đêm".equalsIgnoreCase(bookingType);

        spCheckInTime.setEnabled(enableTime);
        spCheckOutTime.setEnabled(enableTime);

        boolean showUsage = isHourly || isNight;
        lblHours.setVisible(showUsage);
        spHours.setVisible(showUsage);
        lblNightNote.setVisible(isNight);

        if (isHourly) {
            lblHours.setText("Số giờ sử dụng:");
            if ((int) spHours.getValue() < 1) spHours.setValue(1);
        } else if (isNight) {
            lblHours.setText("Số đêm ở:");
            if ((int) spHours.getValue() < 1) spHours.setValue(1);
        }

        txtCheckOut.setEnabled(!isHourly);
        spCheckOutTime.setEnabled(!isHourly && enableTime);

        if (isBlank(txtCheckIn.getText())) txtCheckIn.setText(LocalDate.now().format(formatter));
        if (isBlank(txtCheckOut.getText())) txtCheckOut.setText(LocalDate.now().format(formatter));
        if (isBlank(txtBookingDate.getText())) txtBookingDate.setText(LocalDate.now().format(formatter));

        if (isHourly) {
            setSpinnerTime(spCheckInTime, LocalTime.now().withSecond(0).withNano(0));
            updateCheckoutFieldsForHourType();
        } else if (isNight) {
            setSpinnerTime(spCheckInTime, LocalTime.of(19, 0));
            updateCheckoutFieldsForNightTypeSingle();
        } else {
            setSpinnerTime(spCheckInTime, LocalTime.of(0, 0));
            setSpinnerTime(spCheckOutTime, LocalTime.of(0, 0));
        }
    }

    private LocalDateTime calcCheckOutForHourType() {
        LocalDate checkInDate = parseDate(txtCheckIn.getText().trim());
        if (checkInDate == null) return null;
        return LocalDateTime.of(checkInDate, readTime(spCheckInTime)).plusHours((int) spHours.getValue());
    }

    private void updateCheckoutFieldsForHourType() {
        if (!"Giờ".equalsIgnoreCase(String.valueOf(cbxBookingType.getSelectedItem()))) return;
        LocalDateTime out = calcCheckOutForHourType();
        if (out == null) return;
        txtCheckOut.setText(out.toLocalDate().format(formatter));
        setSpinnerTime(spCheckOutTime, out.toLocalTime());
    }

    private LocalDateTime calcCheckOutForNightTypeSingle() {
        LocalDate checkInDate = parseDate(txtCheckIn.getText().trim());
        if (checkInDate == null) return null;
        int nights = Math.max((int) spHours.getValue(), 1);
        return LocalDateTime.of(checkInDate.plusDays(nights), LocalTime.of(8, 0));
    }

    private void updateCheckoutFieldsForNightTypeSingle() {
        if (!"Đêm".equalsIgnoreCase(String.valueOf(cbxBookingType.getSelectedItem()))) return;
        LocalDateTime out = calcCheckOutForNightTypeSingle();
        if (out == null) return;
        txtCheckOut.setText(out.toLocalDate().format(formatter));
        setSpinnerTime(spCheckOutTime, out.toLocalTime());
    }

    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return SocketSessionManager.send(BaseRequest.of(commandType, data));
    }


    private JTextField text() {
        JTextField t = new JTextField();
        t.setBackground(CARD_BG);
        t.setForeground(FG);
        t.setCaretColor(FG);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        t.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        return t;
    }

    private JTextField roText(String value) {
        JTextField t = text();
        t.setEditable(false);
        t.setText(value);
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
        JSpinner.DateEditor editor = new JSpinner.DateEditor(sp, "HH:mm");
        sp.setEditor(editor);
        sp.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return sp;
    }

    private JPanel rowWithPicker(JTextField tf, JButton pick) {
        JPanel p = new JPanel(new MigLayout("insets 0, fillx", "[grow]8[]", "[]"));
        p.setBackground(BG);
        p.add(tf, "growx");
        p.add(pick, "w 44!, h 28!");
        return p;
    }

    private JPanel rowWithPickerDateTime(JTextField tf, JButton pick, JSpinner time) {
        JPanel p = new JPanel(new MigLayout("insets 0, fillx", "[grow]8[]12[90!]", "[]"));
        p.setBackground(BG);
        p.add(tf, "growx");
        p.add(pick, "w 44!, h 28!");
        p.add(time, "h 28!");
        return p;
    }

    private JButton btnCalendar(JTextField field) {
        JButton b = new JButton("📅");
        b.setBackground(GOLD);
        b.setForeground(Color.BLACK);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));

        b.addActionListener(e -> showDatePopup(field));

        return b;
    }

    private JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(GOLD);
        b.setForeground(new Color(0x0B1F33));
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return b;
    }

    private JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(0x1B4F72));
        b.setForeground(FG);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return b;
    }

    private JLabel label(String s) {
        JLabel l = new JLabel(s);
        l.setForeground(FG);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return l;
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
            field.setText(date.format(formatter));
        }
    }


    private LocalDate parseDate(String s) {
        try {
            return LocalDate.parse(s, formatter);
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
}