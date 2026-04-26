package server.core.service.impl;

import common.dto.DashboardOverviewDTO;
import common.dto.DashboardRoomTypeStatDTO;
import common.dto.DashboardSummaryDTO;
import common.dto.DashboardTopServiceDTO;
import common.dto.DashboardUpcomingBookingDTO;
import server.core.repository.DashboardRepository;
import server.core.service.DashboardService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;

public class DashboardServiceImpl implements DashboardService {

    private static final String NOTE_PLAN = "DASHBOARD_PLAN";
    private static final String NOTE_ALERT = "DASHBOARD_ALERT";

    private final DashboardRepository dashboardRepository;

    public DashboardServiceImpl(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    @Override
    public DashboardSummaryDTO getTodaySummary(LocalDate today) {
        if (today == null) {
            throw new IllegalArgumentException("Ngày hiện tại không được rỗng.");
        }

        return new DashboardSummaryDTO(dashboardRepository.totalRooms(), dashboardRepository.occupiedNow(),
                dashboardRepository.revenueOfMonth(YearMonth.from(today)), dashboardRepository.bookingsOn(today), dashboardRepository.customersVisitOn(today)
        );
    }

    @Override
    public DashboardOverviewDTO getDashboardOverview(LocalDate today, int daysAhead, int topN) {
        if (today == null) {
            throw new IllegalArgumentException("Ngày hiện tại không được rỗng.");
        }

        if (daysAhead <= 0) {
            throw new IllegalArgumentException("daysAhead phải > 0.");
        }

        if (topN <= 0) {
            throw new IllegalArgumentException("topN phải > 0.");
        }

        DashboardOverviewDTO dto = new DashboardOverviewDTO();
        dto.setSummary(getTodaySummary(today));
        dto.setUpcomingBookings(dashboardRepository.upcomingBookings(daysAhead));
        dto.setTopServices(dashboardRepository.topServicesOfMonth(YearMonth.from(today), topN));
        dto.setRoomTypeDistribution(dashboardRepository.roomTypeDistribution());
        dto.setManagerPlanText(dashboardRepository.getDashboardNote(NOTE_PLAN));
        dto.setManagerAlertText(dashboardRepository.getDashboardNote(NOTE_ALERT));

        return dto;
    }

    @Override
    public int customersVisitBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Ngày bắt đầu/kết thúc không được rỗng.");
        }

        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu.");
        }

        return dashboardRepository.customersVisitBetween(start, end);
    }

    @Override
    public List<DashboardUpcomingBookingDTO> getUpcomingBookings(int daysAhead) {
        if (daysAhead <= 0) {
            return Collections.emptyList();
        }
        return dashboardRepository.upcomingBookings(daysAhead);
    }

    @Override
    public List<DashboardTopServiceDTO> getTopServicesThisMonth(int topN) {
        if (topN <= 0) {
            return Collections.emptyList();
        }
        return dashboardRepository.topServicesOfMonth(YearMonth.now(), topN);
    }

    @Override
    public List<DashboardRoomTypeStatDTO> getRoomTypeDistribution() {
        return dashboardRepository.roomTypeDistribution();
    }

    @Override
    public String getManagerPlanText() {
        return dashboardRepository.getDashboardNote(NOTE_PLAN);
    }

    @Override
    public String getManagerAlertText() {
        return dashboardRepository.getDashboardNote(NOTE_ALERT);
    }

    @Override
    public boolean saveManagerPlanText(String content, String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new IllegalArgumentException("employeeId không hợp lệ.");
        }
        return dashboardRepository.saveDashboardNote(NOTE_PLAN, content, employeeId.trim());
    }

    @Override
    public boolean saveManagerAlertText(String content, String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new IllegalArgumentException("employeeId không hợp lệ.");
        }
        return dashboardRepository.saveDashboardNote(NOTE_ALERT, content, employeeId.trim());
    }
}