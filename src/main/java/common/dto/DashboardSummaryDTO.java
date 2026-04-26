package common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DashboardSummaryDTO implements Serializable {
    private int totalRooms;
    private int occupiedNow;
    private double revenueThisMonth;
    private int bookingsToday;
    private int customersToday;

    public DashboardSummaryDTO() {
    }

    public DashboardSummaryDTO(int totalRooms, int occupiedNow, double revenueThisMonth,
                               int bookingsToday, int customersToday) {
        this.totalRooms = totalRooms;
        this.occupiedNow = occupiedNow;
        this.revenueThisMonth = revenueThisMonth;
        this.bookingsToday = bookingsToday;
        this.customersToday = customersToday;
    }

    public int getTotalRooms() {
        return totalRooms;
    }

    public void setTotalRooms(int totalRooms) {
        this.totalRooms = totalRooms;
    }

    public int getOccupiedNow() {
        return occupiedNow;
    }

    public void setOccupiedNow(int occupiedNow) {
        this.occupiedNow = occupiedNow;
    }

    public double getRevenueThisMonth() {
        return revenueThisMonth;
    }

    public void setRevenueThisMonth(double revenueThisMonth) {
        this.revenueThisMonth = revenueThisMonth;
    }

    public int getBookingsToday() {
        return bookingsToday;
    }

    public void setBookingsToday(int bookingsToday) {
        this.bookingsToday = bookingsToday;
    }

    public int getCustomersToday() {
        return customersToday;
    }

    public void setCustomersToday(int customersToday) {
        this.customersToday = customersToday;
    }
}