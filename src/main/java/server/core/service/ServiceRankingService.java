package server.core.service;

import common.dto.ServiceRankingDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface ServiceRankingService {
    List<ServiceRankingDTO> getByRange(LocalDateTime start, LocalDateTime end);

    List<ServiceRankingDTO> getTopByRange(LocalDateTime start, LocalDateTime end, int topN);
}