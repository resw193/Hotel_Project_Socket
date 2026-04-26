package common.dto.request_dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

public class RoomCalendarRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDate fromDate;
    private LocalDate toDate;

    public RoomCalendarRequestDTO() {
    }

    public RoomCalendarRequestDTO(LocalDate fromDate, LocalDate toDate) {
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }
}