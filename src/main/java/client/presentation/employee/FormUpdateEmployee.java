package client.presentation.employee;

import com.formdev.flatlaf.FlatClientProperties;
import common.dto.EmployeeDTO;
import client.network.socket.SocketSessionManager;
import common.protocol.command.CommandType;
import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class FormUpdateEmployee extends JDialog {

    private final FormEmployeeManagement parent;
    private final EmployeeDTO employee;

    private JTextField txtFullName;
    private JTextField txtPhone;
    private JTextField txtEmail;
    private JTextField txtImgPath;

    private JComboBox<String> cbxGender;

    private JLabel lblType;

    public FormUpdateEmployee(Window owner, FormEmployeeManagement parent, EmployeeDTO employee) {
        super(owner, "Cập nhật nhân viên", ModalityType.APPLICATION_MODAL);
        this.parent = parent;
        this.employee = cloneEmployee(employee);

        initUI();
        fillData();
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        getContentPane().setBackground(new Color(0x0E2942));

        setLayout(new MigLayout(
                "insets 20 28 18 28",
                "[right]18[280,grow,fill]",
                "[][][][][][][]15[]"
        ));

        setPreferredSize(new Dimension(570, 400));

        JLabel lblTitle = new JLabel("Cập nhật thông tin nhân viên");
        lblTitle.setForeground(new Color(0xE9EEF6));
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 20f));
        add(lblTitle, "span 2, alignx center, wrap 10");

        add(new JSeparator(), "span 2, growx, wrap 15");

        txtFullName = new JTextField();
        styleTextField(txtFullName);
        add(label("Họ tên:"), "");
        add(txtFullName, "growx, wrap");

        txtPhone = new JTextField();
        styleTextField(txtPhone);
        add(label("Số điện thoại:"), "");
        add(txtPhone, "growx, wrap");

        txtEmail = new JTextField();
        styleTextField(txtEmail);
        add(label("Email:"), "");
        add(txtEmail, "growx, wrap");

        cbxGender = new JComboBox<>(new String[]{"Nam", "Nữ"});
        styleComboBox(cbxGender);
        add(label("Giới tính:"), "");
        add(cbxGender, "growx, wrap");

        lblType = new JLabel();
        lblType.setForeground(new Color(0xE9EEF6));
        add(label("Loại nhân viên:"), "");
        add(lblType, "growx, wrap");

        txtImgPath = new JTextField();
        styleTextField(txtImgPath);

        JButton btnChooseImg = new JButton("Chọn ảnh...");
        styleSecondary(btnChooseImg);
        btnChooseImg.addActionListener(e -> chooseImage());

        add(label("Hình ảnh:"), "");
        add(txtImgPath, "split 2, growx");
        add(btnChooseImg, "gapleft 8, wrap");

        JButton btnCancel = new JButton("Hủy");
        JButton btnSave = new JButton("Lưu");

        styleSecondary(btnCancel);
        stylePrimary(btnSave);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> updateEmployee());

        add(btnCancel, "span 2, split 2, right, gaptop 10");
        add(btnSave, "gaptop 10");
    }

    private void fillData() {
        if (employee == null) return;

        txtFullName.setText(nvl(employee.getFullName()));
        txtPhone.setText(nvl(employee.getPhone()));
        txtEmail.setText(nvl(employee.getEmail()));
        txtImgPath.setText(nvl(employee.getImgSource()));

        cbxGender.setSelectedItem(employee.isGender() ? "Nam" : "Nữ");

        lblType.setText(nvl(employee.getEmployeeTypeName()));
    }

    private void updateEmployee() {
        try {
            String fullName = txtFullName.getText().trim();
            String phone = txtPhone.getText().trim();
            String email = txtEmail.getText().trim();
            String imgPath = txtImgPath.getText().trim();

            if (fullName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Họ tên không được rỗng.");
                txtFullName.requestFocus();
                return;
            }

            if (phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Số điện thoại không được rỗng.");
                txtPhone.requestFocus();
                return;
            }

            if (email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Email không được rỗng.");
                txtEmail.requestFocus();
                return;
            }

            employee.setFullName(fullName);
            employee.setPhone(phone);
            employee.setEmail(email);
            employee.setImgSource(imgPath);

            String selectedGender = String.valueOf(cbxGender.getSelectedItem());
            employee.setGender("Nam".equalsIgnoreCase(selectedGender));

            BaseResponse response = sendRequest(CommandType.UPDATE_EMPLOYEE, employee);

            if (!response.isSuccess()) {
                JOptionPane.showMessageDialog(this, response.getMessage());
                return;
            }

            JOptionPane.showMessageDialog(this, "Cập nhật nhân viên thành công.");

            if (parent != null) {
                parent.loadEmployees();
            }

            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return SocketSessionManager.send(BaseRequest.of(commandType, data));
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Hình ảnh", "png", "jpg", "jpeg", "gif"));

        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            txtImgPath.setText(f.getPath().replace("\\", "/"));
        }
    }

    private EmployeeDTO cloneEmployee(EmployeeDTO src) {
        if (src == null) return null;

        EmployeeDTO dto = new EmployeeDTO();
        dto.setEmployeeId(src.getEmployeeId());
        dto.setFullName(src.getFullName());
        dto.setPhone(src.getPhone());
        dto.setEmail(src.getEmail());
        dto.setEmployeeTypeId(src.getEmployeeTypeId());
        dto.setEmployeeTypeName(src.getEmployeeTypeName());
        dto.setImgSource(src.getImgSource());
        dto.setGender(src.isGender());

        return dto;
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }

    private JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(0xE9EEF6));
        return lbl;
    }

    private void styleTextField(JTextField txt) {
        txt.putClientProperty(
                FlatClientProperties.STYLE,
                "arc:10; background:#102D4A; foreground:#E9EEF6; " +
                        "borderColor:#274A6B; padding:6,10,6,10;"
        );
    }

    private void styleComboBox(JComboBox<?> cbx) {
        cbx.putClientProperty(
                FlatClientProperties.STYLE,
                "arc:10; background:#102D4A; foreground:#E9EEF6; " +
                        "borderColor:#274A6B; padding:6,10,6,10;"
        );
        cbx.setForeground(new Color(0xE9EEF6));
        cbx.setBackground(new Color(0x102D4A));
        cbx.setFocusable(false);
    }

    private void stylePrimary(AbstractButton b) {
        b.putClientProperty(
                FlatClientProperties.STYLE,
                "arc:12; background:#2563EB; foreground:#FFFFFF; borderColor:#1B4F72; " +
                        "hoverBackground:#1D4ED8; focusWidth:1; innerFocusWidth:0; padding:4,12,4,12;"
        );
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleSecondary(AbstractButton b) {
        b.putClientProperty(
                FlatClientProperties.STYLE,
                "arc:12; background:#102A43; foreground:#E9EEF6; borderColor:#274A6B; " +
                        "hoverBackground:#153C5B; focusWidth:1; innerFocusWidth:0; padding:4,12,4,12;"
        );
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}