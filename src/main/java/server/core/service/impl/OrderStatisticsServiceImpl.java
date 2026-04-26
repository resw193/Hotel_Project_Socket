package server.core.service.impl;

import common.dto.DailyDetailDTO;
import common.dto.OrderStatisticsDTO;
import server.core.repository.OrderStatisticsRepository;
import server.core.service.OrderStatisticsService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

public class OrderStatisticsServiceImpl implements OrderStatisticsService {

    private OrderStatisticsRepository orderStatisticsRepository;

    public OrderStatisticsServiceImpl(OrderStatisticsRepository orderStatisticsRepository) {
        this.orderStatisticsRepository = orderStatisticsRepository;
    }

    @Override
    public OrderStatisticsDTO getDailyOrderStatistics(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Ngày thống kê không được rỗng.");
        }

        return orderStatisticsRepository.getDailyOrderStatistics(date.atStartOfDay());
    }

    @Override
    public DailyDetailDTO getDailyDetail(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Ngày thống kê không được rỗng.");
        }

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        return orderStatisticsRepository.getDetailByRange(start, end);
    }

    @Override
    public DailyDetailDTO getMonthlyDetail(YearMonth yearMonth) {
        if (yearMonth == null) {
            throw new IllegalArgumentException("Tháng thống kê không được rỗng.");
        }

        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        return orderStatisticsRepository.getDetailByRange(start, end);
    }

    @Override
    public DailyDetailDTO getDetailByRange(LocalDateTime start, LocalDateTime end) {
        validateRange(start, end);
        return orderStatisticsRepository.getDetailByRange(start, end);
    }

    @Override
    public double getRevenueByRange(LocalDateTime start, LocalDateTime end) {
        validateRange(start, end);
        return orderStatisticsRepository.getRevenueByRange(start, end);
    }

    private void validateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Khoảng thời gian không được rỗng.");
        }

        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu.");
        }
    }
}