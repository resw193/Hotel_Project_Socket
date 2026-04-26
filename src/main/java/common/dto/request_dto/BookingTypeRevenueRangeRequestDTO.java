package common.dto.request_dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public class BookingTypeRevenueRangeRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDateTime start;
    private LocalDateTime end;

    public BookingTypeRevenueRangeRequestDTO() {
    }

    public BookingTypeRevenueRangeRequestDTO(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }
}