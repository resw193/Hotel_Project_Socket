package client.presentation.promotion;

import common.dto.PromotionDTO;
import net.miginfocom.swing.MigLayout;
import server.core.service.PromotionService;
import client.network.socket.SocketSessionManager;
import common.protocol.command.CommandType;
import common.protocol.request.BaseRequest;
import common.protocol.response.BaseResponse;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class FormPromotionManagement extends JPanel {

    private static final Color BG = new Color(0x0B1F33);
    private static final Color BORDER = new Color(0x274A6B);
    private static final Color TEXT = new Color(0xE6F1FF);
    private static final Color ACCENT = new Color(0x22D3EE);
    private static final Font BASE_FONT = new Font("Segoe UI", Font.PLAIN, 13);

    private final PromotionService promotionService;
    private final boolean isReception;

    private PromotionTableModel tableModel = new PromotionTableModel();
    private JTable tablePromotion;
    private JButton btnAdd, btnUpdate, btnDelete;
    private JTextField txtSearchName;
    private JComboBox<String> cbxStatus;

    private static final int[] COL_WEIGHTS = {10, 26, 10, 22, 22, 10};

    public FormPromotionManagement(PromotionService promotionService, boolean isReception) {
        this.promotionService = promotionService;
        this.isReception = isReception;

        setLayout(new BorderLayout());
        setBackground(BG);

        JPanel top = new JPanel(new MigLayout(
                "insets 10 12 10 12",
                "[]push[]20[]20[]",
                "[]"
        ));
        top.setBackground(BG);

        txtSearchName = new JTextField();
        txtSearchName.putClientProperty("JTextField.placeholderText", "Tìm theo tên khuyến mãi…");
        styleTextField(txtSearchName);

        cbxStatus = new JComboBox<>(new String[]{"Tất cả", "Còn hiệu lực", "Hết hạn"});
        styleCombo(cbxStatus);

        JLabel lblStatus = new JLabel("Tình trạng:");
        lblStatus.setForeground(TEXT);
        lblStatus.setFont(BASE_FONT);

        top.add(txtSearchName, "w 260!, h 32!, split 3");
        top.add(lblStatus);
        top.add(cbxStatus, "w 150!, h 32!");
        top.add(Box.createHorizontalGlue(), "growx");

        btnAdd = primaryButton("Thêm khuyến mãi", true);
        btnUpdate = primaryButton("Cập nhật", false);
        btnDelete = dangerButton("Xóa khuyến mãi");

        if (!isReception) {
            top.add(btnAdd, "w 150!, h 32!");
            top.add(btnUpdate, "w 120!, h 32!");
            top.add(btnDelete, "w 140!, h 32!");
        }

        add(top, BorderLayout.NORTH);

        tablePromotion = createTable();
        JScrollPane scrollPane = new JScrollPane(tablePromotion);
        scrollPane.getViewport().setBackground(BG);
        scrollPane.setBorder(new LineBorder(BORDER));
        add(scrollPane, BorderLayout.CENTER);

        fitColumnsToViewport(tablePromotion, COL_WEIGHTS);
        scrollPane.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                fitColumnsToViewport(tablePromotion, COL_WEIGHTS);
            }
        });

        btnAdd.addActionListener(e -> {
            Window owner = SwingUtilities.getWindowAncestor(this);
            new FormAddPromotion(owner).setVisible(true);
            loadDataToTable();
        });

        btnUpdate.addActionListener(e -> updatePromotion());
        btnDelete.addActionListener(e -> deletePromotion());

        txtSearchName.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { loadDataToTable(); }
            @Override public void removeUpdate(DocumentEvent e) { loadDataToTable(); }
            @Override public void changedUpdate(DocumentEvent e) { loadDataToTable(); }
        });

        cbxStatus.addActionListener(e -> loadDataToTable());

        loadDataToTable();
    }

    private void deletePromotion() {
        int row = tablePromotion.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một khuyến mãi để xóa.");
            return;
        }

        PromotionDTO p = tableModel.getAt(row);
        if (p == null) return;

        int opt = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa khuyến mãi này không?",
                "Xóa khuyến mãi",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (opt != JOptionPane.YES_OPTION) return;

        try {
            BaseResponse response = sendRequest(CommandType.DELETE_PROMOTION, p.getPromotionId());
            boolean ok = response.isSuccess();

            if (ok) {
                JOptionPane.showMessageDialog(this, "Đã xóa khuyến mãi.");
                loadDataToTable();
            }
            else {
                JOptionPane.showMessageDialog(this, response.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
            return;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JTable createTable() {
        JTable t = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? new Color(0x0E253D) : new Color(0x0C2136));
                    c.setForeground(TEXT);
                } else {
                    c.setBackground(new Color(0x10344F));
                    c.setForeground(Color.WHITE);
                }
                if (c instanceof JComponent jc) {
                    jc.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                }
                return c;
            }
        };

        t.setRowHeight(30);
        t.setFont(BASE_FONT);
        t.setForeground(TEXT);
        t.setBackground(BG);
        t.setGridColor(new Color(0x13314A));
        t.setFillsViewportHeight(true);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JTableHeader h = t.getTableHeader();
        h.setBackground(new Color(0x102A43));
        h.setForeground(ACCENT);
        h.setFont(BASE_FONT.deriveFont(Font.BOLD, 13f));
        ((DefaultTableCellRenderer) h.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        t.getColumnModel().getColumn(2).setCellRenderer(center);
        t.getColumnModel().getColumn(5).setCellRenderer(center);

        return t;
    }

    private void loadDataToTable() {
        List<PromotionDTO> dsKM;

        try {
            BaseResponse response = sendRequest(CommandType.GET_ALL_PROMOTIONS, null);
            if (!response.isSuccess()) {
                throw new RuntimeException(response.getMessage());
            }

            @SuppressWarnings("unchecked")
            List<PromotionDTO> list = (List<PromotionDTO>) response.getData();
            dsKM = list == null ? new ArrayList<>() : list;

        } catch (Exception ex) {
            dsKM = new ArrayList<>();
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        String keyword = txtSearchName != null ? txtSearchName.getText().trim().toLowerCase() : "";
        String statusOpt = cbxStatus != null ? (String) cbxStatus.getSelectedItem() : "Tất cả";
        LocalDate today = LocalDate.now(ZoneId.systemDefault());

        List<PromotionDTO> filtered = new ArrayList<>();

        for (PromotionDTO p : dsKM) {
            if (!keyword.isEmpty()) {
                if (p.getPromotionName() == null || !p.getPromotionName().toLowerCase().contains(keyword)) {
                    continue;
                }
            }

            boolean active = isPromotionActiveToday(p, today);

            if ("Còn hiệu lực".equals(statusOpt) && !active) continue;
            if ("Hết hạn".equals(statusOpt) && active) continue;

            filtered.add(p);
        }

        tableModel.setPromotions(filtered);

        if (!filtered.isEmpty()) {
            tablePromotion.setRowSelectionInterval(0, 0);
        }
        fitColumnsToViewport(tablePromotion, COL_WEIGHTS);
    }

    private boolean isPromotionActiveToday(PromotionDTO p, LocalDate today) {
        LocalDateTime startDate = p.getStartTime();
        LocalDateTime endDate = p.getEndTime();

        if (startDate == null || endDate == null) return false;

        LocalDate start = startDate.toLocalDate();
        LocalDate end = endDate.toLocalDate();

        if (today.isBefore(start)) return false;
        return !today.isAfter(end);
    }

    private void updatePromotion() {
        int row = tablePromotion.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một khuyến mãi để cập nhật.");
            return;
        }

        PromotionDTO promotion = tableModel.getAt(row);
        if (promotion == null) return;

        Window owner = SwingUtilities.getWindowAncestor(this);
        new FormUpdatePromotion(owner, promotion.getPromotionId()).setVisible(true);
        loadDataToTable();
    }

    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return SocketSessionManager.send(BaseRequest.of(commandType, data));
    }

    private JButton primaryButton(String text, boolean solid) {
        JButton b = new JButton(text);
        b.setFont(BASE_FONT.deriveFont(Font.BOLD));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(new Color(0x1B4F72)));
        b.setBackground(solid ? new Color(0x2563EB) : new Color(0x0EA5E9));
        b.setOpaque(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton dangerButton(String text) {
        JButton b = new JButton(text);
        b.setFont(BASE_FONT.deriveFont(Font.BOLD));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(new Color(0x7F1D1D)));
        b.setBackground(new Color(0xDC2626));
        b.setOpaque(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void styleTextField(JTextField f) {
        f.setFont(BASE_FONT);
        f.setForeground(TEXT);
        f.setBackground(new Color(0x102A43));
        f.setCaretColor(TEXT);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
    }

    private void styleCombo(JComboBox<?> cb) {
        cb.setFont(BASE_FONT);
        cb.setForeground(TEXT);
        cb.setBackground(new Color(0x102A43));
        cb.setBorder(new LineBorder(BORDER));
    }

    private void fitColumnsToViewport(JTable table, int[] weights) {
        Component p = table.getParent();
        if (!(p instanceof JViewport vp)) return;

        int vw = vp.getWidth();
        javax.swing.table.TableColumnModel cm = table.getColumnModel();

        int total = 0;
        for (int w : weights) total += w;

        for (int i = 0; i < cm.getColumnCount() && i < weights.length; i++) {
            int w = (int) Math.round(vw * (weights[i] / (double) total));
            cm.getColumn(i).setPreferredWidth(Math.max(w, 90));
        }
    }
}