package client.presentation.room;

import client.network.socket.HotelClient;
import client.network.socket.SocketRequestExecutor;
import client.network.socket.SocketSessionManager;
import client.presentation.login.main.Application;
import com.formdev.flatlaf.FlatClientProperties;
import common.dto.RoomTypeDTO;
import common.protocol.command.CommandType;
import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;
import net.miginfocom.swing.MigLayout;
import server.core.service.RoomTypeService;

import javax.swing.*;
import java.awt.*;

public class FormUpdateRoomTypePricing extends JDialog {

    private final RoomTypeService roomTypeService;
    private final RoomTypeDTO roomTypeDTO;
    private final Runnable onUpdated;

    private JTextField txtTypeName;
    private JTextField txtPricePerHour;
    private JTextField txtPricePerNight;
    private JTextField txtPricePerDay;
    private JTextField txtLateFeePerHour;
    private JTextField txtMaxAdults;
    private JTextField txtMaxChildren;
    private JTextArea txtDescription;

    public FormUpdateRoomTypePricing(Window owner,
                                     RoomTypeDTO roomTypeDTO,
                                     RoomTypeService roomTypeService,
                                     Runnable onUpdated) {
        super(owner, "Cập nhật giá loại phòng", ModalityType.APPLICATION_MODAL);
        this.roomTypeDTO = roomTypeDTO;
        this.roomTypeService = roomTypeService;
        this.onUpdated = onUpdated;

        initUI();
        bindData();

        setSize(620, 500);
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        JPanel root = new JPanel(new MigLayout("fillx, insets 16, wrap 2", "[140!][grow,fill]", ""));
        root.setBackground(new Color(0x0B1F33));
        setContentPane(root);

        txtTypeName = createField();
        txtTypeName.setEditable(false);

        txtPricePerHour = createField();
        txtPricePerNight = createField();
        txtPricePerDay = createField();
        txtLateFeePerHour = createField();
        txtMaxAdults = createField();
        txtMaxChildren = createField();

        txtDescription = new JTextArea(4, 20);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setBackground(new Color(0x102D4A));
        txtDescription.setForeground(new Color(0xE9EEF6));
        txtDescription.setBorder(BorderFactory.createLineBorder(new Color(0x274A6B)));

        root.add(label("Tên loại phòng:"));
        root.add(txtTypeName);

        root.add(label("Giá theo giờ:"));
        root.add(txtPricePerHour);

        root.add(label("Giá theo đêm:"));
        root.add(txtPricePerNight);

        root.add(label("Giá theo ngày:"));
        root.add(txtPricePerDay);

        root.add(label("Phụ thu trễ/giờ:"));
        root.add(txtLateFeePerHour);

        root.add(label("Người lớn tối đa:"));
        root.add(txtMaxAdults);

        root.add(label("Trẻ em tối đa:"));
        root.add(txtMaxChildren);

        root.add(label("Mô tả:"), "top");
        root.add(new JScrollPane(txtDescription), "growx");

        JButton btnSave = new JButton("Lưu");
        JButton btnCancel = new JButton("Đóng");

        stylePrimary(btnSave);
        styleSecondary(btnCancel);

        btnSave.addActionListener(e -> saveData());
        btnCancel.addActionListener(e -> dispose());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(btnSave);
        actions.add(btnCancel);

        root.add(actions, "span 2, right, gaptop 10");
    }

    private void bindData() {
        txtTypeName.setText(roomTypeDTO.getTypeName());
        txtPricePerHour.setText(String.valueOf((long) roomTypeDTO.getPricePerHour()));
        txtPricePerNight.setText(String.valueOf((long) roomTypeDTO.getPricePerNight()));
        txtPricePerDay.setText(String.valueOf((long) roomTypeDTO.getPricePerDay()));
        txtLateFeePerHour.setText(String.valueOf((long) roomTypeDTO.getLateFeePerHour()));
        txtMaxAdults.setText(String.valueOf(roomTypeDTO.getMaxAdults()));
        txtMaxChildren.setText(String.valueOf(roomTypeDTO.getMaxChildren()));
        txtDescription.setText(roomTypeDTO.getDescription() == null ? "" : roomTypeDTO.getDescription());
    }

    private void saveData() {
        try {
            roomTypeDTO.setPricePerHour(Double.parseDouble(txtPricePerHour.getText().trim()));
            roomTypeDTO.setPricePerNight(Double.parseDouble(txtPricePerNight.getText().trim()));
            roomTypeDTO.setPricePerDay(Double.parseDouble(txtPricePerDay.getText().trim()));
            roomTypeDTO.setLateFeePerHour(Double.parseDouble(txtLateFeePerHour.getText().trim()));
            roomTypeDTO.setMaxAdults(Integer.parseInt(txtMaxAdults.getText().trim()));
            roomTypeDTO.setMaxChildren(Integer.parseInt(txtMaxChildren.getText().trim()));
            roomTypeDTO.setDescription(txtDescription.getText().trim());

            BaseResponse response = sendRequest(CommandType.UPDATE_ROOM_TYPE_PRICING, roomTypeDTO);
            JOptionPane.showMessageDialog(
                    this,
                    response.isSuccess() ? "Cập nhật giá phòng thành công." : response.getMessage()
            );

            if (response.isSuccess()) {
                if (onUpdated != null) onUpdated.run();
                dispose();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đúng định dạng số.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return SocketSessionManager.send(BaseRequest.of(commandType, data));
    }

    private JTextField createField() {
        JTextField txt = new JTextField();
        txt.putClientProperty(
                FlatClientProperties.STYLE,
                "arc:10; background:#102D4A; foreground:#E9EEF6; borderColor:#274A6B; padding:6,10,6,10;"
        );
        return txt;
    }

    private JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(0xE9EEF6));
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return lbl;
    }

    private void stylePrimary(AbstractButton b) {
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(0x2563EB));
        b.setFocusPainted(false);
    }

    private void styleSecondary(AbstractButton b) {
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(0x0EA5E9));
        b.setFocusPainted(false);
    }
}