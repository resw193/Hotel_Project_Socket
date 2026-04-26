package server.core.service;

import common.dto.DailyDetailDTO;
import common.dto.OrderStatisticsDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

public interface OrderStatisticsService {
    OrderStatisticsDTO getDailyOrderStatistics(LocalDate date);

    DailyDetailDTO getDailyDetail(LocalDate date);

    DailyDetailDTO getMonthlyDetail(YearMonth yearMonth);

    DailyDetailDTO getDetailByRange(LocalDateTime start, LocalDateTime end);

    double getRevenueByRange(LocalDateTime start, LocalDateTime end);
}