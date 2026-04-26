package client.presentation.room;

import common.dto.RoomDTO;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class RoomTableModel extends AbstractTableModel {

    private final String[] columns = {"ID", "Thông tin phòng", "Trạng thái", "Loại phòng", "View"};
    private List<RoomDTO> rooms = new ArrayList<>();

    @Override
    public int getRowCount() {
        return rooms == null ? 0 : rooms.size();
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
        RoomDTO r = rooms.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> r.getRoomId();
            case 1 -> r.getDescription();
            case 2 -> r.isAvailable() ? "Trống" : "Đã đặt";
            case 3 -> r.getRoomTypeName() == null ? "" : r.getRoomTypeName();
            case 4 -> r.getView() == null ? "" : r.getView();
            default -> null;
        };
    }

    public RoomDTO getRoomAt(int row) {
        if (row < 0 || row >= rooms.size()) return null;
        return rooms.get(row);
    }

    public void setRooms(List<RoomDTO> data) {
        this.rooms = (data == null) ? new ArrayList<>() : new ArrayList<>(data);
        fireTableDataChanged();
    }
}