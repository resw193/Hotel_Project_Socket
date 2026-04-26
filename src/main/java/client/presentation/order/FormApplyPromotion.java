package client.presentation.order;

import common.dto.OrderDTO;
import common.dto.PromotionDTO;
import common.protocol.command.CommandType;
import common.protocol.response.BaseResponse;
import net.miginfocom.swing.MigLayout;
import server.core.service.PromotionService;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FormApplyPromotion extends JDialog {

    private static final Color BG        = new Color(0x0B1F33);
    private static final Color CARD_BG   = new Color(0x0F2A44);
    private static final Color BORDER    = new Color(0x274A6B);
    private static final Color TEXT      = new Color(0xE6F1FF);
    private static final Color MUTED     = new Color(0x9FB6CC);
    private static final Color ACCENT    = new Color(0x22D3EE);
    private static final Color ACCENT2   = new Color(0xF59E0B);
    private static final Font BASE_FONT  = new Font("Segoe UI", Font.PLAIN, 13);

    private final OrderDTO order;
    private final double baseTotal;
    private final PromotionService promotionService;

    private boolean accepted = false;

    private static class PromoItem {
        PromotionDTO promotion;
        String reason;

        PromoItem(PromotionDTO p) {
            this.promotion = p;
        }
    }

    private final List<PromoItem> available = new ArrayList<>();
    private final List<PromoItem> unavailable = new ArrayList<>();

    private PromotionDTO recommendedPromotion;
    private PromotionDTO selectedPromotion;
    private PromotionDTO resultPromotion;

    public FormApplyPromotion(Window owner, OrderDTO order, PromotionService promotionService) {
        super(owner, "Chọn khuyến mãi", ModalityType.APPLICATION_MODAL);
        this.order = order;
        this.baseTotal = order == null ? 0d : order.getTotal();
        this.promotionService = promotionService;

        if (order != null && !isBlank(order.getPromotionId())) {
            PromotionDTO current = new PromotionDTO();
            current.setPromotionId(order.getPromotionId());
            current.setPromotionName(order.getPromotionName());
            current.setDiscount(order.getPromotionDiscount());
            this.selectedPromotion = current;
        }

        classifyPromotions();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(820, 660));

        rebuildAndResize();
        setLocationRelativeTo(owner);
    }

    private void rebuildAndResize() {
        buildContent();
        pack();

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int minHeight = 660;
        int maxHeight = Math.min(720, screen.height - 80);

        int newHeight = getHeight();
        if (newHeight < minHeight) newHeight = minHeight;
        if (newHeight > maxHeight) newHeight = maxHeight;

        setSize(getWidth(), newHeight);
    }

    public boolean isAccepted() {
        return accepted;
    }

    public PromotionDTO getSelectedPromotion() {
        return selectedPromotion;
    }

    public PromotionDTO getResultPromotion() {
        return resultPromotion;
    }

    private void classifyPromotions() {
        available.clear();
        unavailable.clear();

        BaseResponse response = sendRequest(CommandType.GET_ALL_PROMOTIONS, null);
        List<PromotionDTO> list;
        if (response.isSuccess() && response.getData() instanceof List<?>) {
            List<PromotionDTO> tmp = (List<PromotionDTO>) response.getData();
            list = tmp;
        }
        else {
            list = new ArrayList<>();
        }

        int loyalty = order == null ? 0 : order.getCustomerLoyaltyPoint();
        LocalDateTime now = LocalDateTime.now();

        for (PromotionDTO p : list) {
            boolean timeOk =
                    (p.getStartTime() == null || !p.getStartTime().isAfter(now)) &&
                            (p.getEndTime() == null || !p.getEndTime().isBefore(now));

            boolean quantityOk = p.getQuantity() > 0;
            boolean loyaltyOk = loyaltySatisfied(p, loyalty);

            boolean ok = timeOk && quantityOk && loyaltyOk;
            PromoItem item = new PromoItem(p);

            if (!ok) {
                StringBuilder sb = new StringBuilder();

                if (!timeOk) {
                    if (p.getEndTime() != null && p.getEndTime().isBefore(now)) {
                        sb.append("Khuyến mãi đã hết hạn. ");
                    } else {
                        sb.append("Chưa đến thời gian áp dụng. ");
                    }
                }
                if (!quantityOk) {
                    sb.append("Đã hết lượt sử dụng. ");
                }
                if (!loyaltyOk) {
                    sb.append("Không đủ điểm thân thiết. ");
                }

                item.reason = sb.toString().trim();
                unavailable.add(item);
            } else {
                available.add(item);
            }
        }

        if (!available.isEmpty()) {
            recommendedPromotion = available.stream()
                    .map(i -> i.promotion)
                    .min(Comparator.comparingDouble(this::calcFinalTotalForPromotion))
                    .orElse(null);
        }
    }

    private boolean loyaltySatisfied(PromotionDTO p, int loyaltyPoints) {
        int disc = (int) Math.round(p.getDiscount());

        if (disc == 10) return loyaltyPoints >= 10;
        if (disc == 15) return loyaltyPoints >= 20;
        if (disc == 20) return loyaltyPoints >= 40;

        return true;
    }

    private double calcFinalTotalForPromotion(PromotionDTO p) {
        double percent = (p == null ? 0 : p.getDiscount());
        if (percent < 0) percent = 0;
        if (percent > 100) percent = 100;

        double discountAmount = baseTotal * percent / 100.0;
        double afterPromo = baseTotal - discountAmount;
        double vat = afterPromo * 0.10;
        return afterPromo + vat;
    }

    private boolean samePromotion(PromotionDTO a, PromotionDTO b) {
        if (a == null || b == null) return false;
        if (a.getPromotionId() == null || b.getPromotionId() == null) return false;
        return a.getPromotionId().equalsIgnoreCase(b.getPromotionId());
    }

    private void buildContent() {
        Container c = getContentPane();
        c.removeAll();
        c.setBackground(BG);
        c.setLayout(new MigLayout(
                "insets 12 16 12 16, gapx 10, gapy 12, fillx, filly",
                "[grow,fill]",
                "[]15[grow]15[]"
        ));

        c.add(buildSummaryPanel(), "wrap, growx");

        JComponent listsPanel = buildListsPanel();
        JScrollPane centerScroll = new JScrollPane(listsPanel);
        prepareScroll(centerScroll);
        centerScroll.setBorder(null);

        c.add(centerScroll, "wrap, grow, pushy");
        c.add(buildButtonsPanel(), "growx");

        c.revalidate();
        c.repaint();
    }

    private JComponent buildSummaryPanel() {
        JPanel p = new JPanel(new MigLayout(
                "insets 8, gap 8",
                "[grow,fill][260!]",
                "[]"
        ));
        p.setOpaque(true);
        p.setBackground(CARD_BG);
        p.setBorder(new LineBorder(BORDER));

        JPanel left = new JPanel(new MigLayout("wrap, insets 0, gap 4", "[grow,fill]", "[]"));
        left.setOpaque(false);
        left.add(makeSectionTitle("Thông tin thanh toán chi tiết"), "wrap");

        double percent = (selectedPromotion == null ? 0 : selectedPromotion.getDiscount());
        if (percent < 0) percent = 0;
        if (percent > 100) percent = 100;

        double discountAmount = baseTotal * percent / 100.0;
        double afterPromo = baseTotal - discountAmount;
        double vat = afterPromo * 0.10;
        double finalTotal = afterPromo + vat;

        left.add(makeText("Tổng (Chưa áp KM): " + formatVND(baseTotal)), "wrap");

        String promoTxt = (percent <= 0)
                ? "Không có"
                : ((int) percent) + "% (-" + formatVND(discountAmount) + ")";
        left.add(makeText("Khuyến mãi đang chọn: " + promoTxt), "wrap");

        left.add(makeText("Tổng (Đã áp KM): " + formatVND(afterPromo)), "wrap");
        left.add(makeText("VAT (10%): " + formatVND(vat)), "wrap");

        JLabel lblFinal = makeBoldText("Thành tiền: " + formatVND(finalTotal));
        lblFinal.setForeground(new Color(0xFDE68A));
        left.add(lblFinal, "wrap");

        JPanel right = new JPanel(new MigLayout("wrap, insets 0, gap 4", "[grow,fill]", "[]"));
        right.setOpaque(false);

        right.add(makeSectionTitle("Khách hàng"), "wrap");

        String name = order != null ? nullToEmpty(order.getCustomerName()) : "Khách lẻ";
        String customerId = order != null ? nullToEmpty(order.getCustomerId()) : "";
        int loyalty = order == null ? 0 : order.getCustomerLoyaltyPoint();

        right.add(makeText("Tên: " + name), "wrap");
        right.add(makeText("Mã khách hàng: " + customerId), "wrap");
        right.add(makeText("Điểm thân thiết: " + loyalty), "wrap");

        String tier;
        if (loyalty >= 40) tier = "Kim cương (20%)";
        else if (loyalty >= 20) tier = "Vàng (15%)";
        else if (loyalty >= 10) tier = "Bạc (10%)";
        else tier = "Chưa đạt hạng thành viên";

        right.add(makeMuted("Hạng hiện tại: " + tier), "wrap");

        p.add(left, "grow");
        p.add(right, "growy");

        return p;
    }

    private JComponent buildListsPanel() {
        JPanel root = new JPanel(new MigLayout(
                "wrap, insets 6 0 0 0, gapx 0, gapy 14, fillx",
                "[grow,fill]",
                "[][][]"
        ));
        root.setOpaque(false);

        root.add(buildCurrentPromoPanel(), "growx, wrap");
        root.add(buildAvailablePanel(), "growx, wrap");
        root.add(buildUnavailablePanel(), "growx");

        return root;
    }

    private JComponent buildCurrentPromoPanel() {
        JPanel wrapper = new JPanel(new MigLayout("wrap, insets 0 0 2 0, gap 6", "[grow,fill]", "[]"));
        wrapper.setOpaque(false);

        wrapper.add(makeSectionTitle("Khuyến mãi hiện đang chọn"), "wrap");

        if (selectedPromotion == null) {
            wrapper.add(makeMuted("Chưa chọn khuyến mãi nào. Hệ thống sẽ tính theo giá gốc hoặc khuyến mãi mặc định."), "growx");
            return wrapper;
        }

        JPanel card = new JPanel(new MigLayout(
                "wrap, insets 10 14 10 14, gap 6",
                "[grow,fill][]",
                "[][][]"
        ));
        card.setOpaque(true);
        card.setBackground(new Color(0x102A43));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, ACCENT2),
                new LineBorder(BORDER)
        ));

        JLabel lblName = makeBoldText(selectedPromotion.getPromotionName());
        card.add(lblName, "cell 0 0, growx");

        JLabel badge = makeBadge("Hiện đang chọn", ACCENT2);
        card.add(badge, "cell 1 0, alignx right");

        int percent = (int) Math.round(selectedPromotion.getDiscount());
        if (percent < 0) percent = 0;
        if (percent > 100) percent = 100;

        double discountAmount = baseTotal * percent / 100.0;
        double finalTotal = calcFinalTotalForPromotion(selectedPromotion);

        card.add(makeText("Giảm " + percent + "%"), "cell 0 1 2 1, growx");
        card.add(makeSavingLabel("Tiết kiệm: " + formatVND(discountAmount)), "cell 0 2 2 1, growx");
        card.add(makeText("Thành tiền: " + formatVND(finalTotal)), "cell 0 3 2 1, growx");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        if (selectedPromotion.getStartTime() != null && selectedPromotion.getEndTime() != null) {
            String time = "Thời gian: " + fmt.format(selectedPromotion.getStartTime())
                    + " - " + fmt.format(selectedPromotion.getEndTime());
            card.add(makeText(time), "cell 0 4 2 1, growx");
        }

        card.add(makeMuted("Điều kiện: " + buildConditionText(selectedPromotion)), "cell 0 5 2 1, growx");

        wrapper.add(card, "growx");
        return wrapper;
    }

    private JComponent buildAvailablePanel() {
        JPanel wrapper = new JPanel(new MigLayout(
                "wrap, insets 0, gap 6",
                "[grow,fill]",
                "[]"
        ));
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER.darker()),
                BorderFactory.createEmptyBorder(6, 0, 0, 0)
        ));

        wrapper.add(makeSectionTitle("Danh sách khuyến mãi khả dụng"), "wrap");

        JPanel listPanel = new JPanel(new MigLayout(
                "wrap, insets 0, gap 8",
                "[grow,fill]",
                "[]"
        ));
        listPanel.setOpaque(false);

        ButtonGroup group = new ButtonGroup();

        if (available.isEmpty()) {
            listPanel.add(makeMuted("Không có khuyến mãi nào có thể áp dụng."), "growx");
        } else {
            for (PromoItem item : available) {
                PromotionDTO p = item.promotion;
                boolean isCurrent = selectedPromotion != null && samePromotion(selectedPromotion, p);
                boolean isBest = recommendedPromotion != null && samePromotion(recommendedPromotion, p);
                JPanel card = createAvailablePromoCard(p, isCurrent, isBest, group);
                listPanel.add(card, "growx");
            }
        }

        wrapper.add(listPanel, "growx");
        return wrapper;
    }

    private JComponent buildUnavailablePanel() {
        JPanel wrapper = new JPanel(new MigLayout(
                "wrap, insets 0, gap 6",
                "[grow,fill]",
                "[]"
        ));
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER.darker()),
                BorderFactory.createEmptyBorder(6, 0, 0, 0)
        ));

        wrapper.add(makeSectionTitle("Danh sách khuyến mãi không khả dụng"), "wrap");

        JPanel listPanel = new JPanel(new MigLayout(
                "wrap, insets 0, gap 8",
                "[grow,fill]",
                "[]"
        ));
        listPanel.setOpaque(false);

        if (unavailable.isEmpty()) {
            listPanel.add(makeMuted("Tất cả khuyến mãi hiện có đều có thể áp dụng."), "growx");
        } else {
            for (PromoItem item : unavailable) {
                JPanel card = createUnavailablePromoCard(item);
                listPanel.add(card, "growx");
            }
        }

        wrapper.add(listPanel, "growx");
        return wrapper;
    }

    private JComponent buildButtonsPanel() {
        JPanel p = new JPanel(new MigLayout("insets 0, align right", "[]10[]10[]", "[]"));
        p.setOpaque(false);

        JButton btnClear = new JButton("Bỏ khuyến mãi");
        btnClear.setFont(BASE_FONT);
        btnClear.addActionListener(e -> {
            selectedPromotion = null;
            rebuildAndResize();
        });

        JButton btnCancel = new JButton("Hủy");
        btnCancel.setFont(BASE_FONT);
        btnCancel.addActionListener(e -> {
            accepted = false;
            resultPromotion = null;
            dispose();
        });

        JButton btnOk = new JButton("Đồng ý");
        btnOk.setFont(BASE_FONT.deriveFont(Font.BOLD));
        btnOk.addActionListener(e -> {
            accepted = true;
            resultPromotion = selectedPromotion;
            dispose();
        });

        p.add(btnClear);
        p.add(btnCancel);
        p.add(btnOk);

        return p;
    }

    private JPanel createAvailablePromoCard(PromotionDTO p, boolean isCurrent, boolean isBest, ButtonGroup group) {
        JPanel card = new JPanel(new MigLayout(
                "insets 10 14 10 14, gap 6",
                "[grow,fill][]",
                "[][][][]"
        ));
        card.setOpaque(true);

        if (isCurrent) {
            card.setBackground(new Color(0x102A43));
        } else if (isBest) {
            card.setBackground(new Color(0x0D314A));
        } else {
            card.setBackground(CARD_BG);
        }

        Color borderColor = isCurrent ? ACCENT2 : (isBest ? ACCENT : BORDER);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor),
                BorderFactory.createEmptyBorder(0, 2, 0, 2)
        ));

        JLabel lblName = makeBoldText(p.getPromotionName());
        card.add(lblName, "cell 0 0, growx");

        if (isCurrent) {
            card.add(makeBadge("Hiện đang chọn", ACCENT2), "cell 1 0, alignx right");
        } else if (isBest) {
            card.add(makeBadge("Lựa chọn tốt nhất", ACCENT), "cell 1 0, alignx right");
        }

        int percent = (int) Math.round(p.getDiscount());
        if (percent < 0) percent = 0;
        if (percent > 100) percent = 100;

        double discountAmount = baseTotal * percent / 100.0;
        double finalTotal = calcFinalTotalForPromotion(p);

        card.add(makeText("Giảm " + percent + "%"), "cell 0 1 2 1, growx");
        card.add(makeSavingLabel("Tiết kiệm: " + formatVND(discountAmount)), "cell 0 2 2 1, growx");
        card.add(makeText("Thành tiền: " + formatVND(finalTotal)), "cell 0 3 2 1, growx");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        if (p.getStartTime() != null && p.getEndTime() != null) {
            String time = "Thời gian: " + fmt.format(p.getStartTime()) + " - " + fmt.format(p.getEndTime());
            card.add(makeText(time), "cell 0 4 2 1, growx");
        }

        card.add(makeMuted("Điều kiện: " + buildConditionText(p)), "cell 0 5 2 1, growx");

        JRadioButton radio = new JRadioButton();
        radio.setOpaque(false);
        radio.setSelected(isCurrent);
        radio.setEnabled(!isCurrent);
        group.add(radio);
        card.add(radio, "cell 1 3, alignx right");

        if (!isCurrent) {
            MouseAdapter listener = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (!radio.isSelected()) {
                        radio.setSelected(true);
                        onChoosePromotion(p);
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            };

            attachClickListenerRecursive(card, listener, radio);

            radio.addActionListener(e -> {
                if (radio.isSelected()) {
                    onChoosePromotion(p);
                }
            });
        }

        return card;
    }

    private JPanel createUnavailablePromoCard(PromoItem item) {
        PromotionDTO p = item.promotion;

        JPanel card = new JPanel(new MigLayout(
                "insets 10 14 10 14, gap 6",
                "[grow,fill][]",
                "[][][]"
        ));
        card.setOpaque(true);
        card.setBackground(new Color(0x091727));
        card.setBorder(new LineBorder(new Color(0x1F2933)));

        JLabel lblName = makeBoldText(p.getPromotionName());
        lblName.setForeground(new Color(0x9CA3AF));
        card.add(lblName, "cell 0 0, growx");

        JLabel badge = makeBadge("Không khả dụng", new Color(0x4B5563));
        badge.setForeground(Color.WHITE);
        card.add(badge, "cell 1 0, alignx right");

        card.add(makeMuted("Khuyến mãi: " + (int) p.getDiscount() + "%"), "cell 0 1 2 1, growx");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        if (p.getStartTime() != null && p.getEndTime() != null) {
            String time = "Thời gian: " + fmt.format(p.getStartTime()) + " - " + fmt.format(p.getEndTime());
            card.add(makeMuted(time), "cell 0 2 2 1, growx");
        }

        card.add(makeMuted("Điều kiện: " + buildConditionText(p)), "cell 0 3 2 1, growx");

        if (item.reason != null && !item.reason.isBlank()) {
            JLabel reason = makeMuted(item.reason);
            reason.setFont(reason.getFont().deriveFont(Font.ITALIC, 11f));
            card.add(reason, "cell 0 4 2 1, growx");
        }

        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String msg = p.getPromotionName() + "\n\nĐiều kiện: " + buildConditionText(p);
                if (item.reason != null && !item.reason.isBlank()) {
                    msg += "\n\nLý do hiện không áp dụng: " + item.reason;
                }
                JOptionPane.showMessageDialog(FormApplyPromotion.this,
                        msg,
                        "Chi tiết khuyến mãi",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        return card;
    }

    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return client.network.socket.SocketSessionManager.send(
                common.protocol.request.BaseRequest.of(commandType, data)
        );
    }


    private void prepareScroll(JScrollPane scroll) {
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JScrollBar vsb = scroll.getVerticalScrollBar();
        vsb.setUnitIncrement(24);
        vsb.setBlockIncrement(140);
    }

    private void attachClickListenerRecursive(Component comp, MouseAdapter listener, Component... ignores) {
        for (Component ig : ignores) {
            if (comp == ig) return;
        }

        comp.addMouseListener(listener);

        if (comp instanceof Container container) {
            for (Component child : container.getComponents()) {
                attachClickListenerRecursive(child, listener, ignores);
            }
        }
    }

    private JLabel makeText(String s) {
        JLabel l = new JLabel(s);
        l.setFont(BASE_FONT);
        l.setForeground(TEXT);
        return l;
    }

    private JLabel makeBoldText(String s) {
        JLabel l = makeText(s);
        l.setFont(l.getFont().deriveFont(Font.BOLD));
        return l;
    }

    private JLabel makeMuted(String s) {
        JLabel l = new JLabel(s);
        l.setFont(BASE_FONT);
        l.setForeground(MUTED);
        return l;
    }

    private JLabel makeSectionTitle(String s) {
        JLabel l = new JLabel(s);
        l.setFont(BASE_FONT.deriveFont(Font.BOLD, 14f));
        l.setForeground(ACCENT);
        return l;
    }

    private JLabel makeBadge(String text, Color bg) {
        JLabel l = new JLabel(text);
        l.setFont(BASE_FONT.deriveFont(Font.BOLD, 11f));
        l.setForeground(new Color(0x0B1F33));
        l.setOpaque(true);
        l.setBackground(bg);
        l.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        return l;
    }

    private String buildConditionText(PromotionDTO p) {
        int disc = (int) Math.round(p.getDiscount());
        return switch (disc) {
            case 10 -> "Khách hàng thân thiết từ 10–19 điểm.";
            case 15 -> "Khách hàng thân thiết từ 20–39 điểm.";
            case 20 -> "Khách hàng thân thiết từ 40 điểm trở lên.";
            default -> "Không yêu cầu điểm thân thiết (khuyến mãi sự kiện).";
        };
    }

    private String formatVND(double v) {
        return String.format("%,.0f VND", v);
    }

    private void onChoosePromotion(PromotionDTO p) {
        this.selectedPromotion = p;
        rebuildAndResize();
    }

    private JLabel makeSavingLabel(String s) {
        JLabel l = new JLabel(s);
        l.setFont(BASE_FONT.deriveFont(Font.BOLD, 13f));
        l.setForeground(new Color(0xFACC15));
        return l;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}