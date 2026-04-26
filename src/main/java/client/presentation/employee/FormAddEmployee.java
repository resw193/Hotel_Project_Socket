package client.presentation.employee;

import server.core.service.EmployeeService;
import server.core.service.EmployeeTypeService;
import com.formdev.flatlaf.FlatClientProperties;
import common.dto.EmployeeDTO;
import common.dto.EmployeeTypeDTO;
import net.miginfocom.swing.MigLayout;
import client.network.socket.SocketSessionManager;
import common.protocol.command.CommandType;
import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.List;

public class FormAddEmployee extends JDialog {

    private FormEmployeeManagement parent;

    private JTextField txtFullName;
    private JTextField txtPhone;
    private JTextField txtEmail;
    private JComboBox<String> cbxType;
    private JRadioButton rdMale;
    private JRadioButton rdFemale;
    private JTextField txtImgPath;

    private List<EmployeeTypeDTO> employeeTypes;

    public FormAddEmployee(Window owner, FormEmployeeManagement parent) {
        super(owner, "Thêm nhân viên", ModalityType.APPLICATION_MODAL);
        this.parent = parent;

        initUI();
        loadEmployeeTypes();
        pack();
        setResizable(false);
    }

    private void initUI() {
        getContentPane().setBackground(new Color(0x0E2942));

        setLayout(new MigLayout(
                "insets 20 28 18 28",
                "[right]18[260,grow,fill]",
                "[][][][][][][]15[]"
        ));
        setPreferredSize(new Dimension(520, 360));

        JLabel lblTitle = new JLabel("Thêm nhân viên");
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

        cbxType = new JComboBox<>();
        cbxType.putClientProperty(FlatClientProperties.STYLE,
                "arc:10; background:#102D4A; foreground:#E9EEF6; " +
                        "borderColor:#274A6B; padding:6,10,6,10;");
        add(label("Loại nhân viên:"), "");
        add(cbxType, "growx, wrap");

        rdMale = new JRadioButton("Nam", true);
        rdFemale = new JRadioButton("Nữ");
        rdMale.setOpaque(false);
        rdFemale.setOpaque(false);
        rdMale.setForeground(new Color(0xE9EEF6));
        rdFemale.setForeground(new Color(0xE9EEF6));

        ButtonGroup groupGender = new ButtonGroup();
        groupGender.add(rdMale);
        groupGender.add(rdFemale);

        JPanel pnGender = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnGender.setOpaque(false);
        pnGender.add(rdMale);
        pnGender.add(rdFemale);

        add(label("Giới tính:"), "");
        add(pnGender, "growx, wrap");

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
        btnSave.addActionListener(e -> saveEmployee());

        add(btnCancel, "span 2, split 2, right, gaptop 10");
        add(btnSave, "gaptop 10");
    }

    private void loadEmployeeTypes() {
        try {
            BaseResponse response = sendRequest(CommandType.GET_ALL_EMPLOYEE_TYPES, null);
            if (!response.isSuccess()) {
                throw new RuntimeException(response.getMessage());
            }

            @SuppressWarnings("unchecked")
            List<EmployeeTypeDTO> list = (List<EmployeeTypeDTO>) response.getData();
            employeeTypes = list;

            cbxType.removeAllItems();
            if (employeeTypes != null) {
                for (EmployeeTypeDTO type : employeeTypes) {
                    cbxType.addItem(type.getTypeName());
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Không tải được loại nhân viên:\n" + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveEmployee() {
        try {
            EmployeeTypeDTO selectedType = getSelectedType();
            if (selectedType == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn loại nhân viên.");
                return;
            }

            EmployeeDTO dto = new EmployeeDTO();
            dto.setFullName(txtFullName.getText().trim());
            dto.setPhone(txtPhone.getText().trim());
            dto.setEmail(txtEmail.getText().trim());
            dto.setGender(rdMale.isSelected());
            dto.setImgSource(txtImgPath.getText().trim());
            dto.setEmployeeTypeId(selectedType.getTypeId());
            dto.setEmployeeTypeName(selectedType.getTypeName());

            BaseResponse response = sendRequest(CommandType.ADD_EMPLOYEE, dto);
            if (!response.isSuccess()) {
                JOptionPane.showMessageDialog(this, response.getMessage());
                return;
            }

            JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công.");
            if (parent != null) parent.loadEmployees();
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private EmployeeTypeDTO getSelectedType() {
        String typeName = (String) cbxType.getSelectedItem();
        if (typeName == null || employeeTypes == null) return null;

        for (EmployeeTypeDTO type : employeeTypes) {
            if (typeName.equalsIgnoreCase(type.getTypeName())) {
                return type;
            }
        }
        return null;
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

    private JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(0xE9EEF6));
        return lbl;
    }

    private void styleTextField(JTextField txt) {
        txt.putClientProperty(FlatClientProperties.STYLE,
                "arc:10; background:#102D4A; foreground:#E9EEF6; " +
                        "borderColor:#274A6B; padding:6,10,6,10;");
    }

    private void stylePrimary(AbstractButton b) {
        b.putClientProperty(FlatClientProperties.STYLE,
                "arc:12; background:#2563EB; foreground:#FFFFFF; borderColor:#1B4F72; " +
                        "hoverBackground:#1D4ED8; focusWidth:1; innerFocusWidth:0; padding:4,12,4,12;");
    }

    private void styleSecondary(AbstractButton b) {
        b.putClientProperty(FlatClientProperties.STYLE,
                "arc:12; background:#102A43; foreground:#E9EEF6; borderColor:#274A6B; " +
                        "hoverBackground:#153C5B; focusWidth:1; innerFocusWidth:0; padding:4,12,4,12;");
    }
}