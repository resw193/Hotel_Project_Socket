package client.presentation.order;

import common.dto.OrderDTO;
import common.dto.OrderDetailRoomDTO;
import common.dto.OrderDetailServiceDTO;
import common.enums.BookingType;
import common.enums.OrderStatus;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class FormPayOrder extends JDialog {

    private static final Color SHELL_BG   = new Color(0x0B1F33);
    private static final Color SHELL_BRD  = new Color(0x274A6B);
    private static final Color PAPER_BG   = Color.WHITE;
    private static final Color PAPER_TXT  = new Color(0x111827);
    private static final Color PAPER_DIM  = new Color(0x6B7280);
    private static final Color PRIMARY    = new Color(0xB91C1C);

    private static final Font MONO         = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font MONO_BOLD    = MONO.deriveFont(Font.BOLD);
    private static final Font TITLE        = MONO_BOLD.deriveFont(26f);
    private static final Font SECTION      = MONO_BOLD.deriveFont(16f);
    private static final Font TABLE_HEADER = MONO_BOLD.deriveFont(16f);
    private static final Font HOTEL_NAME   = MONO_BOLD.deriveFont(18f);
    private static final Font INFO_FONT    = MONO_BOLD.deriveFont(14f);
    private static final Font ITEM_FONT    = MONO_BOLD.deriveFont(14f);
    private static final Font GRAND_VALUE  = MONO_BOLD.deriveFont(18f);

    private static final String[] DIGITS = {
            "không","một","hai","ba","bốn","năm","sáu","bảy","tám","chín"
    };
    private static final String[] UNITS = {
            "", "ngàn", "triệu", "tỷ", "ngàn tỷ", "triệu tỷ"
    };

    private final DateTimeFormatter headerDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final Double cashReceived;

    public FormPayOrder(Window owner,
                        OrderDTO order,
                        List<OrderDetailRoomDTO> roomLines,
                        List<OrderDetailServiceDTO> serviceLines,
                        String logoImagePath) {
        this(owner, order, roomLines, serviceLines, logoImagePath, null);
    }

    public FormPayOrder(Window owner,
                        OrderDTO order,
                        List<OrderDetailRoomDTO> roomLines,
                        List<OrderDetailServiceDTO> serviceLines,
                        String logoImagePath,
                        Double cashReceived) {
        super(owner, "HÓA ĐƠN THANH TOÁN – " + (order == null ? "" : order.getOrderId()), ModalityType.APPLICATION_MODAL);
        this.cashReceived = cashReceived;

        JPanel pnShell = new JPanel(new BorderLayout());
        pnShell.setBackground(SHELL_BG);

        JComponent paperWrap = buildPaper(order, roomLines, serviceLines, logoImagePath);

        JScrollPane scrollPane = new JScrollPane(paperWrap);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(SHELL_BG);
        scrollPane.setBorder(new LineBorder(SHELL_BRD));
        scrollPane.getVerticalScrollBar().setUnitIncrement(28);

        int paperW = Math.max(600, paperWrap.getPreferredSize().width);
        int frameH = Math.min(980, (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 120);
        scrollPane.setPreferredSize(new Dimension(paperW + 8, frameH));

        pnShell.add(scrollPane, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);

        JButton btnClose = new JButton("Đóng");
        btnClose.setBackground(new Color(0x2563EB));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> dispose());
        actions.add(btnClose);

        pnShell.add(actions, BorderLayout.SOUTH);
        setContentPane(pnShell);
        pack();
        setLocationRelativeTo(owner);
    }

    private JComponent buildPaper(OrderDTO order,
                                  List<OrderDetailRoomDTO> roomLines,
                                  List<OrderDetailServiceDTO> serviceLines,
                                  String logoImagePath) {
        int paperWidth = 840;

        JPanel paper = new JPanel(new MigLayout("wrap, insets 18 24 20 24", "[grow,fill]", ""));
        paper.setBackground(PAPER_BG);
        paper.setBorder(new EmptyBorder(10, 10, 10, 10));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        JPanel header = new JPanel(new MigLayout(
                "insets 0 0 8 0, gap 4",
                "[grow][230!]",
                "[][][][][]"
        ));
        header.setOpaque(false);

        JLabel lblLogo = new JLabel();
        if (logoImagePath != null && !logoImagePath.isEmpty()) {
            ImageIcon logo = loadIcon(logoImagePath, 100, 100);
            if (logo != null) {
                lblLogo.setIcon(logo);
                lblLogo.setHorizontalAlignment(SwingConstants.RIGHT);
            }
        }

        JLabel lblInvoiceTitle = new JLabel("HÓA ĐƠN");
        lblInvoiceTitle.setFont(TITLE);
        lblInvoiceTitle.setForeground(Color.BLACK);

        JLabel lblHotelName = new JLabel("KHÁCH SẠN MIMOSA");
        lblHotelName.setFont(HOTEL_NAME);
        lblHotelName.setForeground(PRIMARY);

        String customerName = order != null ? nullToEmpty(order.getCustomerName()) : "Khách lẻ";
        String ngayLap = (order != null && order.getOrderDate() != null)
                ? headerDateFormatter.format(order.getOrderDate())
                : "";

        header.add(lblInvoiceTitle, "cell 0 0, aligny top, alignx left");
        header.add(lblLogo,        "cell 1 0 1 2, alignx right, aligny top");
        header.add(lblHotelName,   "cell 0 1, alignx left");

        header.add(lbl("Tên khách hàng: " + customerName, MONO, PAPER_TXT),
                "cell 0 2, growx");
        header.add(lbl("Hóa đơn #" + (order != null ? order.getOrderId() : ""), INFO_FONT, PAPER_TXT),
                "cell 1 2, alignx right, gapright 10");

        header.add(lbl("Mã khách hàng: " + (order != null ? nullToEmpty(order.getCustomerId()) : "-"), MONO, PAPER_TXT),
                "cell 0 3, growx");
        header.add(lbl("Ngày lập hóa đơn: " + ngayLap, INFO_FONT, PAPER_TXT),
                "cell 1 3, alignx right, gapright 10");

        header.add(lbl("Thành phố Hồ Chí Minh", MONO, PAPER_TXT), "cell 0 4, span 2, gapy 2 0");

        paper.add(header, "growx");
        paper.add(new SolidDivider(), "growx, gaptop 4, gapbottom 8");

        paper.add(lbl("Các phòng đã đặt", SECTION, PAPER_TXT), "growx, gaptop 2");
        paper.add(rowHeader("Mục", "Số lượng", "Đơn giá", "Thành tiền"), "growx, gapbottom 8");

        double roomsSubtotal = 0d;
        double servicesSubtotal = 0d;

        if (roomLines != null && !roomLines.isEmpty()) {
            for (OrderDetailRoomDTO r : roomLines) {
                String roomName = nvl(r.getRoomDescription());
                String type = r.getBookingType() == null ? "" : r.getBookingType().getDisplayName();
                double fee = r.getRoomFee();

                if (fee <= 0) fee = calculateRoomFee(r);
                roomsSubtotal += fee;

                paper.add(rowItem(roomName + " (" + type + ")", "1",
                        money(fee) + "đ", money(fee) + "đ"), "growx");

                String timeLine = "  "
                        + (r.getCheckInDate() != null ? formatter.format(r.getCheckInDate()) : "")
                        + "  →  "
                        + (r.getCheckOutDate() != null ? formatter.format(r.getCheckOutDate()) : "");
                paper.add(lbl(timeLine, MONO, PAPER_DIM), "span 4, growx");
            }
        } else {
            paper.add(lbl("Không có phòng.", MONO, PAPER_DIM), "growx");
        }

        if (serviceLines != null && !serviceLines.isEmpty()) {
            paper.add(new DashedDivider(), "growx, gaptop 8, gapbottom 4");
            paper.add(lbl("Dịch vụ đã sử dụng", SECTION, PAPER_TXT), "growx, gaptop 2");
            paper.add(rowHeader("Mục", "Số lượng", "Giá", "Thành tiền"), "growx, gapbottom 8");

            Map<String, ServiceAgg> agg = new LinkedHashMap<>();
            for (OrderDetailServiceDTO s : serviceLines) {
                String name = nvl(s.getServiceName());
                int quantity = Math.max(0, s.getQuantity());
                double total = s.getServiceFee();
                double unit = quantity > 0 ? total / quantity : 0d;

                ServiceAgg a = agg.computeIfAbsent(name, k -> new ServiceAgg());
                a.qty += quantity;
                a.unit = unit;
                a.total += total;
            }

            for (Map.Entry<String, ServiceAgg> e : agg.entrySet()) {
                ServiceAgg a = e.getValue();
                servicesSubtotal += a.total;

                paper.add(rowItem(e.getKey(),
                        String.valueOf(a.qty),
                        money(a.unit) + "đ",
                        money(a.total) + "đ"), "growx");
            }
        }

        paper.add(new DashedDivider(), "growx, gaptop 8, gapbottom 4");

        double tong = roomsSubtotal + servicesSubtotal;
        double discountPercent = order == null ? 0d : order.getPromotionDiscount();
        if (discountPercent < 0) discountPercent = 0;
        if (discountPercent > 100) discountPercent = 100;

        double discountRate = discountPercent / 100.0;
        double tienKhuyenMai = tong * discountRate;
        double sauKhuyenMai = tong - tienKhuyenMai;
        double vat = sauKhuyenMai * 0.10;
        double thanhTien = sauKhuyenMai + vat;

        if (order != null && order.getOrderStatus() == OrderStatus.DA_THANH_TOAN && order.getTotal() > 0) {
            thanhTien = order.getTotal();
        }

        String promoTxt = (discountPercent == 0) ? "0%" : ((int) discountPercent) + "%";

        paper.add(rowSum("Tổng", money(tong) + "đ"), "growx");
        paper.add(rowSum("Khuyến mãi (" + promoTxt + ")", "-" + money(tienKhuyenMai) + "đ"), "growx");
        paper.add(rowSum("VAT (10%)", money(vat) + "đ"), "growx");

        paper.add(new SolidDivider(), "growx, gapy 4");
        paper.add(rowGrand("TỔNG THANH TOÁN", money(thanhTien) + "Đ"), "growx");

        boolean isPaid = order != null && order.getOrderStatus() == OrderStatus.DA_THANH_TOAN;
        double cashEff = this.cashReceived != null ? this.cashReceived : (isPaid ? thanhTien : 0d);
        double change = Math.max(0, cashEff - thanhTien);
        long thanhTienRounded = Math.round(thanhTien);

        paper.add(lbl("Bằng chữ: " + toVietnameseMoney(thanhTienRounded), MONO, PAPER_DIM), "growx, gaptop 4");
        paper.add(rowSum("Tiền khách đưa", money(cashEff) + "đ"), "growx");
        paper.add(rowSum("Tiền thối", money(change) + "đ"), "growx");

        paper.add(new SolidDivider(), "growx, gaptop 10, gapbottom 6");

        JPanel bottomInfo = new JPanel(new MigLayout("insets 0, gap 24", "[grow][grow]", "[]"));
        bottomInfo.setOpaque(false);

        JPanel paymentInfo = new JPanel(new MigLayout("insets 0, wrap, gapy 4", "[grow]", "[]"));
        paymentInfo.setOpaque(false);
        paymentInfo.add(lbl("Thông tin thanh toán", SECTION, PRIMARY), "gapy 0 8");
        paymentInfo.add(lbl("Ngân hàng MBBank", MONO_BOLD, PAPER_TXT));
        paymentInfo.add(lbl("Tên tài khoản: Khách sạn MIMOSA", MONO, PAPER_TXT));
        paymentInfo.add(lbl("Số tài khoản: 123-456-7890", MONO, PAPER_TXT));
        paymentInfo.add(lbl("Hạn thanh toán: 01/01/2028", MONO, PAPER_TXT));

        JPanel contactInfo = new JPanel(new MigLayout("insets 0, wrap, gapy 4", "[grow]", "[]"));
        contactInfo.setOpaque(false);
        contactInfo.add(lbl("Thông tin liên hệ", SECTION, PRIMARY), "gapy 0 8");
        contactInfo.add(lbl("abc@mimosahotel.com", MONO, PAPER_TXT));
        contactInfo.add(lbl("Số 298, đường Đầm Nại, Ninh Chữ, Ninh Hải, Ninh Thuận", MONO, PAPER_TXT));
        contactInfo.add(lbl("+84 123 456 789", MONO, PAPER_TXT));

        bottomInfo.add(paymentInfo, "grow");
        bottomInfo.add(contactInfo, "grow");

        paper.add(bottomInfo, "growx");

        JLabel lblFinalThank = lbl("Cảm ơn quý khách!", MONO_BOLD, PAPER_TXT);
        lblFinalThank.setHorizontalAlignment(SwingConstants.CENTER);
        paper.add(lblFinalThank, "growx, gaptop 12");

        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setBackground(SHELL_BG);

        JPanel holder = new JPanel(new BorderLayout());
        holder.setBackground(PAPER_BG);
        holder.setBorder(new LineBorder(new Color(0xDDDDDD)));
        holder.add(paper, BorderLayout.NORTH);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTH;
        wrap.add(holder, gbc);

        paper.doLayout();
        Dimension pref = paper.getPreferredSize();
        paper.setPreferredSize(new Dimension(paperWidth, pref.height + 24));

        return wrap;
    }

    private static String toVietnameseMoney(long n) {
        if (n == 0) return "không đồng";
        if (n < 0) return "âm " + toVietnameseMoney(-n);

        List<Integer> blocks = new ArrayList<>();
        while (n > 0) {
            blocks.add((int) (n % 1000));
            n /= 1000;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = blocks.size() - 1; i >= 0; i--) {
            int block = blocks.get(i);
            if (block == 0) continue;

            boolean full = (i != blocks.size() - 1);
            String part = readThreeDigits(block, full);
            if (!UNITS[i].isEmpty()) part += " " + UNITS[i];

            if (sb.length() > 0) sb.append(" ");
            sb.append(part);
        }
        return sb.toString().replaceAll("\\s+", " ").trim() + " đồng";
    }

    private static String readThreeDigits(int n, boolean full) {
        int tr = n / 100;
        int ch = (n % 100) / 10;
        int dv = n % 10;

        StringBuilder sb = new StringBuilder();

        if (tr > 0) {
            sb.append(DIGITS[tr]).append(" trăm");
        } else if (full && (ch > 0 || dv > 0)) {
            sb.append("không trăm");
        }

        if (ch > 1) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(DIGITS[ch]).append(" mươi");
            if (dv == 1) sb.append(" mốt");
            else if (dv == 4) sb.append(" tư");
            else if (dv == 5) sb.append(" lăm");
            else if (dv > 0) sb.append(" ").append(DIGITS[dv]);
        } else if (ch == 1) {
            if (sb.length() > 0) sb.append(" ");
            sb.append("mười");
            if (dv == 5) sb.append(" lăm");
            else if (dv > 0) sb.append(" ").append(DIGITS[dv]);
        } else {
            if (dv > 0) {
                if (sb.length() > 0) sb.append(" lẻ ");
                sb.append(DIGITS[dv]);
            }
        }
        return sb.toString().trim();
    }

    private JLabel lbl(String s, Font f, Color c) {
        JLabel l = new JLabel(s);
        l.setFont(f);
        l.setForeground(c);
        return l;
    }

    private JPanel rowHeader(String c1, String c2, String c3, String c4) {
        JPanel p = new JPanel(new MigLayout("insets 0, gap 8", "[grow]30[][150!][200!]", "[]"));
        p.setOpaque(false);
        p.add(lbl(c1, TABLE_HEADER, PAPER_TXT), "growx");
        p.add(lbl(c2, TABLE_HEADER, PAPER_TXT), "al center");
        p.add(lbl(c3, TABLE_HEADER, PAPER_TXT), "al right");
        p.add(lbl(c4, TABLE_HEADER, PAPER_TXT), "al right");
        return p;
    }

    private JPanel rowItem(String name, String qty, String price, String total) {
        JPanel p = new JPanel(new MigLayout("insets 0, gap 8", "[grow]30[][150!][200!]", "[]"));
        p.setOpaque(false);
        p.add(lbl(name, ITEM_FONT, PAPER_TXT), "growx");
        p.add(lbl(qty, ITEM_FONT, PAPER_TXT), "al center");
        p.add(lbl(price, ITEM_FONT, PAPER_TXT), "al right");
        p.add(lbl(total, ITEM_FONT, PAPER_TXT), "al right");
        return p;
    }

    private JPanel rowSum(String label, String value) {
        JPanel p = new JPanel(new MigLayout("insets 0, gap 8", "[grow][140!]", "[]"));
        p.setOpaque(false);
        p.add(lbl(label, MONO_BOLD, PAPER_TXT), "growx");
        p.add(lbl(value, MONO_BOLD, PAPER_TXT), "al right");
        return p;
    }

    private JPanel rowGrand(String label, String value) {
        JPanel p = new JPanel(new MigLayout("insets 0, gap 8", "[grow][140!]", "[]"));
        p.setOpaque(false);

        JLabel lblLeft = lbl(label, SECTION, PAPER_TXT);
        JLabel lblRight = lbl(value, GRAND_VALUE, PRIMARY);
        lblRight.setHorizontalAlignment(SwingConstants.RIGHT);

        p.add(lblLeft, "growx");
        p.add(lblRight, "al right");
        return p;
    }

    private static class DashedDivider extends JComponent {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0xBBBBBB));
            float[] dash = {4f, 4f};
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f));
            int y = getHeight() / 2;
            g2.drawLine(0, y, getWidth(), y);
            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(1, 8);
        }
    }

    private static class SolidDivider extends JComponent {
        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(new Color(0xCCCCCC));
            int y = getHeight() / 2;
            g.drawLine(0, y, getWidth(), y);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(1, 8);
        }
    }

    private static double calculateRoomFee(OrderDetailRoomDTO r) {
        try {
            if (r == null) return 0d;
            LocalDateTime in = r.getCheckInDate();
            LocalDateTime out = r.getCheckOutDate();
            if (in == null || out == null || !out.isAfter(in)) return 0d;

            if (r.getRoomFee() > 0) return r.getRoomFee();

            long duration = Duration.between(in, out).toHours();
            if (duration <= 0) return 0d;

            return 0d;
        } catch (Exception ex) {
            return Math.max(0d, r.getRoomFee());
        }
    }

    private static class ServiceAgg {
        int qty;
        double unit;
        double total;
    }

    private static String money(double v) {
        return String.format("%,.0f", v);
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private static ImageIcon loadIcon(String path, int w, int h) {
        try {
            java.net.URL url = FormPayOrder.class.getResource(path);
            if (url == null) return null;
            ImageIcon raw = new ImageIcon(url);
            Image scaled = raw.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception ex) {
            return null;
        }
    }
}