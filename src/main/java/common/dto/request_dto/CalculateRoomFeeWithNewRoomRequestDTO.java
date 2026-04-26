package common.dto.request_dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public class CalculateRoomFeeWithNewRoomRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String newRoomID;
    private String bookingType;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;

    public CalculateRoomFeeWithNewRoomRequestDTO() {
    }

    public CalculateRoomFeeWithNewRoomRequestDTO(String newRoomID, String bookingType,
                                                 LocalDateTime checkIn, LocalDateTime checkOut) {
        this.newRoomID = newRoomID;
        this.bookingType = bookingType;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    public String getNewRoomID() {
        return newRoomID;
    }

    public void setNewRoomID(String newRoomID) {
        this.newRoomID = newRoomID;
    }

    public String getBookingType() {
        return bookingType;
    }

    public void setBookingType(String bookingType) {
        this.bookingType = bookingType;
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
}