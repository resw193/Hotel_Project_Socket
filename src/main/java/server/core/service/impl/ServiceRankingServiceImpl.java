package server.core.service.impl;

import common.dto.ServiceRankingDTO;
import server.core.repository.ServiceRankingRepository;
import server.core.service.ServiceRankingService;

import java.time.LocalDateTime;
import java.util.List;

public class ServiceRankingServiceImpl implements ServiceRankingService {

    private final ServiceRankingRepository serviceRankingRepository;

    public ServiceRankingServiceImpl(ServiceRankingRepository serviceRankingRepository) {
        this.serviceRankingRepository = serviceRankingRepository;
    }

    @Override
    public List<ServiceRankingDTO> getByRange(LocalDateTime start, LocalDateTime end) {
        validateRange(start, end);
        return serviceRankingRepository.getByRange(start, end);
    }

    @Override
    public List<ServiceRankingDTO> getTopByRange(LocalDateTime start, LocalDateTime end, int topN) {
        validateRange(start, end);

        if (topN <= 0) {
            throw new IllegalArgumentException("topN phải > 0.");
        }

        return serviceRankingRepository.getTopByRange(start, end, topN);
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