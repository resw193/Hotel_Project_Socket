package server.core.repository;

import common.dto.DailyDetailDTO;
import common.dto.OrderStatisticsDTO;

import java.time.LocalDateTime;

public interface OrderStatisticsRepository {
    OrderStatisticsDTO getDailyOrderStatistics(LocalDateTime dateTime);

    DailyDetailDTO getDetailByRange(LocalDateTime start, LocalDateTime end);

    double getRevenueByRange(LocalDateTime start, LocalDateTime end);
}