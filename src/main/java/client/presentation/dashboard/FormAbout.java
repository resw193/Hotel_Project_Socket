package client.presentation.dashboard;

import com.formdev.flatlaf.FlatClientProperties;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Locale;

public class FormAbout extends JPanel {

    private static final Color BG         = new Color(0x0B1F33);
    private static final Color HERO_C1    = new Color(0x4C1D95);
    private static final Color HERO_C2    = new Color(0x0EA5E9);
    private static final Color CARD_BG    = new Color(0x0F1B2D);
    private static final Color CARD_GLOW1 = new Color(0x9333EA);
    private static final Color CARD_GLOW2 = new Color(0x22D3EE);
    private static final Color TEXT       = new Color(0xEAF2FF);
    private static final Color MUTED      = new Color(0xBBD0E7);
    private static final Color ACCENT     = new Color(0x5EEAD4);
    private static final Color BORDER     = new Color(0x17324C);

    public FormAbout() {
        setBackground(BG);
        setLayout(new MigLayout("fill, wrap, insets 0", "[grow]", "[][grow]"));

        JPanel pnHero = new HeroPanel(HERO_C1, HERO_C2);
        pnHero.setLayout(new MigLayout("insets 20 24 24 24, fillx, wrap", "[grow,fill]", "[]8[]8[]8[]"));
        add(pnHero, "growx");

        JLabel lblTitle = new JLabel("MIMOSA HOTEL – ABOUT");
        lblTitle.setForeground(TEXT);
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 22f));
        pnHero.add(lblTitle, "growx");

        JLabel lblSubTitle = new JLabel("Khách sạn Mimosa • Java Distributed • NoSQL Neo4j");
        lblSubTitle.setForeground(new Color(255, 255, 255, 210));
        pnHero.add(lblSubTitle, "growx");

        JPanel pnBadges = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnBadges.setOpaque(false);
        pnBadges.add(pill("Java Distributed"));
        pnBadges.add(pill("NoSQL Neo4j"));
        pnBadges.add(pill("Khoa CNTT"));
        pnHero.add(pnBadges, "growx");

        JPanel pnSupport = new GlassCard();
        pnSupport.setLayout(new MigLayout("insets 10, gap 8", "[grow]", "[]"));
        pnSupport.add(kv("Phiên bản", "1.0.0"), "growx");
        pnSupport.add(kv("Hỗ trợ", "Quản lý phòng, đặt phòng, dịch vụ, khách hàng"), "growx");
        pnSupport.add(kv("24/7", "Lễ tân – đặt phòng"), "growx");
        pnHero.add(pnSupport, "growx, gaptop 6");

        JPanel pnBody = new JPanel(new MigLayout("insets 18 20 20 20, gapy 18, fillx, wrap", "[grow,fill]"));
        pnBody.setOpaque(false);

        JPanel pnCardIntro = card("Giới thiệu Khách sạn Mimosa");
        pnCardIntro.add(gradientDivider(), "growx, h 4!, gaptop 2, gapbottom 6");
        pnCardIntro.add(text(
                "Khách sạn Mimosa tọa lạc tại số 298, đường Đầm Nại, Ninh Chữ, Ninh Hải, Ninh Thuận.",
                "Cung cấp nhiều loại phòng, phù hợp gia đình, cặp đôi hoặc công tác.",
                "Dịch vụ bữa sáng tự chọn và lễ tân 24/7 mang đến trải nghiệm thoải mái và tiện nghi."
        ), "growx");
        pnBody.add(pnCardIntro, "growx");

        JPanel pnCardInfoApp = card("Thông tin ứng dụng");
        pnCardInfoApp.add(gradientDivider(), "growx, h 4!, gaptop 2, gapbottom 6");
        pnCardInfoApp.add(text(
                "Ứng dụng: Hotel Management System - Mimosa",
                "Phiên bản: 1.0.0",
                "Mô tả: Hỗ trợ quản lý dịch vụ, phòng, đặt phòng và khách hàng trực quan.",
                "Nền tảng: Java Distributed"
        ), "growx");
        pnBody.add(pnCardInfoApp, "growx");

        JPanel pnCardHardware = card("Yêu cầu phần cứng");
        pnCardHardware.add(gradientDivider(), "growx, h 4!, gaptop 2, gapbottom 6");
        JPanel gridHW = new JPanel(new MigLayout("insets 0, gap 8", "[right]16[grow,fill]", "[][][][]"));
        gridHW.setOpaque(false);
        gridHW.add(kvKey("CPU"));          gridHW.add(kvVal("Intel Core i5, 3.4 GHz"), "wrap");
        gridHW.add(kvKey("RAM"));          gridHW.add(kvVal("16 GB"), "wrap");
        gridHW.add(kvKey("Ổ cứng"));       gridHW.add(kvVal("360 GB"), "wrap");
        gridHW.add(kvKey("Hệ kiến trúc")); gridHW.add(kvVal("64 bit"), "wrap");
        pnCardHardware.add(gridHW, "growx");
        pnBody.add(pnCardHardware, "growx");

        JPanel pnCardSoftware = card("Yêu cầu phần mềm");
        pnCardSoftware.add(gradientDivider(), "growx, h 4!, gaptop 2, gapbottom 6");
        JTable tbl = new JTable(new DefaultTableModel(
                new Object[][]{
                        {"Eclipse IDE for Java EE Developers", "4.28", "IDE cho Java"},
                        {"Neo4j", "19.0.1084.56", "NoSQL"},
                        {"Microsoft Windows 11", "11/11", "Hệ điều hành"}
                },
                new String[]{"Tên phần mềm", "Phiên bản", "Loại"}
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        });

        styleTable(tbl);
        JScrollPane scrollPane = new JScrollPane(tbl);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        pnCardSoftware.add(scrollPane, "growx, h 120!");
        pnBody.add(pnCardSoftware, "growx");

        JPanel pnCardTeam = card("Đơn vị phát triển – Nhóm ...");
        pnCardTeam.add(gradientDivider(), "growx, h 4!, gaptop 2, gapbottom 6");
        pnCardTeam.add(bulletList(
                "Nguyễn Bảo Định",
                "Trần Ngọc Oanh",
                "Hoàng Ngọc Hải",
                "Dương Thiên Ân"
        ), "growx");
        pnBody.add(pnCardTeam, "growx");

        JScrollPane sp = new JScrollPane(pnBody);
        sp.setBorder(new LineBorder(BORDER));
        sp.getViewport().setOpaque(false);
        sp.setOpaque(false);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUnitIncrement(24);
        add(sp, "grow");
    }

    // Style
    private JPanel card(String header) {
        JPanel card = new GlassCard();
        card.setLayout(new MigLayout("insets 16 18 18 18, gapy 8, fillx, wrap", "[grow,fill]"));
        JLabel h = new JLabel(header);
        h.setForeground(ACCENT);
        h.setFont(h.getFont().deriveFont(Font.BOLD, 16f));
        card.add(h, "wrap");
        return card;
    }

    private JPanel kv(String k, String v) {
        JPanel p = new JPanel(new MigLayout("insets 4 6 4 6", "[][grow]", "[]"));
        p.setOpaque(false);
        JLabel lk = new JLabel(k + ":");
        lk.setForeground(ACCENT);
        lk.setFont(lk.getFont().deriveFont(Font.BOLD));
        JLabel lv = new JLabel(v);
        lv.setForeground(TEXT);
        p.add(lk); p.add(lv, "growx");
        return p;
    }

    private JLabel kvKey(String t){
        JLabel l = new JLabel(t);
        l.setForeground(ACCENT);
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        return l;
    }
    private JLabel kvVal(String t){
        JLabel l = new JLabel(t);
        l.setForeground(MUTED);
        return l;
    }

    private JTextArea text(String... lines) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            sb.append(lines[i]);
            if (i < lines.length - 1) sb.append("\n");
        }
        JTextArea a = new JTextArea(sb.toString());
        a.setEditable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setOpaque(false);
        a.putClientProperty(FlatClientProperties.STYLE, "foreground:#BBD0E7;font:+0;borderWidth:0");
        return a;
    }

    private JPanel bulletList(String... items) {
        JPanel list = new JPanel(new MigLayout("insets 0, gapy 6", "[][grow]", ""));
        list.setOpaque(false);
        for (String it : items) {
            JLabel dot = new JLabel("•");
            dot.setForeground(MUTED);
            JLabel txt = new JLabel(it);
            txt.setForeground(MUTED);
            list.add(dot); list.add(txt, "growx, wrap");
        }
        return list;
    }

    private void styleTable(JTable t){
        t.putClientProperty(FlatClientProperties.STYLE,
                "rowHeight:28; showHorizontalLines:true; showVerticalLines:false; " +
                        "selectionBackground:#143455; selectionForeground:#EAF2FF; gridColor:#1E3B58");
        t.setForeground(MUTED);
        t.setBackground(new Color(0x12355A));
        t.getTableHeader().putClientProperty(FlatClientProperties.STYLE,
                "background:#102A46; foreground:#EAF2FF; font:bold");
        t.getTableHeader().setReorderingAllowed(false);
    }

    private JPanel pill(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(0x05243A));
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        p.add(l);
        p.putClientProperty(FlatClientProperties.STYLE, "arc:999; background:#5EEAD4; borderColor:#51CDBE;");
        return p;
    }

    private static JComponent gradientDivider() {
        return new JComponent() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0x22D3EE),
                        getWidth(), 0, new Color(0x9333EA));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
            }
        };
    }

    private class HeroPanel extends JPanel {
        private final Color c1, c2;
        HeroPanel(Color c1, Color c2) { this.c1 = c1; this.c2 = c2; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setPaint(new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(255, 255, 255, 28));
            for (int i = 0; i < 80; i++) g2.fillOval((i * 53) % getWidth(), (i * 31) % getHeight(), 6, 6);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class GlassCard extends JPanel {
        GlassCard() {
            setOpaque(true);
            setBackground(CARD_BG);
            putClientProperty(FlatClientProperties.STYLE, "arc:18; borderColor:#17324C");
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = new GradientPaint(0, 0, CARD_GLOW1, getWidth(), 0, CARD_GLOW2);
            g2.setPaint(gp);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 18, 18);
            g2.dispose();
        }
    }
}
