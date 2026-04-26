package common.dto.request_dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

public class OrderStatisticsRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDate date;
    private boolean byMonth;

    public OrderStatisticsRequestDTO() {
    }

    public OrderStatisticsRequestDTO(LocalDate date, boolean byMonth) {
        this.date = date;
        this.byMonth = byMonth;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public boolean isByMonth() {
        return byMonth;
    }

    public void setByMonth(boolean byMonth) {
        this.byMonth = byMonth;
    }
}