package client.presentation.employee;

import common.dto.EmployeeDTO;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class EmployeeTableModel extends AbstractTableModel {

    private final String[] columns = {
            "Mã nhân viên", "Họ tên", "Số điện thoại", "Email", "Giới tính", "Loại nhân viên"
    };

    private List<EmployeeDTO> employees = new ArrayList<>();

    public EmployeeTableModel() {
    }

    public EmployeeTableModel(List<EmployeeDTO> employees) {
        setEmployees(employees);
    }

    @Override
    public int getRowCount() {
        return employees == null ? 0 : employees.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        EmployeeDTO e = employees.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> e.getEmployeeId();
            case 1 -> e.getFullName();
            case 2 -> e.getPhone();
            case 3 -> e.getEmail();
            case 4 -> e.isGender() ? "Nam" : "Nữ";
            case 5 -> e.getEmployeeTypeName();
            default -> null;
        };
    }

    public EmployeeDTO getEmployeeAt(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= getRowCount()) return null;
        return employees.get(rowIndex);
    }

    public void setEmployees(List<EmployeeDTO> list) {
        this.employees = (list == null) ? new ArrayList<>() : new ArrayList<>(list);
        fireTableDataChanged();
    }
}