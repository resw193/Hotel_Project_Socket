package common.dto.request_dto;

import java.io.Serializable;
import java.time.LocalDate;

public class DashboardOverviewRequestDTO implements Serializable {
    private LocalDate today;
    private int daysAhead;
    private int topN;

    public DashboardOverviewRequestDTO() {
    }

    public DashboardOverviewRequestDTO(LocalDate today, int daysAhead, int topN) {
        this.today = today;
        this.daysAhead = daysAhead;
        this.topN = topN;
    }

    public LocalDate getToday() {
        return today;
    }

    public void setToday(LocalDate today) {
        this.today = today;
    }

    public int getDaysAhead() {
        return daysAhead;
    }

    public void setDaysAhead(int daysAhead) {
        this.daysAhead = daysAhead;
    }

    public int getTopN() {
        return topN;
    }

    public void setTopN(int topN) {
        this.topN = topN;
    }
}