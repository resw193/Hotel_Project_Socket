package server.core.service;

import common.dto.DashboardOverviewDTO;
import common.dto.DashboardRoomTypeStatDTO;
import common.dto.DashboardSummaryDTO;
import common.dto.DashboardTopServiceDTO;
import common.dto.DashboardUpcomingBookingDTO;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public interface DashboardService {

    DashboardSummaryDTO getTodaySummary(LocalDate today);

    DashboardOverviewDTO getDashboardOverview(LocalDate today, int daysAhead, int topN);

    int customersVisitBetween(LocalDate start, LocalDate end);

    List<DashboardUpcomingBookingDTO> getUpcomingBookings(int daysAhead);

    List<DashboardTopServiceDTO> getTopServicesThisMonth(int topN);

    List<DashboardRoomTypeStatDTO> getRoomTypeDistribution();

    String getManagerPlanText();

    String getManagerAlertText();

    boolean saveManagerPlanText(String content, String employeeId);

    boolean saveManagerAlertText(String content, String employeeId);
}