package client.presentation.roomBooking;

import client.network.socket.HotelClient;
import client.network.socket.SocketRequestExecutor;
import client.network.socket.SocketSessionManager;
import client.presentation.login.main.Application;
import common.dto.OdrInfoDTO;
import common.dto.RoomDTO;
import common.dto.request_dto.RoomIdRequestDTO;
import common.protocol.command.CommandType;
import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;
import net.miginfocom.swing.MigLayout;
import server.core.service.RoomService;
import server.core.service.RoomStayService;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FormChangeRoomWhileCheckIn extends JDialog {

    private final String oldRoomID;
    private final FormRoomBookingManagement parent;
    private JComboBox<RoomDTO> cbxNewRoom;
    private JLabel lblCheckInInfo;

    public FormChangeRoomWhileCheckIn(Window owner,
                                      String oldRoomID,
                                      RoomService roomService,
                                      RoomStayService roomStayService,
                                      FormRoomBookingManagement parent) {
        super(owner, "Đổi phòng khi đang check-in", ModalityType.APPLICATION_MODAL);
        this.oldRoomID = oldRoomID;
        this.parent = parent;
        initUI();
        loadAvailableRooms();
        loadCheckInInfo();
    }

    private void initUI() {
        JPanel root = new JPanel(new MigLayout("wrap 2, insets 16", "[right]15[grow,360!]"));
        root.setBackground(new Color(0x0B1F33));

        cbxNewRoom = new JComboBox<>();
        cbxNewRoom.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof RoomDTO r) {
                    setText(r.getRoomId() + " - " + safe(r.getDescription()));
                } else {
                    setText("");
                }
                return this;
            }
        });

        lblCheckInInfo = new JLabel("-");
        lblCheckInInfo.setForeground(new Color(0xE9EEF6));

        JButton btnChoose = new JButton("Tiếp tục");
        JButton btnClose = new JButton("Đóng");

        root.add(label("Phòng hiện tại:"));
        root.add(value(oldRoomID));

        root.add(label("Phòng mới:"));
        root.add(cbxNewRoom, "growx");

        root.add(label("Khách đang ở:"));
        root.add(lblCheckInInfo, "growx");

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(btnClose);
        actions.add(btnChoose);
        root.add(actions, "span 2, growx");

        btnClose.addActionListener(e -> dispose());
        btnChoose.addActionListener(e -> openDetail());

        setContentPane(root);
        pack();
        setSize(580, getPreferredSize().height + 20);
        setLocationRelativeTo(getOwner());
    }

    private void loadAvailableRooms() {
        try {
            BaseResponse response = sendRequest(CommandType.GET_ALL_ROOMS, null);
            if (!response.isSuccess()) throw new RuntimeException(response.getMessage());

            @SuppressWarnings("unchecked")
            List<RoomDTO> rooms = (List<RoomDTO>) response.getData();
            if (rooms == null) rooms = new ArrayList<>();

            DefaultComboBoxModel<RoomDTO> model = new DefaultComboBoxModel<>();
            for (RoomDTO r : rooms) {
                if (r.isAvailable() && !oldRoomID.equalsIgnoreCase(r.getRoomId())) {
                    model.addElement(r);
                }
            }
            cbxNewRoom.setModel(model);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCheckInInfo() {
        try {
            BaseResponse response = sendRequest(CommandType.GET_ACTIVE_CHECKIN_INFO, new RoomIdRequestDTO(oldRoomID));
            if (!response.isSuccess()) {
                lblCheckInInfo.setText(response.getMessage());
                return;
            }

            OdrInfoDTO odr = (OdrInfoDTO) response.getData();
            if (odr == null) {
                lblCheckInInfo.setText("Không có khách đang check-in.");
            } else {
                lblCheckInInfo.setText(
                        safe(odr.getFullName()) + " | " +
                                (odr.getCheckIn() == null ? "-" : odr.getCheckIn().toString()) +
                                " → " +
                                (odr.getCheckOut() == null ? "-" : odr.getCheckOut().toString())
                );
            }
        } catch (Exception ex) {
            lblCheckInInfo.setText(ex.getMessage());
        }
    }

    private void openDetail() {
        RoomDTO newRoom = (RoomDTO) cbxNewRoom.getSelectedItem();
        if (newRoom == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phòng mới.");
            return;
        }

        try {
            BaseResponse response = sendRequest(CommandType.GET_ACTIVE_CHECKIN_INFO, new RoomIdRequestDTO(oldRoomID));
            if (!response.isSuccess()) {
                JOptionPane.showMessageDialog(this, response.getMessage());
                return;
            }

            OdrInfoDTO odr = (OdrInfoDTO) response.getData();
            if (odr == null) {
                JOptionPane.showMessageDialog(this, "Không có thông tin check-in hợp lệ.");
                return;
            }

            FormChangeRoomWhileCheckInDetail detail = new FormChangeRoomWhileCheckInDetail(
                    this,
                    oldRoomID,
                    newRoom,
                    odr
            );
            detail.setLocationRelativeTo(this);
            detail.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void closeAfterSuccess() {
        dispose();
        if (parent != null) {
            parent.loadData();
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

    private JLabel value(String s) {
        JLabel lb = new JLabel(s);
        lb.setForeground(new Color(0xF5C452));
        return lb;
    }

    private String safe(String s) {
        return s == null ? "-" : s;
    }
}