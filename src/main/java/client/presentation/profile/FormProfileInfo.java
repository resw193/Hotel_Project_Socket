package client.presentation.profile;

import client.network.socket.SocketSessionManager;
import client.presentation.login.main.Application;
import com.formdev.flatlaf.FlatClientProperties;
import common.dto.EmployeeDTO;
import common.dto.request_dto.ChangePasswordRequestDTO;
import common.protocol.command.CommandType;
import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;
import net.miginfocom.swing.MigLayout;
import server.core.service.EmployeeService;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class FormProfileInfo extends JPanel {

    private final EmployeeService employeeService;

    private JPanel panel;
    private JLabel lblTitle;
    private JLabel lblAvatar;

    private JTextField txtEmployeeID;
    private JTextField txtFullName;
    private JComboBox<String> cbGender;
    private JTextField txtPhone;
    private JTextField txtEmail;
    private JTextField txtTypeName;

    private JButton btnUpdate;
    private JButton btnChangePassword;

    private BufferedImage bgImage;
    private EmployeeDTO currentEmployee;

    private static final Color PANEL_BG = new Color(0x0F2A47);
    private static final Color FIELD_BG = new Color(0x12355A);
    private static final Color TEXT = new Color(0xEAF2FF);
    private static final Color GOLD = new Color(0xF2C94C);
    private static final Color BLUE = new Color(0x2563EB);
    private static final Color CYAN = new Color(0x0EA5E9);
    private static final Color BORDER = new Color(0x274A6B);

    public FormProfileInfo(EmployeeService employeeService) {
        this.employeeService = employeeService;

        setLayout(new BorderLayout());
        initGUI();
        loadEmployeeInformation();
        add(panel, BorderLayout.CENTER);
    }

    private void initGUI() {
        panel = new JPanel(new MigLayout(
                "wrap 2, fillx, insets 22 70 22 70, gap 18 20",
                "[grow 0,trail]20[fill,grow]"
        ));
        panel.putClientProperty(FlatClientProperties.STYLE,
                "arc:18;background:#0F2A47;foreground:#EAF2FF;");

        lblTitle = new JLabel("Thông tin cá nhân");
        lblTitle.putClientProperty(FlatClientProperties.STYLE,
                "font:bold +6;foreground:#F2C94C");

        lblAvatar = new JLabel();
        lblAvatar.setHorizontalAlignment(SwingConstants.CENTER);
        lblAvatar.setPreferredSize(new Dimension(140, 140));

        txtEmployeeID = roField();
        txtFullName = editField();
        cbGender = genderCombo();
        txtPhone = editField();
        txtEmail = editField();
        txtTypeName = roField();

        JPanel pnHeader = new JPanel(new MigLayout("wrap, insets 0 0 12 0", "[grow]", "[]10[]"));
        pnHeader.setOpaque(false);
        pnHeader.add(lblTitle, "al left");
        pnHeader.add(lblAvatar, "al center");

        panel.add(pnHeader, "span 2, growx, wrap");

        panel.add(label("EmployeeID:"));
        panel.add(txtEmployeeID, "growx");

        panel.add(label("Họ tên:"));
        panel.add(txtFullName, "growx");

        panel.add(label("Giới tính:"));
        panel.add(cbGender, "growx");

        panel.add(label("Số điện thoại:"));
        panel.add(txtPhone, "growx");

        panel.add(label("Email:"));
        panel.add(txtEmail, "growx");

        panel.add(label("Loại nhân viên:"));
        panel.add(txtTypeName, "growx");

        btnUpdate = primaryButton("CẬP NHẬT THÔNG TIN");
        btnChangePassword = secondaryButton("ĐỔI MẬT KHẨU");

        JPanel pnButton = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnButton.setOpaque(false);
        pnButton.add(btnChangePassword);
        pnButton.add(btnUpdate);

        panel.add(pnButton, "span 2, growx, gaptop 12");

        btnUpdate.addActionListener(e -> updateProfile());
        btnChangePassword.addActionListener(e -> openChangePasswordDialog());
    }

    private void loadEmployeeInformation() {
        try {
            String username = Application.session.getUsername();
            EmployeeDTO e = employeeService != null
                    ? employeeService.getByUsername(username)
                    : null;

            if (e == null && Application.session.getEmployee() != null) {
                String employeeId = Application.session.getEmployee().getEmployeeId();
                BaseResponse response = sendRequest(CommandType.GET_EMPLOYEE_BY_ID, employeeId);
                if (response.isSuccess()) {
                    e = (EmployeeDTO) response.getData();
                }
            }

            if (e == null) {
                JOptionPane.showMessageDialog(this,
                        "Không tìm thấy thông tin nhân viên đang đăng nhập.",
                        "Thông báo",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            currentEmployee = e;

            txtEmployeeID.setText(nvl(e.getEmployeeId()));
            txtFullName.setText(nvl(e.getFullName()));
            cbGender.setSelectedItem(e.isGender() ? "Nam" : "Nữ");
            txtPhone.setText(nvl(e.getPhone()));
            txtEmail.setText(nvl(e.getEmail()));
            txtTypeName.setText(nvl(e.getEmployeeTypeName()));

            displayImage(e.getImgSource());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Không tải được thông tin cá nhân:\n" + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateProfile() {
        try {
            if (currentEmployee == null) {
                JOptionPane.showMessageDialog(this, "Chưa có dữ liệu nhân viên để cập nhật.");
                return;
            }

            String employeeId = txtEmployeeID.getText().trim();
            String fullName = txtFullName.getText().trim();
            String phone = txtPhone.getText().trim();
            String email = txtEmail.getText().trim();
            String genderText = String.valueOf(cbGender.getSelectedItem());

            if (employeeId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Thiếu mã nhân viên.");
                return;
            }

            if (fullName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Họ tên không được rỗng.");
                txtFullName.requestFocus();
                return;
            }

            if (!phone.matches("^0\\d{9}$")) {
                JOptionPane.showMessageDialog(this, "Số điện thoại phải có 10 số và bắt đầu bằng 0.");
                txtPhone.requestFocus();
                return;
            }

            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                JOptionPane.showMessageDialog(this, "Email không hợp lệ.");
                txtEmail.requestFocus();
                return;
            }

            EmployeeDTO dto = new EmployeeDTO();
            dto.setEmployeeId(employeeId);
            dto.setFullName(fullName);
            dto.setPhone(phone);
            dto.setEmail(email);
            dto.setGender("Nam".equalsIgnoreCase(genderText));

            dto.setEmployeeTypeId(currentEmployee.getEmployeeTypeId());
            dto.setEmployeeTypeName(currentEmployee.getEmployeeTypeName());
            dto.setImgSource(currentEmployee.getImgSource());

            BaseResponse response = sendRequest(CommandType.UPDATE_EMPLOYEE, dto);

            if (!response.isSuccess()) {
                JOptionPane.showMessageDialog(this,
                        response.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            currentEmployee = dto;

            if (Application.session != null) {
                Application.session.setEmployee(dto);
            }

            JOptionPane.showMessageDialog(this, "Cập nhật thông tin cá nhân thành công.");
            loadEmployeeInformation();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi cập nhật thông tin:\n" + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openChangePasswordDialog() {
        if (currentEmployee == null || isBlank(currentEmployee.getEmployeeId())) {
            JOptionPane.showMessageDialog(this, "Không xác định được nhân viên đang đăng nhập.");
            return;
        }

        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Đổi mật khẩu",
                Dialog.ModalityType.APPLICATION_MODAL
        );

        JPanel root = new JPanel(new MigLayout(
                "wrap 2, fillx, insets 18 22 18 22, gap 12 14",
                "[right]14[grow,fill]"
        ));
        root.setBackground(PANEL_BG);
        root.setBorder(new LineBorder(BORDER, 1));

        JPasswordField txtOldPassword = passwordField();
        JPasswordField txtNewPassword = passwordField();
        JPasswordField txtConfirmPassword = passwordField();

        root.add(label("Mật khẩu cũ:"));
        root.add(txtOldPassword, "growx");

        root.add(label("Mật khẩu mới:"));
        root.add(txtNewPassword, "growx");

        root.add(label("Xác nhận mật khẩu:"));
        root.add(txtConfirmPassword, "growx");

        JButton btnSave = primaryButton("XÁC NHẬN ĐỔI");
        JButton btnCancel = secondaryButton("HỦY");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(btnCancel);
        actions.add(btnSave);

        root.add(actions, "span 2, growx, gaptop 8");

        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            String oldPass = new String(txtOldPassword.getPassword()).trim();
            String newPass = new String(txtNewPassword.getPassword()).trim();
            String confirmPass = new String(txtConfirmPassword.getPassword()).trim();

            if (oldPass.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập mật khẩu cũ.");
                txtOldPassword.requestFocus();
                return;
            }

            if (newPass.length() < 8) {
                JOptionPane.showMessageDialog(dialog, "Mật khẩu mới phải tối thiểu 8 ký tự.");
                txtNewPassword.requestFocus();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(dialog, "Xác nhận mật khẩu không khớp.");
                txtConfirmPassword.requestFocus();
                return;
            }

            ChangePasswordRequestDTO dto = new ChangePasswordRequestDTO(
                    currentEmployee.getEmployeeId(),
                    oldPass,
                    newPass,
                    confirmPass
            );

            BaseResponse response = sendRequest(CommandType.CHANGE_PASSWORD, dto);

            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(dialog, "Đổi mật khẩu thành công.");
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog,
                        response.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setSize(480, dialog.getPreferredSize().height + 20);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return SocketSessionManager.send(BaseRequest.of(commandType, data));
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.putClientProperty(FlatClientProperties.STYLE,
                "foreground:#D9E6FF;font:+1");
        return l;
    }

    private JTextField roField() {
        JTextField t = baseField();
        t.setEditable(false);
        t.setFocusable(false);
        t.putClientProperty(FlatClientProperties.STYLE,
                "background:#12355A;foreground:#B8C4D4;borderWidth:0;arc:14;padding:10,14,10,14;font:+1");
        return t;
    }

    private JTextField editField() {
        JTextField t = baseField();
        t.setEditable(true);
        t.putClientProperty(FlatClientProperties.STYLE,
                "background:#12355A;foreground:#EAF2FF;borderWidth:0;arc:14;padding:10,14,10,14;font:+1");
        return t;
    }

    private JTextField baseField() {
        JTextField t = new JTextField();
        t.setCaretColor(TEXT);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return t;
    }

    private JComboBox<String> genderCombo() {
        JComboBox<String> cb = new JComboBox<>(new String[]{"Nam", "Nữ"});
        cb.setBackground(FIELD_BG);
        cb.setForeground(TEXT);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cb.setBorder(new LineBorder(BORDER, 1));
        return cb;
    }

    private JPasswordField passwordField() {
        JPasswordField p = new JPasswordField();
        p.setBackground(FIELD_BG);
        p.setForeground(TEXT);
        p.setCaretColor(TEXT);
        p.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        return p;
    }

    private JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(BLUE);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0x1D4ED8), 1),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        return b;
    }

    private JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(CYAN);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0x0284C7), 1),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        return b;
    }

    private String nvl(String v) {
        return v == null ? "" : v;
    }

    private boolean isBlank(String v) {
        return v == null || v.trim().isEmpty();
    }

    private void displayImage(String imagePath) {
        try {
            if (imagePath == null || imagePath.trim().isEmpty()) {
                lblAvatar.setIcon(null);
                return;
            }

            Image img = null;
            File f = new File(imagePath);
            if (f.exists()) {
                img = ImageIO.read(f);
            } else {
                java.net.URL u = getClass().getResource(imagePath.startsWith("/") ? imagePath : "/" + imagePath);
                if (u != null) img = ImageIO.read(u);
            }

            if (img != null) {
                Image scaled = img.getScaledInstance(140, 140, Image.SCALE_SMOOTH);
                lblAvatar.setIcon(new ImageIcon(scaled));
            } else {
                lblAvatar.setIcon(null);
            }
        } catch (Exception e) {
            lblAvatar.setIcon(null);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        if (bgImage != null) {
            g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
        } else {
            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(10, 34, 55),
                    getWidth(), getHeight(), new Color(20, 60, 100)
            );
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
        g2.dispose();
    }
}