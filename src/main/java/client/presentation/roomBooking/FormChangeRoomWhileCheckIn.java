package client.presentation.roomBooking;

import client.network.socket.SocketSessionManager;
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
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FormChangeRoomWhileCheckIn extends JDialog {

    private final Color PANEL_BG = new Color(0x123657);
    private final Color BG = new Color(0x0B1F33);
    private final Color FG = new Color(0xE9EEF6);
    private final Color CARD_BG = new Color(0x102D4A);
    private final Color BORDER = new Color(0x274A6B);
    private final Color PRIMARY = new Color(0xF5C452);
    private final Color MUTED = new Color(0xB8C4D4);
    private final Color BTN_SECONDARY = new Color(0x1B4D78);

    private final String oldRoomID;
    private final FormRoomBookingManagement parent;

    private JComboBox<RoomDTO> cbxNewRoom;

    private JLabel lblCustomerName;
    private JLabel lblPhone;
    private JLabel lblBookingType;
    private JLabel lblStayRange;

    private JTextArea txtNewRoomPreview;

    public FormChangeRoomWhileCheckIn(Window owner, String oldRoomID, RoomService roomService, RoomStayService roomStayService, FormRoomBookingManagement parent) {
        super(owner, "Đổi phòng khi đang check-in", ModalityType.APPLICATION_MODAL);
        this.oldRoomID = oldRoomID;
        this.parent = parent;

        initUI();
        loadAvailableRooms();
        loadCheckInInfo();
    }

    private void initUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);

        applyLargeDialogSize(0.82, 0.72, 1180, 680);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        setContentPane(root);
        setLocationRelativeTo(getOwner());
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new MigLayout("insets 18 20 14 20, fillx", "[grow][right]"));
        header.setBackground(BG);

        JPanel left = new JPanel(new MigLayout("wrap 1, insets 0", "[grow]"));
        left.setOpaque(false);

        JLabel title = new JLabel("ĐỔI PHÒNG KHI ĐANG CHECK-IN");
        title.setForeground(FG);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));

        JLabel sub = new JLabel("Chọn phòng mới cho khách đang ở và tiếp tục sang bước xác nhận chi tiết.");
        sub.setForeground(MUTED);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        left.add(title);
        left.add(sub);

        JLabel badge = new JLabel("Phòng hiện tại: " + oldRoomID);
        badge.setOpaque(true);
        badge.setBackground(PRIMARY);
        badge.setForeground(new Color(0x0B1F33));
        badge.setFont(new Font("Segoe UI", Font.BOLD, 14));
        badge.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        header.add(left, "growx");
        header.add(badge);

        return header;
    }

    private JComponent buildCenter() {
        JPanel content = new JPanel(new MigLayout(
                "insets 0 20 16 20, gap 16, fill",
                "[720::,grow,fill][390::,fill]",
                "[grow,fill]"
        ));
        content.setBackground(BG);

        content.add(buildSelectionCard(), "grow");
        content.add(buildCurrentStayCard(), "growy");

        JScrollPane sp = new JScrollPane(content);
        disableHorizontalScroll(sp);

        return sp;
    }

    private JComponent buildSelectionCard() {
        JPanel card = cardPanel(new MigLayout(
                "wrap 1, insets 18, fillx",
                "[grow,fill]",
                "[]12[]12[]12[grow,fill]"
        ));

        JLabel title = sectionTitle("1. Chọn phòng mới");
        JLabel desc = mutedLabel("Danh sách dưới đây là các phòng có thể chọn để chuyển cho khách đang check-in.");

        card.add(title);
        card.add(desc);

        JPanel form = new JPanel(new MigLayout(
                "insets 0, gap 10 10, fillx",
                "[110!][grow,fill]",
                "[][]"
        ));
        form.setOpaque(false);

        form.add(label("Phòng cũ:"));
        form.add(valueField(oldRoomID), "growx");

        cbxNewRoom = new JComboBox<>();
        styleComboBox(cbxNewRoom);
        cbxNewRoom.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof RoomDTO r) {
                    setText(r.getRoomId()
                            + " - " + safe(r.getDescription())
                            + " - " + safe(r.getRoomTypeName())
                            + " - View: " + safe(r.getView()));
                } else {
                    setText("");
                }
                setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                return this;
            }
        });
        cbxNewRoom.addActionListener(e -> updateNewRoomPreview());

        form.add(label("Phòng mới:"));
        form.add(cbxNewRoom, "growx");

        card.add(form, "growx");

        JPanel previewCard = innerCard(new MigLayout("wrap 1, insets 14, fill", "[grow,fill]", "[]8[grow,fill]"));
        JLabel pTitle = yellowTitle("Thông tin phòng mới đã chọn");
        txtNewRoomPreview = new JTextArea();
        txtNewRoomPreview.setEditable(false);
        txtNewRoomPreview.setLineWrap(true);
        txtNewRoomPreview.setWrapStyleWord(true);
        txtNewRoomPreview.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtNewRoomPreview.setForeground(FG);
        txtNewRoomPreview.setBackground(new Color(0x14385D));
        txtNewRoomPreview.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        txtNewRoomPreview.setText("Chưa chọn phòng mới.");

        previewCard.add(pTitle);
        previewCard.add(txtNewRoomPreview, "grow");

        card.add(previewCard, "grow");
        return card;
    }

    private JComponent buildCurrentStayCard() {
        JPanel card = cardPanel(new MigLayout(
                "wrap 1, insets 18, fillx",
                "[grow,fill]",
                "[]12[]"
        ));

        card.add(sectionTitle("2. Khách đang ở"));
        card.add(mutedLabel("Tóm tắt khách hiện đang check-in trong phòng cũ để bạn đối chiếu trước khi chuyển."));

        JPanel grid = new JPanel(new MigLayout(
                "insets 0, gap 10 10, fillx",
                "[grow,fill]",
                "[][][][]"
        ));
        grid.setOpaque(false);

        lblCustomerName = infoValue("-");
        lblPhone = infoValue("-");
        lblBookingType = infoValue("-");
        lblStayRange = infoValue("-");

        grid.add(infoBox("Khách hàng", lblCustomerName), "growx, wrap");
        grid.add(infoBox("Số điện thoại", lblPhone), "growx, wrap");
        grid.add(infoBox("Kiểu booking", lblBookingType), "growx, wrap");
        grid.add(infoBox("Thời gian lưu trú", lblStayRange), "growx, wrap");

        JTextArea note = new JTextArea(
                "Lưu ý:\n" +
                        "- Đây là khách hiện đang ở trong phòng cũ.\n" +
                        "- Bước tiếp theo sẽ cho phép chọn thời điểm chuyển phòng.\n" +
                        "- Nghiệp vụ đổi phòng đang ở sẽ giữ nguyên logic hiện tại của bạn."
        );
        note.setEditable(false);
        note.setLineWrap(true);
        note.setWrapStyleWord(true);
        note.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        note.setForeground(MUTED);
        note.setBackground(new Color(0x14385D));
        note.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        card.add(grid, "growx");
        card.add(note, "growx");

        return card;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new MigLayout("insets 10 20 18 20", "[grow]push[][]", "[]"));
        footer.setBackground(BG);

        JLabel tip = new JLabel("Mẹo: chọn phòng mới trước, sau đó bấm “Tiếp tục” để xác nhận thời điểm chuyển.");
        tip.setForeground(MUTED);
        tip.setFont(new Font("Segoe UI", Font.ITALIC, 13));

        JButton btnClose = secondaryButton("Đóng");
        JButton btnNext = primaryButton("Tiếp tục");

        btnClose.addActionListener(e -> dispose());
        btnNext.addActionListener(e -> openDetail());

        footer.add(tip, "growx");
        footer.add(btnClose, "w 120!, h 38!");
        footer.add(btnNext, "w 140!, h 38!");

        return footer;
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
            updateNewRoomPreview();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCheckInInfo() {
        try {
            BaseResponse response = sendRequest(CommandType.GET_ACTIVE_CHECKIN_INFO, new RoomIdRequestDTO(oldRoomID));
            if (!response.isSuccess()) {
                lblCustomerName.setText(response.getMessage());
                lblPhone.setText("-");
                lblBookingType.setText("-");
                lblStayRange.setText("-");
                return;
            }

            OdrInfoDTO odr = (OdrInfoDTO) response.getData();
            if (odr == null) {
                lblCustomerName.setText("Không có khách đang check-in");
                lblPhone.setText("-");
                lblBookingType.setText("-");
                lblStayRange.setText("-");
            } else {
                lblCustomerName.setText(safe(odr.getFullName()));
                lblPhone.setText(safe(odr.getPhone()));
                lblBookingType.setText(safe(odr.getBookingType()));
                lblStayRange.setText(
                        (odr.getCheckIn() == null ? "-" : odr.getCheckIn().toString())
                                + "  →  " +
                                (odr.getCheckOut() == null ? "-" : odr.getCheckOut().toString())
                );
            }
        } catch (Exception ex) {
            lblCustomerName.setText(ex.getMessage());
            lblPhone.setText("-");
            lblBookingType.setText("-");
            lblStayRange.setText("-");
        }
    }

    private void updateNewRoomPreview() {
        RoomDTO room = (RoomDTO) cbxNewRoom.getSelectedItem();
        if (room == null) {
            txtNewRoomPreview.setText("Chưa có phòng mới phù hợp để chọn.");
            return;
        }

        txtNewRoomPreview.setText(
                "Phòng: " + safe(room.getRoomId()) + "\n" +
                        "Mô tả: " + safe(room.getDescription()) + "\n" +
                        "Loại phòng: " + safe(room.getRoomTypeName()) + "\n" +
                        "View: " + safe(room.getView()) + "\n\n" +
                        "Phòng này sẽ được dùng để chuyển khách từ phòng " + oldRoomID + " sang ở giai đoạn tiếp theo."
        );
        txtNewRoomPreview.setCaretPosition(0);
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

            FormChangeRoomWhileCheckInDetail detail = new FormChangeRoomWhileCheckInDetail(this, oldRoomID, newRoom, odr);
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

    private void applyLargeDialogSize(double widthRatio, double heightRatio, int minWidth, int minHeight) {
        Rectangle screen = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();

        int width = (int) Math.min(screen.width - 40, Math.max(minWidth, screen.width * widthRatio));
        int height = (int) Math.min(screen.height - 40, Math.max(minHeight, screen.height * heightRatio));

        setMinimumSize(new Dimension(minWidth, minHeight));
        setPreferredSize(new Dimension(width, height));
        setSize(width, height);
        setLocation(
                screen.x + (screen.width - width) / 2,
                screen.y + (screen.height - height) / 2
        );
    }

    private void disableHorizontalScroll(JScrollPane sp) {
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.getVerticalScrollBar().setUnitIncrement(18);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(BG);
    }

    private JPanel cardPanel(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));
        return p;
    }

    private JPanel innerCard(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(new Color(0x123657));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        ));
        return p;
    }

    private JLabel sectionTitle(String s) {
        JLabel lb = new JLabel(s);
        lb.setForeground(PRIMARY);
        lb.setFont(new Font("Segoe UI", Font.BOLD, 20));
        return lb;
    }

    private JLabel yellowTitle(String s) {
        JLabel lb = new JLabel(s);
        lb.setForeground(PRIMARY);
        lb.setFont(new Font("Segoe UI", Font.BOLD, 16));
        return lb;
    }

    private JLabel mutedLabel(String s) {
        JLabel lb = new JLabel(s);
        lb.setForeground(MUTED);
        lb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return lb;
    }

    private JLabel label(String s) {
        JLabel lb = new JLabel(s);
        lb.setForeground(FG);
        lb.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return lb;
    }

    private JComponent valueField(String s) {
        JLabel lb = new JLabel(s);
        lb.setOpaque(true);
        lb.setBackground(new Color(0x14385D));
        lb.setForeground(PRIMARY);
        lb.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        return lb;
    }

    private JLabel infoValue(String s) {
        JLabel lb = new JLabel(s);
        lb.setForeground(FG);
        lb.setFont(new Font("Segoe UI", Font.BOLD, 15));
        return lb;
    }

    private JPanel infoBox(String title, JComponent value) {
        JPanel p = new JPanel(new MigLayout(
                "wrap 1, insets 10 12 10 12, fillx",
                "[grow,fill]",
                "[]4[]"
        ));
        p.setBackground(PANEL_BG);
        p.setBorder(BorderFactory.createLineBorder(BORDER));

        JLabel t = new JLabel(title);
        t.setForeground(MUTED);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        p.add(t, "growx");
        p.add(value, "growx");

        p.setMinimumSize(new Dimension(0, 58));
        p.setPreferredSize(new Dimension(10, 64));

        return p;
    }

    private void styleComboBox(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(new Color(0x14385D));
        combo.setForeground(FG);
        combo.setBorder(BorderFactory.createLineBorder(BORDER));
        combo.setPreferredSize(new Dimension(320, 38));
    }

    private JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(PRIMARY);
        b.setForeground(new Color(0x0B1F33));
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        return b;
    }

    private JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(BTN_SECONDARY);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        return b;
    }

    private String safe(String s) {
        return s == null || s.trim().isEmpty() ? "-" : s.trim();
    }
}