package client.presentation.roomBooking;

import client.network.socket.SocketSessionManager;
import common.dto.OdrInfoDTO;
import net.miginfocom.swing.MigLayout;
import server.core.service.RoomStayService;
import client.network.socket.HotelClient;
import client.network.socket.SocketRequestExecutor;
import client.presentation.login.main.Application;
import common.dto.request_dto.OdrIdRequestDTO;
import common.protocol.command.CommandType;
import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class FormRoomPendingBookingsDialog extends JDialog {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Color BG = new Color(0x0B1F33);
    private static final Color CARD_BG = new Color(0x102D4A);
    private static final Color CARD_BG_2 = new Color(0x12355A);
    private static final Color BORDER = new Color(0x274A6B);
    private static final Color FG = new Color(0xE9EEF6);
    private static final Color MUTED = new Color(0xB8C4D4);
    private static final Color PRIMARY = new Color(0xF5C452);
    private static final Color HOVER = new Color(0xFFD36E);

    private static final Color SUCCESS = new Color(0x16A34A);
    private static final Color SUCCESS_HOVER = new Color(0x22C55E);

    private static final Color DANGER = new Color(0xDC2626);
    private static final Color DANGER_HOVER = new Color(0xEF4444);

    private static final Color BADGE_SOON_BG = new Color(0xF59E0B);
    private static final Color BADGE_SOON_FG = new Color(0x0B1F33);

    private static final Color BADGE_TODAY_BG = new Color(0x22C55E);
    private static final Color BADGE_TODAY_FG = Color.WHITE;

    private static final Color BADGE_FUTURE_BG = new Color(0x2563EB);
    private static final Color BADGE_FUTURE_FG = Color.WHITE;

    public FormRoomPendingBookingsDialog(Window owner,
                                         String roomId,
                                         List<OdrInfoDTO> bookings,
                                         RoomStayService roomStayService,
                                         Runnable onChanged) {
        super(owner, "Danh sách khách đã đặt - " + roomId, ModalityType.APPLICATION_MODAL);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(920, 640);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBackground(BG);
        root.setBorder(new EmptyBorder(14, 14, 14, 14));
        setContentPane(root);

        List<OdrInfoDTO> sortedBookings = bookings == null
                ? List.of()
                : bookings.stream()
                .sorted(Comparator.comparing(OdrInfoDTO::getCheckIn,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        root.add(buildHeader(roomId, sortedBookings.size()), BorderLayout.NORTH);
        root.add(buildCenter(sortedBookings, roomStayService, onChanged), BorderLayout.CENTER);
        root.add(buildFooter(sortedBookings.size()), BorderLayout.SOUTH);
    }

    private JComponent buildHeader(String roomId, int totalBookings) {
        JPanel header = new JPanel(new MigLayout("insets 14 18 14 18, fillx", "[grow][]", "[][]"));
        header.setBackground(CARD_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(4, 4, 4, 4)
        ));

        JLabel lblTitle = new JLabel("Danh sách khách hàng đã đặt phòng");
        lblTitle.setForeground(FG);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel lblRoom = new JLabel("Phòng: " + roomId);
        lblRoom.setForeground(PRIMARY);
        lblRoom.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JLabel lblCount = new JLabel("Tổng booking: " + totalBookings);
        lblCount.setForeground(MUTED);
        lblCount.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel lblHint = new JLabel("Chọn Check-in hoặc Hủy đặt cho từng booking tương ứng");
        lblHint.setForeground(MUTED);
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 13));

        header.add(lblTitle, "growx");
        header.add(lblRoom, "right, wrap");
        header.add(lblCount, "split 2");
        header.add(lblHint, "right");

        return header;
    }

    private JComponent buildCenter(List<OdrInfoDTO> bookings,
                                   RoomStayService roomStayService,
                                   Runnable onChanged) {

        JPanel listPanel = new JPanel(new MigLayout("wrap 1, insets 8, gapy 12, fillx", "[grow]", ""));
        listPanel.setBackground(BG);

        if (bookings == null || bookings.isEmpty()) {
            JPanel emptyPanel = new JPanel(new MigLayout("insets 30, center, wrap", "[center]", "[][]"));
            emptyPanel.setBackground(CARD_BG);
            emptyPanel.setBorder(new LineBorder(BORDER, 1, true));

            JLabel lblEmpty = new JLabel("Không có booking nào cho phòng này");
            lblEmpty.setForeground(FG);
            lblEmpty.setFont(new Font("Segoe UI", Font.BOLD, 18));

            JLabel lblSub = new JLabel("Danh sách booking đang trống.");
            lblSub.setForeground(MUTED);
            lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));

            emptyPanel.add(lblEmpty);
            emptyPanel.add(lblSub);

            listPanel.add(emptyPanel, "growx");
        } else {
            int index = 1;
            for (OdrInfoDTO odr : bookings) {
                listPanel.add(buildBookingCard(index++, odr, roomStayService, onChanged), "growx");
            }
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);

        JScrollBar vBar = scroll.getVerticalScrollBar();
        vBar.setUnitIncrement(32);
        vBar.setBlockIncrement(160);

        return scroll;
    }

    private JComponent buildFooter(int totalBookings) {
        JPanel footer = new JPanel(new MigLayout("insets 10 16 10 16, fillx", "[grow][]", "[]"));
        footer.setBackground(CARD_BG);
        footer.setBorder(new LineBorder(BORDER, 1, true));

        JLabel lblSummary = new JLabel("Tổng số booking hiển thị: " + totalBookings);
        lblSummary.setForeground(MUTED);
        lblSummary.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JButton btnClose = new JButton("Đóng");
        styleNeutralButton(btnClose);
        btnClose.addActionListener(e -> dispose());

        footer.add(lblSummary, "growx");
        footer.add(btnClose, "w 120!, h 40!");

        return footer;
    }

    private JPanel buildBookingCard(int index,
                                    OdrInfoDTO odr,
                                    RoomStayService roomStayService,
                                    Runnable onChanged) {

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG_2);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JPanel top = new JPanel(new MigLayout("insets 0, fillx", "[grow]", "[]"));
        top.setOpaque(false);

        JLabel lblIndex = new JLabel("Booking #" + index);
        lblIndex.setForeground(PRIMARY);
        lblIndex.setFont(new Font("Segoe UI", Font.BOLD, 18));
        top.add(lblIndex, "left");

        JPanel content = new JPanel(new MigLayout("insets 12 0 0 0, fillx", "[grow][320!]", "[grow]"));
        content.setOpaque(false);

        JPanel leftInfo = new JPanel(new MigLayout(
                "insets 0, wrap 2, fillx, gapy 8",
                "[140!]12[grow]",
                "[][][][][]"
        ));
        leftInfo.setOpaque(false);

        leftInfo.add(label("Mã khách hàng:"));
        leftInfo.add(value(odr.getCustomerId()));

        leftInfo.add(label("Tên khách hàng:"));
        leftInfo.add(value(odr.getFullName()));

        leftInfo.add(label("Số điện thoại:"));
        leftInfo.add(value(odr.getPhone()));

        leftInfo.add(label("Kiểu đặt phòng:"));
        leftInfo.add(value(formatBookingType(odr.getBookingType())));

        leftInfo.add(label("Tiền phòng:"));
        leftInfo.add(value(formatMoney(odr.getRoomFee())));

        JPanel rightInfo = new JPanel(new MigLayout(
                "insets 0, wrap 1, fillx, gapy 10",
                "[grow]",
                "[][][][]"
        ));
        rightInfo.setOpaque(false);

        JPanel badgeWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgeWrap.setOpaque(false);
        badgeWrap.add(buildBookingBadge(odr));

        JLabel lblCheckIn = new JLabel("Check-in: " + formatDateTime(odr.getCheckIn()));
        lblCheckIn.setForeground(FG);
        lblCheckIn.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JLabel lblCheckOut = new JLabel("Check-out: " + formatDateTime(odr.getCheckOut()));
        lblCheckOut.setForeground(FG);
        lblCheckOut.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(8, 0, 0, 0));

        JButton btnCheckIn = new JButton("Check-in");
        styleSuccessButton(btnCheckIn);
        btnCheckIn.addActionListener(e -> {
            try {
                BaseResponse response = sendRequest(
                        CommandType.CHECK_IN_BY_ODR_ID,
                        new OdrIdRequestDTO(odr.getOrderDetailRoomId())
                );

                JOptionPane.showMessageDialog(this,
                        response.isSuccess() ? "Check-in thành công." : response.getMessage());

                if (response.isSuccess()) {
                    dispose();
                    if (onChanged != null) onChanged.run();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnCancel = new JButton("Hủy đặt");
        styleDangerButton(btnCancel);
        btnCancel.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Xác nhận hủy booking của khách " + safe(odr.getFullName()) + "?\n" + formatRange(odr),
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    BaseResponse response = sendRequest(
                            CommandType.CANCEL_BOOKING_BY_ODR_ID,
                            new OdrIdRequestDTO(odr.getOrderDetailRoomId())
                    );

                    JOptionPane.showMessageDialog(this,
                            response.isSuccess() ? "Đã hủy booking." : response.getMessage());

                    if (response.isSuccess()) {
                        dispose();
                        if (onChanged != null) onChanged.run();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        actions.add(btnCheckIn);
        actions.add(btnCancel);

        rightInfo.add(badgeWrap);
        rightInfo.add(lblCheckIn);
        rightInfo.add(lblCheckOut);
        rightInfo.add(actions);

        content.add(leftInfo, "growx, top");
        content.add(rightInfo, "growx, top");

        card.add(top, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);

        return card;
    }

    private JLabel buildBookingBadge(OdrInfoDTO odr) {
        JLabel badge = new JLabel(getBadgeText(odr), SwingConstants.CENTER);
        badge.setOpaque(true);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        badge.setBorder(new EmptyBorder(4, 10, 4, 10));

        Color bg;
        Color fg;

        int dayDiff = getDaysUntilCheckIn(odr);

        if (dayDiff == 0) {
            bg = BADGE_TODAY_BG;
            fg = BADGE_TODAY_FG;
        } else if (dayDiff > 0 && dayDiff <= 2) {
            bg = BADGE_SOON_BG;
            fg = BADGE_SOON_FG;
        } else {
            bg = BADGE_FUTURE_BG;
            fg = BADGE_FUTURE_FG;
        }

        badge.setBackground(bg);
        badge.setForeground(fg);
        return badge;
    }

    private String getBadgeText(OdrInfoDTO odr) {
        int dayDiff = getDaysUntilCheckIn(odr);

        if (dayDiff == 0) {
            return "Check-in hôm nay";
        }
        if (dayDiff > 0 && dayDiff <= 2) {
            return "Sắp check-in";
        }
        if (dayDiff > 2) {
            return "Booking tương lai";
        }
        return "Đã tới hạn";
    }

    private int getDaysUntilCheckIn(OdrInfoDTO odr) {
        if (odr == null || odr.getCheckIn() == null) return Integer.MAX_VALUE;
        Duration duration = Duration.between(LocalDateTime.now(), odr.getCheckIn());
        long hours = duration.toHours();

        if (hours < 0) return -1;
        return (int) Math.floor(hours / 24.0);
    }

    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return SocketSessionManager.send(BaseRequest.of(commandType, data));
    }

    private void styleSuccessButton(JButton button) {
        button.setBackground(SUCCESS);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(120, 40));
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(SUCCESS_HOVER.darker(), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(SUCCESS_HOVER);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(SUCCESS);
            }
        });
    }

    private void styleDangerButton(JButton button) {
        button.setBackground(DANGER);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(120, 40));
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(DANGER_HOVER.darker(), 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(DANGER_HOVER);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(DANGER);
            }
        });
    }

    private void styleNeutralButton(JButton button) {
        button.setBackground(PRIMARY);
        button.setForeground(new Color(0x0B1F33));
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBorder(BorderFactory.createLineBorder(new Color(0xF1B93A), 1));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(HOVER);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(PRIMARY);
            }
        });
    }

    private JLabel label(String text) {
        JLabel lb = new JLabel(text);
        lb.setForeground(PRIMARY);
        lb.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return lb;
    }

    private JLabel value(String text) {
        JLabel lb = new JLabel(safe(text));
        lb.setForeground(FG);
        lb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return lb;
    }

    private String formatRange(OdrInfoDTO odr) {
        return "Check-in: " + formatDateTime(odr.getCheckIn()) + "   |   Check-out: " + formatDateTime(odr.getCheckOut());
    }

    private String formatDateTime(LocalDateTime dt) {
        return dt == null ? "-" : DTF.format(dt);
    }

    private String formatBookingType(String bookingType) {
        if (bookingType == null || bookingType.isBlank()) return "-";

        return switch (bookingType.trim().toUpperCase()) {
            case "NGAY" -> "Ngày";
            case "DEM" -> "Đêm";
            case "GIO" -> "Giờ";
            default -> bookingType;
        };
    }

    private String formatMoney(double value) {
        return String.format("%,.0f đ", value);
    }

    private String safe(String text) {
        return text == null || text.isBlank() ? "-" : text;
    }
}