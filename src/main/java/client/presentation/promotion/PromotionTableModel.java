package client.presentation.promotion;

import common.dto.PromotionDTO;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PromotionTableModel extends AbstractTableModel {

    private final String[] cols = {
            "PromotionID", "Tên khuyến mãi", "Giảm giá", "Bắt đầu", "Kết thúc", "Số lượng"
    };

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private List<PromotionDTO> promotions = new ArrayList<>();

    public void setPromotions(List<PromotionDTO> list) {
        promotions.clear();
        if (list != null) promotions.addAll(list);
        fireTableDataChanged();
    }

    public PromotionDTO getAt(int row) {
        if (row < 0 || row >= promotions.size()) return null;
        return promotions.get(row);
    }

    @Override
    public int getRowCount() {
        return promotions.size();
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
        PromotionDTO p = promotions.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> p.getPromotionId();
            case 1 -> p.getPromotionName();
            case 2 -> (int) Math.round(p.getDiscount()) + " %";
            case 3 -> p.getStartTime() == null ? "" : p.getStartTime().format(formatter);
            case 4 -> p.getEndTime() == null ? "" : p.getEndTime().format(formatter);
            case 5 -> p.getQuantity();
            default -> "";
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 5 -> Integer.class;
            default -> String.class;
        };
    }
}