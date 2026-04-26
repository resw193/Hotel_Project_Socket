package common.dto.request_dto;

import common.dto.CustomerDTO;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public class BookRoomRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private CustomerDTO customerDTO;
    private String roomID;
    private String employeeID;
    private LocalDateTime bookingDate;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private String bookingType;

    public BookRoomRequestDTO() {
    }

    public BookRoomRequestDTO(CustomerDTO customerDTO, String roomID, String employeeID,
                              LocalDateTime bookingDate, LocalDateTime checkInDate,
                              LocalDateTime checkOutDate, String bookingType) {
        this.customerDTO = customerDTO;
        this.roomID = roomID;
        this.employeeID = employeeID;
        this.bookingDate = bookingDate;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.bookingType = bookingType;
    }

    public CustomerDTO getCustomerDTO() {
        return customerDTO;
    }

    public void setCustomerDTO(CustomerDTO customerDTO) {
        this.customerDTO = customerDTO;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalDateTime getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDateTime checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDateTime getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDateTime checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public String getBookingType() {
        return bookingType;
    }

    public void setBookingType(String bookingType) {
        this.bookingType = bookingType;
    }
}