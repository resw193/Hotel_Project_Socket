package common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)

public class DailyDetailDTO implements Serializable {
    private int soLuongHoaDon;
    private int totalBookings;
    private int totalServiceQty;
    private double roomRevenue;
    private double serviceRevenue;
    private double totalRevenue;

    public DailyDetailDTO() {
    }

    public DailyDetailDTO(int soLuongHoaDon, int totalBookings, int totalServiceQty, double roomRevenue, double serviceRevenue, double totalRevenue) {
        this.soLuongHoaDon = soLuongHoaDon;
        this.totalBookings = totalBookings;
        this.totalServiceQty = totalServiceQty;
        this.roomRevenue = roomRevenue;
        this.serviceRevenue = serviceRevenue;
        this.totalRevenue = totalRevenue;
    }

    public int getSoLuongHoaDon() {
        return soLuongHoaDon;
    }

    public void setSoLuongHoaDon(int soLuongHoaDon) {
        this.soLuongHoaDon = soLuongHoaDon;
    }

    public int getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(int totalBookings) {
        this.totalBookings = totalBookings;
    }

    public int getTotalServiceQty() {
        return totalServiceQty;
    }

    public void setTotalServiceQty(int totalServiceQty) {
        this.totalServiceQty = totalServiceQty;
    }

    public double getRoomRevenue() {
        return roomRevenue;
    }

    public void setRoomRevenue(double roomRevenue) {
        this.roomRevenue = roomRevenue;
    }

    public double getServiceRevenue() {
        return serviceRevenue;
    }

    public void setServiceRevenue(double serviceRevenue) {
        this.serviceRevenue = serviceRevenue;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}