package server.core.service;

import common.dto.BookingTypeRevenueDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingTypeRevenueService {
    List<BookingTypeRevenueDTO> stats(LocalDateTime start, LocalDateTime end);
}