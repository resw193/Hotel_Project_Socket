package client.presentation.customer;

import client.presentation.core.ServiceRegistry;
import common.dto.CustomerDTO;
import net.miginfocom.swing.MigLayout;
import server.core.service.CustomerService;
import client.network.socket.SocketSessionManager;
import common.dto.request_dto.SearchCustomersRequestDTO;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class FormCustomerManagement extends JPanel {

    private static final Color BG = new Color(0x0B1F33);
    private static final Color BORDER = new Color(0x274A6B);
    private static final Color TEXT = new Color(0xE6F1FF);
    private static final Color ACCENT = new Color(0x22D3EE);
    private static final Font BASE_FONT = new Font("Segoe UI", Font.PLAIN, 13);

    private CustomerTableModel tableModel = new CustomerTableModel();

    private JTable tableCustomer;
    private JTextField txtSearchName;
    private JComboBox<String> cbxFilter;
    private JButton btnAdd, btnUpdate;

    private static final int[] COL_WEIGHTS = {12, 22, 14, 20, 14, 10, 8};

    public FormCustomerManagement() {

        setLayout(new BorderLayout());
        setBackground(BG);

        JPanel top = new JPanel(new MigLayout("insets 10 12 10 12", "[]push[]20[]20[]", "[]"));
        top.setBackground(BG);

        txtSearchName = new JTextField();
        txtSearchName.putClientProperty("JTextField.placeholderText", "Tìm theo mã hoặc tên khách hàng…");
        styleTextField(txtSearchName);
        top.add(txtSearchName, "w 360!");

        cbxFilter = new JComboBox<>(new String[]{"Tất cả", ">= 20", ">= 40"});
        styleCombo(cbxFilter);
        cbxFilter.setPreferredSize(new Dimension(140, 32));

        JLabel lblLoyalty = new JLabel("Điểm thân thiết:");
        lblLoyalty.setForeground(TEXT);
        lblLoyalty.setFont(BASE_FONT);

        JPanel pnLoyalty = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnLoyalty.setOpaque(false);
        pnLoyalty.add(lblLoyalty);
        pnLoyalty.add(cbxFilter);

        top.add(pnLoyalty);

        btnAdd = primaryButton("Thêm", true);
        btnUpdate = primaryButton("Cập nhật", false);
        top.add(btnAdd, "w 110!, h 32!");
        top.add(btnUpdate, "w 110!, h 32!");

        add(top, BorderLayout.NORTH);

        tableCustomer = createTable();
        JScrollPane sp = new JScrollPane(tableCustomer);
        sp.getViewport().setBackground(BG);
        sp.setBorder(new LineBorder(BORDER));
        add(sp, BorderLayout.CENTER);

        fitColumnsToViewport(tableCustomer, COL_WEIGHTS);
        sp.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                fitColumnsToViewport(tableCustomer, COL_WEIGHTS);
            }
        });

        txtSearchName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                loadDataToTable();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                loadDataToTable();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                loadDataToTable();
            }
        });
        cbxFilter.addActionListener(e -> loadDataToTable());

        btnAdd.addActionListener(e -> {
            Window owner = SwingUtilities.getWindowAncestor(this);
            new FormAddCustomer(owner).setVisible(true);
            loadDataToTable();
        });

        btnUpdate.addActionListener(e -> updateCustomer());

        tableCustomer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    updateCustomer();
                }
            }
        });

        loadDataToTable();
    }

    private JTable createTable() {
        JTable t = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
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

        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) h.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        int[] centerCols = {0, 1, 2, 3, 4, 6};
        for (int col : centerCols) {
            t.getColumnModel().getColumn(col).setCellRenderer(center);
        }

        return t;
    }

    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return SocketSessionManager.send(BaseRequest.of(commandType, data));
    }

    private void loadDataToTable() {
        try {
            String opt = String.valueOf(cbxFilter.getSelectedItem());
            Integer min = null;
            if (">= 20".equals(opt)) min = 20;
            if (">= 40".equals(opt)) min = 40;

            String kw = txtSearchName.getText();

            BaseResponse response = sendRequest(CommandType.SEARCH_CUSTOMERS, new SearchCustomersRequestDTO(kw, min));

            if (!response.isSuccess()) {
                throw new RuntimeException(response.getMessage());
            }

            @SuppressWarnings("unchecked")
            List<CustomerDTO> filtered = (List<CustomerDTO>) response.getData();
            if (filtered == null) filtered = List.of();

            tableModel.setDsKH(filtered);
            if (!filtered.isEmpty()) tableCustomer.setRowSelectionInterval(0, 0);
            fitColumnsToViewport(tableCustomer, COL_WEIGHTS);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCustomer() {
        int row = tableCustomer.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một khách hàng để cập nhật.");
            return;
        }
        CustomerDTO c = tableModel.getAt(row);
        if (c == null) return;

        Window owner = SwingUtilities.getWindowAncestor(this);
        new FormUpdateCustomer(owner, c.getCustomerId()).setVisible(true);
        loadDataToTable();
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

    private void fitColumnsToViewport(JTable table, int[] weights) {
        Component p = table.getParent();
        if (!(p instanceof JViewport vp)) return;
        int vw = vp.getWidth();
        var cm = table.getColumnModel();

        int total = 0;
        for (int w : weights) total += w;
        for (int i = 0; i < cm.getColumnCount() && i < weights.length; i++) {
            int w = (int) Math.round(vw * (weights[i] / (double) total));
            cm.getColumn(i).setPreferredWidth(Math.max(w, 90));
        }
    }
}