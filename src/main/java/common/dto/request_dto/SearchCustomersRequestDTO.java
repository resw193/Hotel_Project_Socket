package common.dto.request_dto;

import java.io.Serial;
import java.io.Serializable;

public class SearchCustomersRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String keyword;
    private Integer minLoyalty;

    public SearchCustomersRequestDTO() {
    }

    public SearchCustomersRequestDTO(String keyword, Integer minLoyalty) {
        this.keyword = keyword;
        this.minLoyalty = minLoyalty;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getMinLoyalty() {
        return minLoyalty;
    }

    public void setMinLoyalty(Integer minLoyalty) {
        this.minLoyalty = minLoyalty;
    }
}