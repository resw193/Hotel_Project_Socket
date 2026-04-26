package client.presentation.order;

import common.dto.OrderDTO;
import common.enums.OrderStatus;

import javax.swing.table.AbstractTableModel;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OrderTableModel extends AbstractTableModel {

    private final String[] cols = {
            "Mã hóa đơn", "Ngày lập hóa đơn", "Nhân viên", "Khách hàng", "Mã KM", "Trạng thái", "Tổng tiền"
    };

    private final List<OrderDTO> dsHoaDon = new ArrayList<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void setDsHoaDon(List<OrderDTO> rows) {
        dsHoaDon.clear();
        if (rows != null) dsHoaDon.addAll(rows);
        fireTableDataChanged();
    }

    public OrderDTO getAt(int row) {
        return (row >= 0 && row < dsHoaDon.size()) ? dsHoaDon.get(row) : null;
    }

    @Override
    public int getRowCount() {
        return dsHoaDon.size();
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
        OrderDTO o = dsHoaDon.get(r);
        boolean isPaid = o.getOrderStatus() == OrderStatus.DA_THANH_TOAN;

        return switch (c) {
            case 0 -> o.getOrderId();
            case 1 -> o.getOrderDate() == null ? "" : formatter.format(o.getOrderDate());
            case 2 -> nvl(o.getEmployeeName());
            case 3 -> nvl(o.getCustomerName());
            case 4 -> nvl(o.getPromotionId());
            case 5 -> o.getOrderStatus() == null ? "" : o.getOrderStatus().getDisplayName();
            case 6 -> formatVND(isPaid ? o.getTotal() : calculateFinalTotal(o));
            default -> "";
        };
    }

    private double calculateFinalTotal(OrderDTO order) {
        double base = order.getTotal();

        double discountPercent = order.getPromotionDiscount();
        if (discountPercent < 0) discountPercent = 0;
        if (discountPercent > 100) discountPercent = 100;

        double tienKhuyenMai = base * discountPercent / 100.0;
        double sauKhuyenMai = base - tienKhuyenMai;
        double vat = sauKhuyenMai * 0.10;

        return sauKhuyenMai + vat;
    }

    private String formatVND(double v) {
        return String.format("%,.0f VND", v);
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}