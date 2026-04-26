package server.core.repository;

import common.dto.DashboardRoomTypeStatDTO;
import common.dto.DashboardTopServiceDTO;
import common.dto.DashboardUpcomingBookingDTO;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public interface DashboardRepository {

    int totalRooms();

    int occupiedNow();

    double revenueOfMonth(YearMonth yearMonth);

    int bookingsOn(LocalDate date);

    int customersVisitOn(LocalDate date);

    int customersVisitBetween(LocalDate start, LocalDate end);

    List<DashboardUpcomingBookingDTO> upcomingBookings(int daysAhead);

    List<DashboardTopServiceDTO> topServicesOfMonth(YearMonth yearMonth, int topN);

    List<DashboardRoomTypeStatDTO> roomTypeDistribution();

    String getDashboardNote(String noteKey);

    boolean saveDashboardNote(String noteKey, String content, String employeeId);
}