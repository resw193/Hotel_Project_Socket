package client.presentation.service;

import client.presentation.core.ServiceRegistry;
import common.dto.ServiceDTO;
import net.miginfocom.swing.MigLayout;
import server.core.service.ServiceService;
import client.network.socket.SocketSessionManager;
import common.protocol.command.CommandType;
import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class FormAddService extends JDialog {

    private final FormServiceManagement parent;

    private JTextField txtName, txtPrice, txtQty;
    private JComboBox<String> cbxType;
    private JLabel lblPreview, lblError;
    private File fileChosen;

    private static final Color CARD_BG = new Color(0x13385A);
    private static final Color TEXT_PRIMARY = new Color(0xE9EEF6);
    private static final Color GOLD_PRIMARY = new Color(0xF5C452);
    private static final Color ERROR_RED = new Color(0xD64545);

    public FormAddService(FormServiceManagement parent) {
        this.parent = parent;

        setTitle("Thêm dịch vụ mới");
        setModal(true);
        setSize(620, 660);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new MigLayout(
                "wrap 2, fillx, insets 20 30 20 30, gap 12",
                "[140::,right]20[fill, grow]"
        ));
        root.setBorder(new EmptyBorder(20, 20, 20, 20));
        root.setBackground(CARD_BG);

        JLabel lblTitle = new JLabel("Thêm dịch vụ mới", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        root.add(lblTitle, "span, al center, gaptop 4, gapbottom 15");

        lblPreview = new JLabel("No image", SwingConstants.CENTER);
        lblPreview.setPreferredSize(new Dimension(160, 160));
        lblPreview.setOpaque(true);
        lblPreview.setBackground(new Color(0x102C49));
        lblPreview.setForeground(TEXT_PRIMARY);
        lblPreview.setBorder(BorderFactory.createDashedBorder(new Color(0x355C7D)));

        JButton btnChooseImage = new JButton("Chọn ảnh");
        stylePrimary(btnChooseImage, false);

        root.add(new JLabel("Hình ảnh:"), "gapbottom 4");
        root.add(lblPreview, "wrap, h 160!, al center");
        root.add(new JLabel(""));
        root.add(btnChooseImage, "wrap, al center");

        txtName = new JTextField();
        txtPrice = new JTextField();
        txtQty = new JTextField();
        cbxType = new JComboBox<>(new String[]{"Food", "Drink", "Laundry", "Health"});

        styleField(txtName);
        styleField(txtPrice);
        styleField(txtQty);
        styleCombo(cbxType);

        addLabeledField(root, "Tên dịch vụ:", txtName);
        addLabeledField(root, "Loại dịch vụ:", cbxType);
        addLabeledField(root, "Giá (VND):", txtPrice);
        addLabeledField(root, "Số lượng:", txtQty);

        lblError = new JLabel(" ");
        lblError.setForeground(ERROR_RED);
        root.add(lblError, "span, growx, gaptop 6");

        JButton btnAdd = new JButton("Thêm dịch vụ");
        JButton btnCancel = new JButton("Hủy");
        stylePrimary(btnAdd, true);
        styleDanger(btnCancel);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 6));
        btnPanel.setOpaque(false);
        btnPanel.add(btnAdd);
        btnPanel.add(btnCancel);

        root.add(btnPanel, "span, al center, gaptop 15, wrap");
        setContentPane(root);

        btnChooseImage.addActionListener(e -> chooseImage());
        btnCancel.addActionListener(e -> dispose());
        btnAdd.addActionListener(e -> addService());
    }

    private void addLabeledField(JPanel panel, String label, JComponent field) {
        JLabel lbl = new JLabel(label);
        lbl.setForeground(TEXT_PRIMARY);
        panel.add(lbl, "gapbottom 4");
        panel.add(field, "wrap");
    }

    private void chooseImage() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "jpeg", "png", "gif", "bmp"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            fileChosen = fc.getSelectedFile();
            Image img = new ImageIcon(fileChosen.getAbsolutePath()).getImage();
            lblPreview.setText("");
            lblPreview.setIcon(new ImageIcon(img.getScaledInstance(160, 160, Image.SCALE_SMOOTH)));
        }
    }

    private void addService() {
        if (!validData()) return;

        try {
            ServiceDTO dto = new ServiceDTO();
            dto.setServiceName(txtName.getText().trim());
            dto.setServiceType((String) cbxType.getSelectedItem());
            dto.setQuantity(Integer.parseInt(txtQty.getText().trim()));
            dto.setPrice(Double.parseDouble(txtPrice.getText().trim()));
            dto.setImgSource(fileChosen != null ? fileChosen.getAbsolutePath().replace("\\", "/") : "");

            BaseResponse response = sendRequest(CommandType.ADD_SERVICE, dto);
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Thêm dịch vụ thành công!");
                parent.loadData();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage());
            }
        } catch (Exception ex) {
            lblError.setText("Lỗi: " + ex.getMessage());
        }
    }

    private boolean validData() {
        lblError.setText(" ");

        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên dịch vụ không được để trống");
            txtName.requestFocus();
            return false;
        }

        try {
            double gia = Double.parseDouble(txtPrice.getText().trim());
            if (gia <= 0) {
                JOptionPane.showMessageDialog(this, "Giá phải > 0");
                txtPrice.requestFocus();
                return false;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Giá phải là số");
            txtPrice.requestFocus();
            return false;
        }

        if (txtQty.getText().trim().isEmpty() || !txtQty.getText().trim().matches("^\\d+$")) {
            JOptionPane.showMessageDialog(this, "Số lượng phải khác rỗng và là số nguyên");
            txtQty.requestFocus();
            return false;
        }

        if (fileChosen == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ảnh dịch vụ");
            return false;
        }

        return true;
    }

    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return SocketSessionManager.send(BaseRequest.of(commandType, data));
    }

    private void styleField(JTextField field) {
        field.setBackground(new Color(0x102C49));
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(GOLD_PRIMARY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x274A6B)),
                new EmptyBorder(6, 8, 6, 8)
        ));
    }

    private void styleCombo(JComboBox<?> combo) {
        combo.setBackground(new Color(0x102C49));
        combo.setForeground(TEXT_PRIMARY);
        combo.setBorder(BorderFactory.createLineBorder(new Color(0x274A6B)));
    }

    private void stylePrimary(JButton btn, boolean solid) {
        btn.setBackground(solid ? new Color(0xF5C452) : new Color(0x2563EB));
        btn.setForeground(solid ? new Color(0x0B1F33) : Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
    }

    private void styleDanger(JButton btn) {
        btn.setBackground(new Color(0xD64545));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
    }
}