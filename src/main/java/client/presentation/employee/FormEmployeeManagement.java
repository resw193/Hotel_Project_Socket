package client.presentation.employee;

import client.presentation.core.ServiceRegistry;
import server.core.service.EmployeeService;
import server.core.service.EmployeeTypeService;
import com.formdev.flatlaf.FlatClientProperties;
import common.dto.EmployeeDTO;
import net.miginfocom.swing.MigLayout;
import client.network.socket.SocketSessionManager;
import common.dto.request_dto.EmployeeTypeFilterRequestDTO;
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
import java.util.ArrayList;
import java.util.List;

public class FormEmployeeManagement extends JPanel {

    private static final Color BG = new Color(0x0B1F33);
    private static final Color PANEL_TOP = new Color(0x0E2942);
    private static final Color TEXT_PRIMARY = new Color(0xE9EEF6);
    private static final Color HEADER_BG = new Color(0x102A43);
    private static final Color HEADER_ACCENT = new Color(0x22D3EE);
    private static final Font BASE_FONT = new Font("Segoe UI", Font.PLAIN, 13);

    private JTable tableEmployee;
    private EmployeeTableModel employeeTableModel;
    private JComboBox<String> cbxFilterType;
    private JButton btnAdd, btnUpdate, btnDelete;
    private JTextField txtSearchEmployeeID;

    public FormEmployeeManagement() {
        setLayout(new MigLayout("fill, wrap, insets 0", "[grow]", "[grow 0][grow]"));
        setBackground(BG);

        initTopPanel();
        initTable();
        loadEmployees();
    }

    @Override
    public void addNotify() {
        super.addNotify();

        SwingUtilities.invokeLater(() -> {
            if (txtSearchEmployeeID != null) {
                txtSearchEmployeeID.setFocusable(true);
                txtSearchEmployeeID.setEnabled(true);
                txtSearchEmployeeID.requestFocusInWindow();
                txtSearchEmployeeID.selectAll();
            }
        });
    }

    private void initTopPanel() {
        JPanel top = new JPanel(new MigLayout(
                "insets 12 16 12 24",
                "[]16[240!]16[grow,fill]16[]8[]8[]16[]",
                "[]"
        ));
        top.setBackground(PANEL_TOP);
        add(top, "growx");

        JLabel lblTitle = new JLabel("Quản lý nhân viên | Employees");
        lblTitle.setForeground(TEXT_PRIMARY);
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 18f));
        top.add(lblTitle, "cell 0 0");

        txtSearchEmployeeID = new JTextField();
        txtSearchEmployeeID.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Tìm theo mã nhân viên");
        txtSearchEmployeeID.putClientProperty(
                FlatClientProperties.STYLE,
                "arc:10; background:#102D4A; foreground:#E9EEF6; " +
                        "borderColor:#274A6B; padding:6,10,6,10;"
        );

        txtSearchEmployeeID.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                SwingUtilities.invokeLater(() -> txtSearchEmployeeID.requestFocusInWindow());
            }
        });

        top.add(txtSearchEmployeeID, "cell 1 0, growx");

        top.add(new JLabel(), "cell 2 0, growx");

        btnAdd = new JButton("Thêm");
        stylePrimarySolid(btnAdd);
        top.add(btnAdd, "cell 3 0, w 100!");

        btnUpdate = new JButton("Cập nhật");
        stylePrimarySoft(btnUpdate);
        top.add(btnUpdate, "cell 4 0, w 110!");

        btnDelete = new JButton("Xóa");
        styleDanger(btnDelete);
        top.add(btnDelete, "cell 5 0, w 90!");

        cbxFilterType = new JComboBox<>(new String[]{"Tất cả", "Lễ tân", "Quản lý"});
        cbxFilterType.putClientProperty(FlatClientProperties.STYLE,
                "arc:10; background:#102D4A; foreground:#E9EEF6; borderColor:#274A6B; padding:6,12,6,12;");
        cbxFilterType.setPreferredSize(new Dimension(150, 32));

        JLabel lblFilterType = new JLabel("Loại nhân viên:");
        lblFilterType.setForeground(TEXT_PRIMARY);
        lblFilterType.setFont(BASE_FONT);

        JPanel pnFilterType = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnFilterType.setOpaque(false);
        pnFilterType.add(lblFilterType);
        pnFilterType.add(cbxFilterType);

        top.add(pnFilterType, "cell 6 0");

        cbxFilterType.addActionListener(e -> applyFilters());

        btnAdd.addActionListener(e -> {
            FormAddEmployee dialog = new FormAddEmployee(SwingUtilities.getWindowAncestor(this), this);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        });

        btnUpdate.addActionListener(e -> openUpdateDialogForSelectedRow());
        btnDelete.addActionListener(e -> deleteSelectedEmployee());

        txtSearchEmployeeID.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilters();
            }
        });
    }

    private void initTable() {
        employeeTableModel = new EmployeeTableModel();
        tableEmployee = new JTable(employeeTableModel);

        tableEmployee.setRowHeight(30);
        tableEmployee.setFont(BASE_FONT);
        tableEmployee.setForeground(TEXT_PRIMARY);
        tableEmployee.setBackground(BG);
        tableEmployee.setGridColor(new Color(0x13314A));

        tableEmployee.putClientProperty(FlatClientProperties.STYLE,
                "background:#0B1F33; foreground:#E9EEF6; selectionBackground:#153C5B; " +
                        "selectionForeground:#E9EEF6; gridColor:#13314A");

        JTableHeader header = tableEmployee.getTableHeader();
        header.setBackground(HEADER_BG);
        header.setForeground(HEADER_ACCENT);
        header.setFont(BASE_FONT.deriveFont(Font.BOLD, 13f));

        DefaultTableCellRenderer headerRenderer =
                (DefaultTableCellRenderer) header.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        tableEmployee.getColumnModel().getColumn(0).setCellRenderer(center);
        tableEmployee.getColumnModel().getColumn(1).setCellRenderer(center);
        tableEmployee.getColumnModel().getColumn(2).setCellRenderer(center);
        tableEmployee.getColumnModel().getColumn(4).setCellRenderer(center);
        tableEmployee.getColumnModel().getColumn(5).setCellRenderer(center);

        tableEmployee.getColumnModel().getColumn(0).setPreferredWidth(90);
        tableEmployee.getColumnModel().getColumn(1).setPreferredWidth(220);
        tableEmployee.getColumnModel().getColumn(2).setPreferredWidth(120);
        tableEmployee.getColumnModel().getColumn(3).setPreferredWidth(220);

        tableEmployee.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (tableEmployee.getSelectedRow() != -1 && e.getClickCount() == 2) {
                    openUpdateDialogForSelectedRow();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tableEmployee);
        scrollPane.setBorder(null);
        add(scrollPane, "grow");
    }

    public void loadEmployees() {
        applyFilters();
    }

    private void applyFilters() {
        try {
            String filterType = (String) cbxFilterType.getSelectedItem();
            String keyword = txtSearchEmployeeID == null
                    ? ""
                    : txtSearchEmployeeID.getText().trim().toLowerCase();

            BaseResponse response = sendRequest(CommandType.GET_EMPLOYEES_BY_TYPE, new EmployeeTypeFilterRequestDTO(filterType));

            if (!response.isSuccess()) {
                throw new RuntimeException(response.getMessage());
            }

            @SuppressWarnings("unchecked")
            List<EmployeeDTO> list = (List<EmployeeDTO>) response.getData();
            if (list == null) list = new ArrayList<>();

            if (!keyword.isEmpty()) {
                List<EmployeeDTO> filtered = new ArrayList<>();
                for (EmployeeDTO e : list) {
                    if (e.getEmployeeId() != null && e.getEmployeeId().toLowerCase().contains(keyword)) {
                        filtered.add(e);
                    }
                }
                list = filtered;
            }

            employeeTableModel.setEmployees(list);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Không tải được danh sách nhân viên:\n" + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openUpdateDialogForSelectedRow() {
        int row = tableEmployee.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 nhân viên để cập nhật.");
            return;
        }

        EmployeeDTO employee = employeeTableModel.getEmployeeAt(row);
        if (employee == null) {
            JOptionPane.showMessageDialog(this, "Không lấy được thông tin nhân viên.");
            return;
        }

        FormUpdateEmployee dialog = new FormUpdateEmployee(SwingUtilities.getWindowAncestor(this), this, employee);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void deleteSelectedEmployee() {
        int row = tableEmployee.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 nhân viên để xóa.");
            return;
        }

        EmployeeDTO employee = employeeTableModel.getEmployeeAt(row);
        if (employee == null) {
            JOptionPane.showMessageDialog(this, "Không lấy được thông tin nhân viên.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa nhân viên " + employee.getFullName() + " ?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            try {
                BaseResponse response = sendRequest(CommandType.DELETE_EMPLOYEE, employee.getEmployeeId());
                boolean ok = response.isSuccess();
                if (!ok) {
                    JOptionPane.showMessageDialog(this, response.getMessage().isBlank()
                            ? "Không thể xóa nhân viên (có thể đang được sử dụng trong hệ thống)."
                            : response.getMessage());
                    return;
                }
                else {
                    JOptionPane.showMessageDialog(this, "Đã xóa nhân viên.");
                    loadEmployees();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private BaseResponse sendRequest(CommandType commandType, Object data) {
        return SocketSessionManager.send(BaseRequest.of(commandType, data));
    }

    // ...
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

    private void styleDanger(AbstractButton b) {
        b.setFont(BASE_FONT.deriveFont(Font.BOLD));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(new Color(0x7F1D1D)));
        b.setBackground(new Color(0xDC2626));
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMargin(new Insets(4, 10, 4, 10));
    }
}