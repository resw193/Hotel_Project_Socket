package client.presentation.service;

import client.presentation.core.ServiceRegistry;
import common.dto.ServiceDTO;
import net.miginfocom.swing.MigLayout;
import server.core.service.ServiceService;
import client.network.socket.SocketSessionManager;
import common.dto.request_dto.IncreaseServiceQuantityRequestDTO;
import common.protocol.command.CommandType;
import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class FormUpdateQuantityService extends JDialog {

    private final Color BG = new Color(0x0B1F33);
    private final Color FG = new Color(0xE9EEF6);
    private final Color CARD_BG = new Color(0x102D4A);
    private final Color BORDER = new Color(0x274A6B);
    private final Color PRIMARY = new Color(0xF5C452);
    private final Color DANGER = new Color(0xF26B6B);

    private final FormServiceManagement parent;
    private final ServiceDTO currentService;

    private JTextField txtAdd;
    private JLabel lblError;

    public FormUpdateQuantityService(FormServiceManagement parent, ServiceDTO currentService) {
        this.parent = parent;
        this.currentService = currentService;
        initUI();
    }

    private void initUI() {
        setTitle("Thêm số lượng dịch vụ");
        setSize(520, 320);
        setResizable(false);
        setLocationRelativeTo(parent);

        JPanel root = new JPanel();
        root.setBorder(new EmptyBorder(16, 20, 16, 20));
        root.setLayout(new MigLayout("wrap, fillx, insets 0, gapy 12", "[grow,fill]"));
        root.setOpaque(true);
        root.setBackground(BG);

        JLabel lblTitle = new JLabel("THÊM SỐ LƯỢNG DỊCH VỤ", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(FG);
        root.add(lblTitle, "growx");

        JLabel lblSubTitle = new JLabel(currentService.getServiceName(), SwingConstants.CENTER);
        lblSubTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblSubTitle.setForeground(PRIMARY);
        root.add(lblSubTitle, "growx");

        String unitName = currentService.getUnitName() == null ? "" : currentService.getUnitName();

        JLabel lblCurrentQty = new JLabel(
                "Tồn kho hiện tại: " + currentService.getQuantity() + " " + unitName,
                SwingConstants.CENTER
        );
        lblCurrentQty.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblCurrentQty.setForeground(new Color(0xA5B4C5));
        root.add(lblCurrentQty, "growx, gapbottom 6");

        JPanel pnForm = new JPanel(new MigLayout(
                "wrap 2, insets 10 18 8 18, gapx 12, gapy 8",
                "[120::,right]8[150::,grow,fill]"
        ));
        pnForm.setOpaque(false);

        JLabel lblAmount = new JLabel("Số lượng thêm (" + unitName + "):");
        lblAmount.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblAmount.setForeground(FG);
        pnForm.add(lblAmount);

        txtAdd = new JTextField();
        styleField(txtAdd);
        txtAdd.setColumns(8);
        pnForm.add(txtAdd, "growx");

        lblError = new JLabel(" ");
        lblError.setForeground(DANGER);
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pnForm.add(lblError, "span 2, growx");

        root.add(pnForm, "growx");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton btnCancel = secondaryButton("Hủy");
        JButton btnAddQuantity = primaryButton("Thêm");

        actions.add(btnCancel);
        actions.add(btnAddQuantity);
        root.add(actions, "growx");

        setContentPane(root);
        getRootPane().setDefaultButton(btnAddQuantity);

        btnCancel.addActionListener(e -> dispose());

        btnAddQuantity.addActionListener(e -> {
            lblError.setText(" ");

            String text = txtAdd.getText().trim();
            if (!text.matches("^\\d+$")) {
                JOptionPane.showMessageDialog(this, "Số lượng thêm phải là số nguyên dương.");
                txtAdd.requestFocus();
                return;
            }

            int added = Integer.parseInt(text);

            try {
                BaseResponse response = sendRequest(CommandType.INCREASE_SERVICE_QUANTITY, new IncreaseServiceQuantityRequestDTO(currentService.getServiceId(), added));

                if (response.isSuccess()) {
                    JOptionPane.showMessageDialog(this, "Đã cập nhật số lượng dịch vụ.");
                    dispose();
                    parent.loadData();
                }
                else {
                    JOptionPane.showMessageDialog(this, response.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                lblError.setText(ex.getMessage());
            }
        });
    }

    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return SocketSessionManager.send(BaseRequest.of(commandType, data));
    }

    private void styleField(JTextField t) {
        t.setBackground(CARD_BG);
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