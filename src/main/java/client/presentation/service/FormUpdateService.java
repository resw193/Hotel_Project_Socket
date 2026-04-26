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

public class FormUpdateService extends JDialog {

    private final Color BG = new Color(0x0B1F33);
    private final Color FG = new Color(0xE9EEF6);
    private final Color BORDER = new Color(0x274A6B);
    private final Color PRIMARY = new Color(0xF5C452);
    private final Color DANGER = new Color(0xF26B6B);

    private final FormServiceManagement parent;
    private final ServiceDTO currentService;

    private JTextField txtName, txtPrice;
    private JLabel lblError;
    private JLabel lblImagePreview;
    private File selectedImageFile;

    public FormUpdateService(FormServiceManagement parent, ServiceDTO currentService) {
        this.parent = parent;
        this.currentService = currentService;
        initUI();
    }

    private void initUI() {
        setTitle("Cập nhật dịch vụ");
        setSize(640, 480);
        setResizable(false);
        setLocationRelativeTo(parent);

        JPanel root = new JPanel();
        root.setBorder(new EmptyBorder(16, 20, 16, 20));
        root.setLayout(new MigLayout("wrap, fillx, insets 0, gapy 12", "[grow,fill]"));
        root.setOpaque(true);
        root.setBackground(BG);

        JLabel lblTitle = new JLabel("CẬP NHẬT THÔNG TIN DỊCH VỤ", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(FG);
        root.add(lblTitle, "growx");

        JLabel lblSubTitle = new JLabel(currentService.getServiceName(), SwingConstants.CENTER);
        lblSubTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblSubTitle.setForeground(PRIMARY);
        root.add(lblSubTitle, "growx, gapbottom 4");

        JPanel pnForm = new JPanel(new MigLayout(
                "wrap 2, insets 10 18 10 18, gapx 12, gapy 8",
                "[130::,right]8[260::,grow,fill]"
        ));
        pnForm.setOpaque(false);

        JLabel lblName = new JLabel("Tên dịch vụ:");
        lblName.setForeground(FG);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pnForm.add(lblName);

        txtName = new JTextField(currentService.getServiceName());
        styleField(txtName);
        pnForm.add(txtName, "growx");

        JLabel lblPrice = new JLabel("Giá:");
        lblPrice.setForeground(FG);
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pnForm.add(lblPrice);

        txtPrice = new JTextField(String.valueOf(currentService.getPrice()));
        styleField(txtPrice);

        JPanel pnPriceRow = new JPanel(new MigLayout("insets 0, fillx", "[grow]8[]", "[]"));
        pnPriceRow.setOpaque(false);
        pnPriceRow.add(txtPrice, "growx");

        JLabel lblCurrency = new JLabel("VNĐ");
        lblCurrency.setForeground(new Color(0xA5B4C5));
        pnPriceRow.add(lblCurrency);

        pnForm.add(pnPriceRow, "growx");

        JLabel lblImg = new JLabel("Ảnh dịch vụ:");
        lblImg.setForeground(FG);
        lblImg.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pnForm.add(lblImg);

        JPanel imgPanel = new JPanel(new MigLayout("insets 0, gapy 6", "[grow,center]", "[][grow]"));
        imgPanel.setOpaque(false);

        lblImagePreview = new JLabel("Không có ảnh", SwingConstants.CENTER);
        lblImagePreview.setPreferredSize(new Dimension(150, 90));
        lblImagePreview.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        lblImagePreview.setForeground(new Color(0xA5B4C5));

        updateImagePreviewFromPath(currentService.getImgSource());

        JButton btnChangeImage = secondaryButton("Đổi ảnh");
        btnChangeImage.addActionListener(e -> chooseNewImage());

        imgPanel.add(lblImagePreview, "growx, wrap");
        imgPanel.add(btnChangeImage, "center");

        pnForm.add(imgPanel, "growx");

        JLabel lblQty = new JLabel(
                "Số lượng hiện tại trong kho: " + currentService.getQuantity()
        );
        lblQty.setForeground(new Color(0xA5B4C5));
        lblQty.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnForm.add(lblQty, "span 2, growx");

        lblError = new JLabel(" ");
        lblError.setForeground(DANGER);
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnForm.add(lblError, "span 2, growx");

        root.add(pnForm, "growx");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton btnCancel = secondaryButton("Hủy");
        JButton btnUpdateService = primaryButton("Cập nhật");

        actions.add(btnCancel);
        actions.add(btnUpdateService);
        root.add(actions, "growx");

        setContentPane(root);
        getRootPane().setDefaultButton(btnUpdateService);

        btnCancel.addActionListener(e -> dispose());

        btnUpdateService.addActionListener(e -> {
            lblError.setText(" ");

            String name = txtName.getText().trim();
            String priceText = txtPrice.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tên dịch vụ không được để trống.");
                txtName.requestFocus();
                return;
            }

            double gia;
            try {
                gia = Double.parseDouble(priceText);
                if (gia <= 0) {
                    JOptionPane.showMessageDialog(this, "Giá phải > 0.");
                    txtPrice.requestFocus();
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Giá phải là số.");
                txtPrice.requestFocus();
                return;
            }

            try {
                ServiceDTO dto = new ServiceDTO();
                dto.setServiceId(currentService.getServiceId());
                dto.setServiceName(name);
                dto.setServiceType(currentService.getServiceType());
                dto.setQuantity(currentService.getQuantity());
                dto.setPrice(gia);
                dto.setImgSource(selectedImageFile != null
                        ? selectedImageFile.getAbsolutePath().replace("\\", "/")
                        : currentService.getImgSource());

                BaseResponse response = sendRequest(CommandType.UPDATE_SERVICE, dto);
                if (response.isSuccess()) {
                    JOptionPane.showMessageDialog(this, "Đã cập nhật dịch vụ.");
                    dispose();
                    parent.loadData();
                } else {
                    JOptionPane.showMessageDialog(this, response.getMessage());
                }
            } catch (Exception ex) {
                lblError.setText(ex.getMessage());
            }
        });
    }

    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return SocketSessionManager.send(BaseRequest.of(commandType, data));
    }

    private void chooseNewImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn ảnh dịch vụ");
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Ảnh (.png, .jpg, .jpeg, .gif)", "png", "jpg", "jpeg", "gif"
        ));

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = chooser.getSelectedFile();
            updateImagePreviewFromPath(selectedImageFile.getAbsolutePath());
        }
    }

    private void updateImagePreviewFromPath(String path) {
        try {
            if (path == null || path.isBlank()) {
                lblImagePreview.setText("Không có ảnh");
                lblImagePreview.setIcon(null);
                return;
            }

            Image img = null;
            File f = new File(path);
            if (f.exists()) {
                img = new ImageIcon(f.getAbsolutePath()).getImage();
            } else {
                java.net.URL u = getClass().getResource(path.startsWith("/") ? path : "/" + path);
                if (u != null) img = new ImageIcon(u).getImage();
            }

            if (img != null) {
                lblImagePreview.setText("");
                lblImagePreview.setIcon(new ImageIcon(img.getScaledInstance(150, 90, Image.SCALE_SMOOTH)));
            } else {
                lblImagePreview.setText("Không có ảnh");
                lblImagePreview.setIcon(null);
            }
        } catch (Exception e) {
            lblImagePreview.setText("Không có ảnh");
            lblImagePreview.setIcon(null);
        }
    }

    private void styleField(JTextField t) {
        t.setBackground(new Color(0x102D4A));
        t.setForeground(FG);
        t.setCaretColor(FG);
        t.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(4, 6, 4, 6)
        ));
    }

    private JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(PRIMARY);
        b.setForeground(new Color(0x0B1F33));
        b.setFocusPainted(false);
        return b;
    }

    private JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(0x2A4365));
        b.setForeground(FG);
        b.setFocusPainted(false);
        return b;
    }
}