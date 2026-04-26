package client.presentation.roomBooking;

import common.dto.RecommendOptionDTO;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FormSuggestDialog extends JDialog {

    private final Color BG      = new Color(0x0B1F33);
    private final Color FG      = new Color(0xE9EEF6);
    private final Color CARD_BG = new Color(0x102D4A);
    private final Color BORDER  = new Color(0x274A6B);
    private final Color ACCENT  = new Color(0xF5C452);
    private final Color SUBTEXT = new Color(0x8FA4BF);

    @FunctionalInterface
    public interface SelectionCallback {
        void onSelect(Set<String> roomIDs);
    }

    public FormSuggestDialog(Window owner, List<RecommendOptionDTO> options, String view, SelectionCallback callback) {
        super(owner, "Gợi ý đặt phòng", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(BG);
        setContentPane(content);

        JPanel header = new JPanel(new MigLayout("insets 10 16 6 16, fillx", "[grow]", "[]2[]"));
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("Gợi ý phòng phù hợp");
        lblTitle.setForeground(FG);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));

        String subtitleText = "Chọn phương án phù hợp với nhu cầu của khách.";
        if (view != null && !view.isBlank()) subtitleText += " Ưu tiên view: " + view + ".";
        JLabel lblSubtitle = new JLabel(subtitleText);
        lblSubtitle.setForeground(SUBTEXT);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        header.add(lblTitle, "wrap");
        header.add(lblSubtitle, "wrap");
        content.add(header, BorderLayout.NORTH);

        JPanel listPanel = new JPanel(new MigLayout("insets 6 12 12 12, gapy 10, fillx, wrap 1", "[grow,fill]", ""));
        listPanel.setBackground(BG);

        int index = 1;
        if (options != null) {
            for (RecommendOptionDTO opt : options) {
                listPanel.add(createOptionCard(opt, index++, view, callback), "growx");
            }
        }

        JScrollPane scrollPane = new JScrollPane(
                listPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        content.add(scrollPane, BorderLayout.CENTER);

        JPanel footer = new JPanel(new MigLayout("insets 6 16 10 16, fillx", "[grow][]", "[]"));
        footer.setOpaque(false);

        JLabel lblHint = new JLabel("Mẹo: Nhấn F5 để đóng nhanh cửa sổ này.");
        lblHint.setForeground(SUBTEXT);
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        JButton btnClose = new JButton("Đóng");
        stylePrimaryButton(btnClose);
        btnClose.addActionListener(e -> dispose());

        footer.add(lblHint, "growx");
        footer.add(btnClose);
        content.add(footer, BorderLayout.SOUTH);

        setResizable(true);
        setPreferredSize(new Dimension(600, 600));
        pack();
        setLocationRelativeTo(owner);

        JRootPane root = getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();
        im.put(KeyStroke.getKeyStroke("F5"), "closeOnF5");
        am.put("closeOnF5", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private JPanel createOptionCard(RecommendOptionDTO opt, int index, String view, SelectionCallback callback) {
        JPanel card = new JPanel(new MigLayout(
                "insets 10 12 10 12, gapy 4, fillx",
                "[grow][]",
                "[][][]unrel[]"
        ));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                BorderFactory.createEmptyBorder(4, 4, 6, 4)
        ));

        JLabel lblPlan = new JLabel("Phương án " + index);
        lblPlan.setForeground(FG);
        lblPlan.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel lblLabel = new JLabel(opt.getLabel());
        lblLabel.setForeground(ACCENT);
        lblLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        card.add(lblPlan, "split 2");
        card.add(lblLabel, "wrap");

        if (view != null && !view.isBlank()) {
            JLabel lblViewTag = new JLabel("View: " + view);
            lblViewTag.setOpaque(true);
            lblViewTag.setBackground(new Color(0x123B5A));
            lblViewTag.setForeground(ACCENT);
            lblViewTag.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
            lblViewTag.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            card.add(lblViewTag, "span, wrap");
        }

        card.add(makeInfoLine("Phòng đơn", formatRoomList(opt.getSingleRoomIDs())), "span, growx, wrap");
        card.add(makeInfoLine("Phòng đôi", formatRoomList(opt.getDoubleRoomIDs())), "span, growx, wrap");

        JButton btnChoose = new JButton("Chọn phương án này");
        stylePrimaryButton(btnChoose);
        btnChoose.addActionListener(e -> {
            if (callback != null) {
                Set<String> ids = new LinkedHashSet<>();
                if (opt.getDoubleRoomIDs() != null) ids.addAll(opt.getDoubleRoomIDs());
                if (opt.getSingleRoomIDs() != null) ids.addAll(opt.getSingleRoomIDs());
                callback.onSelect(ids);
            }
            dispose();
        });

        card.add(btnChoose, "span, right, gaptop 4");
        return card;
    }

    private JPanel makeInfoLine(String label, String value) {
        JPanel p = new JPanel(new MigLayout("insets 0, fillx", "[right]8[grow,fill]", "[]"));
        p.setOpaque(false);

        JLabel lbKey = new JLabel(label + ":");
        lbKey.setForeground(new Color(0xB8C4D4));
        lbKey.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JLabel lbVal = new JLabel(value);
        lbVal.setForeground(FG);
        lbVal.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        p.add(lbKey, "right");
        p.add(lbVal, "growx");
        return p;
    }

    private String formatRoomList(List<String> ids) {
        if (ids == null || ids.isEmpty()) return "-";
        return ids.stream().collect(Collectors.joining(", "));
    }

    private void stylePrimaryButton(JButton b) {
        b.setBackground(new Color(0xF5C452));
        b.setForeground(new Color(0x0B1F33));
        b.setBorder(BorderFactory.createLineBorder(new Color(0xF1B93A), 1));
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setFocusable(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}