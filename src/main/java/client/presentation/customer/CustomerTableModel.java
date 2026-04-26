package client.presentation.customer;

import common.dto.CustomerDTO;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CustomerTableModel extends AbstractTableModel {
    private final String[] cols = {"CustomerID", "Tên khách hàng", "Số điện thoại", "Email", "Ngày đăng ký", "CCCD", "Điểm thân thiết"};

    private List<CustomerDTO> dsKH = new ArrayList<>();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void setDsKH(List<CustomerDTO> list) {
        dsKH.clear();
        if (list != null) dsKH.addAll(list);
        fireTableDataChanged();
    }

    public CustomerDTO getAt(int row) {
        if (row < 0 || row >= dsKH.size()) return null;
        return dsKH.get(row);
    }

    @Override
    public int getRowCount() {
        return dsKH.size();
    }

    @Override
    public int getColumnCount() {
        return cols.length;
    }

    @Override
    public String getColumnName(int column) {
        return cols[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        CustomerDTO c = dsKH.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> c.getCustomerId();
            case 1 -> c.getFullName();
            case 2 -> c.getPhone();
            case 3 -> c.getEmail();
            case 4 -> c.getRegisDate() == null ? "" : c.getRegisDate().format(formatter);
            case 5 -> c.getIdCard();
            case 6 -> c.getLoyaltyPoint();
            default -> "";
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 6 -> Integer.class;
            default -> String.class;
        };
    }
}