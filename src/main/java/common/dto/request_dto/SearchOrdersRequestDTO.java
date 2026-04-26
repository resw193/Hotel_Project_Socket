package common.dto.request_dto;

import java.io.Serial;
import java.io.Serializable;

public class SearchOrdersRequestDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String status;
    private String keyword;

    public SearchOrdersRequestDTO() {
    }

    public SearchOrdersRequestDTO(String status, String keyword) {
        this.status = status;
        this.keyword = keyword;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}