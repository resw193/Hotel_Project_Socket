package client.presentation.statistics.bookingType_revenue;

import common.dto.BookingTypeRevenueDTO;

import javax.swing.table.AbstractTableModel;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookingTypeRevenueTableModel extends AbstractTableModel {

    private final String[] cols = {"STT", "Kiểu đặt phòng", "Số lượt đặt", "Doanh thu phòng"};
    private final List<BookingTypeRevenueDTO> list = new ArrayList<>();

    public void setData(List<BookingTypeRevenueDTO> data) {
        list.clear();
        if (data != null) {
            list.addAll(data);
        }
        fireTableDataChanged();
    }

    public BookingTypeRevenueDTO getAt(int row) {
        if (row < 0 || row >= list.size()) return null;
        return list.get(row);
    }

    @Override
    public int getRowCount() {
        return list.size();
    }

    @Override
    public int getColumnCount() {
        return cols.length;
    }

    @Override
    public String getColumnName(int c) {
        return cols[c];
    }

    @Override
    public Object getValueAt(int r, int c) {
        BookingTypeRevenueDTO x = list.get(r);
        return switch (c) {
            case 0 -> r + 1;
            case 1 -> x.getBookingType();
            case 2 -> x.getSoLuot();
            case 3 -> NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(x.getRoomRevenue());
            default -> "";
        };
    }

    @Override
    public Class<?> getColumnClass(int c) {
        return switch (c) {
            case 0, 2 -> Integer.class;
            default -> String.class;
        };
    }
}