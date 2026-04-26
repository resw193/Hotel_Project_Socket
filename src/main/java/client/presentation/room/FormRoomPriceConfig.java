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
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class FormRoomPriceConfig extends JDialog {

    private static final Color BG = new Color(0x0B1F33);
    private static final Color PANEL_TOP = new Color(0x0E2942);
    private static final Color TEXT_PRIMARY = new Color(0xE9EEF6);
    private static final Color GOLD = new Color(0xF5C452);
    private static final Color HEADER_BG = new Color(0x102A43);
    private static final Color HEADER_ACCENT = new Color(0x22D3EE);
    private static final Font BASE_FONT = new Font("Segoe UI", Font.PLAIN, 13);

    private final RoomTypeService roomTypeService;
    private JTable table;
    private RoomTypePriceTableModel tableModel;

    public FormRoomPriceConfig(Window owner, RoomTypeService roomTypeService) {
        super(owner, "Bảng thông tin giá phòng", ModalityType.APPLICATION_MODAL);
        this.roomTypeService = roomTypeService;

        initUI();
        loadData();

        setSize(980, 520);
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        setLayout(new MigLayout("fill, wrap, insets 0", "[grow]", "[grow 0][grow]"));
        getContentPane().setBackground(BG);

        JPanel top = new JPanel(new MigLayout("insets 12 16 12 16", "[][grow]push[]8[]", "[]"));
        top.setBackground(PANEL_TOP);
        add(top, "growx");

        JLabel lblTitle = new JLabel("Bảng thông tin giá phòng");
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 18f));
        top.add(lblTitle);

        JLabel lblDesc = new JLabel("Xem và cập nhật giá phòng hiện hành theo loại phòng");
        lblDesc.setForeground(GOLD);
        lblDesc.setFont(BASE_FONT);
        top.add(lblDesc);

        JButton btnRefresh = new JButton("Làm mới");
        JButton btnUpdate = new JButton("Cập nhật giá");

        stylePrimarySoft(btnRefresh);
        stylePrimarySolid(btnUpdate);

        top.add(btnRefresh, "w 110!");
        top.add(btnUpdate, "w 130!");

        tableModel = new RoomTypePriceTableModel();
        table = new JTable(tableModel);
        table.setRowHeight(32);
        table.setFont(BASE_FONT);
        table.setForeground(TEXT_PRIMARY);
        table.setBackground(BG);
        table.setGridColor(new Color(0x13314A));
        table.putClientProperty(
                FlatClientProperties.STYLE,
                "background:#0B1F33; foreground:#E9EEF6; selectionBackground:#153C5B; selectionForeground:#E9EEF6; gridColor:#13314A"
        );

        JTableHeader header = table.getTableHeader();
        header.setBackground(HEADER_BG);
        header.setForeground(HEADER_ACCENT);
        header.setFont(BASE_FONT.deriveFont(Font.BOLD, 13f));

        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) header.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        add(scrollPane, "grow");

        btnRefresh.addActionListener(e -> loadData());

        btnUpdate.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 loại phòng để cập nhật.");
                return;
            }

            RoomTypeDTO roomTypeDTO = tableModel.getRoomTypeAt(row);
            FormUpdateRoomTypePricing dialog = new FormUpdateRoomTypePricing(
                    this,
                    roomTypeDTO,
                    roomTypeService,
                    this::loadData
            );
            dialog.setVisible(true);
        });
    }

    private void loadData() {
        try {
            BaseResponse response = sendRequest(CommandType.GET_ALL_ROOM_TYPES, null);
            if (!response.isSuccess()) {
                throw new RuntimeException(response.getMessage());
            }

            @SuppressWarnings("unchecked")
            List<RoomTypeDTO> list = (List<RoomTypeDTO>) response.getData();
            tableModel.setRoomTypes(list);
        } catch (Exception ex) {
            tableModel.setRoomTypes(Collections.emptyList());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return SocketSessionManager.send(BaseRequest.of(commandType, data));
    }

    private void stylePrimarySolid(AbstractButton b) {
        b.setFont(BASE_FONT.deriveFont(Font.BOLD));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(new Color(0x1B4F72)));
        b.setBackground(new Color(0x2563EB));
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMargin(new Insets(4, 10, 4, 10));
    }

    private void stylePrimarySoft(AbstractButton b) {
        b.setFont(BASE_FONT.deriveFont(Font.BOLD));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(new Color(0x1B4F72)));
        b.setBackground(new Color(0x0EA5E9));
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMargin(new Insets(4, 10, 4, 10));
    }
}