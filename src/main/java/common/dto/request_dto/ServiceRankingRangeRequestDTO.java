package common.dto.request_dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public class ServiceRankingRangeRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDateTime start;
    private LocalDateTime end;
    private Integer topN;

    public ServiceRankingRangeRequestDTO() {
    }

    public ServiceRankingRangeRequestDTO(LocalDateTime start, LocalDateTime end, Integer topN) {
        this.start = start;
        this.end = end;
        this.topN = topN;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public Integer getTopN() {
        return topN;
    }

    public void setTopN(Integer topN) {
        this.topN = topN;
    }
}