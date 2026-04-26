package server.core.repository;

import common.dto.BookingTypeRevenueDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingTypeRevenueRepository {
    List<BookingTypeRevenueDTO> stats(LocalDateTime start, LocalDateTime end);
}