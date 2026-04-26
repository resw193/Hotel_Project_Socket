package client.presentation.room;

import client.network.socket.HotelClient;
import client.network.socket.SocketRequestExecutor;
import client.network.socket.SocketSessionManager;
import client.presentation.login.main.Application;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import common.dto.RoomDTO;
import common.dto.request_dto.RoomSearchRequestDTO;
import common.dto.RoomTypeDTO;
import common.protocol.command.CommandType;
import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;
import net.miginfocom.swing.MigLayout;
import server.core.service.RoomService;
import server.core.service.RoomTypeService;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;

public class FormRoomManagement extends JPanel {

    private static final Color BG = new Color(0x0B1F33);
    private static final Color PANEL_TOP = new Color(0x0E2942);
    private static final Color TEXT_PRIMARY = new Color(0xE9EEF6);
    private static final Color GOLD = new Color(0xF5C452);
    private static final Color HEADER_BG = new Color(0x102A43);
    private static final Color HEADER_ACCENT = new Color(0x22D3EE);
    private static final Font BASE_FONT = new Font("Segoe UI", Font.PLAIN, 13);

    private final RoomService roomService;
    private final RoomTypeService roomTypeService;
    private final boolean isReception;

    private JTextField txtSearch;
    private JComboBox<String> cbxFilter;
    private JButton btnAdd, btnUpdate, btnPriceConfig;
    private JTable tableRoom;
    private RoomTableModel roomTableModel;

    public FormRoomManagement(RoomService roomService, RoomTypeService roomTypeService, boolean isReception) {
        this.roomService = roomService;
        this.roomTypeService = roomTypeService;
        this.isReception = isReception;

        setLayout(new MigLayout("fill, wrap, insets 0", "[grow]", "[grow 0][grow]"));
        setBackground(BG);

        JPanel top = new JPanel(new MigLayout("insets 12 16 12 16", "[]16[grow,fill]16[]8[]8[]8[]", "[]"));
        top.setBackground(PANEL_TOP);
        add(top, "growx");

        JLabel lblTitle = new JLabel("Quản lý phòng | Rooms");
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 18f));
        top.add(lblTitle);

        txtSearch = new JTextField();
        txtSearch.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm theo mã phòng (RoomID)");
        txtSearch.putClientProperty(
                FlatClientProperties.STYLE,
                "arc:10; background:#102D4A; foreground:#E9EEF6; borderColor:#274A6B; padding:6,10,6,10;"
        );
        top.add(txtSearch, "w 320!, growx");

        cbxFilter = new JComboBox<>(new String[]{"All", "Check-in"});
        cbxFilter.putClientProperty(
                FlatClientProperties.STYLE,
                "arc:10; background:#102D4A; foreground:#E9EEF6; borderColor:#274A6B; padding:6,12,6,12;"
        );
        cbxFilter.setPreferredSize(new Dimension(130, 32));

        JLabel lblFilterStatus = new JLabel("Tình trạng:");
        lblFilterStatus.setForeground(TEXT_PRIMARY);
        lblFilterStatus.setFont(BASE_FONT);

        JPanel pnFilterStatus = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnFilterStatus.setOpaque(false);
        pnFilterStatus.add(lblFilterStatus);
        pnFilterStatus.add(cbxFilter);
        top.add(pnFilterStatus);

        FlatSVGIcon.ColorFilter goldFilter = new FlatSVGIcon.ColorFilter() {
            @Override
            public Color filter(Color color) {
                return GOLD;
            }
        };
        FlatSVGIcon addI = new FlatSVGIcon("client/presentation/icon/svg/add.svg", 0.35f);
        addI.setColorFilter(goldFilter);

        FlatSVGIcon editI = new FlatSVGIcon("client/presentation/icon/svg/edit.svg", 0.35f);
        editI.setColorFilter(goldFilter);

        btnAdd = new JButton("Thêm phòng", addI);
        stylePrimarySolid(btnAdd);

        btnUpdate = new JButton("Cập nhật", editI);
        stylePrimarySoft(btnUpdate);

        btnPriceConfig = new JButton("Bảng thông tin giá phòng");
        stylePrimarySoft(btnPriceConfig);

        if (!isReception) {
            top.add(btnPriceConfig, "w 190!");
            top.add(btnAdd, "w 140!");
        }
        top.add(btnUpdate, "w 120!");

        roomTableModel = new RoomTableModel();
        tableRoom = new JTable(roomTableModel);
        tableRoom.setRowHeight(30);
        tableRoom.setFont(BASE_FONT);
        tableRoom.setForeground(TEXT_PRIMARY);
        tableRoom.setBackground(BG);
        tableRoom.setGridColor(new Color(0x13314A));
        tableRoom.putClientProperty(
                FlatClientProperties.STYLE,
                "background:#0B1F33; foreground:#E9EEF6; selectionBackground:#153C5B; selectionForeground:#E9EEF6; gridColor:#13314A"
        );

        JTableHeader header = tableRoom.getTableHeader();
        header.setBackground(HEADER_BG);
        header.setForeground(HEADER_ACCENT);
        header.setFont(BASE_FONT.deriveFont(Font.BOLD, 13f));

        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) header.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        tableRoom.getColumnModel().getColumn(2).setCellRenderer(center);
        tableRoom.getColumnModel().getColumn(3).setCellRenderer(center);
        tableRoom.getColumnModel().getColumn(4).setCellRenderer(center);

        tableRoom.getColumnModel().getColumn(0).setPreferredWidth(90);
        tableRoom.getColumnModel().getColumn(1).setPreferredWidth(420);
        tableRoom.getColumnModel().getColumn(4).setPreferredWidth(110);

        tableRoom.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (tableRoom.getSelectedRow() != -1 && e.getClickCount() == 2) {
                    int selectedRow = tableRoom.getSelectedRow();
                    RoomDTO room = roomTableModel.getRoomAt(selectedRow);
                    if (room == null) {
                        JOptionPane.showMessageDialog(FormRoomManagement.this, "Không tìm thấy phòng");
                        return;
                    }

                    RoomTypeDTO roomType = null;
                    try {
                        if (room.getRoomTypeId() != null && !room.getRoomTypeId().isBlank()) {
                            roomType = getRoomTypeByIdViaSocket(room.getRoomTypeId());
                        }
                    } catch (Exception ignored) {
                    }

                    FormRoomDetail dialog = new FormRoomDetail(room, roomType);
                    dialog.setModal(true);
                    dialog.setLocationRelativeTo(FormRoomManagement.this);
                    dialog.setVisible(true);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableRoom);
        scrollPane.setBorder(null);
        add(scrollPane, "grow");

        btnAdd.addActionListener(e -> {
            FormAddRoom dialog = new FormAddRoom(this, roomService, roomTypeService);
            dialog.setModal(true);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        });

        btnUpdate.addActionListener(e -> {
            int row = tableRoom.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 phòng để cập nhật.");
                return;
            }
            RoomDTO room = roomTableModel.getRoomAt(row);
            FormUpdateRoomInformation dialog = new FormUpdateRoomInformation(this, room, roomService);
            dialog.setModal(true);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        });

        btnPriceConfig.addActionListener(e -> {
            FormRoomPriceConfig dialog = new FormRoomPriceConfig(
                    SwingUtilities.getWindowAncestor(this),
                    roomTypeService
            );
            dialog.setVisible(true);
        });

        cbxFilter.addActionListener(e -> searchAndFilter());

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { searchAndFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { searchAndFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { searchAndFilter(); }
        });

        txtSearch.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                txtSearch.requestFocusInWindow();
            }
        });

        searchAndFilter();
    }

    public void searchAndFilter() {
        try {
            String keyword = txtSearch.getText().trim();
            String filter = String.valueOf(cbxFilter.getSelectedItem());

            RoomSearchRequestDTO dto = new RoomSearchRequestDTO(keyword, filter);
            BaseResponse response = sendRequest(CommandType.SEARCH_ROOMS, dto);

            if (!response.isSuccess()) {
                throw new RuntimeException(response.getMessage());
            }

            @SuppressWarnings("unchecked")
            List<RoomDTO> list = (List<RoomDTO>) response.getData();
            roomTableModel.setRooms(list);
        } catch (Exception ex) {
            roomTableModel.setRooms(Collections.emptyList());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private RoomTypeDTO getRoomTypeByIdViaSocket(String roomTypeId) {
        try {
            BaseResponse response = sendRequest(CommandType.GET_ROOM_TYPE_BY_ID, roomTypeId);
            if (!response.isSuccess()) return null;
            return (RoomTypeDTO) response.getData();
        } catch (Exception e) {
            return null;
        }
    }

    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return SocketSessionManager.send(BaseRequest.of(commandType, data));
    }

    @Override
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(() -> txtSearch.requestFocusInWindow());
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