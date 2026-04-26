package common.dto.request_dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public class CalculateRoomFeeRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String roomID;
    private String bookingType;
    private LocalDateTime from;
    private LocalDateTime to;

    public CalculateRoomFeeRequestDTO() {
    }

    public CalculateRoomFeeRequestDTO(String roomID, String bookingType, LocalDateTime from, LocalDateTime to) {
        this.roomID = roomID;
        this.bookingType = bookingType;
        this.from = from;
        this.to = to;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getBookingType() {
        return bookingType;
    }

    public void setBookingType(String bookingType) {
        this.bookingType = bookingType;
    }

    public LocalDateTime getFrom() {
        return from;
    }

    public void setFrom(LocalDateTime from) {
        this.from = from;
    }

    public LocalDateTime getTo() {
        return to;
    }

    public void setTo(LocalDateTime to) {
        this.to = to;
    }
}