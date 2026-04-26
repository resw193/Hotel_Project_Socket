package client.presentation.roomBooking;

import javax.swing.table.AbstractTableModel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RoomMultiBookTableModel extends AbstractTableModel {

    public static class Row {
        private String roomID;
        private LocalDateTime checkIn;
        private LocalDateTime checkOut;
        private String bookingType;   // "Giờ" | "Ngày" | "Đêm"
        private boolean customized;

        public Row(String roomID, LocalDateTime checkIn, LocalDateTime checkOut, String bookingType, boolean customized) {
            this.roomID = roomID;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.bookingType = bookingType;
            this.customized = customized;
        }

        public String getRoomID() {
            return roomID;
        }

        public void setRoomID(String roomID) {
            this.roomID = roomID;
        }

        public LocalDateTime getCheckIn() {
            return checkIn;
        }

        public void setCheckIn(LocalDateTime checkIn) {
            this.checkIn = checkIn;
        }

        public LocalDateTime getCheckOut() {
            return checkOut;
        }

        public void setCheckOut(LocalDateTime checkOut) {
            this.checkOut = checkOut;
        }

        public String getBookingType() {
            return bookingType;
        }

        public void setBookingType(String bookingType) {
            this.bookingType = bookingType;
        }

        public boolean isCustomized() {
            return customized;
        }

        public void setCustomized(boolean customized) {
            this.customized = customized;
        }
    }

    private final String[] cols = {"STT", "RoomID", "Check-in", "Check-out", "Kiểu đặt", "Tùy chỉnh"};
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final List<Row> rows = new ArrayList<>();

    private LocalDateTime defaultCheckIn;
    private LocalDateTime defaultCheckOut;
    private String defaultType;

    public void setRooms(List<String> roomIDs, LocalDateTime defaultCheckIn, LocalDateTime defaultCheckOut, String defaultBookingType) {
        rows.clear();

        this.defaultCheckIn = defaultCheckIn;
        this.defaultCheckOut = defaultCheckOut;
        this.defaultType = defaultBookingType;

        if (roomIDs != null) {
            for (String id : roomIDs) {
                rows.add(new Row(id, this.defaultCheckIn, this.defaultCheckOut, this.defaultType, false));
            }
        }
        fireTableDataChanged();
    }

    public void applyNewDefaults(LocalDateTime newCheckIn,
                                 LocalDateTime newCheckOut,
                                 String newType) {
        this.defaultCheckIn = newCheckIn;
        this.defaultCheckOut = newCheckOut;
        this.defaultType = newType;

        for (Row r : rows) {
            if (!r.isCustomized()) {
                r.setCheckIn(newCheckIn);
                r.setCheckOut(newCheckOut);
                r.setBookingType(newType);
            }
        }
        fireTableDataChanged();
    }

    public Row getRowAt(int index) {
        return (index >= 0 && index < rows.size()) ? rows.get(index) : null;
    }

    public List<Row> getAllRows() {
        return Collections.unmodifiableList(rows);
    }

    public void setOverride(String roomID, LocalDateTime checkIn, LocalDateTime checkOut, String bookingType) {
        for (Row r : rows) {
            if (Objects.equals(r.getRoomID(), roomID)) {
                r.setCheckIn(checkIn);
                r.setCheckOut(checkOut);
                r.setBookingType(bookingType);
                r.setCustomized(true);
                break;
            }
        }
        fireTableDataChanged();
    }

    public void resetOverride(String roomID) {
        for (Row r : rows) {
            if (Objects.equals(r.getRoomID(), roomID)) {
                r.setCheckIn(defaultCheckIn);
                r.setCheckOut(defaultCheckOut);
                r.setBookingType(defaultType);
                r.setCustomized(false);
                break;
            }
        }
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return rows.size();
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
        Row row = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> rowIndex + 1;
            case 1 -> row.getRoomID();
            case 2 -> row.getCheckIn() == null ? "" : formatter.format(row.getCheckIn());
            case 3 -> row.getCheckOut() == null ? "" : formatter.format(row.getCheckOut());
            case 4 -> row.getBookingType();
            case 5 -> "Tùy chỉnh";
            default -> "";
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 5;
    }
}