package client.presentation.roomBooking;

import client.network.socket.HotelClient;
import client.network.socket.SocketRequestExecutor;
import client.network.socket.SocketSessionManager;
import client.presentation.login.main.Application;
import common.dto.RoomCalendarSlotDTO;
import common.dto.RoomDTO;
import common.protocol.command.CommandType;
import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;
import server.core.service.RoomService;
import server.core.service.RoomStayService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class FormCalendarBooking extends JDialog {

    static final Color BG            = new Color(0x061522);
    static final Color CARD_BG       = new Color(0x102D4A);
    static final Color CARD_BG_ALT   = new Color(0x0D253D);
    static final Color GRID_LINE     = new Color(0x173956);
    static final Color TEXT_MAIN     = new Color(0xE9EEF6);
    static final Color TEXT_SUB      = new Color(0xB8C4D4);

    static final Color ROW_EVEN_BG        = new Color(0x102D4A);
    static final Color ROW_ODD_BG         = new Color(0x0A2035);
    static final Color WEEKEND_EVEN_BG    = new Color(0x153557);
    static final Color WEEKEND_ODD_BG     = new Color(0x102847);

    static final Color COLOR_BOOKED_BAR   = new Color(0xF2C94C);
    static final Color COLOR_CHECKIN_BAR  = new Color(0x27AE60);

    static final Color COLOR_CHECKIN_MARK  = new Color(0x4AA3FF);
    static final Color COLOR_CHECKOUT_MARK = new Color(0xFF6B6B);
    static final Color COLOR_TODAY_COL     = new Color(0x173C63);
    static final Color COLOR_WEEKEND_COL   = new Color(0x122B46);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final RoomService roomService;
    private final RoomStayService roomStayService;

    private LocalDate startDate = LocalDate.now();
    private int dayCount = 14;

    private JLabel lblRange;
    private JComboBox<Integer> cbxDays;
    private JButton btnPrev;
    private JButton btnNext;
    private JButton btnToday;

    private CalendarPanel calendarPanel;
    private JTable tableCheckinSoon;
    private JTable tableCheckoutSoon;

    public FormCalendarBooking(Window owner, RoomService roomService, RoomStayService roomStayService) {
        super(owner, "Lịch phòng – Đặt / Check-in / Check-out", ModalityType.APPLICATION_MODAL);
        this.roomService = roomService;
        this.roomStayService = roomStayService;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1650, 1000);
        setMinimumSize(new Dimension(1200, 700));
        setLocationRelativeTo(owner);
        setResizable(true);

        initUI();
        reloadData();
    }

    private void initUI() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(BG);

        JPanel pnTop = new JPanel(new BorderLayout());
        pnTop.setBackground(BG);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        left.setOpaque(false);

        JLabel title = new JLabel("Lịch đặt phòng & mốc Check-in / Check-out");
        title.setForeground(TEXT_MAIN);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        left.add(title);

        left.add(Box.createHorizontalStrut(18));

        btnPrev = new JButton("<");
        btnNext = new JButton(">");
        styleNavButton(btnPrev);
        styleNavButton(btnNext);

        lblRange = new JLabel();
        lblRange.setForeground(TEXT_MAIN);
        lblRange.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        cbxDays = new JComboBox<>(new Integer[]{7, 14, 30});
        cbxDays.setSelectedItem(dayCount);
        cbxDays.setBackground(CARD_BG);
        cbxDays.setForeground(TEXT_MAIN);

        JLabel lblRangeTitle = new JLabel("Khoảng thời gian:");
        lblRangeTitle.setForeground(TEXT_SUB);

        JLabel lblDaysTitle = new JLabel("Số ngày hiển thị:");
        lblDaysTitle.setForeground(TEXT_SUB);

        left.add(lblRangeTitle);
        left.add(btnPrev);
        left.add(lblRange);
        left.add(btnNext);
        left.add(Box.createHorizontalStrut(18));
        left.add(lblDaysTitle);
        left.add(cbxDays);

        btnToday = new JButton("Về hôm nay");
        styleTodayButton(btnToday);
        left.add(Box.createHorizontalStrut(8));
        left.add(btnToday);

        pnTop.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        right.setOpaque(false);
        right.add(makeLegendBox(COLOR_BOOKED_BAR, "Đặt"));
        right.add(makeLegendBox(COLOR_CHECKIN_BAR, "Check-in"));
        right.add(makeLegendLine(COLOR_CHECKIN_MARK, "Ngày Check-in"));
        right.add(makeLegendLine(COLOR_CHECKOUT_MARK, "Ngày Check-out"));
        pnTop.add(right, BorderLayout.EAST);

        getContentPane().add(pnTop, BorderLayout.NORTH);

        calendarPanel = new CalendarPanel();
        JScrollPane scrollCalendar = new JScrollPane(calendarPanel);
        scrollCalendar.getViewport().setBackground(BG);
        scrollCalendar.setBorder(BorderFactory.createEmptyBorder(8, 16, 4, 16));
        scrollCalendar.getHorizontalScrollBar().setUnitIncrement(20);
        scrollCalendar.getVerticalScrollBar().setUnitIncrement(16);
        getContentPane().add(scrollCalendar, BorderLayout.CENTER);

        tableCheckinSoon = createBaseTable(new String[]{"Ngày check-in", "Phòng", "Khách hàng", "Trạng thái", "Loại đặt"});
        tableCheckoutSoon = createBaseTable(new String[]{"Ngày check-out", "Phòng", "Khách hàng", "Trạng thái", "Loại đặt"});

        JScrollPane scrollIn = wrapTableWithTitle(tableCheckinSoon, "Phòng sắp Check-in trong khoảng đang xem");
        JScrollPane scrollOut = wrapTableWithTitle(tableCheckoutSoon, "Phòng sắp Check-out trong khoảng đang xem");

        JPanel pnBottom = new JPanel(new GridLayout(1, 2, 16, 0));
        pnBottom.setBorder(BorderFactory.createEmptyBorder(4, 16, 12, 16));
        pnBottom.setBackground(BG);
        pnBottom.add(scrollIn);
        pnBottom.add(scrollOut);
        getContentPane().add(pnBottom, BorderLayout.SOUTH);

        btnPrev.addActionListener(this::onPrevRange);
        btnNext.addActionListener(this::onNextRange);
        btnToday.addActionListener(this::onGoToday);

        cbxDays.addActionListener(e -> {
            Integer v = (Integer) cbxDays.getSelectedItem();
            if (v != null && v > 0) {
                dayCount = v;
                reloadData();
            }
        });
    }

    private JTable createBaseTable(String[] columns) {
        JTable t = new JTable(new DefaultTableModel(new Object[][]{}, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        t.setRowHeight(26);
        t.setForeground(TEXT_MAIN);
        t.setBackground(CARD_BG);
        t.setGridColor(GRID_LINE);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.getTableHeader().setForeground(TEXT_MAIN);
        t.getTableHeader().setBackground(new Color(0x12355A));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        return t;
    }

    private JScrollPane wrapTableWithTitle(JTable table, String title) {
        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(100, 170));
        sp.getViewport().setBackground(CARD_BG);
        sp.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GRID_LINE),
                title,
                0, 0,
                new Font("Segoe UI", Font.PLAIN, 12),
                TEXT_SUB
        ));
        return sp;
    }

    private JPanel makeLegendBox(Color color, String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        JComponent box = new JComponent() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(color);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
            }
            @Override public Dimension getPreferredSize() { return new Dimension(16, 10); }
        };
        JLabel lb = new JLabel(text);
        lb.setForeground(TEXT_SUB);
        p.add(box);
        p.add(lb);
        return p;
    }

    private JPanel makeLegendLine(Color color, String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        JComponent line = new JComponent() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(color);
                g2.fillRect(0, getHeight() / 2 - 1, getWidth(), 3);
            }
            @Override public Dimension getPreferredSize() { return new Dimension(18, 10); }
        };
        JLabel lb = new JLabel(text);
        lb.setForeground(TEXT_SUB);
        p.add(line);
        p.add(lb);
        return p;
    }

    private void styleNavButton(JButton b) {
        b.setBackground(CARD_BG);
        b.setForeground(TEXT_MAIN);
        b.setFocusable(false);
        b.setBorder(BorderFactory.createLineBorder(GRID_LINE));
        b.setPreferredSize(new Dimension(32, 26));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleTodayButton(JButton b) {
        b.setBackground(new Color(0x1B4F72));
        b.setForeground(TEXT_MAIN);
        b.setFocusable(false);
        b.setBorder(BorderFactory.createLineBorder(GRID_LINE));
        b.setPreferredSize(new Dimension(95, 26));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void onPrevRange(ActionEvent e) {
        startDate = startDate.minusDays(dayCount);
        reloadData();
    }

    private void onNextRange(ActionEvent e) {
        startDate = startDate.plusDays(dayCount);
        reloadData();
    }

    private void onGoToday(ActionEvent e) {
        startDate = LocalDate.now();
        reloadData();
    }

    private void reloadData() {
        try {
            LocalDate endDate = startDate.plusDays(dayCount - 1);
            lblRange.setText(startDate.format(DATE_FMT) + "  –  " + endDate.format(DATE_FMT));

            BaseResponse roomRes = sendRequest(CommandType.GET_ALL_ROOMS, null);
            if (!roomRes.isSuccess()) {
                throw new RuntimeException(roomRes.getMessage());
            }

            @SuppressWarnings("unchecked")
            List<RoomDTO> rooms = (List<RoomDTO>) roomRes.getData();
            if (rooms == null) rooms = Collections.emptyList();

            BaseResponse calRes = sendRequest(
                    CommandType.GET_ROOM_CALENDAR,
                    new common.dto.request_dto.RoomCalendarRequestDTO(startDate, endDate)
            );
            if (!calRes.isSuccess()) {
                throw new RuntimeException(calRes.getMessage());
            }

            @SuppressWarnings("unchecked")
            List<RoomCalendarSlotDTO> slots = (List<RoomCalendarSlotDTO>) calRes.getData();
            if (slots == null) slots = Collections.emptyList();

            rooms = rooms.stream()
                    .sorted(Comparator.comparing(RoomDTO::getRoomId))
                    .collect(Collectors.toList());

            calendarPanel.setModel(rooms, slots, startDate, dayCount);

            DefaultTableModel mIn = (DefaultTableModel) tableCheckinSoon.getModel();
            mIn.setRowCount(0);
            slots.stream()
                    .filter(s -> s.getCheckIn() != null)
                    .filter(s -> {
                        LocalDate d = s.getCheckIn().toLocalDate();
                        return !d.isBefore(startDate) && !d.isAfter(endDate);
                    })
                    .sorted(Comparator.comparing(RoomCalendarSlotDTO::getCheckIn))
                    .forEach(s -> mIn.addRow(new Object[]{
                            s.getCheckIn().format(DATETIME_FMT),
                            s.getRoomId(),
                            s.getCustomer(),
                            s.getStatus(),
                            s.getBookingType()
                    }));

            DefaultTableModel mOut = (DefaultTableModel) tableCheckoutSoon.getModel();
            mOut.setRowCount(0);
            slots.stream()
                    .filter(s -> s.getCheckOut() != null)
                    .filter(s -> {
                        LocalDate d = s.getCheckOut().toLocalDate();
                        return !d.isBefore(startDate) && !d.isAfter(endDate);
                    })
                    .sorted(Comparator.comparing(RoomCalendarSlotDTO::getCheckOut))
                    .forEach(s -> mOut.addRow(new Object[]{
                            s.getCheckOut().format(DATETIME_FMT),
                            s.getRoomId(),
                            s.getCustomer(),
                            s.getStatus(),
                            s.getBookingType()
                    }));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return SocketSessionManager.send(BaseRequest.of(commandType, data));
    }

    private static class CalendarPanel extends JPanel {
        private List<RoomDTO> rooms = new ArrayList<>();
        private Map<String, List<RoomCalendarSlotDTO>> dataByRoom = new HashMap<>();
        private LocalDate startDate = LocalDate.now();
        private int dayCount = 14;

        private final int labelWidth = 110;
        private final int headerHeight = 40;
        private final int rowHeight = 44;
        private final int minColWidth = 80;

        public CalendarPanel() {
            setOpaque(true);
            setBackground(BG);
            setToolTipText("");
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    updateTooltip(e.getX(), e.getY());
                }
            });
        }

        public void setModel(List<RoomDTO> rooms, List<RoomCalendarSlotDTO> slots, LocalDate startDate, int dayCount) {
            this.rooms = rooms == null ? new ArrayList<>() : new ArrayList<>(rooms);
            this.startDate = startDate == null ? LocalDate.now() : startDate;
            this.dayCount = Math.max(1, dayCount);

            dataByRoom.clear();
            if (slots != null) {
                for (RoomCalendarSlotDTO s : slots) {
                    dataByRoom.computeIfAbsent(s.getRoomId(), k -> new ArrayList<>()).add(s);
                }
            }

            int colWidth = Math.max(minColWidth, 80);
            int w = labelWidth + colWidth * this.dayCount + 40;
            int h = headerHeight + rowHeight * this.rooms.size() + 40;
            setPreferredSize(new Dimension(w, h));

            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            int width = getWidth();

            g2.setColor(BG);
            g2.fillRect(0, 0, width, getHeight());

            int colWidth = Math.max(minColWidth, (width - labelWidth - 24) / Math.max(dayCount, 1));
            LocalDate today = LocalDate.now();

            g2.setColor(CARD_BG);
            g2.fillRect(0, 0, labelWidth, headerHeight);
            g2.setColor(TEXT_SUB);
            g2.drawString("Phòng", 10, headerHeight - 12);

            for (int i = 0; i < dayCount; i++) {
                LocalDate d = startDate.plusDays(i);
                int x = labelWidth + i * colWidth;
                boolean isWeekend = d.getDayOfWeek() == DayOfWeek.SATURDAY || d.getDayOfWeek() == DayOfWeek.SUNDAY;
                boolean isToday = d.equals(today);

                Color headerBg = CARD_BG;
                if (isWeekend) headerBg = COLOR_WEEKEND_COL;
                if (isToday) headerBg = COLOR_TODAY_COL;

                g2.setColor(headerBg);
                g2.fillRect(x, 0, colWidth, headerHeight);
                g2.setColor(GRID_LINE);
                g2.drawRect(x, 0, colWidth, headerHeight);

                g2.setColor(TEXT_MAIN);
                g2.drawString(d.format(DAY_FMT), x + 10, 15);
                g2.setColor(TEXT_SUB);
                g2.drawString(shortDow(d.getDayOfWeek()), x + 10, headerHeight - 10);
            }

            for (int row = 0; row < rooms.size(); row++) {
                RoomDTO r = rooms.get(row);
                int y = headerHeight + row * rowHeight;

                g2.setColor((row % 2 == 0) ? ROW_EVEN_BG : ROW_ODD_BG);
                g2.fillRect(0, y, labelWidth, rowHeight);
                g2.setColor(GRID_LINE);
                g2.drawRect(0, y, labelWidth, rowHeight);
                g2.setColor(TEXT_MAIN);
                g2.drawString(r.getRoomId(), 10, y + rowHeight - 12);

                for (int i = 0; i < dayCount; i++) {
                    LocalDate d = startDate.plusDays(i);
                    int x = labelWidth + i * colWidth;
                    boolean isWeekend = d.getDayOfWeek() == DayOfWeek.SATURDAY || d.getDayOfWeek() == DayOfWeek.SUNDAY;
                    g2.setColor((row % 2 == 0)
                            ? (isWeekend ? WEEKEND_EVEN_BG : ROW_EVEN_BG)
                            : (isWeekend ? WEEKEND_ODD_BG : ROW_ODD_BG));
                    g2.fillRect(x, y, colWidth, rowHeight);
                    g2.setColor(GRID_LINE);
                    g2.drawRect(x, y, colWidth, rowHeight);
                }

                List<RoomCalendarSlotDTO> slots = dataByRoom.getOrDefault(r.getRoomId(), Collections.emptyList());
                for (RoomCalendarSlotDTO slot : slots) {
                    if (slot.getCheckIn() == null || slot.getCheckOut() == null) continue;

                    LocalDate from = slot.getCheckIn().toLocalDate();
                    LocalDate to = slot.getCheckOut().toLocalDate();

                    int startCol = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, from);
                    int endCol = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, to);

                    startCol = Math.max(0, startCol);
                    endCol = Math.min(dayCount - 1, endCol);

                    if (startCol > endCol) continue;

                    int x = labelWidth + startCol * colWidth + 4;
                    int w = (endCol - startCol + 1) * colWidth - 8;
                    int barY = y + 12;
                    int barH = 18;

                    g2.setColor("Check-in".equalsIgnoreCase(slot.getStatus()) ? COLOR_CHECKIN_BAR : COLOR_BOOKED_BAR);
                    g2.fillRoundRect(x, barY, w, barH, 10, 10);

                    g2.setColor(COLOR_CHECKIN_MARK);
                    int inCol = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, slot.getCheckIn().toLocalDate());
                    if (inCol >= 0 && inCol < dayCount) {
                        int markX = labelWidth + inCol * colWidth + 2;
                        g2.fillRect(markX, y + 4, 3, rowHeight - 8);
                    }

                    g2.setColor(COLOR_CHECKOUT_MARK);
                    int outCol = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, slot.getCheckOut().toLocalDate());
                    if (outCol >= 0 && outCol < dayCount) {
                        int markX = labelWidth + outCol * colWidth + colWidth - 5;
                        g2.fillRect(markX, y + 4, 3, rowHeight - 8);
                    }
                }
            }
            g2.dispose();
        }

        private void updateTooltip(int x, int y) {
            if (y < headerHeight) {
                setToolTipText(null);
                return;
            }
            int row = (y - headerHeight) / rowHeight;
            if (row < 0 || row >= rooms.size()) {
                setToolTipText(null);
                return;
            }
            int colWidth = Math.max(minColWidth, (getWidth() - labelWidth - 24) / Math.max(dayCount, 1));
            int col = (x - labelWidth) / colWidth;
            if (col < 0 || col >= dayCount) {
                setToolTipText(null);
                return;
            }

            RoomDTO room = rooms.get(row);
            LocalDate date = startDate.plusDays(col);
            List<RoomCalendarSlotDTO> slots = dataByRoom.getOrDefault(room.getRoomId(), Collections.emptyList());

            for (RoomCalendarSlotDTO slot : slots) {
                if (slot.getCheckIn() == null || slot.getCheckOut() == null) continue;
                LocalDate from = slot.getCheckIn().toLocalDate();
                LocalDate to = slot.getCheckOut().toLocalDate();
                if ((date.isEqual(from) || date.isAfter(from)) && (date.isEqual(to) || date.isBefore(to))) {
                    setToolTipText("<html>"
                            + "<b>Phòng:</b> " + room.getRoomId() + "<br>"
                            + "<b>Khách:</b> " + safe(slot.getCustomer()) + "<br>"
                            + "<b>Trạng thái:</b> " + safe(slot.getStatus()) + "<br>"
                            + "<b>Loại đặt:</b> " + safe(slot.getBookingType()) + "<br>"
                            + "<b>Check-in:</b> " + slot.getCheckIn().format(DATETIME_FMT) + "<br>"
                            + "<b>Check-out:</b> " + slot.getCheckOut().format(DATETIME_FMT)
                            + "</html>");
                    return;
                }
            }
            setToolTipText(null);
        }

        private static String shortDow(DayOfWeek d) {
            return switch (d) {
                case MONDAY -> "T2";
                case TUESDAY -> "T3";
                case WEDNESDAY -> "T4";
                case THURSDAY -> "T5";
                case FRIDAY -> "T6";
                case SATURDAY -> "T7";
                case SUNDAY -> "CN";
            };
        }

        private static String safe(String s) {
            return s == null ? "-" : s;
        }
    }
}