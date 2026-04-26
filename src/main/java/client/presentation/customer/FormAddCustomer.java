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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FormAddCustomer extends JDialog {

    private static final Color BG = new Color(0x0B1F33);
    private static final Color BORDER = new Color(0x274A6B);
    private static final Color TEXT = new Color(0xE6F1FF);
    private static final Color ACCENT = new Color(0x22D3EE);
    private static final Font BASE_FONT = new Font("Segoe UI", Font.PLAIN, 13);

    private JTextField txtName, txtPhone, txtEmail, txtCCCD;
    private JLabel lblRegisDate, lblLoyalty;

    public FormAddCustomer(Window owner) {
        super(owner, "Thêm khách hàng", ModalityType.APPLICATION_MODAL);

        setSize(520, 360);
        setLocationRelativeTo(owner);

        JPanel panelRoot = new JPanel(new MigLayout("wrap, insets 12, gap 10", "[grow,fill][grow,fill]", "[][][][][]push[]"));
        panelRoot.setBackground(BG);
        panelRoot.setBorder(new LineBorder(BORDER));
        add(panelRoot);

        txtName = textField();
        txtPhone = textField();
        txtEmail = textField();
        txtCCCD = textField();

        lblRegisDate = labelValue(nowStr());
        lblLoyalty = labelValue("0");

        panelRoot.add(label("Họ tên")); panelRoot.add(txtName);
        panelRoot.add(label("Số điện thoại")); panelRoot.add(txtPhone);
        panelRoot.add(label("Email")); panelRoot.add(txtEmail);
        panelRoot.add(label("CCCD")); panelRoot.add(txtCCCD);
        panelRoot.add(label("Ngày đăng ký")); panelRoot.add(lblRegisDate);
        panelRoot.add(label("Điểm thân thiết")); panelRoot.add(lblLoyalty);

        JPanel buttonPanel = new JPanel(new MigLayout("insets 0", "[grow][]12[]", "[]"));
        buttonPanel.setBackground(BG);

        JButton btnSave = primaryButton("Lưu", true);
        JButton btnCancel = primaryButton("Hủy", false);

        buttonPanel.add(btnSave, "skip, w 100!, h 36!");
        buttonPanel.add(btnCancel, "w 100!, h 36!");

        panelRoot.add(buttonPanel, "span 2, growx, gaptop 8");

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> addCustomer());
    }

    private void addCustomer() {
        try {
            CustomerDTO dto = new CustomerDTO();
            dto.setFullName(txtName.getText().trim());
            dto.setPhone(txtPhone.getText().trim());
            dto.setEmail(txtEmail.getText().trim());
            dto.setIdCard(txtCCCD.getText().trim());

            BaseResponse response = sendRequest(CommandType.ADD_CUSTOMER, dto);

            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Thêm khách hàng thành công!");
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

    private JLabel labelValue(String s) {
        JLabel l = new JLabel(s);
        l.setForeground(TEXT);
        l.setFont(BASE_FONT);
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

    private static String nowStr() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private void msg(String s) {
        JOptionPane.showMessageDialog(this, s);
    }
}