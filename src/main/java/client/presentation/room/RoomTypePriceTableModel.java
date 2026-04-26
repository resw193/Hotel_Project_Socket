package client.presentation.room;

import common.dto.RoomTypeDTO;

import javax.swing.table.AbstractTableModel;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RoomTypePriceTableModel extends AbstractTableModel {

    private final String[] cols = {
            "Mã loại phòng",
            "Tên loại phòng",
            "Giá theo giờ",
            "Giá theo đêm",
            "Giá theo ngày",
            "Phụ thu trễ/giờ",
            "Người lớn",
            "Trẻ em"
    };

    private final NumberFormat VND = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private List<RoomTypeDTO> roomTypes = new ArrayList<>();

    public void setRoomTypes(List<RoomTypeDTO> list) {
        this.roomTypes = list != null ? list : new ArrayList<>();
        fireTableDataChanged();
    }

    public RoomTypeDTO getRoomTypeAt(int row) {
        if (row < 0 || row >= roomTypes.size()) return null;
        return roomTypes.get(row);
    }

    @Override
    public int getRowCount() {
        return roomTypes.size();
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
        RoomTypeDTO rt = roomTypes.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> rt.getRoomTypeId();
            case 1 -> rt.getTypeName();
            case 2 -> VND.format(rt.getPricePerHour());
            case 3 -> VND.format(rt.getPricePerNight());
            case 4 -> VND.format(rt.getPricePerDay());
            case 5 -> VND.format(rt.getLateFeePerHour());
            case 6 -> rt.getMaxAdults();
            case 7 -> rt.getMaxChildren();
            default -> "";
        };
    }
}