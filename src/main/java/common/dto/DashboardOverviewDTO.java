package common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DashboardOverviewDTO implements Serializable {
    private DashboardSummaryDTO summary;
    private List<DashboardUpcomingBookingDTO> upcomingBookings = new ArrayList<>();
    private List<DashboardTopServiceDTO> topServices = new ArrayList<>();
    private List<DashboardRoomTypeStatDTO> roomTypeDistribution = new ArrayList<>();
    private String managerPlanText;
    private String managerAlertText;

    public DashboardOverviewDTO() {
    }

    public DashboardSummaryDTO getSummary() {
        return summary;
    }

    public void setSummary(DashboardSummaryDTO summary) {
        this.summary = summary;
    }

    public List<DashboardUpcomingBookingDTO> getUpcomingBookings() {
        return upcomingBookings;
    }

    public void setUpcomingBookings(List<DashboardUpcomingBookingDTO> upcomingBookings) {
        this.upcomingBookings = upcomingBookings;
    }

    public List<DashboardTopServiceDTO> getTopServices() {
        return topServices;
    }

    public void setTopServices(List<DashboardTopServiceDTO> topServices) {
        this.topServices = topServices;
    }

    public List<DashboardRoomTypeStatDTO> getRoomTypeDistribution() {
        return roomTypeDistribution;
    }

    public void setRoomTypeDistribution(List<DashboardRoomTypeStatDTO> roomTypeDistribution) {
        this.roomTypeDistribution = roomTypeDistribution;
    }

    public String getManagerPlanText() {
        return managerPlanText;
    }

    public void setManagerPlanText(String managerPlanText) {
        this.managerPlanText = managerPlanText;
    }

    public String getManagerAlertText() {
        return managerAlertText;
    }

    public void setManagerAlertText(String managerAlertText) {
        this.managerAlertText = managerAlertText;
    }
}