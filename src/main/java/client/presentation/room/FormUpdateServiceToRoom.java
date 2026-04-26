package client.presentation.room;

import client.network.socket.HotelClient;
import client.network.socket.SocketRequestExecutor;
import client.network.socket.SocketSessionManager;
import client.presentation.login.main.Application;
import com.formdev.flatlaf.FlatClientProperties;
import common.dto.ServiceDTO;
import common.dto.request_dto.AddServiceToRoomRequestDTO;
import common.protocol.command.CommandType;
import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;
import net.miginfocom.swing.MigLayout;
import server.core.service.RoomStayService;
import server.core.service.ServiceService;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class FormUpdateServiceToRoom extends JDialog {

    private static final Color BG = new Color(0x0B1F33);
    private static final Color TEXT = new Color(0xE9EEF6);
    private static final Color GOLD = new Color(0xF5C452);

    private final String roomID;

    private JComboBox<ServiceDTO> cbxService;
    private JSpinner spQty;
    private JButton btnAdd;
    private JLabel lblStock;

    public FormUpdateServiceToRoom(Component parent, String roomID, RoomStayService roomStayService, ServiceService serviceService) {
        this.roomID = roomID;

        setTitle("Thêm dịch vụ cho phòng " + roomID);
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel pnRoot = new JPanel(new MigLayout(
                "insets 12 16 16 16, wrap 2",
                "[right]16[300,grow]",
                "[]6[]0[]20[]"
        ));
        pnRoot.setBackground(BG);

        JLabel lblService = new JLabel("Service:");
        lblService.setForeground(TEXT);
        pnRoot.add(lblService);

        cbxService = new JComboBox<>();
        cbxService.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof ServiceDTO s) setText(s.getServiceName());
                else setText("");
                return this;
            }
        });
        cbxService.putClientProperty(FlatClientProperties.STYLE,
                "arc:10; background:#102D4A; foreground:#E9EEF6; borderColor:#274A6B; padding:6,10,6,10;");
        pnRoot.add(cbxService, "growx");

        pnRoot.add(new JLabel());
        lblStock = new JLabel("Còn: -");
        lblStock.setForeground(GOLD);
        pnRoot.add(lblStock, "growx");

        JLabel lblQty = new JLabel("Quantity:");
        lblQty.setForeground(TEXT);
        pnRoot.add(lblQty);

        spQty = new JSpinner(new SpinnerNumberModel(1, null, null, 1));
        spQty.setEditor(new JSpinner.NumberEditor(spQty, "#"));
        ((JSpinner.NumberEditor) spQty.getEditor()).getTextField().setColumns(8);
        NumberFormatter formatter = (NumberFormatter) ((JSpinner.NumberEditor) spQty.getEditor()).getTextField().getFormatter();
        formatter.setMinimum(1);
        formatter.setAllowsInvalid(false);
        pnRoot.add(spQty, "w 120!");

        btnAdd = new JButton("Thêm");
        btnAdd.putClientProperty(FlatClientProperties.STYLE,
                "arc:12; background:#F5C452; foreground:#0B1F33; borderColor:#F1B93A; hoverBackground:#FFD36E;");
        pnRoot.add(btnAdd, "span 2, al right, w 120!, h 34!");

        setContentPane(pnRoot);
        pack();
        setLocationRelativeTo(parent);

        loadServices();

        cbxService.addActionListener(e -> updateStockLabel());
        btnAdd.addActionListener(e -> addService());
    }

    private void loadServices() {
        try {
            BaseResponse response = sendRequest(CommandType.GET_ALL_SERVICES, null);
            if (!response.isSuccess()) {
                throw new RuntimeException(response.getMessage());
            }

            @SuppressWarnings("unchecked")
            List<ServiceDTO> list = (List<ServiceDTO>) response.getData();
            if (list == null) list = new ArrayList<>();

            DefaultComboBoxModel<ServiceDTO> model = new DefaultComboBoxModel<>();
            for (ServiceDTO s : list) model.addElement(s);
            cbxService.setModel(model);

            updateStockLabel();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStockLabel() {
        ServiceDTO s = (ServiceDTO) cbxService.getSelectedItem();
        lblStock.setText(s == null ? "Còn: -" : "Còn: " + s.getQuantity());
    }

    private void addService() {
        try {
            spQty.commitEdit();
        } catch (ParseException ignored) {
        }

        ServiceDTO s = (ServiceDTO) cbxService.getSelectedItem();
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn dịch vụ.");
            return;
        }

        int qty = ((Number) spQty.getValue()).intValue();
        if (qty <= 0) {
            JOptionPane.showMessageDialog(this, "Số lượng phải > 0.");
            return;
        }

        try {
            BaseResponse response = sendRequest(
                    CommandType.ADD_SERVICE_TO_ROOM,
                    new AddServiceToRoomRequestDTO(roomID, s.getServiceName(), qty)
            );

            JOptionPane.showMessageDialog(this,
                    response.isSuccess() ? "Thêm dịch vụ thành công." : response.getMessage());

            if (response.isSuccess()) {
                dispose();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return SocketSessionManager.send(BaseRequest.of(commandType, data));
    }
}