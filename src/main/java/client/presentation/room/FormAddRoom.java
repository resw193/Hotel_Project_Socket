package client.presentation.room;

import client.network.socket.HotelClient;
import client.network.socket.SocketRequestExecutor;
import client.network.socket.SocketSessionManager;
import client.presentation.login.main.Application;
import com.formdev.flatlaf.FlatClientProperties;
import common.dto.RoomDTO;
import common.dto.RoomTypeDTO;
import common.protocol.command.CommandType;
import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;
import net.miginfocom.swing.MigLayout;
import server.core.service.RoomService;
import server.core.service.RoomTypeService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class FormAddRoom extends JDialog {

    private final RoomService roomService;
    private final RoomTypeService roomTypeService;
    private final FormRoomManagement parent;

    private JTextField txtDescription;
    private JCheckBox chkAvailable;
    private JComboBox<String> cbxRoomType;
    private JComboBox<String> cbxView;
    private List<RoomTypeDTO> roomTypes = new ArrayList<>();
    private JLabel lblPreview;
    private String imgPath = "";

    public FormAddRoom(FormRoomManagement parent, RoomService roomService, RoomTypeService roomTypeService) {
        this.parent = parent;
        this.roomService = roomService;
        this.roomTypeService = roomTypeService;

        setTitle("Thêm phòng mới");
        setLayout(new MigLayout("wrap 2, insets 16, gap 10", "[right][350!, grow]"));
        getContentPane().setBackground(new Color(0x0B1F33));

        JLabel lblDesc = label("Mô tả:");
        txtDescription = field();

        JLabel lblStatus = label("Tình trạng:");
        chkAvailable = new JCheckBox("Trống");
        chkAvailable.setOpaque(false);
        chkAvailable.setForeground(new Color(0xE9EEF6));
        chkAvailable.setSelected(true);
        chkAvailable.setEnabled(false);

        JLabel lblType = label("Loại phòng:");
        cbxRoomType = new JComboBox<>();
        cbxRoomType.putClientProperty(FlatClientProperties.STYLE,
                "arc:10; background:#102D4A; foreground:#E9EEF6; borderColor:#274A6B;");

        JLabel lblView = label("View:");
        cbxView = new JComboBox<>(new String[]{"(trống)", "Ban công", "Vườn", "Thành phố", "Biển"});
        cbxView.putClientProperty(FlatClientProperties.STYLE,
                "arc:10; background:#102D4A; foreground:#E9EEF6; borderColor:#274A6B;");

        JLabel lblImage = label("Ảnh phòng:");
        JButton btnChoose = new JButton("Chọn ảnh");
        styleSecondary(btnChoose);

        lblPreview = new JLabel();
        lblPreview.setPreferredSize(new Dimension(160, 120));

        JButton btnAddRoom = new JButton("Thêm");
        stylePrimary(btnAddRoom);

        add(lblDesc);   add(txtDescription, "growx");
        add(lblStatus); add(chkAvailable, "left");
        add(lblType);   add(cbxRoomType, "w 220!");
        add(lblView);   add(cbxView, "w 220!");
        add(lblImage);  add(btnChoose, "split 2"); add(lblPreview, "wrap");
        add(new JLabel()); add(btnAddRoom, "right");

        loadRoomTypes();

        btnChoose.addActionListener(e -> chooseImage());
        btnAddRoom.addActionListener(e -> addRoom());

        pack();
        setSize(560, getHeight());
        setLocationRelativeTo(parent);
    }

    private void loadRoomTypes() {
        try {
            BaseResponse response = sendRequest(CommandType.GET_ALL_ROOM_TYPES, null);
            if (!response.isSuccess()) {
                throw new RuntimeException(response.getMessage());
            }

            @SuppressWarnings("unchecked")
            List<RoomTypeDTO> list = (List<RoomTypeDTO>) response.getData();
            roomTypes = list != null ? list : new ArrayList<>();

            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            for (RoomTypeDTO t : roomTypes) {
                model.addElement(t.getTypeName());
            }
            cbxRoomType.setModel(model);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Không tải được danh sách loại phòng: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
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

    private void addRoom() {
        String description = txtDescription.getText().trim();
        int idx = cbxRoomType.getSelectedIndex();
        RoomTypeDTO type = (idx >= 0 && idx < roomTypes.size()) ? roomTypes.get(idx) : null;
        String view = cbxView.getSelectedItem() != null && !"(trống)".equals(cbxView.getSelectedItem())
                ? cbxView.getSelectedItem().toString() : null;

        if (description.isEmpty() || description.length() > 200) {
            JOptionPane.showMessageDialog(this, "Mô tả không rỗng và không quá 200 kí tự");
            return;
        }

        if (type == null) {
            JOptionPane.showMessageDialog(this, "Chưa chọn loại phòng.");
            return;
        }

        try {
            RoomDTO dto = new RoomDTO();
            dto.setDescription(description);
            dto.setRoomTypeId(type.getRoomTypeId());
            dto.setRoomTypeName(type.getTypeName());
            dto.setImgRoomSource(imgPath);
            dto.setView(view);

            BaseResponse response = sendRequest(CommandType.ADD_ROOM, dto);
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Thêm phòng thành công");
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
}