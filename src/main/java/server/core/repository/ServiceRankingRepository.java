package server.core.repository;

import common.dto.ServiceRankingDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface ServiceRankingRepository {
    List<ServiceRankingDTO> getByRange(LocalDateTime start, LocalDateTime end);

    List<ServiceRankingDTO> getTopByRange(LocalDateTime start, LocalDateTime end, int topN);
}