package client.presentation.customer;

import client.presentation.core.ServiceRegistry;
import common.dto.CustomerDTO;
import net.miginfocom.swing.MigLayout;
import server.core.service.CustomerService;
import client.network.socket.SocketSessionManager;
import common.protocol.command.CommandType;
import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class FormUpdateCustomer extends JDialog {

    private static final Color BG = new Color(0x0B1F33);
    private static final Color BORDER = new Color(0x274A6B);
    private static final Color TEXT = new Color(0xE6F1FF);
    private static final Color ACCENT = new Color(0x22D3EE);
    private static final Font BASE_FONT = new Font("Segoe UI", Font.PLAIN, 13);

    private final String customerID;

    private JTextField txtName, txtPhone, txtEmail;

    public FormUpdateCustomer(Window owner, String customerID) {
        super(owner, "Cập nhật khách hàng – " + customerID, ModalityType.APPLICATION_MODAL);
        this.customerID = customerID;

        setSize(520, 260);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new MigLayout("wrap, insets 12, gap 10", "[grow,fill][grow,fill]", "[][][]push[]"));
        root.setBackground(BG);
        root.setBorder(new LineBorder(BORDER));
        add(root);

        txtName = textField();
        txtPhone = textField();
        txtEmail = textField();

        root.add(label("Họ tên")); root.add(txtName);
        root.add(label("Số điện thoại")); root.add(txtPhone);
        root.add(label("Email")); root.add(txtEmail);

        JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "[grow][]12[]", "[]"));
        buttonPanel.setBackground(BG);

        JButton btnSave = primaryButton("Lưu", true);
        JButton btnCancel = primaryButton("Hủy", false);

        buttonPanel.add(btnSave, "skip, w 100!, h 36!");
        buttonPanel.add(btnCancel, "w 100!, h 36!");

        root.add(buttonPanel, "span 2, growx, gaptop 8");

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> update());

        loadData();
    }

    private void loadData() {
        try {
            BaseResponse response = sendRequest(CommandType.GET_CUSTOMER_BY_ID, customerID);

            if (!response.isSuccess()) {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                dispose();
                return;
            }

            CustomerDTO c = (CustomerDTO) response.getData();
            if (c == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy khách hàng!");
                dispose();
                return;
            }

            txtName.setText(c.getFullName());
            txtPhone.setText(c.getPhone());
            txtEmail.setText(c.getEmail());

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    private void update() {
        try {
            CustomerDTO dto = new CustomerDTO();
            dto.setCustomerId(customerID);
            dto.setFullName(txtName.getText().trim());
            dto.setPhone(txtPhone.getText().trim());
            dto.setEmail(txtEmail.getText().trim());

            BaseResponse response = sendRequest(CommandType.UPDATE_CUSTOMER, dto);

            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                dispose();
            } else {
                msg(response.getMessage());
            }
        } catch (Exception ex) {
            msg(ex.getMessage());
        }
    }

    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return SocketSessionManager.send(BaseRequest.of(commandType, data));
    }

    private JLabel label(String s) {
        JLabel l = new JLabel(s);
        l.setForeground(ACCENT);
        l.setFont(BASE_FONT.deriveFont(Font.BOLD));
        return l;
    }

    private JTextField textField() {
        JTextField f = new JTextField();
        f.setFont(BASE_FONT);
        f.setForeground(TEXT);
        f.setBackground(new Color(0x102A43));
        f.setCaretColor(TEXT);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        return f;
    }

    private JButton primaryButton(String text, boolean solid) {
        JButton b = new JButton(text);
        b.setFont(BASE_FONT.deriveFont(Font.BOLD));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(new Color(0x1B4F72)));
        b.setBackground(solid ? new Color(0x2563EB) : new Color(0x0EA5E9));
        b.setOpaque(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void msg(String s) {
        JOptionPane.showMessageDialog(this, s);
    }
}