package common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingTypeRevenueDTO implements Serializable {
    private String bookingType;
    private int soLuot;
    private double roomRevenue;

    public BookingTypeRevenueDTO() {
    }

    public BookingTypeRevenueDTO(String bookingType, int soLuot, double roomRevenue) {
        this.bookingType = bookingType;
        this.soLuot = soLuot;
        this.roomRevenue = roomRevenue;
    }

    public String getBookingType() {
        return bookingType;
    }

    public void setBookingType(String bookingType) {
        this.bookingType = bookingType;
    }

    public int getSoLuot() {
        return soLuot;
    }

    public void setSoLuot(int soLuot) {
        this.soLuot = soLuot;
    }

    public double getRoomRevenue() {
        return roomRevenue;
    }

    public void setRoomRevenue(double roomRevenue) {
        this.roomRevenue = roomRevenue;
    }
}