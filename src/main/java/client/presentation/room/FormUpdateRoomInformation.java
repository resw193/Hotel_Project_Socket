package client.presentation.room;

import client.network.socket.HotelClient;
import client.network.socket.SocketRequestExecutor;
import client.network.socket.SocketSessionManager;
import client.presentation.login.main.Application;
import com.formdev.flatlaf.FlatClientProperties;
import common.dto.RoomDTO;
import common.protocol.command.CommandType;
import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;
import net.miginfocom.swing.MigLayout;
import server.core.service.RoomService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FormUpdateRoomInformation extends JDialog {

    private final RoomService roomService;
    private final FormRoomManagement parent;
    private final RoomDTO room;

    private JTextField txtDescription;
    private JComboBox<String> cbxView;
    private JLabel lblPreview;
    private String imgPath;

    public FormUpdateRoomInformation(FormRoomManagement parent, RoomDTO room, RoomService roomService) {
        this.parent = parent;
        this.room = room;
        this.roomService = roomService;

        setTitle("Cập nhật thông tin phòng");
        setLayout(new MigLayout("wrap 2, insets 16, gap 10", "[right][350!, grow]"));
        getContentPane().setBackground(new Color(0x0B1F33));

        JLabel lblRoomID = label("Mã phòng:");
        JTextField txtID = field();
        txtID.setText(room.getRoomId());
        txtID.setEditable(false);

        JLabel lblDesc = label("Mô tả:");
        txtDescription = field();
        txtDescription.setText(room.getDescription());

        JLabel lblView = label("View:");
        cbxView = new JComboBox<>(new String[]{"(trống)", "Ban công", "Vườn", "Thành phố", "Biển"});
        cbxView.putClientProperty(FlatClientProperties.STYLE,
                "arc:10; background:#102D4A; foreground:#E9EEF6; borderColor:#274A6B;");
        if (room.getView() != null) cbxView.setSelectedItem(room.getView());

        JLabel lblImage = label("Ảnh phòng:");
        JButton btnChoose = new JButton("Chọn ảnh");
        styleSecondary(btnChoose);

        lblPreview = new JLabel();
        lblPreview.setPreferredSize(new Dimension(160, 120));

        if (room.getImgRoomSource() != null && !room.getImgRoomSource().trim().isEmpty()) {
            String p = room.getImgRoomSource();
            Image img = null;
            File f = new File(p);
            if (f.exists()) img = new ImageIcon(f.getAbsolutePath()).getImage();
            else {
                java.net.URL u = getClass().getResource(p.startsWith("/") ? p : "/" + p);
                if (u != null) img = new ImageIcon(u).getImage();
            }
            if (img != null) {
                lblPreview.setIcon(new ImageIcon(img.getScaledInstance(160, 120, Image.SCALE_SMOOTH)));
            }
            imgPath = p;
        } else {
            imgPath = room.getImgRoomSource();
        }

        JButton btnSave = new JButton("Lưu");
        stylePrimary(btnSave);

        add(lblRoomID); add(txtID, "growx");
        add(lblDesc);   add(txtDescription, "growx");
        add(lblView);   add(cbxView, "w 220!");
        add(lblImage);  add(btnChoose, "split 2"); add(lblPreview, "wrap");
        add(new JLabel()); add(btnSave, "right");

        btnChoose.addActionListener(e -> chooseImage());
        btnSave.addActionListener(e -> updateRoomInformation());

        pack();
        setSize(560, getHeight());
        setLocationRelativeTo(parent);
    }

    private void chooseImage() {
        JFileChooser ch = new JFileChooser();
        ch.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "jpeg", "png", "gif", "bmp"));
        if (ch.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = ch.getSelectedFile();
            File copied = copyToResourcesImages(f);
            imgPath = "/images/" + copied.getName();
            Image img = new ImageIcon(copied.getAbsolutePath()).getImage()
                    .getScaledInstance(160, 120, Image.SCALE_SMOOTH);
            lblPreview.setIcon(new ImageIcon(img));
        }
    }

    private void updateRoomInformation() {
        String description = txtDescription.getText().trim();
        if (description.isEmpty() || description.length() > 200) {
            JOptionPane.showMessageDialog(this, "Mô tả phải khác rỗng và không quá 200 kí tự");
            return;
        }

        String view = cbxView.getSelectedItem() != null && !"(trống)".equals(cbxView.getSelectedItem())
                ? cbxView.getSelectedItem().toString() : null;

        try {
            RoomDTO dto = new RoomDTO();
            dto.setRoomId(room.getRoomId());
            dto.setDescription(description);
            dto.setImgRoomSource(imgPath);
            dto.setView(view);
            dto.setAvailable(room.isAvailable());
            dto.setRoomTypeId(room.getRoomTypeId());
            dto.setRoomTypeName(room.getRoomTypeName());

            BaseResponse response = sendRequest(CommandType.UPDATE_ROOM_INFORMATION, dto);
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công");
                dispose();
                parent.searchAndFilter();
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return SocketSessionManager.send(BaseRequest.of(commandType, data));
    }

    private JLabel label(String s) {
        JLabel lb = new JLabel(s);
        lb.setForeground(new Color(0xE9EEF6));
        return lb;
    }

    private JTextField field() {
        JTextField tf = new JTextField();
        tf.putClientProperty(FlatClientProperties.STYLE,
                "arc:10; background:#102D4A; foreground:#E9EEF6; borderColor:#274A6B; padding:6,10,6,10;");
        return tf;
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

    private File copyToResourcesImages(File src) {
        try {
            java.net.URL u = getClass().getResource("/images/");
            Path destDir = (u != null && "file".equalsIgnoreCase(u.getProtocol()))
                    ? Paths.get(u.toURI())
                    : Paths.get("src", "main", "resources", "images");
            Files.createDirectories(destDir);
            Path dest = destDir.resolve(src.getName());
            Files.copy(src.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
            return dest.toFile();
        } catch (Exception ex) {
            return src;
        }
    }
}