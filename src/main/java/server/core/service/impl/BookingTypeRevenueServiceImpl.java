package server.core.service.impl;

import common.dto.BookingTypeRevenueDTO;
import server.core.repository.BookingTypeRevenueRepository;
import server.core.service.BookingTypeRevenueService;

import java.time.LocalDateTime;
import java.util.List;

public class BookingTypeRevenueServiceImpl implements BookingTypeRevenueService {

    private final BookingTypeRevenueRepository bookingTypeRevenueRepository;

    public BookingTypeRevenueServiceImpl(BookingTypeRevenueRepository bookingTypeRevenueRepository) {
        this.bookingTypeRevenueRepository = bookingTypeRevenueRepository;
    }

    @Override
    public List<BookingTypeRevenueDTO> stats(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Thời gian bắt đầu/kết thúc không được rỗng.");
        }

        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu.");
        }

        return bookingTypeRevenueRepository.stats(start, end);
    }
}